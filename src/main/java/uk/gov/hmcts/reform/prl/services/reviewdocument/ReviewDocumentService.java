package uk.gov.hmcts.reform.prl.services.reviewdocument;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.Roles;
import uk.gov.hmcts.reform.prl.enums.YesNoNotSure;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.ScannedDocument;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.services.managedocuments.ManageDocumentsService;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_TIME_PATTERN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.formatDateTime;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewDocumentService {

    private final CoreCaseDataService coreCaseDataService;
    private final CaseDocumentClient caseDocumentClient;
    private final AuthTokenGenerator authTokenGenerator;
    private final SystemUserService systemUserService;
    private final ManageDocumentsService manageDocumentsService;

    private final ObjectMapper objectMapper;
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
    public static final String CITIZEN_UPLOAD_DOC_LIST_CONF_TAB = "citizenUploadDocListConfTab";
    public static final String BULKSCAN_UPLOAD_DOC_LIST_CONF_TAB = "bulkScannedDocListConfTab";
    public static final String LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB = "legalProfUploadDocListDocTab";
    public static final String CAFCASS_UPLOAD_DOC_LIST_DOC_TAB = "cafcassUploadDocListDocTab";
    public static final String COURT_STAFF_UPLOAD_DOC_LIST_DOC_TAB = "courtStaffUploadDocListDocTab";
    public static final String CITIZEN_UPLOADED_DOC_LIST_DOC_TAB = "citizenUploadedDocListDocTab";
    public static final String BULKSCAN_UPLOADED_DOC_LIST_DOC_TAB = "bulkScannedDocListDocTab";
    public static final String CONFIDENTIAL_CATEGORY_ID = "confidential";
    public static final String CONFIDENTIAL = "Confidential_";
    public static final String CASE_DETAILS_URL = "/cases/case-details/";
    public static final String SEND_AND_REPLY_URL = "/trigger/sendOrReplyToMessages/sendOrReplyToMessages1";
    public static final String SEND_AND_REPLY_MESSAGE_LABEL = "\">Send and reply to messages</a>";

    public List<DynamicListElement> fetchDocumentDynamicListElements(CaseData caseData, Map<String, Object> caseDataUpdated) {
        List<Element<QuarantineLegalDoc>> tempQuarantineDocumentList = new ArrayList<>();
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        if (isNotEmpty(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(manageDocumentsService.getQuarantineDocumentForUploader(
                                                       element.getValue().getUploaderRole(),
                                                       element.getValue()
                                                   )
                                                          .getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
            tempQuarantineDocumentList.addAll(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList());
        }
        //added for cafcass
        if (isNotEmpty(caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(manageDocumentsService.getQuarantineDocumentForUploader(
                                                       element.getValue().getUploaderRole(),
                                                       element.getValue()
                                                   )
                                                          .getDocumentFileName()
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
                                                   )
                                                          .getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
            tempQuarantineDocumentList.addAll(caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList());
        }
        if (isNotEmpty(caseData.getDocumentManagementDetails().getCitizenUploadQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getDocumentManagementDetails().getCitizenUploadQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getCitizenDocument().getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + CommonUtils.formatDate(
                                                   D_MMM_YYYY,
                                                   element.getValue().getDateCreated()
                                               ))
                                               .build()).toList());
            //Handle Citizen Document management later
        }
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
        log.info("** Quarantine Doc ** {}", quarantineLegalDoc);

        String docTobeReviewed = formatDocumentTobeReviewed(submittedBy, quarantineLegalDoc);

        caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);
        caseDataUpdated.put(DOC_LABEL,LABEL_WITH_HINT);

        Document documentTobeReviewed = manageDocumentsService.getQuarantineDocumentForUploader(
            submittedBy, quarantineLegalDoc);
        caseDataUpdated.put(REVIEW_DOC, documentTobeReviewed);
        log.info(REVIEW_DOC + " {}", documentTobeReviewed);
    }

    public void processReviewDocument(Map<String, Object> caseDataUpdated, CaseData caseData, UUID uuid) {
        boolean isDocumentFound = false;
        Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElementOptional;
        if (YesNoNotSure.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())
            || YesNoNotSure.yes.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            if (null != caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList()) {
                quarantineLegalDocElementOptional =
                    getQuarantineDocumentById(
                        caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList(),
                        uuid
                    );
                if (quarantineLegalDocElementOptional.isPresent()) {
                    isDocumentFound = true;
                    processDocumentsAfterReviewNew(
                        caseData,
                        caseDataUpdated,
                        quarantineLegalDocElementOptional,
                        UserDetails.builder().roles(List.of(Roles.SOLICITOR.getValue())).build(),
                        SOLICITOR
                    );

                    removeDocumentFromQuarantineList(
                        caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList(),
                        uuid,
                        caseDataUpdated,
                        "legalProfQuarantineDocsList"
                    );
                }

            }
            if (!isDocumentFound && null != caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList()) {
                quarantineLegalDocElementOptional =
                    getQuarantineDocumentById(
                        caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList(),
                        uuid
                    );
                if (quarantineLegalDocElementOptional.isPresent()) {
                    isDocumentFound = true;
                    processDocumentsAfterReviewNew(
                        caseData,
                        caseDataUpdated,
                        quarantineLegalDocElementOptional,
                        UserDetails.builder().roles(List.of(CAFCASS)).build(),
                        CAFCASS
                    );
                    removeDocumentFromQuarantineList(
                        caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList(),
                        uuid,
                        caseDataUpdated,
                        "cafcassQuarantineDocsList"
                    );
                }

            }
            if (!isDocumentFound && null != caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList()) {
                quarantineLegalDocElementOptional =
                    getQuarantineDocumentById(
                        caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList(),
                        uuid
                    );
                if (quarantineLegalDocElementOptional.isPresent()) {
                    isDocumentFound = true;
                    processDocumentsAfterReviewNew(
                        caseData,
                        caseDataUpdated,
                        quarantineLegalDocElementOptional,
                        UserDetails.builder().roles(List.of(Roles.COURT_ADMIN.getValue())).build(),
                        COURT_STAFF
                    );
                    removeDocumentFromQuarantineList(
                        caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList(),
                        uuid,
                        caseDataUpdated,
                        "courtStaffQuarantineDocsList"
                    );
                }

            }
            if (!isDocumentFound && null != caseData.getDocumentManagementDetails().getCitizenUploadQuarantineDocsList()) {

                Optional<Element<UploadedDocuments>> quarantineCitizenDocElementOptional = caseData.getDocumentManagementDetails()
                    .getCitizenUploadQuarantineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
                if (quarantineCitizenDocElementOptional.isPresent()) {
                    Element<UploadedDocuments> quarantineCitizenDocElement = quarantineCitizenDocElementOptional.get();
                    //remove from quarantine
                    caseData.getDocumentManagementDetails().getCitizenUploadQuarantineDocsList().remove(
                        quarantineCitizenDocElement);

                    if (null != caseData.getReviewDocuments().getCitizenUploadDocListConfTab()) {
                        caseData.getReviewDocuments().getCitizenUploadDocListConfTab().add(quarantineCitizenDocElement);
                        caseDataUpdated.put(
                            CITIZEN_UPLOAD_DOC_LIST_CONF_TAB,
                            caseData.getReviewDocuments().getCitizenUploadDocListConfTab()
                        );
                    } else {
                        caseDataUpdated.put(CITIZEN_UPLOAD_DOC_LIST_CONF_TAB, List.of(quarantineCitizenDocElement));
                    }
                }
            }
            if (!isDocumentFound && isNotEmpty(caseData.getScannedDocuments())) {
                quarantineLegalDocElementOptional = getQuarantineBulkScanDocElement(caseData, uuid);
                if (quarantineLegalDocElementOptional.isPresent()) {
                    processDocumentsAfterReviewNew(
                        caseData,
                        caseDataUpdated,
                        quarantineLegalDocElementOptional,
                        UserDetails.builder().roles(List.of(Roles.BULK_SCAN.getValue())).build(),
                        BULK_SCAN
                    );
                    removeFromScannedDocumentListAfterReview(caseDataUpdated, caseData, uuid);
                }

            }
        }


    }

    private void removeDocumentFromQuarantineList(List<Element<QuarantineLegalDoc>> quarantineList, UUID uuid,
                                                  Map<String, Object> caseDataUpdated, String caseDataObjectToBeModified) {
        //new changes done
        Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElementOptional =
            getQuarantineDocumentById(quarantineList, uuid);
        if (quarantineLegalDocElementOptional.isPresent()) {
            Element<QuarantineLegalDoc> quarantineLegalDocElement = quarantineLegalDocElementOptional.get();
            //remove document from quarantine
            quarantineList.remove(quarantineLegalDocElement);
            caseDataUpdated.put(caseDataObjectToBeModified, quarantineList);
        }
    }

    private void processDocumentsAfterReviewNew(CaseData caseData, Map<String, Object> caseDataUpdated,
                                                Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElementOptional,
                                                UserDetails userDetails, String userRole) {
        if (quarantineLegalDocElementOptional.isPresent()) {
            QuarantineLegalDoc tempQuarantineDoe = quarantineLegalDocElementOptional.get().getValue();
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
    }

    private void removeFromScannedDocumentListAfterReview(Map<String, Object> caseDataUpdated,
                                                          CaseData caseData, UUID uuid) {
        caseData.getScannedDocuments().forEach(sc -> log.info("scanned doc list id {}", sc.getId()));
        log.info("UUID is {}", uuid);
        Optional<Element<ScannedDocument>> scannedDocumentElement = caseData.getScannedDocuments().stream()
            .filter(element -> element.getId().equals(uuid)).findFirst();
        if (scannedDocumentElement.isPresent()) {
            log.info("removing document from scanned docs");
            caseData.getScannedDocuments().remove(scannedDocumentElement.get());
            caseDataUpdated.put("scannedDocuments", caseData.getScannedDocuments());
            log.info("scanned documents after deletion {}", caseData.getScannedDocuments());
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
            .isConfidential(null) //bulk scan docs always go to confidential if decision is Yes
            .isRestricted(null) //fix to getRestrictedOrConfidentialKey=confidential
            .uploaderRole(BULK_SCAN);
    }

    public ResponseEntity<SubmittedCallbackResponse> getReviewResult(String authorisation, CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList())
            && (CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList()))
            && CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getCitizenUploadQuarantineDocsList())
            && CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList())) {
            coreCaseDataService.triggerEventWithAuthorisation(
                authorisation,
                JURISDICTION,
                CASE_TYPE,
                caseData.getId(),
                C100_CASE_TYPE.equals(caseData.getCaseTypeOfApplication())
                    ? "c100-all-docs-reviewed" : "fl401-all-docs-reviewed",
                null
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
        appendQuarantineDocumentDetails(reviewDetailsBuilder, quarantineDoc);

        //PRL-5006 bulk scan fields
        if (BULK_SCAN.equals(submittedBy)) {
            appendBulkScanDocumentDetails(reviewDetailsBuilder, quarantineDoc);
        }
        reviewDetailsBuilder.append("<br/>");
        return reviewDetailsBuilder.toString();
    }

    private void appendQuarantineDocumentDetails(StringBuilder reviewDetailsBuilder,
                                                 QuarantineLegalDoc quarantineDoc) {
        if (CommonUtils.isNotEmpty(quarantineDoc.getCategoryName())) {
            reviewDetailsBuilder.append(format(DOCUMENT_CATEGORY_LABEL,
                                               quarantineDoc.getCategoryName()));
        }
        if (CommonUtils.isNotEmpty(quarantineDoc.getNotes())) {
            reviewDetailsBuilder.append(format(DOCUMENT_COMMENTS_LABEL,
                                               quarantineDoc.getNotes()));
        }
        if (null != quarantineDoc.getIsConfidential()) {
            reviewDetailsBuilder.append(format(CONFIDENTIAL_INFO_LABEL,
                                               quarantineDoc.getIsConfidential().getDisplayedValue()));
        }
        if (null != quarantineDoc.getIsRestricted()) {
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
