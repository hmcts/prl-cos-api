package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangeContentProvider;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeEventHandler {
    private final EmailService emailService;
    private final NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @EventListener(condition = "#event.typeOfEvent eq 'Add Legal Representation'")
    public void notifyLegalRepresentative(final NoticeOfChangeEvent event) {
        CaseData caseData = event.getCaseData();
        //PRL-3211 - notify new LR
        sendEmailToSolicitor(caseData, event, EmailTemplateNames.CA_DA_SOLICITOR_NOC);

        //PRL-3211 - notify LiP
        sendEmailToLitigant(caseData, event, EmailTemplateNames.CA_DA_APPLICANT_RESPONDENT_NOC);

        //PRL-3211 - notify applicants/respondents other parties except litigant
        sendEmailToApplicantsRespondents(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_NOC);

        //PRL-3211 - notify other persons if any
        sendEmailToOtherParties(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_NOC);

        //PRL-3211 - notify applicants/respondents LRs
        sendEmailToAppRespSolicitors(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_NOC);
    }

    private void sendEmailToAppRespSolicitors(CaseData caseData, NoticeOfChangeEvent event, EmailTemplateNames emailTemplateNames) {
        Map<String, String> solicitorsToNotify = new HashMap<>();
        solicitorsToNotify.putAll(CaseUtils.getApplicantSolicitorsToNotify(caseData));
        solicitorsToNotify.putAll(CaseUtils.getRespondentSolicitorsToNotify(caseData));
        if (!solicitorsToNotify.isEmpty()) {
            solicitorsToNotify.forEach(
                (key, value) -> emailService.send(
                    key,
                    emailTemplateNames,
                    noticeOfChangeContentProvider.buildNocEmailSolicitor(caseData, event.getSolicitorName()),
                    LanguagePreference.getPreferenceLanguage(caseData)
                ));
        }
    }

    private void sendEmailToOtherParties(CaseData caseData, NoticeOfChangeEvent event, EmailTemplateNames emailTemplateNames) {
        Map<String, String> othersToNotify = CaseUtils.getOthersToNotify(caseData);
        if (!othersToNotify.isEmpty()) {
            othersToNotify.forEach(
                (key, value) -> emailService.send(
                    key,
                    emailTemplateNames,
                    noticeOfChangeContentProvider.buildNocEmailCitizen(caseData, event.getSolicitorName(), value, true, ""),
                    LanguagePreference.getPreferenceLanguage(caseData)
                ));
        }
    }

    private void sendEmailToApplicantsRespondents(CaseData caseData, NoticeOfChangeEvent event, EmailTemplateNames emailTemplateNames) {
        Element<PartyDetails> partyElement = getLitigantParty(caseData, event);
        Map<String, String> applicantsRespondentsToNotify = new HashMap<>();
        applicantsRespondentsToNotify.putAll(CaseUtils.getApplicantsToNotify(caseData, null != partyElement ? partyElement.getId() : null));
        applicantsRespondentsToNotify.putAll(CaseUtils.getRespondentsToNotify(caseData, null != partyElement ? partyElement.getId() : null));
        if (!applicantsRespondentsToNotify.isEmpty()) {
            applicantsRespondentsToNotify.forEach(
                (key, value) -> emailService.send(
                    key,
                    emailTemplateNames,
                    noticeOfChangeContentProvider.buildNocEmailCitizen(caseData, event.getSolicitorName(), value, false, ""),
                    LanguagePreference.getPreferenceLanguage(caseData)
                ));
        }
    }

    private void sendEmailToLitigant(CaseData caseData, NoticeOfChangeEvent event, EmailTemplateNames emailTemplateName) {
        Element<PartyDetails> partyElement = getLitigantParty(caseData, event);
        if (null != partyElement && null != partyElement.getValue()) {
            PartyDetails partyDetails = partyElement.getValue();
            if (null != partyDetails.getEmail()) {
                emailService.send(
                    partyDetails.getEmail(),
                    emailTemplateName,
                    noticeOfChangeContentProvider.buildNocEmailCitizen(caseData, event.getSolicitorName(),
                                                                       partyDetails.getFirstName() + EMPTY_SPACE_STRING + partyDetails.getLastName(),
                                                                       false,
                                                                       event.getAccessCode()
                    ),
                    LanguagePreference.getPreferenceLanguage(caseData)
                );
            } else {
                log.info("Unable to send email to Litigant as the they don't have any email address");
            }
        }
    }

    private void sendEmailToSolicitor(CaseData caseData, NoticeOfChangeEvent event, EmailTemplateNames emailTemplateName) {
        if (null != event.getSolicitorEmailAddress()) {
            emailService.send(
                event.getSolicitorEmailAddress(),
                emailTemplateName,
                noticeOfChangeContentProvider.buildNocEmailSolicitor(caseData, event.getSolicitorName()),
                LanguagePreference.getPreferenceLanguage(caseData)
            );
        } else {
            log.info("Unable to send email to Solicitor as the they don't have any email address");
        }
    }

    private Element<PartyDetails> getLitigantParty(CaseData caseData, NoticeOfChangeEvent event) {
        int representingPartyIndex = event.getRepresentedPartyIndex();
        if (SolicitorRole.Representing.CAAPPLICANT.equals(event.getRepresenting())) {
            return caseData.getApplicants().get(representingPartyIndex);
        } else {
            return caseData.getRespondents().get(representingPartyIndex);
        }
    }


    @EventListener(condition = "#event.typeOfEvent eq 'Remove Legal Representation'")
    public void notifyWhenLegalRepresentativeRemoved(final NoticeOfChangeEvent event) {
        CaseData caseData = event.getCaseData();
        //PRL-3215 - notify old LR
        sendEmailToSolicitor(caseData, event, EmailTemplateNames.CA_DA_REMOVE_SOLICITOR_NOC);

        //PRL-3215 - notify LiP
        sendEmailToLitigant(caseData, event, EmailTemplateNames.CA_DA_APPLICANT_REMOVE_RESPONDENT_NOC);

        //PRL-3215 - notify applicants/respondents other parties except litigant
        sendEmailToApplicantsRespondents(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_REMOVE_NOC);

        //PRL-3215 - notify other persons if any
        sendEmailToOtherParties(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_REMOVE_NOC);

        //PRL-3215 - notify applicants/respondents LRs
        sendEmailToAppRespSolicitors(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_REMOVE_NOC);
    }
}
