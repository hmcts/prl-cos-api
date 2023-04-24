package uk.gov.hmcts.reform.prl.utils.noticeofchange;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.Silent.class)
@Slf4j
public class NoticeOfChangePartiesConverterTest {

    @InjectMocks
    NoticeOfChangePartiesConverter noticeOfChangePartiesConverter;

    PartyDetails partyDetails;

    @Before
    public void setUp() {

        partyDetails =
            PartyDetails.builder().firstName("Test").lastName("Test").build();

    }

    @Test
    public void generateForSubmissionTest() {

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(partyDetails).build();

        NoticeOfChangeParties submission = noticeOfChangePartiesConverter.generateCaForSubmission(wrappedApplicant);

        assertEquals("Test", submission.getFirstName());
    }

    @Test
    public void generateDaForSubmissionTest() {
        log.info("My changes");
        NoticeOfChangeParties submission = noticeOfChangePartiesConverter.generateDaForSubmission(partyDetails);

        assertEquals("Test", submission.getFirstName());
    }
}
