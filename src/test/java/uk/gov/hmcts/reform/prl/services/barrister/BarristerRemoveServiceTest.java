package uk.gov.hmcts.reform.prl.services.barrister;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.junit.Assert.assertEquals;

@ExtendWith(SpringExtension.class)
class BarristerRemoveServiceTest extends BarristerTestAbstract {
    @InjectMocks
    BarristerRemoveService barristerRemoveService;

    @Test
    void shouldGetCaseworkerRemovalBarristersC100() {
        setupApplicantsC100();
        setupRespondentsC100();

        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication("C100")
            .applicants(allApplicants)
            .respondents(allRespondents)
            .build();

        DynamicList listOfBarristersToRemove = barristerRemoveService.getBarristerListToRemove(caseData);

        assertEquals(listOfBarristersToRemove.getValue(), null);
        assertEquals(2, listOfBarristersToRemove.getListItems().size());

        assertPartyToRemove(listOfBarristersToRemove, true, BARRISTER_PARTY_ID_PREFIX, 0, 3);
        assertPartyToRemove(listOfBarristersToRemove, false, BARRISTER_PARTY_ID_PREFIX, 1, 7);
    }

    protected void assertPartyToRemove(DynamicList listOfBarristersToRemove, boolean appResp, String prefix, int itemIndex, int partyIndex) {
        String appRepPrefix = appResp ? "App" : "Resp";
        DynamicListElement appParty = listOfBarristersToRemove.getListItems().get(itemIndex);
        String label = appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + " "
            + (appResp ? "(Applicant)" : "(Respondent)") + ", "
            + appRepPrefix + "FN" + partyIndex + " " + appRepPrefix + "LN" + partyIndex + ", "
            + "BarFN" + partyIndex + " " + "BarLN" + partyIndex;
        assertEquals(label, appParty.getLabel());
        assertEquals(prefix + partyIndex, appParty.getCode());
    }

}
