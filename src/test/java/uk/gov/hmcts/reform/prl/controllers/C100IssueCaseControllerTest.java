package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.C100IssueCaseService;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class C100IssueCaseControllerTest {

    public static final String authToken = "Bearer TestAuthToken";

    @InjectMocks
    private C100IssueCaseController c100IssueCaseController;

    @Mock
    private C100IssueCaseService c100IssueCaseService;

    @Test
    public void testIssueAndSendLocalCourt() throws Exception {

        CaseData caseData = CaseData.builder()
            .id(123L)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                                                       .data(stringObjectMap).build()).build();

        when(c100IssueCaseService.issueAndSendToLocalCourt(
            any(String.class),
            any(CallbackRequest.class)
        )).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = c100IssueCaseController.issueAndSendToLocalCourt(authToken,
                                                                                                                                     callbackRequest);

        assertNotNull(aboutToStartOrSubmitCallbackResponse);
        assertEquals(stringObjectMap, aboutToStartOrSubmitCallbackResponse.getData());


    }



}

