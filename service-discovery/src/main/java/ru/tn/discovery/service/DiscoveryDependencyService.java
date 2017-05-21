package ru.tn.discovery.service;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.agent.model.Service;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.google.common.io.Resources;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import ru.tn.discovery.model.ServiceDescription;
import rx.Observable;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * @author dsnimshchikov on 11.05.17.
 */
@Slf4j
@Configuration
public class DiscoveryDependencyService {
    private static final String GATEWAY_SERVICE_KEY = "gateway/service/";

    @Autowired
    public AsciiDocConverterService adocConverterService;
    @Autowired
    private ConsulClient consulClient;

    @Value("classpath:microservice-graph.adoc")
    private Resource microserviceTemplate;

    /**
     * Вызывается при изменении пути gateway/service в KV Consul.
     * Собирает данные о зависимостях из Consul и создает ADoc файл
     * Вызывает asciiDoctor для генерации SVG файла
     */
    @EventListener(ContextRefreshedEvent.class)
    public void contextCreateEventListener() {
        List<ServiceDescription> binaryLink = new ArrayList<>();
        List<Map.Entry<String, Service>> gatewayServices = buildDependenciesList(binaryLink);
        //todo формировать файл .adoc с графом сервисов
        System.out.println(gatewayServices);
        buildGraphAdoc(binaryLink);
    }

    private List<Map.Entry<String, Service>> buildDependenciesList(List<ServiceDescription> binaryLink) {
        List<Map.Entry<String, Service>> gatewayServices = consulClient.getAgentServices().getValue().entrySet().stream()
                .filter(m -> m.getValue().getTags().contains("GATEWAY_SERVICE"))
                .collect(toList());
        for (Map.Entry<String, Service> entry : gatewayServices) {
            String serviceName = entry.getValue().getService();
            List<GetValue> serviceDependencies = consulClient.getKVValues(GATEWAY_SERVICE_KEY + serviceName + "/dependency").getValue();
            List<ServiceDescription> dependencies = new ArrayList<>();
            nullableListToStream(serviceDependencies).forEach(value -> {
                String[] serviceNameUrl = StringUtils.split(value.getDecodedValue(), "|");
                if (serviceNameUrl.length == 2) {
                    dependencies.add(ServiceDescription.builder()
                            .name(serviceNameUrl[0])
                            .baseUrls(singletonList(serviceNameUrl[1]))
                            .build());
                }
            });
            GetValue pathValue = consulClient.getKVValue(GATEWAY_SERVICE_KEY + serviceName + "/path").getValue();
            String servicePath = pathValue == null ? "" : pathValue.getDecodedValue();

            binaryLink.add(ServiceDescription.builder()
                    .name(serviceName)
                    .baseUrls(singletonList(servicePath))
                    .innerLink(true)
                    .dependencies(dependencies)
                    .build());
        }
        return gatewayServices;
    }

    static Stream<GetValue> nullableListToStream(List<GetValue> coll) {
        return Optional.ofNullable(coll)
                .map(Collection::stream)
                .orElseGet(Stream::empty);
    }

    /**
     * Создаем описание сервиса. Его заголовок и все публичные url
     *
     * @param serviceName Имя сервиса
     * @param endPoints   Корневые URL по которым мы можем взаимодействовать с сервисом
     * @return описание сервиса
     */
    public String makeGraphVertex(String serviceName, List<String> endPoints) {
        String result = "\"" + serviceName + "\"         [label=\"<h>" + serviceName;
        for (String link : endPoints) {
            result += "| <" + link + ">" + link + "\\l";
        }
        return result + "\"];\n";
    }

    /**
     * Создание одной строки связи сервисов. В случае если А ссылается на Б и Б на А, то таких строк будет 2.
     * :h означает что стрелка связи будет идти к заголовку
     *
     * @param primaryServiceName сервис А откуда идет связь
     * @param dependsServices    сервисы на который ссылаются
     * @return строку для graphvith
     */
    private String makeUnaryLink(String primaryServiceName, List<ServiceDescription> dependsServices) {
        return dependsServices.stream()
                .map(s -> makeUnaryLink("\"" + primaryServiceName + "\"", "\"" + s.getName() + "\""))
                .collect(joining("\n"));
    }

    private String makeUnaryLink(String primaryServiceName, String dependsServiceName) {
        return primaryServiceName + ":h -> " + dependsServiceName + ":h ";
    }

    private String makeUnaryLink(String primaryServiceName, String dependsServiceName, String dependsLinkName) {
        return primaryServiceName + " -> " + dependsServiceName + ":" + dependsLinkName;
    }

    private String makeUnaryLink(String primaryServiceName, String primaryLinkName, String dependsServiceName, String dependsLinkName) {
        return primaryServiceName + ":" + primaryLinkName + " -> " + dependsServiceName + ":" + dependsLinkName;
    }

    private String makeUnaryLink(List<ServiceDescription> descriptions, String serviceName) {
        return descriptions.stream().filter(s -> Objects.equals(s.getName(), serviceName))
                .flatMap(s -> s.getDependencies().stream())
                .map(d -> makeUnaryLink(serviceName, d.getName()))
                .collect(joining("\n"));
    }

    @SneakyThrows
    public void buildGraphAdoc(List<ServiceDescription> binaryLink) {
        StringBuilder serviceDescriptions = new StringBuilder();
        StringBuilder serviceDepends = new StringBuilder();
        String graphTemplate = Resources.toString(microserviceTemplate.getURL(), UTF_8);

        binaryLink.forEach(l ->
                serviceDescriptions.append(makeGraphVertex(l.getName(), l.getBaseUrls())));

        Observable.from(binaryLink).distinct(ServiceDescription::getName)
                .filter(l -> l.getDependencies() != null)
                .forEach(l -> serviceDepends.append(
                        makeUnaryLink(l.getName(), l.getDependencies())));

        graphTemplate = graphTemplate.replace("include::serviceDescriptions", serviceDescriptions);
        graphTemplate = graphTemplate.replace("include::serviceDepends", serviceDepends);
        try {
            File resultAdoc = saveFile(graphTemplate, "microservice-graph", ".adoc");
            convertToSVG(resultAdoc);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void convertToSVG(File fileAdoc) throws IOException {
        File htmlFile = saveFile(adocConverterService.convertFile(fileAdoc), "microservice-graph", ".html");
        log.info("make new graph microservices in path:" + htmlFile.getAbsolutePath());
    }

    private File saveFile(String content, String fileName, String fileExtensions) throws IOException {
        File file = new File(fileName + fileExtensions);
        try (Writer w = Files.newBufferedWriter(Paths.get(fileName + fileExtensions))) {
            w.write(content);
        }
        return file;
    }

}
