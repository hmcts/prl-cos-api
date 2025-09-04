package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroResponse;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
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

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
            baisDocumentUploadService,
            "sourceDirectory",
            System.getProperty("java.io.tmpdir") + "/acro-source"
        );
        ReflectionTestUtils.setField(
            baisDocumentUploadService,
            "outputDirectory",
            System.getProperty("java.io.tmpdir") + "/acro-output"
        );
    }

    @Test
    void shouldUploadBaisDocumentWhenNumberOfCasesInSearchIsZero() throws Exception {
        when(systemUserService.getSysUserToken()).thenReturn(AUTHORISATION);
        AcroResponse acroResponse = AcroResponse.builder().total(0).cases(null).build();
        when(acroCaseDataService.getCaseData(AUTHORISATION)).thenReturn(acroResponse);
        File tempCsv = File.createTempFile("test", ".csv");
        when(csvWriter.createCsvFileWithHeaders()).thenReturn(tempCsv);
        when(acroZipService.zip()).thenReturn("/path/to/archive.7z");

        baisDocumentUploadService.uploadFL404Orders();

        verify(systemUserService, Mockito.times(1)).getSysUserToken();
        verify(acroCaseDataService, Mockito.times(1)).getCaseData(eq(AUTHORISATION));
        verify(acroZipService, Mockito.times(1)).zip();
        verify(csvWriter, Mockito.times(1)).createCsvFileWithHeaders();
        verify(csvWriter, Mockito.times(1)).appendCsvRowToFile(
            eq(tempCsv),
            any(AcroCaseData.class),
            eq(false),
            eq(null)
        );
    }

    //@Test
    void shouldUploadBaisDocumentWhenNumberOfCasesInSearchIsOne() throws Exception {
        when(systemUserService.getSysUserToken()).thenReturn(AUTHORISATION);
        AcroCaseData acroCaseData = AcroCaseData.builder()
            .fl404Orders(List.of(OrderDetails.builder().dateCreated(LocalDateTime.now())
                                     .orderDocument(Document.builder().documentUrl("some url").documentBinaryUrl(
                                         "some binary").build())
                                     .orderDocumentWelsh(Document.builder().documentUrl("some url").documentBinaryUrl(
                                         "some binary").build())
                                     .build()))
            .build();
        AcroCaseDetail case1 = AcroCaseDetail.builder().id(1L).caseData(acroCaseData).build();
        AcroResponse acroResponse = AcroResponse.builder().total(1).cases(List.of(case1)).build();
        when(acroCaseDataService.getCaseData(AUTHORISATION)).thenReturn(acroResponse);
        File tempCsv = File.createTempFile("test", ".csv");
        when(csvWriter.createCsvFileWithHeaders()).thenReturn(tempCsv);
        when(acroZipService.zip()).thenReturn("/path/to/archive.7z");

        when(pdfExtractorService.downloadPdf(anyString(), anyString(), any(Document.class), anyString()))
            .thenReturn(null);

        baisDocumentUploadService.uploadFL404Orders();

        verify(systemUserService, Mockito.times(1)).getSysUserToken();
        verify(acroCaseDataService, Mockito.times(1)).getCaseData(eq(AUTHORISATION));
        verify(acroZipService, Mockito.times(1)).zip();
        verify(csvWriter, Mockito.times(1)).createCsvFileWithHeaders();
        verify(csvWriter, Mockito.times(1)).appendCsvRowToFile(eq(tempCsv), eq(acroCaseData), eq(true), eq(null));
        verify(pdfExtractorService, Mockito.times(2)).downloadPdf(
            anyString(), anyString(), any(Document.class), anyString());
    }

    //@Test
    void shouldCopyDownloadedFilesWhenPresentAndExists() throws Exception {
        when(systemUserService.getSysUserToken()).thenReturn(AUTHORISATION);
        File tempCsv = File.createTempFile("test", ".csv");
        when(csvWriter.createCsvFileWithHeaders()).thenReturn(tempCsv);
        when(acroZipService.zip()).thenReturn("/path/to/archive.7z");

        File englishFile = File.createTempFile("english", ".pdf");
        File welshFile = File.createTempFile("welsh", ".pdf");
        AcroCaseData acroCaseData = AcroCaseData.builder()
            .fl404Orders(List.of(OrderDetails.builder().dateCreated(LocalDateTime.now())
                                     .orderDocument(Document.builder().documentUrl("url").documentBinaryUrl("binary").build())
                                     .orderDocumentWelsh(Document.builder().documentUrl("url").documentBinaryUrl(
                                         "binary").build())
                                     .build()))
            .build();
        AcroCaseDetail case1 = AcroCaseDetail.builder().id(1L).caseData(acroCaseData).build();
        AcroResponse acroResponse = AcroResponse.builder().total(1).cases(List.of(case1)).build();
        when(acroCaseDataService.getCaseData(AUTHORISATION)).thenReturn(acroResponse);
        when(pdfExtractorService.downloadPdf(anyString(), anyString(), any(Document.class), anyString()))
            .thenReturn(englishFile)
            .thenReturn(welshFile);

        baisDocumentUploadService.uploadFL404Orders();

        verify(csvWriter, Mockito.times(1)).createCsvFileWithHeaders();
        verify(csvWriter, Mockito.times(2)).appendCsvRowToFile(eq(tempCsv), eq(acroCaseData), eq(true), anyString());
        verify(pdfExtractorService, Mockito.times(2))
            .downloadPdf(anyString(), anyString(), any(Document.class), anyString());
    }

    //@Test
    void shouldLogErrorWhenFileCopyThrowsIoException() throws Exception {
        when(systemUserService.getSysUserToken()).thenReturn(AUTHORISATION);
        File tempCsv = File.createTempFile("test", ".csv");
        when(csvWriter.createCsvFileWithHeaders()).thenReturn(tempCsv);
        when(acroZipService.zip()).thenReturn("/path/to/archive.7z");

        File englishFile = File.createTempFile("english", ".pdf");
        AcroCaseData acroCaseData = AcroCaseData.builder()
            .fl404Orders(List.of(OrderDetails.builder().dateCreated(LocalDateTime.now())
                                     .orderDocument(Document.builder().documentUrl("url").documentBinaryUrl("binary").build())
                                     .orderDocumentWelsh(Document.builder().documentUrl("url").documentBinaryUrl(
                                         "binary").build())
                                     .build()))
            .build();
        AcroCaseDetail case1 = AcroCaseDetail.builder().id(1L).caseData(acroCaseData).build();
        AcroResponse acroResponse = AcroResponse.builder().total(1).cases(List.of(case1)).build();
        when(acroCaseDataService.getCaseData(AUTHORISATION)).thenReturn(acroResponse);
        when(pdfExtractorService.downloadPdf(anyString(), anyString(), any(Document.class), anyString()))
            .thenReturn(englishFile)
            .thenReturn(null);

        baisDocumentUploadService.uploadFL404Orders();

        verify(csvWriter, Mockito.times(1)).createCsvFileWithHeaders();
        verify(csvWriter, Mockito.times(1)).appendCsvRowToFile(eq(tempCsv), eq(acroCaseData), eq(true), anyString());
        verify(pdfExtractorService, Mockito.times(2))
            .downloadPdf(anyString(), anyString(), any(Document.class), anyString());
    }
}
