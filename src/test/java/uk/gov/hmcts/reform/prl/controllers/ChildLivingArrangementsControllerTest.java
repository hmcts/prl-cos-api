package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityC8RefugeService;
import uk.gov.hmcts.reform.prl.services.UpdatePartyDetailsService;
import uk.gov.hmcts.reform.prl.services.cafcass.CafcassDateTimeService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.payment.PaymentRequestService;
import uk.gov.hmcts.reform.prl.services.validators.ChildLivingArrangementsValidator;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.services.validators.ChildLivingArrangementsValidator.REMOVAL_ERROR;
import static uk.gov.hmcts.reform.prl.services.validators.ChildLivingArrangementsValidator.SUBMISSION_ERROR;

@ExtendWith(MockitoExtension.class)
class ChildLivingArrangementsControllerTest {

    @InjectMocks
    private CallbackController controller;
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private ChildLivingArrangementsValidator validator;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private UpdatePartyDetailsService partyDetailsService;
    @Mock
    private DocumentGenService documentGenService;
    @Mock
    private PaymentRequestService paymentRequestService;
    @Mock
    private CafcassDateTimeService cafcassDateTimeService;
    @Mock
    private ConfidentialityC8RefugeService confidentialityC8RefugeService;

    @ParameterizedTest
    @ValueSource(strings = {"applicantsDetails", "amendApplicantsDetails", "respondentsDetails", "amendRespondentsDetails",
        "otherPeopleInTheCaseRevised", "amendOtherPeopleInTheCaseRevised"})
    void shouldRejectReferencedPartyRemovalOnFirstContinue(String eventId) {
        CallbackRequest request = request(eventId);
        when(authorisationService.isAuthorized("user", "service")).thenReturn(true);
        when(validator.validateRemoval(request)).thenReturn(List.of(REMOVAL_ERROR));

        AboutToStartOrSubmitCallbackResponse response = controller.handleUpdatePartyDetailsMidEvent("user", "service", request);

        assertThat(response.getErrors()).containsExactly(REMOVAL_ERROR);
        assertThat(response.getData()).isEqualTo(request.getCaseDetails().getData());
        verifyNoInteractions(partyDetailsService, documentGenService, paymentRequestService, cafcassDateTimeService,
                             confidentialityC8RefugeService);
    }

    @ParameterizedTest
    @ValueSource(strings = {"applicantsDetails", "amendApplicantsDetails", "respondentsDetails", "amendRespondentsDetails",
        "otherPeopleInTheCaseRevised", "amendOtherPeopleInTheCaseRevised", "legacyOtherPeople"})
    void shouldRejectRemovalBeforeRunningUpdatesOrSideEffects(String eventId) {
        CallbackRequest request = request(eventId);
        when(authorisationService.isAuthorized("user", "service")).thenReturn(true);
        when(validator.validateRemoval(request)).thenReturn(List.of(REMOVAL_ERROR));

        AboutToStartOrSubmitCallbackResponse response = switch (eventId) {
            case "otherPeopleInTheCaseRevised", "amendOtherPeopleInTheCaseRevised" ->
                controller.updateOtherPeoplePartyDetails("user", "service", request);
            case "legacyOtherPeople" -> controller.amendOtherPeopleInTheCase("user", "service", request);
            default -> controller.updatePartyDetails("user", "service", request);
        };

        assertThat(response.getErrors()).containsExactly(REMOVAL_ERROR);
        verifyNoInteractions(partyDetailsService, documentGenService, paymentRequestService, cafcassDateTimeService,
                             confidentialityC8RefugeService);
    }

    @Test
    void shouldRejectSubmissionBeforeGeneratingDocumentsOrCreatingPaymentRequest() throws Exception {
        CallbackRequest request = request("submitAndPay");
        CaseData caseData = CaseData.builder().caseTypeOfApplication("C100").build();
        when(authorisationService.isAuthorized("user", "service")).thenReturn(true);
        when(objectMapper.convertValue(request.getCaseDetails().getData(), CaseData.class)).thenReturn(caseData);
        when(validator.validateSubmission(caseData)).thenReturn(List.of(SUBMISSION_ERROR));

        AboutToStartOrSubmitCallbackResponse response = controller.generateDocumentSubmitApplication("user", "service", request);

        assertThat(response.getErrors()).containsExactly(SUBMISSION_ERROR);
        assertThat(request.getCaseDetails().getData()).isEmpty();
        verifyNoInteractions(partyDetailsService, documentGenService, paymentRequestService, cafcassDateTimeService,
                             confidentialityC8RefugeService);
    }

    private CallbackRequest request(String eventId) {
        return CallbackRequest.builder().eventId(eventId)
            .caseDetails(CaseDetails.builder().id(12345L).state("AWAITING_SUBMISSION_TO_HMCTS").data(new HashMap<>()).build())
            .build();
    }
}
