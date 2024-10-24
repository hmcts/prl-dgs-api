package uk.gov.hmcts.reform.prl.documentgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(
    scanBasePackages = {"uk.gov.hmcts.reform.prl",
        "uk.gov.hmcts.reform.logging.appinsights",
        "uk.gov.hmcts.reform.ccd.document"}
    )
public class DocumentGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentGeneratorApplication.class, args);
    }
}
