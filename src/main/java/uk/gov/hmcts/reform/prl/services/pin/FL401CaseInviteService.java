package uk.gov.hmcts.reform.prl.services.pin;

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


    private CaseInvite generateCaseInvite(PartyDetails partyDetails, YesOrNo isApplicant) {
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

    public CaseData generateAndSendCaseInviteForFL401Respondent(CaseData caseData, PartyDetails partyDetails) {
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites() != null ? caseData.getCaseInvites() : new ArrayList<>();
        if (launchDarklyClient.isFeatureEnabled("generate-pin")) {

            log.info("Generating case invites and sending notification to FL401 respondent with email address present");

            CaseInvite caseInvite = generateCaseInvite(partyDetails, No);
            caseInvites.add(element(caseInvite));
            sendCaseInvite(caseInvite, partyDetails, caseData);
            log.info("Case invite generated and sent" + caseInvite);
        }
        return caseData.toBuilder().caseInvites(caseInvites).build();
    }

    public CaseData generateAndSendCaseInviteEmailForFL401Citizen(CaseData caseData, PartyDetails partyDetails) {
        List<Element<CaseInvite>> caseInvites = caseData.getCaseInvites() != null ? caseData.getCaseInvites() : new ArrayList<>();
        if (launchDarklyClient.isFeatureEnabled("generate-da-citizen-applicant-pin")
            && CaseCreatedBy.CITIZEN.equals(caseData.getCaseCreatedBy())) {
            log.info("Generating case invites and sending notification to FL401 citizen applicants with email");
            if (YesNoDontKnow.no.equals(partyDetails.getDoTheyHaveLegalRepresentation())
                && Yes.equals(partyDetails.getCanYouProvideEmailAddress())) {
                CaseInvite caseInvite = generateCaseInvite(partyDetails, Yes);
                caseInvites.add(element(caseInvite));
                sendCaseInvite(caseInvite, partyDetails, caseData);
                log.info("Case invite generated and sent" + caseInvite);
            }

        }
        return caseData.toBuilder().caseInvites(caseInvites).build();
    }

    public boolean respondentHasLegalRepresentation(PartyDetails partyDetails) {
        return yes.equals(partyDetails.getDoTheyHaveLegalRepresentation());
    }
}
