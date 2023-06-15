package uk.gov.hmcts.reform.prl.services.pin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.YesNoDontKnow.yes;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
public class FL401CaseInviteService implements CaseInviteService {

    @Autowired
    CaseInviteEmailService caseInviteEmailService;

    @Autowired
    private LaunchDarklyClient launchDarklyClient;


    public CaseInvite generateCaseInvite(PartyDetails partyDetails, YesOrNo isApplicant) {
        //no party id required as fl401 cases have only a single respondent
        return new CaseInvite().generateAccessCode(partyDetails.getEmail(), null, isApplicant);
    }

    private void sendCaseInvite(CaseInvite caseInvite, PartyDetails partyDetails, CaseData caseData) {
        caseInviteEmailService.sendCaseInviteEmail(caseInvite, partyDetails, caseData);
    }

    @Override
    public CaseData generateAndSendCaseInvite(CaseData caseData) {
        PartyDetails respondent = caseData.getRespondentsFL401();
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites() != null ? caseData.getCaseInvites() : new ArrayList<>();

        if (!respondentHasLegalRepresentation(respondent) && Yes.equals(respondent.getCanYouProvideEmailAddress())) {
            CaseInvite caseInvite = generateCaseInvite(respondent, YesOrNo.No);
            caseInvites.add(element(caseInvite));
            if (Yes.equals(respondent.getCanYouProvideEmailAddress())) {
                sendCaseInvite(caseInvite, respondent, caseData);
            }
        }

        if (launchDarklyClient.isFeatureEnabled("generate-da-citizen-applicant-pin")) {
            PartyDetails applicant = caseData.getApplicantsFL401();
            CaseInvite caseInvite = generateCaseInvite(applicant, YesOrNo.Yes);
            caseInvites.add(element(caseInvite));
            if (Yes.equals(applicant.getCanYouProvideEmailAddress())) {
                sendCaseInvite(caseInvite, applicant, caseData);
            }
        }
        return caseData.toBuilder().caseInvites(caseInvites).build();
    }

    public List<Element<CaseInvite>> generateAndSendCaseInviteForDaRespondent(CaseData caseData, PartyDetails partyDetails) {
        List<Element<CaseInvite>> caseInvites = new ArrayList<>();
        if (partyDetails.getCanYouProvideEmailAddress() != null) {
            log.info("Generating case invites and sending notification to FL401 respondent with email address present");
            CaseInvite caseInvite = generateCaseInvite(partyDetails, No);
            caseInvites.add(element(caseInvite));
            sendCaseInvite(caseInvite, partyDetails, caseData);
            log.info("Case invite generated and sent" + caseInvite);
        }
        return caseInvites;
    }

    public List<Element<CaseInvite>> generateAndSendCaseInviteForDaApplicant(CaseData caseData, PartyDetails partyDetails) {
        List<Element<CaseInvite>> caseInvites = new ArrayList<>();
        if (YesNoDontKnow.no.equals(partyDetails.getDoTheyHaveLegalRepresentation())
            && Yes.equals(partyDetails.getCanYouProvideEmailAddress())) {
            log.info("Generating case invites and sending notification to FL401 citizen applicants with email");
            CaseInvite caseInvite = generateCaseInvite(partyDetails, Yes);
            caseInvites.add(element(caseInvite));
            sendCaseInvite(caseInvite, partyDetails, caseData);
            log.info("Case invite generated and sent" + caseInvite);
        }
        return caseInvites;
    }

    public boolean respondentHasLegalRepresentation(PartyDetails partyDetails) {
        return yes.equals(partyDetails.getDoTheyHaveLegalRepresentation());
    }
}
