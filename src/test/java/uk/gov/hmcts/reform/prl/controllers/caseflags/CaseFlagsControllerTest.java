package uk.gov.hmcts.reform.prl.controllers.caseflags;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.caseflags.CaseFlagsWaService;
import uk.gov.hmcts.reform.prl.services.caseflags.FlagsService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseFlagsControllerTest {

    public static final String AUTH_TOKEN = "auth-token";
    public static final String SERVICE_TOKEN = "service-token";
    public static final String CLIENT_CONTEXT = "client-context";
    private static final String REQUESTED = "Requested";
    private static final String ACTIVE = "Active";

    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private CaseFlagsWaService caseFlagsWaService;
    @Mock
    private FlagsService flagsService;
    @Mock
    ObjectMapper objectMapper;

    @InjectMocks
    private CaseFlagsController caseFlagsController;

    @Test
    public void testSetUpWaTaskForCaseFlags2() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        caseFlagsController
            .setUpWaTaskForCaseFlags2(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());

        verify(caseFlagsWaService, times(1))
            .setUpWaTaskForCaseFlagsEventHandler(Mockito.any(),Mockito.any());
    }

    @Test(expected = RuntimeException.class)
    public void testSetUpWaTaskForCaseFlags2WhenAuthorisationFails() {

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
        caseFlagsController
            .setUpWaTaskForCaseFlags2(AUTH_TOKEN, SERVICE_TOKEN, CallbackRequest.builder().build());
    }

    @Test
    public void testHandleAboutToStart() {
        Map<String, Object> aboutToStartMap = new HashMap<>();
        aboutToStartMap.put("selectedFlags", new ArrayList<>());

        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseData caseData = CaseData.builder().id(12345L).build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        caseFlagsController.handleAboutToStart(AUTH_TOKEN, callbackRequest);

        verify(caseFlagsWaService).setSelectedFlags(any(CaseData.class));
    }

    @Test
    public void testCheckWorkAllocationTaskStatusWhenCaseFlagsStatusIsRequested() {

        FlagDetail caseLevelDetail = FlagDetail.builder().status(REQUESTED).build();
        List<Element<FlagDetail>> caseLevelFlagDetails = new ArrayList<>();
        caseLevelFlagDetails.add(ElementUtils.element(caseLevelDetail));
        Flags caseLevelFlag = Flags.builder().details(caseLevelFlagDetails).build();

        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseData caseData = CaseData.builder().id(12345L).build();
        Element<FlagDetail> mostRecentlyModified = caseLevelFlag.getDetails().getFirst();
        when(caseFlagsWaService.validateAllFlags(caseData)).thenReturn(mostRecentlyModified);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = caseFlagsController.handleAboutToSubmit(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest);
        Assert.assertTrue(response.getErrors().size() > 0);
        verify(caseFlagsWaService).validateAllFlags(caseData);
        verify(caseFlagsWaService, never()).searchAndUpdateCaseFlags(caseData, mostRecentlyModified);

    }

    @Test
    public void testCheckWorkAllocationTaskStatusWhenCaseFlagsStatusIsNotRequested() {
        FlagDetail caseLevelDetail = FlagDetail.builder().status(REQUESTED).build();
        List<Element<FlagDetail>> caseLevelFlagDetails = new ArrayList<>();
        caseLevelFlagDetails.add(ElementUtils.element(caseLevelDetail));
        Flags caseLevelFlag = Flags.builder().details(caseLevelFlagDetails).build();

        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseData caseData = CaseData.builder().id(12345L).build();
        Element<FlagDetail> mostRecentlyModified = caseLevelFlag.getDetails().getFirst();
        mostRecentlyModified.getValue().setStatus(ACTIVE);

        when(caseFlagsWaService.validateAllFlags(caseData)).thenReturn(mostRecentlyModified);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = caseFlagsController.handleAboutToSubmit(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest);
        Assert.assertTrue(response.getErrors().isEmpty());

        verify(caseFlagsWaService).validateAllFlags(caseData);
        verify(caseFlagsWaService).searchAndUpdateCaseFlags(caseData, mostRecentlyModified);

    }

    @Test
    public void testHandleSubmitted() {
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        CaseData caseData = CaseData.builder().id(12345L).build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        caseFlagsController.handleSubmitted(AUTH_TOKEN, callbackRequest);

        verify(caseFlagsWaService).checkAllRequestedFlagsAndCloseTask(any(CaseData.class));
    }


    @Test
    public void testReviewLangAndSmAboutToStart() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(
                CaseDetails.builder()
                    .data(Map.of())
                    .build())
            .build();
        caseFlagsController
            .handleAboutToStartForReviewLangSm(AUTH_TOKEN, SERVICE_TOKEN, CLIENT_CONTEXT,callbackRequest);
        verify(flagsService, times(1)).prepareSelectedReviewLangAndSmReq(Map.of(), CLIENT_CONTEXT);
        verify(authorisationService, times(1)).isAuthorized(AUTH_TOKEN, SERVICE_TOKEN);
    }

    @Test
    public void testReviewLangAndSmAboutToStartWhenAuthorisationFails() {
        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(false);
        CallbackRequest callbackRequest = CallbackRequest.builder().build();
        assertThrows(RuntimeException.class, () -> {
            caseFlagsController
                .handleAboutToStartForReviewLangSm(AUTH_TOKEN, SERVICE_TOKEN, CLIENT_CONTEXT, callbackRequest);
        });
        verify(flagsService, never()).prepareSelectedReviewLangAndSmReq(Map.of(), CLIENT_CONTEXT);
    }

    @Test
    public void testHandleAboutToSubmitEventWithErrors() {
        List<String> errors = List.of("Please select");
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetailsBefore(
                CaseDetails.builder()
                    .data(Map.of())
                    .build()
            )
            .caseDetails(
                CaseDetails.builder()
                    .data(Map.of())
                    .build())
            .build();

        when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(true);
        when(flagsService.validateNewFlagStatus(Map.of()))
            .thenReturn(errors);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = caseFlagsController
            .handleAboutToSubmitForReviewLangSm(AUTH_TOKEN, SERVICE_TOKEN, callbackRequest);
        assertThat(aboutToStartOrSubmitCallbackResponse.getErrors()).containsAll(errors);
        verify(flagsService, times(1)).validateNewFlagStatus(Map.of());
    }
}

