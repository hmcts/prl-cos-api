package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

public class C100RespondentSolicitorControllerTest {

    //    @InjectMocks
    //    private C100RespondentSolicitorController c100RespondentSolicitorController;
    //
    //    @Mock
    //    C100RespondentSolicitorService respondentSolicitorService;
    //
    //    @Mock
    //    private RespondentSolicitorMiamService respondentSolicitorMiamService;
    //
    //    @Mock
    //    private ObjectMapper objectMapper;
    //
    //    private CaseData caseData;
    //
    //    @Mock
    //    private GeneratedDocumentInfo generatedDocumentInfo;
    //
    //    @Mock
    //    private OrganisationService organisationService;
    //
    //    @Mock
    //    private DocumentGenService documentGenService;
    //
    //    public static final String authToken = "Bearer TestAuthToken";
    //    private static final Map<String, Object> c7DraftMap = new HashMap<>();

    //    @Before
    //    public void setUp() {
    //
    //        MockitoAnnotations.openMocks(this);
    //
    //        List<ConfidentialityListEnum> confidentialityListEnums = new ArrayList<>();
    //
    //        confidentialityListEnums.add(ConfidentialityListEnum.email);
    //        confidentialityListEnums.add(ConfidentialityListEnum.phoneNumber);
    //
    //        caseData = CaseData.builder()
    //            .courtName("testcourt")
    //            .welshLanguageRequirement(Yes)
    //            .welshLanguageRequirementApplication(english)
    //            .languageRequirementApplicationNeedWelsh(Yes)
    //            .keepContactDetailsPrivateOther(KeepDetailsPrivate.builder()
    //                                                .confidentiality(Yes)
    //                                                .confidentialityList(confidentialityListEnums)
    //                                                .build())
    //            .build();
    //    }
    //
    //    @Test
    //    public void testKeepDetailsPrivateAsYes() throws Exception {
    //
    //        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
    //        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
    //
    //        CaseDetails caseDetails = CaseDetails.builder()
    //            .id(123L)
    //            .data(stringObjectMap)
    //            .build();
    //
    //        CallbackRequest callbackRequest = CallbackRequest.builder()
    //            .caseDetails(caseDetails)
    //            .build();
    //
    //        AboutToStartOrSubmitCallbackResponse aboutToStartOrSubmitCallbackResponse = c100RespondentSolicitorController
    //            .generateConfidentialityDynamicSelectionDisplay(callbackRequest);
    //        assertNotNull(aboutToStartOrSubmitCallbackResponse.getData().get("confidentialListDetails"));
    //
    //    }
    //
    //    @Test
    //    public void generateAndStoreC7DraftDocument() throws Exception {
    //        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
    //            .representativeLastName("Xyz")
    //            .gender(Gender.male)
    //            .email("abc@xyz.com")
    //            .phoneNumber("1234567890")
    //            .canYouProvideEmailAddress(YesOrNo.Yes)
    //            .isEmailAddressConfidential(YesOrNo.Yes)
    //            .isPhoneNumberConfidential(YesOrNo.Yes)
    //            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
    //            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
    //            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
    //            .build();
    //        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
    //        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);
    //
    //        CaseData caseData = CaseData.builder()
    //            .welshLanguageRequirement(Yes)
    //            .welshLanguageRequirementApplication(english)
    //            .languageRequirementApplicationNeedWelsh(Yes)
    //            .draftC7ResponseDoc(Document.builder()
    //                                    .documentUrl(generatedDocumentInfo.getUrl())
    //                                    .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
    //                                    .documentHash(generatedDocumentInfo.getHashToken())
    //                                    .documentFileName("c7DraftFilename.pdf")
    //                                    .build())
    //            .id(123L)
    //            .applicants(applicantList)
    //            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
    //            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
    //            .build();
    //
    //        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());
    //
    //        when(documentGenService.generateC7DraftDocuments(Mockito.anyString(), Mockito.any(CaseData.class))).thenReturn(
    //            c7DraftMap);
    //
    //        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);
    //
    //        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
    //            .CallbackRequest.builder()
    //            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
    //                             .id(123L)
    //                             .data(stringObjectMap)
    //                             .build())
    //            .build();
    //
    //        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorController.generateC7ResponseDraftDocument(
    //            authToken,
    //            callbackRequest
    //        );
    //
    //        assertTrue(response.getData().containsKey("draftC7ResponseDoc"));
    //    }
}



