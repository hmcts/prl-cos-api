package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.utils.ResourceLoader;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestingSupportFixtureLoader {

    private final Environment environment;

    public String loadJson(String filePath) throws Exception {
        String rawJson = ResourceLoader.loadJson(filePath);
        log.info("Loading TestingSupport placeholders for filepath={} on env={}", filePath, environment.toString());
        String resolved = environment.resolvePlaceholders(rawJson);
        log.info("Loaded TestingSupport placeholders for filepath={} as resolved={}", filePath, resolved);
        return resolved;
    }
}
