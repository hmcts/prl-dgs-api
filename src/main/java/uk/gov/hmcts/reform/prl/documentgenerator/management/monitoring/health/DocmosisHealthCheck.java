package uk.gov.hmcts.reform.prl.documentgenerator.management.monitoring.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DocmosisHealthCheck extends WebServiceHealthCheck {
    @Autowired
    public DocmosisHealthCheck(HttpEntityFactory httpEntityFactory,
                               @Qualifier("healthCheckRestTemplate") RestTemplate restTemplate,
                               @Value("${docmosis.service.pdf-service.health}") String uri) {
        super(httpEntityFactory, restTemplate, uri);
    }
}
