package uk.gov.hmcts.reform.prl.services.reviewdocument;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
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
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
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
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_PROFESSIONAL;
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
            // TODO Handle Citizen Document management later
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
            // TODO Handle Scanned Document management later
        }
        caseDataUpdated.put("tempQuarantineDocumentList", tempQuarantineDocumentList);
        return dynamicListElements;
    }

    public void getReviewedDocumentDetailsNew(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (null != caseData.getReviewDocuments().getReviewDocsDynamicList() && null != caseData.getReviewDocuments()
            .getReviewDocsDynamicList().getValue()) {
            UUID uuid = UUID.fromString(caseData.getReviewDocuments().getReviewDocsDynamicList().getValue().getCode());

            List<Element<QuarantineLegalDoc>> tempQuarantineDocumentList = caseData.getDocumentManagementDetails().getTempQuarantineDocumentList();
            Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElement = Optional.empty();
            quarantineLegalDocElement = getQuarantineDocumentById(tempQuarantineDocumentList, uuid);

            updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(
                caseDataUpdated,
                quarantineLegalDocElement.get(),
                quarantineLegalDocElement.get().getValue().getUploaderRole()
            );

            log.info("** uuid ** {}", uuid);

            if (CollectionUtils.isNotEmpty(caseData.getScannedDocuments())) {
                Optional<Element<QuarantineLegalDoc>> quarantineBulkscanDocElement;
                quarantineBulkscanDocElement = Optional.of(
                    element(QuarantineLegalDoc.builder()
                                .url(caseData.getScannedDocuments().stream()
                                         .filter(element -> element.getId().equals(uuid))
                                         .collect(Collectors.toList()).stream().findFirst().map(Element::getValue).map(
                                        ScannedDocument::getUrl).orElse(null)).build()));
                if (quarantineBulkscanDocElement.isPresent()) {
                    updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(
                        caseDataUpdated,
                        quarantineBulkscanDocElement.get(),
                        BULK_SCAN
                    );
                }
            }
        }
    }

    private void updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(Map<String, Object> caseDataUpdated,
                                                                      Element<QuarantineLegalDoc> quarantineDocElement,
                                                                      String submittedBy) {

        QuarantineLegalDoc quarantineLegalDoc = quarantineDocElement.getValue();
        log.info("** Quarantine Doc ** {}", quarantineLegalDoc);

        String docTobeReviewed = formatDocumentTobeReviewed(
            submittedBy,
            quarantineLegalDoc.getCategoryName(),
            quarantineLegalDoc.getNotes(),
            quarantineLegalDoc.getIsRestricted(),
            quarantineLegalDoc.getIsConfidential(),
            quarantineLegalDoc.getRestrictedDetails()
        );

        caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);
        caseDataUpdated.put(DOC_LABEL,LABEL_WITH_HINT);

        switch (submittedBy) {
            case LEGAL_PROFESSIONAL, CAFCASS, COURT_STAFF:
                Document documentTobeReviewed = manageDocumentsService.getQuarantineDocumentForUploader(
                    quarantineLegalDoc.getUploaderRole(),
                    quarantineLegalDoc
                );
                caseDataUpdated.put(REVIEW_DOC, documentTobeReviewed);
                log.info(REVIEW_DOC + " {}", documentTobeReviewed);
                break;
            case BULK_SCAN:
                caseDataUpdated.put(REVIEW_DOC, quarantineLegalDoc.getUrl());
                log.info(REVIEW_DOC + " {}", quarantineLegalDoc.getUrl());
                break;
            default:
        }
    }

    private void uploadDocForConfOrDocTab(Map<String, Object> caseDataUpdated,
                                         List<Element<QuarantineLegalDoc>> quarantineDocsList,
                                         UUID uuid,
                                         boolean isReviewDecisionYes,
                                         List<Element<QuarantineLegalDoc>> uploadDocListConfOrDocTab,
                                         String uploadDocListConfOrDocTabKey,
                                         String uploadedBy) {

        Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElementOptional =
            getQuarantineDocumentById(quarantineDocsList, uuid);
        if (quarantineLegalDocElementOptional.isPresent()) {
            Element<QuarantineLegalDoc> quarantineLegalDocElement = quarantineLegalDocElementOptional.get();
            //remove document from quarantine
            quarantineDocsList.remove(quarantineLegalDocElement);

            QuarantineLegalDoc uploadDoc = DocumentUtils.getQuarantineUploadDocument(
                isReviewDecisionYes ? CONFIDENTIAL_CATEGORY_ID : quarantineLegalDocElement.getValue().getCategoryId(),
                getQuarantineDocument(uploadedBy, quarantineLegalDocElement.getValue()), objectMapper
            );

            uploadDoc = manageDocumentsService.addQuarantineDocumentFields(
                uploadDoc,
                quarantineLegalDocElement.getValue()
            );

            if (null != uploadDocListConfOrDocTab) {
                uploadDocListConfOrDocTab.add(element(uploadDoc));
                caseDataUpdated.put(
                    uploadDocListConfOrDocTabKey,
                    uploadDocListConfOrDocTab
                );
            } else {
                caseDataUpdated.put(uploadDocListConfOrDocTabKey, List.of(element(uploadDoc)));
            }
        }
    }

    private Document getQuarantineDocument(String uploadedBy,
                                           QuarantineLegalDoc quarantineLegalDoc) {
        return switch (uploadedBy) {
            case SOLICITOR -> quarantineLegalDoc.getDocument();
            case CAFCASS -> quarantineLegalDoc.getCafcassQuarantineDocument();
            case COURT_STAFF -> quarantineLegalDoc.getCourtStaffQuarantineDocument();
            case BULK_SCAN -> quarantineLegalDoc.getUrl();
            default -> null;
        };
    }

    public void processReviewDocument(Map<String, Object> caseDataUpdated, CaseData caseData, UUID uuid) {

        Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElementOptional = null;
        String userRole = null;
        if (YesNoNotSure.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())
            || YesNoNotSure.yes.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            if (null != caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList()) {
                quarantineLegalDocElementOptional =
                    getQuarantineDocumentById(
                        caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList(),
                        uuid
                    );
                userRole = SOLICITOR;

                processDocumentsAfterReviewNew(
                    caseData,
                    caseDataUpdated,
                    quarantineLegalDocElementOptional,
                    UserDetails.builder().roles(List.of(Roles.SOLICITOR.getValue())).build(),
                    userRole
                );

                removeDocumentFromQuarantineList(
                    caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList(),
                    uuid,
                    caseDataUpdated,
                    "legalProfQuarantineDocsList"
                );
            } else if (null != caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList()) {
                quarantineLegalDocElementOptional =
                    getQuarantineDocumentById(
                        caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList(),
                        uuid
                    );
                userRole = CAFCASS;
                processDocumentsAfterReviewNew(
                    caseData,
                    caseDataUpdated,
                    quarantineLegalDocElementOptional,
                    UserDetails.builder().roles(List.of(CAFCASS)).build(),
                    userRole
                );
                removeDocumentFromQuarantineList(
                    caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList(),
                    uuid,
                    caseDataUpdated,
                    "cafcassQuarantineDocsList"
                );
            } else if (null != caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList()) {
                quarantineLegalDocElementOptional =
                    getQuarantineDocumentById(
                        caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList(),
                        uuid
                    );
                userRole = COURT_STAFF;
                processDocumentsAfterReviewNew(
                    caseData,
                    caseDataUpdated,
                    quarantineLegalDocElementOptional,
                    UserDetails.builder().roles(List.of(Roles.COURT_ADMIN.getValue())).build(),
                    userRole
                );
                removeDocumentFromQuarantineList(
                    caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList(),
                    uuid,
                    caseDataUpdated,
                    "courtStaffQuarantineDocsList"
                );
            } else if (null != caseData.getDocumentManagementDetails().getCitizenUploadQuarantineDocsList()) {

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
            } else if (null != caseData.getScannedDocuments()) {
                uploadDocForConfOrDocTab(
                    caseDataUpdated, // casedataUpdated
                    convertScannedDocumentsToQuarantineDocList(caseData.getScannedDocuments(), uuid),
                    uuid,
                    true,
                    caseData.getReviewDocuments().getBulkScannedDocListConfTab(),
                    BULKSCAN_UPLOAD_DOC_LIST_CONF_TAB,
                    BULK_SCAN
                );
                removeFromScannedDocumentListAfterReview(caseData, uuid);
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

    private void removeFromScannedDocumentListAfterReview(
        CaseData caseData, UUID uuid) {
        caseData.getScannedDocuments().forEach(sc -> log.info("scanned doc list id {}", sc.getId()));
        log.info("UUID is {}", uuid);
        Optional<Element<ScannedDocument>> scannedDocumentElement = caseData.getScannedDocuments().stream()
            .filter(element -> element.getId().equals(uuid)).findFirst();
        if (scannedDocumentElement.isPresent()) {
            log.info("removing document from scanned docs");
            caseData.getScannedDocuments().remove(scannedDocumentElement.get());
            log.info("scanned documents after deletion {}", caseData.getScannedDocuments());
        }
    }

    private List<Element<QuarantineLegalDoc>> convertScannedDocumentsToQuarantineDocList(
        List<Element<ScannedDocument>> scannedDocumentElements, UUID uuid) {
        return scannedDocumentElements.stream().map(
                scannedDocumentElement -> {
                    ScannedDocument scannedDocument = scannedDocumentElement.getValue();
                    return element(uuid, QuarantineLegalDoc.builder()
                            .fileName(scannedDocument.getFileName())
                            .controlNumber(scannedDocument.getControlNumber())
                            .type(scannedDocument.getType())
                            .subtype(scannedDocument.getSubtype())
                            .exceptionRecordReference(scannedDocument.getExceptionRecordReference())
                            .url(scannedDocument.getUrl())
                            //.document(scannedDocument.getUrl())
                            .scannedDate(scannedDocument.getScannedDate())
                            .deliveryDate(scannedDocument.getDeliveryDate())
                            .build());
                }).collect(Collectors.toList());
    }

    public ResponseEntity<SubmittedCallbackResponse> getReviewResult(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getLegalProfQuarantineDocsList())
            && (CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getCourtStaffQuarantineDocsList()))
            && CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getCitizenUploadQuarantineDocsList())
            && CollectionUtils.isEmpty(caseData.getDocumentManagementDetails().getCafcassQuarantineDocsList())) {
            coreCaseDataService.triggerEvent(
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
                                              String category,
                                              String notes,
                                              YesOrNo isRestricted,
                                              YesOrNo isConfidential,
                                              String restrictedDetails) {
        if (BULK_SCAN.equals(submittedBy)) {
            return String.join(
                format(SUBMITTED_BY_LABEL, submittedBy)
            );
        }
        StringBuilder reviewDetailsBuilder = new StringBuilder();
        reviewDetailsBuilder.append(format(SUBMITTED_BY_LABEL, submittedBy));
        reviewDetailsBuilder.append(format(DOCUMENT_CATEGORY_LABEL, category));
        if (StringUtils.isNotEmpty(notes)) {
            reviewDetailsBuilder.append(format(DOCUMENT_COMMENTS_LABEL, notes));
        }
        if (null != isConfidential) {
            reviewDetailsBuilder.append(format(CONFIDENTIAL_INFO_LABEL, isConfidential.getDisplayedValue()));
        }
        if (null != isRestricted) {
            reviewDetailsBuilder.append(format(RESTRICTED_INFO_LABEL, isRestricted.getDisplayedValue()));
        }
        if (StringUtils.isNotEmpty(restrictedDetails)) {
            reviewDetailsBuilder.append(format(RESTRICTION_REASON_LABEL, restrictedDetails));
        }
        reviewDetailsBuilder.append("<br/>");
        return reviewDetailsBuilder.toString();
    }

}
