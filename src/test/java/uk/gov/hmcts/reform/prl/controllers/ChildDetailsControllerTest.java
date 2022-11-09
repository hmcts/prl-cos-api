package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.sendandreply.MessageMetaData;
import uk.gov.hmcts.reform.prl.services.ChildDetailsService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply.SEND;




@RunWith(MockitoJUnitRunner.class)
public class ChildDetailsControllerTest {


    @InjectMocks
    ChildDetailsController childDetailsController;

    @Mock
    ChildDetailsService childDetailsService;

    @Mock
    ObjectMapper objectMapper;

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;
    CallbackRequest callbackRequest;
    String auth = "authorisation";

    @Before
   public void setUp() {
        caseDataMap = new HashMap<>();

        caseData = CaseData.builder()
            .id(12345678L)
            .chooseSendOrReply(SEND)
            .build();

        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(caseDataMap)
            .build();
        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
    }

    @Test
   public void handleAboutToStart() {
        Map<String, Object> aboutToStartMap = new HashMap<>();
        aboutToStartMap.put("messageObject", MessageMetaData.builder().build());
        when(childDetailsService.getApplicantDetails(caseData)).thenReturn(aboutToStartMap);
        childDetailsController.handleAboutToStart(auth, callbackRequest);
        verify(childDetailsService).getApplicantDetails(caseData);
    }
}