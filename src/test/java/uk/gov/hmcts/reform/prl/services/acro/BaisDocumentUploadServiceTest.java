package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroResponse;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
        when(csvWriter.writeCcdOrderDataToCsv(any(AcroCaseData.class), anyBoolean())).thenReturn(new File("test.csv"));
        when(acroZipService.zip()).thenReturn("/path/to/archive.7z");

        baisDocumentUploadService.uploadFL404Orders();

        verify(systemUserService, Mockito.times(1)).getSysUserToken();
        verify(acroCaseDataService, Mockito.times(1)).getCaseData(eq(AUTHORISATION));
        verify(acroZipService, Mockito.times(1)).zip();
        verify(csvWriter, Mockito.times(1)).writeCcdOrderDataToCsv(any(AcroCaseData.class), anyBoolean());
    }

    @Test
    void shouldUploadBaisDocumentWhenNumberOfCasesInSearchIsOne() throws Exception {
        when(systemUserService.getSysUserToken()).thenReturn(AUTHORISATION);
        AcroCaseData acroCaseData = AcroCaseData.builder()
            .fl404Orders(List.of(OrderDetails.builder().dateCreated(LocalDateTime.now())
                                     .orderDocument(Document.builder().documentUrl("some url").documentBinaryUrl("some binary").build())
                                     .orderDocumentWelsh(Document.builder().documentUrl("some url").documentBinaryUrl("some binary").build())
                                     .build()))
            .build();
        AcroCaseDetail case1 = AcroCaseDetail.builder().id(1L).caseData(acroCaseData).build();
        AcroResponse acroResponse = AcroResponse.builder().total(1).cases(List.of(case1)).build();
        when(acroCaseDataService.getCaseData(AUTHORISATION)).thenReturn(acroResponse);
        when(csvWriter.writeCcdOrderDataToCsv(any(AcroCaseData.class), anyBoolean())).thenReturn(new File("test.csv"));
        when(acroZipService.zip()).thenReturn("/path/to/archive.7z");

        baisDocumentUploadService.uploadFL404Orders();

        verify(systemUserService, Mockito.times(1)).getSysUserToken();
        verify(acroCaseDataService, Mockito.times(1)).getCaseData(eq(AUTHORISATION));
        verify(acroZipService, Mockito.times(1)).zip();
        verify(csvWriter, Mockito.times(1)).writeCcdOrderDataToCsv(any(AcroCaseData.class), anyBoolean());
        verify(pdfExtractorService, Mockito.times(2)).downloadFl404aDocument(
            anyString(), eq(AUTHORISATION), anyString(), any(Document.class));
    }
}
