package uk.gov.hmcts.reform.prl.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.FeeResponse;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamPolicyUpgradeDetails;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.CourtFinderService;
import uk.gov.hmcts.reform.prl.services.DgsService;
import uk.gov.hmcts.reform.prl.services.DocumentLanguageService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.services.FeeService;
import uk.gov.hmcts.reform.prl.services.MiamPolicyUpgradeService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.document.C100DocumentTemplateFinderService;
import uk.gov.hmcts.reform.prl.services.validators.SubmitAndPayChecker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@PropertySource(value = "classpath:application.yaml")
@RunWith(MockitoJUnitRunner.Silent.class)
public class PrePopulateFeeAndSolicitorNameControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private PrePopulateFeeAndSolicitorNameController prePopulateFeeAndSolicitorNameController;

    @Mock
    private UserService userService;

    @Mock
    private DgsService dgsService;

    @Mock
    private GeneratedDocumentInfo generatedDocumentInfo;

    @Mock
    private FeeService feesService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CourtFinderService courtFinderService;

    @Mock
    private UserDetails userDetails;

    @Mock
    private FeeResponse feeResponse;

    @Mock
    private Court court;

    @Mock
    private CaseDetails caseDetails;

    @Mock
    private CaseData caseData;

    @Mock
    private CaseData caseDataForAllegationOfHarmRevised;

    @Mock
    private SubmitAndPayChecker submitAndPayChecker;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private CallbackRequest callbackRequest;

    @Mock
    private DocumentLanguage documentLanguage;

    @Mock
    private AuthorisationService authorisationService;

    @Mock
    private DocumentLanguageService documentLanguageService;
    @Mock
    private MiamPolicyUpgradeService miamPolicyUpgradeService;

    @Mock
    private EventService eventPublisher;

    @Mock
    private C100DocumentTemplateFinderService c100DocumentTemplateFinderService;

    @Value("${document.templates.c100.c100_draft_template}")
    protected String c100DraftTemplate;

    @Value("${document.templates.c100.c100_draft_filename}")
    protected String c100DraftFilename;

    @Value("${document.templates.c100.c100_draft_welsh_template}")
    protected String c100DraftWelshTemplate;

    @Value("${document.templates.c100.c100_draft_welsh_filename}")
    protected String c100DraftWelshFilename;

    public static final String authToken = "Bearer TestAuthToken";
    public static final String s2sToken = "s2s AuthToken";
    private static final String DRAFT_C_100_APPLICATION = "Draft_c100_application.pdf";

    @Before
    public void setUp() {


        feeResponse = FeeResponse.builder()
            .amount(BigDecimal.valueOf(232.00))
            .build();

        caseData = CaseData.builder()
            .courtName("testcourt")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .miamPolicyUpgradeDetails(MiamPolicyUpgradeDetails.builder().build())
            .taskListVersion(TASK_LIST_VERSION_V3)
            .build();

        caseDataForAllegationOfHarmRevised = CaseData.builder()
            .courtName("testcourt")
            .allegationOfHarmRevised(AllegationOfHarmRevised.builder().newAllegationsOfHarmYesNo(Yes).build())
            .taskListVersion(TASK_LIST_VERSION_V2)
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .build();

        caseDetails = CaseDetails.builder()
            .caseId("123")
            .caseData(caseData)
            .build();

        court = Court.builder()
            .courtName("testcourt")
            .build();

        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        generatedDocumentInfo = GeneratedDocumentInfo.builder().build();

        documentLanguage = DocumentLanguage.builder()
            .isGenEng(true)
            .isGenWelsh(true)
            .build();
    }

    //TODO Update this testcase once we have integration with Fee and Pay
    @Test
    public void testUserDetailsForSolicitorName() throws Exception {
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        when(courtFinderService.getNearestFamilyCourt(caseDetails.getCaseData()))
            .thenReturn(court);

        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        assertNotNull(prePopulateFeeAndSolicitorNameController.prePopulateSolicitorAndFees(authToken, s2sToken, callbackRequest));

    }

    @Test
    public void testUserDetailsForSolicitorName_FeeException() throws Exception {
        when(submitAndPayChecker.hasMandatoryCompleted(Mockito.any(CaseData.class))).thenReturn(true);
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);

        when(courtFinderService.getNearestFamilyCourt(caseDetails.getCaseData()))
            .thenReturn(court);

        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenThrow(new Exception());

        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        assertNotNull(prePopulateFeeAndSolicitorNameController.prePopulateSolicitorAndFees(authToken, s2sToken, callbackRequest));

    }

    @Test
    public void testWhenControllerCalledOneInvokeToDgsService() throws Exception {
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        when(courtFinderService.getNearestFamilyCourt(caseDetails.getCaseData()))
            .thenReturn(court);
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(submitAndPayChecker.hasMandatoryCompleted(Mockito.any(CaseData.class))).thenReturn(true);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        prePopulateFeeAndSolicitorNameController.prePopulateSolicitorAndFees(authToken, s2sToken, callbackRequest);
        verify(dgsService, times(1)).generateDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );
        verify(dgsService, times(1)).generateWelshDocument(
            Mockito.anyString(),
            Mockito.any(CaseDetails.class),
            Mockito.any()
        );

    }

    @Test
    public void testWhenControllerCalledOneMiamUpgrade() throws Exception {
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(organisationService.getApplicantOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        when(courtFinderService.getNearestFamilyCourt(caseDetails.getCaseData()))
            .thenReturn(court);
        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(submitAndPayChecker.hasMandatoryCompleted(Mockito.any(CaseData.class))).thenReturn(true);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(miamPolicyUpgradeService.updateMiamPolicyUpgradeDetails(Mockito.any(), Mockito.any())).thenReturn(caseData);

        assertNotNull(prePopulateFeeAndSolicitorNameController.prePopulateSolicitorAndFees(authToken, s2sToken, callbackRequest));


    }


    @Test
    public void testFeeDetailsForFeeAmount() throws Exception {
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        when(courtFinderService.getNearestFamilyCourt(caseDetails.getCaseData()))
            .thenReturn(court);

        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        assertNotNull(prePopulateFeeAndSolicitorNameController.prePopulateSolicitorAndFees(authToken,s2sToken, callbackRequest));
    }

    @Test
    public void testCourtDetailsWithCourtName() throws Exception {
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .address(Address.builder()
                         .postCode("SE1 9BA")
                         .build())
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseDataForCourt = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .children(listOfChildren)
            .courtName("testcourt")
            .build();

        CaseDetails caseDetails1 = CaseDetails.builder()
            .caseData(caseDataForCourt)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails1)
            .build();

        Court court1 = Court.builder()
            .courtName("testcourt")
            .build();
        when(submitAndPayChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(courtFinderService.getNearestFamilyCourt(callbackRequest.getCaseDetails().getCaseData()))
            .thenReturn(court1);

        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        ;

        CaseData caseData1 = objectMapper.convertValue(
            CaseData.builder()
                .solicitorName(userDetails.getFullName())
                .applicantSolicitorEmailAddress("test@gmail.com")
                .caseworkerEmailAddress("prl_caseworker_solicitor@mailinator.com")
                .feeAmount(feeResponse.getAmount().toString())
                .submitAndPayDownloadApplicationLink(Document.builder()
                                                         .documentUrl(generatedDocumentInfo.getUrl())
                                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                                         .documentHash(generatedDocumentInfo.getHashToken())
                                                         .documentFileName("Draft_c100_application.pdf").build())
                .courtName(court1.getCourtName())
                .build(),
            CaseData.class
        );

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getCaseData(), CaseData.class))
            .thenReturn(caseData1);
        assertNotNull(prePopulateFeeAndSolicitorNameController.prePopulateSolicitorAndFees(authToken, s2sToken,callbackRequest));
    }

    @Test
    public void testCaseCreationAndSubmitWithAllegationHarmRevised() throws Exception {
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
                .thenReturn(caseData);
        PartyDetails applicant = PartyDetails.builder()
                .firstName("TestFirst")
                .lastName("TestLast")
                .address(Address.builder()
                        .postCode("SE1 9BA")
                        .build())
                .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
                .childLiveWith(childLiveWithList)
                .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseDataForCourt = CaseData.builder()
                .id(12345L)
                .applicantCaseName("TestCaseName")
                .applicantSolicitorEmailAddress("test@test.com")
                .applicants(listOfApplicants)
                .children(listOfChildren)
                .courtName("testcourt")
                .build();

        CaseDetails caseDetails1 = CaseDetails.builder()
                .caseData(caseDataForCourt)
                .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
                .caseDetails(caseDetails1)
                .build();

        Court court1 = Court.builder()
                .courtName("testcourt")
                .build();
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(true);
        when(submitAndPayChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(courtFinderService.getNearestFamilyCourt(callbackRequest.getCaseDetails().getCaseData()))


                .thenReturn(court1);

        UserDetails userDetails = UserDetails.builder()
                .forename("userFirst")
                .surname("userLast")
                .build();

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
                .url("TestUrl")
                .binaryUrl("binaryUrl")
                .hashToken("testHashToken")
                .build();
        ;

        CaseData caseData1 = objectMapper.convertValue(
                CaseData.builder()
                        .allegationOfHarmRevised(AllegationOfHarmRevised.builder().newAllegationsOfHarmYesNo(Yes)
                                .newAllegationsOfHarmChildAbuseYesNo(Yes)
                                .newAllegationsOfHarmDomesticAbuseYesNo(Yes)
                                .newAllegationsOfHarmChildAbductionYesNo(Yes).build())
                        .taskListVersion(TASK_LIST_VERSION_V2)
                        .solicitorName(userDetails.getFullName())
                        .applicantSolicitorEmailAddress("test@gmail.com")
                        .caseworkerEmailAddress("prl_caseworker_solicitor@mailinator.com")
                        .feeAmount(feeResponse.getAmount().toString())
                        .submitAndPayDownloadApplicationLink(Document.builder()
                                .documentUrl(generatedDocumentInfo.getUrl())
                                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                .documentHash(generatedDocumentInfo.getHashToken())
                                .documentFileName("Draft_c100_application.pdf").build())
                        .courtName(court1.getCourtName())
                        .build(),
                CaseData.class
        );

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
                .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
                .thenReturn(generatedDocumentInfo);

        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);

        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getCaseData(), CaseData.class))
                .thenReturn(caseData1);
        assertNotNull(prePopulateFeeAndSolicitorNameController.prePopulateSolicitorAndFees(authToken,s2sToken, callbackRequest));
    }



    @Test
    public void testCourtDetailsWithoutCourtName() throws Exception {
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .address(Address.builder()
                         .postCode("SE1 9BA")
                         .build())
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseDataForCourt = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .children(listOfChildren)
            .courtName("testcourt")
            .build();

        CaseDetails caseDetails1 = CaseDetails.builder()
            .caseData(caseDataForCourt)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails1)
            .build();

        when(submitAndPayChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(courtFinderService.getNearestFamilyCourt(callbackRequest.getCaseDetails().getCaseData()))
            .thenReturn(null);

        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        ;

        CaseData caseData1 = objectMapper.convertValue(
            CaseData.builder()
                .solicitorName(userDetails.getFullName())
                .applicantSolicitorEmailAddress("test@gmail.com")
                .caseworkerEmailAddress("prl_caseworker_solicitor@mailinator.com")
                .feeAmount(feeResponse.getAmount().toString())
                .submitAndPayDownloadApplicationLink(Document.builder()
                                                         .documentUrl(generatedDocumentInfo.getUrl())
                                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                                         .documentHash(generatedDocumentInfo.getHashToken())
                                                         .documentFileName("Draft_c100_application.pdf").build())
                .courtName("No Court Fetched")
                .build(),
            CaseData.class
        );

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getCaseData(), CaseData.class))
            .thenReturn(caseData1);
        assertNotNull(prePopulateFeeAndSolicitorNameController.prePopulateSolicitorAndFees(authToken, s2sToken, callbackRequest));
    }

    @Test
    public void testExceptionCourtDetailsWithoutCourtName() throws Exception {
        when(organisationService.getRespondentOrganisationDetails(Mockito.any(CaseData.class)))
            .thenReturn(caseData);
        PartyDetails applicant = PartyDetails.builder()
            .firstName("TestFirst")
            .lastName("TestLast")
            .address(Address.builder()
                         .postCode("SE1 9BA")
                         .build())
            .build();

        Element<PartyDetails> wrappedApplicants = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> listOfApplicants = Collections.singletonList(wrappedApplicants);

        List<LiveWithEnum> childLiveWithList = new ArrayList<>();
        childLiveWithList.add(LiveWithEnum.applicant);

        Child child = Child.builder()
            .childLiveWith(childLiveWithList)
            .build();

        String childNames = "child1 child2";

        Element<Child> wrappedChildren = Element.<Child>builder().value(child).build();
        List<Element<Child>> listOfChildren = Collections.singletonList(wrappedChildren);

        CaseData caseDataForCourt = CaseData.builder()
            .id(12345L)
            .applicantCaseName("TestCaseName")
            .applicantSolicitorEmailAddress("test@test.com")
            .applicants(listOfApplicants)
            .children(listOfChildren)
            .courtName("testcourt")
            .build();

        CaseDetails caseDetails1 = CaseDetails.builder()
            .caseData(caseDataForCourt)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails1)
            .build();

        when(submitAndPayChecker.hasMandatoryCompleted(caseData)).thenReturn(true);
        when(courtFinderService.getNearestFamilyCourt(callbackRequest.getCaseDetails().getCaseData()))
            .thenReturn(null);

        UserDetails userDetails = UserDetails.builder()
            .forename("userFirst")
            .surname("userLast")
            .build();

        when(userService.getUserDetails(authToken)).thenReturn(userDetails);
        when(feesService.fetchFeeDetails(FeeType.C100_SUBMISSION_FEE)).thenReturn(feeResponse);

        GeneratedDocumentInfo generatedDocumentInfo = GeneratedDocumentInfo.builder()
            .url("TestUrl")
            .binaryUrl("binaryUrl")
            .hashToken("testHashToken")
            .build();
        ;

        CaseData caseData1 = objectMapper.convertValue(
            CaseData.builder()
                .solicitorName(userDetails.getFullName())
                .applicantSolicitorEmailAddress("test@gmail.com")
                .caseworkerEmailAddress("prl_caseworker_solicitor@mailinator.com")
                .feeAmount(feeResponse.getAmount().toString())
                .submitAndPayDownloadApplicationLink(Document.builder()
                                                         .documentUrl(generatedDocumentInfo.getUrl())
                                                         .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                                                         .documentHash(generatedDocumentInfo.getHashToken())
                                                         .documentFileName("Draft_c100_application.pdf").build())
                .courtName("No Court Fetched")
                .build(),
            CaseData.class
        );

        when(dgsService.generateDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);
        when(dgsService.generateWelshDocument(Mockito.anyString(), Mockito.any(CaseDetails.class), Mockito.any()))
            .thenReturn(generatedDocumentInfo);

        when(documentLanguageService.docGenerateLang(Mockito.any(CaseData.class))).thenReturn(documentLanguage);
        when(authorisationService.isAuthorized(any(),any())).thenReturn(true);
        when(objectMapper.convertValue(callbackRequest.getCaseDetails().getCaseData(), CaseData.class))
            .thenReturn(caseData1);
        Mockito.when(authorisationService.isAuthorized(authToken, s2sToken)).thenReturn(false);
        assertExpectedException(() -> {
            prePopulateFeeAndSolicitorNameController.prePopulateSolicitorAndFees(authToken, s2sToken, callbackRequest);
        }, RuntimeException.class, "Invalid Client");
    }

    protected <T extends Throwable> void assertExpectedException(ThrowingRunnable methodExpectedToFail, Class<T> expectedThrowableClass,
                                                                 String expectedMessage) {
        T exception = assertThrows(expectedThrowableClass, methodExpectedToFail);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
