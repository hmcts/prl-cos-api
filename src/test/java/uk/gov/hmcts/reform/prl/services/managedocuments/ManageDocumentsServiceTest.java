package uk.gov.hmcts.reform.prl.services.managedocuments;


import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.Document;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClientApi;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
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
import uk.gov.hmcts.reform.prl.services.reviewdocument.ReviewDocumentService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JUDGE_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_ROLE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ManageDocumentsServiceTest {

    @InjectMocks
    ManageDocumentsService manageDocumentsService;

    @Mock
    ObjectMapper objectMapper;

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

    @Mock
    private ReviewDocumentService reviewDocumentService;
    private final String auth = "auth-token";

    private final String serviceAuthToken = "Bearer testServiceAuth";

    Element<ManageDocuments> manageDocumentsElement;

    Element<QuarantineLegalDoc> quarantineLegalDocElement;

    Document document;

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

    UserDetails userDetailsCourtStaffRole;
    UserDetails userDetailsCourtStaffRoleExpectAdmin;

    List<String> categoriesToExclude;

    RestrictToCafcassHmcts restrictToCafcassHmcts = RestrictToCafcassHmcts.restrictToGroup;

    @Before
    public void init() {

        document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        subCategory1 = new Category("confidential", "confidentialName", 1, List.of(document), null);
        subCategory2 = new Category("subCategory2Id", "subCategory2Name", 1, List.of(document), null);

        category = new Category("categoryId", "categoryName", 2, List.of(document), List.of(subCategory1,subCategory2));
        categoriesToExclude = Arrays.asList("citizenQuarantine", "legalProfQuarantine", "cafcassQuarantine", "courtStaffQuarantine", "confidential");

        categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(document));

        parentCategories = nullSafeCollection(categoriesAndDocuments.getCategories())
            .stream()
            .sorted(Comparator.comparing(Category::getCategoryName))
            .collect(Collectors.toList());

        dynamicListElementList = new ArrayList<>();
        CaseUtils.createCategorySubCategoryDynamicList(parentCategories, dynamicListElementList, categoriesToExclude);

        dynamicList = DynamicList.builder().value(DynamicListElement.EMPTY)
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
        userDetailsCourtStaffRole = UserDetails.builder()
            .forename("test")
            .surname("test")
            .roles(Collections.singletonList(COURT_ADMIN_ROLE))
            .build();
        userDetailsCourtStaffRoleExpectAdmin = UserDetails.builder()
            .forename("test")
            .surname("test")
            .roles(Collections.singletonList(JUDGE_ROLE))
            .build();


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
        String docCode  = updatedCaseData.getManageDocuments().get(0).getValue().getDocumentCategories().getListItems().get(0).getCode();
        assertEquals(subCategory2.getCategoryId(),docCode);
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
        List categoryList  = updatedCaseData.getManageDocuments().get(0).getValue().getDocumentCategories().getListItems();
        assertEquals(0,categoryList.size());
    }

    @Test
    @DisplayName("test case for populateDocumentCategories Exception.")
    public void testPopulateDocumentCategoriesException() {
        when(authTokenGenerator.generate()).thenThrow(new RuntimeException());
        CaseData caseData = CaseData.builder().build();
        CaseData updatedCaseData = manageDocumentsService.populateDocumentCategories(auth, caseData);
        List<DynamicListElement> listItems  = updatedCaseData.getManageDocuments().get(0).getValue().getDocumentCategories().getListItems();
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
        caseDataMapInitial.put("legalProfQuarantineDocsList",legalProfQuarantineDocsListInitial);

        List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTabInitial = new ArrayList<>();
        caseDataMapInitial.put("legalProfUploadDocListDocTab",legalProfUploadDocListDocTabInitial);

        manageDocumentsElement = element(manageDocuments);

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().build();
        quarantineLegalDocElement = element(quarantineLegalDoc);

        ReviewDocuments reviewDocuments = ReviewDocuments.builder().build();

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .manageDocuments(List.of(manageDocumentsElement)).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        legalProfQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("legalProfQuarantineDocsList");

        legalProfUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("legalProfUploadDocListDocTab");

        assertNull(caseDataMapUpdated.get("manageDocuments"));
        assertEquals(1,legalProfQuarantineDocsList.size());
        assertEquals(0,legalProfUploadDocListDocTab.size());
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

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().build();
        quarantineLegalDocElement = element(quarantineLegalDoc);

        legalProfUploadDocListDocTabInitial.add(quarantineLegalDocElement);
        caseDataMapInitial.put("legalProfUploadDocListDocTab",legalProfUploadDocListDocTabInitial);

        ReviewDocuments reviewDocuments = ReviewDocuments.builder().legalProfUploadDocListDocTab(legalProfUploadDocListDocTabInitial).build();

        List<Element<QuarantineLegalDoc>> listQuarantine = new ArrayList<>();
        listQuarantine.add(quarantineLegalDocElement);

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder().legalProfQuarantineDocsList(listQuarantine).build())
            .manageDocuments(List.of(manageDocumentsElement)).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        legalProfQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("legalProfQuarantineDocsList");

        legalProfUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("legalProfUploadDocListDocTab");

        assertNull(caseDataMapUpdated.get("manageDocuments"));
        assertEquals(2,legalProfQuarantineDocsList.size());
        assertEquals(1,legalProfUploadDocListDocTab.size());
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
                .cafcassQuarantineDocsList(listQuarantine).build())
            .manageDocuments(List.of(manageDocumentsElement)).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCafcassRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        cafcassQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("cafcassQuarantineDocsList");

        cafcassUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("cafcassUploadDocListDocTab");

        assertNull(caseDataMapUpdated.get("manageDocuments"));
        assertEquals(2,cafcassQuarantineDocsList.size());
        assertEquals(0,cafcassUploadDocListDocTab.size());
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
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .manageDocuments(List.of(manageDocumentsElement)).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCafcassRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        cafcassQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("cafcassQuarantineDocsList");

        cafcassUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("cafcassUploadDocListDocTab");

        assertNull(caseDataMapUpdated.get("manageDocuments"));
        assertEquals(1,cafcassQuarantineDocsList.size());
        assertEquals(1,cafcassUploadDocListDocTab.size());
    }

    @Test
    public void testCopyDocumentIfRestrictedWithCourtAdminRole() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(DynamicList.builder().value(DynamicListElement.builder().code("test").build()).build())
            .isRestricted(YesOrNo.Yes)
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().categoryId("test").build())
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
            .manageDocuments(List.of(manageDocumentsElement)).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(hashMap, QuarantineLegalDoc.class)).thenReturn(quarantineLegalDoc);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCourtStaffRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        courtStaffQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("courtStaffQuarantineDocsList");

        courtStaffUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("courtStaffUploadDocListDocTab");

        List<Element<QuarantineLegalDoc>> courtStaffUploadDocListConfTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get(
            "courtStaffUploadDocListConfTab");

        assertNull(caseDataMapUpdated.get("manageDocuments"));
        assertEquals(0,courtStaffQuarantineDocsList.size());
        assertEquals(0,courtStaffUploadDocListDocTab.size());

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

        List<Element<QuarantineLegalDoc>> courtStaffQuarantineDocsListInitial = new ArrayList<>();
        caseDataMapInitial.put("courtStaffQuarantineDocsList",courtStaffQuarantineDocsListInitial);

        List<Element<QuarantineLegalDoc>> courtStaffUploadDocListDocTabInitial = new ArrayList<>();
        caseDataMapInitial.put("courtStaffUploadDocListDocTab",courtStaffUploadDocListDocTabInitial);

        manageDocumentsElement = element(manageDocuments);

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder().build();
        quarantineLegalDocElement = element(quarantineLegalDoc);

        ReviewDocuments reviewDocuments = ReviewDocuments.builder().build();

        List<Element<QuarantineLegalDoc>> listQuarantine = new ArrayList<>();
        listQuarantine.add(quarantineLegalDocElement);

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder()
                .legalProfQuarantineDocsList(listQuarantine).build())
            .manageDocuments(List.of(manageDocumentsElement)).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCourtStaffRoleExpectAdmin);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        courtStaffQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("courtStaffQuarantineDocsList");

        courtStaffUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("courtStaffUploadDocListDocTab");

        List<Element<QuarantineLegalDoc>> courtStaffUploadDocListConfTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get(
            "courtStaffUploadDocListConfTab");

        assertNull(courtStaffUploadDocListConfTab);
        assertNull(caseDataMapUpdated.get("manageDocuments"));
        assertEquals(1,courtStaffQuarantineDocsList.size());
        assertEquals(0,courtStaffUploadDocListDocTab.size());

    }


    @Test
    public void testCopyDocumentIfRestrictedWithCourtAdminRoleNonEmptyCourtStaffUploadDocListDocTab() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(DynamicList.builder().value(DynamicListElement.builder().code("test").label("test").build()).build())
            .isRestricted(YesOrNo.Yes)
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
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
            .manageDocuments(List.of(manageDocumentsElement)).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(hashMap, QuarantineLegalDoc.class)).thenReturn(quarantineLegalDoc);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCourtStaffRole);


        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        courtStaffQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("courtStaffQuarantineDocsList");

        courtStaffUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("courtStaffUploadDocListDocTab");

        assertNull(caseDataMapUpdated.get("manageDocuments"));
        assertEquals(0,courtStaffQuarantineDocsList.size());
        assertEquals(1,courtStaffUploadDocListDocTab.size());

    }

    @Test
    public void testCopyDocumentIfRestricted() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(DynamicList.builder().value(DynamicListElement.builder().code("test").label("test").build()).build())
            .isRestricted(YesOrNo.Yes)
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        caseDataMapInitial.put("manageDocuments",manageDocuments);

        List<Element<QuarantineLegalDoc>> courtStaffQuarantineDocsListInitial = new ArrayList<>();
        caseDataMapInitial.put("courtStaffQuarantineDocsList",courtStaffQuarantineDocsListInitial);
        manageDocumentsElement = element(manageDocuments);

        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .isConfidential(YesOrNo.Yes)
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentFileName("test")
                .documentUrl("1accfb1e-2574-4084-b97e-1cd53fd14815").build())
            .isRestricted(YesOrNo.No).categoryId("test").build();
        HashMap hashMap = new HashMap();
        hashMap.put("testDocument", quarantineLegalDoc.getDocument());
        quarantineLegalDocElement = element(quarantineLegalDoc);
        List<Element<QuarantineLegalDoc>> courtStaffUploadDocListDocTabInitial = new ArrayList<>();
        courtStaffUploadDocListDocTabInitial.add(quarantineLegalDocElement);
        caseDataMapInitial.put("courtStaffUploadDocListDocTab",courtStaffUploadDocListDocTabInitial);

        ReviewDocuments reviewDocuments = ReviewDocuments.builder().courtStaffUploadDocListDocTab(courtStaffUploadDocListDocTabInitial).build();

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .manageDocuments(List.of(manageDocumentsElement)).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(authTokenGenerator.generate()).thenReturn("test");
        Resource expectedResource = new ClassPathResource("task-list-markdown.md");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, HttpStatus.OK);
        when(caseDocumentClient
            .getDocumentBinary(Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
            .thenReturn(expectedResponse);
        when(caseDocumentClientApi.getDocumentBinary(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
            .thenReturn(expectedResponse);
        when(objectMapper.convertValue(hashMap, QuarantineLegalDoc.class)).thenReturn(quarantineLegalDoc);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        manageDocumentsService
            .moveDocumentsToRespectiveCategoriesNew(quarantineLegalDoc, userDetailsSolicitorRole, caseData, caseDataMapInitial, "Legal adviser");
        assertNotNull(quarantineLegalDoc);
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
            .documentManagementDetails(DocumentManagementDetails.builder().build())
            .manageDocuments(List.of(manageDocumentsElement)).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(hashMap, QuarantineLegalDoc.class)).thenReturn(quarantineLegalDoc);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        legalProfQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("legalProfQuarantineDocsList");

        legalProfUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("legalProfUploadDocListDocTab");

        assertNull(caseDataMapUpdated.get("manageDocuments"));
        assertEquals(0,legalProfQuarantineDocsList.size());
        assertEquals(1,legalProfUploadDocListDocTab.size());

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
            .manageDocuments(List.of(manageDocumentsElement)).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(hashMap, QuarantineLegalDoc.class)).thenReturn(quarantineLegalDoc);
        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCourtStaffRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        courtStaffUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("courtStaffUploadDocListDocTab");

        assertNull(caseDataMapUpdated.get("manageDocuments"));
        assertEquals(1,courtStaffUploadDocListDocTab.size());
    }

    @Test
    public void returnTrueIfUserIsCourtStaff() {
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCourtStaffRole);

        Assert.assertTrue(manageDocumentsService.checkIfUserIsCourtStaff(userDetailsCourtStaffRole));
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
            .manageDocuments(manageDocumentsList).build();
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
            .manageDocuments(manageDocumentsList).build();
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
            .manageDocuments(List.of(manageDocumentsElement)).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        List<String>  caseDataMapUpdated = manageDocumentsService.validateRestrictedReason(callbackRequest, userDetailsSolicitorRole);

        assertNotNull(caseDataMapUpdated);
        assertTrue(!caseDataMapUpdated.isEmpty());

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
            .manageDocuments(List.of(manageDocumentsElement)).build();
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
            .manageDocuments(List.of(manageDocumentsElement)).build();
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
            .manageDocuments(manageDocumentsList).build();
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
            .manageDocuments(manageDocumentsList).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        UserDetails userDetails = UserDetails.builder().roles(List.of(COURT_ADMIN_ROLE)).build();
        when(objectMapper.convertValue(callbackRequest.getCaseDetails(), CaseData.class)).thenReturn(caseData);
        List<String> list = manageDocumentsService.validateCourtUser(callbackRequest, userDetails);
        Assert.assertNotNull(list);
    }

    @Test
    public void moveToConfidentialOrRestricted() {
        List<Element<QuarantineLegalDoc>> listQuarantine = new ArrayList<>();
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .documentUploadedDate(LocalDateTime.now()).build();
        listQuarantine.add(element(quarantineLegalDoc));

        Map<String, Object> caseDataUpdated = new HashMap<>();
        manageDocumentsService
            .moveToConfidentialOrRestricted(caseDataUpdated, listQuarantine, quarantineLegalDoc, "Yes");
        assertNotNull(caseDataUpdated);
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

    @Test
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

    @Test
    public void appendConfidentialDocumentNameForCourtAdminEvrythingFilled() {
        UserDetails userDetails = UserDetails.builder().roles(List.of(COURT_ADMIN_ROLE)).build();
        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);

        List<Element<QuarantineLegalDoc>> quarantineList = new ArrayList<>();
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .hasTheConfidentialDocumentBeenRenamed(YesOrNo.No)
            .categoryId("test")
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentUrl("00000000-0000-0000-0000-000000000000")
                .documentFileName("test").build())
            .uploaderRole("Staff").build();
        quarantineLegalDocElement = element(quarantineLegalDoc);
        quarantineList.add(quarantineLegalDocElement);
        Map<String, Object> map = new HashMap<>();
        map.put("testDocument", quarantineLegalDoc);

        when(objectMapper.convertValue(quarantineLegalDoc, Map.class)).thenReturn(map);
        when(objectMapper.convertValue(map.get("testDocument"), uk.gov.hmcts.reform.prl.models.documents.Document.class))
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

    @Test
    public void appendConfidentialDocumentNameForCourtAdminRestrictedDocuments() {
        UserDetails userDetails = UserDetails.builder().roles(List.of(COURT_ADMIN_ROLE)).build();
        when(userService.getUserDetails(any(String.class))).thenReturn(userDetails);

        List<Element<QuarantineLegalDoc>> quarantineList = new ArrayList<>();
        QuarantineLegalDoc quarantineLegalDoc = QuarantineLegalDoc.builder()
            .hasTheConfidentialDocumentBeenRenamed(YesOrNo.No)
            .categoryId("test")
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder()
                .documentUrl("00000000-0000-0000-0000-000000000000")
                .documentFileName("test").build())
            .uploaderRole("Staff").build();
        quarantineLegalDocElement = element(quarantineLegalDoc);
        quarantineList.add(quarantineLegalDocElement);
        Map<String, Object> map = new HashMap<>();
        map.put("testDocument", quarantineLegalDoc);

        when(objectMapper.convertValue(quarantineLegalDoc, Map.class)).thenReturn(map);
        when(objectMapper.convertValue(map.get("testDocument"), uk.gov.hmcts.reform.prl.models.documents.Document.class))
            .thenReturn(quarantineLegalDoc.getDocument());

        Map<String, Object> caseDataMapInitial = new HashMap<>();
        CaseData caseData = CaseData.builder().reviewDocuments(ReviewDocuments.builder()
            .restrictedDocuments(quarantineList).build()).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(systemUserService.getSysUserToken()).thenReturn("test");
        when(authTokenGenerator.generate()).thenReturn("test");

        Resource expectedResource = new ClassPathResource("documents/document.pdf");
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<Resource> expectedResponse = new ResponseEntity<>(expectedResource, headers, HttpStatus.OK);
        when(caseDocumentClient
            .getDocumentBinary("test", "test",
                "00000000-0000-0000-0000-000000000000")).thenReturn(expectedResponse);

        Map<String,Object> caseDataUpdated = manageDocumentsService
            .appendConfidentialDocumentNameForCourtAdmin(callbackRequest, auth);
        assertNotNull(caseDataUpdated);
    }

    @Test
    public void testGetQuarantineDocumentForUploaderCafcass() {
        uk.gov.hmcts.reform.prl.models.documents.Document document1 = manageDocumentsService
            .getQuarantineDocumentForUploader("Cafcass",
            QuarantineLegalDoc.builder()
                .cafcassQuarantineDocument(uk.gov.hmcts.reform.prl.models.documents.Document
                    .builder().build()).build());

        assertNotNull(document1);
    }

    @Test
    public void testGetQuarantineDocumentForUploaderBulkScan() {
        uk.gov.hmcts.reform.prl.models.documents.Document document1 = manageDocumentsService
            .getQuarantineDocumentForUploader("Bulk scan",
                QuarantineLegalDoc.builder()
                    .url(uk.gov.hmcts.reform.prl.models.documents.Document
                        .builder().build()).build());

        assertNotNull(document1);
    }

    @Test
    public void testGetQuarantineDocumentForUploaderDefault() {
        uk.gov.hmcts.reform.prl.models.documents.Document document1 = manageDocumentsService
            .getQuarantineDocumentForUploader(" ",
                QuarantineLegalDoc.builder().build());

        assertNull(document1);
    }
}

