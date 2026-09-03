package uk.gov.hmcts.reform.prl.models.common.staff;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class StaffUser {

    private String idamId;

    public String getIdamId() {
        return idamId;
    }

    public String getPersonalCode() {
        return personalCode;
    }

    private String personalCode;

    @JsonCreator
    public StaffUser(@JsonProperty("idamId") String idamId,
                        @JsonProperty("personalCode") String personalCode) {
        this.idamId = idamId;
        this.personalCode = personalCode;
    }
}
