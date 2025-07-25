package uk.gov.hmcts.reform.prl.services.barrister;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

@ExtendWith(SpringExtension.class)
class BarristerAllocationServiceTest {
    @InjectMocks
    BarristerAllocationService barristerAllocationService;

    private List<Element<PartyDetails>> allApplicants = new ArrayList<>();
    private List<Element<PartyDetails>> allRespondents = new ArrayList<>();
    private PartyDetails applicantFL401;
    private PartyDetails respondentFL401;

    private static final String PARTY_ID_PREFIX = "c0651c7d-0db9-47aa-9baa-933013f482f";
    private static final String SOL_PARTY_ID_PREFIX = "c0651c7d-0db9-47aa-9baa-933013f482e";


    @Test
    void shouldGetAllocatedBarristerC100() {
        setupApplicantsC100();
        setupRespondentsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .respondents(allRespondents)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAllocationService.getAllocatedBarrister(caseData);
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(4, partiesDynamicList.getListItems().size());
        DynamicListElement appParty1 = partiesDynamicList.getListItems().get(0);
        assertEquals("appFirstName1 appLastName1 (Applicant), appSolFN1 appSolLN1, appSolOrgName1", appParty1.getLabel());
        assertEquals(PARTY_ID_PREFIX + "1", appParty1.getCode());

        DynamicListElement appParty2 = partiesDynamicList.getListItems().get(1);
        assertEquals("appFirstName2 appLastName2 (Applicant), appSolFN2 appSolLN2, appSolOrgName2", appParty2.getLabel());
        assertEquals(PARTY_ID_PREFIX + "2", appParty2.getCode());

        DynamicListElement resParty1 = partiesDynamicList.getListItems().get(2);
        assertEquals("resFirstName1 resLastName1 (Respondent), resSolFN1 resSolLN1, resSolOrgName1", resParty1.getLabel());
        assertEquals(PARTY_ID_PREFIX + "4", resParty1.getCode());

        DynamicListElement resParty2 = partiesDynamicList.getListItems().get(3);
        assertEquals("resFirstName2 resLastName2 (Respondent), resSolFN2 resSolLN2, resSolOrgName2", resParty2.getLabel());
        assertEquals(PARTY_ID_PREFIX + "5", resParty2.getCode());
    }

    @Test
    void shouldGetAllocatedBarristerFL401() {
        setupApplicantFL401();
        setupRespondentFl401();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicantFL401)
            .respondentsFL401(respondentFL401)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAllocationService.getAllocatedBarrister(caseData);
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(2, partiesDynamicList.getListItems().size());
        DynamicListElement appParty1 = partiesDynamicList.getListItems().get(0);
        assertEquals("appFirstName1 appLastName1 (Applicant), appSolFN1 appSolLN1, appSolOrgName1", appParty1.getLabel());
        assertEquals(PARTY_ID_PREFIX + "1", appParty1.getCode());

