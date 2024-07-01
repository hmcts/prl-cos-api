package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
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
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
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

    CaseData casedata;

    CaseDetails caseDetails;

    @Before
    public void init() {

        casedata = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .caseSubmittedTimeStamp("2024-06-24T10:46:55.972994696+01:00")
            .id(123L)
            .applicantCaseName("test")
            .state(State.SUBMITTED_NOT_PAID)
            .helpWithFeesNumber("123")
            .caseTypeOfApplication("C100")
            .applicants(List.of(element(PartyDetails.builder()
                                            .firstName("firstName")
                                            .lastName("LastName")
                                            .build())))
            .build();
        caseDetails = CaseDetails.builder()
            .id(123L)
            .state(State.SUBMITTED_NOT_PAID.getLabel())
            .build();
    }

    @Test
    public void testAboutToStart() {

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService.handleAboutToStart(caseDetails);
        assertNotNull(response);
        DynamicList dynamicList = (DynamicList) response.get("hwfAppList");
        assertEquals("Child arrangements application C100 - 24/06/2024 10:46:55", dynamicList.getListItems().get(0).getLabel());
        assertEquals("C100",response.get("caseTypeOfApplication"));
    }

    @Test
    public void testAboutToSubmit() {
        casedata = casedata.toBuilder()
                .state(State.SUBMITTED_PAID)
                    .build();

        caseDetails = caseDetails.toBuilder()
            .state(State.SUBMITTED_PAID.getLabel())
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(casedata);
        Map<String, Object> response = helpWithFeesService.setCaseStatus();
        assertNotNull(response);
        CaseStatus caseStatus = (CaseStatus) response.get("caseStatus");
        assertEquals("Submitted", caseStatus.getState());
    }

    @Test
    public void testSubmitted() {
        ResponseEntity<SubmittedCallbackResponse> submittedResponse = helpWithFeesService.handleSubmitted();
        assertEquals(APPLICATION_UPDATED, submittedResponse.getBody().getConfirmationHeader());
        assertEquals(CONFIRMATION_BODY, submittedResponse.getBody().getConfirmationBody());
    }
}
