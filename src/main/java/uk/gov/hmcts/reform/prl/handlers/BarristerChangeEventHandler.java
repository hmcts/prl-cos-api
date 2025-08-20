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
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.BarristerEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.utils.CommonUtils;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.D_MMM_YYYY;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.URL_STRING;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BarristerChangeEventHandler {
    @Value("${xui.url}")
    private String manageCaseUrl;

    @Value("${citizen.url}")
    private String citizenUrl;

    private final EmailService emailService;

    @Async
    @EventListener(condition = "#event.typeOfEvent.displayedValue eq 'Add Barrister'")
    public void notifyBarrister(final BarristerChangeEvent event) {
        CaseData caseData = event.getCaseData();
        sendEmailToBarrister(caseData, event, EmailTemplateNames.CA_DA_ADD_BARRISTER_SELF);
    }

    private void sendEmailToBarrister(CaseData caseData, BarristerChangeEvent event, EmailTemplateNames emailTemplateName) {
        if (null != event.getAllocatedBarrister().getBarristerEmail()) {
            log.info("Sending Barrister email on case id {}", caseData.getId());

            emailService.send(
                event.getAllocatedBarrister().getBarristerEmail(),
                emailTemplateName,
                buildEmailBarrister(caseData, event.getAllocatedBarrister().getBarristerFullName()),
                LanguagePreference.getPreferenceLanguage(caseData)
            );
        } else {
            log.info(
                "Unable to send email to Barrister as the they don't have any email address for case id {}",
                caseData.getId()
            );
        }
    }

    @Async
    @EventListener(condition = "#event.typeOfEvent.displayedValue eq 'Remove Barrister'")
    public void notifyWhenBarristerRemoved(final BarristerChangeEvent event) {
        CaseData caseData = event.getCaseData();
        sendEmailToBarrister(caseData, event, EmailTemplateNames.CA_DA_REMOVE_BARRISTER_SELF);
    }

    private EmailTemplateVars buildEmailBarrister(CaseData caseData,
                                                    String barristerName) {
        return BarristerEmail.builder()
            .caseReference(String.valueOf(caseData.getId()))
            .caseName(caseData.getApplicantCaseName())
            .barristerName(barristerName)
            .solicitorName(barristerName)
            .caseLink(manageCaseUrl + URL_STRING + caseData.getId())
            .issueDate(CommonUtils.formatDate(D_MMM_YYYY, caseData.getIssueDate()))
            .build();
    }
}
