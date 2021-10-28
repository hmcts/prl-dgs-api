package uk.gov.hmcts.reform.prl.documentgenerator.management.monitoring.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class CaseDocumentHealthCheck extends WebServiceHealthCheck {
    @Autowired
    public CaseDocumentHealthCheck(HttpEntityFactory httpEntityFactory,
                                   @Qualifier("healthCheckRestTemplate") RestTemplate restTemplate,
                                   @Value("${case_document_am.url}") String uri) {
        super(httpEntityFactory, restTemplate, uri + "/health");
    }
}
