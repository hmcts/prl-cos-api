package uk.gov.hmcts.reform.prl.services.pin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
public class C100CaseInviteService implements CaseInviteService {

    @Autowired
    CaseInviteEmailService caseInviteEmailService;

    @Autowired
    private LaunchDarklyClient launchDarklyClient;

    private CaseInvite generateRespondentCaseInvite(Element<PartyDetails> partyDetails) {
        log.info("***Partydetails when returning case invite *** {}", partyDetails);
        return new CaseInvite().generateAccessCode(partyDetails.getValue().getEmail(), partyDetails.getId());
    }

    private void sendCaseInvite(CaseInvite caseInvite, PartyDetails partyDetails, CaseData caseData) {
        log.info(" **** Party Details **** {}",partyDetails);
        caseInviteEmailService.sendCaseInviteEmail(caseInvite, partyDetails, caseData);
    }

    @Override
    public CaseData generateAndSendRespondentCaseInvite(CaseData caseData) {
        List<Element<CaseInvite>> caseInvites = caseData.getRespondentCaseInvites() != null ? caseData.getRespondentCaseInvites() : new ArrayList<>();

        log.info("Generating case invites and sending notification to respondents with email address present");

        for (Element<PartyDetails> respondent : caseData.getRespondents()) {
            if (!respondentHasLegalRepresentation(respondent.getValue()) && Yes.equals(respondent.getValue().getCanYouProvideEmailAddress())) {
                log.info("Respondent data {}", respondent);
                CaseInvite caseInvite = generateRespondentCaseInvite(respondent);
                caseInvites.add(element(caseInvite));
                sendCaseInvite(caseInvite, respondent.getValue(), caseData);
            }
        }
        if (launchDarklyClient.isFeatureEnabled("generate-da-citizen-applicant-pin")) {
            for (Element<PartyDetails> applicant : caseData.getApplicants()) {
                CaseInvite caseInvite = generateRespondentCaseInvite(applicant);
                caseInvites.add(element(caseInvite));
                sendCaseInvite(caseInvite, applicant.getValue(), caseData);
            }
        }
        return caseData.toBuilder().respondentCaseInvites(caseInvites).build();
    }

    public boolean respondentHasLegalRepresentation(PartyDetails partyDetails) {
        return yes.equals(partyDetails.getDoTheyHaveLegalRepresentation());
    }

}
