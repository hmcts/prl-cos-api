package uk.gov.hmcts.reform.prl.services.pin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.hasLegalRepresentation;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
public class C100CaseInviteService implements CaseInviteService {

    @Autowired
    CaseInviteEmailService caseInviteEmailService;

    @Autowired
    private LaunchDarklyClient launchDarklyClient;

    public CaseInvite generateCaseInvite(Element<PartyDetails> partyDetails, YesOrNo isApplicant) {
        return new CaseInvite().generateAccessCode(partyDetails.getValue().getEmail(), partyDetails.getId(), isApplicant);
    }

    private void sendCaseInvite(CaseInvite caseInvite, PartyDetails partyDetails, CaseData caseData) {
        caseInviteEmailService.sendCaseInviteEmail(caseInvite, partyDetails, caseData);
    }

    @Override
    public CaseData generateAndSendCaseInvite(CaseData caseData) {
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites() != null ? caseData.getCaseInvites() : new ArrayList<>();

        log.info("Generating case invites and sending notification to applicants/respondents with email address present");

        for (Element<PartyDetails> respondent : caseData.getRespondents()) {
            if (!hasLegalRepresentation(respondent.getValue()) && Yes.equals(respondent.getValue().getCanYouProvideEmailAddress())) {
                CaseInvite caseInvite = generateCaseInvite(respondent, No);
                caseInvites.add(element(caseInvite));
                sendCaseInvite(caseInvite, respondent.getValue(), caseData);
            }
        }
        //PRLC100-431 - Generate case invites & send notification to c100 applicants for case created/submitted by citizen
        if (launchDarklyClient.isFeatureEnabled("generate-ca-citizen-applicant-pin")
            && CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            log.info("Generating case invites and sending notification to citizen applicants with email");
            for (Element<PartyDetails> applicant : caseData.getApplicants()) {
                if (!hasLegalRepresentation(applicant.getValue()) && Yes.equals(applicant.getValue().getCanYouProvideEmailAddress())) {
                    CaseInvite caseInvite = generateCaseInvite(applicant, Yes);
                    caseInvites.add(element(caseInvite));
                    sendCaseInvite(caseInvite, applicant.getValue(), caseData);
                }
            }
        }
        return caseData.toBuilder().caseInvites(caseInvites).build();
    }

}
