package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.RespondentWelshNeedsListEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.AddressHistory;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.Contact;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResSolInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentChildAbduction;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentInterpreterNeeds;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentOtherConcerns;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorAbilityToParticipateInProceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorInternationalElement;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorKeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.SolicitorMiam;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators.ResponseSubmitChecker;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_FINAL_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@RunWith(MockitoJUnitRunner.Silent.class)
public class C100RespondentSolicitorServiceTest {

    @InjectMocks
    C100RespondentSolicitorService respondentSolicitorService;

    CaseData caseData;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    CcdDataStoreService ccdDataStoreService;

    @Mock
    ResponseSubmitChecker responseSubmitChecker;

    @Mock
    RespondentSolicitorMiamService miamService;

    @Mock
    DocumentGenService documentGenService;

    FindUserCaseRolesResponse findUserCaseRolesResponse;

    boolean mandatoryFinished = false;

    public static final String authToken = "Bearer TestAuthToken";

    @Before
    public void setUp() {

        List<ConfidentialityListEnum> confidentialityListEnums = new ArrayList<>();

        confidentialityListEnums.add(ConfidentialityListEnum.email);
        confidentialityListEnums.add(ConfidentialityListEnum.phoneNumber);

        PartyDetails respondent = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .response(Response.builder()
                          .consent(Consent.builder().build())
                          .citizenDetails(CitizenDetails.builder().build())
                          .c7ResponseSubmitted(No)
                          .activeRespondent(Yes)
                          .solicitorKeepDetailsPriate(SolicitorKeepDetailsPrivate
                                                          .builder()
                                                          .respKeepDetailsPrivateConfidentiality(KeepDetailsPrivate
                                                                                                     .builder()
                                                                                                     .confidentiality(Yes)
                                                                                                     .confidentialityList(confidentialityListEnums)
                                                                                                     .build())
                                                          .respKeepDetailsPrivate(KeepDetailsPrivate
                                                                                      .builder()
                                                                                      .otherPeopleKnowYourContactDetails(YesNoDontKnow.yes)
                                                                                      .confidentiality(Yes)
                                                                                      .confidentialityList(confidentialityListEnums)
                                                                                      .build())
                                                          .build())
                          .solicitorMiam(SolicitorMiam.builder()
                                             .respSolHaveYouAttendedMiam(Miam.builder()
                                                                             .attendedMiam(No)
                                                                             .build())
                                             .respSolWillingnessToAttendMiam(Miam.builder()
                                                                                 .willingToAttendMiam(No)
                                                                                 .reasonNotAttendingMiam("test")
                                                                                 .build())
                                             .build())
                          .resSolInternationalElements(ResSolInternationalElements
                                                           .builder()
                                                           .internationalElementParentInfo(SolicitorInternationalElement
                                                                                               .builder()
                                                                                               .reasonForParentDetails("Test")
                                                                                               .reasonForJurisdictionDetails("Test")
                                                                                               .requestToAuthorityDetails("Test")
                                                                                               .build())
                                                           .internationalElementChildInfo(SolicitorInternationalElement
                                                                                              .builder()
                                                                                              .reasonForChild(Yes)
                                                                                              .reasonForChildDetails("Test")
                                                                                              .reasonForParent(Yes)
                                                                                              .reasonForParentDetails("Test")
                                                                                              .reasonForJurisdiction(Yes)
                                                                                              .reasonForJurisdictionDetails("Test")
                                                                                              .requestToAuthority(Yes)
                                                                                              .requestToAuthorityDetails("Test")
                                                                                              .build())
                                                           .build())
                          .respondentAllegationsOfHarmData(RespondentAllegationsOfHarmData
                                                               .builder()
                                                               .respChildAbductionInfo(RespondentChildAbduction
                                                                                           .builder()
                                                                                           .previousThreatsForChildAbduction(Yes)
                                                                                           .previousThreatsForChildAbductionDetails("Test")
                                                                                           .reasonForChildAbductionBelief("Test")
                                                                                           .whereIsChild("Test")
                                                                                           .hasPassportOfficeNotified(Yes)
                                                                                           .childrenHavePassport(Yes)
                                                                                           .childrenHaveMoreThanOnePassport(Yes)
                                                                                           .whoHasChildPassportOther("father")
                                                                                           .anyOrgInvolvedInPreviousAbduction(Yes)
                                                                                           .anyOrgInvolvedInPreviousAbductionDetails("Test")
                                                                                           .build())
                                                               .respOtherConcernsInfo(RespondentOtherConcerns
                                                                                          .builder()
                                                                                          .childHavingOtherFormOfContact(Yes)
                                                                                          .childSpendingSupervisedTime(Yes)
                                                                                          .ordersRespondentWantFromCourt("Test")
                                                                                          .childSpendingUnsupervisedTime(Yes)
                                                                                          .build())
                                                               .respAllegationsOfHarmInfo(RespondentAllegationsOfHarm
                                                                                              .builder()
                                                                                              .respondentChildAbuse(Yes)
                                                                                              .isRespondentChildAbduction(Yes)
                                                                                              .respondentNonMolestationOrder(Yes)
                                                                                              .respondentOccupationOrder(Yes)
                                                                                              .respondentForcedMarriageOrder(Yes)
                                                                                              .respondentDrugOrAlcoholAbuse(Yes)
                                                                                              .respondentOtherInjunctiveOrder(Yes)
                                                                                              .respondentRestrainingOrder(Yes)
                                                                                              .respondentDomesticAbuse(Yes)
                                                                                              .respondentDrugOrAlcoholAbuseDetails("Test")
                                                                                              .respondentOtherSafetyConcerns(Yes)
                                                                                              .respondentOtherSafetyConcernsDetails("Test")
                                                                                              .build())
                                                               .respAohYesOrNo(Yes)
                                                               .build())
                          .build())
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        List<RespondentWelshNeedsListEnum> welshNeedsListEnum = new ArrayList<>();
        welshNeedsListEnum.add(RespondentWelshNeedsListEnum.speakWelsh);
        List<PartyEnum> party = new ArrayList<>();
        party.add(PartyEnum.respondent);

        List<CaseUser> caseUserList  = new ArrayList<>();
        caseUserList.add(CaseUser.builder()
                             .caseId("12345")
                             .caseRole("[SOLICITORA]")
                             .userId("1afdfa01-8280-4e2c-b810-ab7cf741988a").build());
        findUserCaseRolesResponse = new FindUserCaseRolesResponse();
        findUserCaseRolesResponse.setCaseUsers(caseUserList);

        Address address = Address.builder()
            .addressLine1("test")
            .postCode("test")
            .build();
        RespondentInterpreterNeeds interpreterNeeds = RespondentInterpreterNeeds.builder()
            .party(party)
            .relationName("Test")
            .requiredLanguage("Cornish")
            .build();
        Element<RespondentInterpreterNeeds> wrappedInterpreter = Element.<RespondentInterpreterNeeds>builder()
            .value(interpreterNeeds).build();
        DynamicListElement dynamicListElement = DynamicListElement
            .builder().code("1afdfa01-8280-4e2c-b810-ab7cf741988a").build();
        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder()
            .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
            .value(respondent).build();
        List<Element<RespondentInterpreterNeeds>> interpreterList = Collections.singletonList(wrappedInterpreter);
        DynamicList chooseRespondent = DynamicList.builder().value(dynamicListElement).build();
        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondents);
        Element<Address> wrappedAddress = Element.<Address>builder().value(address).build();
        List<Element<Address>> addressList = Collections.singletonList(wrappedAddress);
        caseData = CaseData.builder().respondents(respondentList).id(1)
            .chooseRespondentDynamicList(chooseRespondent)
            .keepContactDetailsPrivateOther(KeepDetailsPrivate.builder()
                                           .confidentiality(Yes)
                                           .confidentialityList(confidentialityListEnums)
                                           .build())
            .respondentConsentToApplication(Consent
                                                .builder()
                                                .noConsentReason("test")
                                                .courtOrderDetails("test")
                                                .consentToTheApplication(No)
                                                .permissionFromCourt(Yes)
                                                .build())
            .respondentAttendingTheCourt(AttendToCourt.builder()
                                             .respondentWelshNeeds(Yes)
                                             .respondentWelshNeedsList(welshNeedsListEnum)
                                             .isRespondentNeededInterpreter(Yes)
                                             .respondentInterpreterNeeds(interpreterList)
                                             .haveAnyDisability(Yes)
                                             .disabilityNeeds("Test")
                                             .respondentSpecialArrangements(Yes)
                                             .respondentSpecialArrangementDetails("Test")
                                             .respondentIntermediaryNeeds(Yes)
                                             .respondentIntermediaryNeedDetails("Test")
                                             .build())
            .currentOrPastProceedingsForChildren(YesNoDontKnow.yes)
            .abilityToParticipateInProceedings(SolicitorAbilityToParticipateInProceedings.builder()
                                                   .factorsAffectingAbilityToParticipate(YesNoDontKnow.yes)
                                                   .build())
            .internationalElementParent(SolicitorInternationalElement
                                            .builder()
                                            .reasonForParentDetails("Test")
                                            .reasonForJurisdictionDetails("Test")
                                            .requestToAuthorityDetails("Test")
                                            .build())
            .internationalElementChild(SolicitorInternationalElement
                                           .builder()
                                           .reasonForChild(Yes)
                                           .reasonForChildDetails("Test")
                                           .reasonForParent(Yes)
                                           .reasonForParentDetails("Test")
                                           .reasonForJurisdiction(Yes)
                                           .reasonForJurisdictionDetails("Test")
                                           .requestToAuthority(Yes)
                                           .requestToAuthorityDetails("Test")
                                           .build())
            .keepContactDetailsPrivate(KeepDetailsPrivate
                .builder()
                .build())
            .respondentAllegationsOfHarm(RespondentAllegationsOfHarm
                                             .builder()
                                             .respondentChildAbuse(Yes)
                                             .isRespondentChildAbduction(Yes)
                                             .respondentNonMolestationOrder(Yes)
                                             .respondentOccupationOrder(Yes)
                                             .respondentForcedMarriageOrder(Yes)
                                             .respondentDrugOrAlcoholAbuse(Yes)
                                             .respondentOtherInjunctiveOrder(Yes)
                                             .respondentRestrainingOrder(Yes)
                                             .respondentDomesticAbuse(Yes)
                                             .respondentDrugOrAlcoholAbuseDetails("Test")
                                             .respondentOtherSafetyConcerns(Yes)
                                             .respondentOtherSafetyConcernsDetails("Test")
                                             .build())
            .resSolConfirmEditContactDetails(CitizenDetails
                                                 .builder()
                                                 .firstName("Test")
                                                 .lastName("Test")
                                                 .address(address)
                                                 .contact(Contact.builder()
                                                              .email("Test")
                                                              .phoneNumber("0785544").build())
                                                 .addressHistory(AddressHistory.builder()
                                                                     .isAtAddressLessThan5Years(No)
                                                                     .previousAddressHistory(addressList)
                                                                     .build())
                                                 .build())
            .respondentSolicitorHaveYouAttendedMiam(Miam.builder()
                                                        .attendedMiam(No)
                                                        .build())
            .respondentSolicitorWillingnessToAttendMiam(Miam.builder()
                                                            .willingToAttendMiam(No)
                                                            .reasonNotAttendingMiam("test")
                                                            .build())
            .build();
    }

    @Test
    public void populateAboutToStartCaseDataTest() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        when(ccdDataStoreService.findUserCaseRoles(String.valueOf(caseData.getId()),
                                                   authToken)).thenReturn(findUserCaseRolesResponse);

        List<String> errorList = new ArrayList<>();

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.populateAboutToStartCaseData(
            callbackRequest, authToken, errorList
        );

        assertTrue(response.containsKey("respondents"));
    }

    @Test
    public void populateSolicitorRespondentListTest() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.populateSolicitorRespondentList(
            callbackRequest, authToken
        );

        assertTrue(response.containsKey("chooseRespondentDynamicList"));

    }

    @Test
    public void updateActiveRespondentSelectionBySolicitor() {
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(ccdDataStoreService.findUserCaseRoles(String.valueOf(caseData.getId()),
                                                   authToken)).thenReturn(findUserCaseRolesResponse);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("c100ResSolSubmit")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.updateActiveRespondentSelectionBySolicitor(
            callbackRequest, authToken
        );

        assertTrue(response.containsKey("respondents"));
    }

    @Test
    public void generateConfidentialityDynamicSelectionDisplayTest() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.generateConfidentialityDynamicSelectionDisplay(
            callbackRequest
        );

        assertTrue(response.containsKey("confidentialListDetails"));
    }

    @Test
    public void validateActiveRespondentResponse() throws Exception {

        List<String> errorList = new ArrayList<>();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        when(responseSubmitChecker.isFinished(caseData)).thenReturn(mandatoryFinished);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.validateActiveRespondentResponse(
            callbackRequest, errorList, authToken
        );

        assertTrue(response.containsKey("respondents"));

    }

    @Test
    public void validateActiveRespondentResponseWithDocument() throws Exception {

        List<String> errorList = new ArrayList<>();

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Document document = Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName("C7_Response_Final_Document.pdf")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(responseSubmitChecker.isFinished(caseData)).thenReturn(true);
        when(documentGenService.generateSingleDocument(authToken,caseData,SOLICITOR_C7_FINAL_DOCUMENT,false)).thenReturn(document);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.validateActiveRespondentResponse(
            callbackRequest, errorList, authToken
        );

        assertTrue(response.containsKey("respondents"));

    }

    @Test
    public void submitC7ResponseForActiveRespondentTest() {

        List<String> errorList = new ArrayList<>();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.submitC7ResponseForActiveRespondent(
            callbackRequest, authToken, errorList
        );

        assertTrue(response.containsKey("respondents"));
    }


    @ParameterizedTest
    @ValueSource(strings = { "c100ResSolConsentingToApplication", "c100ResSolKeepDetailsPrivate", "c100ResSolConfirmOrEditContactDetails",
        "c100ResSolAttendingTheCourt", "c100ResSolMiam", "c100ResSolCurrentOrPreviousProceedings", "c100ResSolAllegationsOfHarm",
        "c100ResSolInternationalElement", "c100ResSolAbilityToParticipate"})
    public void populateAboutToStartCaseDataConsentToApplicationEvent(String event) {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        when(ccdDataStoreService.findUserCaseRoles(String.valueOf(caseData.getId()),
                                                   authToken)).thenReturn(findUserCaseRolesResponse);

        List<String> errorList = new ArrayList<>();

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId(event)
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.populateAboutToStartCaseData(
            callbackRequest, authToken, errorList
        );

        assertTrue(response.containsKey("respondents"));
    }

    @Test
    public void testC7DraftDocument() throws Exception {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Document document = Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName("C7_Response_Draft_Document.pdf")
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(responseSubmitChecker.isFinished(caseData)).thenReturn(true);
        when(documentGenService.generateSingleDocument(authToken,caseData,SOLICITOR_C7_DRAFT_DOCUMENT,false)).thenReturn(document);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.generateDraftDocumentsForRespondent(
            callbackRequest, authToken
        );

        assertTrue(response.containsKey("draftC7ResponseDoc"));

    }

    @Test
    public void testC1ADraftDocument() throws Exception {


        caseData = caseData.toBuilder()
            .respondentAohYesNo(Yes)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(responseSubmitChecker.isFinished(caseData)).thenReturn(true);
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        Document document = Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName("C1A_Allegation_Of_Harm_Draft_Document.pdf")
            .build();
        when(documentGenService.generateSingleDocument(authToken,caseData,SOLICITOR_C1A_DRAFT_DOCUMENT,false)).thenReturn(document);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.generateDraftDocumentsForRespondent(
            callbackRequest, authToken
        );

        assertTrue(response.containsKey("draftC7ResponseDoc"));

        assertTrue(response.containsKey("draftC1ADoc"));

    }
}
