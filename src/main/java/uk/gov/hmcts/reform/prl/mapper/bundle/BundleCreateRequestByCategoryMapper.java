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
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleCreateRequest;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundleHearingInfo;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingCaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingData;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingRequestDocument;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.hearings.CaseHearing;
import uk.gov.hmcts.reform.prl.models.dto.hearings.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BLANK_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LISTED;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@ConditionalOnProperty(name = "feature.toggle.bundleByCategoryEnabled", havingValue = "true", matchIfMissing = true)
public class BundleCreateRequestByCategoryMapper implements IBundleCreateRequestMapper {

    private final BundleCreateRequestByCategoriesMapper bundleCreateRequestByCategoriesMapper;
    private final SystemUserService systemUserService;
    private final BundleCategoryConfig bundleCategoryConfig;

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

        List<Category> parentCategories = bundleCreateRequestByCategoriesMapper
            .getCategoriesAndDocuments(systemUserService.getSysUserToken(), caseData);
        List<Category> allCategories = parentCategories.stream()
            .flatMap(category -> category.getSubCategories().stream())
            .flatMap(this::flatMapRecursiveCategory)
            .toList();

        Map<String, List<Document>> allCategoriesToMap = allCategories.stream().collect(
            Collectors.toMap(Category::getCategoryId, category -> category.getDocuments().stream()
                .map(this::mapCategoryDocumentToPrlDocument).toList()));

        List<Element<BundlingRequestDocument>> allOtherDocumentsFromCategory = new ArrayList<>();
        List<Element<BundlingRequestDocument>> applicationDocumentFromCategory = new ArrayList<>();
        List<Element<BundlingRequestDocument>> ordersFromCategory = new ArrayList<>();

        bundleCategoryConfig.getFolders().stream().forEach(folder -> {
            folder.getDocuments().stream().forEach(document -> {
                FilterProperties filterProperties = document.getFilters().getFirst();
                if (filterProperties != null && filterProperties.getCategory() != null) {
                    if ("/data/orders".equals(document.getProperty())) {
                        ordersFromCategory.addAll(ElementUtils.wrapElements(mapBundlingRequestDocument(
                            allCategoriesToMap.get(filterProperties.getCategory()),
                            BundlingDocGroupEnum.valueOf(filterProperties.getValue()),
                            filterProperties
                        )));
                    } else if ("/data/applications".equals(document.getProperty())) {
                        applicationDocumentFromCategory.addAll(ElementUtils.wrapElements(mapBundlingRequestDocument(
                            allCategoriesToMap.get(filterProperties.getCategory()),
                            BundlingDocGroupEnum.valueOf(filterProperties.getValue()),
                            filterProperties
                        )));
                    } else if ("/data/allOtherDocuments".equals(document.getProperty())) {
                        allOtherDocumentsFromCategory.addAll(ElementUtils.wrapElements(mapBundlingRequestDocument(
                            allCategoriesToMap.get(filterProperties.getCategory()),
                            BundlingDocGroupEnum.valueOf(filterProperties.getValue()),
                            filterProperties
                        )));
                    }
                }
            });
        });

        return BundlingCaseData.builder().id(String.valueOf(caseData.getId())).bundleConfiguration(
                bundleConfigFileName)
            .data(BundlingData.builder().caseNumber(String.valueOf(caseData.getId())).applicantCaseName(caseData.getApplicantCaseName())
                      .hearingDetails(mapHearingDetails(hearingDetails))
                      .applications(applicationDocumentFromCategory)
                      .orders(ordersFromCategory)
                      .allOtherDocuments(allOtherDocumentsFromCategory).build()).build();

    }

    private Stream<Category> flatMapRecursiveCategory(Category category) {
        if (category.getSubCategories() == null) {
            return Stream.empty();
        }
        return Stream.concat(Stream.of(category), category.getSubCategories().stream()
            .flatMap(this::flatMapRecursiveCategory));
    }

    private BundleHearingInfo mapHearingDetails(Hearings hearingDetails) {
        if (null != hearingDetails && null != hearingDetails.getCaseHearings()) {
            List<CaseHearing> listedCaseHearings = hearingDetails.getCaseHearings().stream()
                .filter(caseHearing -> LISTED.equalsIgnoreCase(caseHearing.getHmcStatus())).toList();
            if (null != listedCaseHearings && !listedCaseHearings.isEmpty()) {
                List<HearingDaySchedule> hearingDaySchedules = listedCaseHearings.get(0).getHearingDaySchedule();
                if (null != hearingDaySchedules && !hearingDaySchedules.isEmpty()) {
                    return BundleHearingInfo.builder().hearingVenueAddress(getHearingVenueAddress(hearingDaySchedules.get(0)))
                        .hearingDateAndTime(null != hearingDaySchedules.get(0).getHearingStartDateTime()
                            ? getBundleDateTime(hearingDaySchedules.get(0).getHearingStartDateTime()) : BLANK_STRING)
                        .hearingJudgeName(hearingDaySchedules.get(0).getHearingJudgeName()).build();
                }
            }
        }
        return BundleHearingInfo.builder().build();
    }

    public static String getBundleDateTime(LocalDateTime bundleDateTime) {
        StringBuilder newBundleDateTime = new StringBuilder();
        LocalDateTime ldt = CaseUtils.convertUtcToBst(bundleDateTime);

        return newBundleDateTime
            .append(bundleDateTime.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)))
            .append(EMPTY_SPACE_STRING)
            .append(CaseUtils.convertLocalDateTimeToAmOrPmTime(ldt))
            .toString();
    }

    private String getHearingVenueAddress(HearingDaySchedule hearingDaySchedule) {
        return null != hearingDaySchedule.getHearingVenueName()
            ? hearingDaySchedule.getHearingVenueName() + "\n" +  hearingDaySchedule.getHearingVenueAddress()
            : hearingDaySchedule.getHearingVenueAddress();
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
        // don't include redacted documents
        if (isRedactedDocument(document) || isDraftDocument(document, filterProperties)) {
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

    private boolean isRedactedDocument(Document document) {
        return (document != null
            && document.getDocumentFileName() != null
            && document.getDocumentUrl() != null
            && document.getDocumentBinaryUrl() != null
            && document.getDocumentUrl().endsWith(REDACTED_DOCUMENT_URL)
            && document.getDocumentBinaryUrl().endsWith(REDACTED_DOCUMENT_URL_BINARY)
            && (document.getDocumentFileName()).equalsIgnoreCase(REDACTED_DOCUMENT_FILE_NAME));
    }

    private boolean isDraftDocument(Document document, FilterProperties filterProperties) {
        if (document != null
            && document.getDocumentFileName() != null
            && filterProperties.getHasdraft() != null
            && filterProperties.getHasdraft()) {
            return !document.getDocumentFileName().contains("Draft");
        }
        return true;
    }
}
