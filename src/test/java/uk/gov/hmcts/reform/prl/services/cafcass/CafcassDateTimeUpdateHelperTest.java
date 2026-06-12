package uk.gov.hmcts.reform.prl.services.cafcass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CafcassDateTimeUpdateHelperTest {

    @Mock
    private HearingService hearingService;

    @Mock
    private SystemUserService systemUserService;

    private CafcassDateTimeUpdateHelper cafcassDateTimeUpdateHelper;

    @BeforeEach
    void setUp() {
        cafcassDateTimeUpdateHelper = new CafcassDateTimeUpdateHelper(
            new CafCassFilter(),
            hearingService,
            systemUserService,
            CcdObjectMapper.getObjectMapper()
        );
        lenient().when(systemUserService.getSysUserToken()).thenReturn("authorisation");
        lenient().when(hearingService.getHearingsForAllCases(anyString(), anyMap())).thenReturn(Collections.emptyList());
    }

    @Test
    void shouldReturnFalseWhenOnlyEmptyCafcassElementsAreFilteredOut() {
        Map<String, Object> documentWithoutValue = new HashMap<>();
        documentWithoutValue.put("id", "00000000-0000-0000-0000-000000000001");
        documentWithoutValue.put("value", null);

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseManagementLocation", caseManagementLocation());
        caseData.put("otherDocuments", List.of(documentWithoutValue));

        Map<String, Object> caseDataBefore = new HashMap<>();
        caseDataBefore.put("caseManagementLocation", caseManagementLocation());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseData)
            .build();
        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .id(123L)
            .data(caseDataBefore)
            .build();

        assertFalse(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(caseDetails, caseDetailsBefore));
    }

    @Test
    void shouldReturnTrueWhenCafcassCaseDataHasChanged() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseManagementLocation", caseManagementLocation());
        caseData.put("caseTypeOfApplication", "FL401");

        Map<String, Object> caseDataBefore = new HashMap<>();
        caseDataBefore.put("caseManagementLocation", caseManagementLocation());
        caseDataBefore.put("caseTypeOfApplication", "C100");

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseData)
            .build();
        CaseDetails caseDetailsBefore = CaseDetails.builder()
            .id(123L)
            .data(caseDataBefore)
            .build();

        assertTrue(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(caseDetails, caseDetailsBefore));
    }

    @Test
    void shouldReturnFalseWhenBothCaseDetailsAreNull() {
        assertFalse(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(null, null));
    }

    @Test
    void shouldReturnTrueWhenOnlyOneCaseDetailsIsNull() {
        assertTrue(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(caseDetailsWithLocation(caseManagementLocation()), null));
    }

    @Test
    void shouldIgnoreChangesWhenCaseManagementLocationIsMissing() {
        CaseDetails caseDetails = caseDetails(Map.of("caseTypeOfApplication", "FL401"));
        CaseDetails caseDetailsBefore = caseDetails(Map.of("caseTypeOfApplication", "C100"));

        assertFalse(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(caseDetails, caseDetailsBefore));
    }

    @Test
    void shouldIgnoreChangesWhenRegionIsNotCafcassCymruRegion() {
        Map<String, Object> unsupportedLocation = new HashMap<>();
        unsupportedLocation.put("regionId", "7");
        unsupportedLocation.put("baseLocationId", "123456");

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseManagementLocation", unsupportedLocation);
        caseData.put("caseTypeOfApplication", "FL401");

        Map<String, Object> caseDataBefore = new HashMap<>();
        caseDataBefore.put("caseManagementLocation", unsupportedLocation);
        caseDataBefore.put("caseTypeOfApplication", "C100");

        assertFalse(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(
            caseDetails(caseData),
            caseDetails(caseDataBefore)
        ));
    }

    @Test
    void shouldUseLegacyRegionAndBaseLocationForHearingSearch() {
        Map<String, Object> legacyLocation = new HashMap<>();
        legacyLocation.put("region", "2");
        legacyLocation.put("baseLocation", "654321");

        assertFalse(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(
            caseDetailsWithLocation(legacyLocation),
            caseDetailsWithLocation(legacyLocation)
        ));

        ArgumentCaptor<Map<String, String>> mapCaptor = ArgumentCaptor.forClass(Map.class);
        verify(hearingService, times(2)).getHearingsForAllCases(anyString(), mapCaptor.capture());
        assertTrue(mapCaptor.getAllValues().stream().allMatch(map -> map.containsValue("2-654321")));
    }

    @Test
    void shouldReturnTrueWhenHearingDetailsAreAddedToCurrentCase() {
        when(hearingService.getHearingsForAllCases(anyString(), anyMap()))
            .thenReturn(List.of(hearingWithListedAndCancelledHearing()))
            .thenReturn(Collections.emptyList());

        assertTrue(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(
            caseDetailsWithLocation(caseManagementLocation()),
            caseDetailsWithLocation(caseManagementLocation())
        ));
    }

    @Test
    void shouldReturnFalseWhenCancelledBeforeListingHearingIsRemoved() {
        when(hearingService.getHearingsForAllCases(anyString(), anyMap()))
            .thenReturn(List.of(hearingWithCancelledBeforeListingOnly()))
            .thenReturn(Collections.emptyList());

        assertFalse(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(
            caseDetailsWithLocation(caseManagementLocation()),
            caseDetailsWithLocation(caseManagementLocation())
        ));
    }

    @Test
    void shouldReturnTrueWhenUploadedDocumentIsAddedToOtherDocuments() {
        String documentId = "11111111-1111-1111-1111-111111111111";
        String documentUrl = "http://dm-store/documents/" + documentId;
        String documentName = "position-statement.pdf";

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseManagementLocation", caseManagementLocation());
        caseData.put("otherDocumentsUploaded", List.of(caseDocument(documentUrl, documentName)));

        assertTrue(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(
            caseDetails(caseData),
            caseDetailsWithLocation(caseManagementLocation())
        ));
    }

    @Test
    void shouldReturnFalseWhenRedactedOtherDocumentIsRemoved() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseManagementLocation", caseManagementLocation());
        caseData.put("otherDocuments", List.of(element(Map.of(
            "documentName", "redacted.pdf",
            "documentOther", Map.of("document_id", "00000000-0000-0000-0000-000000000000")
        ))));

        assertFalse(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(
            caseDetails(caseData),
            caseDetailsWithLocation(caseManagementLocation())
        ));
    }

    @Test
    void shouldReturnFalseWhenPartyAndRedactedOrderFieldsAreNormalisedAway() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseManagementLocation", caseManagementLocation());
        caseData.put("applicants", List.of(element(
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
            Map.of("firstName", "Alex", "lastName", "Green")
        )));
        caseData.put("respondents", List.of(element(
            "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
            Map.of(
            "firstName", "Robin",
            "lastName", "Brown",
            "response", Map.of("legalRepresentation", "No")
        ))));
        caseData.put("orderCollection", List.of(element(Map.of(
            "orderType", "Child arrangements order",
            "serveOrderDetails", Map.of("whenReportsMustBeFiled", "1 January 2026"),
            "orderDocument", Map.of(
                "document_url", "http://dm-store/documents/00000000-0000-0000-0000-000000000000",
                "document_filename", "redacted-order.pdf"
            )
        ))));

        Map<String, Object> caseDataBefore = new HashMap<>();
        caseDataBefore.put("caseManagementLocation", caseManagementLocation());
        caseDataBefore.put("applicants", List.of(element(
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
            Map.of("firstName", "Alex", "lastName", "Green")
        )));
        caseDataBefore.put("respondents", List.of(element(
            "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
            Map.of("firstName", "Robin", "lastName", "Brown")
        )));

        assertFalse(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(
            caseDetails(caseData),
            caseDetails(caseDataBefore)
        ));
    }

    @Test
    void shouldReturnTrueWhenReviewDocumentIsAddedFromCaseFileView() {
        String documentUrl = "http://dm-store/documents/22222222-2222-2222-2222-222222222222";
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseManagementLocation", caseManagementLocation());
        caseData.put("courtStaffUploadDocListDocTab", List.of(element(Map.of(
            "categoryId", "applicantStatements",
            "applicantStatementsDocument", caseDocument(documentUrl, "applicant-statement.pdf")
        ))));

        assertTrue(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(
            caseDetails(caseData),
            caseDetailsWithLocation(caseManagementLocation())
        ));
    }

    @Test
    void shouldReturnTrueWhenConfidentialRespondentDocumentIsAdded() {
        String documentUrl = "http://dm-store/documents/33333333-3333-3333-3333-333333333333";
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseManagementLocation", caseManagementLocation());
        caseData.put("respondentAc8Documents", List.of(element(Map.of(
            "respondentC8Document", caseDocument(documentUrl, "respondent-c8.pdf"),
            "respondentC8DocumentWelsh", caseDocument(documentUrl + "-welsh", "respondent-c8-welsh.pdf")
        ))));

        assertTrue(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(
            caseDetails(caseData),
            caseDetailsWithLocation(caseManagementLocation())
        ));
    }

    @Test
    void shouldReturnTrueWhenServiceOfApplicationDocumentsAreAdded() {
        Map<String, Object> duplicateDocument = caseDocument(
            "http://dm-store/documents/44444444-4444-4444-4444-444444444444",
            "special-arrangements.pdf"
        );
        Map<String, Object> emailedDocument = caseDocument(
            "http://dm-store/documents/55555555-5555-5555-5555-555555555555",
            "emailed-application.pdf"
        );

        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseManagementLocation", caseManagementLocation());
        caseData.put("specialArrangementsLetter", duplicateDocument);
        caseData.put("additionalDocuments", caseDocument(
            "http://dm-store/documents/66666666-6666-6666-6666-666666666666",
            "additional.pdf"
        ));
        caseData.put("additionalDocumentsList", List.of(element(caseDocument(
            "http://dm-store/documents/77777777-7777-7777-7777-777777777777",
            "additional-list.pdf"
        ))));
        caseData.put("stmtOfServiceForOrder", List.of(element(Map.of("stmtOfServiceDocument", caseDocument(
            "http://dm-store/documents/88888888-8888-8888-8888-888888888888",
            "statement-order.pdf"
        )))));
        caseData.put("finalServedApplicationDetailsList", List.of(element(Map.of(
            "bulkPrintDetails", List.of(element(Map.of("printDocs", List.of(element(duplicateDocument))))),
            "emailNotificationDetails", List.of(element(Map.of("docs", List.of(element(emailedDocument)))))
        ))));

        assertTrue(cafcassDateTimeUpdateHelper.hasCafcassCaseDataChanged(
            caseDetails(caseData),
            caseDetailsWithLocation(caseManagementLocation())
        ));
    }

    private Map<String, Object> caseManagementLocation() {
        Map<String, Object> caseManagementLocation = new HashMap<>();
        caseManagementLocation.put("regionId", "1");
        caseManagementLocation.put("baseLocationId", "123456");
        return caseManagementLocation;
    }

    private CaseDetails caseDetails(Map<String, Object> caseData) {
        return CaseDetails.builder()
            .id(123L)
            .data(caseData)
            .build();
    }

    private CaseDetails caseDetailsWithLocation(Map<String, Object> caseManagementLocation) {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("caseManagementLocation", caseManagementLocation);
        return caseDetails(caseData);
    }

    private Hearings hearingWithListedAndCancelledHearing() {
        List<CaseHearing> caseHearings = new ArrayList<>();
        caseHearings.add(CaseHearing.caseHearingWith()
                             .hearingID(1L)
                             .hmcStatus("LISTED")
                             .hearingDaySchedule(List.of(HearingDaySchedule.hearingDayScheduleWith()
                                                            .hearingStartDateTime(LocalDateTime.of(2026, 1, 1, 10, 0))
                                                            .hearingEndDateTime(LocalDateTime.of(2026, 1, 1, 11, 0))
                                                            .hearingVenueId("venue-1")
                                                            .build()))
                             .build());
        caseHearings.add(cancelledBeforeListingHearing());

        return Hearings.hearingsWith()
            .caseRef("123")
            .courtName("Cardiff Civil and Family Justice Centre")
            .courtTypeId("family")
            .caseHearings(caseHearings)
            .build();
    }

    private Hearings hearingWithCancelledBeforeListingOnly() {
        return Hearings.hearingsWith()
            .caseRef("123")
            .courtName("Cardiff Civil and Family Justice Centre")
            .courtTypeId("family")
            .caseHearings(List.of(cancelledBeforeListingHearing()))
            .build();
    }

    private CaseHearing cancelledBeforeListingHearing() {
        return CaseHearing.caseHearingWith()
            .hearingID(2L)
            .hmcStatus("CANCELLED")
            .hearingDaySchedule(List.of(HearingDaySchedule.hearingDayScheduleWith().build()))
            .build();
    }

    private Map<String, Object> caseDocument(String documentUrl, String documentName) {
        Map<String, Object> document = new HashMap<>();
        document.put("document_url", documentUrl);
        document.put("document_binary_url", documentUrl + "/binary");
        document.put("document_filename", documentName);
        return document;
    }

    private Map<String, Object> element(Object value) {
        Map<String, Object> element = new HashMap<>();
        element.put("id", UUID.randomUUID().toString());
        element.put("value", value);
        return element;
    }

    private Map<String, Object> element(String id, Object value) {
        Map<String, Object> element = new HashMap<>();
        element.put("id", id);
        element.put("value", value);
        return element;
    }
}
