package uk.gov.hmcts.reform.prl.models.dto.courtnav;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavRespondent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RespondentDeserializationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeRespondentDetails() throws Exception {
        String json = """
            {
            "fl401": {
                "respondentDetails": {
                    "respondentFirstName": "Dolores",
                    "respondentLastName": "Smith",
                    "respondentOtherNames": "",
                    "respondentDateOfBirth": {
                        "day": 10,
                        "month": 12,
                        "year": 1986
                    },
                    "respondentLivesWithApplicant": true,
                    "respondentAddress": {
                        "addressLine1": "Heaven",
                        "addressLine2": "7 Castle Square",
                        "addressLine3": "cum voluptatum velit",
                        "postTown": "Swansea",
                        "postCode": "SA1 1DW"
                    },
                    "respondentPhoneNumber": "+44 68995 95438",
                    "respondentEmailAddress": "test-resp@hmcts.net"
                    }
                }
            }
            """;

        CourtNavFl401 data = objectMapper.readValue(json, CourtNavFl401.class);
        CourtNavRespondent respondent = data.getFl401().getCourtNavRespondent();

        assertNotNull(respondent);
        assertEquals("Dolores", respondent.getFirstName());
        assertEquals("Smith", respondent.getLastName());
        assertTrue(respondent.isRespondentLivesWithApplicant());

        assertNotNull(respondent.getAddress());
        assertEquals("Swansea", respondent.getAddress().getPostTown());
        assertEquals("SA1 1DW", respondent.getAddress().getPostCode());

        assertEquals("+44 68995 95438", respondent.getPhoneNumber());
        assertEquals("test-resp@hmcts.net", respondent.getEmail());
    }
}
