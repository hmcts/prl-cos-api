package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.PartyEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesNoIDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.ReasonableAdjustmentsEnum;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.RespondentWelshNeedsListEnum;
import uk.gov.hmcts.reform.prl.exception.RespondentSolicitorException;
import uk.gov.hmcts.reform.prl.mapper.citizen.confidentialdetails.ConfidentialDetailsMapper;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.ContactInformation;
import uk.gov.hmcts.reform.prl.models.DxAddress;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.RespChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.RespDomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Respondent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.AddressHistory;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.Contact;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.abilitytoparticipate.AbilityToParticipate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.internationalelements.CitizenInternationalElements;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.miam.Miam;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.supportyouneed.ReasonableAdjustmentsSupport;
import uk.gov.hmcts.reform.prl.models.complextypes.respondentsolicitor.documents.RespondentDocs;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentInterpreterNeeds;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.ResponseToAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespChildAbuseBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.c100respondentsolicitor.RespondentSolicitorData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.RespondentAllegationOfHarmService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators.ResponseSubmitChecker;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.reviewdocument.ReviewDocumentService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_PROFESSIONAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C1A_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_C7_DRAFT_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService.RESPONSE_ALREADY_SUBMITTED_ERROR;
import static uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.C100RespondentSolicitorService.TECH_ERROR;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@RunWith(MockitoJUnitRunner.Silent.class)
public class C100RespondentSolicitorServiceTest {

    @InjectMocks
    C100RespondentSolicitorService respondentSolicitorService;

    @Mock
    ReviewDocumentService reviewDocumentService;

    CaseData caseData;

    CaseData caseData2;

    @Mock
    RespondentAllegationOfHarmService respondentAllegationOfHarmService;

    PartyDetails respondent;

    PartyDetails respondent2;

    PartyDetails respondent3;

    PartyDetails respondent4;

    @Mock
    ObjectMapper objectMapper;


    @Mock
    CcdDataStoreService ccdDataStoreService;

    @Mock
    ResponseSubmitChecker responseSubmitChecker;

    @Mock
    DocumentLanguageService documentLanguageService;

    @Mock
    RespondentSolicitorMiamService miamService;

    @Mock
    DocumentGenService documentGenService;

    @Mock
    RespondentAllegationsOfHarmData allegationsOfHarmData;

    @Mock
    ConfidentialDetailsMapper confidentialDetailsMapper;

    @Mock
    SystemUserService systemUserService;

    @Mock
    ApplicationsTabService applicationsTabService;
    @Mock
    ManageDocumentsService manageDocumentsService;
    @Mock
    UserService userService;

    @Mock
    OrganisationService organisationService;
    @Mock
    LaunchDarklyClient launchDarklyClient;

    ResponseToAllegationsOfHarm responseToAllegationsOfHarm;

    ResponseToAllegationsOfHarm responseToAllegationsOfHarm2;

    boolean mandatoryFinished = false;

    public static final String authToken = "Bearer TestAuthToken";

    Map<String, Object> stringObjectMap;

    Map<String, Object> stringObjectMap2;
    Map<String, Object> allegationsOfHarmDataMap = new HashMap<>();

    CallbackRequest callbackRequest;

    CallbackRequest callbackRequest2;

    QuarantineLegalDoc quarantineLegalDoc;

    Document document;

    @Before
    public void setUp() {

        document = Document.builder()
            .documentFileName("test.pdf")
            .documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15")
            .build();

        quarantineLegalDoc = QuarantineLegalDoc.builder()
            .documentParty(DocumentPartyEnum.APPLICANT.getDisplayedValue())
            .categoryId("respondentC1AApplication")
            .documentUploadedDate(LocalDateTime.now())
            .build();

        RespDomesticAbuseBehaviours domesticAbuseBehaviours = RespDomesticAbuseBehaviours.builder()
                .respTypeOfAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1)
                .respAbuseNatureDescription("Test")
                .respBehavioursStartDateAndLength("5 days")
                .respBehavioursApplicantSoughtHelp(Yes)
                .respBehavioursApplicantHelpSoughtWho("Who from")
                .build();

        Element<RespDomesticAbuseBehaviours> domesticAbuseBehavioursElement = Element.<RespDomesticAbuseBehaviours>builder()
                .value(domesticAbuseBehaviours)
                .build();

        RespChildAbuse childPhysicalAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription("test")
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();

        RespChildAbuse childPsychologicalAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription("test")
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();

        RespChildAbuse childSexualAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription("test")
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();

        RespChildAbuse childEmotionalAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription("test")
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();

        RespChildAbuse childFinancialAbuse = RespChildAbuse.builder()
                .respAbuseNatureDescription("test")
                .respBehavioursStartDateAndLength("start")
                .respBehavioursApplicantHelpSoughtWho("X")
                .respBehavioursApplicantSoughtHelp(Yes)
                .build();


        allegationsOfHarmData = RespondentAllegationsOfHarmData.builder()
                .respAohYesOrNo(Yes)
                .respAohDomesticAbuseYesNo(Yes)
                .respDomesticBehaviours(Collections.singletonList(domesticAbuseBehavioursElement))
                .respOrdersNonMolestation(No)
                .respOrdersOccupation(No)
                .respOrdersForcedMarriageProtection(No)
                .respOrdersRestraining(No)
                .respOrdersOtherInjunctive(No)
                .respOrdersUndertakingInPlace(No)
                .respAohChildAbductionYesNo(No)
                .respAohOtherConcerns(Yes)
                .respAohOtherConcernsDetails("Details")
                .respAohOtherConcernsCourtActions("testing")
                .respAohSubstanceAbuseYesNo(Yes)
                .respAohSubstanceAbuseDetails("Details")
                .respAohOtherConcerns(No)
                .respAgreeChildUnsupervisedTime(No)
                .respAgreeChildSupervisedTime(No)
                .respAgreeChildOtherContact(No)
                .respChildPhysicalAbuse(childPhysicalAbuse)
                .respChildFinancialAbuse(childFinancialAbuse)
                .respChildEmotionalAbuse(childEmotionalAbuse)
                .respChildPsychologicalAbuse(childPsychologicalAbuse)
                .respChildSexualAbuse(childSexualAbuse)
                .build();

        List<ConfidentialityListEnum> confidentialityListEnums = new ArrayList<>();

        confidentialityListEnums.add(ConfidentialityListEnum.email);
        confidentialityListEnums.add(ConfidentialityListEnum.phoneNumber);
        confidentialityListEnums.add(ConfidentialityListEnum.address);

        RespondentProceedingDetails proceedingDetails = RespondentProceedingDetails.builder()
                .caseNumber("122344")
                .nameAndOffice("testoffice")
                .nameOfCourt("testCourt")
                .uploadRelevantOrder(Document.builder().build())
                .build();

        Element<RespondentProceedingDetails> proceedingDetailsElement = Element.<RespondentProceedingDetails>builder()
                .value(proceedingDetails).build();
        List<Element<RespondentProceedingDetails>> proceedingsList = Collections.singletonList(proceedingDetailsElement);

        responseToAllegationsOfHarm = ResponseToAllegationsOfHarm.builder()
                .responseToAllegationsOfHarmYesOrNoResponse(Yes)
                .responseToAllegationsOfHarmDocument(Document.builder().build())
                .build();

        responseToAllegationsOfHarm2 = ResponseToAllegationsOfHarm.builder()
            .responseToAllegationsOfHarmYesOrNoResponse(No)
            .build();

        User user = User.builder().email("respondent@example.net")
                .idamId("1234-5678").solicitorRepresented(Yes).build();

        respondent = PartyDetails.builder()
                .user(user)
                .representativeFirstName("Abc")
                .representativeLastName("Xyz")
                .gender(Gender.male)
                .email("abc@xyz.com")
                .phoneNumber("1234567890")
                .response(Response.builder()
                    .currentOrPastProceedingsForChildren(YesNoDontKnow.yes)
                        .citizenDetails(CitizenDetails.builder()
                                .firstName("test")
                                .lastName("test")
                                .contact(Contact.builder().phoneNumber("test").email("test").build())
                                .address(Address.builder().addressLine1("test").build())
                                .build())
                        .c7ResponseSubmitted(No)
                        .consent(Consent.builder()
                                .consentToTheApplication(No)
                            .permissionFromCourt(No)
                                .noConsentReason("test")
                                .build())
                        .keepDetailsPrivate(KeepDetailsPrivate
                                .builder()
                                .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                .confidentiality(Yes)
                                .confidentialityList(confidentialityListEnums)
                                .build())
                        .miam(Miam.builder().attendedMiam(No)
                                .willingToAttendMiam(No)
                                .reasonNotAttendingMiam("test").build())
                        .respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .respondentExistingProceedings(proceedingsList)
                        .citizenInternationalElements(CitizenInternationalElements
                                .builder()
                                .childrenLiveOutsideOfEnWl(Yes)
                                .childrenLiveOutsideOfEnWlDetails("Test")
                                .parentsAnyOneLiveOutsideEnWl(Yes)
                                .parentsAnyOneLiveOutsideEnWlDetails("Test")
                                .anotherPersonOrderOutsideEnWl(Yes)
                                .anotherPersonOrderOutsideEnWlDetails("test")
                                .anotherCountryAskedInformation(Yes)
                                .anotherCountryAskedInformationDetaails("test")
                                .build())
                        .supportYouNeed(ReasonableAdjustmentsSupport.builder()
                                .reasonableAdjustments(List.of(ReasonableAdjustmentsEnum.nosupport)).build())
                        .responseToAllegationsOfHarm(responseToAllegationsOfHarm)
                        .consent(Consent.builder()
                                     .permissionFromCourt(Yes)
                                     .consentToTheApplication(Yes)
                                     .build())
                        .currentOrPastProceedingsForChildren(YesNoDontKnow.yes)
                        .build())
                .canYouProvideEmailAddress(Yes)
                .isEmailAddressConfidential(No)
                .isAddressConfidential(No)
                .isPhoneNumberConfidential(No)
                .sendSignUpLink("test")
                .solicitorAddress(Address.builder().addressLine1("ABC").addressLine2("test").addressLine3("test").postCode(
                        "AB1 2MN").build())
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .dxNumber("1234")
                .address(Address.builder().addressLine1("").build())
                .solicitorReference("test")
                .build();

