package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;
import uk.gov.hmcts.reform.prl.utils.MaskEmail;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;


class RespondentOrgPolicyServiceTest {

    private RespondentOrgPolicyService service;
    private AssignCaseAccessService assignCaseAccessService;
    private OrganisationService organisationService;
    private MaskEmail maskEmail;

    @BeforeEach
    void setUp() {
        assignCaseAccessService = mock(AssignCaseAccessService.class);
        organisationService = mock(OrganisationService.class);
        maskEmail = mock(MaskEmail.class);
        service = new RespondentOrgPolicyService(assignCaseAccessService, organisationService, maskEmail);

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
    void shouldCreatePolicyWhenMissing() {
        Map<String, Object> caseDataMap = new HashMap<>();
        // No policy exists

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG123")
                              .organisationName("Test Org")
                              .build())
            .solicitorEmail("test@solicitor.org")
            .build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .respondents(List.of(ElementUtils.element(respondent)))
            .build();

        Map<String, Object> updated = service.populateRespondentOrganisations(caseDataMap, caseData);

        assertThat(updated).containsKey("caRespondent1Policy");

        @SuppressWarnings("unchecked")
        Map<String, Object> createdPolicy = (Map<String, Object>) updated.get("caRespondent1Policy");

        assertThat(createdPolicy)
            .containsEntry("OrgPolicyCaseAssignedRole", "[C100RESPONDENTSOLICITOR1]");

        @SuppressWarnings("unchecked")
        Map<String, Object> organisation = (Map<String, Object>) createdPolicy.get("Organisation");

        assertThat(organisation)
            .containsEntry("OrganisationID", "ORG123")
            .containsEntry("OrganisationName", "Test Org");
    }

    @Test
    void shouldCreatePolicyForFl401WhenMissing() {
        Map<String, Object> caseDataMap = new HashMap<>();

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG456")
                              .organisationName("DA Org")
                              .build())
            .solicitorEmail("da@solicitor.org")
            .build();

        CaseData caseData = CaseData.builder()
            .id(456L)
            .caseTypeOfApplication("FL401")
            .state(State.CASE_ISSUED)
            .respondents(List.of(ElementUtils.element(respondent)))
            .build();

        Map<String, Object> updated = service.populateRespondentOrganisations(caseDataMap, caseData);

        assertThat(updated).containsKey("daRespondent1Policy");

        @SuppressWarnings("unchecked")
        Map<String, Object> createdPolicy = (Map<String, Object>) updated.get("daRespondent1Policy");

