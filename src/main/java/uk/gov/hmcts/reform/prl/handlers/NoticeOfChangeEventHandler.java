package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.events.NoticeOfChangeEvent;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangeContentProvider;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class NoticeOfChangeEventHandler {
    private final EmailService emailService;
    private final NoticeOfChangeContentProvider noticeOfChangeContentProvider;
    private final LaunchDarklyClient launchDarklyClient;

    @Async
    @EventListener(condition = "#event.typeOfEvent eq 'Add Legal Representation'")
    public void notifyLegalRepresentative(final NoticeOfChangeEvent event) {
        CaseData caseData = event.getCaseData();
        //PRL-3211 - notify new LR
        sendEmailToSolicitor(caseData, event, EmailTemplateNames.CA_DA_SOLICITOR_NOC);

        //PRL-3211 - notify LiP
        sendEmailToLitigant(caseData, event, EmailTemplateNames.CA_DA_APPLICANT_RESPONDENT_NOC, false);

        //PRL-3211 - notify applicants/respondents other parties except litigant
        sendEmailToApplicantsRespondents(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_NOC, false);

        //PRL-3211 - notify other persons if any
        sendEmailToOtherParties(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_NOC, false);

        //PRL-3211 - notify applicants/respondents LRs
        sendEmailToAppRespSolicitors(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_NOC);
    }

    private void sendEmailToAppRespSolicitors(CaseData caseData, NoticeOfChangeEvent event, EmailTemplateNames emailTemplateNames) {
        Map<String, String> solicitorsToNotify = new HashMap<>();
        solicitorsToNotify.putAll(CaseUtils.getApplicantSolicitorsToNotify(caseData));
        solicitorsToNotify.putAll(CaseUtils.getRespondentSolicitorsToNotify(caseData));
        if (!solicitorsToNotify.isEmpty()) {
            solicitorsToNotify.forEach(
                (key, value) -> {
                    if (!key.equalsIgnoreCase(event.getSolicitorEmailAddress())) {
                        emailService.send(
                            key,
                            emailTemplateNames,
                            noticeOfChangeContentProvider.buildNocEmailSolicitor(caseData, event.getSolicitorName()),
                            LanguagePreference.getPreferenceLanguage(caseData)
                        );
                    }
                });
        }
    }

    private void sendEmailToOtherParties(CaseData caseData, NoticeOfChangeEvent event,
                                         EmailTemplateNames emailTemplateNames, boolean isRemoveLegalRep) {
        Map<String, String> othersToNotify = CaseUtils.getOthersToNotify(caseData);
        if (!othersToNotify.isEmpty()) {
            othersToNotify.forEach(
                (key, value) -> emailService.send(
                    key,
                    emailTemplateNames,
                    noticeOfChangeContentProvider.buildNocEmailCitizen(
                        caseData,
                        event.getSolicitorName(),
                        value,
                        true,
                        isRemoveLegalRep,
                        ""
                    ),
                    LanguagePreference.getPreferenceLanguage(caseData)
                ));
        }
    }

    private void sendEmailToApplicantsRespondents(CaseData caseData, NoticeOfChangeEvent event,
                                                  EmailTemplateNames emailTemplateNames, boolean isRemoveLegalRep) {
        Element<PartyDetails> partyElement = getLitigantParty(caseData, event);
        Map<String, String> applicantsRespondentsToNotify = new HashMap<>();
        applicantsRespondentsToNotify.putAll(CaseUtils.getApplicantsToNotify(
            caseData,
            null != partyElement ? partyElement.getId() : null
        ));
        applicantsRespondentsToNotify.putAll(CaseUtils.getRespondentsToNotify(
            caseData,
            null != partyElement ? partyElement.getId() : null
        ));
        if (!applicantsRespondentsToNotify.isEmpty()) {
            applicantsRespondentsToNotify.forEach(
                (key, value) -> emailService.send(
                    key,
                    emailTemplateNames,
                    noticeOfChangeContentProvider.buildNocEmailCitizen(
                        caseData,
                        event.getSolicitorName(),
                        value,
                        false,
                        isRemoveLegalRep,
                        ""
                    ),
                    LanguagePreference.getPreferenceLanguage(caseData)
                ));
        }
    }

    private void sendEmailToLitigant(CaseData caseData, NoticeOfChangeEvent event, EmailTemplateNames emailTemplateName, boolean isRemoveLegalRep) {
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
                                                                       isRemoveLegalRep,
                                                                       event.getAccessCode()
                    ),
                    LanguagePreference.getPreferenceLanguage(caseData)
                );
            } else {
                log.info(
                    "Unable to send email to LiP as the they don't have any email address for case id {}",
                    caseData.getId()
                );
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
            log.info(
                "Unable to send email to Solicitor as the they don't have any email address for case id {}",
                caseData.getId()
            );
        }
    }

    private Element<PartyDetails> getLitigantParty(CaseData caseData, NoticeOfChangeEvent event) {
        int representingPartyIndex = event.getRepresentedPartyIndex();
        Element<PartyDetails> partyDetailsElement = null;
        switch (event.getRepresenting()) {
            case CAAPPLICANT:
                partyDetailsElement = caseData.getApplicants().get(representingPartyIndex);
                break;
            case CARESPONDENT:
                partyDetailsElement = caseData.getRespondents().get(representingPartyIndex);
                break;
            case DAAPPLICANT:
                partyDetailsElement = ElementUtils.element(
                    caseData.getApplicantsFL401().getPartyId(),
                    caseData.getApplicantsFL401()
                );
                break;
            case DARESPONDENT:
                partyDetailsElement = ElementUtils.element(
                    caseData.getRespondentsFL401().getPartyId(),
                    caseData.getRespondentsFL401()
                );
                break;
            default:
                break;
        }
        return partyDetailsElement;
    }

    @Async
    @EventListener(condition = "#event.typeOfEvent eq 'Remove Legal Representation'")
    public void notifyWhenLegalRepresentativeRemoved(final NoticeOfChangeEvent event) {
        CaseData caseData = event.getCaseData();
        //PRL-3215 - notify old LR
        sendEmailToSolicitor(caseData, event, EmailTemplateNames.CA_DA_REMOVE_SOLICITOR_NOC);

        //Access code will not generate if the case has not reached to Hearing state yet
        if (StringUtils.isNotEmpty(event.getAccessCode())
            && launchDarklyClient.isFeatureEnabled("generate-access-code-for-noc")) {
            //PRL-3215 - notify LiP
            sendEmailToLitigant(caseData, event, EmailTemplateNames.CA_DA_APPLICANT_REMOVE_RESPONDENT_NOC, true);
            //PRL-3215 - notify applicants/respondents other parties except litigant
            sendEmailToApplicantsRespondents(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_REMOVE_NOC, true);
        }

        //PRL-3215 - notify other persons if any
        sendEmailToOtherParties(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_REMOVE_NOC_REVISED, true);

        //PRL-3215 - notify applicants/respondents LRs
        sendEmailToAppRespSolicitors(caseData, event, EmailTemplateNames.CA_DA_OTHER_PARTIES_REMOVE_NOC);
    }
}
