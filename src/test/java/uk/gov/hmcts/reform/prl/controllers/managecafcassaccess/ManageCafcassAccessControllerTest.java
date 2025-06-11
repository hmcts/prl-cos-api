package uk.gov.hmcts.reform.prl.controllers.managecafcassaccess;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ManageOrders;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.controllers.managecafcassaccess.ManageCafcassAccessController.CAFCASS_ALLOWED_HEADER;
import static uk.gov.hmcts.reform.prl.controllers.managecafcassaccess.ManageCafcassAccessController.CAFCASS_NOT_ALLOWED_HEADER;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class ManageCafcassAccessControllerTest {

    @InjectMocks
    ManageCafcassAccessController manageCafcassAccessController;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AuthorisationService authorisationService;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String SERVICE_AUTH = "serviceAuth";

    @Test
    public void testManageCafcassAccessAllowed() throws Exception {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes).build())
            .caseTypeOfApplication("C100")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH)).thenReturn(true);
        ResponseEntity<SubmittedCallbackResponse> submittedCallbackResponse =
            manageCafcassAccessController.manageCafcassAccessSubmitted(AUTH_TOKEN, SERVICE_AUTH, callbackRequest);
        assertEquals(CAFCASS_ALLOWED_HEADER, submittedCallbackResponse.getBody().getConfirmationHeader());
    }

    @Test
    public void testManageCafcassAccessNotAllowed() throws Exception {

        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.No).build())
            .caseTypeOfApplication("C100")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH)).thenReturn(true);
        ResponseEntity<SubmittedCallbackResponse> submittedCallbackResponse =
            manageCafcassAccessController.manageCafcassAccessSubmitted(AUTH_TOKEN, SERVICE_AUTH, callbackRequest);
        assertEquals(CAFCASS_NOT_ALLOWED_HEADER, submittedCallbackResponse.getBody().getConfirmationHeader());
    }

    @Test
    public void testExceptionForSubmitted() throws Exception {
        CaseData caseData = CaseData.builder()
            .manageOrders(ManageOrders.builder().cafcassServedOptions(YesOrNo.Yes).build())
            .caseTypeOfApplication("C100")
            .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, SERVICE_AUTH)).thenReturn(false);
        assertExpectedException(() -> {
            manageCafcassAccessController.manageCafcassAccessSubmitted(AUTH_TOKEN, SERVICE_AUTH, callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }


    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }

}

