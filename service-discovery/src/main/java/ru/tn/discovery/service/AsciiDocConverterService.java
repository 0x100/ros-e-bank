package ru.tn.discovery.service;

import org.asciidoctor.Asciidoctor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.asciidoctor.Asciidoctor.Factory.create;
import static org.asciidoctor.OptionsBuilder.options;

/**
 * @author dsnimshchikov on 14.05.17.
 */
@Component
public class AsciiDocConverterService {

    Asciidoctor asciidoctor = create();

    public String convertFile(File inputAdoc) throws IOException {
//        Map<String, Object> attributes = new HashMap<>();
//        attributes.put("dot", "/usr/bin/dot");
        String content = new String(Files.readAllBytes(Paths.get(inputAdoc.getAbsolutePath())));
        asciidoctor.requireLibrary("asciidoctor-diagram");
        return asciidoctor.render(content, options().asMap());
    }

}
