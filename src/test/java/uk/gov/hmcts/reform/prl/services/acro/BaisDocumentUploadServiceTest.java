package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class BaisDocumentUploadServiceTest {

    private static final String AUTH_TOKEN = "AuthToken";
    private static final String CASE_ID = "1";

    @InjectMocks
    private BaisDocumentUploadService service;

    @Mock private SystemUserService systemUserService;
    @Mock private AcroCaseDataService acroCaseDataService;
    @Mock private AcroZipService acroZipService;
    @Mock private CsvWriter csvWriter;
    @Mock private PdfExtractorService pdfExtractorService;

    private File tempCsv;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(service, "sourceDirectory", System.getProperty("java.io.tmpdir") + "/acro-source");
        ReflectionTestUtils.setField(service, "outputDirectory", System.getProperty("java.io.tmpdir") + "/acro-output");

        tempCsv = File.createTempFile("test", ".csv");

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(csvWriter.createCsvFileWithHeaders()).thenReturn(tempCsv);
        when(acroZipService.zip()).thenReturn("/path/to/archive.7z");
    }

    @Test
    void shouldCreateEmptyCsvWhenNoCasesFound() throws Exception {
        when(acroCaseDataService.getCaseData(AUTH_TOKEN)).thenReturn(emptyResponse());

        service.uploadFL404Orders();

        verify(csvWriter).appendCsvRowToFile(eq(tempCsv), any(AcroCaseData.class), eq(false), eq(null));
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("documentProcessingScenarios")
    void shouldProcessDocumentsBasedOnAvailability(int successfulDownloads, String scenarioDescription) throws Exception {
        when(acroCaseDataService.getCaseData(AUTH_TOKEN)).thenReturn(responseWithOneCase());

        File englishFile = successfulDownloads >= 1 ? File.createTempFile("english", ".pdf") : null;
        File welshFile = successfulDownloads >= 2 ? File.createTempFile("welsh", ".pdf") : null;

        when(pdfExtractorService.downloadPdf(anyString(), eq(CASE_ID), any(Document.class), eq(AUTH_TOKEN)))
            .thenReturn(englishFile, welshFile);

        service.uploadFL404Orders();

        verify(csvWriter, Mockito.times(successfulDownloads))
            .appendCsvRowToFile(eq(tempCsv), any(AcroCaseData.class), eq(true), anyString());
        verify(pdfExtractorService, Mockito.times(2))
            .downloadPdf(anyString(), eq(CASE_ID), any(Document.class), eq(AUTH_TOKEN));
    }

    static Stream<Arguments> documentProcessingScenarios() {
        return Stream.of(
            Arguments.of(0, "No PDFs download successfully"),
            Arguments.of(1, "Only English PDF downloads successfully"),
            Arguments.of(1, "Both English and Welsh PDFs download successfully")
        );
    }

    private AcroResponse emptyResponse() {
        return AcroResponse.builder().total(0).cases(null).build();
    }

    private AcroResponse responseWithOneCase() {
        return AcroResponse.builder()
            .total(1)
            .cases(List.of(AcroCaseDetail.builder()
                .id(1L)
                .caseData(AcroCaseData.builder()
                    .fl404Orders(List.of(OrderDetails.builder()
                        .dateCreated(LocalDateTime.now())
                        .orderDocument(createDocument())
                        .orderDocumentWelsh(createDocument())
                        .build()))
                    .build())
                .build()))
            .build();
    }

    private Document createDocument() {
        return Document.builder()
            .documentUrl("url")
            .documentBinaryUrl("binary")
            .build();
    }
}
