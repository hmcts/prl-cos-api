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

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

@ExtendWith(SpringExtension.class)
class BarristerAddServiceTest extends BarristerTestAbstract {
    @InjectMocks
    BarristerAddService barristerAddService;

    @Test
    void shouldGetAllocatedBarristerC100() {
        setupApplicantsC100();
        setupRespondentsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .respondents(allRespondents)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(caseData);
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(4, partiesDynamicList.getListItems().size());

        assertPartyToAdd(partiesDynamicList, true, PARTY_ID_PREFIX, 0, 1);
        assertPartyToAdd(partiesDynamicList, true, PARTY_ID_PREFIX, 1, 2);
        assertPartyToAdd(partiesDynamicList, false, PARTY_ID_PREFIX, 2, 5);
        assertPartyToAdd(partiesDynamicList, false, PARTY_ID_PREFIX, 3, 6);
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

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(caseData);
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());
        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(2, partiesDynamicList.getListItems().size());

        assertPartyToAdd(partiesDynamicList, true, PARTY_ID_PREFIX, 0, 1);
        assertPartyToAdd(partiesDynamicList, false, PARTY_ID_PREFIX, 1, 1);
    }

    @Test
    void shouldGetAllocatedBarristerFL401EmptyApplicant() {
        setupRespondentFl401();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .respondentsFL401(respondentFL401)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(caseData);
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(1, partiesDynamicList.getListItems().size());

        assertPartyToAdd(partiesDynamicList, false, PARTY_ID_PREFIX, 0, 1);
    }

    @Test
    void shouldGetAllocatedBarristerFL401EmptyRespondent() {
        setupApplicantFL401();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("FL401")
            .applicantsFL401(applicantFL401)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(caseData);
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(1, partiesDynamicList.getListItems().size());
        DynamicListElement appParty1 = partiesDynamicList.getListItems().get(0);

        assertPartyToAdd(partiesDynamicList, true, PARTY_ID_PREFIX, 0, 1);
    }

    @Test
    void shouldGetAllocatedBarristerForNullApplicants() {
        setupRespondentsC100();
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .respondents(allRespondents)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(caseData);
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(2, partiesDynamicList.getListItems().size());

        assertPartyToAdd(partiesDynamicList, false, PARTY_ID_PREFIX, 0, 5);
        assertPartyToAdd(partiesDynamicList, false, PARTY_ID_PREFIX, 1, 6);
    }

    @Test
    void shouldGetAllocatedBarristerForNullRespondents() {
        setupApplicantsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAddService.getAllocatedBarrister(caseData);
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(2, partiesDynamicList.getListItems().size());
        assertPartyToAdd(partiesDynamicList, true, PARTY_ID_PREFIX, 0, 1);
        assertPartyToAdd(partiesDynamicList, true, PARTY_ID_PREFIX, 1, 2);
    }

    @Test
    void shouldThrowExceptionForUndefinedCaseType() {
        setupApplicantsC100();

        CaseData caseData = CaseData.builder()
            .build();

        assertThrows(
            RuntimeException.class,
            () -> barristerAddService.getAllocatedBarrister(caseData)
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

    protected void assertPartyToAdd(DynamicList listOfBarristers, boolean appOrResp, String prefix, int itemIndex, int partyIndex) {
        String appRepPrefix = appOrResp ? "App" : "Resp";
        DynamicListElement appParty = listOfBarristers.getListItems().get(itemIndex);
        String label = appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + " "
            + (appOrResp ? "(Applicant)" : "(Respondent)") + ", "
            + appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + ", "
            + "Org" + partyIndex;
        assertEquals(label, appParty.getLabel());
        assertEquals(prefix + partyIndex, appParty.getCode());
    }

}
