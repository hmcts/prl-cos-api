package uk.gov.hmcts.reform.prl.mapper.bundle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.prl.config.BundleCategoryConfig;
import uk.gov.hmcts.reform.prl.enums.bundle.BundlingDocGroupEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.bundle.FilterProperties;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.reverse;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.AWP_STATUS_CLOSED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(prefix = "feature.toggle", name = "bundleByCategoryEnabled", havingValue = "true", matchIfMissing = true)
public class BundleCreateRequestByCategoryMapper implements IBundleCreateRequestMapper {

    private static final String DATA_ORDERS = "/data/orders";
    private static final String DATA_APPLICATIONS = "/data/applications";
    private static final String DATA_ALL_OTHER_DOCUMENTS = "/data/allOtherDocuments";
    private static final List<String> AWP_CATEGORIES = List.of("applicationsWithinProceedings",
                                                               "applicationsWithinProceedingsRes",
                                                               "applicationsFromOtherProceedings");

    private final CategoriesAndDocumentsHelper categoriesAndDocumentsHelper;
    private final SystemUserService systemUserService;
    private final BundleCategoryConfig bundleCategoryConfig;
    private final HearingDetailsMapperUtil hearingDetailsMapperUtil;

    @Override
    public BundleCreateRequest mapCaseDataToBundleCreateRequest(CaseData caseData, String eventId, Hearings hearingDetails,
                                                                String bundleConfigFileName) {
        BundleCreateRequest bundleCreateRequest = BundleCreateRequest.builder()
            .caseDetails(BundlingCaseDetails.builder()
                .bundleName(caseData.getApplicantName())
                .caseData(mapCaseData(caseData,hearingDetails,
                    bundleConfigFileName))
                .build())
            .caseTypeId(CASE_TYPE).jurisdictionId(JURISDICTION).eventId(eventId).build();
        log.info("*** create bundle request mapped for the case id  : {}", caseData.getId());
        return bundleCreateRequest;
    }

    private BundlingCaseData mapCaseData(CaseData caseData, Hearings hearingDetails, String bundleConfigFileName) {

        List<Category> allCategories = categoriesAndDocumentsHelper
            .getCategoriesAndDocuments(systemUserService.getSysUserToken(), caseData);

        Map<String, List<Document>> allCategoriesToMap = allCategories.stream().collect(
            Collectors.toMap(Category::getCategoryId, category -> category.getDocuments().stream()
                .map(this::mapCategoryDocumentToPrlDocument).toList()));

        List<Element<BundlingRequestDocument>> allOtherDocumentsFromCategory = new ArrayList<>();
        List<Element<BundlingRequestDocument>> applicationDocumentFromCategory = new ArrayList<>();
        List<Element<BundlingRequestDocument>> ordersFromCategory = new ArrayList<>();

        bundleCategoryConfig.getFolders().forEach(folder -> {
            folder.getDocuments().forEach(document -> {
                FilterProperties filterProperties = document.getFilters().getFirst();
                if (filterProperties != null && filterProperties.getCategory() != null
                    && !AWP_CATEGORIES.contains(filterProperties.getCategory())) {
                    if (DATA_ORDERS.equals(document.getProperty())) {
                        List<BundlingRequestDocument> orders = new ArrayList<>(mapBundlingRequestDocument(
                            allCategoriesToMap.get(filterProperties.getCategory()),
                            BundlingDocGroupEnum.valueOf(filterProperties.getValue()),
                            filterProperties
                        ));
                        reverse(orders);
                        ordersFromCategory.addAll(ElementUtils.wrapElements(orders));
                    } else if (DATA_APPLICATIONS.equals(document.getProperty())) {
                        applicationDocumentFromCategory.addAll(ElementUtils.wrapElements(mapBundlingRequestDocument(
                            allCategoriesToMap.get(filterProperties.getCategory()),
                            BundlingDocGroupEnum.valueOf(filterProperties.getValue()),
                            filterProperties
                        )));
                    } else if (DATA_ALL_OTHER_DOCUMENTS.equals(document.getProperty())) {
                        allOtherDocumentsFromCategory.addAll(ElementUtils.wrapElements(mapBundlingRequestDocument(
                            allCategoriesToMap.get(filterProperties.getCategory()),
                            BundlingDocGroupEnum.valueOf(filterProperties.getValue()),
                            filterProperties
                        )));
                    }
                }
            });
        });

        specialCategories(caseData, applicationDocumentFromCategory, allOtherDocumentsFromCategory);

        return BundlingCaseData.builder().id(String.valueOf(caseData.getId())).bundleConfiguration(
                bundleConfigFileName)
            .data(BundlingData.builder().caseNumber(String.valueOf(caseData.getId())).applicantCaseName(caseData.getApplicantCaseName())
                      .hearingDetails(hearingDetailsMapperUtil.mapHearingDetails(hearingDetails))
                      .applications(applicationDocumentFromCategory)
                      .orders(ordersFromCategory)
                      .allOtherDocuments(allOtherDocumentsFromCategory).build()).build();

    }

