package uk.gov.hmcts.reform.prl.models.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.Address;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessCode {
    @JsonProperty("recipientName")
    private String recipientName;
    @JsonProperty("address")
    private Address address;
    @JsonProperty("code")
    private String code;
    @JsonProperty("respondByDate")
    private String respondByDate;
    @JsonProperty("currentDate")
    private String currentDate;
    @JsonProperty("isLinked")
    private String isLinked;
}
