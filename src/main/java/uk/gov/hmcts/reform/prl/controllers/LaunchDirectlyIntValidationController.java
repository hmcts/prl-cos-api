package uk.gov.hmcts.reform.prl.documentgenerator.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.prl.documentgenerator.service.launchdarkly.LaunchdarklyIntValidationService;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping(path = "/launchdarkly")
public class LaunchDirectlyIntValidationController {

    @Autowired
    private LaunchdarklyIntValidationService launchdarklyIntValidationService;

    @GetMapping
    public ResponseEntity<String> checkLaunchDirectly() {

        return ok(launchdarklyIntValidationService.checkFeatureFlag("test-flag"));
    }
}
