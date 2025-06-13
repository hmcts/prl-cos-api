package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.PropertySource;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@PropertySource(value = "classpath:application.yaml")
@ExtendWith(MockitoExtension.class)
class OtherProceedingsControllerTest {

    @InjectMocks
    private OtherProceedingsController otherProceedingsController;

    @Mock
    private OtherProceedingsService otherProceedingsService;

    @Mock
    private ObjectMapper objectMapper;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";

    @Mock
    private AuthorisationService authorisationService;


    @Test
    void testOtherProceedingsAboutToSubmit() throws Exception {
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
        CaseData caseData = CaseData.builder().existingProceedings(existingProceedings).build();
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
                AUTH_TOKEN,S2S_TOKEN,callbackRequest);
        assertNotNull(aboutToStartOrSubmitCallbackResponse);
    }


    @Test
    void testExceptionForOtherProceedingsAboutToSubmit() {
        CaseData caseData = CaseData.builder()
                .id(123L)
                .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(123L)
                        .data(stringObjectMap).build()).build();
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            otherProceedingsController.otherProceedingsAboutToSubmit(AUTH_TOKEN,S2S_TOKEN,callbackRequest);
        });

        assertEquals("Invalid Client", ex.getMessage());
    }
}
