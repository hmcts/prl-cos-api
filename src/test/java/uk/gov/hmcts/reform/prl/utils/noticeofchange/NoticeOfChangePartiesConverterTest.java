package uk.gov.hmcts.reform.prl.utils.noticeofchange;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class NoticeOfChangePartiesConverterTest {

    @InjectMocks
    NoticeOfChangePartiesConverter noticeOfChangePartiesConverter;

    PartyDetails partyDetails;

    @BeforeEach
    void setUp() {

        partyDetails =
            PartyDetails.builder().firstName("Test").lastName("Test").build();

    }

    @Test
    void generateForSubmissionTest() {

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(partyDetails).build();

        NoticeOfChangeParties submission = noticeOfChangePartiesConverter.generateCaForSubmission(wrappedApplicant);

        assertEquals("Test", submission.getFirstName());
    }

    @Test
    void generateDaForSubmissionTest() {
        NoticeOfChangeParties submission = noticeOfChangePartiesConverter.generateDaForSubmission(partyDetails);

        assertEquals("Test", submission.getFirstName());
    }
}
