package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.MaskEmail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;


class RespondentOrgPolicyServiceTest {

    private RespondentOrgPolicyService service;
    private RoleAssignmentService roleAssignmentService;
    private OrganisationService organisationService;
    private MaskEmail maskEmail;

    @BeforeEach
    void setUp() {
        roleAssignmentService = mock(RoleAssignmentService.class);
        organisationService = mock(OrganisationService.class);
        maskEmail = mock(MaskEmail.class);
        service = new RespondentOrgPolicyService(roleAssignmentService, organisationService, maskEmail);

    }

    @Test
    void shouldPopulateOrganisationForExistingCaRespondentPolicy() {

        // Existing policy must be a mutable map because we include nulls.
        Map<String, Object> policy = new HashMap<>();
        policy.put("Organisation", new HashMap<>()); // empty org placeholder
        policy.put("OrgPolicyReference", null);
        policy.put("OrgPolicyCaseAssignedRole", "[C100RESPONDENTSOLICITOR1]");
        Map<String, Object> caseDataMap = new HashMap<>();

        caseDataMap.put("caRespondent1Policy", policy);

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG123")
                              .organisationName("Test Org")
                              .build())
            .solicitorEmail("test@solicitor.org")
            .build();

        List<Element<PartyDetails>> respondentParties = List.of(ElementUtils.element(respondent));

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .respondents(respondentParties)
            .build();

        // WHEN
        Map<String, Object> updated = service.populateRespondentOrganisations(caseDataMap, caseData);

        // THEN
        assertThat(updated).containsKey("caRespondent1Policy");

        @SuppressWarnings("unchecked")
        Map<String, Object> updatedPolicy = (Map<String, Object>) updated.get("caRespondent1Policy");

        @SuppressWarnings("unchecked")
        Map<String, Object> organisation = (Map<String, Object>) updatedPolicy.get("Organisation");

        assertThat(organisation)
            .containsEntry("OrganisationID", "ORG123")
            .containsEntry("OrganisationName", "Test Org");

        assertThat(updatedPolicy)
            .containsEntry("OrgPolicyCaseAssignedRole", "[C100RESPONDENTSOLICITOR1]")
            .containsKey("OrgPolicyReference");
        assertThat(updatedPolicy.get("OrgPolicyReference")).isNull();
    }

    @Test
    void shouldNotUpdatePolicyWhenRespondentHasNoLegalRep() {
        Map<String, Object> originalOrganisation = new HashMap<>();
        originalOrganisation.put("OrganisationID", "OLD");

        Map<String, Object> policy = new HashMap<>();
        policy.put("Organisation", originalOrganisation);
        policy.put("OrgPolicyCaseAssignedRole", "[C100RESPONDENTSOLICITOR1]");
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("caRespondent1Policy", policy);

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG123")
                              .organisationName("Test Org")
                              .build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .respondents(List.of(ElementUtils.element(respondent)))
            .build();

        Map<String, Object> updated = service.populateRespondentOrganisations(caseDataMap, caseData);

        @SuppressWarnings("unchecked")
        Map<String, Object> updatedPolicy = (Map<String, Object>) updated.get("caRespondent1Policy");

        @SuppressWarnings("unchecked")
        Map<String, Object> organisation = (Map<String, Object>) updatedPolicy.get("Organisation");

        assertThat(organisation).containsEntry("OrganisationID", "OLD");
    }

    @Test
    void shouldNotUpdatePolicyWhenSolicitorOrgIdMissing() {
        Map<String, Object> originalOrganisation = new HashMap<>();
        originalOrganisation.put("OrganisationID", "OLD");

        Map<String, Object> policy = new HashMap<>();
        policy.put("Organisation", originalOrganisation);
        policy.put("OrgPolicyCaseAssignedRole", "[C100RESPONDENTSOLICITOR1]");
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("caRespondent1Policy", policy);

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("") // blank => should skip
                              .organisationName("Test Org")
                              .build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .respondents(List.of(ElementUtils.element(respondent)))
            .build();

        Map<String, Object> updated = service.populateRespondentOrganisations(caseDataMap, caseData);

        @SuppressWarnings("unchecked")
        Map<String, Object> updatedPolicy = (Map<String, Object>) updated.get("caRespondent1Policy");

        @SuppressWarnings("unchecked")
        Map<String, Object> organisation = (Map<String, Object>) updatedPolicy.get("Organisation");

        assertThat(organisation).containsEntry("OrganisationID", "OLD");
    }

    @Test
    void shouldSkipWhenExistingPolicyIsNotAMap() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("caRespondent1Policy", "not-a-map");

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG123")
                              .organisationName("Test Org")
                              .build())
            .build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .respondents(List.of(ElementUtils.element(respondent)))
            .build();

        Map<String, Object> updated = service.populateRespondentOrganisations(caseDataMap, caseData);

        // unchanged
        assertThat(updated.get("caRespondent1Policy")).isEqualTo("not-a-map");
    }
}
