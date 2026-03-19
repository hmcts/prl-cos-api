package uk.gov.hmcts.reform.prl.mapper.bundle;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.prl.enums.bundle.BundlingDocGroupEnum;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundleCreateRequestByCategoriesMapper {

    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;
    // TODO: read from yaml file
    @Getter
    private final Map<String, BundlingDocGroupEnum> categoryBundlingMap = new HashMap<>();
    public List<Category> getCategoriesAndDocuments(String authorisation, CaseData caseData) {

        populateCategoryBundlingMap();

        CategoriesAndDocuments categoriesAndDocuments = coreCaseDataApi.getCategoriesAndDocuments(
            authorisation,
            authTokenGenerator.generate(),
            String.valueOf(caseData.getId())
        );

        List<Category> parentCategories = categoriesAndDocuments.getCategories().stream()
            .sorted(Comparator.comparing(Category::getCategoryName))
            .toList();

        return parentCategories;
    }

    private void populateCategoryBundlingMap() {
        this.categoryBundlingMap.clear();
        categoryBundlingMap.put("MIAMCertificate", BundlingDocGroupEnum.applicantMiamCertificate);
        categoryBundlingMap.put("previousOrdersSubmittedWithApplication", BundlingDocGroupEnum.applicantPreviousOrdersSubmittedWithApplication);
        categoryBundlingMap.put("magistratesFactsAndReasons", BundlingDocGroupEnum.magistrateFactAndReasons);
        categoryBundlingMap.put("applicantStatements", BundlingDocGroupEnum.applicantWitnessStatements);
        categoryBundlingMap.put("respondentStatements", BundlingDocGroupEnum.respondentWitnessStatements);
        categoryBundlingMap.put("section37Report", BundlingDocGroupEnum.cafcassSection37Report);
        categoryBundlingMap.put("16aRiskAssessment", BundlingDocGroupEnum.sixteenARiskAssessment);
        categoryBundlingMap.put("otherDocs", BundlingDocGroupEnum.cafcassOtherDocuments);
        categoryBundlingMap.put("sec37Report", BundlingDocGroupEnum.laSection37Report);
        categoryBundlingMap.put("localAuthorityOtherDoc", BundlingDocGroupEnum.laOtherDocuments);
        categoryBundlingMap.put("DNAReports_expertReport", BundlingDocGroupEnum.dnaReports);
        categoryBundlingMap.put("drugAndAlcoholTest(toxicology)", BundlingDocGroupEnum.reportsForDrugAndAlcoholTest);
        categoryBundlingMap.put("anyOtherDoc", BundlingDocGroupEnum.anyOtherDocuments);
    }

    private void createDocumentListFromSubCategories(List<Category> categoryList,
                                                     List<Document> documentList,
                                                     final String parentLabelString,
                                                     final String parentCodeString) {
        categoryList.forEach(category -> {
            if (parentLabelString == null) {
                if (category.getDocuments() != null) {
                    category.getDocuments().forEach(document ->
                                                        documentList.add(getCcdCaseDocument(document)));
                }
                if (category.getSubCategories() != null) {
                    createDocumentListFromSubCategories(
                        category.getSubCategories(),
                        documentList,
                        category.getCategoryName(),
                        category.getCategoryId()
                    );
                }
            } else {
                if (category.getDocuments() != null) {
                    category.getDocuments().forEach(document ->
                                                        documentList.add(getCcdCaseDocument(document)));
                }
                if (category.getSubCategories() != null) {
                    createDocumentListFromSubCategories(category.getSubCategories(), documentList,
                                                        parentLabelString + " -> " + category.getCategoryName(),
                                                        parentCodeString + " -> " + category.getCategoryId()
                    );
                }
            }
        });
    }

    private Document getCcdCaseDocument(uk.gov.hmcts.reform.ccd.client.model.Document document) {
        return Document.builder()
            .documentUrl(document.getDocumentURL())
            .documentBinaryUrl(document.getDocumentBinaryURL())
            .documentFileName(document.getDocumentFilename())
            .build();
    }
}
