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
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
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
    public void testSearchAndUpdateCaseLevelFlags() {
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

    @Test
    public void testSearchAndUpdatePartyLevelFlags() {
        FlagDetail flagDetail1 = FlagDetail.builder().status("Requested").build();
        FlagDetail flagDetail2 = FlagDetail.builder().status("Requested").build();
        FlagDetail modifiedFlagDetail = FlagDetail.builder().status("Active").build();

        List<Element<FlagDetail>> partyLevelFlagDetails = new ArrayList<>();
        partyLevelFlagDetails.add(ElementUtils.element(flagDetail1));
        partyLevelFlagDetails.add(ElementUtils.element(flagDetail2));

        Flags flags = Flags.builder().details(partyLevelFlagDetails).build();
        CaseData caseData = CaseData.builder()
            .caseFlags(Flags.builder().build())
            .allPartyFlags(AllPartyFlags.builder()
                               .caApplicant1ExternalFlags(flags)
                               .build())
            .build();

        Element<FlagDetail> recentlyModifiedFlag =  Element.<FlagDetail>builder()
            .id(partyLevelFlagDetails.get(0).getId())
            .value(modifiedFlagDetail)
            .build();

        caseFlagsWaService.searchAndUpdateCaseFlags(caseData, recentlyModifiedFlag);

        Assert.assertEquals(modifiedFlagDetail.getStatus(), caseData.getAllPartyFlags()
            .getCaApplicant1ExternalFlags().getDetails().get(0).getValue().getStatus());
    }

    @Test
    public void testSearchAndUpdateBothCaseLevelAndPartyLevelFlags() {
        FlagDetail applicant1ExternalFlag1 = FlagDetail.builder().status("Requested").build();
        FlagDetail applicant1ExternalFlag2 = FlagDetail.builder().status("Requested").build();
        List<Element<FlagDetail>> partyLevelFlagDetails = new ArrayList<>();
        partyLevelFlagDetails.add(ElementUtils.element(applicant1ExternalFlag1));
        partyLevelFlagDetails.add(ElementUtils.element(applicant1ExternalFlag2));
        Flags caApplicant1ExternalFlags = Flags.builder().details(partyLevelFlagDetails).build();

        FlagDetail applicant2ExternalFlag1 = FlagDetail.builder().status("Requested").build();
        List<Element<FlagDetail>> applicant2ExternalFlagDetails = new ArrayList<>();
        applicant2ExternalFlagDetails.add(ElementUtils.element(applicant2ExternalFlag1));
        Flags caApplicant2ExternalFlags = Flags.builder().details(applicant2ExternalFlagDetails).build();

        FlagDetail caseLevelDetail = FlagDetail.builder().status("Requested").build();
        List<Element<FlagDetail>> caseLevelFlagDetails = new ArrayList<>();
        caseLevelFlagDetails.add(ElementUtils.element(caseLevelDetail));
        Flags caseLevelFlags = Flags.builder().details(caseLevelFlagDetails).build();

        FlagDetail modifiedFlagDetail = FlagDetail.builder().status("Active").build();

        CaseData caseData = CaseData.builder()
            .caseFlags(caseLevelFlags)
            .allPartyFlags(AllPartyFlags.builder()
                               .caApplicant1ExternalFlags(caApplicant1ExternalFlags)
                               .caApplicant2ExternalFlags(caApplicant2ExternalFlags)
                               .build())
            .build();

        Element<FlagDetail> recentlyModifiedFlag =  Element.<FlagDetail>builder()
            .id(applicant2ExternalFlagDetails.get(0).getId())
            .value(modifiedFlagDetail)
            .build();

        caseFlagsWaService.searchAndUpdateCaseFlags(caseData, recentlyModifiedFlag);

        Assert.assertEquals(modifiedFlagDetail.getStatus(), caseData.getAllPartyFlags()
            .getCaApplicant2ExternalFlags().getDetails().get(0).getValue().getStatus());
    }
}
