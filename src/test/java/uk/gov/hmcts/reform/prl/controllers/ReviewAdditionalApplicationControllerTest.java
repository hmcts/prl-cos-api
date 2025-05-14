package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ReviewAdditionalApplicationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
@PropertySource(value = "classpath:application.yaml")
public class ReviewAdditionalApplicationControllerTest {

    @Mock
    private  ObjectMapper objectMapper;
    @Mock
    private ReviewAdditionalApplicationService reviewAdditionalApplicationService;

    @InjectMocks
    private ReviewAdditionalApplicationController reviewAdditionalApplicationController;

    @Mock
    private AuthorisationService authorisationService;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";
    public static Map<String, Object> clientContext = new HashMap<>();

    @Before
    public void setUp() {
        clientContext.put("test", "test");
    }

    @Test
    public void shouldPopulateReviewAdditionalApplication() {
        Element<AdditionalApplicationsBundle> reviewAdditionalApplicationElement = Element.<AdditionalApplicationsBundle>builder().build();
        List<Element<AdditionalApplicationsBundle>> reviewAdditionalApplicationCollection = new ArrayList<>();
        reviewAdditionalApplicationCollection.add(reviewAdditionalApplicationElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .additionalApplicationsBundle(reviewAdditionalApplicationCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, caseData.getCaseTypeOfApplication());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(reviewAdditionalApplicationService.populateReviewAdditionalApplication(caseData,
                                                                                    AUTH_TOKEN,
                                                                                    "clcx",
                                                                                    Event.REVIEW_ADDITIONAL_APPLICATION.getId()))
            .thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = reviewAdditionalApplicationController
            .populateReviewAdditionalApplication(AUTH_TOKEN, S2S_TOKEN, "clcx", callbackRequest);
        Assert.assertNotNull(response);
    }

    @Test
    public void shouldAboutToStartReviewAdditionalApplication() {
        Element<AdditionalApplicationsBundle> reviewAdditionalApplicationElement = Element.<AdditionalApplicationsBundle>builder().build();
        List<Element<AdditionalApplicationsBundle>> reviewAdditionalApplicationCollection = new ArrayList<>();
        reviewAdditionalApplicationCollection.add(reviewAdditionalApplicationElement);
        CaseData caseData = CaseData.builder()
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .id(123L)
            .additionalApplicationsBundle(reviewAdditionalApplicationCollection)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, caseData.getCaseTypeOfApplication());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(reviewAdditionalApplicationService.populateReviewAdditionalApplication(caseData,
                                                                                    AUTH_TOKEN,
                                                                                    "clcx",
                                                                                    Event.REVIEW_ADDITIONAL_APPLICATION.getId()))
            .thenReturn(caseDataMap);
        AboutToStartOrSubmitCallbackResponse response = reviewAdditionalApplicationController
            .aboutToStartReviewAdditionalApplication(AUTH_TOKEN, S2S_TOKEN, "clcx", callbackRequest);
        Assert.assertNotNull(response);
    }
}
