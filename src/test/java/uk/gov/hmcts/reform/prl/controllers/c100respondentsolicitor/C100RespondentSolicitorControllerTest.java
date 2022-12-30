package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.RespondentSolicitorMiamService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.Silent.class)
public class C100RespondentSolicitorControllerTest {


    @InjectMocks
    private C100RespondentSolicitorController c100RespondentSolicitorController;

    @Mock
    private RespondentSolicitorMiamService respondentSolicitorMiamService;

    @Mock
    private ObjectMapper objectMapper;


    @org.junit.Test
    public void testServiceOfApplicationAboutToStart() {

        Map<String, Object> caseData = new HashMap<>();
        CaseData caseData1 = CaseData.builder()
            .build();
        caseData.put("miamHeader", "TestHeader");
        caseData.put("option1", "1");

        when(objectMapper.convertValue(caseData, CaseData.class)).thenReturn(caseData1);
        when(respondentSolicitorMiamService.getCollapsableOfWhatIsMiamPlaceHolder()).thenReturn("Collapsable");

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(1L)
                             .data(caseData).build()).build();


        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = c100RespondentSolicitorController
            .handleAboutToStartMiam(callbackRequest);
        assertEquals("Collapsable", aboutToStartOrSubmitCallbackResponse.getData().get("whatIsMiamPlaceHolder"));
        assertEquals("1", aboutToStartOrSubmitCallbackResponse.getData().get("option1"));
        assertEquals("TestHeader", aboutToStartOrSubmitCallbackResponse.getData().get("miamHeader"));
    }
}



