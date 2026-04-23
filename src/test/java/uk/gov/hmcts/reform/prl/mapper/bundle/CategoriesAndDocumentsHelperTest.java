package uk.gov.hmcts.reform.prl.mapper.bundle;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriesAndDocumentsHelperTest {

    @Mock
    private AuthTokenGenerator authTokenGenerator;
    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @InjectMocks
    private CategoriesAndDocumentsHelper categoriesAndDocumentsHelper;

    private final String authorization = "authToken";

    @Test
    void shouldReturnGetCategoriesAndDocuments() {

        CaseData caseData = CaseData.builder()
            .id(12345L).build();

        uk.gov.hmcts.reform.ccd.client.model.Document documents =
            new uk.gov.hmcts.reform.ccd.client.model
                .Document("documentURL", "fileName", "binaryUrl", "attributePath", LocalDateTime.now());

        Category subCategory = new Category("categoryId", "categoryName", 3, List.of(documents), new ArrayList<>());
        Category category = new Category("parentCategoryId", "parentCategoryName", 2, List.of(documents), List.of(subCategory));

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, List.of(category), List.of(documents));
        when(coreCaseDataApi.getCategoriesAndDocuments(authorization, authTokenGenerator.generate(), String.valueOf(caseData.getId())))
            .thenReturn(categoriesAndDocuments);

        List<Category> categories = categoriesAndDocumentsHelper.getCategoriesAndDocuments(authorization, caseData);
        assertNotNull(categories);
        assertEquals(2, categories.size());
        assertEquals(category.getCategoryId(), categories.get(0).getCategoryId());
        assertEquals(category.getSubCategories().getFirst().getCategoryId(), categories.get(1).getCategoryId());

    }

    @Test
    void shouldReturnEmptyGetCategoriesAndDocuments() {

        CaseData caseData = CaseData.builder()
            .id(12345L).build();

        CategoriesAndDocuments categoriesAndDocuments = new CategoriesAndDocuments(1, new ArrayList<>(), new  ArrayList<>());
        when(coreCaseDataApi.getCategoriesAndDocuments(authorization, authTokenGenerator.generate(), String.valueOf(caseData.getId())))
            .thenReturn(categoriesAndDocuments);

        List<Category> categories = categoriesAndDocumentsHelper.getCategoriesAndDocuments(authorization, caseData);
        assertNotNull(categories);
        assertTrue(categories.isEmpty());
    }
}
