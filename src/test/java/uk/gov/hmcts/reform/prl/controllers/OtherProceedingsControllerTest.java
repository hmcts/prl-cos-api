package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.OtherProceedingsService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class OtherProceedingsControllerTest {

    private MockMvc mockMvc;

    private CaseData caseData;

    @InjectMocks
    private OtherProceedingsController otherProceedingsController;

    public static final String authToken = "Bearer TestAuthToken";
    @Mock
    private OtherProceedingsService otherProceedingsService;
    @Mock
    private ObjectMapper objectMapper;

    public static final String s2sToken = "s2s AuthToken";

    @Mock
    private AuthorisationService authorisationService;


    @Test
    public void testOtherProceedingsAboutToSubmit() throws Exception {
        ArrayList<TypeOfOrderEnum> typeOfOrder = new ArrayList<>();
        typeOfOrder.add(TypeOfOrderEnum.emergencyProtectionOrder);
        typeOfOrder.add(TypeOfOrderEnum.childArrangementsOrder);

        ProceedingDetails proceedingDetails = ProceedingDetails.builder().dateEnded(LocalDate.of(1990, 8, 1))
                .caseNumber("2344").dateStarted(LocalDate.of(2020, 8, 1))
                .nameOfCourt("Court Name").nameOfJudge("Judge Name").typeOfOrder(typeOfOrder)
                .nameAndOffice("Office Name").nameOfGuardian("Guardian Name").otherTypeOfOrder("Other Type of Order")
                .nameOfChildrenInvolved("Name Of Children").previousOrOngoingProceedings(ProceedingsEnum.ongoing)
                .uploadRelevantOrder(Document.builder().build())
                .build();
        Element<ProceedingDetails> proceedingDetailsElement = Element.<ProceedingDetails>builder()
                .value(proceedingDetails).build();
        List<Element<ProceedingDetails>> existingProceedings = Collections.singletonList(proceedingDetailsElement);
        caseData = CaseData.builder().existingProceedings(existingProceedings).build();
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("existingProceedings",existingProceedings);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        doNothing().when(otherProceedingsService).populateCaseDocumentsData(
                any(CaseData.class),any(Map.class));
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                        .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = otherProceedingsController.otherProceedingsAboutToSubmit(
                authToken,s2sToken,callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse);
    }


    @Test
    public void testExceptionForOtherProceedingsAboutToSubmit() throws Exception {
        CaseData caseData = CaseData.builder()
                .id(123L)
                .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                        .data(stringObjectMap).build()).build();
        doNothing().when(otherProceedingsService).populateCaseDocumentsData(
                any(CaseData.class),any(Map.class));
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            otherProceedingsController
                    .otherProceedingsAboutToSubmit(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }


}
