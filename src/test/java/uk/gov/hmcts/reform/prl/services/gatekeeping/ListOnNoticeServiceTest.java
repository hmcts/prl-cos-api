package uk.gov.hmcts.reform.prl.services.gatekeeping;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.ListOnNoticeReasonsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EmailService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LIST_ON_NOTICE_REASONS_SELECTED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SELECTED_AND_ADDITIONAL_REASONS;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ListOnNoticeServiceTest {

    @InjectMocks
    ListOnNoticeService listOnNoticeService;

    @Mock
    private EmailService emailService;

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
    public void testSendNotification() {
        List<Element<PartyDetails>> applicants = new ArrayList<>();
        PartyDetails applicant1 = PartyDetails.builder().solicitorEmail("abc@test.com").build();
        PartyDetails applicant2 = PartyDetails.builder().email("abc@test.com").build();
        applicants.add(element(applicant1));
        applicants.add(element(applicant2));
        CaseData caseData = CaseData.builder()
            .id(123455L)
            .caseTypeOfApplication("C100")
            .applicants(applicants).build();
        doNothing().when(emailService)
            .send(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any());
        listOnNoticeService.sendNotification(caseData, "test");
        verify(emailService,times(2)).send(Mockito.anyString(),
                                           Mockito.any(),
                                           Mockito.any(), Mockito.any());

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
