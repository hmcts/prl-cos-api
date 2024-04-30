package uk.gov.hmcts.reform.prl.services.reviewdocument;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.records.StartAllTabsUpdateDataContent;
import uk.gov.hmcts.reform.prl.enums.CaseEvent;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.YesNoNotSure;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.ScannedDocument;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
import uk.gov.hmcts.reform.prl.services.tab.alltabs.AllTabServiceImpl;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.lang.String.format;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BULK_SCAN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CITIZEN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_TIME_PATTERN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.formatDateTime;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewDocumentService {

    public static final String LEGAL_PROF_QUARANTINE_DOCS_LIST = "legalProfQuarantineDocsList";
    public static final String CAFCASS_QUARANTINE_DOCS_LIST = "cafcassQuarantineDocsList";
    public static final String COURT_STAFF_QUARANTINE_DOCS_LIST = "courtStaffQuarantineDocsList";
    public static final String CITIZEN_QUARANTINE_DOCS_LIST = "citizenQuarantineDocsList";

    private final AllTabServiceImpl allTabService;
    private final SystemUserService systemUserService;
    private final ManageDocumentsService manageDocumentsService;

    public static final String DOCUMENT_SUCCESSFULLY_REVIEWED = "# Document successfully reviewed";
    public static final String DOCUMENT_IN_REVIEW = "# Document review in progress";
    private static final String REVIEW_YES = "### You have successfully reviewed this document"
        + System.lineSeparator()
        + "This document can only be seen by court staff and the judiciary. "
        + "You can view it in case file view and the confidential details tab.";
    private static final String REVIEW_NO = "### You have successfully reviewed this document"
        + System.lineSeparator()
        + " This document is visible to all parties and can be viewed in the case documents tab.";
    private static final String REVIEW_NOT_SURE = "### You need to confirm if the uploaded document needs to be restricted"
        + System.lineSeparator()
        + "If you are not sure, you can use %s to get further information about whether "
        + "the document needs to be restricted.";
    public static final String LABEL_WITH_HINT =
        "<h3 class='govuk-heading-s'>Document</h3><label class='govuk-label' for='more-detail'>"
            + "<div id='more-detail-hint' class='govuk-hint'>The document will open in a new tab.</div></label>";
    public static final String GOVUK_LIST_BULLET_LABEL = "<ul class='govuk-list govuk-list--bullet'><li>%s</li></ul></label>";
    public static final String SUBMITTED_BY_LABEL =
        "<h3 class='govuk-heading-s'>Submitted by</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String DOCUMENT_CATEGORY_LABEL =
        "<h3 class='govuk-heading-s'>Document category</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String DOCUMENT_COMMENTS_LABEL =
        "<h3 class='govuk-heading-s'>Details/comments</h3><label class='govuk-label' for='more-detail'>"
            + "<ul class='govuk-list govuk-list--bullet'> <li>%s</li></ul></label>";
    public static final String  CONFIDENTIAL_INFO_LABEL =
        "<h3 class='govuk-heading-s'>Confidential information included</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;

    public static final String  RESTRICTED_INFO_LABEL =
        "<h3 class='govuk-heading-s'>Request to restrict access</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String  RESTRICTION_REASON_LABEL =
        "<h3 class='govuk-heading-s'>Reasons to restrict access</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;

    public static final String  BULK_SCAN_TYPE_LABEL =
        "<h3 class='govuk-heading-s'>Type</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String  BULK_SCAN_SUB_TYPE_LABEL =
        "<h3 class='govuk-heading-s'>Sub type</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String  BULK_SCAN_EXCEPTION_RECORD_REF_LABEL =
        "<h3 class='govuk-heading-s'>Exception record reference</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String  BULK_SCAN_SCANNED_DATE_LABEL =
        "<h3 class='govuk-heading-s'>Scanned date</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;
    public static final String  BULK_SCAN_DELIVERY_DATE_LABEL =
        "<h3 class='govuk-heading-s'>Delivery date</h3><label class='govuk-label' for='more-detail'>"
            + GOVUK_LIST_BULLET_LABEL;

    public static final String DOC_TO_BE_REVIEWED = "docToBeReviewed";
    public static final String DOC_LABEL = "docLabel";
    public static final String REVIEW_DOC = "reviewDoc";
    public static final String CASE_DETAILS_URL = "/cases/case-details/";
    public static final String SEND_AND_REPLY_URL = "/trigger/sendOrReplyToMessages/sendOrReplyToMessages1";
    public static final String SEND_AND_REPLY_MESSAGE_LABEL = "\">Send and reply to messages</a>";

    public List<DynamicListElement> fetchDocumentDynamicListElements(CaseData caseData, Map<String, Object> caseDataUpdated) {
        List<Element<QuarantineLegalDoc>> tempQuarantineDocumentList = new ArrayList<>();
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        //solcitor
        if (isNotEmpty(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(manageDocumentsService.getQuarantineDocumentForUploader(
                                                       element.getValue().getUploaderRole(),
                                                       element.getValue()
                                                   ).getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
            tempQuarantineDocumentList.addAll(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList());
        }
        //Cafcass
        if (isNotEmpty(caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(manageDocumentsService.getQuarantineDocumentForUploader(
                                                       element.getValue().getUploaderRole(),
                                                       element.getValue()
                                                   ).getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
            tempQuarantineDocumentList.addAll(caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList());
        }
        //court staff
        if (isNotEmpty(caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(manageDocumentsService.getQuarantineDocumentForUploader(
                                                       element.getValue().getUploaderRole(),
                                                       element.getValue()
                                                   ).getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
            tempQuarantineDocumentList.addAll(caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList());
        }
        //citizen
        if (CollectionUtils.isNotEmpty(caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(manageDocumentsService.getQuarantineDocumentForUploader(
                                                   element.getValue().getUploaderRole(),
                                                   element.getValue()
                                               ).getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
            tempQuarantineDocumentList.addAll(caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList());
        }
        //bulkscan
        if (isNotEmpty(caseData.getScannedDocuments())) {
            dynamicListElements.addAll(caseData.getScannedDocuments().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getFileName()
                                                          + HYPHEN_SEPARATOR
                                                          + CommonUtils.formatDate(
                                                   D_MMM_YYYY,
                                                   element.getValue().getScannedDate().toLocalDate()
                                               ))
                                               .build()).toList());
            tempQuarantineDocumentList.addAll(convertScannedDocumentsToQuarantineDocList(caseData.getScannedDocuments()));
        }
        caseDataUpdated.put("tempQuarantineDocumentList", tempQuarantineDocumentList);
        return dynamicListElements;
    }

    public void getReviewedDocumentDetailsNew(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (null != caseData.getReviewDocuments().getReviewDocsDynamicList()
            && null != caseData.getReviewDocuments().getReviewDocsDynamicList().getValue()) {
            UUID uuid = UUID.fromString(caseData.getReviewDocuments().getReviewDocsDynamicList().getValue().getCode());
            log.info("** uuid ** {}", uuid);
            List<Element<QuarantineLegalDoc>> tempQuarantineDocumentList = caseData.getDocumentManagementDetails().getTempQuarantineDocumentList();
            Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElement =
                getQuarantineDocumentById(tempQuarantineDocumentList, uuid);

            quarantineLegalDocElement.ifPresent(legalDocElement -> updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(
                    caseDataUpdated,
                    legalDocElement,
                    legalDocElement.getValue().getUploaderRole()
            ));
        }
    }

    private void updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(Map<String, Object> caseDataUpdated,
                                                                      Element<QuarantineLegalDoc> quarantineDocElement,
                                                                      String submittedBy) {

        QuarantineLegalDoc quarantineLegalDoc = quarantineDocElement.getValue();

        String docTobeReviewed = formatDocumentTobeReviewed(submittedBy, quarantineLegalDoc);

        caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);
        caseDataUpdated.put(DOC_LABEL,LABEL_WITH_HINT);

        Document documentTobeReviewed = manageDocumentsService.getQuarantineDocumentForUploader(
            submittedBy, quarantineLegalDoc);
        caseDataUpdated.put(REVIEW_DOC, documentTobeReviewed);
    }

    public void processReviewDocument(Map<String, Object> caseDataUpdated, CaseData caseData, UUID uuid) {
        boolean isDocumentFound = false;
        Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElementOptional;
        if (YesNoNotSure.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())
            || YesNoNotSure.yes.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            //solicitor uploaded docs
            if (null != caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList()) {
                isDocumentFound = processReviewDocument(caseData, caseDataUpdated,
                                                        caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList(),
                                                        uuid, UserDetails.builder().roles(List.of(Roles.SOLICITOR.getValue())).build(),
                                                        SOLICITOR, LEGAL_PROF_QUARANTINE_DOCS_LIST);

            }
            //cafcass uploaded docs
            if (!isDocumentFound && null != caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList()) {
                isDocumentFound = processReviewDocument(caseData, caseDataUpdated,
                                                        caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList(),
                                                        uuid, UserDetails.builder().roles(List.of(CAFCASS)).build(),
                                                        CAFCASS, CAFCASS_QUARANTINE_DOCS_LIST);

            }
            //court staff uploaded docs
            if (!isDocumentFound && null != caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList()) {
                isDocumentFound = processReviewDocument(caseData, caseDataUpdated,
                                                        caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList(),
                                                        uuid, UserDetails.builder().roles(List.of(Roles.COURT_ADMIN.getValue())).build(),
                                                        COURT_STAFF, COURT_STAFF_QUARANTINE_DOCS_LIST);

            }
            //citizen uploaded docs
            if (!isDocumentFound && null != caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList()) {
                isDocumentFound = processReviewDocument(caseData, caseDataUpdated,
                                                        caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList(),
                                                        uuid, UserDetails.builder().roles(List.of(Roles.CITIZEN.getValue())).build(),
                                                        CITIZEN, CITIZEN_QUARANTINE_DOCS_LIST);

            }
            //Bulk scan
            if (!isDocumentFound && isNotEmpty(caseData.getScannedDocuments())) {
                quarantineLegalDocElementOptional = getQuarantineBulkScanDocElement(caseData, uuid);
                if (quarantineLegalDocElementOptional.isPresent()) {
                    processDocumentsAfterReviewNew(
                        caseData,
                        caseDataUpdated,
                        quarantineLegalDocElementOptional.get(),
                        UserDetails.builder().roles(List.of(Roles.BULK_SCAN.getValue())).build(),
                        BULK_SCAN
                    );
                    removeFromScannedDocumentListAfterReview(caseDataUpdated, caseData, uuid);
                }
            }
        }
    }


    private boolean processReviewDocument(CaseData caseData,
                                          Map<String, Object> caseDataUpdated,
                                          List<Element<QuarantineLegalDoc>> quarantineDocsList,
                                          UUID uuid,
                                          UserDetails userDetails,
                                          String userRole,
                                          String quarantineDocsListToBeModified) {
        boolean isDocumentFound = false;
        Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElementOptional = getQuarantineDocumentById(
            quarantineDocsList,
            uuid
        );
        if (quarantineLegalDocElementOptional.isPresent()) {
            isDocumentFound = true;
            processDocumentsAfterReviewNew(
                caseData,
                caseDataUpdated,
                quarantineLegalDocElementOptional.get(),
                userDetails,
                userRole
            );

            //remove document from quarantine
            log.info(quarantineLegalDocElementOptional.get() + "quarantineLegalDocElementOptional******");
            quarantineDocsList.remove(quarantineLegalDocElementOptional.get());
            caseDataUpdated.put(quarantineDocsListToBeModified, quarantineDocsList);
        }
        return isDocumentFound;
    }

    private void processDocumentsAfterReviewNew(CaseData caseData,
                                                Map<String, Object> caseDataUpdated,
                                                Element<QuarantineLegalDoc> quarantineLegalDocElement,
                                                UserDetails userDetails,
                                                String userRole) {
        QuarantineLegalDoc tempQuarantineDoe = quarantineLegalDocElement.getValue();
        if (YesNoNotSure.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            tempQuarantineDoe = tempQuarantineDoe.toBuilder()
                .isConfidential(null)
                .isRestricted(null)
                .restrictedDetails(null)
                .build();
        }
        manageDocumentsService.moveDocumentsToRespectiveCategoriesNew(
            tempQuarantineDoe,
            userDetails,
            caseData,
            caseDataUpdated,
            userRole
        );
    }

    private void removeFromScannedDocumentListAfterReview(Map<String, Object> caseDataUpdated,
                                                          CaseData caseData, UUID uuid) {
        log.info("UUID is {}", uuid);
        Optional<Element<ScannedDocument>> scannedDocumentElement = caseData.getScannedDocuments().stream()
            .filter(element -> element.getId().equals(uuid)).findFirst();
        if (scannedDocumentElement.isPresent()) {
            log.info("removing document from scanned docs");
            caseData.getScannedDocuments().remove(scannedDocumentElement.get());
            caseDataUpdated.put("scannedDocuments", caseData.getScannedDocuments());
        }
    }

    private List<Element<QuarantineLegalDoc>> convertScannedDocumentsToQuarantineDocList(
        List<Element<ScannedDocument>> scannedDocumentElements) {
        return scannedDocumentElements.stream().map(
                scannedDocumentElement -> {
                    ScannedDocument scannedDocument = scannedDocumentElement.getValue();
                    return element(scannedDocumentElement.getId(),
                                   getBulkScanQuarantineDoc(scannedDocument)
                            .build());
                }).toList();
    }

    private static Optional<Element<QuarantineLegalDoc>> getQuarantineBulkScanDocElement(CaseData caseData, UUID uuid) {

        ScannedDocument scannedDocument = caseData.getScannedDocuments().stream()
            .filter(element -> element.getId().equals(uuid))
            .findFirst()
            .map(Element::getValue)
            .orElse(null);

        if (null != scannedDocument) {
            return Optional.of(element(uuid, getBulkScanQuarantineDoc(scannedDocument).build()));
        }
        return Optional.empty();
    }

    private static QuarantineLegalDoc.QuarantineLegalDocBuilder getBulkScanQuarantineDoc(ScannedDocument scannedDocument) {
        return QuarantineLegalDoc.builder()
            .fileName(scannedDocument.getFileName())
            .controlNumber(scannedDocument.getControlNumber())
            .type(scannedDocument.getType())
            .subtype(scannedDocument.getSubtype())
            .exceptionRecordReference(scannedDocument.getExceptionRecordReference())
            .url(scannedDocument.getUrl())
            .scannedDate(scannedDocument.getScannedDate())
            .deliveryDate(scannedDocument.getDeliveryDate())
            .documentParty(BULK_SCAN)
            .uploadedBy(BULK_SCAN)
            .documentUploadedDate(scannedDocument.getScannedDate())
            .isConfidential(YesOrNo.Yes) //bulk scan docs always go to confidential if decision is Yes
            .isRestricted(YesOrNo.No) //fix to getRestrictedOrConfidentialKey=confidential
            .uploaderRole(BULK_SCAN);
    }

    public ResponseEntity<SubmittedCallbackResponse> getReviewResult(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList())
            && (CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList()))
            && CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getCitizenQuarantineDocsList())
            && CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList())
            && CollectionUtils.isEmpty(caseData.getScannedDocuments())) {
            StartAllTabsUpdateDataContent startAllTabsUpdateDataContent = allTabService.getStartUpdateForSpecificEvent(
                String.valueOf(
                    caseData.getId()),
                C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())
                    ? CaseEvent.C100_ALL_DOCS_REVIEWED.getValue() : CaseEvent.FL401_ALL_DOCS_REVIEWED.getValue()
            );
            Map<String, Object> caseDataUpdated = startAllTabsUpdateDataContent.caseDataMap();
            allTabService.submitAllTabsUpdate(
                startAllTabsUpdateDataContent.authorisation(),
                String.valueOf(caseData.getId()),
                startAllTabsUpdateDataContent.startEventResponse(),
                startAllTabsUpdateDataContent.eventRequestData(),
                caseDataUpdated
            );

        }
        if (YesNoNotSure.yes.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            return ResponseEntity.ok(SubmittedCallbackResponse.builder()
                                         .confirmationHeader(DOCUMENT_SUCCESSFULLY_REVIEWED)
                                         .confirmationBody(REVIEW_YES).build());
        } else if (YesNoNotSure.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            return ResponseEntity.ok(SubmittedCallbackResponse.builder()
                                         .confirmationHeader(DOCUMENT_SUCCESSFULLY_REVIEWED)
                                         .confirmationBody(REVIEW_NO).build());
        } else {
            String sendReplyLink = "<a href=\"" + CASE_DETAILS_URL
                + caseData.getId()
                + SEND_AND_REPLY_URL
                + SEND_AND_REPLY_MESSAGE_LABEL;
            return ResponseEntity.ok(SubmittedCallbackResponse.builder()
                                         .confirmationHeader(DOCUMENT_IN_REVIEW)
                                         .confirmationBody(String.format(REVIEW_NOT_SURE, sendReplyLink))
                                         .build());
        }
    }

    private Optional<Element<QuarantineLegalDoc>> getQuarantineDocumentById(
        List<Element<QuarantineLegalDoc>> quarantineDocsList, UUID uuid) {
        return quarantineDocsList.stream()
            .filter(element -> element.getId().equals(uuid)).findFirst();
    }

    private String formatDocumentTobeReviewed(String submittedBy,
                                              QuarantineLegalDoc quarantineDoc) {
        StringBuilder reviewDetailsBuilder = new StringBuilder();
        reviewDetailsBuilder.append(format(SUBMITTED_BY_LABEL, submittedBy));
        //append quarantine document details for solicitor, cafcass & court staff
        appendQuarantineDocumentDetails(reviewDetailsBuilder, quarantineDoc, submittedBy);

        //PRL-5006 bulk scan fields
        if (BULK_SCAN.equals(submittedBy)) {
            appendBulkScanDocumentDetails(reviewDetailsBuilder, quarantineDoc);
        }
        reviewDetailsBuilder.append("<br/>");
        return reviewDetailsBuilder.toString();
    }

    private void appendQuarantineDocumentDetails(StringBuilder reviewDetailsBuilder,
                                                 QuarantineLegalDoc quarantineDoc,
                                                 String submittedBy) {
        if (CommonUtils.isNotEmpty(quarantineDoc.getCategoryName())) {
            reviewDetailsBuilder.append(format(DOCUMENT_CATEGORY_LABEL,
                                               quarantineDoc.getCategoryName()));
        }
        if (CommonUtils.isNotEmpty(quarantineDoc.getNotes())) {
            reviewDetailsBuilder.append(format(DOCUMENT_COMMENTS_LABEL,
                                               quarantineDoc.getNotes()));
        }
        if (null != quarantineDoc.getIsConfidential() && !BULK_SCAN.equals(submittedBy)) {
            reviewDetailsBuilder.append(format(CONFIDENTIAL_INFO_LABEL,
                                               quarantineDoc.getIsConfidential().getDisplayedValue()));
        }
        if (null != quarantineDoc.getIsRestricted() && !BULK_SCAN.equals(submittedBy)) {
            reviewDetailsBuilder.append(format(RESTRICTED_INFO_LABEL,
                                               quarantineDoc.getIsRestricted().getDisplayedValue()));
        }
        if (CommonUtils.isNotEmpty(quarantineDoc.getRestrictedDetails())) {
            reviewDetailsBuilder.append(format(RESTRICTION_REASON_LABEL,
                                               quarantineDoc.getRestrictedDetails()));
        }
    }

    private void appendBulkScanDocumentDetails(StringBuilder reviewDetailsBuilder,
                                               QuarantineLegalDoc quarantineDoc) {
        if (CommonUtils.isNotEmpty(quarantineDoc.getType())) {
            reviewDetailsBuilder.append(format(BULK_SCAN_TYPE_LABEL,
                                               quarantineDoc.getType()));
        }
        if (CommonUtils.isNotEmpty(quarantineDoc.getSubtype())) {
            reviewDetailsBuilder.append(format(BULK_SCAN_SUB_TYPE_LABEL,
                                               quarantineDoc.getSubtype()));
        }
        if (CommonUtils.isNotEmpty(quarantineDoc.getExceptionRecordReference())) {
            reviewDetailsBuilder.append(format(BULK_SCAN_EXCEPTION_RECORD_REF_LABEL,
                                               quarantineDoc.getExceptionRecordReference()));
        }
        if (null != quarantineDoc.getScannedDate()) {
            reviewDetailsBuilder.append(format(BULK_SCAN_SCANNED_DATE_LABEL,
                                               formatDateTime(DATE_TIME_PATTERN,
                                                              quarantineDoc.getScannedDate())));
        }
        if (null != quarantineDoc.getDeliveryDate()) {
            reviewDetailsBuilder.append(format(BULK_SCAN_DELIVERY_DATE_LABEL,
                                               formatDateTime(DATE_TIME_PATTERN,
                                                              quarantineDoc.getDeliveryDate())));
        }
    }

}
