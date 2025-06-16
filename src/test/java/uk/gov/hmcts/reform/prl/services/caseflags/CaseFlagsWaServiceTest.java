package uk.gov.hmcts.reform.prl.services.caseflags;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseFlagsWaServiceTest {

    @Mock
    private EventService eventPublisher;

    @InjectMocks
    private CaseFlagsWaService caseFlagsWaService;


    @Test
    public void testSetUpWaTaskForCaseFlagsEventHandler() {
        caseFlagsWaService.setUpWaTaskForCaseFlagsEventHandler("auth-token", CallbackRequest.builder().build());
        Mockito.verify(eventPublisher,Mockito.times(1)).publishEvent(Mockito.any());
    }

    @Test
    public void testSearchAndUpdateCaseFlags() {
        FlagDetail flagDetail = FlagDetail.builder().status("Requested").build();
        FlagDetail modifiedFlagDetail = FlagDetail.builder().status("Active").build();
        List<Element<FlagDetail>> caseLevelFlagDetails = new ArrayList<>();
        caseLevelFlagDetails.add(ElementUtils.element(flagDetail));
        Flags flags = Flags.builder().details(caseLevelFlagDetails).build();
        CaseData caseData = CaseData.builder().caseFlags(flags)
            .build();

        Element<FlagDetail> recentlyModifiedFlag =  Element.<FlagDetail>builder()
            .id(caseLevelFlagDetails.get(0).getId())
            .value(modifiedFlagDetail)
            .build();

        caseFlagsWaService.searchAndUpdateCaseFlags(caseData, recentlyModifiedFlag);

        Assert.assertEquals(modifiedFlagDetail.getStatus(), caseData.getCaseFlags().getDetails().get(0).getValue().getStatus());
    }
}
