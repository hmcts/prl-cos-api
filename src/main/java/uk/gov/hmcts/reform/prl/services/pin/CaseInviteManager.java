package uk.gov.hmcts.reform.prl.services.pin;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.config.launchdarkly.LaunchDarklyClient;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@Slf4j
@Service
public class CaseInviteManager {
    @Autowired
    private LaunchDarklyClient launchDarklyClient;
    @Autowired
    private C100CaseInviteService c100CaseInviteService;
    @Autowired
    private FL401CaseInviteService fl401CaseInviteService;

    public CaseData generatePinAndSendNotificationEmail(CaseData caseData) {
        if (launchDarklyClient.isFeatureEnabled("generate-pin")) {
            log.info("Generating and sending PIN to respondents");
            if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                caseData = c100CaseInviteService.generateAndSendRespondentCaseInvite(caseData);
            } else {
                caseData = fl401CaseInviteService.generateAndSendRespondentCaseInvite(caseData);
            }
        }
        return caseData;
    }

    public CaseData reGeneratePinAndSendNotificationEmail(CaseData caseData) {

        caseData = caseData.toBuilder().respondentCaseInvites(new ArrayList<>()).build();
        if (C100_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            caseData = c100CaseInviteService.generateAndSendRespondentCaseInvite(caseData);
        } else {
            caseData = fl401CaseInviteService.generateAndSendRespondentCaseInvite(caseData);
        }
        return caseData;
    }


}
