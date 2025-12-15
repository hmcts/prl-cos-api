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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

class RespondentOrgPolicyServiceTest {

    private RespondentOrgPolicyService service;

    @BeforeEach
    void setUp() {
        service = new RespondentOrgPolicyService();
    }

    @Test
    void shouldPopulateOrganisationForExistingCaRespondentPolicy() {
        // GIVEN: CCD-style map
        Map<String, Object> caseDataMap = new HashMap<>();
        List<Object> respondentList = List.of(
            Map.of(
                "value", Map.of(
                    "doTheyHaveLegalRepresentation", "yes",
                    "solicitorOrg", Map.of(
                        "OrganisationID", "ORG123",
                        "OrganisationName", "Test Org"
                    )
                )
            ));
        caseDataMap.put("respondents", respondentList);

        Map<String, Object> policy = new HashMap<>();
        policy.put("Organisation", new HashMap<>()); // or Map.of("OrganisationID", "...")
        policy.put("OrgPolicyReference", null);
        policy.put("OrgPolicyCaseAssignedRole", "[C100RESPONDENTSOLICITOR1]");

        caseDataMap.put("caRespondent1Policy", policy);
        PartyDetails respondent = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorOrg(Organisation.builder()
                              .organisationID("ORG123")
                              .organisationName("Test Org")
                              .build())
            .build();

        List<Element<PartyDetails>> respondentParties = List.of(ElementUtils.element(respondent));

        CaseData caseData = CaseData.builder()
            .id(123L)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.CASE_ISSUED)
            .respondents(respondentParties)
            .build();

        Map<String, Object> updates =
            service.populateRespondentOrganisations(
                caseDataMap,
                caseData.getRespondents(),
                C100_CASE_TYPE
            );


        assertThat(updates).containsKey("caRespondent1Policy");

        @SuppressWarnings("unchecked")
        Map<String, Object> updatedPolicy =
            (Map<String, Object>) updates.get("caRespondent1Policy");

        @SuppressWarnings("unchecked")
        Map<String, Object> organisation =
            (Map<String, Object>) updatedPolicy.get("Organisation");

        assertThat(organisation).containsEntry("OrganisationID","ORG123").containsEntry("OrganisationName","Test Org");

        assertThat(updatedPolicy).containsEntry("OrgPolicyCaseAssignedRole","[C100RESPONDENTSOLICITOR1]");
    }
}
