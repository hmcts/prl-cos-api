package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Assertions;
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
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
public class C100RespondentSolicitorControllerTest {
    @InjectMocks
    private C100RespondentSolicitorController c100RespondentSolicitorController;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    EventService eventService;

    @Mock
    ConfidentialDetailsMapper confidentialDetailsMapper;

    @Mock
    C100RespondentSolicitorService respondentSolicitorService;

    @Mock
    private AuthorisationService authorisationService;

    private CaseData caseData;
    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";
    private final String invalidClient = "Invalid Client";

    @BeforeEach
    public void setUp() {

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
            .courtName("testcourt")
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
    public void testHandleAboutToStart() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(respondentSolicitorService.populateAboutToStartCaseData(any(), any())).thenReturn(stringObjectMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.handleAboutToStart(
            authToken,
            s2sToken,
            PrlAppsConstants.ENGLISH,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("state"));
    }

    @Test
    public void testHandleAboutToSubmit() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(respondentSolicitorService.populateAboutToSubmitCaseData(callbackRequest)).thenReturn(stringObjectMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.handleAboutToSubmit(
            authToken,
            s2sToken,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("state"));
    }

    @Test
    public void testKeepDetailsPrivateAsYes() {

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
        when(confidentialDetailsMapper.mapConfidentialData(caseData, false)).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        CallbackResponse response = c100RespondentSolicitorController
            .generateConfidentialityDynamicSelectionDisplay(authToken,s2sToken,callbackRequest);

        assertEquals(123L, response.getData().getId());
        assertNotNull(respondentSolicitorService.generateConfidentialityDynamicSelectionDisplay(callbackRequest));
        assertTrue(authorisationService.isAuthorized(authToken, s2sToken));
        assertNotNull(confidentialDetailsMapper.mapConfidentialData(caseData, false));
    }

    @Test
    public void testGenerateAndStoreC7DraftDocument() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
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

        when(respondentSolicitorService.generateDraftDocumentsForRespondent(callbackRequest, authToken)).thenReturn(
            caseDataUpdated);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.generateC7ResponseDraftDocument(
            authToken,
            s2sToken,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("draftC7ResponseDoc"));
    }

    @Test
    public void validateActiveRespondentResponseBeforeSubmitTest() throws Exception {

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
            authToken
        )).thenReturn(stringObjectMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.validateActiveRespondentResponseBeforeStart(
            authToken,
            s2sToken,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("state"));
    }

    @Test
    public void updateC7ResponseSubmitTest() throws Exception {

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
            authToken, callbackRequest)
        ).thenReturn(stringObjectMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);

        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.updateC7ResponseSubmit(
            authToken,
            s2sToken,
            callbackRequest
        );

        verify(authorisationService).isAuthorized(authToken, s2sToken);
        assertTrue(authorisationService.isAuthorized(authToken, s2sToken));
        assertTrue(response.getData().containsKey("state"));
        assertTrue(response.getData().containsValue(caseData.getId()));
    }

    @Test
    public void testC7ResponseSubmitted() {

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
            .submittedC7Response(authToken, callbackRequest);

        assertNotNull(response);
        assertEquals(SubmittedCallbackResponse.builder().build(), response.getBody());
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    public void testExceptionForHandleAboutToStart() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
            c100RespondentSolicitorController.handleAboutToStart(authToken, s2sToken,  PrlAppsConstants.ENGLISH, callbackRequest)
        );

        verify(authorisationService).isAuthorized(authToken, s2sToken);

        assertEquals(invalidClient, exception.getMessage());
        assertFalse(authorisationService.isAuthorized(authToken, s2sToken));
    }

    @Test
    public void testExceptionForHandleAboutToSubmit() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
            c100RespondentSolicitorController.handleAboutToSubmit(authToken, s2sToken, callbackRequest)
        );

        verify(authorisationService).isAuthorized(authToken, s2sToken);

        assertEquals(invalidClient, exception.getMessage());
        assertFalse(authorisationService.isAuthorized(authToken, s2sToken));
    }

    @Test
    public void testExceptionForValidateResponse() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
            c100RespondentSolicitorController.validateActiveRespondentResponseBeforeStart(authToken, s2sToken, callbackRequest)
        );

        verify(authorisationService).isAuthorized(authToken, s2sToken);

        assertEquals(invalidClient, exception.getMessage());
        assertFalse(authorisationService.isAuthorized(authToken, s2sToken));
    }

    @Test
    public void testExceptionForC7DraftDocument() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
            c100RespondentSolicitorController.generateC7ResponseDraftDocument(authToken, s2sToken, callbackRequest)
        );

        verify(authorisationService).isAuthorized(authToken, s2sToken);
        assertEquals(invalidClient, exception.getMessage());
        assertFalse(authorisationService.isAuthorized(authToken, s2sToken));
    }

    @Test
    public void testExceptionForConfidentiality() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
            c100RespondentSolicitorController.generateConfidentialityDynamicSelectionDisplay(authToken, s2sToken, callbackRequest)
        );

        verify(authorisationService).isAuthorized(authToken, s2sToken);

        assertEquals(invalidClient, exception.getMessage());
        assertFalse(authorisationService.isAuthorized(authToken, s2sToken));
    }

    @Test
    public void testExceptionForUpdateC7Response() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);

        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () ->
            c100RespondentSolicitorController.updateC7ResponseSubmit(authToken, s2sToken, callbackRequest)
        );

        verify(authorisationService).isAuthorized(authToken, s2sToken);

        assertEquals(invalidClient, exception.getMessage());
        assertFalse(authorisationService.isAuthorized(authToken, s2sToken));
    }

}



