package uk.gov.hmcts.reform.prl;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import uk.gov.hmcts.reform.prl.exception.PdfConversionException;
import uk.gov.hmcts.reform.prl.services.document.pdf.PdfGenerationRequest;
import uk.gov.hmcts.reform.prl.services.document.pdf.PdfGenerationService;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
    "docmosis.url=http://localhost:${wiremock.server.port}"
})
@AutoConfigureWireMock(port = 0)
public class PdfGenerationServiceIntegrationTest {

    @Autowired
    private PdfGenerationService pdfGenerationService;

    @BeforeEach
    public void setup() {
        WireMock.reset();
    }

    @Test
    public void shouldRetry3TimesAndThrowExceptionOn502() {
        stubFor(post(urlPathEqualTo("/rs/convert"))
                    .willReturn(aResponse()
                                    .withStatus(502)
                                    .withHeader("Content-Type", "application/json")));

        PdfGenerationRequest request = PdfGenerationRequest.builder()
            .caseId("1234567890123456")
            .authToken("authToken")
            .sourceFilename("test.docx")
            .fileContent("content".getBytes())
            .build();

        assertThrows(PdfConversionException.class, () -> pdfGenerationService.generateAndStore(request));

        WireMock.verify(3, postRequestedFor(urlPathEqualTo("/rs/convert")));
    }
}

