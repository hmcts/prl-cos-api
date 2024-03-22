package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
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
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
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
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class C100RespondentSolicitorControllerTest {
    @InjectMocks
    private C100RespondentSolicitorController c100RespondentSolicitorController;
    private CaseData caseData;
    private Address address;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    DocumentGenService documentGenService;

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

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    Map<String, Object> c7DraftMap = new HashMap<>();

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
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

        DynamicListElement dynamicListElement = DynamicListElement.builder().code(String.valueOf(0)).build();
        DynamicList chooseRespondent = DynamicList.builder().value(dynamicListElement).build();

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

        List<String> errorList = new ArrayList<>();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(respondentSolicitorService.populateAboutToStartCaseData(callbackRequest)).thenReturn(stringObjectMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.handleAboutToStart(
            authToken,
            s2sToken,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("state"));
    }

    @Test
    public void testHandleAboutToSubmit() throws Exception {

        List<String> errorList = new ArrayList<>();

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
    }

    @Test
    public void testGenerateAndStoreC7DraftDocument() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(documentGenService.generateC7DraftDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
            c7DraftMap);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

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

        List<String> errorList = new ArrayList<>();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(respondentSolicitorService.submitC7ResponseForActiveRespondent(
            authToken, callbackRequest
        )).thenReturn(stringObjectMap);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.updateC7ResponseSubmit(
            authToken,
            s2sToken,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("state"));
    }

    @Test
    public void testC7ResponseSubmitted() throws Exception {
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
    }

    @Test
    public void testExceptionForHandleAboutToStart() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            c100RespondentSolicitorController.handleAboutToStart(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    @Test
    public void testExceptionForHandleAboutToSubmit() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            c100RespondentSolicitorController.handleAboutToSubmit(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    @Test
    public void testExceptionForValidateResponse() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            c100RespondentSolicitorController.validateActiveRespondentResponseBeforeStart(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    @Test
    public void testExceptionForC7DraftDocument() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            c100RespondentSolicitorController.generateC7ResponseDraftDocument(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    @Test
    public void testExceptionForConfidentiality() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            c100RespondentSolicitorController.generateConfidentialityDynamicSelectionDisplay(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    @Test
    public void testExceptionForUpdateC7Response() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(authToken,s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            c100RespondentSolicitorController.updateC7ResponseSubmit(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");

    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}



