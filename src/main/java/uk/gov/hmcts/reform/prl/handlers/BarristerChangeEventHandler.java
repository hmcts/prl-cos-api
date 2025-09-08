package uk.gov.hmcts.reform.prl.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.events.BarristerChangeEvent;
import uk.gov.hmcts.reform.prl.exception.GovUkNotificationException;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.BarristerEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.services.FeatureToggleService;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;
import uk.gov.hmcts.reform.prl.utils.MaskEmail;

import java.util.function.Function;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_STRING;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.getPreferenceLanguage;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.CA_DA_ADD_BARRISTER_SELF;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.CA_DA_ADD_BARRISTER_TO_SOLICITOR;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.CA_DA_REMOVE_BARRISTER_SELF;
import static uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames.CA_DA_REMOVE_BARRISTER_TO_SOLICITOR;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BarristerChangeEventHandler {
    private final MaskEmail maskEmail;
    @Value("${xui.url}")
    private String manageCaseUrl;

    private final EmailService emailService;
    private final FeatureToggleService featureToggleService;

    @Async
    @EventListener(condition = "#event.typeOfEvent.displayedValue eq 'Add Barrister'")
    public void notifyAddBarrister(final BarristerChangeEvent event) {
        // notify - barrister
        sendEmail(event, CA_DA_ADD_BARRISTER_SELF, AllocatedBarrister::getBarristerEmail);

        // notify - solicitor
        sendEmail(event, CA_DA_ADD_BARRISTER_TO_SOLICITOR, AllocatedBarrister::getSolicitorEmail);
    }

    private void sendEmail(BarristerChangeEvent event,
                           EmailTemplateNames emailTemplateName,
                           Function<AllocatedBarrister, String> emailToSend) {
        CaseData caseData = event.getCaseData();
        String email = emailToSend.apply(caseData.getAllocatedBarrister());
        if (email != null) {
            log.info("Event: {} - For case id {}, sending email to barrister {}",
                     event.getTypeOfEvent().getDisplayedValue(),
                     caseData.getId(),
                     maskEmail.mask(email));

            ignoreAndLogNotificationFailures(() -> emailService.send(
                email,
                emailTemplateName,
                buildEmailBarrister(caseData, EMPTY_STRING),
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
    @EventListener(condition = "#event.typeOfEvent.displayedValue eq 'Remove Barrister'")
    public void notifyWhenBarristerRemoved(final BarristerChangeEvent event) {
        if (featureToggleService.isBarristerFeatureEnabled()) {
            // notify - barrister
            sendEmail(event, CA_DA_REMOVE_BARRISTER_SELF, AllocatedBarrister::getBarristerEmail);

            // notify - solicitor
            sendEmail(event, CA_DA_REMOVE_BARRISTER_TO_SOLICITOR, AllocatedBarrister::getSolicitorEmail);
        }
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
