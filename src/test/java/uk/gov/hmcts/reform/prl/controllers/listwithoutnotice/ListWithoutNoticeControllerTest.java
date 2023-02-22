package uk.gov.hmcts.reform.prl.controllers.listwithoutnotice;

/*
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
import uk.gov.hmcts.reform.prl.services.RefDataUserService;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HEARINGTYPE;


@Slf4j
@RunWith(MockitoJUnitRunner.Silent.class)
public class ListWithoutNoticeControllerTest {

    @InjectMocks
    ListWithoutNoticeController listWithoutNoticeController;

    @Mock
    RefDataUserService refDataUserService;

    @Mock
    private ObjectMapper objectMapper;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String serviceAuth = "serviceAuth";

  /*  @Test
    public void shouldSeeHearingDetails() throws Exception {

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

        when(refDataUserService.retrieveCategoryValues(authToken,HEARINGTYPE)).thenReturn(List.of(DynamicListElement.builder()
                                                                                                      .build()));

        AboutToStartOrSubmitCallbackResponse response = listWithoutNoticeController.prePopulateHearingPageData(authToken,callbackRequest);
        assertNotNull(response.getData().containsKey("listWithoutNoticeHearingDetails"));
    }

}*/
