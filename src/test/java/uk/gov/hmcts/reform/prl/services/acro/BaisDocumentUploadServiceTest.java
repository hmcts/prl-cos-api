package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroResponse;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.io.File;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BaisDocumentUploadServiceTest {

    public static final String AUTHORISATION = "AuthToken";
    @InjectMocks
    private BaisDocumentUploadService baisDocumentUploadService;

    @Mock
    private SystemUserService systemUserService;
    @Mock
    private AcroCaseDataService acroCaseDataService;
    @Mock
    private AcroZipService acroZipService;
    @Mock
    private CsvWriter csvWriter;
    @Mock
    private PdfExtractorService pdfExtractorService;

    @Test
    void shouldUploadBaisDocumentWhenNumberOfCasesInSearchIsZero() throws Exception {

        when(systemUserService.getSysUserToken()).thenReturn(AUTHORISATION);
        AcroResponse acroResponse = AcroResponse.builder().total(0).cases(null).build();
        when(acroCaseDataService.getCaseData(AUTHORISATION)).thenReturn(acroResponse);

        baisDocumentUploadService.uploadFL404Orders();

        verify(systemUserService, Mockito.times(1)).getSysUserToken();
        verify(acroCaseDataService, Mockito.times(1)).getCaseData(AUTHORISATION);
        verify(acroZipService, Mockito.times(1)).zip(any(File.class), any(File.class));
        verify(csvWriter, Mockito.times(1)).writeCcdOrderDataToCsv(any(AcroCaseData.class), anyBoolean());
        verify(pdfExtractorService, Mockito.times(1)).downloadFl404aDocument(
            anyString(), AUTHORISATION, anyString(), any(
                Document.class)
        );
    }
}
