package uk.gov.hmcts.reform.prl.services.serviceofdocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.EventRequestData;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesNoNotApplicable;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.AdditionalRecipients;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.ServiceOfDocumentsCheckEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.SodCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.SodSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.DocumentsDynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.ServeOrgDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofdocuments.SodPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ServiceOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.notify.serviceofapplication.EmailNotificationDetails;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.models.serviceofdocuments.ServiceOfDocuments;
import uk.gov.hmcts.reform.prl.services.BulkPrintService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.SendAndReplyService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationEmailService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationPostService;
import uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.dynamicmultiselectlist.DynamicMultiSelectListService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_OTHER_PEOPLE_PRESENT_IN_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_RECIPIENT_OPTIONS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TEST_UUID;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_AUTHORIZATION;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_CASE_ID;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServiceOfDocumentsServiceTest {

    @InjectMocks
    ServiceOfDocumentsService serviceOfDocumentsService;

    private SodPack sodPack;

    private ServiceOfDocuments serviceOfDocuments;

    private CaseData caseData;

    private CallbackRequest callbackRequest;

    private CaseDetails caseDetails;

    private PartyDetails partyDetails;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ServiceOfApplicationService serviceOfApplicationService;

    @Mock
    private SendAndReplyService sendAndReplyService;

    @Mock
    private DynamicMultiSelectListService dynamicMultiSelectListService;

    @Mock
    private UserService userService;

    @Mock
    private AllTabServiceImpl allTabService;

    @Mock
    private DocumentLanguageService documentLanguageService;

    @Mock
    private ServiceOfApplicationEmailService serviceOfApplicationEmailService;

    @Mock
    private ServiceOfApplicationPostService serviceOfApplicationPostService;

    @Mock
    private BulkPrintService bulkPrintService;

    @Before
    public void setUp() {

        List<Element<Document>> documents = new ArrayList<>();
        Document document = Document.builder()
            .documentUrl("testUrl")
            .documentBinaryUrl("testUrl")
            .documentFileName("testFile.pdf")
            .build();
        documents.add(element(document));
        sodPack = SodPack.builder()
            .documents(documents)
            .build();
        DocumentsDynamicList documentsDynamicList = DocumentsDynamicList.builder().documentsList(DynamicList.builder()
                                                                                                     .value(DynamicListElement.EMPTY)
                                                                                                     .listItems(List.of(DynamicListElement.EMPTY))
                                                                                                     .build()).build();
        serviceOfDocuments = ServiceOfDocuments.builder()
            .sodServeToRespondentOptions(YesNoNotApplicable.Yes)
            .sodDocumentsList(List.of(element(documentsDynamicList)))
            .sodAdditionalDocumentsList(List.of(element(document)))
            .build();
        partyDetails = PartyDetails.builder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .firstName("test")
            .lastName("test")
            .email("test@hmcts.com")
            .solicitorEmail("solicitor@hmcts.net")
            .address(Address.builder().addressLine1("addressLine1").postCode("postcode").build())
            .build();
        caseData = CaseData.builder()
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .id(Long.parseLong(TEST_CASE_ID))
            .applicants(List.of(element(UUID.fromString(TEST_UUID), partyDetails)))
            .respondents(List.of(element(UUID.fromString(TEST_UUID),partyDetails)))
            .othersToNotify(List.of(element(UUID.fromString(TEST_UUID),partyDetails)))
            .build();

        caseDetails = CaseDetails.builder()
            .id(Long.valueOf(TEST_CASE_ID))
            .data(caseData.toMap(new ObjectMapper()))
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();


    }

    @Test
    public void testHandleAboutToStart() {
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(serviceOfApplicationService.checkIfPostalAddressMissedForRespondentAndOtherParties(any(CaseData.class), anyString())).thenReturn(
            EMPTY_STRING);
        List<DynamicMultiselectListElement> otherPeopleList = List.of(DynamicMultiselectListElement.builder()
                                                                          .label("otherPeople")
                                                                          .code("otherPeople")
                                                                          .build());
        when(dynamicMultiSelectListService.getOtherPeopleMultiSelectList(any(CaseData.class))).thenReturn(otherPeopleList);
        List<DynamicMultiselectListElement> applicantRespondentList = List.of(DynamicMultiselectListElement.builder()
                                                                                  .label("applicant")
                                                                                  .code("applicant")
                                                                                  .build(),
                                                                              DynamicMultiselectListElement.builder()
                                                                                  .label("respondent")
                                                                                  .code("respondent")
                                                                                  .build()
                                                                              );
        DynamicMultiSelectList combinedRecipients = DynamicMultiSelectList.builder()
            .listItems(applicantRespondentList)
            .build();
        when(serviceOfApplicationService.getCombinedRecipients(any(CaseData.class))).thenReturn(combinedRecipients);
        List<DynamicListElement> categoriesAndDocuments = new ArrayList<>();
        categoriesAndDocuments.add(DynamicListElement.builder().label(EMPTY_STRING).build());
        when(sendAndReplyService.getCategoriesAndDocuments(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(DynamicList.builder().listItems(categoriesAndDocuments).build());

        Map<String, Object> response = serviceOfDocumentsService.handleAboutToStart(TEST_AUTHORIZATION, callbackRequest, "en");
        assertNotNull(response);
        assertEquals(YesOrNo.Yes, response.get(SOA_OTHER_PEOPLE_PRESENT_IN_CASE));
        assertEquals(combinedRecipients, response.get(SOA_RECIPIENT_OPTIONS));
    }

    /*@Test
    public void testHandleAboutToStartWithSodUnServedPack() {
        serviceOfDocuments = serviceOfDocuments.toBuilder()
            .sodUnServedPack(sodPack)
            .build();
        caseData = caseData.toBuilder()
            .serviceOfDocuments(serviceOfDocuments)
            .build();
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        Map<String, Object> response = serviceOfDocumentsService.handleAboutToStart(TEST_AUTHORIZATION, callbackRequest);
        assertNotNull(response);
        assertEquals(List.of("Can not execute service of documents, there are unserved document(s) pending review"), response.get("errors"));
    }*/

    @Test
    public void testHandleAboutToSubmitC100CasePersonalServiceForApplicantSolicitor() {
        serviceOfDocuments = serviceOfDocuments.toBuilder()
            .sodServeToRespondentOptions(YesNoNotApplicable.Yes)
            .sodSolicitorServingRespondentsOptions(SodSolicitorServingRespondentsEnum.applicantLegalRepresentative)
            .build();
        DynamicMultiSelectList soaOtherParties = DynamicMultiSelectList.builder().value(List.of(DynamicMultiselectListElement.builder().code(
            UUID.randomUUID().toString()).label("Other person").build())).build();
        caseData = caseData.toBuilder()
            .serviceOfDocuments(serviceOfDocuments)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaOtherParties(soaOtherParties)
                                      .build())
            .build();
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder().surname("admin").forename("test").build());
        Map<String, Object> response = serviceOfDocumentsService.handleAboutToSubmit(TEST_AUTHORIZATION, callbackRequest);
        assertNotNull(response);
    }

    @Test
    public void testHandleAboutToSubmitC100CasePersonalServiceForApplicantLip() {
        partyDetails = partyDetails.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .solicitorEmail(null)
            .build();
        serviceOfDocuments = serviceOfDocuments.toBuilder()
            .sodServeToRespondentOptions(YesNoNotApplicable.Yes)
            .sodCitizenServingRespondentsOptions(SodCitizenServingRespondentsEnum.unrepresentedApplicant)
            .build();

        caseData = caseData.toBuilder()
            .serviceOfDocuments(serviceOfDocuments)
            .applicants(List.of(element(partyDetails)))
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .build())
            .build();
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder().surname("admin").forename("test").build());
        Map<String, Object> response = serviceOfDocumentsService.handleAboutToSubmit(TEST_AUTHORIZATION, callbackRequest);
        assertNotNull(response);
    }

    @Test
    public void testHandleAboutToSubmitC100CaseNonPersonalService() {
        serviceOfDocuments = serviceOfDocuments.toBuilder()
            .sodServeToRespondentOptions(YesNoNotApplicable.No)
            .sodAdditionalRecipients(List.of(AdditionalRecipients.additionalRecipients))
            .sodAdditionalRecipientsList(List.of(element(ServeOrgDetails.builder().build())))
            .build();
        caseData = caseData.toBuilder()
            .serviceOfDocuments(serviceOfDocuments)
            .serviceOfApplication(ServiceOfApplication.builder()
                                      .soaRecipientsOptions(DynamicMultiSelectList.builder().value(List.of(DynamicMultiselectListElement.builder()
                                                                                                               .code(TEST_UUID)
                                                                                                               .build())).build())
                                      .build())
            .build();
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(userService.getUserDetails(anyString())).thenReturn(UserDetails.builder().surname("admin").forename("test").build());
        Map<String, Object> response = serviceOfDocumentsService.handleAboutToSubmit(TEST_AUTHORIZATION, callbackRequest);
        assertNotNull(response);
    }

    @Test
    public void testHandleSubmittedPersonalServiceForApplicantLip() {
        partyDetails = partyDetails.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .solicitorEmail(null)
            .build();

        sodPack = sodPack.toBuilder()
            .isPersonalService(YesOrNo.Yes)
            .servedBy(SodCitizenServingRespondentsEnum.unrepresentedApplicant
                          .getDisplayedValue())
            .build();
        serviceOfDocuments = serviceOfDocuments.toBuilder()
            .sodUnServedPack(sodPack)
            .sodDocumentsCheckOptions(ServiceOfDocumentsCheckEnum.noCheck)
            .build();

        caseData = caseData.toBuilder()
            .serviceOfDocuments(serviceOfDocuments)
            .build();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            TEST_AUTHORIZATION,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(new ObjectMapper()),
            caseData,
            null
        );
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(DocumentLanguage
                                                                               .builder()
                                                                               .isGenEng(true)
                                                                               .isGenWelsh(true)
                                                                               .build());
        when(serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(Mockito.anyString(),Mockito.anyString(),
                                                                                    Mockito.any(),Mockito.any(),Mockito.any(),
                                                                                    Mockito.anyString()))
            .thenReturn(EmailNotificationDetails.builder().build());
        ResponseEntity<SubmittedCallbackResponse> response = serviceOfDocumentsService.handleSubmitted(TEST_AUTHORIZATION, callbackRequest);
        assertNotNull(response);

    }

    @Test
    public void testHandleSubmittedPersonalServiceForApplicantSolicitor() {
        sodPack = sodPack.toBuilder()
            .isPersonalService(YesOrNo.Yes)
            .servedBy(SodSolicitorServingRespondentsEnum.applicantLegalRepresentative
                          .getDisplayedValue())
            .build();
        serviceOfDocuments = serviceOfDocuments.toBuilder()
            .sodUnServedPack(sodPack)
            .sodDocumentsCheckOptions(ServiceOfDocumentsCheckEnum.noCheck)
            .build();

        caseData = caseData.toBuilder()
            .serviceOfDocuments(serviceOfDocuments)
            .build();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            TEST_AUTHORIZATION,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(new ObjectMapper()),
            caseData,
            null
        );
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(DocumentLanguage
                                                                               .builder()
                                                                               .isGenEng(true)
                                                                               .isGenWelsh(true)
                                                                               .build());
        when(serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(Mockito.anyString(),Mockito.anyString(),
                                                                                    Mockito.any(),Mockito.any(),Mockito.any(),
                                                                                    Mockito.anyString()))
            .thenReturn(EmailNotificationDetails.builder().build());
        ResponseEntity<SubmittedCallbackResponse> response = serviceOfDocumentsService.handleSubmitted(TEST_AUTHORIZATION, callbackRequest);
        assertNotNull(response);

    }

    @Test
    public void testHandleSubmittedNonPersonalServiceForApplicantSolicitor() {
        sodPack = sodPack.toBuilder()
            .isPersonalService(YesOrNo.No)
            .applicantIds(List.of(element(TEST_UUID)))
            .respondentIds(List.of(element(TEST_UUID)))
            .build();
        serviceOfDocuments = serviceOfDocuments.toBuilder()
            .sodUnServedPack(sodPack)
            .sodDocumentsCheckOptions(ServiceOfDocumentsCheckEnum.noCheck)
            .build();

        caseData = caseData.toBuilder()
            .serviceOfDocuments(serviceOfDocuments)
            .build();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            TEST_AUTHORIZATION,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(new ObjectMapper()),
            caseData,
            null
        );
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(DocumentLanguage
                                                                               .builder()
                                                                               .isGenEng(true)
                                                                               .isGenWelsh(true)
                                                                               .build());
        when(serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(Mockito.anyString(),Mockito.anyString(),
                                                                                    Mockito.any(),Mockito.any(),Mockito.any(),
                                                                                    Mockito.anyString()))
            .thenReturn(EmailNotificationDetails.builder().build());
        ResponseEntity<SubmittedCallbackResponse> response = serviceOfDocumentsService.handleSubmitted(TEST_AUTHORIZATION, callbackRequest);
        assertNotNull(response);

    }

    @Test
    public void testHandleSubmittedNonPersonalServiceForLip() throws Exception {
        partyDetails = partyDetails.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .solicitorEmail(null)
            .build();

        sodPack = sodPack.toBuilder()
            .isPersonalService(YesOrNo.No)
            .applicantIds(List.of(element(TEST_UUID)))
            .respondentIds(List.of(element(TEST_UUID)))
            .build();
        serviceOfDocuments = serviceOfDocuments.toBuilder()
            .sodUnServedPack(sodPack)
            .sodDocumentsCheckOptions(ServiceOfDocumentsCheckEnum.noCheck)
            .build();

        caseData = caseData.toBuilder()
            .serviceOfDocuments(serviceOfDocuments)
            .applicants(List.of(element(UUID.fromString(TEST_UUID), partyDetails)))
            .respondents(List.of(element(UUID.fromString(TEST_UUID),partyDetails)))
            .build();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            TEST_AUTHORIZATION,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(new ObjectMapper()),
            caseData,
            null
        );
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(DocumentLanguage
                                                                               .builder()
                                                                               .isGenEng(true)
                                                                               .isGenWelsh(true)
                                                                               .build());
        when(serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(Mockito.anyString(),Mockito.anyString(),
                                                                                    Mockito.any(),Mockito.any(),Mockito.any(),
                                                                                    Mockito.anyString()))
            .thenReturn(EmailNotificationDetails.builder().build());
        when(serviceOfApplicationPostService.getCoverSheets(
            any(CaseData.class),
            anyString(),
            any(Address.class),
            anyString(),
            anyString()
        )).thenReturn(
            List.of(Document.builder().build()));
        when(bulkPrintService.send(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(UUID.randomUUID());
        ResponseEntity<SubmittedCallbackResponse> response = serviceOfDocumentsService.handleSubmitted(TEST_AUTHORIZATION, callbackRequest);
        assertNotNull(response);

    }

    @Test
    public void testHandleSubmittedNonPersonalServiceForLipContactPreferenceEmail() {
        partyDetails = partyDetails.toBuilder()
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
            .solicitorEmail(null)
            .contactPreferences(ContactPreferences.email)
            .build();

        sodPack = sodPack.toBuilder()
            .isPersonalService(YesOrNo.No)
            .applicantIds(List.of(element(TEST_UUID)))
            .respondentIds(List.of(element(TEST_UUID)))
            .otherPersonIds(List.of(element(TEST_UUID)))
            .additionalRecipients(List.of(element(ServeOrgDetails.builder()
                                                      .serveByPostOrEmail(DeliveryByEnum.email)
                                                      .emailInformation(EmailInformation.builder().emailAddress("test@hmcts.net").build())
                                                      .build()),
                                          element(ServeOrgDetails.builder()
                                                      .serveByPostOrEmail(DeliveryByEnum.post)
                                                      .postalInformation(PostalInformation.builder().postalAddress(
                                                          Address.builder().addressLine1("addressLine1").build()).build())
                                                      .build())))
            .build();
        serviceOfDocuments = serviceOfDocuments.toBuilder()
            .sodUnServedPack(sodPack)
            .sodDocumentsCheckOptions(ServiceOfDocumentsCheckEnum.noCheck)
            .build();

        caseData = caseData.toBuilder()
            .serviceOfDocuments(serviceOfDocuments)
            .applicants(List.of(element(UUID.fromString(TEST_UUID), partyDetails)))
            .respondents(List.of(element(UUID.fromString(TEST_UUID),partyDetails)))
            .build();

        StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = new StartAllTabsUpdateDataContent(
            TEST_AUTHORIZATION,
            EventRequestData.builder().build(),
            StartEventResponse.builder().build(),
            caseData.toMap(new ObjectMapper()),
            caseData,
            null
        );
        when(allTabService.getStartAllTabsUpdate(anyString())).thenReturn(startAllTabsUpdateDataContent);
        when(objectMapper.convertValue(anyMap(), eq(CaseData.class))).thenReturn(caseData);
        when(documentLanguageService.docGenerateLang(caseData)).thenReturn(DocumentLanguage
                                                                               .builder()
                                                                               .isGenEng(true)
                                                                               .isGenWelsh(true)
                                                                               .build());
        when(serviceOfApplicationEmailService.sendEmailUsingTemplateWithAttachments(Mockito.anyString(),Mockito.anyString(),
                                                                                    Mockito.any(),Mockito.any(),Mockito.any(),
                                                                                    Mockito.anyString()))
            .thenReturn(EmailNotificationDetails.builder().build());
        ResponseEntity<SubmittedCallbackResponse> response = serviceOfDocumentsService.handleSubmitted(TEST_AUTHORIZATION, callbackRequest);
        assertNotNull(response);

    }

}
