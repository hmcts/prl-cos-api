package uk.gov.hmcts.reform.prl.models.dto.courtnav;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.Situation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum.nonMolestationOrder;
import static uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum.occupationOrder;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum.deterredFromPursuingApplication;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum.respondentDeliberatelyEvadingService;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.WithoutNoticeReasonEnum.riskOfSignificantHarm;

class SituationDeserializationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeSituation() throws Exception {
        String json = """
            {
                "fl401": {
                    "situation": {
                        "ordersAppliedFor": ["nonMolestationOrder", "occupationOrder"],
                        "ordersAppliedWithoutNotice": true,
                        "ordersAppliedWithoutNoticeReason": [
                            "riskOfSignificantHarm",
                            "deterredFromPursuingApplication",
                            "respondentDeliberatelyEvadingService"
                        ],
                        "ordersAppliedWithoutNoticeReasonDetails": "Reason details",
                        "bailConditionsOnRespondent": false,
                        "additionalDetailsForCourt": "Additional details"
                    }
                }
            }
            """;

        CourtNavFl401 data = objectMapper.readValue(json, CourtNavFl401.class);
        Situation situation = data.getFl401().getSituation();

        assertNotNull(situation);
        assertEquals(List.of(nonMolestationOrder, occupationOrder), situation.getOrdersAppliedFor());
        assertTrue(situation.isOrdersAppliedWithoutNotice());
        assertEquals(List.of(
            riskOfSignificantHarm,
            deterredFromPursuingApplication,
            respondentDeliberatelyEvadingService),
                     situation.getOrdersAppliedWithoutNoticeReason());
        assertEquals("Reason details", situation.getOrdersAppliedWithoutNoticeReasonDetails());
        assertFalse(situation.isBailConditionsOnRespondent());
        assertEquals("Additional details", situation.getAdditionalDetailsForCourt());
    }
}