        DynamicListElement resParty1 = partiesDynamicList.getListItems().get(1);
        assertEquals("resFirstName1 resLastName1 (Respondent), resSolFN1 resSolLN1, resSolOrgName1", resParty1.getLabel());
        assertEquals(PARTY_ID_PREFIX + "4", resParty1.getCode());
    }

    @Test
    void shouldGetAllocatedBarristerFL401EmptyApplicant() {
        setupRespondentFl401();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(respondentFL401)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAllocationService.getAllocatedBarrister(caseData);
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(1, partiesDynamicList.getListItems().size());

        DynamicListElement resParty1 = partiesDynamicList.getListItems().get(0);
        assertEquals("resFirstName1 resLastName1 (Respondent), resSolFN1 resSolLN1, resSolOrgName1", resParty1.getLabel());
        assertEquals(PARTY_ID_PREFIX + "4", resParty1.getCode());
    }

    @Test
    void shouldGetAllocatedBarristerFL401EmptyRespondent() {
        setupApplicantFL401();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicantFL401)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAllocationService.getAllocatedBarrister(caseData);
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(1, partiesDynamicList.getListItems().size());
        DynamicListElement appParty1 = partiesDynamicList.getListItems().get(0);
        assertEquals("appFirstName1 appLastName1 (Applicant), appSolFN1 appSolLN1, appSolOrgName1", appParty1.getLabel());
        assertEquals(PARTY_ID_PREFIX + "1", appParty1.getCode());
    }

    @Test
    void shouldGetAllocatedBarristerForNullApplicants() {
        setupRespondentsC100();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondents(allRespondents)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAllocationService.getAllocatedBarrister(caseData);
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(2, partiesDynamicList.getListItems().size());

        DynamicListElement resParty1 = partiesDynamicList.getListItems().get(0);
        assertEquals("resFirstName1 resLastName1 (Respondent), resSolFN1 resSolLN1, resSolOrgName1", resParty1.getLabel());
        assertEquals(PARTY_ID_PREFIX + "4", resParty1.getCode());

        DynamicListElement resParty2 = partiesDynamicList.getListItems().get(1);
        assertEquals("resFirstName2 resLastName2 (Respondent), resSolFN2 resSolLN2, resSolOrgName2", resParty2.getLabel());
        assertEquals(PARTY_ID_PREFIX + "5", resParty2.getCode());
    }

    @Test
    void shouldGetAllocatedBarristerForNullRespondents() {
        setupApplicantsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAllocationService.getAllocatedBarrister(caseData);
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(2, partiesDynamicList.getListItems().size());
        DynamicListElement appParty1 = partiesDynamicList.getListItems().get(0);
        assertEquals("appFirstName1 appLastName1 (Applicant), appSolFN1 appSolLN1, appSolOrgName1", appParty1.getLabel());
        assertEquals(PARTY_ID_PREFIX + "1", appParty1.getCode());

        DynamicListElement appParty2 = partiesDynamicList.getListItems().get(1);
        assertEquals("appFirstName2 appLastName2 (Applicant), appSolFN2 appSolLN2, appSolOrgName2", appParty2.getLabel());
        assertEquals(PARTY_ID_PREFIX + "2", appParty2.getCode());
    }

    @Test
    void shouldThrowExceptionForUndefinedCaseType() {
        setupApplicantsC100();

        CaseData caseData = CaseData.builder()
            .build();

        assertThrows(
            RuntimeException.class,
            () -> barristerAllocationService.getAllocatedBarrister(caseData)
        );
    }

    private Element<PartyDetails> buildPartyDetails(String id, String appFirstName, String appLastName, boolean hasRep,
                                                    String repappFirstName, String repappLastName, String orgName) {
        return Element.<PartyDetails>builder().id(UUID.fromString(PARTY_ID_PREFIX + id))
            .value(getPartyDetails(id, appFirstName, appLastName, hasRep, repappFirstName, repappLastName, orgName))
            .build();
    }

    private PartyDetails getPartyDetails(String id, String appFirstName, String appLastName, boolean hasRep,
                                         String repappFirstName, String repappLastName, String orgName) {
        return PartyDetails.builder()
            .partyId(UUID.fromString(PARTY_ID_PREFIX + id))
            .firstName(appFirstName)
            .lastName(appLastName)
            .doTheyHaveLegalRepresentation(hasRep ? YesNoDontKnow.yes : null)
            .solicitorPartyId(hasRep ? UUID.fromString(SOL_PARTY_ID_PREFIX + id) : null)
            .representativeFirstName(hasRep ? repappFirstName : null)
            .representativeLastName(hasRep ? repappLastName : null)
            .solicitorOrg(Organisation.builder().organisationName(orgName).build())
            .build();
    }

    void setupApplicantsC100() {
        allApplicants.add(buildPartyDetails("1", "appFirstName1", "appLastName1", true, "appSolFN1", "appSolLN1", "appSolOrgName1"));
        allApplicants.add(buildPartyDetails("2", "appFirstName2", "appLastName2", true, "appSolFN2", "appSolLN2", "appSolOrgName2"));
        allApplicants.add(buildPartyDetails("3", "appFirstName3", "appLastName3", false, null, null, null));
    }

    void setupRespondentsC100() {
        allRespondents.add(buildPartyDetails("4", "resFirstName1", "resLastName1", true, "resSolFN1", "resSolLN1", "resSolOrgName1"));
        allRespondents.add(buildPartyDetails("5", "resFirstName2", "resLastName2", true, "resSolFN2", "resSolLN2", "resSolOrgName2"));
        allRespondents.add(buildPartyDetails("6", "resFirstName3", "resLastName3", false, null, null, null));

    }

    void setupApplicantFL401() {
        applicantFL401 = getPartyDetails("1", "appFirstName1", "appLastName1", true, "appSolFN1", "appSolLN1", "appSolOrgName1");
    }

    void setupRespondentFl401() {
        respondentFL401 = getPartyDetails("4", "resFirstName1", "resLastName1", true, "resSolFN1", "resSolLN1", "resSolOrgName1");

    }
}
