package uk.gov.hmcts.reform.prl.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    @Value("${test.secret}")
    private String testSecret;

    public String testMethod() {
        return testSecret;
    }
}
