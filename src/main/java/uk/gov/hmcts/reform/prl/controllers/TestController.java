package uk.gov.hmcts.reform.prl.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.services.TestService;

import static org.springframework.http.ResponseEntity.ok;

@RestController
public class TestController {

    @Autowired
    TestService testService;

    @GetMapping("/test")
    public ResponseEntity<String> welcome() {

        return ok(testService.testMethod());
    }
}
