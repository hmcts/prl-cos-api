package uk.gov.hmcts.reform.prl.controllers;

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
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtAddress;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.court.ServiceArea;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CaseWorkerEmailService;
import uk.gov.hmcts.reform.prl.services.ConfidentialityTabService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.FL401SubmitApplicationService;
import uk.gov.hmcts.reform.prl.services.LocationRefDataService;
import uk.gov.hmcts.reform.prl.services.SolicitorEmailService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.DocumentGenService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.services.validators.FL401StatementOfTruthAndSubmitChecker;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_C8_WELSH;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DOCUMENT_FIELD_FINAL_WELSH;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class FL401SubmitApplicationControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private FL401SubmitApplicationController fl401SubmitApplicationController;

    @Mock
    private UserService userService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CourtFinderService courtFinderService;

    @Mock
    private UserDetails userDetails;


    @Mock
    ConfidentialityTabService confidentialityTabService;

    @Mock
    AllTabServiceImpl allTabsService;

    @Mock
    CaseWorkerEmailService caseWorkerEmailService;

    @Mock
    SolicitorEmailService solicitorEmailService;

    @Mock
    FL401StatementOfTruthAndSubmitChecker fl401StatementOfTruthAndSubmitChecker;

    @Mock
    private Court court;

    @Mock
    private CaseDetails caseDetails;

    @Mock
    private CaseData caseData;

    @Mock
    private DocumentGenService documentGenService;

    @Mock
    private LocationRefDataService locationRefDataService;

    @Mock
    private FL401SubmitApplicationService fl401SubmitApplicationService;

    @Mock
    private CourtFinderApi courtFinderApi;

    @Mock
    private AuthorisationService authorisationService;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";

    private TypeOfApplicationOrders orders;
    private LinkToCA linkToCA;

    private static final Map<String, Object> fl401DocsMap = new HashMap<>();
    private DynamicList dynamicList;

    @Before
    public void setUp() {


        CourtEmailAddress courtEmailAddress = CourtEmailAddress.builder()
            .address("brighton.breathingspace@justice.gov.uk")
            .description("Horsham Court")
            .explanation("Family")
            .build();

        court = Court.builder()
            .courtName("testcourt")
            .courtEmailAddresses(Collections.singletonList(courtEmailAddress))
            .build();

        userDetails = UserDetails.builder()
            .email("solicitor@example.com")
            .surname("userLast")
            .build();

        fl401DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_C8, "test");
        fl401DocsMap.put(PrlAppsConstants.DOCUMENT_FIELD_FINAL, "test");
        fl401DocsMap.put(DOCUMENT_FIELD_C8_WELSH, "test");
        fl401DocsMap.put(DOCUMENT_FIELD_FINAL_WELSH, "test");
        Court horshamCourt = Court.builder()
            .courtName("Horsham County Court and Family Court")
            .courtSlug("horsham-county-court-and-family-court")
            .courtEmailAddresses(Collections.singletonList(courtEmailAddress))
            .countyLocationCode(333)
            .dxNumber(Collections.singletonList("The Law Courts"))
            .inPerson(true)
            .accessScheme(true)
            .address(Collections.singletonList(CourtAddress.builder().build()))
            .build();
        dynamicList = DynamicList.builder()
            .value(DynamicListElement.builder().code("reg-base-courtname-postcode-regname-basename").build()).build();
        when(courtFinderApi.findClosestDomesticAbuseCourtByPostCode(Mockito.anyString()))
            .thenReturn(ServiceArea.builder()
                            .courts(Collections.singletonList(horshamCourt))
                            .build());
        when(courtFinderApi.getCourtDetails(Mockito.anyString())).thenReturn(horshamCourt);
        when(courtFinderService.getEmailAddress(horshamCourt)).thenReturn(Optional.of(courtEmailAddress));
        when(locationRefDataService.getCourtDetailsFromEpimmsId(Mockito.anyString(), Mockito.anyString()))
            .thenReturn(Optional.of(CourtVenue.builder()
                                        .courtName("test")
                                        .regionId("1")
                                        .siteName("test")
                                        .region("test")
                                        .build()));
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
    }

    @Test
    public void testSubmitApplicationEventValidation() throws Exception {

        PartyDetails fl401Applicant = PartyDetails.builder()
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        String applicantNames = "TestFirst TestLast";

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .courtEmailAddress("localcourt@test.com")
            .dateSubmitted(String.valueOf("22-02-2022"))
            .welshLanguageRequirementApplication(LanguagePreference.english)
            .languageRequirementApplicationNeedWelsh(YesOrNo.Yes)
            .submitCountyCourtSelection(dynamicList)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
        CallbackResponse callbackResponse = CallbackResponse.builder()
            .data(caseData)
            .errors(Collections.singletonList("test"))
            .build();

        UserDetails userDetails = UserDetails.builder()
            .forename("test")
            .surname("last")
            .build();

        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(fl401StatementOfTruthAndSubmitChecker.hasMandatoryCompleted(caseData)).thenReturn(true);

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();

        fl401SubmitApplicationController.fl401SubmitApplicationValidation(authToken, s2sToken, callbackRequest);
        verify(fl401StatementOfTruthAndSubmitChecker, times(1)).hasMandatoryCompleted(caseData);
    }

    @Test
    public void testSubmitApplicationEventValidationMandatoryNotDone() throws Exception {

        PartyDetails fl401Applicant = PartyDetails.builder()
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        String applicantNames = "TestFirst TestLast";

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .courtEmailAddress("localcourt@test.com")
            .dateSubmitted(String.valueOf("22-02-2022"))
            .welshLanguageRequirementApplication(LanguagePreference.english)
            .languageRequirementApplicationNeedWelsh(YesOrNo.Yes)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(12345L)
                             .data(stringObjectMap)
                             .build())
            .build();

        CallbackResponse callbackResponse = CallbackResponse.builder()
            .data(caseData)
            .errors(Collections.singletonList("test"))
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
        when(fl401StatementOfTruthAndSubmitChecker.hasMandatoryCompleted(caseData)).thenReturn(false);
        AboutToStartOrSubmitCallbackResponse callbackResponseTest = fl401SubmitApplicationController.fl401SubmitApplicationValidation(
            authToken,
            s2sToken,
            callbackRequest
        );
        verify(fl401StatementOfTruthAndSubmitChecker, times(1)).hasMandatoryCompleted(caseData);
        Assertions.assertEquals(1, callbackResponseTest.getErrors().size());
        Assertions.assertEquals("Statement of truth and submit is not allowed for this case unless "
                                    + "you finish all the mandatory events", callbackResponseTest.getErrors().get(0));

    }

    @Test
    public void testCourtNameAndEmailAddressReturnedWhileFamilyEmailAddressReturned_WithBothOrders() throws Exception {

        generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();

        List<FL401OrderTypeEnum> orderList = new ArrayList<>();
        orderList.add(FL401OrderTypeEnum.occupationOrder);
        orderList.add(FL401OrderTypeEnum.nonMolestationOrder);

        orders = TypeOfApplicationOrders.builder()
            .orderType(orderList)
            .build();

        linkToCA = LinkToCA.builder()
            .linkToCaApplication(YesOrNo.Yes)
            .caApplicationNumber("123")
            .build();

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .phoneNumber("1234567890")
            .isEmailAddressConfidential(YesOrNo.Yes)
            .isPhoneNumberConfidential(YesOrNo.Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        CaseData caseData = CaseData.builder()
            .typeOfApplicationOrders(orders)
            .typeOfApplicationLinkToCA(linkToCA)
            .draftOrderDoc(Document.builder()
                               .documentUrl(generatedDocumentInfo.getUrl())
                               .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                               .documentHash(generatedDocumentInfo.getHashToken())
                               .documentFileName("FL401-Final.docx")
                               .build())
            .applicantsFL401(applicant)
            .caseTypeOfApplication(PrlAppsConstants.FL401_CASE_TYPE)
            .state(State.AWAITING_FL401_SUBMISSION_TO_HMCTS)
            .submitCountyCourtSelection(dynamicList)
            .build();

        CallbackResponse callbackResponse = CallbackResponse.builder()
            .data(CaseData.builder()
                      .draftOrderDoc(Document.builder()
                                         .documentUrl(generatedDocumentInfo.getUrl())
                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                         .documentHash(generatedDocumentInfo.getHashToken())
                                         .documentFileName("FL401-Final.docx")
                                         .build())
                      .state(State.AWAITING_SUBMISSION_TO_HMCTS)
                      .build())
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        when(documentGenService.generateDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
            fl401DocsMap);
        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Court closestDomesticAbuseCourt = courtFinderService.getNearestFamilyCourt(
            CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper));
        Optional<CourtEmailAddress> matchingEmailAddress = courtFinderService.getEmailAddress(closestDomesticAbuseCourt);

        when(courtFinderService.getNearestFamilyCourt(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )))
            .thenReturn(court);

        UserDetails userDetails = UserDetails.builder()
            .forename("test")
            .surname("test")
            .build();
        when(userService.getUserDetails(Mockito.anyString())).thenReturn(userDetails);
        when(fl401SubmitApplicationService.fl401GenerateDocumentSubmitApplication(
            "test-auth",
            callbackRequest,
            caseData
        )).thenReturn(stringObjectMap);

        AboutToStartOrSubmitCallbackResponse response = fl401SubmitApplicationController
            .fl401GenerateDocumentSubmitApplication(authToken, s2sToken, callbackRequest);

        assertNotNull(response.getData());
    }

    @Test
    public void testFl401SendApplicationNotification() throws Exception {

        PartyDetails fl401Applicant = PartyDetails.builder()
            .canYouProvideEmailAddress(YesOrNo.No)
            .isAddressConfidential(YesOrNo.No)
            .isPhoneNumberConfidential(YesOrNo.No)
            .build();

        CaseData caseData = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantsFL401(fl401Applicant)
            .courtEmailAddress("localcourt@test.com")
            .isNotificationSent("Yes")
            .submitCountyCourtSelection(dynamicList)
            .build();

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        uk.gov.hmcts.reform.ccd.client.model.CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder().caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder().id(1L)
                                                       .data(stringObjectMap).build()).build();

        when(fl401SubmitApplicationService.fl401SendApplicationNotification(
            Mockito.anyString(),
            Mockito.any(uk.gov.hmcts.reform.ccd.client.model.CallbackRequest.class)
        )).thenReturn(null);

        assertNull(fl401SubmitApplicationController.fl401SendApplicationNotification(
            authToken,
            s2sToken,
            callbackRequest
        ).getData());

    }

    @Test
    public void testExceptionForFl401SubmitApplicationValidation() throws Exception {

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
            fl401SubmitApplicationController.fl401SubmitApplicationValidation(
                authToken,
                s2sToken,
                callbackRequest
            );
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForFl401GenerateDocumentSubmitApplication() throws Exception {

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
            fl401SubmitApplicationController.fl401GenerateDocumentSubmitApplication(
                authToken,
                s2sToken,
                callbackRequest
            );
        }, RuntimeException.class, "Invalid Client");
    }

    @Test
    public void testExceptionForFl401SendApplicationNotification() throws Exception {

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
            fl401SubmitApplicationController.fl401SendApplicationNotification(
                authToken,
                s2sToken,
                callbackRequest
            );
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
