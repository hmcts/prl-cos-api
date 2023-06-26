package uk.gov.hmcts.reform.prl.services.reviewdocument;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.SubmittedCallbackResponse;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.CoreCaseDataService;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.DocumentUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_STAFF;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.JURISDICTION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LEGAL_PROFESSIONAL;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewDocumentService {

    @Autowired
    CoreCaseDataService coreCaseDataService;

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
    public static final String LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB = "legalProfUploadDocListDocTab";
    public static final String CAFCASS_UPLOAD_DOC_LIST_DOC_TAB = "cafcassUploadDocListDocTab";
    public static final String COURT_STAFF_UPLOAD_DOC_LIST_DOC_TAB = "courtStaffUploadDocListDocTab";
    public static final String CITIZEN_UPLOADED_DOC_LIST_DOC_TAB = "citizenUploadedDocListDocTab";
    public static final String CONFIDENTIAL_CATEGORY_ID = "confidential";

    public List<DynamicListElement> getDynamicListElements(CaseData caseData) {
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(caseData.getLegalProfQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getLegalProfQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getDocument().getDocumentFileName()
                                                          + " - " + element.getValue().getDocumentUploadedDate()
                                                   .format(DateTimeFormatter.ofPattern(D_MMMM_YYYY, Locale.UK)))
                                               .build()).collect(Collectors.toList()));
        }
        //added for cafcass
        if (CollectionUtils.isNotEmpty(caseData.getCafcassQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getCafcassQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getCafcassQuarantineDocument().getDocumentFileName()
                                                          + " - " + element.getValue().getDocumentUploadedDate()
                                                   .format(DateTimeFormatter.ofPattern(D_MMMM_YYYY, Locale.UK)))
                                               .build()).collect(Collectors.toList()));
        }
        //court staff
        if (CollectionUtils.isNotEmpty(caseData.getCourtStaffQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getCourtStaffQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getCourtStaffQuarantineDocument().getDocumentFileName()
                                                          + " - " + element.getValue().getDocumentUploadedDate()
                                                   .format(DateTimeFormatter.ofPattern(D_MMMM_YYYY, Locale.UK)))
                                               .build()).collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(caseData.getCitizenUploadQuarantineDocsList())) {
            dynamicListElements.addAll(caseData.getCitizenUploadQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getCitizenDocument().getDocumentFileName()
                                                          + " - " + CommonUtils.formatDate(
                                                   D_MMMM_YYYY,
                                                   element.getValue().getDateCreated()
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
                QuarantineLegalDoc document = quarantineLegalDocElement.get().getValue();
                log.info("** QuarantineLegalDoc ** {}", document);

                String docTobeReviewed = formatDocumentTobeReviewed(
                    LEGAL_PROFESSIONAL,
                    document.getCategoryName(),
                    document.getNotes()
                );

                caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);
                caseDataUpdated.put(REVIEW_DOC, document.getDocument());
                log.info(DOC_TO_BE_REVIEWED + " {}", docTobeReviewed);
                log.info(REVIEW_DOC + " {}", document.getDocument());
            } else if (cafcassQuarantineDocElement.isPresent()) {
                QuarantineLegalDoc cafcassDocument = cafcassQuarantineDocElement.get().getValue();
                log.info("** cafcassQuarantineDoc ** {}", cafcassDocument);

                String docTobeReviewed = formatDocumentTobeReviewed(
                    CAFCASS,
                    cafcassDocument.getCategoryName(),
                    cafcassDocument.getNotes()
                );

                caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);
                caseDataUpdated.put(REVIEW_DOC, cafcassDocument.getCafcassQuarantineDocument());
                log.info(DOC_TO_BE_REVIEWED + " {}", docTobeReviewed);
                log.info(REVIEW_DOC + " {}", cafcassDocument.getCafcassQuarantineDocument());
            } else if (courtStaffQuarantineDocElement.isPresent()) {
                QuarantineLegalDoc courtStaffDocument = courtStaffQuarantineDocElement.get().getValue();
                log.info("** courtStaffQuarantineDoc ** {}", courtStaffDocument);

                String docTobeReviewed = formatDocumentTobeReviewed(
                    COURT_STAFF,
                    courtStaffDocument.getCategoryName(),
                    courtStaffDocument.getNotes()
                );

                caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);
                caseDataUpdated.put(REVIEW_DOC, courtStaffDocument.getCourtStaffQuarantineDocument());
                log.info(DOC_TO_BE_REVIEWED + " {}", docTobeReviewed);
                log.info(REVIEW_DOC + " {}", courtStaffDocument.getCourtStaffQuarantineDocument());
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
                log.info(DOC_TO_BE_REVIEWED + " {}", docTobeReviewed);
                log.info(REVIEW_DOC + " {}", document.getCitizenDocument());
            }
        }
    }

    private void uploadDocForConfOrDocTab(Map<String, Object> caseDataUpdated,
                                     List<Element<QuarantineLegalDoc>> quarantineDocsList,
                                     UUID uuid,
                                     boolean isReviewDecisionYes,
                                     List<Element<QuarantineLegalDoc>> uploadDocListConfOrDocTab,
                                     String uploadDocListConfOrDocTabKey) {

        Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElementOptional =
            getQuarantineDocumentById(quarantineDocsList, uuid);
        if (quarantineLegalDocElementOptional.isPresent()) {
            Element<QuarantineLegalDoc> quarantineLegalDocElement = quarantineLegalDocElementOptional.get();
            //remove document from quarantine
            quarantineDocsList.remove(quarantineLegalDocElement);

            QuarantineLegalDoc legalProfUploadDoc = DocumentUtils.getQuarantineUploadDocument(
                isReviewDecisionYes ? CONFIDENTIAL_CATEGORY_ID : quarantineLegalDocElement.getValue().getCategoryId(),
                quarantineLegalDocElement.getValue().getDocument());


            legalProfUploadDoc = addQuarantineDocumentFields(
                legalProfUploadDoc,
                quarantineLegalDocElement.getValue()
            );

            if (null != uploadDocListConfOrDocTab) {
                uploadDocListConfOrDocTab.add(element(legalProfUploadDoc));
                caseDataUpdated.put(
                    uploadDocListConfOrDocTabKey,
                    uploadDocListConfOrDocTab
                );
            } else {
                caseDataUpdated.put(uploadDocListConfOrDocTabKey, List.of(element(legalProfUploadDoc)));
            }
        }
    }

    public void processReviewDocument(Map<String, Object> caseDataUpdated, CaseData caseData, UUID uuid) {
        if (YesNoDontKnow.yes.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            if (null != caseData.getLegalProfQuarantineDocsList()) {
                uploadDocForConfOrDocTab(caseDataUpdated,
                                    caseData.getLegalProfQuarantineDocsList(),
                                    uuid,
                                   true,
                                    caseData.getReviewDocuments().getLegalProfUploadDocListConfTab(),
                                    LEGAL_PROF_UPLOAD_DOC_LIST_CONF_TAB);

                log.info(
                    "*** legal prof docs conf tab ** {}",
                    caseDataUpdated.get(LEGAL_PROF_UPLOAD_DOC_LIST_CONF_TAB)
                );
            }
            //cafcass
            if (null != caseData.getCafcassQuarantineDocsList()) {

                uploadDocForConfOrDocTab(caseDataUpdated,
                                    caseData.getCafcassQuarantineDocsList(),
                                    uuid,
                                    true,
                                    caseData.getReviewDocuments().getCafcassUploadDocListConfTab(),
                                    CAFCASS_UPLOAD_DOC_LIST_CONF_TAB);

                log.info("*** cafcass docs conf tab ** {}", caseDataUpdated.get(CAFCASS_UPLOAD_DOC_LIST_CONF_TAB));
            }
            //court staff
            if (null != caseData.getCourtStaffQuarantineDocsList()) {
                uploadDocForConfOrDocTab(caseDataUpdated,
                                    caseData.getCourtStaffQuarantineDocsList(),
                                    uuid,
                                    true,
                                    caseData.getReviewDocuments().getCourtStaffUploadDocListConfTab(),
                                    COURT_STAFF_UPLOAD_DOC_LIST_CONF_TAB);

                log.info(
                    "*** court staff docs conf tab ** {}",
                    caseDataUpdated.get(COURT_STAFF_UPLOAD_DOC_LIST_CONF_TAB)
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
                log.info("*** citizen docs conf tab ** {}", caseDataUpdated.get(CITIZEN_UPLOAD_DOC_LIST_CONF_TAB));
            }
        } else if (YesNoDontKnow.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            if (null != caseData.getLegalProfQuarantineDocsList()) {

                uploadDocForConfOrDocTab(caseDataUpdated,
                                    caseData.getLegalProfQuarantineDocsList(),
                                    uuid,
                                    false,
                                    caseData.getReviewDocuments().getLegalProfUploadDocListDocTab(),
                                    LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB);

                log.info("*** legal prof docs tab ** {}", caseDataUpdated.get(LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB));
            }
            //cafcass
            if (null != caseData.getCafcassQuarantineDocsList()) {
                uploadDocForConfOrDocTab(caseDataUpdated,
                                    caseData.getCafcassQuarantineDocsList(),
                                    uuid,
                                    false,
                                    caseData.getReviewDocuments().getCafcassUploadDocListDocTab(),
                                    CAFCASS_UPLOAD_DOC_LIST_DOC_TAB);

                log.info("*** cafcass docs tab ** {}", caseDataUpdated.get(CAFCASS_UPLOAD_DOC_LIST_DOC_TAB));
            }
            //court staff
            if (null != caseData.getCourtStaffQuarantineDocsList()) {
                uploadDocForConfOrDocTab(caseDataUpdated,
                                    caseData.getCourtStaffQuarantineDocsList(),
                                    uuid,
                                    false,
                                    caseData.getReviewDocuments().getCourtStaffUploadDocListDocTab(),
                                    COURT_STAFF_UPLOAD_DOC_LIST_DOC_TAB);

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
        }
    }

    public ResponseEntity<SubmittedCallbackResponse> getReviewResult(CaseData caseData) {
        if (caseData.getLegalProfQuarantineDocsList().isEmpty()
            || caseData.getCitizenUploadQuarantineDocsList().isEmpty()
            || caseData.getCafcassQuarantineDocsList().isEmpty()) {
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
        return String.join(
            format(SUBMITTED_BY_LABEL, submittedBy),
            format(DOCUMENT_CATEGORY_LABEL, category),
            format(DOCUMENT_COMMENTS_LABEL, notes, "<br/>")
        );
    }

    private QuarantineLegalDoc addQuarantineDocumentFields(QuarantineLegalDoc legalProfUploadDoc,
                                                           QuarantineLegalDoc quarantineLegalDoc) {

        return legalProfUploadDoc.toBuilder()
            .documentParty(quarantineLegalDoc.getDocumentParty())
            .documentUploadedDate(quarantineLegalDoc.getDocumentUploadedDate())
            .notes(quarantineLegalDoc.getNotes())
            .categoryId(quarantineLegalDoc.getCategoryId())
            .categoryName(quarantineLegalDoc.getCategoryName())
            .build();
    }

}
