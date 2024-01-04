package uk.gov.hmcts.reform.prl.services.reviewdocument;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
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
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.BULK_SCAN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONFIDENTIAL_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DATE_TIME_PATTERN;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.HYPHEN_SEPARATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_PROFESSIONAL;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.RESTRICTED_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
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

    @Value("${case_document_am.url}")
    private String myurl;


    private final ObjectMapper objectMapper;
    public static final String DOCUMENT_SUCCESSFULLY_REVIEWED = "# Document successfully reviewed";
    public static final String DOCUMENT_IN_REVIEW = "# Document review in progress";
    private static final String REVIEW_YES = "### You have successfully reviewed this document"
        + System.lineSeparator()
        + "This document can only be seen by court staff, Cafcass and the judiciary. "
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
    public static final String DOCUMENT_UUID_REGEX = "\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}";
    public static final String CONFIDENTIAL = "Confidential_";
    public static final String CASE_DETAILS_URL = "/cases/case-details/";
    public static final String SEND_AND_REPLY_URL = "/trigger/sendOrReplyToMessages/sendOrReplyToMessages1";
    public static final String SEND_AND_REPLY_MESSAGE_LABEL = "\">Send and reply to messages</a>";

    public List<DynamicListElement> getDynamicListElements(CaseData caseData) {
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        if (isNotEmpty(caseData.getLegalProfQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getLegalProfQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getDocument().getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(DATE_TIME_PATTERN,
                                                                                   element.getValue().getDocumentUploadedDate()))
                                               .build())
                                           .toList());
        }
        //added for cafcass
        if (isNotEmpty(caseData.getCafcassQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getCafcassQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getCafcassQuarantineDocument().getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(DATE_TIME_PATTERN,
                                                                                   element.getValue().getDocumentUploadedDate()))
                                               .build())
                                           .toList());
        }
        //court staff
        if (isNotEmpty(caseData.getCourtStaffQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getCourtStaffQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getCourtStaffQuarantineDocument().getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(DATE_TIME_PATTERN,
                                                                                   element.getValue().getDocumentUploadedDate()))
                                               .build())
                                           .toList());
        }
        if (isNotEmpty(caseData.getCitizenUploadQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getCitizenUploadQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getCitizenDocument().getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + CommonUtils.formatDate(
                                                   D_MMM_YYYY,
                                                   element.getValue().getDateCreated()
                                               ))
                                               .build()).toList());
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
        }
        return dynamicListElements;
    }

    public void getReviewedDocumentDetails(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (null != caseData.getReviewDocuments().getReviewDocsDynamicList() && null != caseData.getReviewDocuments()
            .getReviewDocsDynamicList().getValue()) {
            UUID uuid = UUID.fromString(caseData.getReviewDocuments().getReviewDocsDynamicList().getValue().getCode());
            log.info("** uuid ** {}", uuid);
            //solicitor uploaded quarantine doc
            Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElement = Optional.empty();
            if (isNotEmpty(caseData.getLegalProfQuarantineDocsList())) {
                quarantineLegalDocElement = getQuarantineDocumentById(caseData.getLegalProfQuarantineDocsList(), uuid);
            }
            //cafcass quarantine doc
            Optional<Element<QuarantineLegalDoc>> cafcassQuarantineDocElement = Optional.empty();
            if (quarantineLegalDocElement.isEmpty() && isNotEmpty(caseData.getCafcassQuarantineDocsList())) {
                cafcassQuarantineDocElement = getQuarantineDocumentById(caseData.getCafcassQuarantineDocsList(), uuid);
            }
            //court staff
            Optional<Element<QuarantineLegalDoc>> courtStaffQuarantineDocElement = Optional.empty();
            if (cafcassQuarantineDocElement.isEmpty() && isNotEmpty(caseData.getCourtStaffQuarantineDocsList())) {
                courtStaffQuarantineDocElement = getQuarantineDocumentById(
                    caseData.getCourtStaffQuarantineDocsList(),
                    uuid
                );
            }
            updateReviewdocs(
                caseData,
                caseDataUpdated,
                uuid,
                quarantineLegalDocElement,
                cafcassQuarantineDocElement,
                courtStaffQuarantineDocElement
            );
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

    private void updateReviewdocs(CaseData caseData, Map<String, Object> caseDataUpdated, UUID uuid,
                                  Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElement,
                                  Optional<Element<QuarantineLegalDoc>> cafcassQuarantineDocElement,
                                  Optional<Element<QuarantineLegalDoc>> courtStaffQuarantineDocElement) {
        Optional<Element<UploadedDocuments>> quarantineCitizenDocElement = Optional.empty();
        if (null != caseData.getCitizenUploadQuarantineDocsList()) {
            quarantineCitizenDocElement = caseData.getCitizenUploadQuarantineDocsList().stream()
                .filter(element -> element.getId().equals(uuid)).findFirst();
        }

        if (quarantineLegalDocElement.isPresent()) {
            updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(
                caseDataUpdated,
                quarantineLegalDocElement.get(),
                LEGAL_PROFESSIONAL
            );
        } else if (cafcassQuarantineDocElement.isPresent()) {
            updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(
                caseDataUpdated,
                cafcassQuarantineDocElement.get(),
                CAFCASS
            );
        } else if (courtStaffQuarantineDocElement.isPresent()) {
            updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(
                caseDataUpdated,
                courtStaffQuarantineDocElement.get(),
                COURT_STAFF
            );
        } else if (quarantineCitizenDocElement.isPresent()) {
            UploadedDocuments document = quarantineCitizenDocElement.get().getValue();
            log.info("** citizen document ** {}", document);

            String docTobeReviewed = formatDocumentTobeReviewed(
                document.getPartyName(),
                document.getDocumentType(),
                "",
                null,
                null,
                ""
            );

            caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);
            caseDataUpdated.put(DOC_LABEL,LABEL_WITH_HINT);
            caseDataUpdated.put(REVIEW_DOC, document.getCitizenDocument());
        }
        if (isNotEmpty(caseData.getScannedDocuments())) {
            Optional<Element<QuarantineLegalDoc>> quarantineBulkscanDocElement;
            quarantineBulkscanDocElement = Optional.of(
                element(QuarantineLegalDoc.builder()
                            .url(caseData.getScannedDocuments().stream()
                                     .filter(element -> element.getId().equals(uuid))
                                     .toList().stream().findFirst().map(Element::getValue).map(
                                    ScannedDocument::getUrl).orElse(null)).build()));
            updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(
                caseDataUpdated,
                quarantineBulkscanDocElement.get(),
                BULK_SCAN
            );
        }
    }

    private void updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(Map<String, Object> caseDataUpdated,
                                                                      Element<QuarantineLegalDoc> quarantineDocElement,
                                                                      String submittedBy) {

        QuarantineLegalDoc document = quarantineDocElement.getValue();
        log.info("** Quarantine Doc ** {}", document);

        String docTobeReviewed = formatDocumentTobeReviewed(
            submittedBy,
            document.getCategoryName(),
            document.getNotes(),
            document.getIsRestricted(),
            document.getIsConfidential(),
            document.getRestrictedDetails()
        );

        caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);
        caseDataUpdated.put(DOC_LABEL,LABEL_WITH_HINT);

        switch (submittedBy) {
            case LEGAL_PROFESSIONAL:
                caseDataUpdated.put(REVIEW_DOC, document.getDocument());
                log.info(REVIEW_DOC + " {}", document.getDocument());
                break;
            case CAFCASS:
                caseDataUpdated.put(REVIEW_DOC, document.getCafcassQuarantineDocument());
                log.info(REVIEW_DOC + " {}", document.getCafcassQuarantineDocument());
                break;
            case COURT_STAFF:
                caseDataUpdated.put(REVIEW_DOC, document.getCourtStaffQuarantineDocument());
                log.info(REVIEW_DOC + " {}", document.getCourtStaffQuarantineDocument());
                break;
            case BULK_SCAN:
                caseDataUpdated.put(REVIEW_DOC, document.getUrl());
                log.info(REVIEW_DOC + " {}", document.getUrl());
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

            uploadDoc = addQuarantineDocumentFields(
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
        if (YesNoNotSure.yes.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            forReviewDecisionYes(caseData, caseDataUpdated, uuid);
        } else if (YesNoNotSure.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            forReviewDecisionNo(caseData, caseDataUpdated, uuid);
        }
        caseDataUpdated.put("legalProfQuarantineDocsList", caseData.getLegalProfQuarantineDocsList());
        caseDataUpdated.put("cafcassQuarantineDocsList", caseData.getCafcassQuarantineDocsList());
        caseDataUpdated.put("citizenUploadQuarantineDocsList", caseData.getCitizenUploadQuarantineDocsList());
        caseDataUpdated.put("courtStaffQuarantineDocsList", caseData.getCourtStaffQuarantineDocsList());
        caseDataUpdated.put("scannedDocuments", caseData.getScannedDocuments());
    }

    private void forReviewDecisionYes(CaseData caseData, Map<String, Object> caseDataUpdated, UUID uuid) {
        log.info("QQQQQQQQQQQQ {}",caseData.getLegalProfQuarantineDocsList());

        if (null != caseData.getLegalProfQuarantineDocsList()) {
            moveDocumentToConfidentialTab(caseDataUpdated,
                                          caseData,
                                          caseData.getLegalProfQuarantineDocsList(),
                                          uuid,
                                          SOLICITOR);
        }
        //cafcass
        if (null != caseData.getCafcassQuarantineDocsList()) {
            moveDocumentToConfidentialTab(caseDataUpdated,
                                          caseData,
                                          caseData.getCafcassQuarantineDocsList(),
                                          uuid,
                                          CAFCASS);
        }
        //court staff
        if (null != caseData.getCourtStaffQuarantineDocsList()) {
            moveDocumentToConfidentialTab(caseDataUpdated,
                                          caseData,
                                          caseData.getCourtStaffQuarantineDocsList(),
                                          uuid,
                                          COURT_STAFF);
        }
        //NEED TO BE REVISITED
        if (null != caseData.getCitizenUploadQuarantineDocsList()) {
            Optional<Element<UploadedDocuments>> quarantineCitizenDocElementOptional = caseData.getCitizenUploadQuarantineDocsList().stream()
                .filter(element -> element.getId().equals(uuid)).findFirst();
            if (quarantineCitizenDocElementOptional.isPresent()) {
                Element<UploadedDocuments> quarantineCitizenDocElement = quarantineCitizenDocElementOptional.get();
                //remove from quarantine
                caseData.getCitizenUploadQuarantineDocsList().remove(quarantineCitizenDocElement);

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
        //NEED TO BE REVISITED
        if (null != caseData.getScannedDocuments()) {

            uploadDocForConfOrDocTab(
                caseDataUpdated,
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

    private void forReviewDecisionNo(CaseData caseData, Map<String, Object> caseDataUpdated, UUID uuid) {

        if (null != caseData.getLegalProfQuarantineDocsList()) {

            uploadDocForConfOrDocTab(
                caseDataUpdated,
                caseData.getLegalProfQuarantineDocsList(),
                uuid,
                false,
                caseData.getReviewDocuments().getLegalProfUploadDocListDocTab(),
                LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB,
                SOLICITOR
            );

            log.info("*** legal prof docs tab ** {}", caseDataUpdated.get(LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB));
        }
        //cafcass
        if (null != caseData.getCafcassQuarantineDocsList()) {
            uploadDocForConfOrDocTab(
                caseDataUpdated,
                caseData.getCafcassQuarantineDocsList(),
                uuid,
                false,
                caseData.getReviewDocuments().getCafcassUploadDocListDocTab(),
                CAFCASS_UPLOAD_DOC_LIST_DOC_TAB,
                CAFCASS
            );

            log.info("*** cafcass docs tab ** {}", caseDataUpdated.get(CAFCASS_UPLOAD_DOC_LIST_DOC_TAB));
        }
        //court staff
        if (null != caseData.getCourtStaffQuarantineDocsList()) {
            uploadDocForConfOrDocTab(
                caseDataUpdated,
                caseData.getCourtStaffQuarantineDocsList(),
                uuid,
                false,
                caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab(),
                COURT_STAFF_UPLOAD_DOC_LIST_DOC_TAB,
                COURT_STAFF
            );

            log.info("*** court staff docs tab ** {}", caseDataUpdated.get(COURT_STAFF_UPLOAD_DOC_LIST_DOC_TAB));
        }
        if (null != caseData.getCitizenUploadQuarantineDocsList()) {
            Optional<Element<UploadedDocuments>> quarantineCitizenDocElementOptional = caseData.getCitizenUploadQuarantineDocsList().stream()
                .filter(element -> element.getId().equals(uuid)).findFirst();
            if (quarantineCitizenDocElementOptional.isPresent()) {
                Element<UploadedDocuments> quarantineCitizenDocElement = quarantineCitizenDocElementOptional.get();
                //remove from quarantine
                caseData.getCitizenUploadQuarantineDocsList().remove(quarantineCitizenDocElement);

                if (null != caseData.getReviewDocuments().getCitizenUploadedDocListDocTab()) {
                    caseData.getReviewDocuments().getCitizenUploadedDocListDocTab().add(quarantineCitizenDocElement);
                    caseDataUpdated.put(
                        CITIZEN_UPLOADED_DOC_LIST_DOC_TAB,
                        caseData.getReviewDocuments().getCitizenUploadedDocListDocTab()
                    );
                } else {
                    caseDataUpdated.put(CITIZEN_UPLOADED_DOC_LIST_DOC_TAB, List.of(quarantineCitizenDocElement));
                }
            }
            log.info("*** citizen docs tab ** {}", caseDataUpdated.get(CITIZEN_UPLOADED_DOC_LIST_DOC_TAB));
        }
        // for bulk scan documents
        if (null != caseData.getScannedDocuments()) {
            uploadDocForConfOrDocTab(
                caseDataUpdated,
                convertScannedDocumentsToQuarantineDocList(caseData.getScannedDocuments(), uuid),
                uuid,
                false,
                caseData.getReviewDocuments().getBulkScannedDocListDocTab(),
                BULKSCAN_UPLOADED_DOC_LIST_DOC_TAB,
                BULK_SCAN
            );
            removeFromScannedDocumentListAfterReview(caseData, uuid);
            log.info("*** Bulk scan docs tab ** {}", caseDataUpdated.get(BULKSCAN_UPLOADED_DOC_LIST_DOC_TAB));
        }
    }

    public ResponseEntity<SubmittedCallbackResponse> getReviewResult(CaseData caseData) {
        if (CollectionUtils.isEmpty(caseData.getLegalProfQuarantineDocsList())
            && (CollectionUtils.isEmpty(caseData.getCourtStaffQuarantineDocsList()))
            && CollectionUtils.isEmpty(caseData.getCitizenUploadQuarantineDocsList())
            && CollectionUtils.isEmpty(caseData.getCafcassQuarantineDocsList())) {
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

    private QuarantineLegalDoc addQuarantineDocumentFields(QuarantineLegalDoc legalProfUploadDoc,
                                                           QuarantineLegalDoc quarantineLegalDoc) {

        return legalProfUploadDoc.toBuilder()
            .documentParty(quarantineLegalDoc.getDocumentParty())
            .documentUploadedDate(quarantineLegalDoc.getDocumentUploadedDate())
            .notes(quarantineLegalDoc.getNotes())
            .categoryId(quarantineLegalDoc.getCategoryId())
            .categoryName(quarantineLegalDoc.getCategoryName())
            .fileName(quarantineLegalDoc.getFileName())
            .controlNumber(quarantineLegalDoc.getControlNumber())
            .type(quarantineLegalDoc.getType())
            .subtype(quarantineLegalDoc.getSubtype())
            .exceptionRecordReference(quarantineLegalDoc.getExceptionRecordReference())
            .url(legalProfUploadDoc.getConfidentialDocument() == null ? quarantineLegalDoc.getUrl() : null)
            .scannedDate(quarantineLegalDoc.getScannedDate())
            .deliveryDate(quarantineLegalDoc.getDeliveryDate())
            //PRL-4320 - Manage documents redesign
            .isConfidential(quarantineLegalDoc.getIsConfidential())
            .isRestricted(quarantineLegalDoc.getIsRestricted())
            .notes(quarantineLegalDoc.getRestrictedDetails())
            .uploadedBy(quarantineLegalDoc.getUploadedBy())
            .uploadedByIdamId(quarantineLegalDoc.getUploadedByIdamId())
            .build();
    }

    private void moveDocumentToConfidentialTab(Map<String, Object> caseDataUpdated,
                                               CaseData caseData,
                                               List<Element<QuarantineLegalDoc>> quarantineDocsList,
                                               UUID uuid,
                                               String uploadedBy) {
        Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElementOptional =
            getQuarantineDocumentById(quarantineDocsList, uuid);
        if (quarantineLegalDocElementOptional.isPresent()) {
            Element<QuarantineLegalDoc> quarantineLegalDocElement = quarantineLegalDocElementOptional.get();
            //remove document from quarantine
            quarantineDocsList.remove(quarantineLegalDocElement);

            String restrictedOrConfidentialKey = getRestrictedOrConfidentialKey(quarantineLegalDocElement.getValue());
            QuarantineLegalDoc uploadDoc = downloadAndDeleteDocument(uploadedBy,
                                                                     quarantineLegalDocElement);
            uploadDoc = addQuarantineDocumentFields(uploadDoc, quarantineLegalDocElement.getValue());

            moveToConfidentialOrRestricted(caseDataUpdated,
                                           CONFIDENTIAL_DOCUMENTS.equals(restrictedOrConfidentialKey)
                                               ? caseData.getReviewDocuments().getConfidentialDocuments()
                                               : caseData.getReviewDocuments().getRestrictedDocuments(),
                                           uploadDoc,
                                           restrictedOrConfidentialKey);
        }
    }

    private QuarantineLegalDoc downloadAndDeleteDocument(String uploadedBy,
                                                         Element<QuarantineLegalDoc> quarantineLegalDocElement) {
        try {
            log.info("MYURLLL--> {}",myurl);
            Document document = getQuarantineDocument(uploadedBy, quarantineLegalDocElement.getValue());
            UUID documentId = UUID.fromString(getDocumentId(document.getDocumentUrl()));
            log.info(" DocumentId found {}", documentId);
            Document newUploadedDocument = getNewUploadedDocument(document,
                                                                  documentId);

            log.info("document uploaded {}", newUploadedDocument);
            if (null != newUploadedDocument) {
                caseDocumentClient.deleteDocument(systemUserService.getSysUserToken(),
                                                  authTokenGenerator.generate(),
                                                  documentId, true);
                log.info("deleted document {}", documentId);

                return DocumentUtils.getQuarantineUploadDocument(
                    quarantineLegalDocElement.getValue().getCategoryId(),
                    newUploadedDocument,
                    objectMapper
                );
            } else {
                throw new IllegalStateException("Failed to move document to confidential tab please retry");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to move document to confidential tab please retry", e);
        }
    }

    private Document getNewUploadedDocument(Document document,
                                            UUID documentId) {
        byte[] docData;
        Document newUploadedDocument = null;
        try {
            String sysUserToken = systemUserService.getSysUserToken();
            String serviceToken = authTokenGenerator.generate();
            Resource resource = caseDocumentClient.getDocumentBinary(sysUserToken, serviceToken,
                                                                     documentId
            ).getBody();
            docData = IOUtils.toByteArray(resource.getInputStream());
            UploadResponse uploadResponse = caseDocumentClient.uploadDocuments(
                sysUserToken,
                serviceToken,
                PrlAppsConstants.CASE_TYPE,
                PrlAppsConstants.JURISDICTION,
                List.of(
                    new InMemoryMultipartFile(
                        SOA_MULTIPART_FILE,
                        CONFIDENTIAL + document.getDocumentFileName(),
                        APPLICATION_PDF_VALUE,
                        docData
                    ))
            );
            newUploadedDocument = Document.buildFromDocument(uploadResponse.getDocuments().get(0));
        } catch (Exception ex) {
            log.error("Failed to upload new document {}", ex.getMessage());
        }
        return newUploadedDocument;
    }

    private String getDocumentId(String url) {
        Pattern pairRegex = Pattern.compile(DOCUMENT_UUID_REGEX);
        Matcher matcher = pairRegex.matcher(url);
        String documentId = "";
        if (matcher.find()) {
            documentId = matcher.group(0);
        }
        log.info("document id {}", documentId);
        return documentId;
    }

    /**
     * Based on user input documents will be moved either to confidential or restricted documents.
     * ifConfidential && isRestricted - RESTRICTED
     * !ifConfidential && isRestricted - RESTRICTED
     * ifConfidential && !isRestricted - CONFIDENTIAL
     */
    private String getRestrictedOrConfidentialKey(QuarantineLegalDoc quarantineLegalDoc) {
        if (YesOrNo.Yes.equals(quarantineLegalDoc.getIsConfidential())
            && YesOrNo.No.equals(quarantineLegalDoc.getIsRestricted())) {
            return CONFIDENTIAL_DOCUMENTS;
        } else {
            return RESTRICTED_DOCUMENTS;
        }
    }

    private void moveToConfidentialOrRestricted(Map<String, Object> caseDataUpdated,
                                                List<Element<QuarantineLegalDoc>> confidentialOrRestrictedDocuments,
                                                QuarantineLegalDoc uploadDoc,
                                                String confidentialOrRestrictedKey) {
        if (null != confidentialOrRestrictedDocuments) {
            confidentialOrRestrictedDocuments.add(element(uploadDoc));
            confidentialOrRestrictedDocuments.sort(Comparator.comparing(doc -> doc.getValue().getDocumentUploadedDate(), Comparator.reverseOrder()));
            caseDataUpdated.put(confidentialOrRestrictedKey, confidentialOrRestrictedDocuments);
        } else {
            caseDataUpdated.put(confidentialOrRestrictedKey, List.of(element(uploadDoc)));
        }
    }
}
