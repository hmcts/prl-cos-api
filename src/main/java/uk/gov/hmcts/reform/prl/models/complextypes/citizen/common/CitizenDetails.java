package uk.gov.hmcts.reform.prl.models.complextypes.citizen.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CitizenDetails {

    private final String firstName;
    private final String lastName;
    private final String previousName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    private final String placeOfBirth;
    private YesOrNo liveInRefuge;
    private Document refugeConfidentialityC8Form;
    private final Address address;
    private AddressHistory addressHistory;
    private Contact contact;
}
