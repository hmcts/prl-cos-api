package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.c100respondentsolicitor.RespondentSolicitorData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.RespondentAllegationOfHarmService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class RespondentAllegationOfHarmControllerTest {

    @Mock
    RespondentAllegationOfHarmService allegationOfHarmRevisedService;

    private CaseData caseData;

    @Mock
    C100RespondentSolicitorService respondentSolicitorService;

    @Mock
    ObjectMapper objectMapper;


    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private RespondentAllegationsOfHarmData allegationsOfHarmData;

    @InjectMocks
    private RespondentAllegationOfHarmController allegationOfHarmRevisedController;

    private CaseDetails caseDetails;

    @Test
    public void testPrepopulateChildData() {

        List<DynamicMultiselectListElement> valueElements = new ArrayList<>();
        valueElements.add(DynamicMultiselectListElement.builder().code("test").label("test name").build());

        List<DynamicMultiselectListElement> listItemsElements = new ArrayList<>();
        listItemsElements.add(DynamicMultiselectListElement.builder().code("test1").label("test1 name").build());
        listItemsElements.add(DynamicMultiselectListElement.builder().code("test2").label("test2 name").build());

        DynamicMultiSelectList dynamicMultiSelectList = DynamicMultiSelectList.builder().value(valueElements).listItems(listItemsElements).build();

        allegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respWhichChildrenAreRiskPhysicalAbuse(dynamicMultiSelectList)
                .respWhichChildrenAreRiskPsychologicalAbuse(dynamicMultiSelectList)
                .respWhichChildrenAreRiskSexualAbuse(dynamicMultiSelectList)
                .respWhichChildrenAreRiskEmotionalAbuse(dynamicMultiSelectList)
                .respWhichChildrenAreRiskFinancialAbuse(dynamicMultiSelectList)
                .build();
        CaseData caseData = CaseData.builder()
                .id(123L)
                .respondentSolicitorData(RespondentSolicitorData.builder()
                        .respondentAllegationsOfHarmData(allegationsOfHarmData).build())
                .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(123L)
                .data(stringObjectMap)
                .state("CASE_ISSUED")
                .createdDate(LocalDateTime.now())
                .lastModified(LocalDateTime.now())
                .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        CaseData caseData1 = objectMapper.convertValue(caseDetails.getData(), CaseData.class)
                .toBuilder()
                .id(caseDetails.getId())
                .state(State.valueOf(caseDetails.getState()))
                .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails)
                .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(respondentSolicitorService.populateAboutToStartCaseData(callbackRequest)).thenReturn(stringObjectMap);

        allegationOfHarmRevisedController.handleAboutToStart(authToken, s2sToken, callbackRequest);
        verify(allegationOfHarmRevisedService, times(1))
                .prePopulatedChildData(caseData,stringObjectMap);

    }


    @Test
    public void testExceptionForHandleAboutToStart() throws Exception {

        CaseData caseData = CaseData.builder()
                .id(123L)
                .respondentSolicitorData(RespondentSolicitorData.builder()
                        .respondentAllegationsOfHarmData(allegationsOfHarmData).build())
                .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        caseDetails = CaseDetails.builder()
                .id(123L)
                .data(stringObjectMap)
                .state("CASE_ISSUED")
                .createdDate(LocalDateTime.now())
                .lastModified(LocalDateTime.now())
                .build();
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            allegationOfHarmRevisedController.handleAboutToStart(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

}