        assertThat(createdPolicy)
            .containsEntry("OrgPolicyCaseAssignedRole", "[FL401RESPONDENTSOLICITOR]");
    }

    @Test
    void shouldCreatePolicyForSecondRespondent() {
        Map<String, Object> caseDataMap = new HashMap<>();

        PartyDetails respondent1 = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .build();

        PartyDetails respondent2 = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG789")
                              .organisationName("Second Org")
                              .build())
            .solicitorEmail("second@solicitor.org")
            .build();

        CaseData caseData = CaseData.builder()
            .id(789L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .respondents(List.of(
                ElementUtils.element(respondent1),
                ElementUtils.element(respondent2)
            ))
            .build();

        Map<String, Object> updated = service.populateRespondentOrganisations(caseDataMap, caseData);

        assertThat(updated).containsKey("caRespondent2Policy");
        assertThat(updated).doesNotContainKey("caRespondent1Policy");

        @SuppressWarnings("unchecked")
        Map<String, Object> createdPolicy = (Map<String, Object>) updated.get("caRespondent2Policy");

        assertThat(createdPolicy)
            .containsEntry("OrgPolicyCaseAssignedRole", "[C100RESPONDENTSOLICITOR2]");
    }

    @Test
    void shouldAssignCaseAccessWhenRespondentSolicitorResolvableForC100() {
        // Happy path: verifies the bug fix - we route through AssignCaseAccessService
        // (CCD case-assignment) rather than calling Role Assignment Service directly.
        Map<String, Object> caseDataMap = new HashMap<>();

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG-CA-1")
                              .organisationName("CA Sol Org")
                              .build())
            .solicitorEmail("resolvable@solicitor.org")
            .build();

        CaseData caseData = CaseData.builder()
            .id(1010L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .respondents(List.of(ElementUtils.element(respondent)))
            .build();

        when(organisationService.findUserByEmail("resolvable@solicitor.org"))
            .thenReturn(Optional.of("idam-user-1"));

        service.populateRespondentOrganisations(caseDataMap, caseData);

        verify(assignCaseAccessService).assignCaseAccessToUserWithRole(
            eq("1010"),
            eq("idam-user-1"),
            eq("[C100RESPONDENTSOLICITOR1]"),
            eq("ORG-CA-1")
        );
    }

    @Test
    void shouldAssignCaseAccessWhenRespondentSolicitorResolvableForFl401() {
        Map<String, Object> caseDataMap = new HashMap<>();

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG-DA-1")
                              .organisationName("DA Sol Org")
                              .build())
            .solicitorEmail("da-resolvable@solicitor.org")
            .build();

        CaseData caseData = CaseData.builder()
            .id(2020L)
            .caseTypeOfApplication("FL401")
            .state(State.CASE_ISSUED)
            .respondents(List.of(ElementUtils.element(respondent)))
            .build();

        when(organisationService.findUserByEmail("da-resolvable@solicitor.org"))
            .thenReturn(Optional.of("idam-user-2"));

        service.populateRespondentOrganisations(caseDataMap, caseData);

        verify(assignCaseAccessService).assignCaseAccessToUserWithRole(
            eq("2020"),
            eq("idam-user-2"),
            eq("[FL401RESPONDENTSOLICITOR]"),
            eq("ORG-DA-1")
        );
    }

    @Test
    void shouldNotAssignCaseAccessWhenIdamUserNotResolvable() {
        Map<String, Object> caseDataMap = new HashMap<>();

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG-NO-USER")
                              .organisationName("No IDAM User Org")
                              .build())
            .solicitorEmail("missing@solicitor.org")
            .build();

        CaseData caseData = CaseData.builder()
            .id(3030L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .respondents(List.of(ElementUtils.element(respondent)))
            .build();

        when(organisationService.findUserByEmail("missing@solicitor.org"))
            .thenReturn(Optional.empty());
        when(maskEmail.mask("missing@solicitor.org")).thenReturn("m***@solicitor.org");

        Map<String, Object> updated = service.populateRespondentOrganisations(caseDataMap, caseData);

        // Policy still updated with organisation details ...
        @SuppressWarnings("unchecked")
        Map<String, Object> policy = (Map<String, Object>) updated.get("caRespondent1Policy");
        @SuppressWarnings("unchecked")
        Map<String, Object> organisation = (Map<String, Object>) policy.get("Organisation");
        assertThat(organisation).containsEntry("OrganisationID", "ORG-NO-USER");

        // ... but no case-access call was made.
        verify(assignCaseAccessService, never()).assignCaseAccessToUserWithRole(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void shouldReturnUnchangedMapWhenRespondentsIsNull() {
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("existingKey", "existingValue");

        CaseData caseData = CaseData.builder()
            .id(4040L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(null)
            .build();

        Map<String, Object> updated = service.populateRespondentOrganisations(caseDataMap, caseData);

        assertThat(updated).isSameAs(caseDataMap);
        assertThat(updated).containsEntry("existingKey", "existingValue");
        verify(assignCaseAccessService, never()).assignCaseAccessToUserWithRole(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void shouldReturnUnchangedMapWhenRespondentsIsEmpty() {
        Map<String, Object> caseDataMap = new HashMap<>();

        CaseData caseData = CaseData.builder()
            .id(5050L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondents(List.of())
            .build();

        Map<String, Object> updated = service.populateRespondentOrganisations(caseDataMap, caseData);

        assertThat(updated).isSameAs(caseDataMap);
        verify(assignCaseAccessService, never()).assignCaseAccessToUserWithRole(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString()
        );
    }

    @Test
    void shouldSkipNullRespondentEntry() {
        Map<String, Object> caseDataMap = new HashMap<>();

        PartyDetails respondent2 = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG-2")
                              .organisationName("Org 2")
                              .build())
            .solicitorEmail("r2@solicitor.org")
            .build();

        // First respondent is null - should be skipped without NPE.
        CaseData caseData = CaseData.builder()
            .id(6060L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .respondents(List.of(
                ElementUtils.element(null),
                ElementUtils.element(respondent2)
            ))
            .build();

        when(organisationService.findUserByEmail("r2@solicitor.org"))
            .thenReturn(Optional.of("idam-r2"));

        service.populateRespondentOrganisations(caseDataMap, caseData);

        verify(assignCaseAccessService).assignCaseAccessToUserWithRole(
            eq("6060"),
            eq("idam-r2"),
            eq("[C100RESPONDENTSOLICITOR2]"),
            eq("ORG-2")
        );
    }

    @Test
    void shouldCapProcessingAtMaxRespondents() {
        // The service caps at MAX_RESPONDENTS (5). Anything after that is ignored.
        Map<String, Object> caseDataMap = new HashMap<>();

        PartyDetails legallyRepresented = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG-X")
                              .organisationName("Org X")
                              .build())
            .solicitorEmail("x@solicitor.org")
            .build();

        // Six respondents, all valid - the 6th (index 5) must be ignored.
        CaseData caseData = CaseData.builder()
            .id(7070L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .respondents(List.of(
                ElementUtils.element(legallyRepresented),
                ElementUtils.element(legallyRepresented),
                ElementUtils.element(legallyRepresented),
                ElementUtils.element(legallyRepresented),
                ElementUtils.element(legallyRepresented),
                ElementUtils.element(legallyRepresented)
            ))
            .build();

        when(organisationService.findUserByEmail("x@solicitor.org"))
            .thenReturn(Optional.of("idam-x"));

        Map<String, Object> updated = service.populateRespondentOrganisations(caseDataMap, caseData);

        assertThat(updated).containsKey("caRespondent1Policy");
        assertThat(updated).containsKey("caRespondent2Policy");
        assertThat(updated).containsKey("caRespondent3Policy");
        assertThat(updated).containsKey("caRespondent4Policy");
        assertThat(updated).containsKey("caRespondent5Policy");
        assertThat(updated).doesNotContainKey("caRespondent6Policy");
    }

    @Test
    void shouldPreserveExistingOrgPolicyCaseAssignedRoleForExistingPolicy() {
        // If OrgPolicyCaseAssignedRole is already set on an existing policy, we must not
        // overwrite it - even to the same value. The map merge preserves whatever is there.
        Map<String, Object> policy = new HashMap<>();
        policy.put("Organisation", new HashMap<>());
        policy.put("OrgPolicyCaseAssignedRole", "[C100RESPONDENTSOLICITOR1]");
        policy.put("OrgPolicyReference", "some-existing-ref");
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("caRespondent1Policy", policy);

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG-KEEP-REF")
                              .organisationName("Keep Ref Org")
                              .build())
            .solicitorEmail("keep@solicitor.org")
            .build();

        CaseData caseData = CaseData.builder()
            .id(8080L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .respondents(List.of(ElementUtils.element(respondent)))
            .build();

        when(organisationService.findUserByEmail("keep@solicitor.org"))
            .thenReturn(Optional.of("idam-keep"));

        Map<String, Object> updated = service.populateRespondentOrganisations(caseDataMap, caseData);

        @SuppressWarnings("unchecked")
        Map<String, Object> updatedPolicy = (Map<String, Object>) updated.get("caRespondent1Policy");
        assertThat(updatedPolicy)
            .containsEntry("OrgPolicyCaseAssignedRole", "[C100RESPONDENTSOLICITOR1]")
            .containsEntry("OrgPolicyReference", "some-existing-ref");
    }

    @Test
    void shouldOmitOrganisationNameWhenBlank() {
        // If solicitorOrg.OrganisationName is blank, we should not add the OrganisationName
        // entry to the built organisation map - preserves the "unknown name" case rather than
        // writing an empty string that downstream code may treat as valid.
        Map<String, Object> caseDataMap = new HashMap<>();

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG-NO-NAME")
                              .organisationName("") // blank -> should be omitted
                              .build())
            .solicitorEmail("noname@solicitor.org")
            .build();

        CaseData caseData = CaseData.builder()
            .id(9090L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .respondents(List.of(ElementUtils.element(respondent)))
            .build();

        when(organisationService.findUserByEmail("noname@solicitor.org"))
            .thenReturn(Optional.of("idam-noname"));

        Map<String, Object> updated = service.populateRespondentOrganisations(caseDataMap, caseData);

        @SuppressWarnings("unchecked")
        Map<String, Object> policy = (Map<String, Object>) updated.get("caRespondent1Policy");
        @SuppressWarnings("unchecked")
        Map<String, Object> organisation = (Map<String, Object>) policy.get("Organisation");
        assertThat(organisation)
            .containsEntry("OrganisationID", "ORG-NO-NAME")
            .doesNotContainKey("OrganisationName");
    }

    @Test
    void shouldNotAssignRoleWhenSolicitorEmailBlank() {
        // Blank solicitor email fails the isValidRespondent gate; we never try to resolve
        // an IDAM user nor call the case-assignment API. Existing policy is left alone.
        Map<String, Object> policy = new HashMap<>();
        Map<String, Object> originalOrganisation = new HashMap<>();
        originalOrganisation.put("OrganisationID", "OLD");
        policy.put("Organisation", originalOrganisation);
        policy.put("OrgPolicyCaseAssignedRole", "[C100RESPONDENTSOLICITOR1]");
        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("caRespondent1Policy", policy);

        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG-VALID")
                              .organisationName("Valid Org")
                              .build())
            .solicitorEmail("") // blank -> respondent not "valid"
            .build();

        CaseData caseData = CaseData.builder()
            .id(1111L)
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

        verify(assignCaseAccessService, never()).assignCaseAccessToUserWithRole(
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString()
        );
    }
}
