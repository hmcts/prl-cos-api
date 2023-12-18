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
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.Document;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.enums.managedocuments.DocumentPartyEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ReviewDocuments;
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
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_ADMIN_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_ROLE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@RunWith(MockitoJUnitRunner.class)
public class ManageDocumentsServiceTest {

    @InjectMocks
    ManageDocumentsService manageDocumentsService;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

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

    List<String> categoriesToExclude;

    RestrictToCafcassHmcts restrictToCafcassHmcts = RestrictToCafcassHmcts.restrictToGroup;

    @Before
    public void init() {

        document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        subCategory1 = new Category("subCategory1Id", "subCategory1Name", 1, List.of(document), null);
        subCategory2 = new Category("subCategory2Id", "subCategory2Name", 1, List.of(document), List.of(subCategory1));

        category = new Category("categoryId", "categoryName", 2, List.of(document), List.of(subCategory2));
        categoriesToExclude = Arrays.asList("citizenQuarantine", "legalProfQuarantine", "cafcassQuarantine");

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



    }

    @Test
    public void testPopulateDocumentCategories() {
        when(authTokenGenerator.generate()).thenReturn(serviceAuthToken);

        when(coreCaseDataApi.getCategoriesAndDocuments(
            Mockito.any(),
            Mockito.any(),
            Mockito.any()
        )).thenReturn(categoriesAndDocuments);

        CaseData caseData = CaseData.builder().build();

        CaseData updatedCaseData = manageDocumentsService.populateDocumentCategories(auth, caseData);
        String docCode  = updatedCaseData.getManageDocuments().get(0).getValue().getDocumentCategories().getListItems().get(0).getCode();
        assertEquals("subCategory1Id",docCode);
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
            .documentRestrictCheckbox(List.of(restrictToCafcassHmcts))
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
            .documentRestrictCheckbox(List.of(restrictToCafcassHmcts))
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

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
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
        assertEquals(1,legalProfUploadDocListDocTab.size());
    }

    @Test
    public void testCopyDocumentIfRestrictedWithCafcassRole() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(dynamicList)
            .documentRestrictCheckbox(List.of(restrictToCafcassHmcts))
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

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
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
        assertEquals(0,cafcassUploadDocListDocTab.size());
    }

    @Test
    public void testCopyDocumentIfRestrictedWithCafcassRoleWithNonEmptyCafcassUploadDocListDocTab() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(dynamicList)
            .documentRestrictCheckbox(List.of(restrictToCafcassHmcts))
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
    public void testCopyDocumentIfRestrictedWithCourtStaffRole() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(dynamicList)
            .documentRestrictCheckbox(List.of(restrictToCafcassHmcts))
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

        CaseData caseData = CaseData.builder()
            .reviewDocuments(reviewDocuments)
            .manageDocuments(List.of(manageDocumentsElement)).build();
        CaseDetails caseDetails = CaseDetails.builder().id(12345L).data(caseDataMapInitial).build();
        CallbackRequest callbackRequest = CallbackRequest.builder().caseDetails(caseDetails).build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCourtStaffRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        courtStaffQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("courtStaffQuarantineDocsList");

        courtStaffUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("courtStaffUploadDocListDocTab");

        assertNull(caseDataMapUpdated.get("manageDocuments"));
        assertEquals(1,courtStaffQuarantineDocsList.size());
        assertEquals(0,courtStaffUploadDocListDocTab.size());
    }

    @Test
    public void testCopyDocumentIfRestrictedWithCourtStaffRoleNonEmptyCourtStaffUploadDocListDocTab() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.CAFCASS_CYMRU)
            .documentCategories(dynamicList)
            .documentRestrictCheckbox(List.of(restrictToCafcassHmcts))
            .document(uk.gov.hmcts.reform.prl.models.documents.Document.builder().build())
            .build();

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

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);
        when(caseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper)).thenReturn(caseData);
        when(userService.getUserDetails(auth)).thenReturn(userDetailsCourtStaffRole);

        Map<String, Object>  caseDataMapUpdated = manageDocumentsService.copyDocument(callbackRequest, auth);

        courtStaffQuarantineDocsList = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("courtStaffQuarantineDocsList");

        courtStaffUploadDocListDocTab = (List<Element<QuarantineLegalDoc>>) caseDataMapUpdated.get("courtStaffUploadDocListDocTab");

        assertNull(caseDataMapUpdated.get("manageDocuments"));
        assertEquals(1,courtStaffQuarantineDocsList.size());
        assertEquals(1,courtStaffUploadDocListDocTab.size());
    }

    @Test
    public void testCopyDocumentIfNotRestricted() {

        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentParty(DocumentPartyEnum.RESPONDENT)
            .documentCategories(dynamicList)
            .documentRestrictCheckbox(new ArrayList<>())
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
        assertEquals(0,legalProfQuarantineDocsList.size());
        assertEquals(1,legalProfUploadDocListDocTab.size());

    }

}

