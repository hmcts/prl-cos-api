package uk.gov.hmcts.reform.prl.utils;

import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

@NoArgsConstructor
public class CaseDataProvider {

    public static CaseData empty() {
        return CaseData.builder().build();
    }

    public static CaseData full() {
        return english();
    }

    public static CaseData english() {
        return CaseData.builder().languagePreferenceWelsh(YesOrNo.NO).build();
    }

    public static CaseData welsh() {
        return CaseData.builder().languagePreferenceWelsh(YesOrNo.YES).build();
    }
}
