package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MiamAttendingPersonName {

    @JsonProperty("miamAttendingPersonName")
    private final String miamAttendingPersonName;

    @JsonCreator
    public MiamAttendingPersonName(String miamAttendingPersonName) {
        this.miamAttendingPersonName  = miamAttendingPersonName;
    }

}