        List<ContactInformation> contactInformation = new ArrayList<>();
        List<DxAddress> dxAddress = new ArrayList<>();
        dxAddress.add(DxAddress.builder().dxNumber("dxNumber").build());
        contactInformation.add(ContactInformation.builder()
                .addressLine1("AddressLine1").dxAddress(dxAddress).build());

        respondent2 = PartyDetails.builder()
                .user(user)
                .representativeFirstName("Abc")
                .representativeLastName("Xyz")
                .gender(Gender.male)
                .email("abc@xyz.com")
                .sendSignUpLink("test")
                .phoneNumber("1234567890")
                .response(Response.builder()
                        .currentOrPastProceedingsForChildren(YesNoDontKnow.yes)
                        .citizenDetails(CitizenDetails.builder()
                                .firstName("test")
                                .lastName("test")
                                .build())
                        .consent(Consent.builder()
                                .consentToTheApplication(No)
                                .noConsentReason("test")
                                .permissionFromCourt(Yes)
                                .build())
                        .c7ResponseSubmitted(No)
                        .keepDetailsPrivate(KeepDetailsPrivate
                                .builder()
                                .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                .confidentiality(Yes)
                                .confidentialityList(confidentialityListEnums)
                                .build())
                        .respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .miam(Miam.builder().attendedMiam(Yes)
                                .willingToAttendMiam(No)
                                .reasonNotAttendingMiam("test").build())
                        .respondentExistingProceedings(proceedingsList)
                        .citizenInternationalElements(CitizenInternationalElements
                                .builder()
                                .childrenLiveOutsideOfEnWl(Yes)
                                .childrenLiveOutsideOfEnWlDetails("Test")
                                .parentsAnyOneLiveOutsideEnWl(Yes)
                                .parentsAnyOneLiveOutsideEnWlDetails("Test")
                                .anotherPersonOrderOutsideEnWl(Yes)
                                .anotherPersonOrderOutsideEnWlDetails("test")
                                .anotherCountryAskedInformation(Yes)
                                .anotherCountryAskedInformationDetaails("test")
                                .build())
                        .supportYouNeed(ReasonableAdjustmentsSupport.builder()
                                .reasonableAdjustments(List.of(ReasonableAdjustmentsEnum.nosupport)).build())
                        .responseToAllegationsOfHarm(responseToAllegationsOfHarm)
                        .build())
                .canYouProvideEmailAddress(Yes)
                .isEmailAddressConfidential(Yes)
                .isPhoneNumberConfidential(Yes)
                .isAddressConfidential(Yes)
            .solicitorOrg(Organisation.builder().build())
                .solicitorAddress(Address.builder().addressLine1("ABC").addressLine2("test").addressLine3("test").postCode(
                        "AB1 2MN").build())
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .solicitorReference("test")
                .address(Address.builder().addressLine1("").build())
                .organisations(Organisations.builder().contactInformation(contactInformation).build())
                .build();

        List<RespondentWelshNeedsListEnum> welshNeedsListEnum = new ArrayList<>();
        welshNeedsListEnum.add(RespondentWelshNeedsListEnum.speakWelsh);
        List<PartyEnum> party = new ArrayList<>();
        party.add(PartyEnum.respondent);

