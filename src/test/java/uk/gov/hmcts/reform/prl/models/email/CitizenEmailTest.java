package uk.gov.hmcts.reform.prl.models.email;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.dto.notify.CitizenEmail;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class CitizenEmailTest {

    @Test
    void customBuilderShouldWork() {
        assertThat(CitizenEmail.builder().petitionerName("a").build().getPetitionerName(), is("a"));
        assertThat(CitizenEmail.builder().respondentName("b").build().getRespondentName(), is("b"));
        assertThat(CitizenEmail.builder().caseReference("c").build().getCaseReference(), is("c"));
    }
}
