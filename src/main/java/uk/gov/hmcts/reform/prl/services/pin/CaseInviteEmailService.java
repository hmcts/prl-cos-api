package uk.gov.hmcts.reform.prl.services.pin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CaseInviteEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInviteEmailService {

    private final EmailService emailService;

    @Value("${xui.url}")
    private String manageCaseUrl;
    @Value("${citizen.url}")
    private String citizenSignUpLink;


    public EmailTemplateVars buildCaseInviteEmail(CaseInvite caseInvite, PartyDetails partyDetails, CaseData caseData) {
        return new CaseInviteEmail(caseInvite, String.valueOf(caseData.getId()),
                                   partyDetails, manageCaseUrl, citizenSignUpLink, caseData
        );
    }

    public void sendEmail(String address, EmailTemplateVars email) {
        log.info("Sending case invite PIN");
        emailService.send(
            address,
            EmailTemplateNames.CASE_INVITE,
            email,
            LanguagePreference.english
        );
    }

    public void sendCaseInviteEmail(CaseInvite caseInvite, PartyDetails partyDetails, CaseData caseData) {
        EmailTemplateVars email = buildCaseInviteEmail(caseInvite, partyDetails, caseData);
        sendEmail(caseInvite.getCaseInviteEmail(), email);

    }
}
