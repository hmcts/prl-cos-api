package uk.gov.hmcts.reform.prl.services;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.utils.FixedTime;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static uk.gov.hmcts.reform.prl.utils.ResourceReader.readBytes;

@RunWith(MockitoJUnitRunner.class)
public class AmendedOrderStamperTest {

    @Mock
    private CaseDocumentClient caseDocumentClient;

    @Mock
    private AuthTokenGenerator authTokenGenerator;


    private static final LocalDate FIXED_DATE = LocalDate.of(420, 6, 9);
    private static final String BINARY_URL = "binary url";
    private static final String FILE_NAME = "order.pdf";
    private static final String AUTH = "auth";


    @InjectMocks
    private final AmendedOrderStamper stamper = new AmendedOrderStamper(
        caseDocumentClient, authTokenGenerator, new FixedTime(LocalDateTime.of(FIXED_DATE, LocalTime.MIDNIGHT)));


    @Before
    public void init() {
        //        when(authTokenGenerator.generate()).thenReturn("s2s");
    }

    @Test
    public void amendedPdf() throws IOException {
        final byte[] inputBinaries = readBytes("documents/document.pdf");
        final byte[] outputBinaries = readBytes("documents/document-amended.pdf");

        Document inputPdf = Document.builder()
            .documentFileName(FILE_NAME)
            .documentBinaryUrl(BINARY_URL)
            .build();
        //
        //        ResponseEntity<Resource> response = ResponseEntity.of(Optional.ofNullable(inputBinaries))
        //
        //        when(caseDocumentClient.getDocumentBinary(AUTH, "s2s", BINARY_URL)).thenReturn(response);
        //
        //        byte[] amendedPDF = stamper.amendDocument(inputPDF, AUTH);
        //
        //        assertThat(amendedPDF).isEqualTo(outputBinaries);
    }


}
