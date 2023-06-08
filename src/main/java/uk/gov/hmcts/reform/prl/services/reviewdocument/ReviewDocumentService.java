package uk.gov.hmcts.reform.prl.services.reviewdocument;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CAFCASS;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SOLICITOR;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ReviewDocumentService {

    public static final String DOCUMENT_SUCCESSFULLY_REVIEWED = "# Document successfully reviewed";
    public static final String DOCUMENT_IN_REVIEW = "# Document review in progress";
    private static final String REVIEW_YES = "### You have successfully reviewed this document"
        + System.lineSeparator()
        + "This document can only be seen by court staff, Cafcass and the judiciary. "
        + "You can view it in case file view and the confidential details tab.";
    private static final String REVIEW_NO = "### You have successfully reviewed this document"
        +  System.lineSeparator()
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
    public static final String CITIZEN_UPLOAD_DOC_LIST_CONF_TAB = "citizenUploadDocListConfTab";
    public static final String LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB = "legalProfUploadDocListDocTab";
    public static final String CAFCASS_UPLOAD_DOC_LIST_DOC_TAB = "cafcassUploadDocListDocTab";
    public static final String CITIZEN_UPLOADED_DOC_LIST_DOC_TAB = "citizenUploadedDocListDocTab";

    public List<DynamicListElement> getDynamicListElements(CaseData caseData) {
        List<DynamicListElement> dynamicListElements = new ArrayList<>();
        if (null != caseData.getLegalProfQuarantineDocsList()) {
            dynamicListElements.addAll(caseData.getLegalProfQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getDocument().getDocumentFileName()
                                                          + " - " + element.getValue().getDocumentUploadedDate()
                                                   .format(DateTimeFormatter.ofPattern(D_MMMM_YYYY, Locale.UK)))
                                               .build()).collect(Collectors.toList()));
        }
        //added for cafcass
        if (null != caseData.getCafcassQuarantineDocsList()) {
            dynamicListElements.addAll(caseData.getCafcassQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getDocument().getDocumentFileName()
                                                          + " - " + element.getValue().getDocumentUploadedDate()
                                                   .format(DateTimeFormatter.ofPattern(D_MMMM_YYYY, Locale.UK)))
                                               .build()).collect(Collectors.toList()));
        }
        if (null != caseData.getCitizenUploadQuarantineDocsList()) {
            dynamicListElements.addAll(caseData.getCitizenUploadQuarantineDocsList().stream()
                                           .map(element -> DynamicListElement.builder().code(element.getId().toString())
                                               .label(element.getValue().getCitizenDocument().getDocumentFileName()
                                                          + " - " + CommonUtils.formatDate(D_MMMM_YYYY, element.getValue().getDateCreated()))
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
                quarantineLegalDocElement = caseData.getLegalProfQuarantineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
            }
            //cafcass quarantine doc
            Optional<Element<QuarantineLegalDoc>> cafcassQuarantineDocElement = Optional.empty();
            if (null != caseData.getCafcassQuarantineDocsList()) {
                cafcassQuarantineDocElement = getQuarantineDocumentById(
                    caseData.getCafcassQuarantineDocsList(), uuid);
            }
            Optional<Element<UploadedDocuments>> quarantineCitizenDocElement = Optional.empty();
            if (null != caseData.getCitizenUploadQuarantineDocsList()) {
                quarantineCitizenDocElement = caseData.getCitizenUploadQuarantineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
            }

            if (quarantineLegalDocElement.isPresent()) {
                QuarantineLegalDoc document = quarantineLegalDocElement.get().getValue();
                log.info("** QuarantineLegalDoc ** {}", document);

                String docTobeReviewed = formatDocumentTobeReviewed(document, SOLICITOR);

                caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);
                caseDataUpdated.put(REVIEW_DOC, document.getDocument());
                log.info("docTobeReviewed {}", docTobeReviewed);
                log.info("** review doc ** {}", document.getDocument());
            } else if (cafcassQuarantineDocElement.isPresent()) {
                QuarantineLegalDoc cafcassDocument = cafcassQuarantineDocElement.get().getValue();
                log.info("** cafcassQuarantineDoc ** {}", cafcassDocument);

                String docTobeReviewed = formatDocumentTobeReviewed(cafcassDocument, CAFCASS);

                caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);
                caseDataUpdated.put(REVIEW_DOC, cafcassDocument.getCafcassQuarantineDocument());
                log.info("docTobeReviewed {}", docTobeReviewed);
                log.info("** review doc ** {}", cafcassDocument.getCafcassQuarantineDocument());
            } else if (quarantineCitizenDocElement.isPresent()) {
                UploadedDocuments document = quarantineCitizenDocElement.get().getValue();
                log.info("** citizen document ** {}", document);

                String docTobeReviewed = String
                    .join(format(SUBMITTED_BY_LABEL, document.getPartyName()),
                          format(DOCUMENT_CATEGORY_LABEL, document.getDocumentType()),
                          format(DOCUMENT_COMMENTS_LABEL, " "));

                caseDataUpdated.put(DOC_TO_BE_REVIEWED, docTobeReviewed);
                caseDataUpdated.put(REVIEW_DOC, document.getCitizenDocument());
                log.info("docTobeReviewed {}", docTobeReviewed);
                log.info("** review doc ** {}", document.getCitizenDocument());
            }
        }
    }


    public void processReviewDocument(Map<String, Object> caseDataUpdated, CaseData caseData, UUID uuid) {
        if (YesNoDontKnow.yes.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            if (null != caseData.getLegalProfQuarantineDocsList()) {
                Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElement =
                    getQuarantineDocumentById(caseData.getLegalProfQuarantineDocsList(), uuid);
                if (quarantineLegalDocElement.isPresent()) {
                    Element<QuarantineLegalDoc> docDetails = caseData.getLegalProfQuarantineDocsList()
                        .remove(caseData.getLegalProfQuarantineDocsList().indexOf(quarantineLegalDocElement.get()));
                    QuarantineLegalDoc legalProfUploadDoc = DocumentUtils
                        .getLegalProfUploadDocument("confidential", docDetails.getValue().getDocument());
                    if (null != caseData.getReviewDocuments().getLegalProfUploadDocListConfTab()) {
                        caseData.getReviewDocuments().getLegalProfUploadDocListConfTab().add(element(legalProfUploadDoc));
                        caseDataUpdated.put(LEGAL_PROF_UPLOAD_DOC_LIST_CONF_TAB, caseData.getReviewDocuments().getLegalProfUploadDocListConfTab());
                    } else {
                        caseDataUpdated.put(LEGAL_PROF_UPLOAD_DOC_LIST_CONF_TAB, List.of(element(legalProfUploadDoc)));
                    }
                }
                log.info("*** legal prof docs conf tab ** {}", caseDataUpdated.get(LEGAL_PROF_UPLOAD_DOC_LIST_CONF_TAB));
            }
            //cafcass
            if (null != caseData.getCafcassQuarantineDocsList()) {
                Optional<Element<QuarantineLegalDoc>> cafcassQuarantineDocElement =
                    getQuarantineDocumentById(caseData.getCafcassQuarantineDocsList(), uuid);
                if (cafcassQuarantineDocElement.isPresent()) {
                    Element<QuarantineLegalDoc> docDetails = caseData.getCafcassQuarantineDocsList()
                        .remove(caseData.getCafcassQuarantineDocsList().indexOf(cafcassQuarantineDocElement.get()));
                    QuarantineLegalDoc cafcassUploadDoc = DocumentUtils
                        .getLegalProfUploadDocument("confidential", docDetails.getValue().getCafcassQuarantineDocument());
                    if (null != caseData.getReviewDocuments().getCafcassUploadDocListConfTab()) {
                        caseData.getReviewDocuments().getCafcassUploadDocListConfTab().add(element(cafcassUploadDoc));
                        caseDataUpdated.put(CAFCASS_UPLOAD_DOC_LIST_CONF_TAB, caseData.getReviewDocuments().getCafcassUploadDocListConfTab());
                    } else {
                        caseDataUpdated.put(CAFCASS_UPLOAD_DOC_LIST_CONF_TAB, List.of(element(cafcassUploadDoc)));
                    }
                }
                log.info("*** cafcass docs conf tab ** {}", caseDataUpdated.get(CAFCASS_UPLOAD_DOC_LIST_CONF_TAB));
            }
            if (null != caseData.getCitizenUploadQuarantineDocsList()) {
                Optional<Element<UploadedDocuments>> quarantineCitizenDocElement = caseData.getCitizenUploadQuarantineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
                if (quarantineCitizenDocElement.isPresent()) {
                    Element<UploadedDocuments> docDetails = caseData.getCitizenUploadQuarantineDocsList()
                        .remove(caseData.getCitizenUploadQuarantineDocsList().indexOf(quarantineCitizenDocElement.get()));
                    if (null != caseData.getReviewDocuments().getCitizenUploadDocListConfTab()) {
                        caseData.getReviewDocuments().getCitizenUploadDocListConfTab().add(docDetails);
                        caseDataUpdated.put(CITIZEN_UPLOAD_DOC_LIST_CONF_TAB, caseData.getReviewDocuments().getCitizenUploadDocListConfTab());
                    } else {
                        caseDataUpdated.put(CITIZEN_UPLOAD_DOC_LIST_CONF_TAB, List.of(docDetails));
                    }
                }
                log.info("*** citizen docs conf tab ** {}", caseDataUpdated.get(CITIZEN_UPLOAD_DOC_LIST_CONF_TAB));
            }
        } else if (YesNoDontKnow.no.equals(caseData.getReviewDocuments().getReviewDecisionYesOrNo())) {
            if (null != caseData.getLegalProfQuarantineDocsList()) {
                Optional<Element<QuarantineLegalDoc>> quarantineLegalDocElement =
                    getQuarantineDocumentById(caseData.getLegalProfQuarantineDocsList(), uuid);
                if (quarantineLegalDocElement.isPresent()) {
                    Element<QuarantineLegalDoc> docDetails = caseData.getLegalProfQuarantineDocsList()
                        .remove(caseData.getLegalProfQuarantineDocsList().indexOf(quarantineLegalDocElement.get()));
                    QuarantineLegalDoc legalProfUploadDoc = DocumentUtils
                        .getLegalProfUploadDocument(docDetails.getValue().getCategory(), docDetails
                            .getValue().getDocument());
                    if (null != caseData.getReviewDocuments().getLegalProfUploadDocListDocTab()) {
                        caseData.getReviewDocuments().getLegalProfUploadDocListDocTab().add(element(legalProfUploadDoc));
                        caseDataUpdated.put(LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB, caseData.getReviewDocuments().getLegalProfUploadDocListDocTab());
                    } else {
                        caseDataUpdated.put(LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB, List.of(element(legalProfUploadDoc)));
                    }
                }
                log.info("*** legal prof docs tab ** {}", caseDataUpdated.get(LEGAL_PROF_UPLOAD_DOC_LIST_DOC_TAB));
            }
            //cafcass
            if (null != caseData.getCafcassQuarantineDocsList()) {
                Optional<Element<QuarantineLegalDoc>> cafcassQuarantineDocElement =
                    getQuarantineDocumentById(caseData.getCafcassQuarantineDocsList(), uuid);
                if (cafcassQuarantineDocElement.isPresent()) {
                    Element<QuarantineLegalDoc> docDetails = caseData.getCafcassQuarantineDocsList()
                        .remove(caseData.getCafcassQuarantineDocsList().indexOf(cafcassQuarantineDocElement.get()));
                    QuarantineLegalDoc cafcassUploadDoc = DocumentUtils.getLegalProfUploadDocument(
                        docDetails.getValue().getCategory(), docDetails.getValue().getDocument());
                    if (null != caseData.getReviewDocuments().getCafcassUploadDocListDocTab()) {
                        caseData.getReviewDocuments().getCafcassUploadDocListDocTab().add(element(cafcassUploadDoc));
                        caseDataUpdated.put(CAFCASS_UPLOAD_DOC_LIST_DOC_TAB, caseData.getReviewDocuments().getCafcassUploadDocListDocTab());
                    } else {
                        caseDataUpdated.put(CAFCASS_UPLOAD_DOC_LIST_DOC_TAB, List.of(element(cafcassUploadDoc)));
                    }
                }
                log.info("*** cafcass docs tab ** {}", caseDataUpdated.get(CAFCASS_UPLOAD_DOC_LIST_DOC_TAB));
            }
            if (null != caseData.getCitizenUploadQuarantineDocsList()) {
                Optional<Element<UploadedDocuments>> quarantineCitizenDocElement = caseData.getCitizenUploadQuarantineDocsList().stream()
                    .filter(element -> element.getId().equals(uuid)).findFirst();
                if (quarantineCitizenDocElement.isPresent()) {
                    Element<UploadedDocuments> docDetails = caseData.getCitizenUploadQuarantineDocsList()
                        .remove(caseData.getCitizenUploadQuarantineDocsList().indexOf(quarantineCitizenDocElement.get()));
                    if (null != caseData.getReviewDocuments().getCitizenUploadedDocListDocTab()) {
                        caseData.getReviewDocuments().getCitizenUploadedDocListDocTab().add(docDetails);
                        caseDataUpdated.put(CITIZEN_UPLOADED_DOC_LIST_DOC_TAB, caseData.getReviewDocuments().getCitizenUploadedDocListDocTab());
                    } else {
                        caseDataUpdated.put(CITIZEN_UPLOADED_DOC_LIST_DOC_TAB, List.of(docDetails));
                    }
                }
                log.info("*** citizen docs tab ** {}", caseDataUpdated.get(CITIZEN_UPLOADED_DOC_LIST_DOC_TAB));
            }
        }
    }

    public ResponseEntity<SubmittedCallbackResponse> getReviewResult(CaseData caseData) {
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

    private String formatDocumentTobeReviewed(QuarantineLegalDoc document,
                                              String submittedBy) {
        return String.join(
            format(SUBMITTED_BY_LABEL, submittedBy),
            format(DOCUMENT_CATEGORY_LABEL, document.getCategory()),
            format(DOCUMENT_COMMENTS_LABEL, document.getNotes()), "<br/>");
    }
}
