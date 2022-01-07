package uk.gov.hmcts.reform.prl.tasks.emails;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.framework.context.TaskContext;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;
import uk.gov.hmcts.reform.prl.tasks.emails.generics.SendEmailTask;

@Component
public class SolicitorEmailTask extends SendEmailTask {

    protected SolicitorEmailTask(EmailService emailService) {
        super(emailService);
    }

//    protected String getRecipientEmail(UserDetails userDetails) {
//
//        return "prl_caseworker_solicitor@mailinator.com";

    }
    @Override
    protected EmailTemplateNames getTemplate() {
        return EmailTemplateNames.EXAMPLE;
    }

    @Override
    protected String getRecipientEmail(CaseDetails caseDetails) {
        return "prl_caseworker_solicitor@mailinator.com";
    }


    @Override
    protected EmailTemplateVars getPersonalisation(TaskContext context, CaseDetails caseDetails) {
        // This is fake, we should get these details from CaseDetails case data, but it's just to show a concept
        String applicantName = caseDetails
            .getCaseData()
            .getApplicants()
            .stream()
            .map(element -> element.getValue().getFirstName() + element.getValue().getLastName()).toString();
        return SolicitorEmail.builder()
            .caseReference(caseDetails.getCaseId())
            .caseName(caseDetails.getCaseData().getApplicantCaseName())
            .applicantName(applicantName)
            .courtName("court name")
            .fullName("userDetails.getFullName()")
            .courtEmail("C100applications@justice.gov.uk")
            .build();
    }
    @Override
    protected LanguagePreference getLanguage(CaseDetails caseDetails) {
        return LanguagePreference.ENGLISH;
    }
}
