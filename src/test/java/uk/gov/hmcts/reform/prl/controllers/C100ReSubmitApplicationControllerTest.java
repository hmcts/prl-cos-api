package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import javax.annotation.processing.Generated;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C1A;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.class)
public class C100ReSubmitApplicationControllerTest {

    @Value("${document.templates.c100.c100_final_template}")
    protected String c100FinalTemplate;

    @Value("${document.templates.c100.c100_c8_template}")
    protected String c100C8Template;

    @Value("${document.templates.c100.c100_c1a_template}")
    protected String c100C1aTemplate;

    @Value("${document.templates.c100.c100_final_welsh_template}")
    protected String c100FinalWelshTemplate;

    @Value("${document.templates.c100.c100_c8_welsh_template}")
    protected String c100C8WelshTemplate;

    @Value("${document.templates.c100.c100_c1a_welsh_template}")
    protected String c100C1aWelshTemplate;

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
    DocumentLanguageService documentLanguageService;

    @Mock
    OrganisationService organisationService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    DgsService dgsService;

    private CallbackRequest callbackRequest;
    private CaseDetails caseDetails;
    private CaseData caseData;
    private static final String auth = "auth";


    @Before
    public void init() throws Exception {
        caseData = CaseData.builder()
            .id(12345L)
            .allegationsOfHarmYesNo(Yes)
            .build();

        caseDetails = CaseDetails.builder()
            .id(12345L)
            .data(Map.of("caseTypeOfApplication", "C100"))
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails prlCaseDetails = uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails.builder()
            .caseData(caseData)
            .build();

        GeneratedDocumentInfo englishFinal = GeneratedDocumentInfo.builder().build();
        GeneratedDocumentInfo welshFinal = GeneratedDocumentInfo.builder().build();
        GeneratedDocumentInfo englishC1a = GeneratedDocumentInfo.builder().build();
        GeneratedDocumentInfo welshC1a = GeneratedDocumentInfo.builder().build();
        GeneratedDocumentInfo englishC8 = GeneratedDocumentInfo.builder().build();
        GeneratedDocumentInfo welshC8 = GeneratedDocumentInfo.builder().build();

        when(dgsService.generateDocument(auth, prlCaseDetails, c100FinalTemplate)).thenReturn(englishFinal);
        when(dgsService.generateDocument(auth, prlCaseDetails, c100FinalWelshTemplate)).thenReturn(welshFinal);
        when(dgsService.generateDocument(auth, prlCaseDetails, c100C1aTemplate)).thenReturn(englishC1a);
        when(dgsService.generateDocument(auth, prlCaseDetails, c100C1aWelshTemplate)).thenReturn(welshC1a);
        when(dgsService.generateDocument(auth, prlCaseDetails, c100C8Template)).thenReturn(englishC8);
        when(dgsService.generateDocument(auth, prlCaseDetails, c100C8WelshTemplate)).thenReturn(welshC8);


    }

    @Test
    public void whenLastEventWasSubmitted_thenSubmittedPathFollowed() throws Exception {
        List<CaseEventDetail> caseEvents = List.of(
            CaseEventDetail.builder().stateId(State.AWAITING_RESUBMISSION_TO_HMCTS.getValue()).build(),
            CaseEventDetail.builder().stateId(State.SUBMITTED_PAID.getValue()).build(),
            CaseEventDetail.builder().stateId(State.AWAITING_SUBMISSION_TO_HMCTS.getValue()).build()
        );

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        AboutToStartOrSubmitCallbackResponse response = c100ReSubmitApplicationController.resubmitApplication(auth, callbackRequest);

        assertEquals(response.getData().get("state"), State.SUBMITTED_PAID.getValue());
        verify(caseWorkerEmailService).sendEmail(caseDetails);
        verify(solicitorEmailService).sendEmail(caseDetails);
        verify(allTabService).updateAllTabs(caseData);

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
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseEventService.findEventsForCase(String.valueOf(caseData.getId()))).thenReturn(caseEvents);
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetails(caseData)).thenReturn(caseData);
        when(organisationService.getRespondentOrganisationDetails(caseData)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = c100ReSubmitApplicationController.resubmitApplication(auth, callbackRequest);

        assertEquals(response.getData().get("state"), State.CASE_ISSUE.getValue());
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C1A));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL));
        verify(caseWorkerEmailService).sendEmail(caseDetails);
        verify(solicitorEmailService).sendEmail(caseDetails);
        verify(allTabService).updateAllTabs(caseData);

    }

    @Test
    public void givenNoAllegationsOfHarmAndWelsh_whenLastEventWasIssued_thenIssuedPathFollowedAndCorrectDocsGenerated() throws Exception {
        CaseData caseDataNoAllegations = CaseData.builder()
            .id(12345L)
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
            .isGenEng(false)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseDataNoAllegations);
        when(caseEventService.findEventsForCase(String.valueOf(caseDataNoAllegations.getId()))).thenReturn(caseEvents);
        when(documentLanguageService.docGenerateLang(caseDataNoAllegations)).thenReturn(documentLanguage);
        when(organisationService.getApplicantOrganisationDetails(caseDataNoAllegations)).thenReturn(caseDataNoAllegations);
        when(organisationService.getRespondentOrganisationDetails(caseDataNoAllegations)).thenReturn(caseDataNoAllegations);

        AboutToStartOrSubmitCallbackResponse response = c100ReSubmitApplicationController.resubmitApplication(auth, callbackRequest);

        assertEquals(response.getData().get("state"), State.CASE_ISSUE.getValue());
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_C8));
        assertTrue(response.getData().containsKey(DOCUMENT_FIELD_FINAL));
        assertFalse(response.getData().containsKey(DOCUMENT_FIELD_C1A));
        verify(caseWorkerEmailService).sendEmail(caseDetails);
        verify(solicitorEmailService).sendEmail(caseDetails);
        verify(allTabService).updateAllTabs(caseData);

    }

}
