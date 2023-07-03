package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.TypeOfNocEventEnum;
import uk.gov.hmcts.reform.prl.events.TransferToAnotherCourtEvent;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COLON_SEPERATOR;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_CODE_FROM_FACT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_LIST;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_NAME_FIELD;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.COURT_SEAL_FIELD;

@Service
@Slf4j
@RequiredArgsConstructor
public class AmendCourtService {

    private final C100IssueCaseService c100IssueCaseService;
    private final LocationRefDataService locationRefDataService;
    private final CourtSealFinderService courtSealFinderService;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final CaseWorkerEmailService caseWorkerEmailService;
    private final EventService eventPublisher;
    private final SendgridService sendgridService;

    public Map<String, Object> handleAmendCourtSubmission(String authorisation, CallbackRequest callbackRequest,
                                                          Map<String, Object> caseDataUpdated) {
        CaseData caseData = CaseUtils.getCaseData(callbackRequest.getCaseDetails(), objectMapper);
        String baseLocationId = caseData.getCourtList().getValue().getCode().split(COLON_SEPERATOR)[0];
        if (!CollectionUtils.isEmpty(caseData.getCantFindCourtCheck())) {
            caseDataUpdated.put(COURT_NAME_FIELD, caseData.getAnotherCourt());
            if (caseData.getCourtEmailAddress() != null) {
                sendTransferToAnotherCourtEmail(authorisation,caseData, callbackRequest.getCaseDetails());
            }
        } else {
            Optional<CourtVenue> courtVenue = locationRefDataService.getCourtDetailsFromEpimmsId(
                baseLocationId,
                authorisation
            );
            caseDataUpdated.putAll(CaseUtils.getCourtDetails(courtVenue, baseLocationId));
            courtVenue.ifPresent(venue -> caseDataUpdated.put(
                COURT_CODE_FROM_FACT,
                c100IssueCaseService.getFactCourtId(
                    venue
                )
            ));
            caseDataUpdated.put(COURT_LIST, DynamicList.builder().value(caseData.getCourtList().getValue()).build());
            if (courtVenue.isPresent()) {
                String courtSeal = courtSealFinderService.getCourtSeal(courtVenue.get().getRegionId());
                caseDataUpdated.put(COURT_SEAL_FIELD, courtSeal);
            }
            if (caseData.getCourtEmailAddress() != null) {
                sendCourtAdminEmail(caseData, callbackRequest.getCaseDetails());
            }
        }
        TransferToAnotherCourtEvent event =
            prepareTransferToAnotherCourtEvent(caseData.toBuilder().courtName(
                (String) caseDataUpdated.get("courtName")).build(),
                                               TypeOfNocEventEnum.transferToAnotherCourt.getDisplayedValue());
        eventPublisher.publishEvent(event);
        return caseDataUpdated;
    }

    private void sendTransferToAnotherCourtEmail(String authorization,CaseData caseData, CaseDetails caseDetails) {
        try {
            sendgridService.sendTransferCourtEmailWithAttachments(authorization,
                                                     getEmailProps(caseData.getApplicantCaseName(),
                                                                   String.valueOf(caseData.getId())),
                                                     caseData.getCourtEmailAddress(), getAllCaseDocuments(caseData));
        } catch (IOException e) {
            log.error("Failed to send Email to {}", caseData.getCourtEmailAddress());
        }
    }

    private Map<String, String> getEmailProps(String applicantCaseName, String caseId) {
        Map<String, String> combinedMap = new HashMap<>();
        combinedMap.put("caseName", applicantCaseName);
        combinedMap.put("caseNumber", caseId);
        return combinedMap;
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
        }
        if (null != caseData.getOtherDocuments()) {
            caseData.getOtherDocuments().stream()
                .forEach(element -> {
                    docs.add(element.getValue().getDocumentOther());
                });
        }
        if (null != caseData.getOtherDocumentsUploaded()) {
          docs.addAll(caseData.getOtherDocumentsUploaded());
        }
        docs.addAll(getAllOrderDocuments(caseData));
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
        return Collections.EMPTY_LIST;
    }

    private void sendCourtAdminEmail(CaseData caseData, CaseDetails caseDetails) {
        if (CaseUtils.getCaseTypeOfApplication(caseData).equalsIgnoreCase(C100_CASE_TYPE)) {
            emailService.send(
                caseData.getCourtEmailAddress(),
                EmailTemplateNames.COURTADMIN,
                caseWorkerEmailService.buildCourtAdminEmail(caseDetails),
                LanguagePreference.english
            );
        } else {
            caseWorkerEmailService.sendEmailToFl401LocalCourt(caseDetails, caseData.getCourtEmailAddress());
        }
    }

    private TransferToAnotherCourtEvent prepareTransferToAnotherCourtEvent(CaseData newCaseData,
                                                                   String typeOfEvent) {
        return TransferToAnotherCourtEvent.builder()
            .caseData(newCaseData)
            .typeOfEvent(typeOfEvent)
            .build();
    }

}
