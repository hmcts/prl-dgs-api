package uk.gov.hmcts.reform.prl.documentgenerator.config;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.Map;


@Getter
@Setter
@Component
@Validated
@ConfigurationProperties()
public class TemplateConfig {
    public static final String RELATION = "relation";
    public static final String MONTHS = "months";
    @Valid
    private Map<String, Map<LanguagePreference,  Map<String, String>>> template;
}
