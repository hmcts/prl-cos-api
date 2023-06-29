package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.caseaccess.AssignCaseAccessService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Ignore
@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseInitiationControllerTest {

    @InjectMocks
    private CaseInitiationController caseInitiationController;

    @Mock
    ApplicationsTabService applicationsTabService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EventService eventService;

    @Mock
    AssignCaseAccessService assignCaseAccessService;

    @Mock
    EventService eventPublisher;

    @Mock
    CoreCaseDataApi coreCaseDataApi;

    @Mock
    AuthTokenGenerator authTokenGenerator;
    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHandleSubmitted() {

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "testCaseName");
        String userID = "12345";

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseDataMap)
            .build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("testCaseName")
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        doNothing().when(assignCaseAccessService).assignCaseAccess(String.valueOf(caseData.getId()),authToken);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        caseInitiationController.handleSubmitted(authToken,s2sToken,callbackRequest);
        CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);
        eventService.publishEvent(caseDataChanged);

        applicationsTabService.updateTab(caseData);
        verify(applicationsTabService).updateTab(caseData);
        verify(eventService).publishEvent(caseDataChanged);

    }

    @Test
    public void testExceptionForHandleSubmitted() throws Exception {

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put("applicantCaseName", "testCaseName");
        String userID = "12345";

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(caseDataMap)
            .build();

        CaseData caseData = CaseData.builder()
            .id(123L)
            .applicantCaseName("testCaseName")
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        doNothing().when(assignCaseAccessService).assignCaseAccess(String.valueOf(caseData.getId()),authToken);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            caseInitiationController.handleSubmitted(authToken,s2sToken,callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}

