package ru.tn.error;

import org.springframework.context.annotation.Import;
import ru.tn.error.config.ErrorHandlerConfiguration;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@Import(ErrorHandlerConfiguration.class)
public @interface EnableErrorHandle {
}
