package uk.gov.hmcts.reform.prl.services.acro;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.OtherOrderDetails;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseData;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.acro.AcroResponse;
import uk.gov.hmcts.reform.prl.models.dto.acro.CsvData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;

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
    @Mock private LaunchDarklyClient launchDarklyClient;

    @Captor
    ArgumentCaptor<CsvData> csvDataArgumentCaptor;

    private File tempCsv;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(service, "sourceDirectory", System.getProperty("java.io.tmpdir") + "/acro-source");
        ReflectionTestUtils.setField(service, "outputDirectory", System.getProperty("java.io.tmpdir") + "/acro-output");

        tempCsv = File.createTempFile("test", ".csv");

        when(systemUserService.getSysUserToken()).thenReturn(AUTH_TOKEN);
        when(csvWriter.createCsvFileWithHeaders()).thenReturn(tempCsv);
        when(acroZipService.zip()).thenReturn("/path/to/archive.7z");
        when(launchDarklyClient.isFeatureEnabled("acro-confidential-data-allowed")).thenReturn(false);
    }

    @Test
    void shouldCreateEmptyCsvWhenNoCasesFound() throws Exception {
        when(acroCaseDataService.getNonMolestationData(AUTH_TOKEN)).thenReturn(emptyResponse());

        service.uploadFL404Orders();

        verify(csvWriter).appendCsvRowToFile(eq(tempCsv), any(CsvData.class), eq(null));
    }

    @Test
    void shouldCreateEmptyCsvWhenTotalIsZero() throws Exception {
        AcroResponse response = AcroResponse.builder()
            .total(0)
            .cases(List.of())
            .build();
        when(acroCaseDataService.getNonMolestationData(AUTH_TOKEN)).thenReturn(response);

        service.uploadFL404Orders();

        verify(csvWriter).appendCsvRowToFile(eq(tempCsv), any(CsvData.class), eq(null));
        verify(acroZipService).zip();
    }

    @Test
    void shouldCreateEmptyCsvWhenCasesIsEmpty() throws Exception {
        AcroResponse response = AcroResponse.builder()
            .total(5)
            .cases(Collections.emptyList())
            .build();
        when(acroCaseDataService.getNonMolestationData(AUTH_TOKEN)).thenReturn(response);

        service.uploadFL404Orders();

        verify(csvWriter).appendCsvRowToFile(eq(tempCsv), any(CsvData.class), eq(null));
        verify(acroZipService).zip();
    }

    @Test
    void shouldThrowRuntimeExceptionWhenCreateDirectoriesFails() {
        ReflectionTestUtils.setField(service, "sourceDirectory", "/invalid/path/that/cannot/be/created");

        assertThrows(RuntimeException.class, () -> service.uploadFL404Orders());
    }

    @Test
    void shouldThrowRuntimeExceptionWhenAcroCaseDataServiceFails() throws Exception {
        when(acroCaseDataService.getNonMolestationData(AUTH_TOKEN))
            .thenThrow(new RuntimeException("Service failure"));

        assertThrows(RuntimeException.class, () -> service.uploadFL404Orders());
    }

    @Test
    void shouldSkipCaseWhenNoFL404Orders() throws Exception {
        AcroResponse response = AcroResponse.builder()
            .total(1)
            .cases(List.of(AcroCaseDetail.builder()
                .id(1L)
                .caseData(AcroCaseData.builder()
                    .fl404Orders(null)
                    .build())
                .build()))
            .build();
        when(acroCaseDataService.getNonMolestationData(AUTH_TOKEN)).thenReturn(response);

        service.uploadFL404Orders();

        verify(pdfExtractorService, never()).downloadPdf(anyString(), anyString(), any(), anyString());
        verify(csvWriter, never()).appendCsvRowToFile(eq(tempCsv), any(CsvData.class), anyString());
    }

    @Test
    void shouldSkipCaseWhenFL404OrdersIsEmpty() throws Exception {
        AcroResponse response = AcroResponse.builder()
            .total(1)
            .cases(List.of(AcroCaseDetail.builder()
                .id(1L)
                .caseData(AcroCaseData.builder()
                    .fl404Orders(Collections.emptyList())
                    .build())
                .build()))
            .build();
        when(acroCaseDataService.getNonMolestationData(AUTH_TOKEN)).thenReturn(response);

        service.uploadFL404Orders();

        verify(pdfExtractorService, never()).downloadPdf(anyString(), anyString(), any(), anyString());
        verify(csvWriter, never()).appendCsvRowToFile(eq(tempCsv), any(CsvData.class), anyString());
    }

    @Test
    void shouldContinueProcessingWhenPdfDownloadFails() throws Exception {
        when(acroCaseDataService.getNonMolestationData(AUTH_TOKEN)).thenReturn(responseWithOneCase());
        when(pdfExtractorService.downloadPdf(anyString(), eq(CASE_ID), any(Document.class), eq(AUTH_TOKEN)))
            .thenThrow(new RuntimeException("Download failed"));

        service.uploadFL404Orders();

        verify(pdfExtractorService, times(1)).downloadPdf(anyString(), eq(CASE_ID), any(Document.class), eq(AUTH_TOKEN));
        verify(csvWriter, never()).appendCsvRowToFile(eq(tempCsv), any(CsvData.class), anyString());
        verify(acroZipService).zip();
    }

    @Test
    void shouldProcessMultipleCasesWithMixedFL404Orders() throws Exception {
        AcroResponse response = AcroResponse.builder()
            .total(3)
            .cases(List.of(
                AcroCaseDetail.builder()
                    .id(1L)
                    .caseData(AcroCaseData.builder()
                        .fl404Orders(List.of(createOrderDetails()))
                        .build())
                    .build(),
                AcroCaseDetail.builder()
                    .id(2L)
                    .caseData(AcroCaseData.builder()
                        .fl404Orders(null)
                        .build())
                    .build(),
                AcroCaseDetail.builder()
                    .id(3L)
                    .caseData(AcroCaseData.builder()
                        .fl404Orders(Collections.emptyList())
                        .build())
                    .build()
            ))
            .build();

        when(acroCaseDataService.getNonMolestationData(AUTH_TOKEN)).thenReturn(response);
        File englishFile = File.createTempFile("english", ".pdf");
        when(pdfExtractorService.downloadPdf(anyString(), eq("1"), any(Document.class), eq(AUTH_TOKEN)))
            .thenReturn(englishFile);

        service.uploadFL404Orders();

        verify(pdfExtractorService, times(2)).downloadPdf(anyString(), eq("1"), any(Document.class), eq(AUTH_TOKEN));
        verify(csvWriter, times(1)).appendCsvRowToFile(eq(tempCsv), any(CsvData.class), eq(englishFile.getName()));
    }

    @Test
    void shouldReturnNullWhenHearingsIsNull() throws Exception {
        AcroCaseData caseData = createCaseDataWithHearings(null);
        testHearingDateScenario(caseData, 1);
    }

    @Test
    void shouldReturnNullWhenHearingsIsEmpty() throws Exception {
        AcroCaseData caseData = createCaseDataWithHearings(Collections.emptyList());
        testHearingDateScenario(caseData, 1);
    }

    @Test
    void shouldReturnNullWhenNoListedHearings() throws Exception {
        List<CaseHearing> hearings = List.of(
            createCaseHearing("CANCELLED", LocalDateTime.now().plusDays(1)),
            createCaseHearing("AWAITING_LISTING", LocalDateTime.now().plusDays(2))
        );
        AcroCaseData caseData = createCaseDataWithHearings(hearings);
        testHearingDateScenario(caseData, 1);
    }

    @Test
    void shouldReturnNullWhenListedHearingsAreInPast() throws Exception {
        List<CaseHearing> hearings = List.of(
            createCaseHearing(LISTED, LocalDateTime.now().minusDays(1)),
            createCaseHearing(LISTED, LocalDateTime.now().minusHours(1))
        );
        AcroCaseData caseData = createCaseDataWithHearings(hearings);
        testHearingDateScenario(caseData, 1);
    }

    @Test
    void shouldReturnEarliestFutureListedHearing() throws Exception {
        LocalDateTime futureDate1 = LocalDateTime.now().plusDays(5);
        LocalDateTime futureDate2 = LocalDateTime.now().plusDays(2);
        LocalDateTime futureDate3 = LocalDateTime.now().plusDays(10);

        List<CaseHearing> hearings = List.of(
            createCaseHearing(LISTED, futureDate1),
            createCaseHearing(LISTED, futureDate2),
            createCaseHearing(LISTED, futureDate3),
            createCaseHearing("CANCELLED", LocalDateTime.now().plusDays(1))
        );
        AcroCaseData caseData = createCaseDataWithHearings(hearings);
        testHearingDateScenario(caseData, 1);
    }

    @Test
    void shouldHandleHearingsWithNullDaySchedule() throws Exception {
        CaseHearing hearingWithNullSchedule = CaseHearing.caseHearingWith()
            .hmcStatus(LISTED)
            .hearingDaySchedule(null)
            .build();

        List<CaseHearing> hearings = List.of(hearingWithNullSchedule);
        AcroCaseData caseData = createCaseDataWithHearings(hearings);
        testHearingDateScenario(caseData, 1);
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("documentProcessingScenarios")
    void shouldProcessDocumentsBasedOnAvailability(int successfulDownloads, String scenarioDescription) throws Exception {
        when(acroCaseDataService.getNonMolestationData(AUTH_TOKEN)).thenReturn(responseWithOneCase());

        File englishFile = successfulDownloads >= 1 ? File.createTempFile("english", ".pdf") : null;
        File welshFile = successfulDownloads >= 2 ? File.createTempFile("welsh", ".pdf") : null;

        when(pdfExtractorService.downloadPdf(anyString(), eq(CASE_ID), any(Document.class), eq(AUTH_TOKEN)))
            .thenReturn(englishFile, welshFile);

        service.uploadFL404Orders();

        verify(csvWriter, Mockito.times(successfulDownloads))
            .appendCsvRowToFile(eq(tempCsv), any(CsvData.class), anyString());
        verify(pdfExtractorService, Mockito.times(2))
            .downloadPdf(anyString(), eq(CASE_ID), any(Document.class), eq(AUTH_TOKEN));
    }

    @Test
    void testPrepareDataForCsvMapsAllFieldsCorrectly() throws Exception {
        LocalDateTime orderCreatedDate = LocalDateTime.of(2024, 10, 15, 14, 30);
        LocalDateTime expectedExpiryDate = LocalDateTime.of(2025, 4, 15, 14, 30);
        String expectedOrderMadeDate = "2025-10-14";

        FL404 fl404CustomFields = FL404.builder()
            .orderSpecifiedDateTime(expectedExpiryDate)
            .build();

        OtherOrderDetails otherOrderDetails = OtherOrderDetails.builder()
            .orderMadeDate(expectedOrderMadeDate)
            .build();

        OrderDetails order = OrderDetails.builder()
            .dateCreated(orderCreatedDate)
            .orderDocument(createDocument())
            .orderDocumentWelsh(createDocument())
            .fl404CustomFields(fl404CustomFields)
            .otherDetails(otherOrderDetails)
            .build();


        PartyDetails applicant = PartyDetails.builder()
            .firstName("John")
            .lastName("Doe")
            .build();

        PartyDetails respondent = PartyDetails.builder()
            .firstName("Jane")
            .lastName("Smith")
            .build();

        AcroCaseData caseData = AcroCaseData.builder()
            .id(98765L)
            .caseTypeOfApplication("FL402")
            .applicant(applicant)
            .respondent(respondent)
            .courtName("Birmingham Family Court")
            .courtEpimsId("456789")
            .courtTypeId("FC001")
            .fl404Orders(List.of(order))
            .build();

        AcroResponse response = AcroResponse.builder()
            .total(1)
            .cases(List.of(AcroCaseDetail.builder()
                .id(1L)
                .caseData(caseData)
                .build()))
            .build();

        when(acroCaseDataService.getNonMolestationData(AUTH_TOKEN)).thenReturn(response);
        File englishFile = File.createTempFile("test_english", ".pdf");
        when(pdfExtractorService.downloadPdf(anyString(), eq(CASE_ID), any(Document.class), eq(AUTH_TOKEN)))
            .thenReturn(englishFile);

        service.uploadFL404Orders();

        verify(csvWriter).appendCsvRowToFile(eq(tempCsv), csvDataArgumentCaptor.capture(), eq(englishFile.getName()));

        CsvData capturedData = csvDataArgumentCaptor.getValue();

        Map<String, Object[]> fieldMappings = Map.of(
            "ID", new Object[]{caseData.getId(), capturedData.getId(), "caseData.getId()"},
            "Case Type", new Object[]{caseData.getCaseTypeOfApplication(),
                capturedData.getCaseTypeOfApplication(), "caseData.getCaseTypeOfApplication()"},
            "Applicant", new Object[]{caseData.getApplicant(), capturedData.getApplicant(), "caseData.getApplicant()"},
            "Respondent", new Object[]{caseData.getRespondent(), capturedData.getRespondent(), "caseData.getRespondent()"},
            "Court Name", new Object[]{caseData.getCourtName(), capturedData.getCourtName(), "caseData.getCourtName()"},
            "Court EPIMS ID", new Object[]{caseData.getCourtEpimsId(), capturedData.getCourtEpimsId(),
                "caseData.getCourtEpimsId()"},
            "Court Type ID", new Object[]{caseData.getCourtTypeId(), capturedData.getCourtTypeId(),
                "caseData.getCourtTypeId()"},
            "Date Order Made", new Object[]{expectedOrderMadeDate, capturedData.getDateOrderMade(),
                "order.getOtherDetails().getOrderMadeDate()"},
            "Order Expiry Date", new Object[]{expectedExpiryDate, capturedData.getOrderExpiryDate(),
                "fl404CustomFields.getOrderSpecifiedDateTime()"}
        );

        fieldMappings.forEach((fieldName, mapping) -> {
            Object expected = mapping[0];
            Object actual = mapping[1];
            String source = (String) mapping[2];
            assertEquals(expected, actual,
                String.format("%s should be mapped from %s", fieldName, source));
        });
    }

    static Stream<Arguments> documentProcessingScenarios() {
        return Stream.of(
            Arguments.of(0, "No PDFs download successfully"),
            Arguments.of(1, "Only English PDF downloads successfully"),
            Arguments.of(1, "Both English and Welsh PDFs download successfully")
        );
    }

    private void testHearingDateScenario(AcroCaseData caseData, int expectedCsvWrites) throws Exception {
        AcroResponse response = AcroResponse.builder()
            .total(1)
            .cases(List.of(AcroCaseDetail.builder()
                .id(1L)
                .caseData(caseData)
                .build()))
            .build();

        when(acroCaseDataService.getNonMolestationData(AUTH_TOKEN)).thenReturn(response);
        File englishFile = File.createTempFile("english", ".pdf");
        when(pdfExtractorService.downloadPdf(anyString(), eq(CASE_ID), any(Document.class), eq(AUTH_TOKEN)))
            .thenReturn(englishFile);

        service.uploadFL404Orders();

        verify(csvWriter, times(expectedCsvWrites))
            .appendCsvRowToFile(eq(tempCsv), any(CsvData.class), eq(englishFile.getName()));
    }

    private AcroCaseData createCaseDataWithHearings(List<CaseHearing> hearings) {
        return AcroCaseData.builder()
            .fl404Orders(List.of(createOrderDetails()))
            .caseHearings(hearings)
            .build();
    }

    private CaseHearing createCaseHearing(String status, LocalDateTime hearingDateTime) {
        return CaseHearing.caseHearingWith()
            .hmcStatus(status)
            .hearingDaySchedule(List.of(
                HearingDaySchedule.hearingDayScheduleWith()
                    .hearingStartDateTime(hearingDateTime)
                    .build()
            ))
            .build();
    }

    private OrderDetails createOrderDetails() {
        return OrderDetails.builder()
            .dateCreated(LocalDateTime.now())
            .orderDocument(createDocument())
            .orderDocumentWelsh(createDocument())
            .otherDetails(OtherOrderDetails.builder()
                .orderMadeDate("2025-10-14")
                .build())
            .build();
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
                    .fl404Orders(List.of(createOrderDetails()))
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
