package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder
public class MiamAttendingPersonName {

    @CCD(label = "Name of the person to attend MIAM", searchable = false)
    @JsonProperty("miamAttendingPersonName")
    private final String miamAttendingPersonName;

    @JsonCreator
    public MiamAttendingPersonName(String miamAttendingPersonName) {
        this.miamAttendingPersonName  = miamAttendingPersonName;
    }

}
