package uk.gov.hmcts.reform.prl.clients.hearing;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.dto.hearings.Hearings;

import static org.junit.jupiter.api.Assertions.*;

class HearingApiHackClientTest {

    @Test
    void getHearingDetails() {
        HearingApiHackClient hearingApiHackClient = new HearingApiHackClient();
        Hearings hearingDetails = hearingApiHackClient.getHearingDetails("auth", "service", "case");

    }
}
