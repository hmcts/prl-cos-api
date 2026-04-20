package uk.gov.hmcts.reform.prl.mapper.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CategoriesAndDocumentsHelper {

    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;

    public List<Category> getCategoriesAndDocuments(String authorisation, CaseData caseData) {

        CategoriesAndDocuments categoriesAndDocuments = coreCaseDataApi.getCategoriesAndDocuments(
            authorisation,
            authTokenGenerator.generate(),
            String.valueOf(caseData.getId())
        );

        List<Category> parentCategories = categoriesAndDocuments.getCategories().stream()
            .sorted(Comparator.comparing(Category::getCategoryName))
            .toList();

        return parentCategories.stream()
            .flatMap(category -> category.getSubCategories().stream())
            .flatMap(this::flatMapRecursiveCategory)
            .toList();
    }

    private Stream<Category> flatMapRecursiveCategory(Category category) {
        if (category.getSubCategories() == null) {
            return Stream.empty();
        }
        return Stream.concat(Stream.of(category), category.getSubCategories().stream()
            .flatMap(this::flatMapRecursiveCategory));
    }
}
