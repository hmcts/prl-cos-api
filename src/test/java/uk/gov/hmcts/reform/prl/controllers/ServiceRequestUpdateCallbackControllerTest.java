package uk.gov.hmcts.reform.prl.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.dto.payment.ServiceRequestUpdateDto;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.RequestUpdateCallbackService;
import uk.gov.hmcts.reform.prl.services.payment.FeeService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ServiceRequestUpdateCallbackControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ServiceRequestUpdateCallbackController serviceRequestUpdateCallbackController;

    @Mock
    private RequestUpdateCallbackService requestUpdateCallbackService;

    @Mock
    private LaunchDarklyClient launchDarklyClient;

    @Mock
    private FeeService feesService;

    @Mock
    AuthorisationService authorisationService;

    @Mock
    private FeeResponse feeResponse;

    @Mock
    private ServiceRequestUpdateDto serviceRequestUpdateDto;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String serviceAuthToken = "Bearer TestServiceAuthToken";
    public static final String serviceAuthTokenWithOutBearer = "TestServiceAuthToken";


    @BeforeEach
    void setUp() {

        serviceRequestUpdateDto = ServiceRequestUpdateDto.builder()
            .serviceRequestStatus("paid").ccdCaseNumber("123456").build();

        feeResponse = FeeResponse.builder()
            .code("FEE0325")
            .build();
    }

    @Test
    void testServiceRequestCallBackDetails() throws Exception {

        when(authorisationService.authoriseService(any())).thenReturn(Boolean.TRUE);
        when(launchDarklyClient.isFeatureEnabled(any())).thenReturn(Boolean.TRUE);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        serviceRequestUpdateCallbackController.serviceRequestUpdate(serviceAuthToken, serviceRequestUpdateDto);

        verify(requestUpdateCallbackService).processCallback(serviceRequestUpdateDto);
        verifyNoMoreInteractions(requestUpdateCallbackService);
    }

    @Test
    void testServiceRequestCallBackDetailsServiceAuthWithoutBearer() throws Exception {
        when(authorisationService.authoriseService(any())).thenReturn(Boolean.TRUE);
        when(launchDarklyClient.isFeatureEnabled(any())).thenReturn(Boolean.TRUE);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        serviceRequestUpdateCallbackController.serviceRequestUpdate(serviceAuthTokenWithOutBearer, serviceRequestUpdateDto);

        verify(requestUpdateCallbackService).processCallback(serviceRequestUpdateDto);
        verifyNoMoreInteractions(requestUpdateCallbackService);
    }

    @Test
    void testFeeServiceFeeCodeDetails() throws Exception {
        when(authorisationService.authoriseService(any())).thenReturn(Boolean.TRUE);
        when(launchDarklyClient.isFeatureEnabled(any())).thenReturn(Boolean.TRUE);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        serviceRequestUpdateCallbackController.serviceRequestUpdate(serviceAuthToken, serviceRequestUpdateDto);

        verifyNoMoreInteractions(feesService);
    }

    @Test
    void testServiceRequestCallBackDetailsS2sValidationFailed() throws Exception {
        when(authorisationService.authoriseService(any())).thenReturn(Boolean.FALSE);
        when(launchDarklyClient.isFeatureEnabled(any())).thenReturn(Boolean.TRUE);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        assertThrows(WorkflowException.class, () ->
            serviceRequestUpdateCallbackController.serviceRequestUpdate(serviceAuthToken, serviceRequestUpdateDto)
        );
    }

    @Test
    void testServiceRequestCallBackDetailsWorkFlowException() throws Exception {
        when(authorisationService.authoriseService(any())).thenReturn(Boolean.TRUE);
        when(launchDarklyClient.isFeatureEnabled(any())).thenReturn(Boolean.TRUE);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        doThrow(new RuntimeException()).when(requestUpdateCallbackService)
            .processCallback(serviceRequestUpdateDto);

        assertThrows(WorkflowException.class, () ->
            serviceRequestUpdateCallbackController.serviceRequestUpdate(serviceAuthToken, serviceRequestUpdateDto)
        );
    }
}
