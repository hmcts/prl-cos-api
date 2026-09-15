package uk.gov.hmcts.reform.prl.services.cafcass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.filter.cafcaas.CafCassFilter;
import uk.gov.hmcts.reform.prl.mapper.CcdObjectMapper;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_DATE_TIME;

@ExtendWith(MockitoExtension.class)
class CafcassDateTimeServiceJourneyTest {

    private static final LocalDateTime EXISTING_CAFCASS_DATE_TIME = LocalDateTime.of(2026, 9, 15, 9, 30);

    @Mock
    private FeatureToggleService featureToggleService;

    @Mock
    private HearingService hearingService;

    @Mock
    private SystemUserService systemUserService;

    private CafcassCaseDataHelper cafcassCaseDataHelper;
    private CafcassDateTimeService cafcassDateTimeService;

    @BeforeEach
    void setUp() {
        cafcassCaseDataHelper = new CafcassCaseDataHelper(
            new CafCassFilter(),
            hearingService,
            systemUserService,
            CcdObjectMapper.getObjectMapper()
        );
        cafcassDateTimeService = new CafcassDateTimeService(featureToggleService, cafcassCaseDataHelper);

        ReflectionTestUtils.setField(
            cafcassDateTimeService,
            "caseStateList",
            List.of("DECISION_OUTCOME", "PREPARE_FOR_HEARING_CONDUCT_HEARING", "ALL_FINAL_ORDERS_ISSUED")
        );
        ReflectionTestUtils.setField(cafcassDateTimeService, "excludedEventList", Collections.emptyList());

        lenient().when(systemUserService.getSysUserToken()).thenReturn("authorisation");
        lenient().when(hearingService.getHearingsForAllCases(anyString(), anyMap())).thenReturn(Collections.emptyList());
        when(featureToggleService.isCafcassDateTimeFeatureEnabled()).thenReturn(true);
    }

    @ParameterizedTest
    @ValueSource(strings = {"amendOtherPeopleInTheCaseRevised", "amendChildrenAndApplicants"})
    void shouldNotUpdateCafcassDateTimeWhenC100JourneyAmendEventHasNoCaseDataChange(String eventId) {
        CaseDetails caseDetails = journeyCaseDetails(caseDataAfterNoOpAmend(eventId));
        CaseDetails caseDetailsBefore = journeyCaseDetails(caseDataBeforeNoOpAmend(eventId));

        assertFalse(cafcassCaseDataHelper.hasCafcassCaseDataChanged(caseDetails, caseDetailsBefore, eventId));

        Map<String, Object> updatedCaseData = cafcassDateTimeService.updateCafcassDateTime(CallbackRequest.builder()
            .eventId(eventId)
            .caseDetails(caseDetails)
            .caseDetailsBefore(caseDetailsBefore)
            .build());

        assertEquals(EXISTING_CAFCASS_DATE_TIME, updatedCaseData.get(CAFCASS_DATE_TIME));
    }

    private CaseDetails journeyCaseDetails(Map<String, Object> caseData) {
        return CaseDetails.builder()
            .id(1234567890123456L)
            .state("DECISION_OUTCOME")
            .data(caseData)
            .build();
    }

    private Map<String, Object> caseDataAfterNoOpAmend(String eventId) {
        Map<String, Object> caseData = baseC100JourneyCaseData();
        if ("amendOtherPeopleInTheCaseRevised".equals(eventId)) {
            caseData.put("childAndOtherPeopleRelations", List.of(element(
                "11111111-1111-1111-1111-111111111111",
                otherPeopleRelationship("Case Child", "Sam Taylor")
            )));
        } else {
            caseData.put("childAndApplicantRelations", List.of(element(
                "22222222-2222-2222-2222-222222222222",
                applicantRelationship("Case Child", "Alex Green")
            )));
        }
        return caseData;
    }

    private Map<String, Object> caseDataBeforeNoOpAmend(String eventId) {
        Map<String, Object> caseData = baseC100JourneyCaseData();
        if ("amendOtherPeopleInTheCaseRevised".equals(eventId)) {
            caseData.put("childAndOtherPeopleRelations", List.of(element(
                "33333333-3333-3333-3333-333333333333",
                otherPeopleRelationship("Stale Child Name", "Stale Other Person")
            )));
        } else {
            caseData.put("childAndApplicantRelations", List.of(element(
                "44444444-4444-4444-4444-444444444444",
                applicantRelationship("Stale Child Name", "Stale Applicant")
            )));
        }
        return caseData;
    }

    private Map<String, Object> baseC100JourneyCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CAFCASS_DATE_TIME, EXISTING_CAFCASS_DATE_TIME);
        caseData.put("dateSubmitted", "2026-09-15");
        caseData.put("issueDate", "2026-09-15");
        caseData.put("caseTypeOfApplication", "C100");
        caseData.put("courtName", "Wolverhampton Combined Court Centre");
        caseData.put("caseManagementLocation", Map.of(
            "regionId", "2",
            "baseLocationId", "41047",
            "region", "2",
            "baseLocation", "41047"
        ));
        caseData.put("gatekeeper", List.of(element(
            "55555555-5555-5555-5555-555555555555",
            Map.of("email", "gatekeeper@example.com")
        )));
        caseData.put("cafcassServedOptions", "Yes");
        caseData.put("applicants", List.of(element(
            "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
            Map.of("firstName", "Alex", "lastName", "Green", "gender", "male")
        )));
        caseData.put("otherPartyInTheCaseRevised", List.of(element(
            "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
            Map.of("firstName", "Sam", "lastName", "Taylor", "gender", "female")
        )));
        caseData.put("newChildDetails", List.of(element(
            "cccccccc-cccc-cccc-cccc-cccccccccccc",
            Map.of("firstName", "Case", "lastName", "Child", "gender", "male")
        )));
        caseData.put("orderCollection", List.of(element(
            "66666666-6666-6666-6666-666666666666",
            Map.of(
                "orderType", "Parental responsibility order (C45A)",
                "orderTypeId", "parentalResponsibility",
                "dateCreated", "2026-09-15",
                "otherDetails", Map.of(
                    "createdBy", "Court admin",
                    "orderCreatedDate", "15 Sep 2026",
                    "orderMadeDate", "15 Sep 2026",
                    "orderRecipients", "Applicant"
                ),
                "orderDocument", Map.of(
                    "document_filename", "Parental_Responsibility_Order_C45A.pdf",
                    "document_id", "77777777-7777-7777-7777-777777777777"
                )
            )
        )));
        caseData.put("finalServedApplicationDetailsList", Collections.emptyList());
        return caseData;
    }

    private Map<String, Object> applicantRelationship(String childFullName, String applicantFullName) {
        return Map.of(
            "applicantId", "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa",
            "applicantFullName", applicantFullName,
            "childId", "cccccccc-cccc-cccc-cccc-cccccccccccc",
            "childFullName", childFullName,
            "childAndApplicantRelation", "mother",
            "childLivesWith", "Yes"
        );
    }

    private Map<String, Object> otherPeopleRelationship(String childFullName, String otherPeopleFullName) {
        return Map.of(
            "otherPeopleId", "bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb",
            "otherPeopleFullName", otherPeopleFullName,
            "childId", "cccccccc-cccc-cccc-cccc-cccccccccccc",
            "childFullName", childFullName,
            "childAndOtherPeopleRelation", "guardian",
            "childLivesWith", "No"
        );
    }

    private Map<String, Object> element(String id, Object value) {
        return Map.of("id", id, "value", value);
    }
}
