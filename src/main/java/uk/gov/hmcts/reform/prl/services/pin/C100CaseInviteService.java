package uk.gov.hmcts.reform.prl.services.pin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.utils.CaseUtils.hasLegalRepresentation;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;



@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C100CaseInviteService implements CaseInviteService {

    private final CaseInviteEmailService caseInviteEmailService;
    private final LaunchDarklyClient launchDarklyClient;

    public CaseInvite generateCaseInvite(Element<PartyDetails> partyDetails, YesOrNo isApplicant) {
        return new CaseInvite().generateAccessCode(partyDetails.getValue().getEmail(), partyDetails.getId(), isApplicant);
    }

    private void sendCaseInvite(CaseInvite caseInvite, PartyDetails partyDetails, CaseData caseData) {
        caseInviteEmailService.sendCaseInviteEmail(caseInvite, partyDetails, caseData);
    }

    @Override
    public CaseData sendCaseInviteEmail(CaseData caseData) {
        log.info("Generating case invites and sending notification to applicants/respondents with email address present");

        for (Element<PartyDetails> respondent : caseData.getRespondents()) {
            if (!hasLegalRepresentation(respondent.getValue()) && Yes.equals(respondent.getValue().getCanYouProvideEmailAddress())) {
                sendCaseInvite(CaseUtils.getCaseInvite(respondent.getId(), caseData.getCaseInvites()), respondent.getValue(), caseData);
            }
        }
        //PRLC100-431 - Generate case invites & send notification to c100 applicants for case created/submitted by citizen
        if (launchDarklyClient.isFeatureEnabled("generate-ca-citizen-applicant-pin")
            && CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            log.info("Generating case invites and sending notification to citizen applicants with email");
            for (Element<PartyDetails> applicant : caseData.getApplicants()) {
                if (!hasLegalRepresentation(applicant.getValue()) && Yes.equals(applicant.getValue().getCanYouProvideEmailAddress())) {
                    sendCaseInvite(CaseUtils.getCaseInvite(applicant.getId(), caseData.getCaseInvites()), applicant.getValue(), caseData);
                }
            }
        }
        return caseData;
    }

    public List<Element<CaseInvite>> generateAndSendCaseInviteForCaRespondent(CaseData caseData, Element<PartyDetails> partyDetails) {
        List<Element<CaseInvite>> caseInvites = new ArrayList<>();
        if ((YesNoDontKnow.no.equals(partyDetails.getValue().getDoTheyHaveLegalRepresentation()) || YesNoDontKnow.dontKnow.equals(
            partyDetails.getValue().getDoTheyHaveLegalRepresentation()))
            && Yes.equals(partyDetails.getValue().getCanYouProvideEmailAddress())) {
            log.info("Generating case invites and sending notification to C100 respondent with email address present");
            CaseInvite caseInvite = generateCaseInvite(partyDetails, No);
            caseInvites.add(element(caseInvite));
            sendCaseInvite(caseInvite, partyDetails.getValue(), caseData);
        }
        return caseInvites;
    }

    public List<Element<CaseInvite>> generateAndSendCaseInviteEmailForCaApplicant(CaseData caseData, Element<PartyDetails> applicant) {
        List<Element<CaseInvite>> caseInvites = new ArrayList<>();
        log.info("Generating case invites and sending notification to C100 citizen applicants with email");
        if (YesNoDontKnow.no.equals(applicant.getValue().getDoTheyHaveLegalRepresentation())
            && Yes.equals(applicant.getValue().getCanYouProvideEmailAddress())) {
            CaseInvite caseInvite = generateCaseInvite(applicant, Yes);
            caseInvites.add(element(caseInvite));
            sendCaseInvite(caseInvite, applicant.getValue(), caseData);
        }
        return caseInvites;
    }

    public List<Element<CaseInvite>> generateAndSendCaseInviteForAllC100AppAndResp(CaseData caseData) {
        List<Element<CaseInvite>> caseInvites = new ArrayList<>();
        for (Element<PartyDetails> respondent : caseData.getRespondents()) {
            if (!hasLegalRepresentation(respondent.getValue()) && Yes.equals(respondent.getValue().getCanYouProvideEmailAddress())) {
                CaseInvite caseInvite = generateCaseInvite(respondent, No);
                caseInvites.add(element(caseInvite));
                sendCaseInvite(caseInvite, respondent.getValue(), caseData);
            }
        }
        for (Element<PartyDetails> applicant : caseData.getApplicants()) {
            if (!hasLegalRepresentation(applicant.getValue()) && Yes.equals(applicant.getValue().getCanYouProvideEmailAddress())) {
                CaseInvite caseInvite = generateCaseInvite(applicant, Yes);
                caseInvites.add(element(caseInvite));
                sendCaseInvite(caseInvite, applicant.getValue(), caseData);
            }
        }
        return caseInvites;
    }

}
