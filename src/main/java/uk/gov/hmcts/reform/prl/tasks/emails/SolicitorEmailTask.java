package uk.gov.hmcts.reform.prl.tasks.emails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.hmcts.reform.prl.services.UserService;
import uk.gov.hmcts.reform.prl.tasks.emails.generics.SendEmailTask;

import java.util.stream.Collectors;

@Slf4j
@Component
public class SolicitorEmailTask extends SendEmailTask {

    protected SolicitorEmailTask(EmailService emailService) {
        super(emailService);
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
        String applicantName = String.valueOf(caseDetails
                      .getCaseData()
                      .getApplicants()
                      .stream()
                      .map(element -> element.getValue().getFirstName() + element.getValue().getLastName())
                      .findFirst());

        EmailTemplateVars emailTemplateVars = SolicitorEmail.builder()
            .caseReference(caseDetails.getCaseId())
            .caseName(caseDetails.getCaseData().getApplicantCaseName())
            .applicantName(applicantName)
            .courtName("court name")
            .fullName("userDetails.getFullName()")
            .courtEmail("C100applications@justice.gov.uk")
            .caseLink("http://localhost:3333/cases/case-details/" + caseDetails.getCaseId())
            .build();


        return emailTemplateVars;
    }
    @Override
    protected LanguagePreference getLanguage(CaseDetails caseDetails) {
        return LanguagePreference.ENGLISH;
    }
}
