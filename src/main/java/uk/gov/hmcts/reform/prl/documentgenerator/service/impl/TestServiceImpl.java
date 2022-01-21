package uk.gov.hmcts.reform.prl.documentgenerator.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TestServiceImpl {
    @Value("${test.secret}")
    private String testSecret;

    public String getSecretValue() {
        return testSecret;
    }
}
