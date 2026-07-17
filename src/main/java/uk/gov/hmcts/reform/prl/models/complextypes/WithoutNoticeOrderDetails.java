package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class WithoutNoticeOrderDetails {
    @CCD(
            label = "Do you want to apply for the order without giving notice to the respondent",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("orderWithoutGivingNotice")
    private final YesOrNo orderWithoutGivingNotice;

    @JsonCreator
    public WithoutNoticeOrderDetails(YesOrNo orderWithoutGivingNotice) {
        this.orderWithoutGivingNotice = orderWithoutGivingNotice;
    }
}
