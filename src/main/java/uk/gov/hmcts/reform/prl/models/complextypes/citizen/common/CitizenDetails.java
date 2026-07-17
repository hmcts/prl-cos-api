package uk.gov.hmcts.reform.prl.models.complextypes.citizen.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoIDontKnowV2;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class CitizenDetails {

    @CCD(label = " ", searchable = false)
    private final String firstName;
    @CCD(label = " ", searchable = false)
    private final String lastName;
    @CCD(label = " ", searchable = false)
    private final String previousName;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOfBirth;
    @CCD(label = " ", searchable = false)
    private final String placeOfBirth;
    @CCD(label = "*Do they live in a refuge?", searchable = false)
    private final YesNoIDontKnowV2 liveInRefuge;
    @CCD(
            label = "*Upload a C8 form with the refuge address",
            hint = "You can download the form from www.gov.uk. The address, email address and contact number entered for this party will be kept confidential.",
            regex = ".pdf,.docx",
            categoryID = "confidential",
            searchable = false
    )
    private Document refugeConfidentialityC8Form;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.AddressUK)
    private final Address address;
    @CCD(label = " ", searchable = false)
    private AddressHistory addressHistory;
    @CCD(label = " ", searchable = false)
    private Contact contact;
}
