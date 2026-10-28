package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.framework.exceptions.WorkflowException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ExtendedCaseDetailsService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
@PropertySource(value = "classpath:application.yaml")
public class CaseTypeControllerTest {
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private ExtendedCaseDetailsService extendedCaseDetailsService;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CaseTypeController caseTypeController;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";

    @Test
    public void testPopulateExtendedCaseDetails() throws WorkflowException {

        Map<String, Object> caseDataMap = new HashMap<>();
        CaseData caseData = CaseData.builder()
            .build();
        CaseDetails caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .id(1234567890123456L)
            .build();
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        caseTypeController.populateExtendedCaseDetails(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        verify(extendedCaseDetailsService).setExtendedDetails(any(), any());

    }
}
