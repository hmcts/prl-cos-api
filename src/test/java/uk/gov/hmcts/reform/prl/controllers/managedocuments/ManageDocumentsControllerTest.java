package uk.gov.hmcts.reform.prl.controllers.managedocuments;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.Document;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackResponse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DocumentManagementDetails;
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS_ROLE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR_ROLE;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.nullSafeCollection;

@RunWith(MockitoJUnitRunner.class)
public class ManageDocumentsControllerTest {

    @InjectMocks
    ManageDocumentsController manageDocumentsController;

    @Mock
    ManageDocumentsService manageDocumentsService;
    @Mock
    UserService userService;

    @Mock
    AllTabServiceImpl tabService;

    @Mock
    ObjectMapper objectMapper;

    String auth = "authorisation";

    Map<String, Object> caseDataMap;
    CaseDetails caseDetails;
    CaseData caseData;

    CallbackRequest callbackRequest;

    Document document;

    Category subCategory1;
    Category subCategory2;
    Category category;

    CategoriesAndDocuments categoriesAndDocuments;

    List<Category> parentCategories;

    List<DynamicListElement> dynamicListElementList;

    DynamicList dynamicList;

    List<String> categoriesToExclude;

    @Before
    public void setup() {
        caseDataMap = new HashMap<>();
        caseDataMap.put("id", 12345678L);
        caseData = CaseData.builder()
            .id(12345678L)
            .build();

        caseDetails = CaseDetails.builder()
            .id(12345678L)
            .data(caseDataMap)
            .build();

        callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        when(objectMapper.convertValue(caseDetails.getData(), CaseData.class)).thenReturn(caseData);

        document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        subCategory1 = new Category("subCategory1Id", "subCategory1Name", 1, List.of(document), null);
        subCategory2 = new Category("subCategory2Id", "subCategory2Name", 1, List.of(document), List.of(subCategory1));

        category = new Category("categoryId", "categoryName", 2, List.of(document), List.of(subCategory2));
        categoriesToExclude = Arrays.asList("citizenQuarantine", "legalProfQuarantine", "cafcassQuarantine");

        categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(document));

        parentCategories = nullSafeCollection(categoriesAndDocuments.getCategories())
            .stream()
            .sorted(Comparator.comparing(Category::getCategoryName))
            .toList();

        dynamicListElementList = new ArrayList<>();
        CaseUtils.createCategorySubCategoryDynamicList(parentCategories, dynamicListElementList, categoriesToExclude);

        dynamicList = DynamicList.builder().value(DynamicListElement.EMPTY)
            .listItems(dynamicListElementList).build();
    }

    @Test
    public void testHandleAboutToStart() {
        ManageDocuments manageDocuments = ManageDocuments.builder()
            .documentCategories(dynamicList)
            .build();

        Element<ManageDocuments> manageDocumentsElement = element(manageDocuments);

        CaseData caseDataUpdated = CaseData.builder()
            .documentManagementDetails(DocumentManagementDetails
                                           .builder()
                                           .manageDocuments(List.of(manageDocumentsElement))
                                           .build()).build();

        when(manageDocumentsService.populateDocumentCategories(auth, caseData)).thenReturn(caseDataUpdated);
        CallbackResponse response = manageDocumentsController.handleAboutToStart(auth, callbackRequest);
        Assert.assertNotNull(response.getData().getDocumentManagementDetails().getManageDocuments());

        verify(manageDocumentsService).populateDocumentCategories(auth, caseData);
        verifyNoMoreInteractions(manageDocumentsService);
    }

    @Test
    public void testCopyManageDocs() {

        Map<String, Object> caseDataUpdated = new HashMap<>();

        when(manageDocumentsService.copyDocument(callbackRequest, auth)).thenReturn(caseDataUpdated);

        manageDocumentsController.copyManageDocs(auth, callbackRequest);
        verify(manageDocumentsService).copyDocument(callbackRequest, auth);
    }

    @Test
    public void testHandleSubmitted() {

        ResponseEntity<SubmittedCallbackResponse> abc = manageDocumentsController.handleSubmitted(callbackRequest, auth);
        abc.getBody().getConfirmationHeader();
        Assert.assertEquals("# Documents submitted",abc.getBody().getConfirmationHeader());
        verifyNoMoreInteractions(tabService);

    }

    @Test
    public void testCopyManageDocsMid() {

        UserDetails userDetailsSolicitorRole = UserDetails.builder()
            .forename("test")
            .surname("test")
            .roles(Collections.singletonList(SOLICITOR_ROLE))
            .build();

        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);

        manageDocumentsController.validateManageDocumentsData(auth, callbackRequest);
        verify(manageDocumentsService).validateRestrictedReason(callbackRequest, userDetailsSolicitorRole);

    }

    @Test
    public void testCopyManageDocsMidIfErrorsExist() {

        UserDetails userDetailsSolicitorRole = UserDetails.builder()
            .forename("test")
            .surname("test")
            .roles(Collections.singletonList(SOLICITOR_ROLE))
            .build();

        when(userService.getUserDetails(auth)).thenReturn(userDetailsSolicitorRole);
        when(manageDocumentsService.validateCourtUser(any(), any())).thenReturn(List.of("errors"));

        manageDocumentsController.validateManageDocumentsData(auth, callbackRequest);
        verify(manageDocumentsService).validateRestrictedReason(callbackRequest, userDetailsSolicitorRole);

    }

    @Test
    public void testCopyManageDocsMid_notSolicitor() {

        UserDetails userDetailsCafcassRole = UserDetails.builder()
            .forename("test")
            .surname("test")
            .roles(Collections.singletonList(CAFCASS_ROLE))
            .build();

        when(userService.getUserDetails(auth)).thenReturn(userDetailsCafcassRole);

        manageDocumentsController.validateManageDocumentsData(auth, callbackRequest);
        verify(manageDocumentsService, times(1)).validateRestrictedReason(any(),any());
    }
}
