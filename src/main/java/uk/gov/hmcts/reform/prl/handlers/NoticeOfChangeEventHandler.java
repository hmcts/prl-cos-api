package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
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

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeEventHandler {
    private final EmailService emailService;
    private final NoticeOfChangeContentProvider noticeOfChangeContentProvider;

    @EventListener
    public void notifyLegalRepresentative(final NoticeOfChangeEvent event) {
        log.info("My changes");
        CaseData caseData = event.getCaseData();
        //PRL-3211 - notify LR
        log.debug("Solicitor representing {}",event.getRepresenting());
        log.debug("Represented Party Index {}",event.getRepresentedPartyIndex());
        emailService.send(
            event.getSolicitorEmailAddress(),
            EmailTemplateNames.CA_DA_SOLICITOR_NOC,
            noticeOfChangeContentProvider.buildNoticeOfChangeEmail(caseData, event.getSolicitorName(), null, false),
            LanguagePreference.getPreferenceLanguage(caseData)
        );

        //PRL-3211 - notify LiP
        Element<PartyDetails> partyDetailsElement = caseData.getRespondents().get(event.getRepresentedPartyIndex());
        if (null != partyDetailsElement && null != partyDetailsElement.getValue()) {
            PartyDetails partyDetails = partyDetailsElement.getValue();
            emailService.send(
                partyDetails.getEmail(),
                EmailTemplateNames.CA_DA_APPLICANT_RESPONDENT_NOC,
                noticeOfChangeContentProvider.buildNoticeOfChangeEmail(caseData, event.getSolicitorName(),
                                                                       partyDetails.getFirstName() + " " + partyDetails.getLastName(), true),
                LanguagePreference.getPreferenceLanguage(caseData)
            );
        }

        //PRL-3211 - notify other parties except litigant
        Map<String, String> othersToNotify = new HashMap<>();
        othersToNotify.putAll(CaseUtils.getApplicantsToNotify(caseData, null));
        othersToNotify.putAll(CaseUtils.getRespondentsToNotify(caseData, null != partyDetailsElement ? partyDetailsElement.getId() : null));
        othersToNotify.putAll(CaseUtils.getOthersToNotify(caseData));
        if (!othersToNotify.isEmpty()) {
            othersToNotify.forEach(
                (key, value) -> emailService.send(
                    key,
                    EmailTemplateNames.CA_DA_OTHER_PARTIES_NOC,
                    noticeOfChangeContentProvider.buildNoticeOfChangeEmail(caseData, event.getSolicitorName(), value, true),
                    LanguagePreference.getPreferenceLanguage(caseData)
                ));
        }

        //PRL-3211 - notify applicants/respondents LRs
        Map<String, String> solicitorsToNotify = new HashMap<>();
        solicitorsToNotify.putAll(CaseUtils.getApplicantSolicitorsToNotify(caseData));
        solicitorsToNotify.putAll(CaseUtils.getRespondentSolicitorsToNotify(caseData));
        if (!solicitorsToNotify.isEmpty()) {
            solicitorsToNotify.forEach(
                (key, value) -> emailService.send(
                    key,
                    EmailTemplateNames.CA_DA_OTHER_PARTIES_NOC,
                    noticeOfChangeContentProvider.buildNoticeOfChangeEmail(caseData, event.getSolicitorName(), value, false),
                    LanguagePreference.getPreferenceLanguage(caseData)
                ));
        }
    }
}
