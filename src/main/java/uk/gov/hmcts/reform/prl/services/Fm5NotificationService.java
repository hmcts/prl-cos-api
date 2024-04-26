package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.FmPendingParty;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.SendgridEmailTemplateNames;
import uk.gov.hmcts.reform.prl.utils.EmailUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.config.templates.Templates.BLANK_FM5_DOCUMENT;
import static uk.gov.hmcts.reform.prl.services.ServiceOfApplicationService.DASH_BOARD_LINK;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class Fm5NotificationService {

    private final DgsService dgsService;
    private final ServiceOfApplicationEmailService serviceOfApplicationEmailService;
    @Value("${xui.url}")
    private String manageCaseUrl;

    public void checkFmPendingParties(FmPendingParty fmPendingParty, CaseData caseData, String authorization) {
        List<Element<PartyDetails>> listOfRecipientsOfNudge = new ArrayList<>();
        if (fmPendingParty.equals(FmPendingParty.APPLICANT)) {
            listOfRecipientsOfNudge.addAll(caseData.getApplicants());
            checkWhomToSendNudgeNotification(listOfRecipientsOfNudge, caseData, authorization);
        } else if (fmPendingParty.equals(FmPendingParty.RESPONDENT)) {
            listOfRecipientsOfNudge.addAll(caseData.getRespondents());
            checkWhomToSendNudgeNotification(listOfRecipientsOfNudge, caseData, authorization);
        } else if ((fmPendingParty.equals(FmPendingParty.BOTH))) {
            listOfRecipientsOfNudge.addAll(caseData.getApplicants());
            listOfRecipientsOfNudge.addAll(caseData.getRespondents());
            checkWhomToSendNudgeNotification(listOfRecipientsOfNudge, caseData, authorization);
        }
    }

    private void checkWhomToSendNudgeNotification(List<Element<PartyDetails>> listOfRecipientsOfNudge, CaseData caseData, String authorization) {
      listOfRecipientsOfNudge.forEach(recipient -> {
          if (null != recipient.getValue().getSolicitorEmail()) {
              prepareNudgeEmailDataSolicitor(caseData, recipient, authorization);
          } else {
              checkContactPreferenceForUnrepresentedRecipient(caseData, recipient, authorization);
          }
      });
    }

    private void checkContactPreferenceForUnrepresentedRecipient(CaseData caseData, Element<PartyDetails> recipient, String authorization) {
        if (null != recipient.getValue().getContactPreferences()
            && recipient.getValue().getContactPreferences().equals(ContactPreferences.digital)
            && null != recipient.getValue().getEmail()) {
            prepareNudgeEmailDataCitizen(caseData, recipient, authorization);
        } else {
            sendNudgePost(caseData, recipient, authorization);
        }
    }

    private void prepareNudgeEmailDataSolicitor(CaseData caseData, Element<PartyDetails> recipient, String authorization) {
        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        dynamicData.put("name", recipient.getValue().getRepresentativeFullName());
        dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());

        sendNudgeEmail(caseData, recipient, recipient.getValue().getSolicitorEmail(), authorization, dynamicData);
    }

    private void prepareNudgeEmailDataCitizen(CaseData caseData, Element<PartyDetails> recipient, String authorization) {
        Map<String, Object> dynamicData = EmailUtils.getCommonSendgridDynamicTemplateData(caseData);
        dynamicData.put("name", recipient.getValue().getRepresentativeFullName());
        //needs tg be changed to check if recipiant has access to dashboard and to the dashboard link
        dynamicData.put(DASH_BOARD_LINK, manageCaseUrl + PrlAppsConstants.URL_STRING + caseData.getId());

        sendNudgeEmail(caseData, recipient, recipient.getValue().getSolicitorEmail(), authorization, dynamicData);
    }

    private void sendNudgeEmail(CaseData caseData, Element<PartyDetails> recipient, String email, String authorization,  Map<String, Object> dynamicData) {
        List<Document> blankNudgeDocument = new ArrayList<>();
        blankNudgeDocument.add(generateBlankNudgeDocument(caseData, authorization));
        //need to send email

    }

    private void sendNudgePost(CaseData caseData, Element<PartyDetails> recipient, String authorization) {
        List<Document> blankNudgePack = new ArrayList<>();
        //need to add cover letter
        blankNudgePack.add(generateBlankNudgeDocument(caseData, authorization));
        //need to send post

    }

    private void sendNudgeEmail(CaseData caseData, String authorization,
                                PartyDetails party,
                                Map<String, Object> dynamicData,
                                String servedParty) {
        List<Document> blankNudgeDocument = new ArrayList<>();
        blankNudgeDocument.add(generateBlankNudgeDocument(caseData, authorization));

        serviceOfApplicationEmailService
            .sendEmailUsingTemplateWithAttachments(
                authorization, party.getSolicitorEmail(),
                blankNudgeDocument,
                SendgridEmailTemplateNames.SOA_NUDGE_REMINDER_SOLICITOR,
                dynamicData,
                servedParty);
    }

    private Document generateBlankNudgeDocument(CaseData caseData, String authorisation) {

        String template = BLANK_FM5_DOCUMENT;
        try {
            log.info("generating blank fm5 document : {} for case : {}", template, caseData.getId());
            GeneratedDocumentInfo accessCodeLetter = dgsService.generateDocument(
                authorisation,
                String.valueOf(caseData.getId()),
                template,
                new HashMap<>()
            );

            return Document.builder().documentUrl(accessCodeLetter.getUrl())
                .documentFileName(accessCodeLetter.getDocName()).documentBinaryUrl(accessCodeLetter.getBinaryUrl())
                .documentCreatedOn(new Date())
                .build();
        } catch (Exception e) {
            log.error("*** Blank fm5 document failed for {} :: because of {}", template, e.getMessage());
        }
        return null;
    }
}
