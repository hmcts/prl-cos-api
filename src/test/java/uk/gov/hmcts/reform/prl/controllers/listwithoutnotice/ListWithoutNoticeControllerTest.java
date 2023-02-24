package uk.gov.hmcts.reform.prl.controllers.listwithoutnotice;


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
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.HearingPrePopulateService;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;



@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class ListWithoutNoticeControllerTest {

    @InjectMocks
    ListWithoutNoticeController listWithoutNoticeController;

    @Mock
    HearingPrePopulateService hearingPrePopulateService;

    @Mock
    private ObjectMapper objectMapper;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String serviceAuth = "serviceAuth";

    @Test
    public void shouldSeeHearingDetailswithoutListWithoutNotice() throws Exception {

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

        when(hearingPrePopulateService.prePopulateHearingType(authToken)).thenReturn(List.of(DynamicListElement.builder()
                                                                                                      .build()));

        AboutToStartOrSubmitCallbackResponse response = listWithoutNoticeController.prePopulateHearingPageData(authToken,callbackRequest);
        assertNotNull(response.getData().containsKey("listWithoutNoticeHearingDetails"));
    }

    @Test
    public void shouldSeeHearingDetailswithoutListWithNotice() throws Exception {

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

        when(hearingPrePopulateService.prePopulateHearingType(authToken)).thenReturn(List.of(DynamicListElement.builder()
                                                                                                 .build()));
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        caseDataUpdated.put("listWithoutNoticeHearingDetails",List.of(DynamicListElement.builder()
                                                                          .build()));

        AboutToStartOrSubmitCallbackResponse response = listWithoutNoticeController.prePopulateHearingPageData(authToken,callbackRequest);
        assertNotNull(response.getData().containsKey("listWithoutNoticeHearingDetails"));
    }

}