        List<CaseUser> caseUserList = new ArrayList<>();
        caseUserList.add(CaseUser.builder()
                .caseId("12345")
                .caseRole("[C100RESPONDENTSOLICITOR1]")
                .userId("1afdfa01-8280-4e2c-b810-ab7cf741988a").build());

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
        Element<PartyDetails> wrappedRespondents2 = Element.<PartyDetails>builder()
                .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                .value(respondent2).build();
        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondents);
        respondentList.add(wrappedRespondents2);
        respondentList.add(wrappedRespondents);
        respondentList.add(wrappedRespondents2);
        respondentList.add(wrappedRespondents);
        respondentList.add(wrappedRespondents);
        List<Element<RespondentInterpreterNeeds>> interpreterList = Collections.singletonList(wrappedInterpreter);
        Element<Address> wrappedAddress = Element.<Address>builder().value(address).build();
        List<Element<Address>> addressList = Collections.singletonList(wrappedAddress);
        RespondentDocs respondentDocs = RespondentDocs.builder().build();
        Element<RespondentDocs> respondentDocsElement = Element.<RespondentDocs>builder().value(respondentDocs).build();
        List<Element<RespondentDocs>> respondentDocsList = new ArrayList<>();
        respondentDocsList.add(respondentDocsElement);
        caseData = CaseData.builder().respondents(respondentList).id(1)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .courtSeal("courtSeal")
                .taskListVersion("v3")
                .respondentSolicitorData(RespondentSolicitorData.builder()
                        .respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .keepContactDetailsPrivate(KeepDetailsPrivate.builder()
                                .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                .confidentiality(Yes)
                                .confidentialityList(confidentialityListEnums)
                                .build())

                        .respondentConsentToApplication(Consent
                                .builder()
                                .noConsentReason("test")
                                .courtOrderDetails("test")
                                .consentToTheApplication(Yes)
                                .permissionFromCourt(No)
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
                        .respondentExistingProceedings(proceedingsList)
                        .abilityToParticipateInProceedings(AbilityToParticipate.builder()
                                .factorsAffectingAbilityToParticipate(
                                        Yes)
                                .build())
                        .internationalElementChild(CitizenInternationalElements.builder()
                                .childrenLiveOutsideOfEnWl(Yes)
                                .childrenLiveOutsideOfEnWlDetails("Test")
                                .parentsAnyOneLiveOutsideEnWl(Yes)
                                .parentsAnyOneLiveOutsideEnWlDetails("Test")
                                .anotherPersonOrderOutsideEnWl(Yes)
                                .anotherPersonOrderOutsideEnWlDetails("Test")
                                .anotherCountryAskedInformation(Yes)
                                .anotherCountryAskedInformationDetaails("Test")
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
                                        .isAtAddressLessThan5Years(
                                                No)
                                        .previousAddressHistory(
                                                addressList)
                                        .build())
                                .build())
                                             .hasRespondentAttendedMiam(No)
                                             .respondentWillingToAttendMiam(No)
                                             .respondentReasonNotAttendingMiam("test")
                        .responseToAllegationsOfHarm(responseToAllegationsOfHarm)
                        .build())
                .build();

        stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .eventId("SolicitorA")
                .build();
        when(confidentialDetailsMapper.mapConfidentialData(
                Mockito.any(CaseData.class),
                Mockito.anyBoolean()
        )).thenReturn(caseData);
        when(applicationsTabService.getRespondentsTable(caseData)).thenReturn(List.of(Element.<Respondent>builder().build()));
        when(organisationService.getOrganisationDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(
                Organisations.builder().contactInformation(List.of(ContactInformation.builder().build())).build());
        when(systemUserService.getSysUserToken()).thenReturn("");

        List<ConfidentialityListEnum> confidentialityListEnums2 = new ArrayList<>();

        RespondentProceedingDetails proceedingDetails2 = RespondentProceedingDetails.builder()
                .caseNumber("122344")
                .nameAndOffice("testoffice")
                .nameOfCourt("testCourt")
                .uploadRelevantOrder(Document.builder().build())
                .build();

        Element<RespondentProceedingDetails> proceedingDetailsElement2 = Element.<RespondentProceedingDetails>builder()
                .value(proceedingDetails2).build();
        List<Element<RespondentProceedingDetails>> proceedingsList2 = Collections.singletonList(proceedingDetailsElement2);

        User user2 = User.builder().email("respondent@example.net")
                .idamId("1234-5678").solicitorRepresented(Yes).build();

        respondent3 = PartyDetails.builder()
                .user(user2)
                .representativeFirstName("Abc")
                .representativeLastName("Xyz")
                .gender(Gender.male)
                .email("abc@xyz.com")
                .phoneNumber("1234567890")
                .response(Response.builder()
                    .currentOrPastProceedingsForChildren(YesNoDontKnow.yes)
                        .citizenDetails(CitizenDetails.builder()
                                .firstName(null)
                                .lastName(null)
                                .contact(Contact.builder().phoneNumber("test").email("test").build())
                                .address(Address.builder().addressLine1("test").build())
                                .build())
                        .c7ResponseSubmitted(No)
                        .consent(Consent.builder()
                                .consentToTheApplication(No)
                            .permissionFromCourt(No)
                                .noConsentReason("test")
                                .build())
                        .keepDetailsPrivate(KeepDetailsPrivate
                                .builder()
                                .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                .confidentiality(Yes)
                                .confidentialityList(confidentialityListEnums2)
                                .build())
                        .miam(Miam.builder().attendedMiam(Yes)
                                .willingToAttendMiam(Yes)
                                .reasonNotAttendingMiam("test").build())
                        .respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .respondentExistingProceedings(proceedingsList2)
                        .citizenInternationalElements(CitizenInternationalElements
                                .builder()
                                .childrenLiveOutsideOfEnWl(Yes)
                                .childrenLiveOutsideOfEnWlDetails("Test")
                                .parentsAnyOneLiveOutsideEnWl(Yes)
                                .parentsAnyOneLiveOutsideEnWlDetails("Test")
                                .anotherPersonOrderOutsideEnWl(Yes)
                                .anotherPersonOrderOutsideEnWlDetails("test")
                                .anotherCountryAskedInformation(Yes)
                                .anotherCountryAskedInformationDetaails("test")
                                .build())
                        .supportYouNeed(ReasonableAdjustmentsSupport.builder()
                                .reasonableAdjustments(List.of(ReasonableAdjustmentsEnum.nosupport)).build())
                        .responseToAllegationsOfHarm(responseToAllegationsOfHarm)
                        .build())
                .canYouProvideEmailAddress(Yes)
                .isEmailAddressConfidential(No)
                .isAddressConfidential(No)
                .isPhoneNumberConfidential(No)
                .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
                .solicitorAddress(Address.builder().addressLine1("ABC").addressLine2("test").addressLine3("test").postCode(
                        "AB1 2MN").build())
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .dxNumber("1234")
                .address(Address.builder().addressLine1("").build())
                .solicitorReference("test")
                .build();

        List<ContactInformation> contactInformation2 = new ArrayList<>();
        List<DxAddress> dxAddress2 = new ArrayList<>();
        dxAddress.add(DxAddress.builder().dxNumber("dxNumber").build());
        contactInformation2.add(ContactInformation.builder()
                .addressLine1("AddressLine1").dxAddress(dxAddress2).build());

        respondent4 = PartyDetails.builder()
                .user(user)
                .representativeFirstName("Abc")
                .representativeLastName("Xyz")
                .gender(Gender.male)
                .email("abc@xyz.com")
                .phoneNumber("1234567890")
                .response(Response.builder()
                    .currentOrPastProceedingsForChildren(YesNoDontKnow.yes)
                        .citizenDetails(CitizenDetails.builder()
                                .firstName(null)
                                .lastName(null)
                                .contact(Contact.builder()
                                        .phoneNumber("123")
                                        .email("test@test.com").build())
                                .address(Address.builder().addressLine1("123").build())
                                .build())
                        .consent(Consent.builder()
                                .consentToTheApplication(No)
                            .permissionFromCourt(No)
                                .noConsentReason("test")
                                .build())
                        .c7ResponseSubmitted(No)
                        .keepDetailsPrivate(KeepDetailsPrivate
                                .builder()
                                .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                .confidentiality(Yes)
                                .confidentialityList(confidentialityListEnums2)
                                .build())
                        .miam(Miam.builder().attendedMiam(No)
                                .willingToAttendMiam(No)
                                .reasonNotAttendingMiam("test").build())
                        .respondentExistingProceedings(proceedingsList2)
                        .respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .citizenInternationalElements(CitizenInternationalElements
                                .builder()
                                .childrenLiveOutsideOfEnWl(Yes)
                                .childrenLiveOutsideOfEnWlDetails("Test")
                                .parentsAnyOneLiveOutsideEnWl(Yes)
                                .parentsAnyOneLiveOutsideEnWlDetails("Test")
                                .anotherPersonOrderOutsideEnWl(Yes)
                                .anotherPersonOrderOutsideEnWlDetails("test")
                                .anotherCountryAskedInformation(Yes)
                                .anotherCountryAskedInformationDetaails("test")
                                .build())
                        .supportYouNeed(ReasonableAdjustmentsSupport.builder()
                                .reasonableAdjustments(List.of(ReasonableAdjustmentsEnum.nosupport)).build())
                        .responseToAllegationsOfHarm(responseToAllegationsOfHarm)
                        .build())
                .canYouProvideEmailAddress(Yes)
                .isEmailAddressConfidential(No)
                .isPhoneNumberConfidential(No)
                .isAddressConfidential(No)
                .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
                .solicitorAddress(Address.builder().addressLine1("ABC").addressLine2("test").addressLine3("test").postCode(
                        "AB1 2MN").build())
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .solicitorReference("test")
                .address(Address.builder().addressLine1("").build())
                .organisations(Organisations.builder().contactInformation(contactInformation2).build())
                .build();

        List<RespondentWelshNeedsListEnum> welshNeedsListEnum2 = new ArrayList<>();
        welshNeedsListEnum2.add(RespondentWelshNeedsListEnum.speakWelsh);
        List<PartyEnum> party2 = new ArrayList<>();
        party2.add(PartyEnum.respondent);

        List<CaseUser> caseUserList2 = new ArrayList<>();
        caseUserList2.add(CaseUser.builder()
                .caseId("12345")
                .caseRole("[C100RESPONDENTSOLICITOR1]")
                .userId("1afdfa01-8280-4e2c-b810-ab7cf741988a").build());

        Address address2 = Address.builder()
                .addressLine1("test")
                .postCode("test")
                .build();
        RespondentInterpreterNeeds interpreterNeeds2 = RespondentInterpreterNeeds.builder()
                .party(party)
                .relationName("Test")
                .requiredLanguage("Cornish")
                .build();
        Element<RespondentInterpreterNeeds> wrappedInterpreter2 = Element.<RespondentInterpreterNeeds>builder()
                .value(interpreterNeeds2).build();
        Element<PartyDetails> wrappedRespondents3 = Element.<PartyDetails>builder()
                .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                .value(respondent3).build();
        Element<PartyDetails> wrappedRespondents4 = Element.<PartyDetails>builder()
                .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                .value(respondent4).build();
        List<Element<PartyDetails>> respondentList2 = new ArrayList<>();
        respondentList2.add(wrappedRespondents3);
        respondentList2.add(wrappedRespondents4);
        respondentList2.add(wrappedRespondents3);
        respondentList2.add(wrappedRespondents4);
        respondentList2.add(wrappedRespondents3);
        respondentList2.add(wrappedRespondents4);
        List<Element<RespondentInterpreterNeeds>> interpreterList2 = Collections.singletonList(wrappedInterpreter2);
        Element<Address> wrappedAddress2 = Element.<Address>builder().value(address2).build();
        List<Element<Address>> addressList2 = Collections.singletonList(wrappedAddress2);
        RespondentDocs respondentDocs2 = RespondentDocs.builder().build();
        Element<RespondentDocs> respondentDocsElement2 = Element.<RespondentDocs>builder().value(respondentDocs2).build();
        List<Element<RespondentDocs>> respondentDocsList2 = new ArrayList<>();
        respondentDocsList2.add(respondentDocsElement2);
        caseData2 = CaseData.builder().respondents(respondentList2).id(1)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .respondentSolicitorData(RespondentSolicitorData.builder()
                        .keepContactDetailsPrivate(KeepDetailsPrivate.builder()
                                .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                .confidentiality(Yes)
                                .confidentialityList(confidentialityListEnums2)
                                .build())
                        .respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .respondentConsentToApplication(Consent
                                .builder()
                                .noConsentReason("test")
                                .courtOrderDetails("test")
                                .consentToTheApplication(Yes)
                                .permissionFromCourt(No)
                                .build())
                        .respondentAttendingTheCourt(AttendToCourt.builder()
                                .respondentWelshNeeds(Yes)
                                .respondentWelshNeedsList(welshNeedsListEnum2)
                                .isRespondentNeededInterpreter(Yes)
                                .respondentInterpreterNeeds(interpreterList2)
                                .haveAnyDisability(Yes)
                                .disabilityNeeds("Test")
                                .respondentSpecialArrangements(Yes)
                                .respondentSpecialArrangementDetails("Test")
                                .respondentIntermediaryNeeds(Yes)
                                .respondentIntermediaryNeedDetails("Test")
                                .build())
                        .currentOrPastProceedingsForChildren(YesNoDontKnow.no)
                        .respondentExistingProceedings(proceedingsList2)
                        .abilityToParticipateInProceedings(AbilityToParticipate.builder()
                                .provideDetailsForFactorsAffectingAbilityToParticipate("Test")
                                .detailsOfReferralOrAssessment("Test")
                                .giveDetailsAffectingLitigationCapacity("Test")
                                .factorsAffectingAbilityToParticipate(
                                        Yes)
                                .build())
                        .internationalElementChild(CitizenInternationalElements.builder()
                                .childrenLiveOutsideOfEnWl(Yes)
                                .childrenLiveOutsideOfEnWlDetails("Test")
                                .parentsAnyOneLiveOutsideEnWl(Yes)
                                .parentsAnyOneLiveOutsideEnWlDetails("Test")
                                .anotherPersonOrderOutsideEnWl(Yes)
                                .anotherPersonOrderOutsideEnWlDetails("Test")
                                .anotherCountryAskedInformation(Yes)
                                .anotherCountryAskedInformationDetaails("Test")
                                .build())
                        .resSolConfirmEditContactDetails(CitizenDetails
                                .builder()
                                .firstName("Test")
                                .lastName("Test")
                                .address(address2)
                                .contact(Contact.builder()
                                        .email("Test")
                                        .phoneNumber("0785544").build())
                                .addressHistory(AddressHistory.builder()
                                        .isAtAddressLessThan5Years(
                                                No)
                                        .previousAddressHistory(
                                                addressList2)
                                        .build())
                                .build())
                                             .hasRespondentAttendedMiam(No)
                                             .respondentWillingToAttendMiam(No)
                                             .respondentReasonNotAttendingMiam("test")
                        .responseToAllegationsOfHarm(responseToAllegationsOfHarm)
                        .build())
                .build();



        stringObjectMap2 = caseData2.toMap(new ObjectMapper());


        callbackRequest2 = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap2)
                        .build())
                .eventId("SolicitorA")
                .build();
        when(confidentialDetailsMapper.mapConfidentialData(
                Mockito.any(CaseData.class),
                Mockito.anyBoolean()
        )).thenReturn(caseData);
        when(applicationsTabService.getRespondentsTable(caseData2)).thenReturn(List.of(Element.<Respondent>builder().build()));
        when(organisationService.getOrganisationDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(
                Organisations.builder().contactInformation(List.of(ContactInformation.builder().build())).build());
        when(systemUserService.getSysUserToken()).thenReturn("");

        Map<String, Object> allegationsOfHarmDataMap = Map.of(
                "respAohYesOrNo", "Yes"
        );

        when(objectMapper.convertValue(eq(allegationsOfHarmData),
                Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(allegationsOfHarmDataMap);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(
            Mockito.any(CaseData.class)
        )).thenReturn(documentLanguage);
    }

    @Test
    public void populateAboutToStartCaseDataResSolConsentingToApplicationTest() {

        String[] events = {"c100ResSolConsentingToApplicationA", "c100ResSolKeepDetailsPrivateA",
            "c100ResSolConfirmOrEditContactDetailsA", "c100ResSolAttendingTheCourtA", "c100ResSolMiamA",
            "c100ResSolCurrentOrPreviousProceedingsA",
            "c100ResSolAllegationsOfHarmA", "c100ResSolInternationalElementA", "c100ResSolLitigationCapacityA",
            "c100ResSolViewResponseDraftDocumentA", "c100ResSolResponseToAllegationsOfHarmA"};
        for (String event : events) {
            callbackRequest.setEventId(event);
            Map<String, Object> response = respondentSolicitorService.populateAboutToStartCaseData(
                    callbackRequest
            );

            assertTrue(response.containsKey("respondents"));
        }
    }

    @Test
    public void populateAboutToSubmitCaseDataSolCurrentOrPreviousProceedingTest() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(Mockito.<RespondentAllegationsOfHarmData>any(),
                Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(allegationsOfHarmDataMap);
        List<String> errorList = new ArrayList<>();
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .eventId("c100ResSolCurrentOrPreviousProceedings")
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .build();
        when(objectMapper.convertValue(Mockito.<RespondentAllegationsOfHarmData>any(),
                Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(allegationsOfHarmDataMap);

        Map<String, Object> response = respondentSolicitorService.populateAboutToSubmitCaseData(callbackRequest);

        assertTrue(response.containsKey("respondents"));
    }

    @Test
    public void validateActiveRespondentResponse() throws Exception {
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(responseSubmitChecker.isFinished(respondent, true)).thenReturn(mandatoryFinished);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder()
                .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                .value(respondent).build();
        caseData = caseData.toBuilder()
                .respondentSolicitorData(RespondentSolicitorData.builder()
                        .respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .build())
                .respondents(List.of(wrappedRespondents, wrappedRespondents))
                .build();

        when(documentLanguageService.docGenerateLang(any())).thenReturn(DocumentLanguage.builder().isGenEng(true).isGenWelsh(false).build());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("c100ResSolConsentingToApplicationA")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        List<String> errorList = new ArrayList<>();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(false).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(
            Mockito.any(CaseData.class)
        )).thenReturn(documentLanguage);

        Map<String, Object> response = respondentSolicitorService.validateActiveRespondentResponse(
            callbackRequest, errorList, authToken
        );

        assertTrue(response.containsKey("respondents"));
    }

    @Test
    public void validateActiveRespondentResponseWelsh() throws Exception {
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(responseSubmitChecker.isFinished(respondent, true)).thenReturn(mandatoryFinished);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder()
            .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
            .value(respondent).build();
        caseData = caseData.toBuilder()
            .respondentSolicitorData(RespondentSolicitorData.builder()
                                         .respondentAllegationsOfHarmData(allegationsOfHarmData)
                                         .build())
            .respondents(List.of(wrappedRespondents, wrappedRespondents))
            .build();

        when(documentLanguageService.docGenerateLang(any())).thenReturn(DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("c100ResSolConsentingToApplicationA")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        List<String> errorList = new ArrayList<>();

        Map<String, Object> response = respondentSolicitorService.validateActiveRespondentResponse(
            callbackRequest, errorList, authToken
        );

        assertTrue(response.containsKey("respondents"));
    }

    @Test
    public void validateActiveRespondentResponseIfNoRespondentExists() throws Exception {
        caseData = caseData.toBuilder()
                .respondentSolicitorData(RespondentSolicitorData.builder().respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .build())
                .respondents(new ArrayList<>())
                .build();
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(responseSubmitChecker.isFinished(respondent, true)).thenReturn(mandatoryFinished);
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .eventId("c100ResSolConsentingToApplicationA")
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .build();

        List<String> errorList = new ArrayList<>();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(false).build();
        when(documentLanguageService.docGenerateLang(
            Mockito.any(CaseData.class)
        )).thenReturn(documentLanguage);

        Map<String, Object> response = respondentSolicitorService.validateActiveRespondentResponse(
                callbackRequest, errorList, authToken
        );

        assertTrue(response.containsKey("respondents"));
    }


    @Test
    public void validateActiveRespondentResponseElseCase() throws Exception {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        caseData = caseData.toBuilder().respondentSolicitorData(RespondentSolicitorData.builder()
                .respondentAllegationsOfHarmData(allegationsOfHarmData)
                .build()).build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(responseSubmitChecker.isFinished(Mockito.any(), Mockito.anyBoolean())).thenReturn(true);
        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(false).build();
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(documentLanguage);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .eventId("c100ResSolConsentingToApplicationA")
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .build();

        Map<String, Object> response = respondentSolicitorService.validateActiveRespondentResponse(
                callbackRequest, new ArrayList<>(), authToken
        );

        assertTrue(response.containsKey("finalC1AResponseDoc"));
    }

    @Test
    public void submitC7ResponseForActiveRespondentTest() throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
                .url("TestUrl")
                .binaryUrl("binaryUrl")
                .hashToken("testHashToken")
                .build();

        Document document = Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("c100RespC8Template")
                .build();

        when(documentGenService.generateSingleDocument(
                Mockito.anyString(),
                Mockito.any(CaseData.class),
                Mockito.anyString(),
                Mockito.anyBoolean(),
                Mockito.any(HashMap.class)
        )).thenReturn(document);

        Document document2 = Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("solicitorC1AFinalTemplate")
                .documentCreatedOn(new Date())
                .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(false).build();
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(documentLanguage);

        when(documentGenService.generateSingleDocument(
                Mockito.anyString(),
                Mockito.any(CaseData.class),
                Mockito.anyString(),
                Mockito.anyBoolean(),
                Mockito.any(HashMap.class)
        )).thenReturn(document2);
        UserDetails userDetails = UserDetails.builder().forename("test")
                .roles(Arrays.asList("caseworker-privatelaw-solicitor")).build();

        when(documentLanguageService.docGenerateLang(
            Mockito.any(CaseData.class)
        )).thenReturn(documentLanguage);

        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);

        callbackRequest.setEventId("c100ResSolConsentingToApplicationA");
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(false);

        List<String> errorList = new ArrayList<>();
        Map<String, Object> response = respondentSolicitorService.submitC7ResponseForActiveRespondent(
                authToken, callbackRequest
        );
        Assertions.assertTrue(response.containsKey("respondentAc8"));
    }

    @Test
    public void submitC7ResponseForActiveRespondentWelshTest() throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        Document document = Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName("c100RespC8Template")
            .build();

        when(documentGenService.generateSingleDocument(
            Mockito.anyString(),
            Mockito.any(CaseData.class),
            Mockito.anyString(),
            Mockito.anyBoolean(),
            Mockito.any(HashMap.class)
        )).thenReturn(document);

        Document document2 = Document.builder()
            .documentUrl(generatedDocumentInfo.getUrl())
            .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
            .documentHash(generatedDocumentInfo.getHashToken())
            .documentFileName("solicitorC1AFinalTemplate")
            .documentCreatedOn(new Date())
            .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(true).build();
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(documentLanguage);

        when(documentGenService.generateSingleDocument(
            Mockito.anyString(),
            Mockito.any(CaseData.class),
            Mockito.anyString(),
            Mockito.anyBoolean(),
            Mockito.any(HashMap.class)
        )).thenReturn(document2);
        UserDetails userDetails = UserDetails.builder().forename("test")
            .roles(Arrays.asList("caseworker-privatelaw-solicitor")).build();

        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);

        callbackRequest.setEventId("c100ResSolConsentingToApplicationA");

        List<String> errorList = new ArrayList<>();
        Map<String, Object> response = respondentSolicitorService.submitC7ResponseForActiveRespondent(
            authToken, callbackRequest
        );
        Assertions.assertTrue(response.containsKey("respondentAc8"));
    }

    @Test
    public void submitC7ResponseForActiveRespondentTestB() throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
                .url("TestUrl")
                .binaryUrl("binaryUrl")
                .hashToken("testHashToken")
                .build();

        Document document = Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("c100RespC8Template")
                .documentCreatedOn(new Date())
                .build();

        when(documentGenService.generateSingleDocument(
                Mockito.anyString(),
                Mockito.any(CaseData.class),
                Mockito.anyString(),
                Mockito.anyBoolean(),
                Mockito.any(HashMap.class)
        )).thenReturn(document);


        String[] eventsAndRespts = {"c100ResSolConsentingToApplicationB - respondentBc8",
            "c100ResSolConsentingToApplicationC - respondentCc8",
            "c100ResSolConsentingToApplicationD - respondentDc8"};

        for (String eventsAndResp : eventsAndRespts) {

            Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder()
                    .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                    .value(respondent).build();
            Element<PartyDetails> wrappedRespondents2 = Element.<PartyDetails>builder()
                    .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                    .value(respondent2).build();
            List<Element<PartyDetails>> respondentList = new ArrayList<>();
            respondentList.add(wrappedRespondents);
            respondentList.add(wrappedRespondents2);
            respondentList.add(wrappedRespondents);
            respondentList.add(wrappedRespondents2);

            CaseData caseData = CaseData.builder().respondents(respondentList).id(1)
                    .caseTypeOfApplication(C100_CASE_TYPE)
                    .respondentSolicitorData(RespondentSolicitorData.builder()
                            .respondentAllegationsOfHarmData(allegationsOfHarmData)
                            .build())
                    .build();

            stringObjectMap = caseData.toMap(new ObjectMapper());

            when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

            DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(false).build();
            when(documentLanguageService.docGenerateLang(caseData)).thenReturn(documentLanguage);

            CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                    .CallbackRequest.builder()
                    .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                            .id(123L)
                            .data(stringObjectMap)
                            .build())
                    .build();

            String event = eventsAndResp.split(HYPHEN_SEPARATOR)[0];

            callbackRequest.setEventId(event);
            when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(false);

            UserDetails userDetails = UserDetails.builder().forename("test")
                    .roles(Arrays.asList("caseworker-privatelaw-solicitor")).build();
            String respondent = eventsAndResp.split(HYPHEN_SEPARATOR)[1];
            when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);
            Map<String, Object> response = respondentSolicitorService.submitC7ResponseForActiveRespondent(
                    authToken, callbackRequest
            );
            Assertions.assertTrue(response.containsKey(respondent));
        }
    }

    @Test
    public void submitC7ResponseForActiveRespondentTestE() throws Exception {
        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
                .url("TestUrl")
                .binaryUrl("binaryUrl")
                .hashToken("testHashToken")
                .build();

        Document document = Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("c100RespC8Template")
                .documentCreatedOn(new Date())
                .build();

        caseData = caseData.toBuilder()
                .respondentSolicitorData(RespondentSolicitorData.builder().respondentAllegationsOfHarmData(allegationsOfHarmData
                ).build())
                .build();

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(false).build();
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(documentLanguage);

        when(documentGenService.generateSingleDocument(
                Mockito.anyString(),
                Mockito.any(CaseData.class),
                Mockito.anyString(),
                Mockito.anyBoolean(),
                Mockito.any(HashMap.class)
        )).thenReturn(document);
        when(launchDarklyClient.isFeatureEnabled("role-assignment-api-in-orders-journey")).thenReturn(false);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        UserDetails userDetails = UserDetails.builder().forename("test")
                .roles(Arrays.asList("caseworker-privatelaw-solicitor")).build();

        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);
        callbackRequest.setEventId("c100ResSolConsentingToApplicationE");
        List<String> errorList = new ArrayList<>();
        when(documentLanguageService.docGenerateLang(
            Mockito.any(CaseData.class)
        )).thenReturn(documentLanguage);

        Map<String, Object> response = respondentSolicitorService.submitC7ResponseForActiveRespondent(
                authToken, callbackRequest
        );
        Assertions.assertTrue(response.containsKey("respondentEc8"));
    }

    @Test
    public void populateAboutToSubmitCaseDataForC100ResSolKeepDetailsPrivateATest() throws Exception {

        when(responseSubmitChecker.isFinished(respondent3, true)).thenReturn(mandatoryFinished);
        when(objectMapper.convertValue(Mockito.<RespondentAllegationsOfHarmData>any(),
                Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(allegationsOfHarmDataMap);

        caseData = caseData.toBuilder()
                .respondentSolicitorData(RespondentSolicitorData.builder().respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .build())
                .build();


        String[] events = {"c100ResSolKeepDetailsPrivateA", "c100ResSolConfirmOrEditContactDetailsA", "c100ResSolAttendingTheCourtA",
            "c100ResSolMiamA", "c100ResSolCurrentOrPreviousProceedingsA", "c100ResSolAllegationsOfHarmA", "c100ResSolInternationalElementA",
            "c100ResSolLitigationCapacityA", "c100ResSolConsentingToApplicationA"};
        for (String event : events) {
            callbackRequest.setEventId(event);
            Map<String, Object> response = respondentSolicitorService.populateAboutToSubmitCaseData(
                    callbackRequest
            );
            assertTrue(response.containsKey("respondents"));
        }
    }

    @Test
    public void populateAboutToSubmitCaseDataForC100ResSolCurrentOrPreviousProceedingsAWhileExistingProceedingNoTest() throws Exception {

        when(responseSubmitChecker.isFinished(respondent3, true)).thenReturn(mandatoryFinished);
        when(objectMapper.convertValue(Mockito.<RespondentAllegationsOfHarmData>any(),
                Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(allegationsOfHarmDataMap);

        String[] events = {"c100ResSolCurrentOrPreviousProceedingsA"};
        for (String event : events) {
            callbackRequest.setEventId(event);
            Map<String, Object> response = respondentSolicitorService.populateAboutToSubmitCaseData(
                    callbackRequest
            );
            assertTrue(response.containsKey("respondents"));
        }
    }

    @Test
    public void populateAboutToSubmitCaseDataWithFewNullsTest() {
        List<ConfidentialityListEnum> confidentialityListEnums = new ArrayList<>();

        RespondentProceedingDetails proceedingDetails = RespondentProceedingDetails.builder()
                .caseNumber("122344")
                .nameAndOffice("testoffice")
                .nameOfCourt("testCourt")
                .uploadRelevantOrder(Document.builder().build())
                .build();

        Element<RespondentProceedingDetails> proceedingDetailsElement = Element.<RespondentProceedingDetails>builder()
                .value(proceedingDetails).build();
        List<Element<RespondentProceedingDetails>> proceedingsList = Collections.singletonList(proceedingDetailsElement);

        User user = User.builder().email("respondent@example.net")
                .idamId("1234-5678").solicitorRepresented(Yes).build();

        respondent = PartyDetails.builder()
                .user(user)
                .representativeFirstName("Abc")
                .representativeLastName("Xyz")
                .gender(Gender.male)
                .email("abc@xyz.com")
                .phoneNumber("1234567890")
                .response(Response.builder()
                        .citizenDetails(CitizenDetails.builder()
                                .firstName("test")
                                .lastName("test")
                                .contact(Contact.builder().phoneNumber("test").email("test").build())
                                .address(Address.builder().addressLine1("test").build())
                                .build())
                        .c7ResponseSubmitted(No)
                        .consent(Consent.builder()
                                .consentToTheApplication(No)
                            .permissionFromCourt(No)
                                .noConsentReason("test")
                                .build())
                        .keepDetailsPrivate(KeepDetailsPrivate
                                .builder()
                                .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                .confidentiality(Yes)
                                .confidentialityList(confidentialityListEnums)
                                .build())
                        .miam(Miam.builder().attendedMiam(No)
                                .willingToAttendMiam(No)
                                .reasonNotAttendingMiam("test").build())
                        .respondentExistingProceedings(proceedingsList)
                        .citizenInternationalElements(CitizenInternationalElements
                                .builder()
                                .childrenLiveOutsideOfEnWl(Yes)
                                .childrenLiveOutsideOfEnWlDetails("Test")
                                .parentsAnyOneLiveOutsideEnWl(Yes)
                                .parentsAnyOneLiveOutsideEnWlDetails("Test")
                                .anotherPersonOrderOutsideEnWl(Yes)
                                .anotherPersonOrderOutsideEnWlDetails("test")
                                .anotherCountryAskedInformation(Yes)
                                .anotherCountryAskedInformationDetaails("test")
                                .build())
                        .supportYouNeed(ReasonableAdjustmentsSupport.builder()
                                .reasonableAdjustments(List.of(ReasonableAdjustmentsEnum.nosupport)).build())
                        .build())
                .canYouProvideEmailAddress(Yes)
                .isEmailAddressConfidential(No)
                .isAddressConfidential(No)
                .isPhoneNumberConfidential(No)
                .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
                .solicitorAddress(Address.builder().addressLine1("ABC").addressLine2("test").addressLine3("test").postCode(
                        "AB1 2MN").build())
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .dxNumber("1234")
                .address(Address.builder().addressLine1("").build())
                .solicitorReference("test")
                .build();

        List<ContactInformation> contactInformation = new ArrayList<>();
        List<DxAddress> dxAddress = new ArrayList<>();
        dxAddress.add(DxAddress.builder().dxNumber("dxNumber").build());
        contactInformation.add(ContactInformation.builder()
                .addressLine1("AddressLine1").dxAddress(dxAddress).build());

        respondent2 = PartyDetails.builder()
                .user(user)
                .representativeFirstName("Abc")
                .representativeLastName("Xyz")
                .gender(Gender.male)
                .email("abc@xyz.com")
                .phoneNumber("1234567890")
                .response(Response.builder()
                        .citizenDetails(CitizenDetails.builder()
                                .firstName("test")
                                .lastName("test")
                                .build())
                        .consent(Consent.builder()
                                .consentToTheApplication(No)
                            .permissionFromCourt(No)
                                .noConsentReason("test")
                                .build())
                        .c7ResponseSubmitted(No)
                        .keepDetailsPrivate(KeepDetailsPrivate
                                .builder()
                                .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                .confidentiality(Yes)
                                .confidentialityList(confidentialityListEnums)
                                .build())
                        .miam(Miam.builder().attendedMiam(No)
                                .willingToAttendMiam(No)
                                .reasonNotAttendingMiam("test").build())
                        .respondentExistingProceedings(proceedingsList)
                        .citizenInternationalElements(CitizenInternationalElements
                                .builder()
                                .childrenLiveOutsideOfEnWl(Yes)
                                .childrenLiveOutsideOfEnWlDetails("Test")
                                .parentsAnyOneLiveOutsideEnWl(Yes)
                                .parentsAnyOneLiveOutsideEnWlDetails("Test")
                                .anotherPersonOrderOutsideEnWl(Yes)
                                .anotherPersonOrderOutsideEnWlDetails("test")
                                .anotherCountryAskedInformation(Yes)
                                .anotherCountryAskedInformationDetaails("test")
                                .build())
                        .respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .supportYouNeed(ReasonableAdjustmentsSupport.builder()
                                .reasonableAdjustments(List.of(ReasonableAdjustmentsEnum.nosupport)).build())
                        .build())
                .canYouProvideEmailAddress(Yes)
                .isEmailAddressConfidential(Yes)
                .isPhoneNumberConfidential(Yes)
                .isAddressConfidential(Yes)
                .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
                .solicitorAddress(Address.builder().addressLine1("ABC").addressLine2("test").addressLine3("test").postCode(
                        "AB1 2MN").build())
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .solicitorReference("test")
                .address(Address.builder().addressLine1("").build())
                .organisations(Organisations.builder().contactInformation(contactInformation).build())
                .build();

        List<RespondentWelshNeedsListEnum> welshNeedsListEnum = new ArrayList<>();
        welshNeedsListEnum.add(RespondentWelshNeedsListEnum.speakWelsh);
        List<PartyEnum> party = new ArrayList<>();
        party.add(PartyEnum.respondent);

        List<CaseUser> caseUserList = new ArrayList<>();
        caseUserList.add(CaseUser.builder()
                .caseId("12345")
                .caseRole("[C100RESPONDENTSOLICITOR1]")
                .userId("1afdfa01-8280-4e2c-b810-ab7cf741988a").build());

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
        Element<PartyDetails> wrappedRespondents2 = Element.<PartyDetails>builder()
                .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                .value(respondent2).build();
        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondents);
        respondentList.add(wrappedRespondents2);
        respondentList.add(wrappedRespondents);
        respondentList.add(wrappedRespondents2);
        respondentList.add(wrappedRespondents);
        respondentList.add(wrappedRespondents);
        List<Element<RespondentInterpreterNeeds>> interpreterList = Collections.singletonList(wrappedInterpreter);
        Element<Address> wrappedAddress = Element.<Address>builder().value(address).build();
        List<Element<Address>> addressList = Collections.singletonList(wrappedAddress);
        RespondentDocs respondentDocs = RespondentDocs.builder().build();
        Element<RespondentDocs> respondentDocsElement = Element.<RespondentDocs>builder().value(respondentDocs).build();
        List<Element<RespondentDocs>> respondentDocsList = new ArrayList<>();
        respondentDocsList.add(respondentDocsElement);
        caseData = CaseData.builder().respondents(respondentList).id(1)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .respondentSolicitorData(RespondentSolicitorData.builder()
                        .respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .keepContactDetailsPrivate(KeepDetailsPrivate.builder()
                                .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                .confidentiality(Yes)
                                .confidentialityList(confidentialityListEnums)
                                .build())

                        .respondentConsentToApplication(Consent
                                .builder()
                                .noConsentReason("test")
                                .courtOrderDetails("test")
                                .consentToTheApplication(Yes)
                                .permissionFromCourt(No)
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
                        .respondentExistingProceedings(proceedingsList)
                        .abilityToParticipateInProceedings(AbilityToParticipate.builder()
                                .factorsAffectingAbilityToParticipate(
                                        No)
                                .build())
                        .internationalElementChild(CitizenInternationalElements.builder()
                                .childrenLiveOutsideOfEnWl(Yes)
                                .childrenLiveOutsideOfEnWlDetails("Test")
                                .parentsAnyOneLiveOutsideEnWl(Yes)
                                .parentsAnyOneLiveOutsideEnWlDetails("Test")
                                .anotherPersonOrderOutsideEnWl(Yes)
                                .anotherPersonOrderOutsideEnWlDetails("Test")
                                .anotherCountryAskedInformation(Yes)
                                .anotherCountryAskedInformationDetaails("Test")
                                .build())
                        .respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .resSolConfirmEditContactDetails(CitizenDetails
                                .builder()
                                .firstName("Test")
                                .lastName("Test")
                                .address(address)
                                .contact(Contact.builder()
                                        .email("Test")
                                        .phoneNumber("0785544").build())
                                .addressHistory(AddressHistory.builder()
                                        .isAtAddressLessThan5Years(
                                                No)
                                        .previousAddressHistory(
                                                addressList)
                                        .build())
                                .build())
                                .hasRespondentAttendedMiam(No)
                                .respondentWillingToAttendMiam(No)
                                .respondentReasonNotAttendingMiam("test")
                        .build())
                .build();

        stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .eventId("SolicitorA")
                .build();
        when(confidentialDetailsMapper.mapConfidentialData(
                Mockito.any(CaseData.class),
                Mockito.anyBoolean()
        )).thenReturn(caseData);
        when(applicationsTabService.getRespondentsTable(caseData)).thenReturn(List.of(Element.<Respondent>builder().build()));
        when(organisationService.getOrganisationDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(
                Organisations.builder().contactInformation(List.of(ContactInformation.builder().build())).build());
        when(systemUserService.getSysUserToken()).thenReturn("");

        when(responseSubmitChecker.isFinished(respondent, true)).thenReturn(mandatoryFinished);
        when(objectMapper.convertValue(Mockito.<RespondentAllegationsOfHarmData>any(),
                Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(allegationsOfHarmDataMap);

        String[] events = {"c100ResSolKeepDetailsPrivateA", "c100ResSolConfirmOrEditContactDetailsA", "c100ResSolAttendingTheCourtA",
            "c100ResSolMiamA", "c100ResSolCurrentOrPreviousProceedingsA", "c100ResSolAllegationsOfHarmA", "c100ResSolInternationalElementA",
            "c100ResSolLitigationCapacityA", "c100ResSolConsentingToApplicationA"};
        for (String event : events) {
            callbackRequest.setEventId(event);
            Map<String, Object> response = respondentSolicitorService.populateAboutToSubmitCaseData(
                    callbackRequest
            );

            assertTrue(response.containsKey("respondents"));
        }
    }


    @Test
    public void populateAboutToSubmitCaseDataForC100RMiamWithAttendMiamYesOrNoTest() throws Exception {

        YesOrNo[] attendMiam = {Yes, No};
        for (YesOrNo yesOrNo : attendMiam) {
            User user = User.builder().email("respondent@example.net")
                    .idamId("1234-5678").solicitorRepresented(Yes).build();

            respondent = PartyDetails.builder()
                    .user(user)
                    .response(Response.builder().build())
                    .build();

            List<CaseUser> caseUserList = new ArrayList<>();
            caseUserList.add(CaseUser.builder()
                    .caseId("12345")
                    .caseRole("[C100RESPONDENTSOLICITOR1]")
                    .userId("1afdfa01-8280-4e2c-b810-ab7cf741988a").build());

            Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder()
                    .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                    .value(respondent).build();
            Element<PartyDetails> wrappedRespondents2 = Element.<PartyDetails>builder()
                    .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                    .value(respondent2).build();
            List<Element<PartyDetails>> respondentList = new ArrayList<>();
            respondentList.add(wrappedRespondents);
            respondentList.add(wrappedRespondents2);
            RespondentDocs respondentDocs = RespondentDocs.builder().build();
            Element<RespondentDocs> respondentDocsElement = Element.<RespondentDocs>builder().value(respondentDocs).build();
            List<Element<RespondentDocs>> respondentDocsList = new ArrayList<>();
            respondentDocsList.add(respondentDocsElement);
            CaseData caseData1 = CaseData.builder().respondents(respondentList).id(1)
                    .caseTypeOfApplication(C100_CASE_TYPE)
                    .respondents(respondentList)
                    .respondentSolicitorData(RespondentSolicitorData.builder()
                            .respondentAllegationsOfHarmData(allegationsOfHarmData)
                                                 .hasRespondentAttendedMiam(No)
                                                 .respondentWillingToAttendMiam(No)
                                                 .respondentReasonNotAttendingMiam("test")
                                                 .build())
                    .build();

            Map<String, Object> stringObjectMap1 = caseData1.toMap(new ObjectMapper());

            when(objectMapper.convertValue(stringObjectMap1, CaseData.class)).thenReturn(caseData1);
            when(objectMapper.convertValue(Mockito.<RespondentAllegationsOfHarmData>any(),
                    Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(allegationsOfHarmDataMap);

            when(confidentialDetailsMapper.mapConfidentialData(
                    Mockito.any(CaseData.class),
                    Mockito.anyBoolean()
            )).thenReturn(caseData);

            when(applicationsTabService.getRespondentsTable(caseData1)).thenReturn(List.of(Element.<Respondent>builder().build()));
            when(organisationService.getOrganisationDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(
                    Organisations.builder().contactInformation(List.of(ContactInformation.builder().build())).build());
            when(objectMapper.convertValue(Mockito.<RespondentAllegationsOfHarmData>any(),
                    Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(allegationsOfHarmDataMap);

            when(systemUserService.getSysUserToken()).thenReturn("");

            CallbackRequest callbackRequest1 = uk.gov.hmcts.reform.ccd.client.model
                    .CallbackRequest.builder()
                    .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                            .id(123L)
                            .data(stringObjectMap1)
                            .build())
                    .eventId("SolicitorA")
                    .build();

            callbackRequest1.setEventId("c100ResSolMiamA");
            Map<String, Object> response = respondentSolicitorService.populateAboutToSubmitCaseData(
                    callbackRequest1
            );

            assertTrue(response.containsKey("respondents"));
        }
    }

    @Test
    public void populateAboutToSubmitCaseDataForC100ResSolKeepDetailsPrivateAWithConfidentialityIsNoTest() {

        respondent = PartyDetails.builder()
                .response(Response.builder()
                        .keepDetailsPrivate(KeepDetailsPrivate
                                .builder()
                                .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                .confidentiality(Yes)
                                .build())
                        .build())
                .build();

        when(responseSubmitChecker.isFinished(respondent, true)).thenReturn(mandatoryFinished);

        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder()
                .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                .value(respondent).build();
        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondents);

        CaseData caseData1 = CaseData.builder().respondents(respondentList).id(1)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .respondents(respondentList)
                .respondentSolicitorData(RespondentSolicitorData.builder()
                        .respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .keepContactDetailsPrivate(KeepDetailsPrivate.builder()
                                .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                .confidentiality(No)
                                .build())
                                             .hasRespondentAttendedMiam(No)
                                             .respondentWillingToAttendMiam(No)
                                             .respondentReasonNotAttendingMiam("test")
                        .build())
                .build();


        Map<String, Object> stringObjectMap1 = caseData1.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap1, CaseData.class)).thenReturn(caseData1);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap1)
                        .build())
                .eventId("SolicitorA")
                .build();
        when(confidentialDetailsMapper.mapConfidentialData(
                Mockito.any(CaseData.class),
                Mockito.anyBoolean()
        )).thenReturn(caseData1);
        when(objectMapper.convertValue(Mockito.<RespondentAllegationsOfHarmData>any(),
                Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(allegationsOfHarmDataMap);
        when(applicationsTabService.getRespondentsTable(caseData1)).thenReturn(List.of(Element.<Respondent>builder().build()));
        when(organisationService.getOrganisationDetails(Mockito.anyString(), Mockito.anyString())).thenReturn(
                Organisations.builder().contactInformation(List.of(ContactInformation.builder().build())).build());
        when(systemUserService.getSysUserToken()).thenReturn("");
        when(objectMapper.convertValue(Mockito.<RespondentAllegationsOfHarmData>any(),
                Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(allegationsOfHarmDataMap);


        String[] events = {"c100ResSolKeepDetailsPrivateA"};
        for (String event : events) {
            callbackRequest.setEventId(event);
            Map<String, Object> response = respondentSolicitorService.populateAboutToSubmitCaseData(
                    callbackRequest
            );

            assertTrue(response.containsKey("respondents"));
        }
    }

    @Test
    public void populateAboutToStartCaseDataConsentToApplicationEvent() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        String[] events = {"c100ResSolConsentingToApplicationA", "c100ResSolKeepDetailsPrivate", "c100ResSolConfirmOrEditContactDetails",
            "c100ResSolAttendingTheCourt", "c100ResSolMiamA", "c100ResSolCurrentOrPreviousProceedings", "c100ResSolAllegationsOfHarm",
            "c100ResSolInternationalElement", "c100ResSolLitigationCapacity", Optional.empty().toString()};
        for (String event : events) {
            CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                    .CallbackRequest.builder()
                    .eventId(event)
                    .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                            .id(123L)
                            .data(stringObjectMap)
                            .build())
                    .build();

            Map<String, Object> response = respondentSolicitorService.populateAboutToStartCaseData(
                    callbackRequest
            );

            assertTrue(response.containsKey("respondents"));
        }
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
                .documentFileName("Draft_C7_response.pdf")
                .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(documentGenService.generateSingleDocument(
                authToken,
                caseData,
                SOLICITOR_C7_DRAFT_DOCUMENT,
                false
        )).thenReturn(document);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(false).build();
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(documentLanguage);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .eventId("c100ResSolViewResponseDraftDocumentA")
                .build();

        Map<String, Object> response = respondentSolicitorService.generateDraftDocumentsForRespondent(
                callbackRequest, authToken
        );

        assertTrue(response.containsKey("draftC7ResponseDoc"));

        stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(responseSubmitChecker.isFinished(respondent, true)).thenReturn(true);
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
                .url("TestUrl")
                .binaryUrl("binaryUrl")
                .hashToken("testHashToken")
                .build();
        document = Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("Draft_C1A_allegation_of_harm.pdf")
                .build();
        when(documentGenService.generateSingleDocument(
                authToken,
                caseData,
                SOLICITOR_C1A_DRAFT_DOCUMENT,
                false
        )).thenReturn(document);

        callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .eventId("c100ResSolViewResponseDraftDocumentA")
                .build();

        response = respondentSolicitorService.generateDraftDocumentsForRespondent(
                callbackRequest, authToken
        );

        assertTrue(response.containsKey("draftC7ResponseDoc"));

        assertTrue(response.containsKey("draftC1ADoc"));
    }

    @Test
    public void testC7DraftDocumentNoDxNumber() throws Exception {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
                .url("TestUrl")
                .binaryUrl("binaryUrl")
                .hashToken("testHashToken")
                .build();

        Document document = Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("Draft_C7_response.pdf")
                .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(documentGenService.generateSingleDocument(
                authToken,
                caseData,
                SOLICITOR_C7_DRAFT_DOCUMENT,
                false
        )).thenReturn(document);

        caseData = caseData.toBuilder()
                .respondentSolicitorData((RespondentSolicitorData.builder()
                        .respondentAllegationsOfHarmData(allegationsOfHarmData).build()))
                .build();

        stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(responseSubmitChecker.isFinished(respondent, true)).thenReturn(true);
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
                .url("TestUrl")
                .binaryUrl("binaryUrl")
                .hashToken("testHashToken")
                .build();
        document = Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("Draft_C1A_allegation_of_harm.pdf")
                .build();
        when(documentGenService.generateSingleDocument(
                authToken,
                caseData,
                SOLICITOR_C1A_DRAFT_DOCUMENT,
                false
        )).thenReturn(document);

        DocumentLanguage documentLanguage = DocumentLanguage.builder().isGenEng(true).isGenWelsh(false).build();
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(documentLanguage);

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .eventId("c100ResSolViewResponseDraftDocumentB")
                .build();

        Map<String, Object> response = respondentSolicitorService.generateDraftDocumentsForRespondent(
                callbackRequest, authToken
        );

        assertTrue(response.containsKey("draftC7ResponseDoc"));
        assertTrue(response.containsKey("draftC1ADoc"));
    }

    @Test
    public void testPopulateDraftDocumentForElseConditions() throws Exception {

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
                .url("TestUrl")
                .binaryUrl("binaryUrl")
                .hashToken("testHashToken")
                .build();

        Document document = Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("Draft_C7_response.pdf")
                .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(documentGenService.generateSingleDocument(
                authToken,
                caseData,
                SOLICITOR_C7_DRAFT_DOCUMENT,
                false
        )).thenReturn(document);

        respondent.setIsPhoneNumberConfidential(No);
        respondent.setIsEmailAddressConfidential(No);
        respondent.setIsAddressConfidential(No);
        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder()
                .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                .value(respondent).build();
        Element<PartyDetails> wrappedRespondents2 = Element.<PartyDetails>builder()
                .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                .value(respondent2).build();
        caseData = caseData.toBuilder()
                .respondentSolicitorData(RespondentSolicitorData.builder()
                        .respondentAllegationsOfHarmData(allegationsOfHarmData).build())
                .respondents(List.of(wrappedRespondents, wrappedRespondents2))
                .build();

        stringObjectMap = caseData.toMap(new ObjectMapper());


        when(responseSubmitChecker.isFinished(respondent, true)).thenReturn(true);
        generatedDocumentInfo = GeneratedDocumentInfo.builder()
                .url("TestUrl")
                .binaryUrl("binaryUrl")
                .hashToken("testHashToken")
                .build();
        document = Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName("Draft_C1A_allegation_of_harm.pdf")
                .build();
        when(documentGenService.generateSingleDocument(
                authToken,
                caseData,
                SOLICITOR_C1A_DRAFT_DOCUMENT,
                false
        )).thenReturn(document);

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        Map<String,Object> response1 = populateDataMap(stringObjectMap);
        assertTrue(response1.containsKey("respondent"));
        assertTrue(response1.containsKey("email"));
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData2);
        Map<String,Object> response2 = populateDataMap(stringObjectMap);
        assertTrue(response2.containsKey("respondent"));
        assertTrue(response2.containsKey("email"));

    }

    private Map<String, Object> populateDataMap(Map<String, Object> stringObjectMap) {
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .eventId("c100ResSolViewResponseDraftDocumentB")
                .build();

        return respondentSolicitorService.populateDataMap(callbackRequest, null);


    }

    @Test
    public void testGetOrganisationAddressException() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        when(responseSubmitChecker.isFinished(respondent, true)).thenReturn(true);
        when(organisationService.getOrganisationDetails(Mockito.anyString(), Mockito.anyString())).thenThrow(new RuntimeException());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData2);
        Map<String,Object> response2 = populateDataMap(stringObjectMap);
        assertTrue(response2.containsKey("respondent"));
        assertTrue(response2.containsKey("email"));

    }

    @Test
    public void testGenerateConfidentialMapWithAllConfValues() {

        Map<String, Object> response = respondentSolicitorService.generateConfidentialityDynamicSelectionDisplay(
                callbackRequest
        );

        assertTrue(response.containsKey("confidentialListDetails"));

    }

    @Test
    public void testGenerateConfidentialMapWithoutAllConfValues() {

        Map<String, Object> response = respondentSolicitorService.generateConfidentialityDynamicSelectionDisplay(
                callbackRequest
        );

        assertTrue(response.containsKey("confidentialListDetails"));

    }

    @Test
    public void testsubmittedC7Response() {
        SubmittedCallbackResponse response = respondentSolicitorService.submittedC7Response(
                caseData
        );
        Assertions.assertNotNull(response);
    }

    @Test
    public void testsubmittedC7ResponseWithCourtName() {
        caseData = caseData.toBuilder().courtName("test").build();
        SubmittedCallbackResponse response = respondentSolicitorService.submittedC7Response(
                caseData
        );
        Assertions.assertNotNull(response);
    }

    @Test
    public void testGetSolicitorRoleException() {
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .eventId("")
                .build();
        assertExpectedException(() -> {
            respondentSolicitorService.getSolicitorRole(
                    callbackRequest
            );
        }, RespondentSolicitorException.class, TECH_ERROR);
    }

    @Test
    public void testFindSolicitorRepresentedRespondentsException() {
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        respondent.setResponse(Response.builder().c7ResponseSubmitted(Yes).build());
        respondent2.setResponse(Response.builder().c7ResponseSubmitted(Yes).build());

        caseData = caseData.toBuilder()
                .respondentSolicitorData(RespondentSolicitorData.builder().respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .build())
                .build();

        stringObjectMap = caseData.toMap(new ObjectMapper());
        when(responseSubmitChecker.isFinished(respondent, true)).thenReturn(true);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        assertExpectedException(() -> {
            respondentSolicitorService.findSolicitorRepresentedRespondents(
                    callbackRequest,SolicitorRole.C100APPLICANTSOLICITOR1
            );
        }, RespondentSolicitorException.class, RESPONSE_ALREADY_SUBMITTED_ERROR);
    }

    @Test
    public void populateAboutToSubmitCaseDataSolResponseToAllegationOfHarmYesTest() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(Mockito.<RespondentAllegationsOfHarmData>any(),
                Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(allegationsOfHarmDataMap);
        List<String> errorList = new ArrayList<>();
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .eventId("c100ResSolResponseToAllegationsOfHarmA")
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                        .id(123L)
                        .data(stringObjectMap)
                        .build())
                .build();

        Map<String, Object> response = respondentSolicitorService.populateAboutToSubmitCaseData(callbackRequest);

        assertTrue(response.containsKey("respondents"));
    }

    @Test
    public void populateAboutToSubmitCaseDataSolResponseToAllegationOfHarmNoTest() {

        Element<PartyDetails> wrappedRespondents1 = Element.<PartyDetails>builder()
            .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
            .value(respondent2).build();
        List<Element<PartyDetails>> respondentList = new ArrayList<>();
        respondentList.add(wrappedRespondents1);

        CaseData caseData = CaseData.builder().respondents(respondentList).id(1)
            .caseTypeOfApplication(C100_CASE_TYPE)
            .respondentSolicitorData(RespondentSolicitorData.builder()
                                         .respondentAllegationsOfHarmData(allegationsOfHarmData)
                                         .responseToAllegationsOfHarm(responseToAllegationsOfHarm2)
                                         .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(objectMapper.convertValue(Mockito.<RespondentAllegationsOfHarmData>any(),
                                       Mockito.<TypeReference<Map<String, Object>>>any())).thenReturn(allegationsOfHarmDataMap);
        List<String> errorList = new ArrayList<>();
        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .eventId("c100ResSolResponseToAllegationsOfHarmA")
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Map<String, Object> response = respondentSolicitorService.populateAboutToSubmitCaseData(callbackRequest);

        assertTrue(response.containsKey("respondents"));
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }



    @Test
    public void testPopulateDataMapNoSolOrg() {
        Map<String,Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest1 = CallbackRequest.builder()
                .caseDetails(CaseDetails.builder().data(objectMap).build())
                .build();
        respondent2 = PartyDetails.builder()
                .user(User.builder().build())
                .representativeFirstName("Abc")
                .representativeLastName("Xyz")
                .gender(Gender.male)
                .email("abc@xyz.com")
                .sendSignUpLink("test")
                .phoneNumber("1234567890")
                .response(Response.builder()
                    .currentOrPastProceedingsForChildren(YesNoDontKnow.yes)
                        .citizenDetails(CitizenDetails.builder()
                                .firstName("test")
                                .lastName("test")
                                .build())
                        .consent(Consent.builder()
                                .consentToTheApplication(No)
                                .noConsentReason("test")
                                .permissionFromCourt(Yes)
                                .build())
                        .c7ResponseSubmitted(No)
                        .keepDetailsPrivate(KeepDetailsPrivate
                                .builder()
                                .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                .confidentiality(Yes)
                                .build())
                        .miam(Miam.builder().attendedMiam(No)
                                .willingToAttendMiam(No)
                                .reasonNotAttendingMiam("test").build())
                        .citizenInternationalElements(CitizenInternationalElements
                                .builder()
                                .childrenLiveOutsideOfEnWl(Yes)
                                .childrenLiveOutsideOfEnWlDetails("Test")
                                .parentsAnyOneLiveOutsideEnWl(Yes)
                                .parentsAnyOneLiveOutsideEnWlDetails("Test")
                                .anotherPersonOrderOutsideEnWl(Yes)
                                .anotherPersonOrderOutsideEnWlDetails("test")
                                .anotherCountryAskedInformation(Yes)
                                .anotherCountryAskedInformationDetaails("test")
                                .build())
                        .respondentAllegationsOfHarmData(allegationsOfHarmData)
                        .supportYouNeed(ReasonableAdjustmentsSupport.builder()
                                .reasonableAdjustments(List.of(ReasonableAdjustmentsEnum.nosupport)).build())
                        .build())
                .canYouProvideEmailAddress(Yes)
                .isEmailAddressConfidential(Yes)
                .isPhoneNumberConfidential(Yes)
                .isAddressConfidential(Yes)
                .solicitorOrg(Organisation.builder().build())
                .solicitorAddress(Address.builder().addressLine1("ABC").addressLine2("test").addressLine3("test").postCode(
                        "AB1 2MN").build())
                .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
                .solicitorReference("test")
                .address(Address.builder().addressLine1("").build())
                .build();

        Element<PartyDetails> wrappedRespondents2 = Element.<PartyDetails>builder()
                .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                .value(respondent2).build();
        assertNotNull(respondentSolicitorService.populateDataMap(callbackRequest1, wrappedRespondents2));
    }

    @Test
    public void testC1ADocumentQuarantine() throws Exception {
        quarantineLegalDoc = quarantineLegalDoc.toBuilder()
            .document(document)
            .restrictedDetails("test details")
            .uploaderRole(LEGAL_PROFESSIONAL)
            .build();

        when(documentGenService.generateSingleDocument(
            Mockito.anyString(),
            Mockito.any(CaseData.class),
            Mockito.anyString(),
            Mockito.anyBoolean(),
            Mockito.any(HashMap.class)
        )).thenReturn(document);


        String[] events = {"c100ResSolAllegationsOfHarmA", "c100ResSolAllegationsOfHarmB",
            "c100ResSolAllegationsOfHarmC", "c100ResSolAllegationsOfHarmD"};

        for (String event : events) {

            Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder()
                .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                .value(respondent).build();
            Element<PartyDetails> wrappedRespondents2 = Element.<PartyDetails>builder()
                .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
                .value(respondent2).build();
            List<Element<PartyDetails>> respondentList = new ArrayList<>();
            respondentList.add(wrappedRespondents);
            respondentList.add(wrappedRespondents2);
            respondentList.add(wrappedRespondents);
            respondentList.add(wrappedRespondents2);

            CaseData caseData = CaseData.builder().respondents(respondentList).id(1)
                .caseTypeOfApplication(C100_CASE_TYPE)
                .documentManagementDetails(
                    DocumentManagementDetails.builder()
                        .tempQuarantineDocumentList(List.of(element(UUID.fromString("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355"),quarantineLegalDoc)))
                        .build()
                ).reviewDocuments(ReviewDocuments.builder()
                                     .reviewDocsDynamicList(DynamicList.builder().value(
                                         DynamicListElement.builder()
                                             .code("33dff5a7-3b6f-45f1-b5e7-5f9be1ede355").build()
                                     ).build()).build())
                .respondentSolicitorData(RespondentSolicitorData.builder()
                                             .respondentAllegationsOfHarmData(allegationsOfHarmData)
                                             .build())
                .build();

            Map<String, Object> stringObjectMap = new HashMap<>();
            reviewDocumentService.getReviewedDocumentDetailsNew(caseData, stringObjectMap);

            when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

            CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
                .CallbackRequest.builder()
                .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                                 .id(123L)
                                 .data(stringObjectMap)
                                 .build())
                .build();
            callbackRequest.setEventId(event);


            doCallRealMethod().when(manageDocumentsService).moveDocumentsToRespectiveCategoriesNew(any(), any(), any(), any(), any());
            doCallRealMethod().when(manageDocumentsService).getRestrictedOrConfidentialKey(any());
            doCallRealMethod().when(manageDocumentsService).getQuarantineDocumentForUploader(any(),any());
            doCallRealMethod().when(manageDocumentsService).moveToConfidentialOrRestricted(any(),any(),any(),any());
            doCallRealMethod().when(manageDocumentsService).moveDocumentsToQuarantineTab(any(),any(),any(),any());

            RespChildAbuseBehaviour respChildAbuseBehaviour = RespChildAbuseBehaviour.builder().typeOfAbuse(
                ChildAbuseEnum.emotionalAbuse.getDisplayedValue()).build();
            List<Element<RespChildAbuseBehaviour>> childAbuseBehaviourList = new ArrayList<>();
            childAbuseBehaviourList.add(element(respChildAbuseBehaviour));

            when(respondentAllegationOfHarmService.updateChildAbusesForDocmosis(any())).thenReturn(childAbuseBehaviourList);
            UserDetails userDetails = UserDetails.builder().forename("test")
                .roles(Arrays.asList("caseworker-privatelaw-solicitor")).build();
            when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);
            Map<String, Object> response = respondentSolicitorService.submitC7ResponseForActiveRespondent(
                authToken, callbackRequest
            );

            List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsList
                = (List<Element<QuarantineLegalDoc>>) response.get("legalProfQuarantineDocsList");
            assertNotNull(legalProfQuarantineDocsList);
            assertEquals(1,legalProfQuarantineDocsList.size());

        }
    }

    @Test
    public void testPopulateDataMapNoSolOrgForVersion() {
        Map<String,Object> objectMap = new HashMap<>();
        CallbackRequest callbackRequest1 = CallbackRequest.builder()
            .caseDetails(CaseDetails.builder().data(objectMap).build())
            .build();
        respondent2 = PartyDetails.builder()
            .user(User.builder().build())
            .representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .sendSignUpLink("test")
            .phoneNumber("1234567890")
            .response(Response.builder()
                          .currentOrPastProceedingsForChildren(YesNoDontKnow.yes)
                          .citizenDetails(CitizenDetails.builder()
                                              .firstName("test")
                                              .lastName("test")
                                              .build())
                          .consent(Consent.builder()
                                       .consentToTheApplication(No)
                                       .noConsentReason("test")
                                       .permissionFromCourt(Yes)
                                       .build())
                          .c7ResponseSubmitted(No)
                          .keepDetailsPrivate(KeepDetailsPrivate
                                                  .builder()
                                                  .otherPeopleKnowYourContactDetails(YesNoIDontKnow.yes)
                                                  .confidentiality(Yes)
                                                  .build())
                          .miam(Miam.builder().attendedMiam(No)
                                    .willingToAttendMiam(No)
                                    .reasonNotAttendingMiam("test").build())
                          .citizenInternationalElements(CitizenInternationalElements
                                                            .builder()
                                                            .childrenLiveOutsideOfEnWl(Yes)
                                                            .childrenLiveOutsideOfEnWlDetails("Test")
                                                            .parentsAnyOneLiveOutsideEnWl(Yes)
                                                            .parentsAnyOneLiveOutsideEnWlDetails("Test")
                                                            .anotherPersonOrderOutsideEnWl(Yes)
                                                            .anotherPersonOrderOutsideEnWlDetails("test")
                                                            .anotherCountryAskedInformation(Yes)
                                                            .anotherCountryAskedInformationDetaails("test")
                                                            .build())
                          .respondentAllegationsOfHarmData(allegationsOfHarmData)
                          .supportYouNeed(ReasonableAdjustmentsSupport.builder()
                                              .reasonableAdjustments(List.of(ReasonableAdjustmentsEnum.nosupport)).build())
                          .build())
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .isAddressConfidential(Yes)
            .solicitorOrg(Organisation.builder().build())
            .solicitorAddress(Address.builder().addressLine1("ABC").addressLine2("test").addressLine3("test").postCode(
                "AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .solicitorReference("test")
            .address(Address.builder().addressLine1("").build())
            .build();

        Element<PartyDetails> wrappedRespondents2 = Element.<PartyDetails>builder()
            .id(UUID.fromString("1afdfa01-8280-4e2c-b810-ab7cf741988a"))
            .value(respondent2).build();
        assertNotNull(respondentSolicitorService.populateDataMap(callbackRequest, wrappedRespondents2));
    }
}
