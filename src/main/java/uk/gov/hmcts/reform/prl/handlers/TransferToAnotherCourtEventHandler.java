package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.events.TransferToAnotherCourtEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.transfercase.TransferCaseContentProvider;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMMM_YYYY;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransferToAnotherCourtEventHandler {
    private final EmailService emailService;
    private final TransferCaseContentProvider transferCaseContentProvider;
    private final SendgridService sendgridService;

    @EventListener(condition = "#event.typeOfEvent eq 'Transfer to another court'")
    public void transferCourtEmail(final TransferToAnotherCourtEvent event) {
        CaseData caseData = event.getCaseData();
        if (caseData.getCourtEmailAddress() != null) {
            sendTransferCourtEmail(caseData);
            sendTransferToAnotherCourtEmail(event.getAuthorisation(),caseData);
        }
    }

    private void sendTransferToAnotherCourtEmail(String authorization,CaseData caseData) {
        try {
            sendgridService.sendTransferCourtEmailWithAttachments(authorization,
                                                                  getEmailProps(caseData),
                                                                  caseData.getCourtEmailAddress(), getAllCaseDocuments(caseData));
        } catch (IOException e) {
            log.error("Failed to send Email");
        }
    }

    private void sendTransferCourtEmail(CaseData caseData) {
        emailService.send(
            caseData.getCourtEmailAddress(),
            EmailTemplateNames.TRANSFER_COURT_EMAIL_NOTIFICATION,
            transferCaseContentProvider.buildCourtTransferEmail(caseData,getConfidentialityText(caseData)),
            LanguagePreference.english
        );
    }

    private Map<String, String> getEmailProps(CaseData caseData) {
        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseNumber", caseData.getApplicantCaseName());
        combinedMap.put("caseName", String.valueOf(caseData.getId()));
        combinedMap.put("issueDate", CommonUtils.formatDate(D_MMMM_YYYY, caseData.getIssueDate()));
        combinedMap.put("applicationType", caseData.getCaseTypeOfApplication());
        combinedMap.put("confidentialityText", getConfidentialityText(caseData));
        combinedMap.put("courtName", caseData.getTransferredCourtFrom());
        combinedMap.putAll(getCommonEmailProps());
        return combinedMap;
    }

    private String getConfidentialityText(CaseData caseData) {
        return isCaseContainConfidentialDetails(caseData)
            ? "This case contains confidential contact details." : "";
    }

    private boolean isCaseContainConfidentialDetails(CaseData caseData) {
        return CollectionUtils.isNotEmpty(caseData.getApplicantsConfidentialDetails())
            || CollectionUtils.isNotEmpty(caseData.getRespondentConfidentialDetails())
            || CollectionUtils.isNotEmpty(caseData.getChildrenConfidentialDetails());
    }

    private Map<String, String> getCommonEmailProps() {
        Map<String, String> emailProps = new HashMap<>();
        emailProps.put("subject", "A case has been transferred to your court");
        emailProps.put("content", "Case details");
        emailProps.put("attachmentType", "pdf");
        emailProps.put("disposition", "attachment");
        return emailProps;
    }

    private List<Document> getAllCaseDocuments(CaseData caseData) {
        List<Document> docs = new ArrayList<>();
        if (CaseUtils.getCaseTypeOfApplication(caseData).equalsIgnoreCase(PrlAppsConstants.C100_CASE_TYPE)) {
            if (null != caseData.getFinalDocument()) {
                docs.add(caseData.getFinalDocument());
            }
            if (null != caseData.getFinalWelshDocument()) {
                docs.add(caseData.getFinalWelshDocument());
            }
            if (null != caseData.getC1ADocument()) {
                docs.add(caseData.getC1ADocument());
            }
            if (null != caseData.getC1AWelshDocument()) {
                docs.add(caseData.getC1AWelshDocument());
            }
            if (null != caseData.getC8Document()) {
                docs.add(caseData.getC8Document());
            }
            if (null != caseData.getC8WelshDocument()) {
                docs.add(caseData.getC8WelshDocument());
            }
        } else {
            if (null != caseData.getFinalDocument()) {
                docs.add(caseData.getFinalDocument());
            }
            if (null != caseData.getFinalWelshDocument()) {
                docs.add(caseData.getFinalWelshDocument());
            }
            if (null != caseData.getC8Document()) {
                docs.add(caseData.getC8Document());
            }
            if (null != caseData.getC8WelshDocument()) {
                docs.add(caseData.getC8WelshDocument());
            }
        }
        if (null != caseData.getOtherDocuments()) {
            caseData.getOtherDocuments().stream()
                .forEach(element ->
                    docs.add(element.getValue().getDocumentOther())
                );
        }
        if (null != caseData.getOtherDocumentsUploaded()) {
            docs.addAll(caseData.getOtherDocumentsUploaded());
        }
        docs.addAll(getAllOrderDocuments(caseData));
        docs.addAll(getAllManageDocuments(caseData));
        docs.addAll(getAllCitizenDocuments(caseData));
        docs.addAll(getAllCitizenUploadedDocuments(caseData));
        docs.addAll(getAllUploadedDocument(caseData.getCourtStaffQuarantineDocsList()));
        docs.addAll(getAllUploadedDocument(caseData.getCafcassQuarantineDocsList()));
        docs.addAll(getAllUploadedDocument(caseData.getLegalProfQuarantineDocsList()));
        return docs;
    }

    private List<Document> getAllOrderDocuments(CaseData caseData) {
        List<Document> selectedOrders = new ArrayList<>();

        if (null != caseData.getOrderCollection()) {
            caseData.getOrderCollection().stream()
                .forEach(orderDetailsElement -> {
                    if (orderDetailsElement.getValue().getOrderDocument() != null) {
                        selectedOrders.add(orderDetailsElement.getValue().getOrderDocument());
                    }
                    if (orderDetailsElement.getValue().getOrderDocumentWelsh() != null) {
                        selectedOrders.add(orderDetailsElement.getValue().getOrderDocumentWelsh());
                    }
                });
            return selectedOrders;
        }
        return Collections.emptyList();
    }

    private List<Document> getAllManageDocuments(CaseData caseData) {
        List<Document> selectedDocument = new ArrayList<>();

        if (null != caseData.getManageDocuments()) {
            caseData.getManageDocuments().stream()
                .forEach(documentElement -> {
                    if (documentElement.getValue().getDocument() != null) {
                        selectedDocument.add(documentElement.getValue().getDocument());
                    }
                });
            return selectedDocument;
        }
        return Collections.emptyList();
    }

    private List<Document> getAllUploadedDocument(List<Element<QuarantineLegalDoc>> manageDocuments) {
        List<Document> selectedDocument = new ArrayList<>();

        if (null != manageDocuments) {
            manageDocuments.stream()
                .forEach(documentElement -> {
                    if (documentElement.getValue().getDocument() != null) {
                        selectedDocument.add(documentElement.getValue().getDocument());
                    }
                });
            return selectedDocument;
        }
        return Collections.emptyList();
    }

    private List<Document> getAllCitizenDocuments(CaseData caseData) {
        List<Document> selectedDocument = new ArrayList<>();

        if (null != caseData.getCitizenResponseC7DocumentList()) {
            caseData.getCitizenResponseC7DocumentList().stream()
                .forEach(documentElement -> {
                    if (documentElement.getValue().getCitizenDocument() != null) {
                        selectedDocument.add(documentElement.getValue().getCitizenDocument());
                    }
                });
            return selectedDocument;
        }
        return Collections.emptyList();
    }

    private List<Document> getAllCitizenUploadedDocuments(CaseData caseData) {
        List<Document> selectedDocument = new ArrayList<>();

        if (null != caseData.getCitizenUploadedDocumentList()) {
            caseData.getCitizenUploadedDocumentList().stream()
                .forEach(documentElement -> {
                    if (documentElement.getValue().getCitizenDocument() != null) {
                        selectedDocument.add(documentElement.getValue().getCitizenDocument());
                    }
                });
            return selectedDocument;
        }
        return Collections.emptyList();
    }
}
