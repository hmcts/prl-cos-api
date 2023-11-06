package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.services.time.Time;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AmendedOrderStamperTest {

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private Time time;

    @InjectMocks
    private AmendedOrderStamper stamper;

    private static final LocalDate FIXED_DATE = LocalDate.of(420, 6, 9);
    private static final String BINARY_URL = "binary url";
    private static final String FILE_NAME = "order.pdf";
    private static final String AUTH = "auth";



    @Before
    public void init() {
        when(authTokenGenerator.generate()).thenReturn("s2s");
        when(time.now()).thenReturn(LocalDateTime.of(FIXED_DATE, LocalTime.now()));
    }

    @Test
    public void verifyAmendedPdfHasCorrectStamping() throws IOException {
        byte[] outputBinaries = new ClassPathResource("documents/document-amended.pdf").getInputStream().readAllBytes();
        Document inputPdf = Document.builder()
            .documentFileName(FILE_NAME)
            .documentBinaryUrl(BINARY_URL)
            .documentUrl(BINARY_URL)
            .build();

        Resource expectedResource = new ClassPathResource("documents/document.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, HttpStatus.OK);


        when(caseDocumentClient.getDocumentBinary(AUTH, "s2s", BINARY_URL)).thenReturn(expectedResponse);
        byte[] amendedPdf = stamper.amendDocument(inputPdf, AUTH);
        assertThat(amendedPdf).isEqualTo(outputBinaries);
    }

}
