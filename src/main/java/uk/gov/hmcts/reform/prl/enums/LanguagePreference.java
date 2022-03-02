package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Optional;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum LanguagePreference {

    @JsonProperty("english")
    ENGLISH("english"),
    @JsonProperty("welsh")
    WELSH("welsh");


    private final String code;

    public static LanguagePreference getLanguagePreference(CaseData caseData) {
        boolean preferredLanguageIsWelsh = Optional.ofNullable(caseData.getLanguagePreferenceWelsh())
            .map(YesOrNo.Yes::equals)
            .orElse(false);

        return preferredLanguageIsWelsh ? LanguagePreference.WELSH : LanguagePreference.ENGLISH;
    }
}
