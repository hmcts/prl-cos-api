package uk.gov.hmcts.reform.prl.services.pin;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.notify.CaseInviteEmail;
import uk.gov.hmcts.reform.prl.models.dto.notify.EmailTemplateVars;
import uk.gov.hmcts.reform.prl.models.dto.notify.SolicitorEmail;
import uk.gov.hmcts.reform.prl.models.email.EmailTemplateNames;
import uk.gov.hmcts.reform.prl.services.EmailService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.getElement;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.unwrapElements;

@Slf4j
@Service
public class C100CaseInviteService implements CaseInviteService{

    @Autowired
    EmailService emailService;

    @Override
    public CaseInvite generateRespondentCaseInvite(Element<PartyDetails> partyDetails) {

        return new CaseInvite().generateAccessCode(partyDetails.getValue().getEmail(), partyDetails.getId());
    }

    //need to include logic if someone does not enter an email
    public CaseData generateCaseInviteForAllRespondentWithEmailPresent(CaseData caseData) {
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvite() != null ? caseData.getCaseInvite() : new ArrayList<>();

        for (Element<PartyDetails> respondent : caseData.getRespondents()) {
            CaseInvite caseInvite = generateRespondentCaseInvite(respondent);
            caseInvites.add(element(caseInvite));
            sendCaseInvite(caseInvite, respondent.getValue(), caseData);
        }
        return caseData.toBuilder().caseInvite(caseInvites).build();
    }

    @Override
    public void sendCaseInvite(CaseInvite caseInvite, PartyDetails partyDetails, CaseData caseData) {
        EmailTemplateVars email = buildCaseInviteEmail(caseInvite, partyDetails, caseData);
        sendEmail(caseInvite.getCaseInviteEmail(), email);
    }

    public EmailTemplateVars buildCaseInviteEmail(CaseInvite caseInvite, PartyDetails partyDetails, CaseData caseData) {
        return new CaseInviteEmail(caseInvite, String.valueOf(caseData.getId()), partyDetails);
    }

    public void sendEmail(String address, EmailTemplateVars email ) {
        log.info("Sending case invite PIN");
        emailService.send(
            address,
            EmailTemplateNames.CASE_INVITE,
            email,
            LanguagePreference.english
        );
    }






}
