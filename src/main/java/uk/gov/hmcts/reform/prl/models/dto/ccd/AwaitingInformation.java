package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.awaitinginformation.AwaitingInformationReasonEnum;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AwaitingInformation {
    @JsonProperty("reviewByDate")
    private LocalDate reviewDate;
    @JsonProperty("awaitingInformation")
    private AwaitingInformationReasonEnum awaitingInformationReasonEnum;
}
