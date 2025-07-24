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

@ExtendWith(SpringExtension.class)
class BarristerAllocationServiceTest {
    @InjectMocks
    BarristerAllocationService barristerAllocationService;

    @Test
    void shouldGetAllocatedBarrister() {
        List<Element<PartyDetails>> allApplicants = new ArrayList<>();
        allApplicants.add(buildPartyDetails("1", "appFirstName1", "appLastName1", true, "appSolFN1", "appSolLN1", "appSolOrgName1"));
        allApplicants.add(buildPartyDetails("2", "appFirstName2", "appLastName2", true, "appSolFN2", "appSolLN2", "appSolOrgName2"));
        allApplicants.add(buildPartyDetails("3", "appFirstName3", "appLastName3", false, null, null, null));

        List<Element<PartyDetails>> allRespondents = new ArrayList<>();
        allRespondents.add(buildPartyDetails("4", "resFirstName1", "resLastName1", true, "resSolFN1", "resSolLN1", "resSolOrgName1"));
        allRespondents.add(buildPartyDetails("5", "resFirstName2", "resLastName2", true, "resSolFN2", "resSolLN2", "resSolOrgName2"));
        allRespondents.add(buildPartyDetails("6", "resFirstName3", "resLastName3", false, null, null, null));

        CaseData caseData = CaseData.builder()
            .applicants(allApplicants)
            .respondents(allRespondents)
            .build();

        AllocatedBarrister allocatedBarrister = barristerAllocationService.getAllocatedBarrister(caseData);
        DynamicList partiesDynamicList = allocatedBarrister.getPartyList();

        assertNotNull(allocatedBarrister.getBarristerOrg());

        assertEquals(partiesDynamicList.getValue(), null);
        assertEquals(4, partiesDynamicList.getListItems().size());
        DynamicListElement appParty1 = partiesDynamicList.getListItems().get(0);
        assertEquals("appFirstName1 appLastName1 (Applicant) appSolFN1 appSolLN1 appSolOrgName1", appParty1.getLabel());
        assertEquals("c0651c7d-0db9-47aa-9baa-933013f482f1", appParty1.getCode());

        DynamicListElement appParty2 = partiesDynamicList.getListItems().get(1);
        assertEquals("appFirstName2 appLastName2 (Applicant) appSolFN2 appSolLN2 appSolOrgName2", appParty2.getLabel());
        assertEquals("c0651c7d-0db9-47aa-9baa-933013f482f2", appParty2.getCode());

        DynamicListElement resParty1 = partiesDynamicList.getListItems().get(2);
        assertEquals("resFirstName1 resLastName1 (Respondent) resSolFN1 resSolLN1 resSolOrgName1", resParty1.getLabel());
        assertEquals("c0651c7d-0db9-47aa-9baa-933013f482f4", resParty1.getCode());

        DynamicListElement resParty2 = partiesDynamicList.getListItems().get(3);
        assertEquals("resFirstName2 resLastName2 (Respondent) resSolFN2 resSolLN2 resSolOrgName2", resParty2.getLabel());
        assertEquals("c0651c7d-0db9-47aa-9baa-933013f482f5", resParty2.getCode());
    }

    private Element<PartyDetails> buildPartyDetails(String id, String appFirstName, String appLastName, boolean rep,
                                                    String repappFirstName, String repappLastName, String orgName) {
        return Element.<PartyDetails>builder().id(UUID.fromString("c0651c7d-0db9-47aa-9baa-933013f482e" + id))
            .value(PartyDetails.builder()
                       .partyId(UUID.fromString("c0651c7d-0db9-47aa-9baa-933013f482f" + id))
                       .firstName(appFirstName)
                       .lastName(appLastName)
                       .doTheyHaveLegalRepresentation(rep ? YesNoDontKnow.yes : null)
                       .solicitorPartyId(rep ? UUID.fromString("c0651c7d-0db9-47aa-9baa-933013f482a" + id) : null)
                       .representativeFirstName(rep ? repappFirstName : null)
                       .representativeLastName(rep ? repappLastName : null)
                       .solicitorOrg(Organisation.builder().organisationName(orgName).build())
                       .build())
            .build();
    }
}
