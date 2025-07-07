package uk.gov.hmcts.reform.prl.clients.hearing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HearingApiHackClientTest {

    @Test
    void getHearingDetails() {
        HearingApiHackClient client = new HearingApiHackClient();
        client.getHearingDetails("auth", "serviceAuth", "1234");
    }
}
