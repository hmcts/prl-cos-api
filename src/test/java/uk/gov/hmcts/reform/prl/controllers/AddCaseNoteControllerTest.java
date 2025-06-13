package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@ExtendWith(MockitoExtension.class)
class AddCaseNoteControllerTest {

    @InjectMocks
    private AddCaseNoteController addCaseNoteController;

    @Mock
    private UserService userService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserDetails userDetails;

    @Mock
    private AddCaseNoteService addCaseNoteService;

    @Mock
    private AuthorisationService authorisationService;

    public static final String AUTH_TOKEN = "Bearer TestAuthToken";
    public static final String S2S_TOKEN = "s2s AuthToken";

    @Test
    void testPopulateHeaderCaseNote() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .subject("newsubject")
            .caseNote("newcasenote")
            .build();

        Map<String, Object> stringObjectMap = new HashMap<>();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();
        addCaseNoteController.populateHeader(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        verify(addCaseNoteService, times(1))
            .populateHeader(caseData);
    }

    @Test
    void testSubmitCaseNote() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .subject("newsubject")
            .caseNote("newcasenote")
            .build();

        userDetails = UserDetails.builder()
            .forename("forename1")
            .surname("surname1")
            .id("userid1234")
            .email("test@gmail.com")
            .build();

        Map<String, Object> stringObjectMap = new HashMap<>();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        addCaseNoteController.submitCaseNote(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        verify(addCaseNoteService, times(1))
            .addCaseNoteDetails(caseData,userDetails);

    }

    @Test
    void testExceptionForPopulateHeader() {

        Map<String, Object> stringObjectMap = new HashMap<>();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            addCaseNoteController.populateHeader(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        });
        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForSubmitCaseNote() {

        Map<String, Object> stringObjectMap = new HashMap<>();
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();
        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            addCaseNoteController.submitCaseNote(AUTH_TOKEN, S2S_TOKEN, callbackRequest);
        });
        assertEquals("Invalid Client", ex.getMessage());
    }
}
