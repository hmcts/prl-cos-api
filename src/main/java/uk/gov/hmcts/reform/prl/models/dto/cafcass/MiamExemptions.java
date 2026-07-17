package uk.gov.hmcts.reform.prl.models.dto.cafcass;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class MiamExemptions {

    @CCD(label = "Reasons for the MIAM exemption", searchable = false)
    private String reasonsForMiamExemption;
    @CCD(
            label = "What evidence of domestic violence or abuse does the applicant have?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String domesticViolenceEvidence;
    @CCD(
            label = "What reasons does the applicant have for the application to be made urgently?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String urgencyEvidence;
    @CCD(label = "Child protection concerns", searchable = false, typeOverride = FieldType.TextArea)
    private String childProtectionEvidence;
    @CCD(
            label = "*MIAM evidence - previous MIAM attendance or MIAM exemption",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String previousAttendenceEvidence;
    @CCD(
            label = "MIAM evidence - what other grounds of exemption apply?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private String otherGroundsEvidence;

}
