package ru.tn.discovery.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author dsnimshchikov on 11.05.17.
 */
@Builder
@Data
public class ServiceDescription {

    String name;
    List<String> baseUrls;
    //Нужен в случае когда связь идет не от url, к примеру задание по расписанию
    Boolean innerLink;
    List<ServiceDescription> dependencies;
}
