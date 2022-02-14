package uk.gov.hmcts.reform.prl.tasks.emails.generics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.framework.context.TaskContext;
import uk.gov.hmcts.reform.prl.framework.task.Task;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;

@Slf4j
@RequiredArgsConstructor
public abstract class SendEmailTask implements Task<CaseDetails> {

    private final EmailService emailService;

    protected abstract EmailTemplateVars getPersonalisation(TaskContext context, CaseDetails caseDetails);

    protected abstract EmailTemplateNames getTemplate();

    protected abstract String getRecipientEmail(CaseDetails caseDetails);

    protected LanguagePreference getLanguage(CaseDetails caseDetails) {
        return LanguagePreference.getLanguagePreference(caseDetails.getCaseData());
    }

    /**
     * Indicates if notify email client should be called.
     * @param caseDetails This may be use for further computation in overriding classes.
     */
    @SuppressWarnings("unchecked")
    protected boolean canEmailBeSent(CaseDetails caseDetails) {
        return true;
    }

    @Override
    public CaseDetails execute(TaskContext context, CaseDetails caseDetails) {


        final String caseId = caseDetails.getCaseId();
        final String templateName = getTemplate().name();

        if (canEmailBeSent(caseDetails)) {
            log.info("CaseID: {} email {} is going to be sent.", caseId, templateName);

            emailService.send(
                getRecipientEmail(caseDetails),
                getTemplate(),
                getPersonalisation(context, caseDetails),
                getLanguage(caseDetails)
            );

            log.info("CaseID: {} email {} was sent.", caseId, templateName);
        } else {
            log.warn("CaseID: {} email {} will not be sent.", caseId, templateName);
        }

        return caseDetails;
    }
}

