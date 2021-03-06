package ru.tn.docs.web;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Service;
import com.google.common.io.Resources;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
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
    private static final String DOCS_FILE_PATH = "./docs.html";

    private static final String SERVICE_DISCOVERY_ID = "service-discovery";
    private static final String GRAPH_SVG = "/microservice-graph.svg";

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
    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String buildDocs(@RequestParam(required = false) Boolean cached) {
        if(Boolean.TRUE.equals(cached)) {
            File file = new File(DOCS_FILE_PATH);
            if(file.exists()) {
                return new String(Files.readAllBytes(Paths.get(file.getPath())));
            }
        }

        Map<String, String> services  = consulClient.getAgentServices().getValue().values().stream()
                .filter(service -> hasText(service.getAddress()) && !appName.equals(service.getService()))
                .collect(toMap(Service::getService, service -> "http://" + service.getAddress() + ":" + service.getPort()));

        String serviceDiscoveryUrl = services.getOrDefault(SERVICE_DISCOVERY_ID, "");

        String documentationContent = services.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(SERVICE_DISCOVERY_ID))
                .map(entry -> format(DOCS_ITEM_FORMAT, entry.getValue() + DOCS_URI, entry.getKey()))
                .collect(joining());

        URL resource = this.getClass().getResource("/docs.template");
        String template =  Resources.toString(resource, Charset.forName("UTF-8"));
        String content = template.replace("${contourImgPath}", serviceDiscoveryUrl + GRAPH_SVG)
                .replace("${documentationLinks}", documentationContent);

        try (Writer w = Files.newBufferedWriter(Paths.get(DOCS_FILE_PATH))) {
            w.write(content);
        }
        return content;
    }
}
