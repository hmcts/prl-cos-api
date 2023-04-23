package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.repositories.CcdCaseApi;

import java.util.Optional;

@RequiredArgsConstructor
@Getter
@JsonSerialize(using = CustomEnumSerializer.class)
public enum LanguagePreference {

    @JsonProperty("english")
    english("English"),
    @JsonProperty("welsh")
    welsh("Welsh");

    private static final Logger LOGGER = LoggerFactory.getLogger(CcdCaseApi.class);
    private final String displayedValue;

    public static LanguagePreference getLanguagePreference(CaseData caseData) {
        boolean preferredLanguageIsWelsh = Optional.ofNullable(caseData.getLanguagePreferenceWelsh())
            .map(YesOrNo.Yes::equals)
            .orElse(false);
        LOGGER.info("****************The Language preference "
                        + "for email I received is ***************: "
                        + "{}", caseData.getLanguagePreferenceWelsh());
        return preferredLanguageIsWelsh ? LanguagePreference.welsh : LanguagePreference.english;
    }

    public static LanguagePreference getWelshLanguageRequirementApplication(CaseData caseData) {
        if (caseData.getWelshLanguageRequirementApplication() != null) {
            return caseData.getWelshLanguageRequirementApplication();
        } else {
            return LanguagePreference.english;
        }
    }

    public static LanguagePreference getPreferenceLanguage(CaseData caseData) {
        if (caseData.getWelshLanguageRequirementApplication() != null) {
            return caseData.getWelshLanguageRequirementApplication();
        } else {
            return LanguagePreference.english;
        }
    }
}
