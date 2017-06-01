package ru.tn.docs.web;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Service;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static java.text.MessageFormat.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StringUtils.hasText;


@RestController
@RequestMapping("/docs")
public class DocsController {
    private static final String DOCS_URI = "/docs/index.html";
    private static final String DOCS_ITEM_FORMAT = "<li><a href=\"{0}\" target=\"_blank\">{1}</a></li>";
    public static final String DOCS_FILE_PATH = "./docs-aggregator/src/main/resources/docs.html";

    private static final String SERVICE_DISCOVERY_ID = "service-discovery";

    private final ConsulClient consulClient;

    @Value("${spring.application.name}")
    private String appName;

    @Value("classpath:docs.template")
    private Resource docsTemplate;

    @Autowired
    public DocsController(ConsulClient consulClient) {
        this.consulClient = consulClient;
    }


    @SneakyThrows
    @RequestMapping(method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String buildDocs(@RequestParam(required = false) Boolean rebuild) {
        if(Boolean.TRUE.equals(rebuild)) {
            File file = new File(DOCS_FILE_PATH);
            if(file.exists()) {
                return new String(Files.readAllBytes(Paths.get(file.getPath())));
            }
        }

        Map<String, String> services  = consulClient.getAgentServices().getValue().values().stream()
                .filter(service -> hasText(service.getAddress()) && !appName.equals(service.getId()))
                .collect(toMap(Service::getService, service -> "http://" + service.getAddress() + ":" + service.getPort()));

        String serviceDiscoveryUrl = services.getOrDefault(SERVICE_DISCOVERY_ID, "");

        String documentationContent = services.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(SERVICE_DISCOVERY_ID))
                .map(entry -> format(DOCS_ITEM_FORMAT, entry.getValue() + DOCS_URI, entry.getKey()))
                .collect(joining());

        String template = new String(Files.readAllBytes(Paths.get(docsTemplate.getURI())));
        String content = template.replace("${contourImgPath}", serviceDiscoveryUrl + "/microservice-graph.svg")
                .replace("${documentationLinks}", documentationContent);

        try (Writer w = Files.newBufferedWriter(Paths.get(DOCS_FILE_PATH))) {
            w.write(content);
        }
        return content;
    }
}
