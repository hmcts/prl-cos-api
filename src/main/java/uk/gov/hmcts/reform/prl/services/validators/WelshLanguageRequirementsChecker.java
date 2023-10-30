package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.TaskErrorService;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.WELSH_LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.prl.enums.EventErrorsEnum.WELSH_LANGUAGE_ERROR;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.welsh;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class WelshLanguageRequirementsChecker implements EventChecker {

    private final TaskErrorService taskErrorService;

    @Override
    public boolean isFinished(CaseData caseData) {

        Optional<YesOrNo> welshLanguageRequirement = ofNullable(caseData.getWelshLanguageRequirement());
        Optional<LanguagePreference> applicationLanguage = ofNullable(caseData.getWelshLanguageRequirementApplication());
        Optional<YesOrNo> englishRequirements = ofNullable(caseData.getWelshLanguageRequirementApplicationNeedEnglish());
        Optional<YesOrNo> welshRequirements = ofNullable(caseData.getLanguageRequirementApplicationNeedWelsh());

        if (welshLanguageRequirement.isPresent() && welshLanguageRequirement.get().equals(No)) {
            taskErrorService.removeError(WELSH_LANGUAGE_ERROR);
            return true;
        }
        if (applicationLanguage.isPresent()
            && applicationLanguage.get().equals(english)
            && welshRequirements.isPresent()) {
            taskErrorService.removeError(WELSH_LANGUAGE_ERROR);
            return true;
        }
        if (applicationLanguage.isPresent()
            && applicationLanguage.get().equals(welsh)
            && englishRequirements.isPresent()) {
            taskErrorService.removeError(WELSH_LANGUAGE_ERROR);
            return true;
        }
        return false;
    }

    @Override
    public boolean isStarted(CaseData caseData) {

        Optional<YesOrNo> welshLanguageRequirement = ofNullable(caseData.getWelshLanguageRequirement());

        if (welshLanguageRequirement.isPresent() && welshLanguageRequirement.get().equals(Yes)) {
            taskErrorService.addEventError(WELSH_LANGUAGE_REQUIREMENTS, WELSH_LANGUAGE_ERROR,
                                           WELSH_LANGUAGE_ERROR.getError()
            );

            return true;
        }
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        return false;
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }
}