    private void specialCategories(CaseData caseData,
                                   List<Element<BundlingRequestDocument>> applicationDocumentFromCategory,
                                   List<Element<BundlingRequestDocument>> allOtherDocumentsFromCategory) {

        Map<String, FilterProperties> bundleAllCategoriesMap = new HashMap<>();
        bundleCategoryConfig.getFolders().forEach(folder -> folder.getDocuments()
            .forEach(document -> document.getFilters().stream()
                .filter(f -> f.getCategory() != null)
                .forEach(f -> bundleAllCategoriesMap.put(f.getCategory(), f))));


        // C7 documents
        List<BundlingRequestDocument> citizenUploadedC7Documents = mapC7DocumentsFromCaseData(
            caseData.getCitizenResponseC7DocumentList(), FilterProperties.builder().build());

        if (!citizenUploadedC7Documents.isEmpty()) {
            applicationDocumentFromCategory.addAll(ElementUtils.wrapElements(citizenUploadedC7Documents));
        }

        // Add all other AWP Documents
        List<BundlingRequestDocument> otherAdditionalBundleDocs = mapOtherAdditionalBundleFromCaseData(
            caseData.getAdditionalApplicationsBundle(), bundleAllCategoriesMap);

        if (!otherAdditionalBundleDocs.isEmpty()) {
            allOtherDocumentsFromCategory.addAll(ElementUtils.wrapElements(otherAdditionalBundleDocs));
        }
    }

    private List<BundlingRequestDocument> mapC7DocumentsFromCaseData(List<Element<ResponseDocuments>> citizenResponseC7DocumentList,
                                                                     FilterProperties filterProperties) {
        List<BundlingRequestDocument> c7Documents = new ArrayList<>();
        Optional<List<Element<ResponseDocuments>>> uploadedC7CitizenDocs = ofNullable(citizenResponseC7DocumentList);
        if (uploadedC7CitizenDocs.isEmpty()) {
            return c7Documents;
        }
        ElementUtils.unwrapElements(citizenResponseC7DocumentList)
            .forEach(c7CitizenResponseDocument -> c7Documents
                .add(mapBundlingRequestDocument(c7CitizenResponseDocument.getCitizenDocument(), BundlingDocGroupEnum.c7Documents, filterProperties)));
        return c7Documents;
    }

    private List<BundlingRequestDocument> mapOtherAdditionalBundleFromCaseData(
        List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundleList,
        Map<String, FilterProperties> bundleAllCategoriesMap) {

        List<BundlingRequestDocument> additionalApplicationsBundle = new ArrayList<>();
        Optional<List<Element<AdditionalApplicationsBundle>>> additionalApplicationsBundleDocs = ofNullable(additionalApplicationsBundleList);
        if (additionalApplicationsBundleDocs.isEmpty()) {
            return additionalApplicationsBundle;
        }
        // C2 AWP
        ElementUtils.unwrapElements(additionalApplicationsBundleList).stream()
            .filter(additionalBundle -> additionalBundle.getC2DocumentBundle() != null)
            .filter(applicationsBundle -> AWP_STATUS_CLOSED.equals(applicationsBundle.getC2DocumentBundle().getApplicationStatus()))
            .forEach(applicationsBundle -> {
                additionalApplicationsBundle
                    .addAll(mapBundlingRequestDocumentAwp(
                        ElementUtils.unwrapElements(applicationsBundle.getC2DocumentBundle().getFinalDocument()), bundleAllCategoriesMap
                    ));
                if (applicationsBundle.getC2DocumentBundle().getSupportingEvidenceBundle() != null) {
                    ElementUtils.unwrapElements(applicationsBundle.getC2DocumentBundle().getSupportingEvidenceBundle())
                        .stream().filter(sp -> sp.getDocument() != null
                            && bundleAllCategoriesMap.get(sp.getDocument().getCategoryId()) != null)
                        .forEach(sp -> {
                            FilterProperties filterProperties = bundleAllCategoriesMap.get(sp.getDocument().getCategoryId());
                            if (filterProperties != null) {
                                additionalApplicationsBundle.add(mapBundlingRequestDocument(
                                    sp.getDocument(),
                                    BundlingDocGroupEnum.valueOf(filterProperties.getValue()),
                                    filterProperties
                                ));
                            }
                        });
                }
            });
        // Other AWP
        ElementUtils.unwrapElements(additionalApplicationsBundleList).stream()
            .filter(additionalBundle -> additionalBundle.getOtherApplicationsBundle() != null)
            .filter(applicationsBundle -> AWP_STATUS_CLOSED.equals(applicationsBundle.getOtherApplicationsBundle().getApplicationStatus()))
            .forEach(applicationsBundle -> {
                additionalApplicationsBundle
                    .addAll(mapBundlingRequestDocumentAwp(
                        ElementUtils.unwrapElements(applicationsBundle.getOtherApplicationsBundle().getFinalDocument()),
                        bundleAllCategoriesMap
                    ));
                if (applicationsBundle.getOtherApplicationsBundle().getSupportingEvidenceBundle() != null) {
                    ElementUtils.unwrapElements(applicationsBundle.getOtherApplicationsBundle().getSupportingEvidenceBundle())
                        .stream().filter(sp -> sp.getDocument() != null
                            && bundleAllCategoriesMap.get(sp.getDocument().getCategoryId()) != null)
                        .forEach(sp -> {
                            FilterProperties filterProperties = bundleAllCategoriesMap.get(sp.getDocument().getCategoryId());
                            if (filterProperties != null) {
                                additionalApplicationsBundle.add(mapBundlingRequestDocument(
                                    sp.getDocument(),
                                    BundlingDocGroupEnum.valueOf(filterProperties.getValue()),
                                    filterProperties
                                ));
                            }
                        });
                }
            });
        return additionalApplicationsBundle;
    }

