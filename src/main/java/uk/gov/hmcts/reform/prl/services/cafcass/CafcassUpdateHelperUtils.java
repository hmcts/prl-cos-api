package uk.gov.hmcts.reform.prl.services.cafcass;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import uk.gov.hmcts.reform.prl.enums.DocTypeOtherDocumentsEnum;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.CaseHearing;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.HearingDaySchedule;
import uk.gov.hmcts.reform.prl.models.cafcass.hearing.Hearings;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.ApplicantDetails;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseData;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.CafCassCaseDetail;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Document;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.Element;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CANCELLED;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.REDACTED_DOCUMENT_UUID;

@Slf4j
final class CafcassUpdateHelperUtils {

    private CafcassUpdateHelperUtils() {
    }

    static boolean isCafcassCymruRegion(String region) {
        return region != null && Integer.parseInt(region) < 7;
    }

    static void filterCancelledHearingsBeforeListing(List<Hearings> listOfHearingDetails) {
        if (CollectionUtils.isNotEmpty(listOfHearingDetails)) {
            for (Hearings hearings : listOfHearingDetails) {
                hearings.setCaseHearings(hearings.getCaseHearings()
                                         .stream()
                                         .filter(caseHearing -> !isCancelledBeforeListing(caseHearing))
                                         .toList());
            }
        }
    }

    static void updateCaseWithHearingData(CafCassCaseDetail cafCassCaseDetail, Hearings filteredHearing) {
        cafCassCaseDetail.getCaseData().setHearingData(filteredHearing);
        cafCassCaseDetail.getCaseData().setCourtName(filteredHearing.getCourtName());
        cafCassCaseDetail.getCaseData().setCourtTypeId(filteredHearing.getCourtTypeId());
        filteredHearing.setCourtName(null);
        filteredHearing.setCourtTypeId(null);
        filteredHearing.getCaseHearings().forEach(CafcassUpdateHelperUtils::moveVenueIdToEpimsId);
    }

    static void addInOtherDocuments(String category,
                                    uk.gov.hmcts.reform.prl.models.documents.Document caseDocument,
                                    List<Element<OtherDocuments>> otherDocsList) {
        try {
            if (null != caseDocument && caseDocument.getDocumentUrl() != null
                && !caseDocument.getDocumentUrl().endsWith(REDACTED_DOCUMENT_UUID)) {
                Document documentOther = buildFromCaseDocument(caseDocument);
                otherDocsList.add(Element.<OtherDocuments>builder()
                                      .id(buildDocumentElementId(category, caseDocument))
                                      .value(OtherDocuments.builder()
                                                 .documentOther(documentOther)
                                                 .documentName(caseDocument.getDocumentFileName())
                                                 .documentTypeOther(DocTypeOtherDocumentsEnum.getValue(category))
                                                 .build())
                                      .build());
            }
        } catch (MalformedURLException e) {
            log.error("Error in populating otherDocsList for CAFCASS {}", e.getMessage());
        }
    }

    static void parseQuarantineLegalDocs(List<Element<OtherDocuments>> otherDocsList,
                                         List<uk.gov.hmcts.reform.prl.models.Element<QuarantineLegalDoc>> quarantineLegalDocs,
                                         ObjectMapper objectMapper,
                                         List<String> excludedDocumentCategoryList,
                                         List<String> excludedDocumentList) {
        quarantineLegalDocs.forEach(quarantineLegalDocElement -> {
            uk.gov.hmcts.reform.prl.models.documents.Document document = null;
            if (!StringUtils.isEmpty(quarantineLegalDocElement.getValue().getCategoryId())) {
                String categoryId = quarantineLegalDocElement.getValue().getCategoryId();
                String attributeName = DocumentUtils.populateAttributeNameFromCategoryId(categoryId, null);
                document = objectMapper.convertValue(
                    objectMapper.convertValue(quarantineLegalDocElement.getValue(), java.util.Map.class).get(attributeName),
                    uk.gov.hmcts.reform.prl.models.documents.Document.class
                );
            }
            if (null != document && document.getDocumentUrl() != null && !document.getDocumentUrl().endsWith(REDACTED_DOCUMENT_UUID)) {
                log.info("Found document for category {}", quarantineLegalDocElement.getValue().getCategoryId());
                addDocumentWhenIncluded(
                    quarantineLegalDocElement.getValue().getCategoryId(),
                    document,
                    otherDocsList,
                    excludedDocumentCategoryList,
                    excludedDocumentList
                );
            }
        });
    }

    static CafCassCaseData buildCaseDataWithProcessedDocumentsCleared(
        CafCassCaseData caseData,
        List<Element<OtherDocuments>> otherDocsList,
        List<Element<ApplicantDetails>> respondents
    ) {
        CafCassCaseData updatedCaseData = caseData.toBuilder()
            .otherDocuments(otherDocsList)
            .respondents(respondents)
            .build();
        return clearProcessedDocumentFields(updatedCaseData);
    }

