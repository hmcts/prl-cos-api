package uk.gov.hmcts.reform.prl.services.pin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.CARESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DAAPPLICANT;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole.Representing.DARESPONDENT;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CaseInviteManager {
    private final LaunchDarklyClient launchDarklyClient;
    private final C100CaseInviteService c100CaseInviteService;
    private final FL401CaseInviteService fl401CaseInviteService;

    public Map<String, Object> generatePinAndSendNotificationEmail(CaseData caseData) {
        Map<String, Object> caseDataMap = new HashMap<>();
        if (launchDarklyClient.isFeatureEnabled("generate-pin")) {
            log.info("Generating and sending PIN to respondents");
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                caseDataMap = c100CaseInviteService.generateAndSendCaseInvite(caseData);
            } else {
                caseDataMap = fl401CaseInviteService.generateAndSendCaseInvite(caseData);
            }
        }
        return caseDataMap;
    }

    public CaseData reGeneratePinAndSendNotificationEmail(CaseData caseData) {

        caseData = caseData.toBuilder().caseInvites(new ArrayList<>()).build();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = c100CaseInviteService.generateAndSendCaseInvite(caseData);
        } else {
            caseData = fl401CaseInviteService.generateAndSendCaseInvite(caseData);
        }
        return caseData;
    }

    public CaseInvite generatePinAfterLegalRepresentationRemoved(Element<PartyDetails> newRepresentedPartyDetails,
                                                                 SolicitorRole solicitorRole) {
        CaseInvite caseInvite = null;
        if (Yes.equals(newRepresentedPartyDetails.getValue().getCanYouProvideEmailAddress())) {
            if (CARESPONDENT.equals(solicitorRole.getRepresenting())) {
                caseInvite = c100CaseInviteService.generateCaseInvite(newRepresentedPartyDetails, No);
            } else if (CAAPPLICANT.equals(solicitorRole.getRepresenting())
                && launchDarklyClient.isFeatureEnabled("generate-ca-citizen-applicant-pin")) {
                caseInvite = c100CaseInviteService.generateCaseInvite(newRepresentedPartyDetails, Yes);
            } else if (DARESPONDENT.equals(solicitorRole.getRepresenting())) {
                caseInvite = fl401CaseInviteService.generateCaseInvite(
                    newRepresentedPartyDetails.getValue(),
                    YesOrNo.No
                );
            } else if (DAAPPLICANT.equals(solicitorRole.getRepresenting())
                && launchDarklyClient.isFeatureEnabled("generate-da-citizen-applicant-pin")) {
                caseInvite = fl401CaseInviteService.generateCaseInvite(
                    newRepresentedPartyDetails.getValue(),
                    YesOrNo.Yes
                );
            }
        }
        return caseInvite;
    }

}
