package uk.gov.hmcts.reform.prl.utils.noticeofchange;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;

public class NoticeOfChangePartiesConverterTest {

    @Mock
    NoticeOfChangePartiesConverter noticeOfChangePartiesConverter;

    PartyDetails partyDetails;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        partyDetails =
            PartyDetails.builder().firstName("Test").lastName("Test").build();

    }

    @Test
    public void generateForSubmissionTest() {

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(partyDetails).build();

        NoticeOfChangeParties submission = noticeOfChangePartiesConverter.generateForSubmission(wrappedApplicant);

        Assert.assertNull(submission);
    }
}
