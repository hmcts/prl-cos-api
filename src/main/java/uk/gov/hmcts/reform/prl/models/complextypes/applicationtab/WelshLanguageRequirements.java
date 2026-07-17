package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class WelshLanguageRequirements {

    @CCD(
            label = "Does any person in this case need orders or documents in Welsh?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo welshLanguageRequirement;
    @CCD(label = "Which language are you using to complete this application?", searchable = false)
    private final LanguagePreference welshLanguageRequirementApplication;
    @CCD(
            label = "Does this application need to be translated into Welsh?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo languageRequirementApplicationNeedWelsh;
    @CCD(
            label = "Does this application need to be translated into English?",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo welshLanguageRequirementApplicationNeedEnglish;


}
