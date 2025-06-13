package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor.validators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;

import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
class ViewDraftResponseCheckerTest {

    @InjectMocks
    ViewDraftResponseChecker viewDraftResponseChecker;

    PartyDetails respondent;

    @BeforeEach
    void setUp() {
        respondent = PartyDetails.builder().build();
    }

    @Test
    void isStartedTest() {

        boolean bool = viewDraftResponseChecker.isStarted(respondent, true);

        assertFalse(bool);
    }

    @Test
    void mandatoryCompletedTest() {
        boolean bool = viewDraftResponseChecker.isFinished(respondent, true);

        assertFalse(bool);
    }
}
