package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class OtherDetailsOfWithoutNoticeOrder {
    @CCD(
            label = "Is there anything else about the applicant's situation that you would like the court to be aware of, or know about or consider?",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    @JsonProperty("otherDetails")
    private final String otherDetails;

    @JsonCreator
    public OtherDetailsOfWithoutNoticeOrder(String otherDetails) {
        this.otherDetails = otherDetails;
    }
}
