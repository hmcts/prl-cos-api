package uk.gov.hmcts.reform.prl.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.hmcts.reform.prl.Application;
import uk.gov.hmcts.reform.prl.IntegrationTest;
import uk.gov.hmcts.reform.prl.util.CosApiClient;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;

/**
 * Built-in feature which saves service's swagger specs in temporary directory.
 * Each travis run on master should automatically save and upload (if updated) documentation.
 */
@Slf4j
@SpringBootTest(classes = {Application.class,SwaggerPublisherTest.class})
public class SwaggerPublisherTest extends IntegrationTest {

    @Autowired
    private CosApiClient cosApiClient;

    @DisplayName("Generate swagger documentation")
    @Test
    public void generateDocs() throws Exception {
        byte[] specs = cosApiClient.apiDocs();

        assertThat(specs.length, is(greaterThan(0)));

        Path swaggerSpecPath = Paths.get(System.getProperty("java.io.tmpdir"), "swagger-specs.json");

        log.info("Writing swagger specification to {}", swaggerSpecPath.toAbsolutePath());
        try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(swaggerSpecPath))) {
            outputStream.write(specs);
        }
    }
}
