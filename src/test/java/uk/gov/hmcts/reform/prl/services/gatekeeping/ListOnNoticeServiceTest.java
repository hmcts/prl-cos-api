package uk.gov.hmcts.reform.prl.services.gatekeeping;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.ListOnNoticeReasonsEnum;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ListOnNoticeServiceTest {

    @InjectMocks
    ListOnNoticeService listOnNoticeService;

    @Test
    public void testReturnReasonsSelectedWhenReasonsSelected() {
        List<String> reasonsSelected = new ArrayList<>();
        reasonsSelected.add("childrenResideWithApplicantAndBothProtectedByNonMolestationOrder");
        reasonsSelected.add("noEvidenceOnRespondentSeekToFrustrateTheProcessIfTheyWereGivenNotice");
        String reasonsSelectedString = ListOnNoticeReasonsEnum.getDisplayedValue("childrenResideWithApplicantAndBothProtectedByNonMolestationOrder")
            + "\n\n" + ListOnNoticeReasonsEnum.getDisplayedValue("noEvidenceOnRespondentSeekToFrustrateTheProcessIfTheyWereGivenNotice") + "\n\n";
        assertEquals(reasonsSelectedString,listOnNoticeService.getReasonsSelected(reasonsSelected,Long.valueOf("11111111111111")));
    }

    @Test
    public void testReturnReasonsSelectedWhenNoReasonsSelected() {
        assertEquals(null,listOnNoticeService.getReasonsSelected(null,Long.valueOf("11111111111111")));
    }

}
