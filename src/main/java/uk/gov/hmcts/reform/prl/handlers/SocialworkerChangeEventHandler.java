package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.events.SocialWorkerChangeEvent;
import uk.gov.hmcts.reform.prl.exception.GovUkNotificationException;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.localauthority.LocalAuthoritySocialWorker;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.LaSocialWorkerEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.MaskEmail;

import java.util.function.Function;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.getPreferenceLanguage;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.ADD_LA_SOCIAL_WORKER;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.REMOVE_LA_SOCIAL_WORKER;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SocialworkerChangeEventHandler {
    private final MaskEmail maskEmail;
    @Value("${xui.url}")
    private String manageCaseUrl;

    private final EmailService emailService;
    private final FeatureToggleService featureToggleService;

    @Async
    @EventListener(condition = "#event.typeOfEvent.displayedValue eq 'Add Social Worker'")
    public void notifyAddSocialWorker(final SocialWorkerChangeEvent event) {
        if (featureToggleService.isLaSocialWorkerFeatureEnabled()) {
            // notify - LA social worker
            sendEmail(event, ADD_LA_SOCIAL_WORKER, LocalAuthoritySocialWorker::getLaSocialWorkerEmail);
        }
    }

    private void sendEmail(SocialWorkerChangeEvent event,
                           EmailTemplateNames emailTemplateName,
                           Function<LocalAuthoritySocialWorker, String> emailToSend) {
        CaseData caseData = event.getCaseData();
        LocalAuthoritySocialWorker localAuthoritySocialWorker = caseData.getLocalAuthoritySocialWorker();
        String email = emailToSend.apply(localAuthoritySocialWorker);
        if (email != null) {
            log.info(
                "Event: {} - For case id {}, sending email to {}",
                event.getTypeOfEvent().getDisplayedValue(),
                caseData.getId(),
                maskEmail.mask(email)
            );

            ignoreAndLogNotificationFailures(() -> emailService.send(
                email,
                emailTemplateName,
                buildEmailSocialWorker(caseData),
                getPreferenceLanguage(caseData)
            ));
        } else {
            log.info(
                "Unable to send email to {} as the they don't have any email address for case id {}",
                emailTemplateName,
                caseData.getId()
            );
        }
    }

    @Async
    @EventListener(condition = "#event.typeOfEvent.displayedValue eq 'Remove Social Worker'")
    public void notifyWhenSocialWorkerRemoved(final SocialWorkerChangeEvent event) {
        if (featureToggleService.isLaSocialWorkerFeatureEnabled()) {
            // notify - LA social worker
            sendEmail(event, REMOVE_LA_SOCIAL_WORKER, LocalAuthoritySocialWorker::getLaSocialWorkerEmail);
        }
    }

    private EmailTemplateVars buildEmailSocialWorker(CaseData caseData) {
        LocalAuthoritySocialWorker localAuthoritySocialWorker = caseData.getLocalAuthoritySocialWorker();
        return LaSocialWorkerEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .socialWorkerName(localAuthoritySocialWorker.getLaSocialWorkerFullName())
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
