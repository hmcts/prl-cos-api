package uk.gov.hmcts.reform.prl.services.reviewdocument;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.ccd.document.am.feign.CaseDocumentClient;
import uk.gov.hmcts.reform.ccd.document.am.model.UploadResponse;
import uk.gov.hmcts.reform.ccd.document.am.util.InMemoryMultipartFile;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOA_MULTIPART_FILE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.utils.CommonUtils.formatDateTime;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewDocumentService {

    public static final String CONFIDENTIAL = "Confidential_";

    private final CoreCaseDataService coreCaseDataService;

    private final CaseDocumentClient caseDocumentClient;

    private final AuthTokenGenerator authTokenGenerator;

    private final SystemUserService systemUserService;

    public static final String DOCUMENT_UUID_REGEX = "\\p{XDigit}{8}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{4}-\\p{XDigit}{12}";
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
        + "If you are not sure, you can use Send and reply to messages to get further information about whether "
        + "the document needs to be restricted.";
    public static final String SUBMITTED_BY_LABEL =
        "<h3 class='govuk-heading-s'>Submitted by</h3><label class='govuk-label' for='more-detail'><li>%s</li></label>";
    public static final String DOCUMENT_CATEGORY_LABEL =
        "<h3 class='govuk-heading-s'>Document category</h3><label class='govuk-label' for='more-detail'><li>%s</li></label>";
    public static final String DOCUMENT_COMMENTS_LABEL =
        "<h3 class='govuk-heading-s'>Details/comments</h3><label class='govuk-label' for='more-detail'><li>%s</li></label>";
    public static final String DOC_TO_BE_REVIEWED = "docToBeReviewed";
    public static final String REVIEW_DOC = "reviewDoc";
    public static final String LEGAL_PROF_UPLOAD_DOC_LIST_CONF_TAB = "legalProfUploadDocListConfTab";
    public static final String CAFCASS_UPLOAD_DOC_LIST_CONF_TAB = "cafcassUploadDocListConfTab";
    public static final String COURT_STAFF_UPLOAD_DOC_LIST_CONF_TAB = "courtStaffUploadDocListConfTab";
    public static final String CITIZEN_UPLOAD_DOC_LIST_CONF_TAB = "citizenUploadDocListConfTab";
    public static final String BULKSCAN_UPLOAD_DOC_LIST_CONF_TAB = "bulkScannedDocListConfTab";
    public static final String LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB = "legalProfUploadDocListDocTab";
    public static final String CAFCASS_UPLOAD_DOC_LIST_DOC_TAB = "cafcassUploadDocListDocTab";
    public static final String COURT_STAFF_UPLOAD_DOC_LIST_DOC_TAB = "courtStaffUploadDocListDocTab";
    public static final String CITIZEN_UPLOADED_DOC_LIST_DOC_TAB = "citizenUploadedDocListDocTab";
    public static final String BULKSCAN_UPLOADED_DOC_LIST_DOC_TAB = "bulkScannedDocListDocTab";

    public List<DynamicListElement> getDynamicListElements(CaseData caseData) {
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseData.getLegalProfQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getLegalProfQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getDocument().getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
        }
        //added for cafcass
        if (CollectionUtils.isNotEmpty(caseData.getCafcassQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getCafcassQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getCafcassQuarantineDocument().getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
        }
        //court staff
        if (CollectionUtils.isNotEmpty(caseData.getCourtStaffQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getCourtStaffQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getCourtStaffQuarantineDocument().getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + formatDateTime(
                                                   DATE_TIME_PATTERN,
                                                   element.getValue().getDocumentUploadedDate()
                                               ))
                                               .build())
                                           .toList());
        }
        if (CollectionUtils.isNotEmpty(caseData.getCitizenUploadQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getCitizenUploadQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getCitizenDocument().getDocumentFileName()
                                                          + HYPHEN_SEPARATOR + CommonUtils.formatDate(
                                                   D_MMM_YYYY,
                                                   element.getValue().getDateCreated()
                                               ))
                                               .build()).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(caseData.getScannedDocuments())) {
            dynamicListElements.addAll(caseData.getScannedDocuments().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getFileName()
                                                          + HYPHEN_SEPARATOR
                                                          + CommonUtils.formatDate(
                                                   D_MMM_YYYY,
                                                   element.getValue().getScannedDate().toLocalDate()
                                               ))
                                               .build()).collect(Collectors.toList()));
        }
        return dynamicListElements;
    }

    public void getReviewedDocumentDetails(CaseData caseData, Map<String, Object> caseDataUpdated) {
        if (null != caseData.getReviewDocuments().getReviewDocsDynamicList() && null != caseData.getReviewDocuments()
            .getReviewDocsDynamicList().getValue()) {
            UUID uuid = UUID.fromString(caseData.getReviewDocuments().getReviewDocsDynamicList().getValue().getCode());
            log.info("** uuid ** {}", uuid);
            Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElement = Optional.empty();
            if (null != caseData.getLegalProfQuarantineDocsList()) {
                quarantineLegalDocElement = getQuarantineDocumentById(caseData.getLegalProfQuarantineDocsList(), uuid);
            }
            //cafcass quarantine doc
            Optional<Element<QuarantineLegalDoc>> cafcassQuarantineDocElement = Optional.empty();
            if (null != caseData.getCafcassQuarantineDocsList()) {
                cafcassQuarantineDocElement = getQuarantineDocumentById(caseData.getCafcassQuarantineDocsList(), uuid);
            }
            //court staff
            Optional<Element<QuarantineLegalDoc>> courtStaffQuarantineDocElement = Optional.empty();
            if (null != caseData.getCourtStaffQuarantineDocsList()) {
                courtStaffQuarantineDocElement = getQuarantineDocumentById(
                    caseData.getCourtStaffQuarantineDocsList(),
                    uuid
                );
            }
            Optional<Element<UploadedDocuments>> quarantineCitizenDocElement = Optional.empty();
            if (null != caseData.getCitizenUploadQuarantineDocsList()) {
                quarantineCitizenDocElement = caseData.getCitizenUploadQuarantineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
            }

            if (quarantineLegalDocElement.isPresent()) {
                updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(
                    caseDataUpdated,
                    quarantineLegalDocElement,
                    LEGAL_PROFESSIONAL
                );
            } else if (cafcassQuarantineDocElement.isPresent()) {
                updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(
                    caseDataUpdated,
                    cafcassQuarantineDocElement,
                    CAFCASS
                );
            } else if (courtStaffQuarantineDocElement.isPresent()) {
                updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(
                    caseDataUpdated,
                    courtStaffQuarantineDocElement,
                    COURT_STAFF
                );
            } else if (quarantineCitizenDocElement.isPresent()) {
                UploadedDocuments document = quarantineCitizenDocElement.get().getValue();
                log.info("** citizen document ** {}", document);

                String docTobeReviewed = formatDocumentTobeReviewed(
                    document.getPartyName(),
                    document.getDocumentType(),
                    ""
                );

                caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);
                caseDataUpdated.put(REVIEW_DOC, document.getCitizenDocument());
            }
            if (CollectionUtils.isNotEmpty(caseData.getScannedDocuments())) {
                Optional<Element<QuarantineLegalDoc>> quarantineBulkscanDocElement = Optional.empty();
                quarantineBulkscanDocElement = Optional.of(
                    element(QuarantineLegalDoc.builder()
                                .url(caseData.getScannedDocuments().stream()
                                         .filter(element -> element.getId().equals(uuid))
                                         .collect(Collectors.toList()).stream().findFirst().map(Element::getValue).map(
                                        ScannedDocument::getUrl).orElse(null)).build()));
                if (quarantineBulkscanDocElement.isPresent()) {
                    updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(
                        caseDataUpdated,
                        quarantineBulkscanDocElement,
                        BULK_SCAN
                    );
                }
            }
        }
    }

    private void updateCaseDataUpdatedWithDocToBeReviewedAndReviewDoc(Map<String, Object> caseDataUpdated,
                                                                      Optional<Element<QuarantineLegalDoc>> quarantineDocElement,
                                                                      String submittedBy) {

        QuarantineLegalDoc document = quarantineDocElement.get().getValue();

        String docTobeReviewed = formatDocumentTobeReviewed(
            submittedBy,
            document.getCategoryName(),
            document.getNotes()
        );

        caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);

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
            QuarantineLegalDoc uploadDoc;
            if (isReviewDecisionYes) {
                try {
                    Document document = getQuarantineDocument(uploadedBy, quarantineLegalDocElement.getValue());
                    UUID documentId = UUID.fromString(getDocumentId(document.getDocumentUrl()));
                    log.info(" DocumentId found {}", documentId);
                    Document newUploadedDocument = getNewUploadedDocument(document, documentId);

                    log.info("document uploaded {}", newUploadedDocument);
                    if (null != newUploadedDocument) {
                        caseDocumentClient.deleteDocument(systemUserService.getSysUserToken(),
                                                          authTokenGenerator.generate(), documentId, true
                        );
                        log.info("deleted document {}", documentId);

                        uploadDoc = DocumentUtils.getQuarantineUploadDocument(
                            quarantineLegalDocElement.getValue().getCategoryId(),
                            newUploadedDocument
                        );
                    } else {
                        throw new RuntimeException("Failed to move document to confidential tab please retry");
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to move document to confidential tab please retry");
                }
            } else {
                uploadDoc = DocumentUtils.getQuarantineUploadDocument(
                    quarantineLegalDocElement.getValue().getCategoryId(),
                    getQuarantineDocument(uploadedBy, quarantineLegalDocElement.getValue())
                );
            }

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

    private Document getNewUploadedDocument(Document document, UUID documentId) {
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

    private Document getQuarantineDocument(String uploadedBy,
                                           QuarantineLegalDoc quarantineLegalDoc) {
        switch (uploadedBy) {
            case SOLICITOR:
                return quarantineLegalDoc.getDocument();

            case CAFCASS:
                return quarantineLegalDoc.getCafcassQuarantineDocument();

            case COURT_STAFF:
                return quarantineLegalDoc.getCourtStaffQuarantineDocument();

            case BULK_SCAN:
                return quarantineLegalDoc.getUrl();

            default:
                return null;
        }
    }

    public void processReviewDocument(Map<String, Object> caseDataUpdated, CaseData caseData, UUID uuid) {
        if (YesNoDontKnow.yes.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            forReviewDecisionYes(caseData, caseDataUpdated, uuid);
        } else if (YesNoDontKnow.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            forReviewDecisionNo(caseData, caseDataUpdated, uuid);
        }
        caseDataUpdated.put("legalProfQuarantineDocsList", caseData.getLegalProfQuarantineDocsList());
        caseDataUpdated.put("cafcassQuarantineDocsList", caseData.getCafcassQuarantineDocsList());
        caseDataUpdated.put("citizenUploadQuarantineDocsList", caseData.getCitizenUploadQuarantineDocsList());
        caseDataUpdated.put("courtStaffQuarantineDocsList", caseData.getCourtStaffQuarantineDocsList());
        caseDataUpdated.put("scannedDocuments", caseData.getScannedDocuments());
    }

    private void forReviewDecisionYes(CaseData caseData, Map<String, Object> caseDataUpdated, UUID uuid) {

        if (null != caseData.getLegalProfQuarantineDocsList()) {
            uploadDocForConfOrDocTab(
                caseDataUpdated,
                caseData.getLegalProfQuarantineDocsList(),
                uuid,
                true,
                caseData.getReviewDocuments().getLegalProfUploadDocListConfTab(),
                LEGAL_PROF_UPLOAD_DOC_LIST_CONF_TAB,
                SOLICITOR
            );
        }
        //cafcass
        if (null != caseData.getCafcassQuarantineDocsList()) {

            uploadDocForConfOrDocTab(
                caseDataUpdated,
                caseData.getCafcassQuarantineDocsList(),
                uuid,
                true,
                caseData.getReviewDocuments().getCafcassUploadDocListConfTab(),
                CAFCASS_UPLOAD_DOC_LIST_CONF_TAB,
                CAFCASS
            );
        }
        //court staff
        if (null != caseData.getCourtStaffQuarantineDocsList()) {
            uploadDocForConfOrDocTab(
                caseDataUpdated,
                caseData.getCourtStaffQuarantineDocsList(),
                uuid,
                true,
                caseData.getReviewDocuments().getCourtStaffUploadDocListConfTab(),
                COURT_STAFF_UPLOAD_DOC_LIST_CONF_TAB,
                COURT_STAFF
            );
        }
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
        caseData.getScannedDocuments().stream().forEach(sc ->
                                                            log.info("scanned doc list id {}", sc.getId())
        );
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
        List<Element<QuarantineLegalDoc>> quarantineLegalDocElementList = scannedDocumentElements.stream().map(
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
        return quarantineLegalDocElementList;
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
                caseData.getCaseTypeOfApplication().equalsIgnoreCase(C100_CASE_TYPE)
                    ? "c100-all-docs-reviewed" : "fl401-all-docs-reviewed",
                null
            );
        }
        if (YesNoDontKnow.yes.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            return ResponseEntity.ok(SubmittedCallbackResponse.builder()
                                         .confirmationHeader(DOCUMENT_SUCCESSFULLY_REVIEWED)
                                         .confirmationBody(REVIEW_YES).build());
        } else if (YesNoDontKnow.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            return ResponseEntity.ok(SubmittedCallbackResponse.builder()
                                         .confirmationHeader(DOCUMENT_SUCCESSFULLY_REVIEWED)
                                         .confirmationBody(REVIEW_NO).build());
        } else {
            return ResponseEntity.ok(SubmittedCallbackResponse.builder()
                                         .confirmationHeader(DOCUMENT_IN_REVIEW)
                                         .confirmationBody(REVIEW_NOT_SURE).build());
        }
    }

    private Optional<Element<QuarantineLegalDoc>> getQuarantineDocumentById(
        List<Element<QuarantineLegalDoc>> quarantineDocsList, UUID uuid) {
        return quarantineDocsList.stream()
            .filter(element -> element.getId().equals(uuid)).findFirst();
    }

    private String formatDocumentTobeReviewed(String submittedBy,
                                              String category,
                                              String notes) {
        if (BULK_SCAN.equals(submittedBy)) {
            return String.join(
                format(SUBMITTED_BY_LABEL, submittedBy)
            );
        }
        return String.join(
            format(SUBMITTED_BY_LABEL, submittedBy),
            format(DOCUMENT_CATEGORY_LABEL, category),
            format(DOCUMENT_COMMENTS_LABEL, notes, "<br/>")
        );
    }

    private QuarantineLegalDoc addQuarantineDocumentFields(QuarantineLegalDoc legalProfUploadDoc,
                                                           QuarantineLegalDoc quarantineLegalDoc) {
        log.info("File name {}", quarantineLegalDoc.getFileName());
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
            .build();
    }

}
