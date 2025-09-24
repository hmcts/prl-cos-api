package uk.gov.hmcts.reform.prl.models.dto.ccd.request.acro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class Range {
    @JsonProperty("last_modified")
    private LastModified lastModified;

    @JsonProperty("data.orderCollection.value.dateCreated")
    private LastModified dateCreated;

    @JsonProperty("data.stmtOfServiceForOrder.value.submittedDateTime")
    private LastModified submittedDateTime;
}
