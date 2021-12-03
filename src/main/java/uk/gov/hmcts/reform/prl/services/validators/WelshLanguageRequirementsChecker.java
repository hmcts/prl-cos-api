package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.ENGLISH;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.WELSH;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.NO;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.YES;

@Service
public class WelshLanguageRequirementsChecker implements EventChecker {

    @Override
    public boolean isFinished(CaseData caseData) {

        if (caseData.getWelshLanguageRequirement() != null && caseData.getWelshLanguageRequirement().equals(NO)) {
            return true;
        }
        boolean languageUsedCompleted = caseData.getWelshLanguageRequirementApplication() != null;
        LanguagePreference languagePreference = caseData.getWelshLanguageRequirementApplication();

        if (languageUsedCompleted && languagePreference.equals(ENGLISH)) {
            return caseData.getLanguageRequirementApplicationNeedWelsh() != null;
        }
        if (languageUsedCompleted && languagePreference.equals(WELSH)) {
            return caseData.getWelshLanguageRequirementApplicationNeedEnglish() != null;
        }
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return caseData.getWelshLanguageRequirement() != null && caseData.getWelshLanguageRequirement().equals(YES);
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }
}
