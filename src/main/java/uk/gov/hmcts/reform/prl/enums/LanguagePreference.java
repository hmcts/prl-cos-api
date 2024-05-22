package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Optional;

@RequiredArgsConstructor
@Getter
@JsonSerialize(using = CustomEnumSerializer.class)
public enum LanguagePreference {

    @JsonProperty("english")
    english("English"),
    @JsonProperty("welsh")
    welsh("Welsh");

    private final String displayedValue;

    public static LanguagePreference getLanguagePreference(CaseData caseData) {
        boolean preferredLanguageIsWelsh = Optional.ofNullable(caseData.getLanguagePreferenceWelsh())
            .map(YesOrNo.Yes::equals)
            .orElse(false);

        return preferredLanguageIsWelsh ? LanguagePreference.welsh : LanguagePreference.english;
    }

    public static LanguagePreference getPreferenceLanguage(CaseData caseData) {
        return YesOrNo.Yes.equals(caseData.getWelshLanguageRequirement())
            && welsh.equals(caseData.getWelshLanguageRequirementApplication()) ? LanguagePreference.welsh : LanguagePreference.english;
    }
}
