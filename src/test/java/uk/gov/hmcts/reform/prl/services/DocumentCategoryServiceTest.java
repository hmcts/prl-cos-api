package uk.gov.hmcts.reform.prl.services;

import org.instancio.Instancio;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.ccd.client.model.Document;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentCategoryServiceTest {

    private static final String AUTHORIZATION = "authorisation";
    private static final String TOKEN = "tokenValue";

    @Mock
    private UserService userService;
    @Mock
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;
    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private DocumentCategoryService documentCategoryService;


    @Test
    public void testIsUserAllocatedRoleForCaseLocalAuthorityStaff() {
        // given
        CaseData caseData = Instancio.create(CaseData.class);
        UserDetails userDetails = Instancio.create(UserDetails.class);
        when(userService.getUserDetails(AUTHORIZATION)).thenReturn(userDetails);
        when(roleAssignmentService.isUserAllocatedRoleForCase(String.valueOf(caseData.getId()), userDetails.getId(),
                                                              Roles.LOCAL_AUTHORITY_STAFF.getValue())).thenReturn(true);

        // when
        boolean result = documentCategoryService.isUserAllocatedRoleForCaseLA(AUTHORIZATION, caseData);

        // then
        assertTrue(result);
    }

    @Test
    public void testIsUserAllocatedRoleForCaseLocalAuthoritySolicitor() {
        // given
        CaseData caseData = Instancio.create(CaseData.class);
        UserDetails userDetails = Instancio.create(UserDetails.class);
        when(userService.getUserDetails(AUTHORIZATION)).thenReturn(userDetails);
        when(roleAssignmentService.isUserAllocatedRoleForCase(String.valueOf(caseData.getId()), userDetails.getId(),
                                                              Roles.LOCAL_AUTHORITY_STAFF.getValue())).thenReturn(false);
        when(roleAssignmentService.isUserAllocatedRoleForCase(String.valueOf(caseData.getId()), userDetails.getId(),
                                                              Roles.LOCAL_AUTHORITY_SOLICITOR.getValue())).thenReturn(true);

        // when
        boolean result = documentCategoryService.isUserAllocatedRoleForCaseLA(AUTHORIZATION, caseData);

        // then
        assertTrue(result);
    }



    @Test
    public void testIsUserAllocatedRoleForNeitherALocalAuthoritySolicitorNorLocalAuthorityStaff() {
        // given
        CaseData caseData = Instancio.create(CaseData.class);
        UserDetails userDetails = Instancio.create(UserDetails.class);
        when(userService.getUserDetails(AUTHORIZATION)).thenReturn(userDetails);
        when(roleAssignmentService.isUserAllocatedRoleForCase(String.valueOf(caseData.getId()), userDetails.getId(),
                                                              Roles.LOCAL_AUTHORITY_STAFF.getValue())).thenReturn(false);
        when(roleAssignmentService.isUserAllocatedRoleForCase(String.valueOf(caseData.getId()), userDetails.getId(),
                                                              Roles.LOCAL_AUTHORITY_SOLICITOR.getValue())).thenReturn(false);

        // when
        boolean result = documentCategoryService.isUserAllocatedRoleForCaseLA(AUTHORIZATION, caseData);

        // then
        assertFalse(result);
    }


    @Test
    public void testGetCategoriesSubcategoriesWhenCcdThrowsError() {
        // given
        when(authTokenGenerator.generate()).thenReturn(TOKEN);
        when(coreCaseDataApi.getCategoriesAndDocuments(anyString(), anyString(), anyString())).thenThrow(new RuntimeException("Error"));

        // when
        DynamicList result = documentCategoryService.getCategoriesSubcategories(
            AUTHORIZATION,
            "caseReferenceValue",
            false
        );

        //then
        assertNotNull(result);
    }

    @Test
    public void testGetCategoriesSubcategories() {
        // given
        when(authTokenGenerator.generate()).thenReturn(TOKEN);
        Document document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        Category confidentialCate = new Category("confidential", "confidentialName", 1, List.of(document), null);

        Category category = new Category("categoryId", "categoryName", 2, List.of(document), List.of(confidentialCate));

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(document));
        when(coreCaseDataApi.getCategoriesAndDocuments(anyString(), anyString(), anyString())).thenReturn(categoriesAndDocuments);


        // when
        DynamicList result = documentCategoryService.getCategoriesSubcategories(
            AUTHORIZATION,
            "caseReferenceValue",
            true
        );

        //then
        assertNotNull(result);

    }


    @Test
    public void testRetrieveDocumentCategories() {
        // given
        CaseData caseData = Instancio.create(CaseData.class);
        when(authTokenGenerator.generate()).thenReturn(TOKEN);
        UserDetails userDetails = Instancio.create(UserDetails.class);
        when(userService.getUserDetails(AUTHORIZATION)).thenReturn(userDetails);
        when(roleAssignmentService.isUserAllocatedRoleForCase(String.valueOf(caseData.getId()), userDetails.getId(),
                                                              Roles.LOCAL_AUTHORITY_STAFF.getValue())).thenReturn(false);
        when(roleAssignmentService.isUserAllocatedRoleForCase(String.valueOf(caseData.getId()), userDetails.getId(),
                                                              Roles.LOCAL_AUTHORITY_SOLICITOR.getValue())).thenReturn(false);
        Document document = new Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        Category confidentialCate = new Category("confidential", "confidentialName", 1, List.of(document), null);

        Category category = new Category("categoryId", "categoryName", 2, List.of(document), List.of(confidentialCate));
        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(document));
        when(coreCaseDataApi.getCategoriesAndDocuments(anyString(), anyString(), anyString())).thenReturn(categoriesAndDocuments);


        // when
        DynamicList result = documentCategoryService.retrieveDocumentCategories(AUTHORIZATION, caseData);

        // then
        assertNotNull(result);
    }




}
