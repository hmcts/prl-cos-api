package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CategoriesAndDocuments;
import uk.gov.hmcts.reform.ccd.client.model.Category;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.events.TransferToAnotherCourtEvent;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.SendgridService;
import uk.gov.hmcts.reform.prl.services.transfercase.TransferCaseContentProvider;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.APPLICATION_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ATTACHMENT_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NAME;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_NUMBER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONFIDENTIALITY_TEXT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CONTENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.DISPOSITION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_DATE_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.SUBJECT;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TransferToAnotherCourtEventHandler {
    private final EmailService emailService;
    private final TransferCaseContentProvider transferCaseContentProvider;
    private final SendgridService sendgridService;
    private final AuthTokenGenerator authTokenGenerator;
    private final CoreCaseDataApi coreCaseDataApi;

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
                                                                  caseData.getCourtEmailAddress(), getAllCaseDocuments(authorization,caseData));
        } catch (IOException e) {
            log.error("Failed to send Email ", e);
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
        combinedMap.put(CASE_NUMBER, String.valueOf(caseData.getId()));
        combinedMap.put(CASE_NAME, caseData.getApplicantCaseName());
        combinedMap.put(ISSUE_DATE_FIELD, CommonUtils.formatDate(D_MMM_YYYY, caseData.getIssueDate()));
        combinedMap.put(APPLICATION_TYPE, caseData.getCaseTypeOfApplication());
        combinedMap.put(CONFIDENTIALITY_TEXT, getConfidentialityText(caseData));
        combinedMap.put(COURT_NAME_FIELD, caseData.getTransferredCourtFrom());
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
        emailProps.put(SUBJECT, "A case has been transferred to your court");
        emailProps.put(CONTENT, "Case details");
        emailProps.put(ATTACHMENT_TYPE, "pdf");
        emailProps.put(DISPOSITION, "attachment");
        return emailProps;
    }

    private List<Document> getAllCaseDocuments(String authorisation, CaseData caseData) {

        List<Document> documentList = new ArrayList<>();
        CategoriesAndDocuments categoriesAndDocuments = coreCaseDataApi.getCategoriesAndDocuments(
            authorisation,
            authTokenGenerator.generate(),
            String.valueOf(caseData.getId())
        );

        List<Category> parentCategories = categoriesAndDocuments.getCategories().stream()
            .sorted(Comparator.comparing(Category::getCategoryName))
            .toList();

        createDocumentListFromSubCategories(parentCategories,documentList,null, null);
        categoriesAndDocuments.getUncategorisedDocuments().forEach(document ->
            documentList.add(getCcdCaseDocument(document)));
        return documentList;
    }

    private void createDocumentListFromSubCategories(List<Category> categoryList,
                                                     List<Document> documentList,
                                                     final String parentLabelString,
                                                     final String parentCodeString) {
        categoryList.forEach(category -> {
            if (parentLabelString == null) {
                if (category.getDocuments() != null) {
                    category.getDocuments().forEach(document ->
                        documentList.add(getCcdCaseDocument(document)));
                }
                if (category.getSubCategories() != null) {
                    createDocumentListFromSubCategories(
                        category.getSubCategories(),
                        documentList,
                        category.getCategoryName(),
                        category.getCategoryId()
                    );
                }
            } else {
                if (category.getDocuments() != null) {
                    category.getDocuments().forEach(document ->
                        documentList.add(getCcdCaseDocument(document)));
                }
                if (category.getSubCategories() != null) {
                    createDocumentListFromSubCategories(category.getSubCategories(), documentList,
                                                       parentLabelString + " -> " + category.getCategoryName(),
                                                       parentCodeString + " -> " + category.getCategoryId()
                    );
                }
            }
        });
    }

    private Document getCcdCaseDocument(uk.gov.hmcts.reform.ccd.client.model.Document document) {
        return Document.builder()
            .documentUrl(document.getDocumentURL())
            .documentBinaryUrl(document.getDocumentBinaryURL())
            .documentFileName(document.getDocumentFilename())
            .build();
    }
}
