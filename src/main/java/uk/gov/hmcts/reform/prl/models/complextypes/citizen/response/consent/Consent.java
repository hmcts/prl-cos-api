package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class Consent {
    @CCD(label = "*Do you consent to the application? ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo consentToTheApplication;
    @CCD(
            label = "*Give your reasons for not consenting to the application.",
            showCondition = "consentToTheApplication=\"No\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String noConsentReason;
    @CCD(label = "*When did you receive the application? ", hint = "For example, 27 3 2007", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate applicationReceivedDate;
    @CCD(
            label = "*Does the respondent need permission from the court before making applications? ",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo permissionFromCourt;
    @CCD(
            label = "*Provide details of the court order in place.",
            showCondition = "permissionFromCourt=\"Yes\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String courtOrderDetails;
}
