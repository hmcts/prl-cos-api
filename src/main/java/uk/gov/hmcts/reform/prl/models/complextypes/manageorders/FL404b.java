package uk.gov.hmcts.reform.prl.models.complextypes.manageorders;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Address;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Builder(toBuilder = true)
@Data
public class FL404b {

    @CCD(label = "Court name", showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"", searchable = false)
    private final String fl404bCourtName;
    @CCD(
            label = "Court address",
            showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.AddressUK
    )
    private final Address fl404bCourtAddress;
    @CCD(label = "Case number", showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"", searchable = false)
    private final String fl404bCaseNumber;
    @CCD(label = "Applicant name", showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"", searchable = false)
    private final String fl404bApplicantName;
    @CCD(label = "Applicant reference", showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"", searchable = false)
    private final String fl404bApplicantReference;
    @CCD(label = "Respondent name", showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"", searchable = false)
    private final String fl404bRespondentName;
    @CCD(label = "Respondent reference", showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"", searchable = false)
    private final String fl404bRespondentReference;
    @CCD(label = "Respondent date of birth", showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate fl404bRespondentDob;
    @CCD(
            label = "Respondent address",
            showCondition = "fl404bHearingOutcome=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.AddressUK
    )
    private final Address fl404bRespondentAddress;
    @CCD(label = "Hearing outcome", searchable = false, typeOverride = FieldType.TextArea)
    private final String fl404bHearingOutcome;


}
