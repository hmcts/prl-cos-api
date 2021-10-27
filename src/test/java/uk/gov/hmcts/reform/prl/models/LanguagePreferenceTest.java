package uk.gov.hmcts.reform.prl.models;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class LanguagePreferenceTest {

    @Test
    public void getLanguagePreferenceShouldReturnDefaultValueWhenEmptyCaseData() {
        assertThat(
            LanguagePreference.getLanguagePreference(CaseData.builder().build()),
            is(LanguagePreference.ENGLISH)
        );
    }

    @Test
    public void getLanguagePreferenceShouldReturnWelsh() {
        assertThat(
            LanguagePreference.getLanguagePreference(
                CaseData.builder().languagePreferenceWelsh(YesOrNo.YES).build()),
            is(LanguagePreference.WELSH)
        );
    }

    @Test
    public void getLanguagePreferenceShouldReturnEnglish() {
        assertThat(
            LanguagePreference.getLanguagePreference(
                CaseData.builder().languagePreferenceWelsh(YesOrNo.NO).build()),
            is(LanguagePreference.ENGLISH)
        );
    }

    @Test
    public void valuesShouldReturnListOfTwoElements() {
        assertThat(LanguagePreference.values().length, is(2));
    }
}
