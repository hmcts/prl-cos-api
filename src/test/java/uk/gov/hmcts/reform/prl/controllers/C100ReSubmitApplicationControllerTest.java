package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class C100ReSubmitApplicationControllerTest {

    @InjectMocks
    C100ReSubmitApplicationController c100ReSubmitApplicationController;

    @Mock
    CaseEventService caseEventService;

    @Mock
    SolicitorEmailService solicitorEmailService;

    @Mock
    CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    AllTabServiceImpl allTabService;

    @Mock
    OrganisationService organisationService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    DocumentGenService documentGenService;

    private CallbackRequest callbackRequest;
    private CaseDetails caseDetails;
    private CaseData caseData;
    private CaseData caseDataSubmitted;
    private CaseData caseDataIssued;
    private static final String auth = "auth";


    @Before
    public void init() throws Exception {
        caseData = CaseData.builder()
            .id(12345L)
            .allegationsOfHarmYesNo(Yes)
            .build();
        caseDataSubmitted = CaseData.builder()
            .id(12345L)
            .state(State.SUBMITTED_PAID)
            .allegationsOfHarmYesNo(Yes)
            .build();

        caseDataIssued = CaseData.builder()
            .id(12345L)
            .state(State.CASE_ISSUE)
            .allegationsOfHarmYesNo(Yes)
            .build();

        caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(Map.of("caseTypeOfApplication", "C100"))
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails prlCaseDetailsSubmitted = uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder()
            .caseData(caseDataSubmitted)
            .build();

        uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails prlCaseDetailsIssued = uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder()
            .caseData(caseDataIssued)
            .build();
    }

    @Test
    public void whenLastEventWasSubmitted_thenSubmittedPathFollowed() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.SUBMITTED_PAID.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataSubmitted);
        when(caseEventService.findEventsForCase(String.valueOf(caseDataSubmitted.getId()))).thenReturn(caseEvents);
        AboutToStartOrSubmitCallbackResponse response = c100ReSubmitApplicationController.resubmitApplication(auth, callbackRequest);

        assertEquals(State.SUBMITTED_PAID, response.getData().get("state"));
        verify(caseWorkerEmailService).sendEmail(caseDetails);
        verify(solicitorEmailService).sendEmail(caseDetails);
        verify(allTabService).getAllTabsFields(caseDataSubmitted);

    }

    @Test
    public void givenAllegationsOfHarmAndEnglish_whenLastEventWasIssued_thenIssuedPathFollowedAndCorrectDocsGenerated() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.CASE_ISSUE.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        DocumentLanguage documentLanguage = DocumentLanguage.builder()
            .isGenEng(true)
            .isGenWelsh(false)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataIssued);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class)))
            .thenReturn(Map.of(DOCUMENT_FIELD_C8, "test",
                               DOCUMENT_FIELD_C1A, "test",
                               DOCUMENT_FIELD_FINAL, "test"
            ));
        AboutToStartOrSubmitCallbackResponse response = c100ReSubmitApplicationController.resubmitApplication(auth, callbackRequest);

        assertEquals(State.CASE_ISSUE, response.getData().get("state"));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C1A));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL));
        verify(caseWorkerEmailService).sendEmailToCourtAdmin(caseDetails);
        verify(allTabService).getAllTabsFields(caseDataIssued);

    }

    @Test
    public void givenNoAllegationsOfHarmAndWelsh_whenLastEventWasIssued_thenIssuedPathFollowedAndCorrectDocsGenerated() throws Exception {
        CaseData caseDataNoAllegations = CaseData.builder()
            .id(12345L)
            .state(State.CASE_ISSUE)
            .allegationsOfHarmYesNo(No)
            .build();

        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.CASE_ISSUE.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        DocumentLanguage documentLanguage = DocumentLanguage.builder()
            .isGenWelsh(true)
            .isGenEng(false)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataNoAllegations);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class)))
            .thenReturn(Map.of(DOCUMENT_FIELD_C8_WELSH, "test", DOCUMENT_FIELD_FINAL_WELSH, "test"
            ));

        AboutToStartOrSubmitCallbackResponse response = c100ReSubmitApplicationController.resubmitApplication(auth, callbackRequest);

        assertEquals(State.CASE_ISSUE, response.getData().get("state"));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertFalse(response.getData().containsKey(DOCUMENT_FIELD_C1A_WELSH));
        verify(caseWorkerEmailService).sendEmailToCourtAdmin(caseDetails);
        verify(allTabService).getAllTabsFields(caseDataNoAllegations);

    }

    @Test
    public void givenAllegationsOfHarmAndWelsh_whenLastEventWasIssued_thenIssuedPathFollowedAndCorrectDocsGenerated() throws Exception {

        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.CASE_ISSUE.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        DocumentLanguage documentLanguage = DocumentLanguage.builder()
            .isGenWelsh(true)
            .isGenEng(false)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseDataIssued);
        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class)))
            .thenReturn(Map.of(DOCUMENT_FIELD_C8_WELSH, "test",
                              DOCUMENT_FIELD_FINAL_WELSH, "test",
                              DOCUMENT_FIELD_C1A_WELSH, "test"
            ));

        AboutToStartOrSubmitCallbackResponse response = c100ReSubmitApplicationController.resubmitApplication(auth, callbackRequest);

        assertEquals(State.CASE_ISSUE, response.getData().get("state"));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8_WELSH));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL_WELSH));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C1A_WELSH));
        verify(caseWorkerEmailService).sendEmailToCourtAdmin(caseDetails);
        verify(allTabService).getAllTabsFields(caseDataIssued);

    }

}
