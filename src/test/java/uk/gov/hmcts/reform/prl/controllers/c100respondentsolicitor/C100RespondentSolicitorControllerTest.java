package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

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
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
    ConfidentialDetailsMapper confidentialDetailsMapper;

    @Mock
    C100RespondentSolicitorService respondentSolicitorService;

    public static final String authToken = "Bearer TestAuthToken";

    Map<String, Object> c7DraftMap = new HashMap<>();

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
            .draftC7ResponseDoc(Document.builder()
                                    .documentUrl(generatedDocumentInfo.getUrl())
                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                    .documentHash(generatedDocumentInfo.getHashToken())
                                    .documentFileName("c7DraftFilename.pdf")
                                    .build())
            .keepContactDetailsPrivateOther(KeepDetailsPrivate.builder()
                                                .confidentiality(Yes)
                                                .confidentialityList(confidentialityListEnums)
                                                .build())
            .applicants(applicantList)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .chooseRespondentDynamicList(chooseRespondent)
            .respondents(respondentList)
            .build();
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

        when(respondentSolicitorService.populateAboutToStartCaseData(callbackRequest, authToken, errorList)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.handleAboutToStart(
            authToken,
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

        when(respondentSolicitorService.populateAboutToSubmitCaseData(callbackRequest, authToken, errorList)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.handleAboutToSubmit(
            authToken,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("state"));
    }

    @Test
    public void testPopulateSolicitorRespondentList() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(respondentSolicitorService.populateSolicitorRespondentList(callbackRequest, authToken)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.populateSolicitorRespondentList(
            authToken,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("state"));;
    }

    @Test
    public void testHandleActiveRespondentSelection() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(respondentSolicitorService.updateActiveRespondentSelectionBySolicitor(callbackRequest, authToken)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.handleActiveRespondentSelection(
            authToken,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("state"));
    }

    @Test
    public void testKeepDetailsPrivateAsYes() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CaseDetails caseDetails = CaseDetails.builder()
            .id(123L)
            .data(stringObjectMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        address = Address.builder()
            .addressLine1("AddressLine1")
            .postTown("Xyz town")
            .postCode("AB1 2YZ")
            .build();

        List<Element<ApplicantConfidentialityDetails>> expectedOutput = List
            .of(Element.<ApplicantConfidentialityDetails>builder()
                    .value(ApplicantConfidentialityDetails.builder()
                               .firstName("ABC 1")
                               .lastName("XYZ 2")
                               .email("abc1@xyz.com")
                               .phoneNumber("09876543211")
                               .address(address)
                               .build()).build());

        CaseData updatedCasedata = CaseData.builder()
            .applicantCaseName("test")
            .respondentConfidentialDetails(expectedOutput)
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(respondentSolicitorService.generateConfidentialityDynamicSelectionDisplay(callbackRequest)).thenReturn(stringObjectMap);
        when(confidentialDetailsMapper.mapConfidentialData(caseData)).thenReturn(updatedCasedata);

        CallbackResponse response = c100RespondentSolicitorController
            .generateConfidentialityDynamicSelectionDisplay(callbackRequest);

        assertEquals(response.getData().getId(), 123L);
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

        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.generateC7ResponseDraftDocument(
            authToken,
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

        when(respondentSolicitorService.validateActiveRespondentResponse(callbackRequest, errorList)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.validateActiveRespondentResponseBeforeStart(
            authToken,
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

        when(respondentSolicitorService.submitC7ResponseForActiveRespondent(callbackRequest, authToken, errorList)).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.updateC7ResponseSubmit(
            authToken,
            callbackRequest
        );

        assertTrue(response.getData().containsKey("state"));
    }
}



