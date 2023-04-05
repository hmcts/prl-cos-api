package uk.gov.hmcts.reform.prl.controllers.gatekeeping;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LIST_ON_NOTICE_REASONS_SELECTED;


@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class ListOnNoticeControllerTest {

    @InjectMocks
    ListOnNoticeController listOnNoticeController;
    @Mock
    private ObjectMapper objectMapper;

    public static final String authToken = "Bearer TestAuthToken";

    @Test
    public void testListOnNoticeMidEvent() throws Exception {

        CaseData caseData = CaseData.builder()
            .courtName("testcourt")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        Map<String, Object> summaryTabFields = Map.of(
            "field4", "value4",
            "field5", "value5"
        );

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        List<String> reasonsSelected = new ArrayList<>();
        reasonsSelected.add("childrenResideWithApplicantAndBothProtectedByNonMolestationOrder");
        reasonsSelected.add("noEvidenceOnRespondentSeekToFrustrateTheProcessIfTheyWereGivenNotice");

        caseDataUpdated.put(LIST_ON_NOTICE_REASONS_SELECTED,reasonsSelected);

        AboutToStartOrSubmitCallbackResponse response = listOnNoticeController.listOnNoticeMidEvent(authToken,callbackRequest);
        assertNotNull(response);
    }

}
