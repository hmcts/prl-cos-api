package uk.gov.hmcts.reform.prl.services.barrister;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.applicant;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.respondent;

@ExtendWith(SpringExtension.class)
class BarristerRemoveServiceTest extends BarristerTestAbstract {
    @InjectMocks
    BarristerRemoveService barristerRemoveService;

    @Mock
    private UserDetails userDetails;

    @Test
    void shouldGetCaseworkerRemovalBarristersC100() {
        setupApplicantsC100();
        setupRespondentsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .respondents(allRespondents)
            .build();

        DynamicList listOfBarristersToRemove = barristerRemoveService.getBarristerListToRemove(caseData, userDetails, "");

        assertEquals(listOfBarristersToRemove.getValue(), null);
        assertEquals(2, listOfBarristersToRemove.getListItems().size());

        assertPartyToRemove(listOfBarristersToRemove, applicant, BARRISTER_PARTY_ID_PREFIX, 0, 3);
        assertPartyToRemove(listOfBarristersToRemove, respondent, BARRISTER_PARTY_ID_PREFIX, 1, 7);
    }

    protected void assertPartyToRemove(DynamicList listOfBarristersToRemove, PartyEnum partyEnum, String prefix, int itemIndex, int partyIndex) {
        String appRepPrefix = partyEnum == applicant ? "App" : "Resp";
        DynamicListElement appParty = listOfBarristersToRemove.getListItems().get(itemIndex);
        String label = appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + " "
            + (partyEnum == applicant ? "(Applicant)" : "(Respondent)") + ", "
            + appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + ", "
            + "BarFN" + partyIndex + " " + "BarLN" + partyIndex;
        assertEquals(label, appParty.getLabel());
        assertEquals(prefix + partyIndex, appParty.getCode());
    }

}
