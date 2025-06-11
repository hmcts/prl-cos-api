package uk.gov.hmcts.reform.prl.models.dto.courtnav;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavApplicant;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.CourtNavFl401;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static uk.gov.hmcts.reform.prl.enums.Gender.other;
import static uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.PreferredContactEnum.phone;

class ApplicantDeserializationTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeApplicantDetails() throws Exception {
        String json = """
            {
                "fl401": {
                     "applicantDetails": {
                        "applicantFirstName": "Firstname",
                        "applicantLastName": "Lastname",
                        "applicantGender": "other",
                        "applicantGenderOther": "Prefer not to say",
                        "applicantDateOfBirth": {
                            "day": 13,
                            "month": 10,
                            "year": 1985
                        },
                        "shareContactDetailsWithRespondent": true,
                        "applicantAddress": {
                            "addressLine1": "Address line 1",
                            "addressLine2": "Address line 2",
                            "addressLine3": "Address line 3",
                            "postTown": "Town",
                            "postCode": "SA1 1DW"
                        },
                        "applicantEmailAddress": "applicant@email.com",
                        "applicantPhoneNumber": "123456789",
                        "applicantPreferredContact": [
                            "phone"
                        ],
                        "applicantContactInstructions": "Contact instructions",
                        "applicantHasLegalRepresentative": true,
                        "legalRepresentativeFirm": "Firm",
                        "legalRepresentativeDx": "Dx",
                        "legalRepresentativeReference": "Reference",
                        "legalRepresentativeFirstName": "Firstname",
                        "legalRepresentativeLastName": "Lastname",
                        "legalRepresentativeEmail": "email@mail.com",
                        "legalRepresentativePhone": "0123456789",
                        "legalRepresentativeAddress": {
                            "addressLine1": "Address line 1",
                            "addressLine2": "Address line 2",
                            "addressLine3": "Address line 3",
                            "postTown": "Town",
                            "postCode": "SA1 1DW",
                            "county": "County",
                            "country": "Country"
                        }
                    }
                }
            }
            """;

        CourtNavFl401 data = objectMapper.readValue(json, CourtNavFl401.class);
        CourtNavApplicant applicant = data.getFl401().getCourtNavApplicant();

        assertNotNull(applicant);
        assertEquals("Firstname", applicant.getFirstName());
        assertEquals("Lastname", applicant.getLastName());
        assertEquals(other, applicant.getGender());
        assertEquals("Prefer not to say", applicant.getOtherGender());
        assertEquals(new CourtNavDate(13, 10, 1985), applicant.getDateOfBirth());

        assertTrue(applicant.isShareContactDetailsWithRespondent());
        assertEquals("Address line 1", applicant.getAddress().getAddressLine1());
        assertEquals("Address line 2", applicant.getAddress().getAddressLine2());
        assertEquals("Address line 3", applicant.getAddress().getAddressLine3());
        assertEquals("Town", applicant.getAddress().getPostTown());
        assertEquals("SA1 1DW", applicant.getAddress().getPostCode());

        assertEquals("applicant@email.com", applicant.getEmail());
        assertEquals("123456789", applicant.getPhoneNumber());
        assertEquals(List.of(phone), applicant.getApplicantPreferredContact());
        assertEquals("Contact instructions", applicant.getApplicantContactInstructions());
        assertTrue(applicant.getHasLegalRepresentative());

        assertEquals("Firm", applicant.getSolicitorFirmName());
        assertEquals("Dx", applicant.getDxNumber());
        assertEquals("Reference", applicant.getSolicitorReference());
        assertEquals("Firstname", applicant.getRepresentativeFirstName());
        assertEquals("Lastname", applicant.getRepresentativeLastName());
        assertEquals("email@mail.com", applicant.getSolicitorEmail());
        assertEquals("0123456789", applicant.getSolicitorTelephone());

        assertEquals("Address line 1", applicant.getSolicitorAddress().getAddressLine1());
        assertEquals("Address line 2", applicant.getSolicitorAddress().getAddressLine2());
        assertEquals("Address line 3", applicant.getSolicitorAddress().getAddressLine3());
        assertEquals("Town", applicant.getSolicitorAddress().getPostTown());
        assertEquals("County", applicant.getSolicitorAddress().getCounty());
        assertEquals("SA1 1DW", applicant.getSolicitorAddress().getPostCode());
        assertEquals("Country", applicant.getSolicitorAddress().getCountry());
    }
}
