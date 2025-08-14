package uk.gov.hmcts.reform.prl.models;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class LanguagePreferenceTest {

    @Test
    public void getLanguagePreferenceShouldReturnDefaultValueWhenEmptyCaseData() {
        assertThat(
            LanguagePreference.getLanguagePreference(CaseData.builder().build()),
            is(LanguagePreference.english)
        );
    }

    @Test
    public void getLanguagePreferenceShouldReturnWelsh() {
        assertThat(
            LanguagePreference.getLanguagePreference(
                CaseData.builder().languagePreferenceWelsh(YesOrNo.Yes).build()),
            is(LanguagePreference.welsh)
        );
    }

    @Test
    public void getLanguagePreferenceShouldReturnEnglish() {
        assertThat(
            LanguagePreference.getLanguagePreference(
                CaseData.builder().languagePreferenceWelsh(YesOrNo.No).build()),
            is(LanguagePreference.english)
        );
    }

    @Test
    public void valuesShouldReturnListOfTwoElements() {
        assertThat(LanguagePreference.values().length, is(2));
    }
}
