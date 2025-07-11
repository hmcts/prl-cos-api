package uk.gov.hmcts.reform.prl.models.dto.courtnav;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.BeforeStart;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class BeforeStartDeserializationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeBeforeStart() throws Exception {
        String json = """
            {
                "fl401": {
                    "beforeStart": {
                        "applicantHowOld": "eighteenOrOlder"
                    }
                }
            }
            """;

        CourtNavFl401 data = objectMapper.readValue(json, CourtNavFl401.class);
        BeforeStart beforeStart = data.getFl401().getBeforeStart();

        assertNotNull(beforeStart);
        assertEquals(ApplicantAge.eighteenOrOlder, beforeStart.getApplicantAge());
    }
}
