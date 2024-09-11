package uk.gov.hmcts.reform.prl.services.gatekeeping;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.ListOnNoticeReasonsEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LIST_ON_NOTICE_REASONS_SELECTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SELECTED_AND_ADDITIONAL_REASONS;

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

    @Test
    public void testCleanUpListOnNoticeFields() {
        Map<String, Object> caseDataUpdated = new HashMap<>();
        caseDataUpdated.put(SELECTED_AND_ADDITIONAL_REASONS, "test");
        caseDataUpdated.put(LIST_ON_NOTICE_REASONS_SELECTED, "test");
        listOnNoticeService.cleanUpListOnNoticeFields(caseDataUpdated);
        assertNull(caseDataUpdated.get(SELECTED_AND_ADDITIONAL_REASONS));
        assertNull(caseDataUpdated.get(LIST_ON_NOTICE_REASONS_SELECTED));
    }

}
