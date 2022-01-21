package uk.gov.hmcts.reform.prl.documentgenerator.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.documentgenerator.service.impl.TestServiceImpl;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(path = "/test")
public class TestController {

    @Autowired
    TestServiceImpl testService;

    @GetMapping
    public ResponseEntity<String> validateSecret() {

        return ok(testService.getSecretValue());
    }
}
