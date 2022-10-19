package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.AddCaseNoteService;
import uk.gov.hmcts.reform.prl.services.UserService;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@RunWith(SpringRunner.class)
public class AddCaseNoteControllerTest {

    private MockMvc mockMvc;

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

    public static final String authToken = "Bearer TestAuthToken";

    @Test
    public void testPopulateHeaderCaseNote() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .subject("newsubject")
            .caseNote("newcasenote")
            .build();

        UserDetails userDetails = UserDetails.builder()
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

        addCaseNoteController.populateHeader(callbackRequest);
        verify(addCaseNoteService, times(1))
            .populateHeader(caseData);
    }

    @Test
    public void testSubmitCaseNote() {
        CaseData caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .subject("newsubject")
            .caseNote("newcasenote")
            .build();

        UserDetails userDetails = UserDetails.builder()
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

        addCaseNoteController.submitCaseNote(authToken, callbackRequest);

        verify(addCaseNoteService, times(1))
            .addCaseNoteDetails(caseData,userDetails);

    }
}
