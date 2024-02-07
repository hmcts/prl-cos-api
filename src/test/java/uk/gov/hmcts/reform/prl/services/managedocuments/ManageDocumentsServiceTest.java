package uk.gov.hmcts.reform.prl.services.managedocuments;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONFIDENTIAL_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_ROLE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ManageDocumentsServiceTest {

    @InjectMocks
    ManageDocumentsService manageDocumentsService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    CoreCaseDataService coreCaseDataService;

    @Mock
    SystemUserService systemUserService;

    @Mock
    CaseDocumentClient caseDocumentClient;

    @Mock
    private CaseDocumentClientApi caseDocumentClientApi;

    @Mock
    CaseUtils caseUtils;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @Mock
    private UserService userService;

    private final String auth = "auth-token";

    private final String serviceAuthToken = "Bearer testServiceAuth";

    Element<ManageDocuments> manageDocumentsElement;

    Element<QuarantineLegalDoc> quarantineLegalDocElement;

    Document document;

    uk.gov.hmcts.reform.prl.models.documents.Document document1;

    Category subCategory1;
    Category subCategory2;
    Category category;

    CategoriesAndDocuments categoriesAndDocuments;

    List<Category> parentCategories;

    List<DynamicListElement> dynamicListElementList;

    DynamicList dynamicList;

    List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsList;

    List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTab;

    List<Element<QuarantineLegalDoc>> cafcassQuarantineDocsList;

    List<Element<QuarantineLegalDoc>> cafcassUploadDocListDocTab;

    List<Element<QuarantineLegalDoc>> courtStaffQuarantineDocsList;

    List<Element<QuarantineLegalDoc>> courtStaffUploadDocListDocTab;

    UserDetails userDetailsSolicitorRole;

    UserDetails userDetailsCafcassRole;

    UserDetails userDetailsCourtAdminRole;
    UserDetails userDetailsCourtStaffRoleExpectAdmin;

    List<String> categoriesToExclude;


    uk.gov.hmcts.reform.prl.models.documents.Document caseDoc;

    uk.gov.hmcts.reform.prl.models.documents.Document confidentialDoc;

    QuarantineLegalDoc quarantineConfidentialDoc;

    QuarantineLegalDoc quarantineCaseDoc;

    @Before
    public void init() {

        objectMapper.registerModule(new JavaTimeModule());

        document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        subCategory1 = new Category("confidential", "confidentialName", 1, List.of(document), null);
        subCategory2 = new Category("subCategory2Id", "subCategory2Name", 1, List.of(document), null);

        category = new Category("MIAMCertificate", "MIAM Certificate", 2, List.of(document), List.of(subCategory1,subCategory2));
        categoriesToExclude = Arrays.asList("citizenQuarantine", "legalProfQuarantine", "cafcassQuarantine", "courtStaffQuarantine", "confidential");

        categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(document));

        parentCategories = nullSafeCollection(categoriesAndDocuments.getCategories())
            .stream()
            .sorted(Comparator.comparing(Category::getCategoryName))
            .collect(Collectors.toList());

        dynamicListElementList = new ArrayList<>();
        CaseUtils.createCategorySubCategoryDynamicList(parentCategories, dynamicListElementList, categoriesToExclude);

        dynamicList = DynamicList.builder().value(DynamicListElement.builder().code("MIAMCertificate").label("MIAM Certificate").build())
            .listItems(dynamicListElementList).build();

        userDetailsSolicitorRole = UserDetails.builder()
            .forename("test")
            .surname("test")
            .roles(Collections.singletonList(SOLICITOR_ROLE))
            .build();

        userDetailsCafcassRole = UserDetails.builder()
            .forename("test")
            .surname("test")
            .roles(Collections.singletonList(CAFCASS_ROLE))
            .build();
        userDetailsCourtAdminRole = UserDetails.builder()
            .forename("test")
            .surname("test")
            .roles(Collections.singletonList(COURT_ADMIN_ROLE))
            .build();
        userDetailsCourtStaffRoleExpectAdmin = UserDetails.builder()
            .forename("test")
            .surname("test")
            .roles(Collections.singletonList(JUDGE_ROLE))
            .build();

        confidentialDoc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("Confidential_test.pdf")
            .documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15")
            .build();

        caseDoc = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("test.pdf")
            .documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15")
            .build();

        quarantineConfidentialDoc = QuarantineLegalDoc.builder()
            .documentParty(DocumentPartyEnum.APPLICANT.getDisplayedValue())
            .categoryId("MIAMCertificate")
            .miamCertificateDocument(confidentialDoc)
            .documentUploadedDate(LocalDateTime.now())
            .build();

        quarantineCaseDoc = QuarantineLegalDoc.builder()
            .documentParty(DocumentPartyEnum.APPLICANT.getDisplayedValue())
            .categoryId("MIAMCertificate")
            .miamCertificateDocument(caseDoc)
            .documentUploadedDate(LocalDateTime.now())
            .build();

        document1 = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentFileName("test.pdf")
            .documentUrl("http://dm-store.com/documents/7ab2e6e0-c1f3-49d0-a09d-771ab99a2f15")
            .build();

        Resource expectedResource = new ClassPathResource("task-list-markdown.md");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, HttpStatus.OK);
        when(caseDocumentClient
                 .getDocumentBinary(Mockito.anyString(), Mockito.anyString(), Mockito.any(UUID.class)))
            .thenReturn(expectedResponse);

        byte[] pdf = new byte[]{1,2,3,4,5};
        MultipartFile file = new InMemoryMultipartFile(SOA_MULTIPART_FILE,
                                                       "Confidential_" + document1.getDocumentFileName(),
                                                       APPLICATION_PDF_VALUE, pdf);
        uk.gov.hmcts.reform.ccd.document.am.model.Document document = testDocument();
        UploadResponse uploadResponse = new UploadResponse(List.of(document));
        Mockito.when(caseDocumentClient.uploadDocuments(anyString(), anyString(), anyString(), anyString(), anyList()))
            .thenReturn(uploadResponse);
        Mockito.doNothing().when(caseDocumentClient).deleteDocument(anyString(), anyString(), any(UUID.class), anyBoolean());


    }

    @Test
    public void testPopulateDocumentCategories() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        when(coreCaseDataApi.getCategoriesAndDocuments(
            any(),
            any(),
            any()
        )).thenReturn(categoriesAndDocuments);

        CaseData caseData = CaseData.builder().build();

        CaseData updatedCaseData = manageDocumentsService.populateDocumentCategories(auth, caseData);
        String docCode = updatedCaseData.getDocumentManagementDetails()
            .getManageDocuments().get(0).getValue().getDocumentCategories().getListItems()
            .get(0).getCode();
        assertEquals(subCategory2.getCategoryId(), docCode);

    }

    @Test
    public void testPopulateDocumentCategoriesExcludeCategory() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        Document document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        Category confidentialCate = new Category("confidential", "confidentialName", 1, List.of(document), null);

        Category category = new Category("categoryId", "categoryName", 2, List.of(document), List.of(confidentialCate));

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(document));

        when(coreCaseDataApi.getCategoriesAndDocuments(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(categoriesAndDocuments);

        CaseData caseData = CaseData.builder().build();

        CaseData updatedCaseData = manageDocumentsService.populateDocumentCategories(auth, caseData);
        List categoryList = updatedCaseData.getDocumentManagementDetails().getManageDocuments().get(0)
            .getValue().getDocumentCategories().getListItems();
        assertEquals(0, categoryList.size());
    }

    @Test
    @DisplayName("test case for populateDocumentCategories Exception.")
    public void testPopulateDocumentCategoriesException() {
        when(authTokenGenerator.generate()).thenThrow(new RuntimeException());
        CaseData caseData = CaseData.builder().build();
        CaseData updatedCaseData = manageDocumentsService.populateDocumentCategories(auth, caseData);
        List<DynamicListElement> listItems = updatedCaseData.getDocumentManagementDetails().getManageDocuments()
            .get(0).getValue().getDocumentCategories().getListItems();
        Assert.assertEquals(null, listItems);
    }

    @Test
    public void testCopyDocumentIfRestrictedWithSoliRole() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(dynamicList)
            .isRestricted(YesOrNo.Yes)
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsListInitial = new ArrayList<>();
        legalProfQuarantineDocsListInitial.add(element(QuarantineLegalDoc.builder().build()));
        caseDataMapInitial.put("legalProfQuarantineDocsList",legalProfQuarantineDocsListInitial);

        List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTabInitial = new ArrayList<>();
        caseDataMapInitial.put("legalProfUploadDocListDocTab",legalProfUploadDocListDocTabInitial);

        manageDocumentsElement = element(manageDocuments);

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().build();
        quarantineLegalDocElement = element(quarantineLegalDoc);

        ReviewDocuments reviewDocuments = ReviewDocuments.builder().build();

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        legalProfQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("legalProfQuarantineDocsList");
        assertNotNull(legalProfQuarantineDocsList);
        assertEquals(1,legalProfQuarantineDocsList.size());
        assertNull(caseDataMapUpdated.get("manageDocuments"));
    }

    @Test
    public void testCopyDocumentIfRestrictedWithCafcassRole() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(dynamicList)
            .isRestricted(YesOrNo.Yes)
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        List<Element<QuarantineLegalDoc>> cafcassQuarantineDocsListInitial = new ArrayList<>();
        caseDataMapInitial.put("cafcassQuarantineDocsList",cafcassQuarantineDocsListInitial);

        List<Element<QuarantineLegalDoc>> cafcassUploadDocListDocTabInitial = new ArrayList<>();
        caseDataMapInitial.put("cafcassUploadDocListDocTab",cafcassUploadDocListDocTabInitial);

        manageDocumentsElement = element(manageDocuments);

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().build();
        quarantineLegalDocElement = element(quarantineLegalDoc);

        ReviewDocuments reviewDocuments = ReviewDocuments.builder().build();

        List<Element<QuarantineLegalDoc>> listQuarantine = new ArrayList<>();
        listQuarantine.add(quarantineLegalDocElement);
        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .cafcassQuarantineDocsList(listQuarantine)
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCafcassRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        cafcassQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("cafcassQuarantineDocsList");
        assertNotNull(cafcassQuarantineDocsList);
        assertEquals(2,cafcassQuarantineDocsList.size());
        assertNull(caseDataMapUpdated.get("manageDocuments"));
    }

    @Test
    public void testCopyDocumentIfRestrictedWithJudgeRole() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(dynamicList)
            .isRestricted(YesOrNo.Yes)
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        List<Element<QuarantineLegalDoc>> cafcassQuarantineDocsListInitial = new ArrayList<>();
        cafcassQuarantineDocsListInitial.add(element(QuarantineLegalDoc.builder().build()));
        caseDataMapInitial.put("cafcassQuarantineDocsList",cafcassQuarantineDocsListInitial);

        List<Element<QuarantineLegalDoc>> cafcassUploadDocListDocTabInitial = new ArrayList<>();


        manageDocumentsElement = element(manageDocuments);

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().build();
        quarantineLegalDocElement = element(quarantineLegalDoc);
        cafcassUploadDocListDocTabInitial.add(quarantineLegalDocElement);
        caseDataMapInitial.put("cafcassUploadDocListDocTab",cafcassUploadDocListDocTabInitial);

        ReviewDocuments reviewDocuments = ReviewDocuments.builder().cafcassUploadDocListDocTab(cafcassUploadDocListDocTabInitial).build();

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCourtStaffRoleExpectAdmin);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        courtStaffQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("courtStaffQuarantineDocsList");
        assertNotNull(courtStaffQuarantineDocsList);
        assertEquals(1,courtStaffQuarantineDocsList.size());
        assertNull(caseDataMapUpdated.get("manageDocuments"));

    }

    @Test
    public void testCopyDocumentIfRestrictedWithCourtAdminRoleOld() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(DynamicList.builder().value(DynamicListElement.builder().code("test").build()).build())
            .isRestricted(YesOrNo.Yes)
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().categoryId("test")
                          .documentFileName("test")
                          .documentUrl("http://test.com/documents/d848addb-c53f-4ac0-a8ce-0a9e7f4d17ba")
                          .build())
            .build();

        HashMap hashMap = new HashMap();
        hashMap.put("testDocument", manageDocuments.getDocument());

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        List<Element<QuarantineLegalDoc>> courtStaffQuarantineDocsListInitial = new ArrayList<>();
        caseDataMapInitial.put("courtStaffQuarantineDocsList",courtStaffQuarantineDocsListInitial);

        List<Element<QuarantineLegalDoc>> courtStaffUploadDocListDocTabInitial = new ArrayList<>();
        caseDataMapInitial.put("courtStaffUploadDocListDocTab",courtStaffUploadDocListDocTabInitial);

        manageDocumentsElement = element(manageDocuments);

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().categoryId("test").build();
        quarantineLegalDocElement = element(quarantineLegalDoc);

        ReviewDocuments reviewDocuments = ReviewDocuments.builder().build();

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(hashMap, QuarantineLegalDoc.class)).thenReturn(quarantineLegalDoc);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCourtAdminRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        List<Element<QuarantineLegalDoc>> restrictedDocuments = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("restrictedDocuments");
        assertNotNull(restrictedDocuments);
        assertEquals(1,restrictedDocuments.size());
        assertNull(caseDataMapUpdated.get("manageDocuments"));


    }

    @Test
    public void testCopyDocumentIfRestrictedWithSoliRoleWithNonEmptyLegalProfUploadDocListDocTab() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(dynamicList)
            .isRestricted(YesOrNo.Yes)
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsListInitial = new ArrayList<>();
        caseDataMapInitial.put("legalProfQuarantineDocsList",legalProfQuarantineDocsListInitial);

        List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTabInitial = new ArrayList<>();

        manageDocumentsElement = element(manageDocuments);

        ReviewDocuments reviewDocuments = ReviewDocuments.builder().legalProfUploadDocListDocTab(legalProfUploadDocListDocTabInitial).build();

        List<Element<QuarantineLegalDoc>> listQuarantine = new ArrayList<>();
        listQuarantine.add(quarantineLegalDocElement);

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .legalProfQuarantineDocsList(listQuarantine).build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        legalProfQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("legalProfQuarantineDocsList");
        assertNotNull(legalProfQuarantineDocsList);
        assertEquals(2,legalProfQuarantineDocsList.size());
        assertNull(caseDataMapUpdated.get("manageDocuments"));
    }

    @Test
    public void testCopyDocumentIfRestrictedWithCafcassRoleWithNonEmptyCafcassUploadDocListDocTab() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(dynamicList)
            .isRestricted(YesOrNo.Yes)
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        List<Element<QuarantineLegalDoc>> cafcassQuarantineDocsListInitial = new ArrayList<>();
        caseDataMapInitial.put("cafcassQuarantineDocsList",cafcassQuarantineDocsListInitial);

        List<Element<QuarantineLegalDoc>> cafcassUploadDocListDocTabInitial = new ArrayList<>();

        manageDocumentsElement = element(manageDocuments);

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().build();
        quarantineLegalDocElement = element(quarantineLegalDoc);
        cafcassUploadDocListDocTabInitial.add(quarantineLegalDocElement);
        caseDataMapInitial.put("cafcassUploadDocListDocTab",cafcassUploadDocListDocTabInitial);

        ReviewDocuments reviewDocuments = ReviewDocuments.builder().cafcassUploadDocListDocTab(cafcassUploadDocListDocTabInitial).build();

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCafcassRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        cafcassQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("cafcassQuarantineDocsList");
        assertNotNull(cafcassQuarantineDocsList);
        assertEquals(1,cafcassQuarantineDocsList.size());
        assertNull(caseDataMapUpdated.get("manageDocuments"));
    }


    @Test
    public void testCopyDocumentIfRestrictedWithCourtAdminRoleNonEmptyCourtStaffUploadDocListDocTab() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(DynamicList.builder().value(DynamicListElement.builder().code("test").label("test").build()).build())
            .isRestricted(YesOrNo.Yes)
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                          .documentFileName("test")
                          .documentUrl("http://test.com/documents/d848addb-c53f-4ac0-a8ce-0a9e7f4d17ba").build())
            .build();
        HashMap hashMap = new HashMap();
        hashMap.put("testDocument", manageDocuments.getDocument());

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        List<Element<QuarantineLegalDoc>> courtStaffQuarantineDocsListInitial = new ArrayList<>();
        caseDataMapInitial.put("courtStaffQuarantineDocsList",courtStaffQuarantineDocsListInitial);

        List<Element<QuarantineLegalDoc>> courtStaffUploadDocListDocTabInitial = new ArrayList<>();


        manageDocumentsElement = element(manageDocuments);

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().build();
        quarantineLegalDocElement = element(quarantineLegalDoc);
        courtStaffUploadDocListDocTabInitial.add(quarantineLegalDocElement);
        caseDataMapInitial.put("courtStaffUploadDocListDocTab",courtStaffUploadDocListDocTabInitial);

        ReviewDocuments reviewDocuments = ReviewDocuments.builder().courtStaffUploadDocListDocTab(courtStaffUploadDocListDocTabInitial).build();

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(hashMap, QuarantineLegalDoc.class)).thenReturn(quarantineLegalDoc);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCourtAdminRole);


        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        List<Element<QuarantineLegalDoc>> restrictedDocuments = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("restrictedDocuments");
        assertNotNull(restrictedDocuments);
        assertEquals(1,restrictedDocuments.size());
        assertNull(caseDataMapUpdated.get("manageDocuments"));

    }

    @Test// laterrrrr
    public void testCopyDocumentIfRestricted() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.RESPONDENT)
            .documentCategories(DynamicList.builder().value(DynamicListElement.builder().code("test").label("test").build()).build())
            .isRestricted(YesOrNo.Yes)
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();
        HashMap hashMap = new HashMap();
        hashMap.put("testDocument", manageDocuments.getDocument());

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsListInitial = new ArrayList<>();
        caseDataMapInitial.put("legalProfQuarantineDocsList",legalProfQuarantineDocsListInitial);

        List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTabInitial = new ArrayList<>();
        caseDataMapInitial.put("legalProfUploadDocListDocTab",legalProfUploadDocListDocTabInitial);

        manageDocumentsElement = element(manageDocuments);

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentFileName("test")
                .documentUrl("1accfb1e-2574-4084-b97e-1cd53fd14815").build())
            .isRestricted(YesOrNo.Yes).categoryId("test").build();

        quarantineLegalDocElement = element(quarantineLegalDoc);
        ReviewDocuments reviewDocuments = ReviewDocuments.builder().build();

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(hashMap, QuarantineLegalDoc.class)).thenReturn(quarantineLegalDoc);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);
        legalProfQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("legalProfQuarantineDocsList");

        legalProfUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("legalProfUploadDocListDocTab");

        assertNotNull(legalProfQuarantineDocsList);
        assertEquals(1,legalProfQuarantineDocsList.size());
        assertEquals(0,legalProfUploadDocListDocTab.size());
        assertNull(caseDataMapUpdated.get("manageDocuments"));
    }

    @Test
    public void testCopyDocumentIfNotRestricted() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.RESPONDENT)
            .documentCategories(DynamicList.builder().value(DynamicListElement.builder().code("test").label("test").build()).build())
            .isRestricted(YesOrNo.No)
            .isConfidential(YesOrNo.No)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();
        HashMap hashMap = new HashMap();
        hashMap.put("testDocument", manageDocuments.getDocument());

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsListInitial = new ArrayList<>();
        caseDataMapInitial.put("legalProfQuarantineDocsList",legalProfQuarantineDocsListInitial);

        List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTabInitial = new ArrayList<>();
        caseDataMapInitial.put("legalProfUploadDocListDocTab",legalProfUploadDocListDocTabInitial);

        manageDocumentsElement = element(manageDocuments);

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().build();
        quarantineLegalDocElement = element(quarantineLegalDoc);
        ReviewDocuments reviewDocuments = ReviewDocuments.builder().build();

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(hashMap, QuarantineLegalDoc.class)).thenReturn(quarantineLegalDoc);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        legalProfQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("legalProfQuarantineDocsList");

        legalProfUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("legalProfUploadDocListDocTab");

        assertNotNull(legalProfUploadDocListDocTab);
        assertEquals(0,legalProfQuarantineDocsList.size());
        assertEquals(1,legalProfUploadDocListDocTab.size());
        assertNull(caseDataMapUpdated.get("manageDocuments"));

    }

    @Test
    public void testCopyDocumentIfNotRestrictedAndUploadedOnBehalfOfCourt() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.COURT)
            .documentCategories(dynamicList)
            .documentRestrictCheckbox(new ArrayList<>())
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();
        HashMap hashMap = new HashMap();
        hashMap.put("internalCorrespondenceDocument", manageDocuments.getDocument());

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsListInitial = new ArrayList<>();
        caseDataMapInitial.put("legalProfQuarantineDocsList",legalProfQuarantineDocsListInitial);

        List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTabInitial = new ArrayList<>();
        caseDataMapInitial.put("legalProfUploadDocListDocTab",legalProfUploadDocListDocTabInitial);

        manageDocumentsElement = element(manageDocuments);

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().build();
        quarantineLegalDocElement = element(quarantineLegalDoc);
        ReviewDocuments reviewDocuments = ReviewDocuments.builder().build();

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(hashMap, QuarantineLegalDoc.class)).thenReturn(quarantineLegalDoc);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCourtAdminRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        courtStaffUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("courtStaffUploadDocListDocTab");
        assertNotNull(courtStaffUploadDocListDocTab);
        assertEquals(1,courtStaffUploadDocListDocTab.size());
        assertNull(caseDataMapUpdated.get("manageDocuments"));
    }

    @Test
    public void returnTrueIfUserIsCourtStaff() {
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCourtAdminRole);

        Assert.assertTrue(manageDocumentsService.checkIfUserIsCourtStaff(userDetailsCourtAdminRole));
    }

    @Test
    public void returnFalseIfUserIsOtherThanCourtStaff() {
        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        Assert.assertFalse(manageDocumentsService.checkIfUserIsCourtStaff(userDetailsSolicitorRole));
    }

    @Test
    public void returnTrueIfCourtSelectedInDocumentParty() {
        ManageDocuments manageDocument = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.COURT)
            .documentCategories(dynamicList)
            .documentRestrictCheckbox(new ArrayList<>())
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();
        List<Element<ManageDocuments>> manageDocumentsList = List.of(element(manageDocument));
        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocumentsList);
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(manageDocumentsList)
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        Assert.assertTrue(manageDocumentsService.isCourtSelectedInDocumentParty(callbackRequest));
    }

    @Test
    public void returnFalseIfCourtNotSelectedInDocumentParty() {
        ManageDocuments manageDocument1 = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.APPLICANT)
            .documentCategories(dynamicList)
            .documentRestrictCheckbox(new ArrayList<>())
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();
        ManageDocuments manageDocument2 = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.RESPONDENT)
            .documentCategories(dynamicList)
            .documentRestrictCheckbox(new ArrayList<>())
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();
        List<Element<ManageDocuments>> manageDocumentsList = List.of(element(manageDocument1), element(manageDocument2));
        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocumentsList);
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(manageDocumentsList)
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        Assert.assertFalse(manageDocumentsService.isCourtSelectedInDocumentParty(callbackRequest));
    }

    @Test
    public void testCopyDocumentMidEventIfRestrictedWithSoliRole_whenTriedWithOutReason() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(dynamicList)
            .isRestricted(YesOrNo.Yes)
            .isConfidential(YesOrNo.Yes)
            .restrictedDetails(null)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        manageDocumentsElement = element(manageDocuments);

        CaseData caseData = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        List<String>  caseDataMapUpdated = manageDocumentsService.validateRestrictedReason(callbackRequest, userDetailsSolicitorRole);

        assertNotNull(caseDataMapUpdated);
        assertTrue(!caseDataMapUpdated.isEmpty());
        assertEquals("You must give a reason why the document should be restricted", caseDataMapUpdated.get(0));

    }

    @Test
    public void testCopyDocumentMidEventIfRestrictedWithSoliRole() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(dynamicList)
            .isRestricted(YesOrNo.Yes)
            .isConfidential(YesOrNo.Yes)
            .restrictedDetails("Reason")
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        manageDocumentsElement = element(manageDocuments);

        CaseData caseData = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        List<String>  caseDataMapUpdated = manageDocumentsService.validateRestrictedReason(callbackRequest, userDetailsSolicitorRole);

        assertNotNull(caseDataMapUpdated);
        assertTrue(caseDataMapUpdated.isEmpty());
    }

    @Test
    public void testCopyDocumentMidEventIfNotRestrictedWithSoliRole() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(dynamicList)
            .isRestricted(YesOrNo.No)
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        manageDocumentsElement = element(manageDocuments);

        CaseData caseData = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        List<String>  caseDataMapUpdated = manageDocumentsService.validateRestrictedReason(callbackRequest, userDetailsSolicitorRole);
        assertNotNull(caseDataMapUpdated);
        assertTrue(caseDataMapUpdated.isEmpty());
    }

    @Test
    public void testUpdateCaseData() {
        Map<String, Object> caseDataMapInitial = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();
        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageDocumentsService.updateCaseData(callbackRequest, caseDataUpdated);
        assertNotNull(callbackRequest);
    }

    @Test
    public void validateNonCourtUser() {
        ManageDocuments manageDocument = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.COURT)
            .documentCategories(dynamicList)
            .documentRestrictCheckbox(new ArrayList<>())
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();
        List<Element<ManageDocuments>> manageDocumentsList = List.of(element(manageDocument));
        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocumentsList);
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(manageDocumentsList)
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        UserDetails userDetails = UserDetails.builder().roles(List.of(CAFCASS_ROLE)).build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails(), CaseData.class)).thenReturn(caseData);
        List<String> list = manageDocumentsService.validateCourtUser(callbackRequest, userDetails);
        Assert.assertNotNull(list);
        Assert.assertEquals("Only court admin/Judge can select the value 'court' for 'submitting on behalf of'", list.get(0));
    }

    @Test
    public void validateCourtUser() {
        ManageDocuments manageDocument = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.COURT)
            .documentCategories(dynamicList)
            .documentRestrictCheckbox(new ArrayList<>())
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();
        List<Element<ManageDocuments>> manageDocumentsList = List.of(element(manageDocument));
        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments", manageDocumentsList);
        CaseData caseData = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails.builder()
                                           .manageDocuments(manageDocumentsList)
                                           .build())
            .build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        UserDetails userDetails = UserDetails.builder().roles(List.of(COURT_ADMIN_ROLE)).build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails(), CaseData.class)).thenReturn(caseData);
        List<String> list = manageDocumentsService.validateCourtUser(callbackRequest, userDetails);
        Assert.assertTrue(list.isEmpty());
    }

    @Test//aaa
    public void moveToConfidentialOrRestricted() {
        List<Element<QuarantineLegalDoc>> listQuarantine = new ArrayList<>();
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .documentUploadedDate(LocalDateTime.now()).build();
        listQuarantine.add(element(quarantineLegalDoc));

        QuarantineLegalDoc quarantineLegalDoc2 = QuarantineLegalDoc.builder()
            .documentUploadedDate(LocalDateTime.now()).build();

        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageDocumentsService
            .moveToConfidentialOrRestricted(caseDataUpdated, listQuarantine, quarantineLegalDoc2, CONFIDENTIAL_DOCUMENTS);
        assertNotNull(caseDataUpdated);
        List<Element<QuarantineLegalDoc>> confidentialDocuments = (List<Element<QuarantineLegalDoc>>)caseDataUpdated.get("confidentialDocuments");
        assertEquals(2,confidentialDocuments.size());
    }

    @Test
    public void addQuarantineDocumentFields() {
        QuarantineLegalDoc legalDoc = QuarantineLegalDoc.builder().build();
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().build();
        QuarantineLegalDoc blankDocument = manageDocumentsService
            .addQuarantineDocumentFields(legalDoc, quarantineLegalDoc);
        assertNotNull(blankDocument);
    }

    @Test
    public void addQuarantineDocumentFieldsWithUrl() {
        QuarantineLegalDoc legalDoc = QuarantineLegalDoc.builder().build();
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc
            .builder().url(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build()).build();
        QuarantineLegalDoc urlDocument = manageDocumentsService
            .addQuarantineDocumentFields(legalDoc, quarantineLegalDoc);
        assertNotNull(urlDocument);
        assertNotNull(urlDocument.getUrl());
    }

    @Test
    public void appendConfidentialDocumentNameForCourtAdminNotCourtAdmin() {
        UserDetails userDetails = UserDetails.builder().roles(List.of(SOLICITOR_ROLE)).build();
        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        CaseData caseData = CaseData.builder().build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);

        Map<String,Object> caseDataUpdated = manageDocumentsService
            .appendConfidentialDocumentNameForCourtAdmin(callbackRequest, auth);
        assertNotNull(caseDataUpdated);
    }

    @Test //333
    public void appendConfidentialDocumentNameForCourtAdmin() {
        UserDetails userDetails = UserDetails.builder().roles(List.of(COURT_ADMIN_ROLE)).build();
        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        CaseData caseData = CaseData.builder().reviewDocuments(ReviewDocuments.builder().build()).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);

        Map<String,Object> caseDataUpdated = manageDocumentsService
            .appendConfidentialDocumentNameForCourtAdmin(callbackRequest, auth);
        assertNotNull(caseDataUpdated);
    }

    @Test //222
    public void appendConfidentialDocumentNameForCourtAdminEvrythingFilled() {
        UserDetails userDetails = UserDetails.builder().roles(List.of(COURT_ADMIN_ROLE)).build();
        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);

        List<Element<QuarantineLegalDoc>> quarantineList = new ArrayList<>();
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .hasTheConfidentialDocumentBeenRenamed(YesOrNo.No)
            .categoryId("MIAMCertificate")
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentUrl("00000000-0000-0000-0000-000000000000")
                .documentFileName("test").build())
            .uploaderRole("Staff").build();
        quarantineLegalDocElement = element(quarantineLegalDoc);
        quarantineList.add(quarantineLegalDocElement);
        Map<String, Object> map = new HashMap<>();
        map.put("miamCertificateDocument", quarantineLegalDoc);

        when(objectMapper.convertValue(quarantineLegalDoc, Map.class)).thenReturn(map);
        when(objectMapper.convertValue(map.get("miamCertificateDocument"), uk.gov.hmcts.reform.prl.models.documents.Document.class))
            .thenReturn(quarantineLegalDoc.getDocument());

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        CaseData caseData = CaseData.builder().reviewDocuments(ReviewDocuments.builder()
            .confidentialDocuments(quarantineList).build()).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);

        Map<String,Object> caseDataUpdated = manageDocumentsService
            .appendConfidentialDocumentNameForCourtAdmin(callbackRequest, auth);
        assertNotNull(caseDataUpdated);
    }

    @Test //111
    public void appendConfidentialDocumentNameForCourtAdminRestrictedDocuments() {

        uk.gov.hmcts.reform.prl.models.documents.Document document = uk.gov.hmcts.reform.prl.models.documents.Document.builder()
            .documentUrl("00000000-0000-0000-0000-000000000000")
            .documentFileName("test").build();

        List<Element<QuarantineLegalDoc>> quarantineList = new ArrayList<>();
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .hasTheConfidentialDocumentBeenRenamed(YesOrNo.No)
            .categoryId("MIAMCertificate")
            .document(document)
            .uploaderRole("Staff").build();
        quarantineLegalDocElement = element(quarantineLegalDoc);
        quarantineList.add(quarantineLegalDocElement);

        Map<String, Object> map = new HashMap<>();
        map.put("miamCertificateDocument", quarantineLegalDoc);

        when(objectMapper.convertValue(quarantineLegalDoc, Map.class)).thenReturn(map);
        when(objectMapper.convertValue(map.get("miamCertificateDocument"), uk.gov.hmcts.reform.prl.models.documents.Document.class))
            .thenReturn(quarantineLegalDoc.getDocument());

        UserDetails userDetails = UserDetails.builder().roles(List.of(COURT_ADMIN_ROLE)).build();
        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        CaseData caseData = CaseData.builder().reviewDocuments(ReviewDocuments.builder()
            .restrictedDocuments(quarantineList).build()).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(authTokenGenerator.generate()).thenReturn("test");




        //objectMapper.convertValue(doc1,Map.class);
        Map<String, Object> tempQuarantineObjectMap = new HashMap<>();
        tempQuarantineObjectMap.put("miamCertificateDocument", document);
        tempQuarantineObjectMap.put("hasTheConfidentialDocumentBeenRenamed", YesOrNo.Yes);

        when(objectMapper.convertValue(QuarantineLegalDoc.class, Map.class))
            .thenReturn(tempQuarantineObjectMap);

        when(objectMapper.convertValue(tempQuarantineObjectMap, uk.gov.hmcts.reform.prl.models.documents.Document.class))
            .thenReturn(document);
        QuarantineLegalDoc doc1 = QuarantineLegalDoc.builder()
            .hasTheConfidentialDocumentBeenRenamed(YesOrNo.Yes)
            .categoryId("MIAMCertificate")
            .document(document)
            .uploaderRole("Staff").build();
        QuarantineLegalDoc updatedQuarantineLegalDocumentObject = QuarantineLegalDoc.builder()
            .hasTheConfidentialDocumentBeenRenamed(YesOrNo.Yes)
            .categoryId("MIAMCertificate")
            .document(document)
            .uploaderRole("Staff").build();
        when(objectMapper.convertValue(objectMapper.convertValue(doc1,Map.class), QuarantineLegalDoc.class))
            .thenReturn(updatedQuarantineLegalDocumentObject);

        Map<String,Object> caseDataUpdated = manageDocumentsService
            .appendConfidentialDocumentNameForCourtAdmin(callbackRequest, auth);

        assertNotNull(caseDataUpdated);
    }

    @Test
    public void testGetQuarantineDocumentForUploaderCafcass() {
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .cafcassQuarantineDocument(uk.gov.hmcts.reform.prl.models.documents.Document
                                           .builder().documentUrl("http://test.com/documents/d848addb-c53f-4ac0-a8ce-0a9e7f4d17ba").build()).build();
        uk.gov.hmcts.reform.prl.models.documents.Document document1 = manageDocumentsService
            .getQuarantineDocumentForUploader("Cafcass", quarantineLegalDoc
            );

        assertNotNull(document1);
        assertEquals(quarantineLegalDoc.getCafcassQuarantineDocument().getDocumentUrl(),document1.getDocumentUrl());
    }

    @Test
    public void testGetQuarantineDocumentForUploaderBulkScan() {

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .url(uk.gov.hmcts.reform.prl.models.documents.Document
                     .builder().documentUrl("http://test.com/documents/d848addb-c53f-4ac0-a8ce-0a9e7f4d17ba").build()).build();

        uk.gov.hmcts.reform.prl.models.documents.Document document1 = manageDocumentsService
            .getQuarantineDocumentForUploader("Bulk scan", quarantineLegalDoc
                );

        assertNotNull(document1);
        assertEquals(quarantineLegalDoc.url.getDocumentUrl(),document1.getDocumentUrl());
    }

    @Test
    public void testGetQuarantineDocumentForUploaderDefault() {
        uk.gov.hmcts.reform.prl.models.documents.Document document1 = manageDocumentsService
            .getQuarantineDocumentForUploader(" ",
                QuarantineLegalDoc.builder().build());

        assertNull(document1);
    }

    private static uk.gov.hmcts.reform.ccd.document.am.model.Document testDocument() {
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link binaryLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        binaryLink.href = "http://test.link";
        uk.gov.hmcts.reform.ccd.document.am.model.Document.Link selfLink = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Link();
        selfLink.href = "http://test.link";

        uk.gov.hmcts.reform.ccd.document.am.model.Document.Links links = new uk.gov.hmcts.reform.ccd.document.am.model.Document.Links();
        links.binary = binaryLink;
        links.self = selfLink;

        uk.gov.hmcts.reform.ccd.document.am.model.Document document = uk.gov.hmcts.reform.ccd.document.am.model.Document.builder().build();
        document.links = links;
        document.originalDocumentName = "Confidential_test.pdf";
        return document;
    }
}

