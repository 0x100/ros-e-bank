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

    public static final String GRAPTHVIZ_MODULE = "asciidoctor-diagram";
    Asciidoctor asciidoctor;

    public AsciiDocConverterService() {
        asciidoctor = create();
        asciidoctor.requireLibrary(GRAPTHVIZ_MODULE);
    }

    public String convertFile(File inputAdoc) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(inputAdoc.getAbsolutePath())));
        return asciidoctor.render(content, options()
                .mkDirs(true));
    }

}
