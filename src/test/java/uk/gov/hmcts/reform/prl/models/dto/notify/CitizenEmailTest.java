package uk.gov.hmcts.reform.prl.models.dto.notify;

import org.junit.Test;
import uk.gov.hmcts.reform.prl.utils.CitizenEmailProvider;
import uk.gov.hmcts.reform.prl.utils.EmailTemplateVarsProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_CASE_ID;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_PETITIONER_NAME;
import static uk.gov.hmcts.reform.prl.utils.TestConstants.TEST_RESPONDENT_NAME;

public class CitizenEmailTest {

    public static final String OTHER_CASE_ID = "123";

    @Test
    public void builderShouldReturnValidObject() {
        assertThat(CitizenEmailProvider.empty(), is(instanceOf(EmailTemplateVars.class)));
    }

    @Test
    public void objectShouldBeEqual() {
        assertThat(CitizenEmailProvider.empty(), is(equalTo(CitizenEmailProvider.empty())));

        assertThat(
            CitizenEmailProvider.of(TEST_CASE_ID),
            is(CitizenEmailProvider.of(TEST_CASE_ID))
        );

        assertThat(
            CitizenEmailProvider.of(TEST_CASE_ID, TEST_PETITIONER_NAME, TEST_RESPONDENT_NAME),
            is(CitizenEmailProvider.of(TEST_CASE_ID, TEST_PETITIONER_NAME, TEST_RESPONDENT_NAME))
        );
    }

    @Test
    public void objectShouldBeNotEqual() {
        // different class, different id
        assertThat(
            CitizenEmailProvider.of(TEST_CASE_ID),
            is(not(EmailTemplateVarsProvider.of(OTHER_CASE_ID)))
        );

        // different names
        assertThat(
            CitizenEmailProvider.of(TEST_CASE_ID, TEST_PETITIONER_NAME, TEST_RESPONDENT_NAME),
            is(not(CitizenEmailProvider.of(TEST_CASE_ID, TEST_RESPONDENT_NAME, TEST_PETITIONER_NAME)))
        );
    }
}
