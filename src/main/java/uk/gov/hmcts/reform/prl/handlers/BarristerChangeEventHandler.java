package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.events.BarristerChangeEvent;
import uk.gov.hmcts.reform.prl.exception.GovUkNotificationException;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.BarristerEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BarristerChangeEventHandler {
    @Value("${xui.url}")
    private String manageCaseUrl;

    private final EmailService emailService;

    @Async
    @EventListener(condition = "#event.typeOfEvent.displayedValue eq 'Add Barrister'")
    public void notifyAddBarrister(final BarristerChangeEvent event) {

        // notify - barrister
        ignoreAndLogNotificationFailures(
            () -> sendEmailToBarrister(event, EmailTemplateNames.CA_DA_ADD_BARRISTER_SELF));

        // notify applicants/respondents Solicitors
        ignoreAndLogNotificationFailures(
            () -> sendEmailToAppRespSolicitors(event, EmailTemplateNames.CA_DA_ADD_BARRISTER_TO_SOLICITOR));

    }

    private void sendEmailToBarrister(BarristerChangeEvent event, EmailTemplateNames emailTemplateName) {
        CaseData caseData = event.getCaseData();
        AllocatedBarrister barrister = caseData.getAllocatedBarrister();
        if (null != barrister.getBarristerEmail()) {
            log.info("Sending Barrister email on case id {}", caseData.getId());

            emailService.send(
                barrister.getBarristerEmail(),
                emailTemplateName,
                buildEmailBarrister(caseData, EMPTY_STRING),
                LanguagePreference.getPreferenceLanguage(caseData)
            );
        } else {
            log.info(
                "Unable to send email to Barrister as the they don't have any email address for case id {}",
                caseData.getId()
            );
        }
    }

    private void sendEmailToAppRespSolicitors(BarristerChangeEvent event, EmailTemplateNames emailTemplateNames) {
        CaseData caseData = event.getCaseData();
        Map<String, String> solicitorsToNotify = new HashMap<>();
        solicitorsToNotify.putAll(CaseUtils.getApplicantSolicitorsToNotify(caseData));
        solicitorsToNotify.putAll(CaseUtils.getRespondentSolicitorsToNotify(caseData));
        if (!solicitorsToNotify.isEmpty()) {
            solicitorsToNotify.forEach(
                (key, value) -> emailService.send(
                    key,
                    emailTemplateNames,
                    buildEmailBarrister(caseData, value),
                    LanguagePreference.getPreferenceLanguage(caseData))
            );
        }
    }

    @Async
    @EventListener(condition = "#event.typeOfEvent.displayedValue eq 'Remove Barrister'")
    public void notifyWhenBarristerRemoved(final BarristerChangeEvent event) {
        // notify - barrister
        ignoreAndLogNotificationFailures(
            () -> sendEmailToBarrister(event, EmailTemplateNames.CA_DA_REMOVE_BARRISTER_SELF));

        // notify applicants/respondents Solicitors
        ignoreAndLogNotificationFailures(
            () -> sendEmailToAppRespSolicitors(event, EmailTemplateNames.CA_DA_REMOVE_BARRISTER_TO_SOLICITOR));

    }

    private EmailTemplateVars buildEmailBarrister(CaseData caseData, String solicitorName) {
        AllocatedBarrister allocatedBarrister = caseData.getAllocatedBarrister();
        return BarristerEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .barristerName(allocatedBarrister.getBarristerFullName())
            .solicitorName(solicitorName)
            .caseLink(manageCaseUrl + URL_STRING + caseData.getId())
            .issueDate(CommonUtils.formatDate(D_MMM_YYYY, caseData.getIssueDate()))
            .build();
    }

    private void ignoreAndLogNotificationFailures(Runnable emailTask) {
        try {
            emailTask.run();
        } catch (GovUkNotificationException e) {
            log.error(e.getMessage(), e);
        }
    }
}
