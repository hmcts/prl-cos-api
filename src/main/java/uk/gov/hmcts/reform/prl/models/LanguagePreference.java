package uk.gov.hmcts.reform.prl.models;

import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Optional;

@RequiredArgsConstructor
public enum LanguagePreference {

    ENGLISH("english"),
    WELSH("welsh");

    private final String code;

    public static LanguagePreference getLanguagePreference(CaseData caseData) {
        boolean preferredLanguageIsWelsh = Optional.ofNullable(caseData.getLanguagePreferenceWelsh())
            .map(YesOrNo.YES::equals)
            .orElse(false);

        return preferredLanguageIsWelsh ? LanguagePreference.WELSH : LanguagePreference.ENGLISH;
    }
}
