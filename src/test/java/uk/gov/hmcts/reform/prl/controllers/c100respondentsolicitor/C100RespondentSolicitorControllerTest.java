package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesNoIDontKnow;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.events.CaseDataChanged;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.c100respondentsolicitor.RespondentSolicitorData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class C100RespondentSolicitorControllerTest {

    @InjectMocks
    private C100RespondentSolicitorController c100RespondentSolicitorController;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private EventService eventService;

    @Mock
    C100RespondentSolicitorService respondentSolicitorService;

    @Mock
    private AuthorisationService authorisationService;

    private static final String AUTH_TOKEN = "Bearer TestAuthToken";
    private static final String S2S_TOKEN = "s2s AuthToken";

    private CaseData caseData;

    @BeforeEach
    void setUp() {

        List<ConfidentialityListEnum> confidentialityListEnums = new ArrayList<>();

        confidentialityListEnums.add(ConfidentialityListEnum.email);
        confidentialityListEnums.add(ConfidentialityListEnum.phoneNumber);

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        PartyDetails respondent = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder()
            .courtName("test court")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .respondentSolicitorData((RespondentSolicitorData.builder()
                .draftC7ResponseDoc(Document.builder()
                                        .documentUrl(generatedDocumentInfo.getUrl())
                                        .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                        .documentHash(generatedDocumentInfo.getHashToken())
                                        .documentFileName("c7DraftFilename.pdf")
                                        .build())
                .keepContactDetailsPrivate(KeepDetailsPrivate.builder()
                                               .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                               .confidentiality(Yes)
                                               .confidentialityList(confidentialityListEnums)
                                               .build())
                .build()))
            .applicants(applicantList)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .respondents(respondentList)
            .build();

        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testHandleAboutToStart() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(respondentSolicitorService.populateAboutToStartCaseData(any(), any())).thenReturn(stringObjectMap);
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.handleAboutToStart(
            AUTH_TOKEN,
            S2S_TOKEN,
            PrlAppsConstants.ENGLISH,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("state"));
    }

    @Test
    void testHandleAboutToSubmit() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(respondentSolicitorService.populateAboutToSubmitCaseData(callbackRequest)).thenReturn(stringObjectMap);
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.handleAboutToSubmit(
            AUTH_TOKEN,
            S2S_TOKEN,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("state"));
    }

    @Test
    void testKeepDetailsPrivateAsYes() {

        Map<String, Object> stringObjectMap = new HashMap<>();

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(respondentSolicitorService.generateConfidentialityDynamicSelectionDisplay(callbackRequest)).thenReturn(
            stringObjectMap);
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        CallbackResponse response = c100RespondentSolicitorController
            .generateConfidentialityDynamicSelectionDisplay(AUTH_TOKEN, S2S_TOKEN, callbackRequest);

        assertEquals(123L, response.getData().getId());
    }

    @Test
    void testGenerateAndStoreC7DraftDocument() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Document document = Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName("Draft_C1A_allegation_of_harm.pdf")
            .build();
        caseDataUpdated.put("draftC7ResponseDoc", document);
        when(respondentSolicitorService.generateDraftDocumentsForRespondent(callbackRequest, AUTH_TOKEN)).thenReturn(
            caseDataUpdated);
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.generateC7ResponseDraftDocument(
            AUTH_TOKEN,
            S2S_TOKEN,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("draftC7ResponseDoc"));
    }

    @Test
    void validateActiveRespondentResponseBeforeSubmitTest() throws Exception {

        List<String> errorList = new ArrayList<>();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(respondentSolicitorService.validateActiveRespondentResponse(
            callbackRequest,
            errorList,
            AUTH_TOKEN
        )).thenReturn(stringObjectMap);
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.validateActiveRespondentResponseBeforeStart(
            AUTH_TOKEN,
            S2S_TOKEN,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("state"));
    }

    @Test
    void updateC7ResponseSubmitTest() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .caseDetailsBefore(CaseDetails.builder()
                                   .id(124L)
                                   .data(stringObjectMap).build())
            .build();

        when(respondentSolicitorService.submitC7ResponseForActiveRespondent(
            AUTH_TOKEN, callbackRequest
        )).thenReturn(stringObjectMap);
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.updateC7ResponseSubmit(
            AUTH_TOKEN,
            S2S_TOKEN,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("state"));
    }

    @Test
    void testC7ResponseSubmitted() {
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        CaseDataChanged caseDataChanged = new CaseDataChanged(caseData);
        eventService.publishEvent(caseDataChanged);
        when(respondentSolicitorService.submittedC7Response(
            caseData)).thenReturn(SubmittedCallbackResponse.builder().build());

        ResponseEntity<SubmittedCallbackResponse> response = c100RespondentSolicitorController
            .submittedC7Response(AUTH_TOKEN, callbackRequest);

        assertNotNull(response);
    }

    @Test
    void testExceptionForHandleAboutToStart() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> c100RespondentSolicitorController.handleAboutToStart(
                AUTH_TOKEN,
                S2S_TOKEN,
                PrlAppsConstants.ENGLISH,
                callbackRequest
            )
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForHandleAboutToSubmit() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> c100RespondentSolicitorController.handleAboutToSubmit(AUTH_TOKEN, S2S_TOKEN, callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForValidateResponse() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> c100RespondentSolicitorController.validateActiveRespondentResponseBeforeStart(
                AUTH_TOKEN,
                S2S_TOKEN,
                callbackRequest
            )
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForC7DraftDocument() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> c100RespondentSolicitorController.generateC7ResponseDraftDocument(AUTH_TOKEN, S2S_TOKEN, callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());


    }

    @Test
    void testExceptionForConfidentiality() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> c100RespondentSolicitorController.generateConfidentialityDynamicSelectionDisplay(
                AUTH_TOKEN,
                S2S_TOKEN,
                callbackRequest
            )
        );

        assertEquals("Invalid Client", ex.getMessage());
    }

    @Test
    void testExceptionForUpdateC7Response() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () -> c100RespondentSolicitorController.updateC7ResponseSubmit(AUTH_TOKEN, S2S_TOKEN, callbackRequest)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }
}



