package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyMap;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseFlagsWaServiceTest {

    @Mock
    private EventService eventPublisher;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AllTabServiceImpl allTabService;

    @InjectMocks
    private CaseFlagsWaService caseFlagsWaService;

    private static final String REQUESTED = "Requested";
    private static final String ACTIVE = "Active";


    @Test
    public void testSetUpWaTaskForCaseFlagsEventHandler() {
        caseFlagsWaService.setUpWaTaskForCaseFlagsEventHandler("auth-token", CallbackRequest.builder().build());
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    public void testSearchAndUpdateCaseLevelFlags() {
        FlagDetail modifiedFlagDetail = FlagDetail.builder().status("Active").build();
        Flags flags = getCaseLevelFlags(REQUESTED);
        CaseData caseData = CaseData.builder().caseFlags(flags)
            .build();

        Element<FlagDetail> recentlyModifiedFlag =  Element.<FlagDetail>builder()
            .id(flags.getDetails().getFirst().getId())
            .value(modifiedFlagDetail)
            .build();

        caseFlagsWaService.searchAndUpdateCaseFlags(caseData, recentlyModifiedFlag);

        assertEquals(modifiedFlagDetail.getStatus(), caseData.getCaseFlags().getDetails().getFirst().getValue().getStatus());
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
            .id(partyLevelFlagDetails.getFirst().getId())
            .value(modifiedFlagDetail)
            .build();

        caseFlagsWaService.searchAndUpdateCaseFlags(caseData, recentlyModifiedFlag);

        assertEquals(modifiedFlagDetail.getStatus(), caseData.getAllPartyFlags()
            .getCaApplicant1ExternalFlags().getDetails().getFirst().getValue().getStatus());
    }

    @Test
    public void testSearchAndUpdateBothCaseLevelAndPartyLevelFlags() {

        Flags caApplicant1ExternalFlags = getApplicant1ExternalFlag1();

        FlagDetail applicant2ExternalFlag1 = FlagDetail.builder().status("Requested").build();
        List<Element<FlagDetail>> applicant2ExternalFlagDetails = new ArrayList<>();
        applicant2ExternalFlagDetails.add(ElementUtils.element(applicant2ExternalFlag1));
        Flags caApplicant2ExternalFlags = Flags.builder().details(applicant2ExternalFlagDetails).build();

        Flags caseLevelFlags = getCaseLevelFlags(REQUESTED);

        FlagDetail modifiedFlagDetail = FlagDetail.builder().status("Active").build();

        CaseData caseData = CaseData.builder()
            .caseFlags(caseLevelFlags)
            .allPartyFlags(AllPartyFlags.builder()
                               .caApplicant1ExternalFlags(caApplicant1ExternalFlags)
                               .caApplicant2ExternalFlags(caApplicant2ExternalFlags)
                               .build())
            .build();

        Element<FlagDetail> recentlyModifiedFlag =  Element.<FlagDetail>builder()
            .id(applicant2ExternalFlagDetails.getFirst().getId())
            .value(modifiedFlagDetail)
            .build();

        caseFlagsWaService.searchAndUpdateCaseFlags(caseData, recentlyModifiedFlag);

        assertEquals(modifiedFlagDetail.getStatus(), caseData.getAllPartyFlags()
            .getCaApplicant2ExternalFlags().getDetails().getFirst().getValue().getStatus());
    }

    @Test
    public void testCheckAllRequestedFlagsAndCloseTasksWhenCaseFlagsIsInRequestedState() {
        Flags caseLevelFlags = getCaseLevelFlags(REQUESTED);

        CaseData caseData = CaseData.builder()
            .caseFlags(caseLevelFlags)
            .build();

        caseFlagsWaService.checkAllRequestedFlagsAndCloseTask(caseData);

        verifyNoInteractions(allTabService);
    }

    @Test
    public void testCheckAllRequestedFlagsAndCloseTasksWhenCaseFlagsAreNotInRequestedState() {

        Flags caseLevelFlags = getCaseLevelFlags(ACTIVE);

        CaseData caseData = CaseData.builder()
            .id(123)
            .caseFlags(caseLevelFlags)
            .build();

        StartAllTabsUpdateDataContent dataContent = new StartAllTabsUpdateDataContent("",
                                                                                  EventRequestData.builder().build(),
                                                                                  StartEventResponse.builder().build(),
                                                                                  new HashMap<>(),
                                                                                  caseData,
                                                                                  UserDetails.builder().build());

        when(allTabService.getStartUpdateForSpecificEvent(anyString(), eq(CaseEvent.CLOSE_REVIEW_RA_REQUEST_TASK.getValue())))
            .thenReturn(dataContent);

        caseFlagsWaService.checkAllRequestedFlagsAndCloseTask(caseData);

        verify(allTabService, times(1))
            .getStartUpdateForSpecificEvent(anyString(), eq(CaseEvent.CLOSE_REVIEW_RA_REQUEST_TASK.getValue()));

        verify(allTabService, times(1))
            .submitAllTabsUpdate(anyString(), anyString(), any(StartEventResponse.class), any(EventRequestData.class), anyMap());
    }

    @Test
    public void testCheckCaseFlagsToCreateTaskWhenNoNewCaseFlags() {
        Flags caseLevelFlagsBefore = getCaseLevelFlags(REQUESTED);
        CaseData caseDataBefore = CaseData.builder()
            .id(123)
            .caseFlags(caseLevelFlagsBefore)
            .build();

        Flags caseLevelFlags = getCaseLevelFlags(ACTIVE);
        CaseData caseData = CaseData.builder()
            .id(123)
            .caseFlags(caseLevelFlags)
            .build();
        caseFlagsWaService.checkCaseFlagsToCreateTask(caseData, caseDataBefore);
        verifyNoInteractions(allTabService);
    }

    @Test
    public void testCheckCaseFlagsToCreateTaskWhenNewCaseFlags() {
        CaseData caseDataBefore = CaseData.builder()
            .id(123)
            .caseFlags(Flags.builder().details(new ArrayList<>()).build())
            .build();

        Flags caseLevelFlags = getCaseLevelFlags(REQUESTED);
        CaseData caseData = CaseData.builder()
            .id(123)
            .caseFlags(caseLevelFlags)
            .build();

        StartAllTabsUpdateDataContent dataContent = new StartAllTabsUpdateDataContent("",
                                                                                      EventRequestData.builder().build(),
                                                                                      StartEventResponse.builder().build(),
                                                                                      new HashMap<>(),
                                                                                      caseData,
                                                                                      UserDetails.builder().build());
        when(allTabService.getStartUpdateForSpecificEvent(anyString(), eq(CaseEvent.CREATE_WA_TASK_FOR_CTSC_CASE_FLAGS.getValue())))
            .thenReturn(dataContent);

        caseFlagsWaService.checkCaseFlagsToCreateTask(caseData, caseDataBefore);

        verify(allTabService, times(1))
            .getStartUpdateForSpecificEvent(anyString(), eq(CaseEvent.CREATE_WA_TASK_FOR_CTSC_CASE_FLAGS.getValue()));

        verify(allTabService, times(1))
            .submitAllTabsUpdate(anyString(), anyString(), any(StartEventResponse.class), any(EventRequestData.class), anyMap());
    }

    @Test
    public void testSetSelectedFlagsForCaseLevelFlags() throws IOException {

        Flags caseLevelFlags = getCaseLevelFlags(REQUESTED);
        CaseData caseData = CaseData.builder()
            .id(123)
            .allPartyFlags(AllPartyFlags.builder().build())
            .selectedFlags(new ArrayList<>())
            .caseFlags(caseLevelFlags)
            .build();

        Assert.assertTrue(caseData.getSelectedFlags().isEmpty());
        when(objectMapper.writeValueAsString(caseLevelFlags)).thenReturn("dummyObjectString");
        when(objectMapper.readValue("dummyObjectString", Flags.class)).thenReturn(caseLevelFlags);
        caseFlagsWaService.setSelectedFlags(caseData);

        assertEquals(1, caseData.getSelectedFlags().size());
    }

    @Test
    public void testSetSelectedFlagsForPartyLevelFlags() throws IOException {

        Flags caApplicant1ExternalFlags = getApplicant1ExternalFlag1();
        CaseData caseData = CaseData.builder()
            .id(123)
            .allPartyFlags(AllPartyFlags.builder().caApplicant1ExternalFlags(caApplicant1ExternalFlags).build())
            .selectedFlags(new ArrayList<>())
            .caseFlags(Flags.builder().details(new ArrayList<>()).build())
            .build();

        Assert.assertTrue(caseData.getSelectedFlags().isEmpty());

        when(objectMapper.writeValueAsString(caApplicant1ExternalFlags)).thenReturn("dummyObjectString");
        when(objectMapper.readValue("dummyObjectString", Flags.class)).thenReturn(caApplicant1ExternalFlags);

        caseFlagsWaService.setSelectedFlags(caseData);

        assertEquals(1, caseData.getSelectedFlags().size());
        assertEquals(2, caseData.getSelectedFlags().getFirst().getValue().getDetails().size());
    }

    @Test
    public void testValidateAllFlags() {
        List<Element<Flags>> selectedFlags = new ArrayList<>();
        selectedFlags.add(ElementUtils.element(getApplicant1ExternalFlag1()));
        selectedFlags.add(ElementUtils.element(getCaseLevelFlags(REQUESTED)));
        CaseData caseData = CaseData.builder()
            .id(123)
            .allPartyFlags(AllPartyFlags.builder().build())
            .selectedFlags(selectedFlags)
            .caseFlags(Flags.builder().build())
            .build();

        caseData.getSelectedFlags().getFirst().getValue().getDetails().getFirst().getValue().setDateTimeModified(LocalDateTime.now());
        caseData.getSelectedFlags().getFirst().getValue().getDetails().getFirst().getValue().setStatus(ACTIVE);

        Element<FlagDetail> actualDetails = caseFlagsWaService.validateAllFlags(caseData);
        assertEquals(ACTIVE, actualDetails.getValue().getStatus());
    }

    private Flags getCaseLevelFlags(String status) {
        FlagDetail caseLevelDetail = FlagDetail.builder().status(status).build();
        List<Element<FlagDetail>> caseLevelFlagDetails = new ArrayList<>();
        caseLevelFlagDetails.add(ElementUtils.element(caseLevelDetail));
        return Flags.builder().details(caseLevelFlagDetails).build();
    }

    private Flags getApplicant1ExternalFlag1() {
        FlagDetail applicant1ExternalFlag1 = FlagDetail.builder().status(REQUESTED)
            .dateTimeModified(LocalDateTime.now().minusDays(2)).build();
        FlagDetail applicant1ExternalFlag2 = FlagDetail.builder().status(REQUESTED)
            .dateTimeModified(LocalDateTime.now().minusDays(1)).build();
        List<Element<FlagDetail>> partyLevelFlagDetails = new ArrayList<>();
        partyLevelFlagDetails.add(ElementUtils.element(applicant1ExternalFlag1));
        partyLevelFlagDetails.add(ElementUtils.element(applicant1ExternalFlag2));
        return Flags.builder().details(partyLevelFlagDetails).build();
    }
}