    private static CafCassCaseData clearProcessedDocumentFields(CafCassCaseData caseData) {
        return caseData.toBuilder()
            .legalProfUploadDocListDocTab(null)
            .bulkScannedDocListDocTab(null)
            .cafcassUploadDocListDocTab(null)
            .courtStaffUploadDocListDocTab(null)
            .citizenUploadedDocListDocTab(null)
            .localAuthorityUploadDocListDocTab(null)
            .restrictedDocuments(null)
            .confidentialDocuments(null)
            .respondentAc8Documents(null)
            .respondentBc8Documents(null)
            .respondentCc8Documents(null)
            .respondentDc8Documents(null)
            .respondentEc8Documents(null)
            .c8FormDocumentsUploaded(null)
            .bundleInformation(null)
            .otherDocumentsUploaded(null)
            .uploadOrderDoc(null)
            .specialArrangementsLetter(null)
            .additionalDocuments(null)
            .additionalDocumentsList(null)
            .stmtOfServiceAddRecipient(null)
            .stmtOfServiceForOrder(null)
            .stmtOfServiceForApplication(null)
            .finalServedApplicationDetailsList(null)
            .additionalOrderDocuments(null)
            .build();
    }

    static boolean isDocumentPresent(uk.gov.hmcts.reform.prl.models.documents.Document caseDocument,
                                     List<Element<OtherDocuments>> otherDocsList) {
        if (isNotEmpty(caseDocument)) {
            return otherDocsList.stream().anyMatch(el -> {
                try {
                    return el.getValue().getDocumentOther().equals(buildFromCaseDocument(caseDocument));
                } catch (MalformedURLException e) {
                    log.error("Error in populating otherDocsList for CAFCASS {}", e.getMessage());
                }
                return false;
            });
        }
        return false;
    }

    static boolean shouldExcludeDocument(List<String> excludedDocumentList, String documentFilename) {
        boolean isExcluded = false;
        for (String excludedDocumentName : excludedDocumentList) {
            if (documentFilename.contains(excludedDocumentName)) {
                isExcluded = true;
                break;
            }
        }
        return isExcluded;
    }

    private static void addDocumentWhenIncluded(String category,
                                                uk.gov.hmcts.reform.prl.models.documents.Document caseDocument,
                                                List<Element<OtherDocuments>> otherDocsList,
                                                List<String> excludedDocumentCategoryList,
                                                List<String> excludedDocumentList) {
        boolean categoryIncluded = CollectionUtils.isEmpty(excludedDocumentCategoryList)
            || !excludedDocumentCategoryList.contains(category);
        boolean documentIncluded = CollectionUtils.isEmpty(excludedDocumentList)
            || !shouldExcludeDocument(excludedDocumentList, caseDocument.getDocumentFileName());
        if (categoryIncluded && documentIncluded) {
            addInOtherDocuments(category, caseDocument, otherDocsList);
        }
    }

    static Document buildFromCaseDocument(uk.gov.hmcts.reform.prl.models.documents.Document caseDocument)
        throws MalformedURLException {
        URI uri = URI.create(caseDocument.getDocumentUrl());
        URL url = uri.toURL();
        return Document.builder()
            .documentId(CafCassCaseData.getDocumentId(url))
            .documentFileName(caseDocument.getDocumentFileName())
            .build();
    }

    static <T> List<Element<T>> removeRedactedDocuments(List<Element<T>> documentElements,
                                                        Function<T, String> documentIdResolver) {
        if (CollectionUtils.isEmpty(documentElements)) {
            return documentElements;
        }
        return documentElements.stream()
            .filter(element -> isNotRedactedDocument(element, documentIdResolver))
            .toList();
    }

    private static boolean isCancelledBeforeListing(CaseHearing caseHearing) {
        boolean cancelledBeforeListing = false;
        if (CANCELLED.equals(caseHearing.getHmcStatus()) && caseHearing.getHearingDaySchedule() != null) {
            for (HearingDaySchedule hearingDaySchedule : caseHearing.getHearingDaySchedule()) {
                if (ObjectUtils.isEmpty(hearingDaySchedule.getHearingStartDateTime())
                    && ObjectUtils.isEmpty(hearingDaySchedule.getHearingEndDateTime())) {
                    cancelledBeforeListing = true;
                    break;
                }
            }
        }
        return cancelledBeforeListing;
    }

    private static void moveVenueIdToEpimsId(CaseHearing caseHearing) {
        if (CollectionUtils.isNotEmpty(caseHearing.getHearingDaySchedule())) {
            caseHearing.getHearingDaySchedule().forEach(hearingDaySchedule -> {
                hearingDaySchedule.setEpimsId(hearingDaySchedule.getHearingVenueId());
                hearingDaySchedule.setHearingVenueId(null);
            });
        }
    }

    private static UUID buildDocumentElementId(String category,
                                               uk.gov.hmcts.reform.prl.models.documents.Document caseDocument) {
        return UUID.nameUUIDFromBytes(
            (category + "|" + caseDocument.getDocumentUrl() + "|" + caseDocument.getDocumentFileName())
                .getBytes(StandardCharsets.UTF_8)
        );
    }

    private static <T> boolean isNotRedactedDocument(Element<T> documentElement, Function<T, String> documentIdResolver) {
        if (documentElement == null || documentElement.getValue() == null) {
            return true;
        }
        return !REDACTED_DOCUMENT_UUID.equals(documentIdResolver.apply(documentElement.getValue()));
    }
}
