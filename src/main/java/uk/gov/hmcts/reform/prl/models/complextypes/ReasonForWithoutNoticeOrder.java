package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ReasonForOrderWithoutGivingNoticeEnum;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "reasonForWithoutNoticeOrder", generate = true)
@Data
@Builder(toBuilder = true)
public class ReasonForWithoutNoticeOrder {
    @CCD(
            label = "*Why do you want to apply without giving notice to respondent? You can select more than one reason.",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "ReasonForWithoutGivingNoticeEnum"
    )
    @JsonProperty("reasonForOrderWithoutGivingNotice")
    private final List<ReasonForOrderWithoutGivingNoticeEnum> reasonForOrderWithoutGivingNotice;
    @CCD(
            label = "Provide further details of why a without notice hearing is required.",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("futherDetails")
    private final String futherDetails;
}