    private Document mapCategoryDocumentToPrlDocument(uk.gov.hmcts.reform.ccd.client.model.Document categoryDocument) {

        return Document.builder()
            .documentUrl(categoryDocument.getDocumentURL())
            .documentBinaryUrl(categoryDocument.getDocumentBinaryURL())
            .documentFileName(categoryDocument.getDocumentFilename())
            .documentCreatedOn(Optional.ofNullable(categoryDocument.getUploadTimestamp())
                                   .map(ldt -> ldt.atZone(ZoneId.systemDefault()).toInstant())
                                   .map(Date::from).orElse(null))
            .build();
    }

    private BundlingRequestDocument mapBundlingRequestDocument(Document document,
                                                               BundlingDocGroupEnum applicationsDocGroup,
                                                               FilterProperties filterProperties) {
        // don't include redacted documents and draft documents
        if (isRedactedDocument(document) || isDraftDocument(document) || isConfidentialDocument(document)) {
            return null;
        }

        return BundlingRequestDocument.builder().documentLink(document).documentFileName(document.getDocumentFileName())
                    .documentGroup(applicationsDocGroup).build();
    }


    private List<BundlingRequestDocument> mapBundlingRequestDocument(List<Document> documents,
                                                                     BundlingDocGroupEnum applicationsDocGroup,
                                                                     FilterProperties filterProperties) {
        if (null != documents) {
            return documents.stream().map(d -> mapBundlingRequestDocument(d, applicationsDocGroup, filterProperties))
                .filter(Objects::nonNull).toList();
        }
        return Collections.emptyList();
    }

    private List<BundlingRequestDocument> mapBundlingRequestDocumentAwp(List<Document> documents,
                                                                     Map<String, FilterProperties> bundleAllCategoriesMap) {
        if (null != documents) {
            return documents.stream().map(d -> {
                FilterProperties filterProperties = bundleAllCategoriesMap.get(d.getCategoryId());
                if (null != filterProperties) {
                    return mapBundlingRequestDocument(d,
                                               BundlingDocGroupEnum.valueOf(filterProperties.getValue()),
                                               filterProperties);
                }

                return null;

            }).filter(Objects::nonNull).toList();
        }
        return Collections.emptyList();
    }

    private boolean isRedactedDocument(Document document) {
        return (document != null
            && document.getDocumentFileName() != null
            && document.getDocumentUrl() != null
            && document.getDocumentBinaryUrl() != null
            && document.getDocumentUrl().endsWith(REDACTED_DOCUMENT_URL)
            && document.getDocumentBinaryUrl().endsWith(REDACTED_DOCUMENT_URL_BINARY)
            && (document.getDocumentFileName()).equalsIgnoreCase(REDACTED_DOCUMENT_FILE_NAME));
    }

    private boolean isDraftDocument(Document document) {
        if (document != null
            && document.getDocumentFileName() != null) {
            return document.getDocumentFileName().contains("Draft");
        }
        return false;
    }

    private boolean isConfidentialDocument(Document document) {
        if (document != null
            && document.getDocumentFileName() != null) {
            return document.getDocumentFileName().contains(CONFIDENTIAL);
        }
        return false;
    }
}
