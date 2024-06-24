package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.services.HelpWithFeesService.APPLICATION_UPDATED;
import static uk.gov.hmcts.reform.prl.services.HelpWithFeesService.CONFIRMATION_BODY;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class HelpWithFeesServiceTest {

    @InjectMocks
    private HelpWithFeesService helpWithFeesService;

    @Mock
    private ObjectMapper objectMapper;

    @Test
    public void testAboutToStart() {

        CaseData casedata = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseSubmittedTimeStamp("2024-06-24T10:46:55.972994696+01:00")
            .id(123L)
            .applicantCaseName("test")
            .state(State.SUBMITTED_NOT_PAID)
            .helpWithFeesNumber("123")
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder()
                                            .firstName("")
                                            .lastName("")
                                            .build())))
            .build();
        uk.gov.hmcts.reform.ccd.client.model.CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .state(State.SUBMITTED_NOT_PAID.getLabel())
            .build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService.handleAboutToStart("auth", caseDetails);
        assertNotNull(response);
        DynamicList dynamicList = (DynamicList) response.get("hwfAppList");
        assertEquals("Child arrangements application C100 - 24/06/2024 10:46:55", dynamicList.getListItems().get(0).getLabel());
        assertEquals("C100",response.get("caseTypeOfApplication"));
    }

    @Test
    public void testSubmitted() {
        ResponseEntity<SubmittedCallbackResponse> submittedResponse = helpWithFeesService.handleSubmitted();
        assertEquals(APPLICATION_UPDATED, submittedResponse.getBody().getConfirmationHeader());
        assertEquals(CONFIRMATION_BODY, submittedResponse.getBody().getConfirmationBody());
    }
}
