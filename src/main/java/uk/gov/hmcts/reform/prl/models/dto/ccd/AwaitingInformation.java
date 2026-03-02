package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.awaitinginformation.RequestFurtherInformationReasonEnum;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AwaitingInformation {
    @JsonProperty("reviewByDate")
    private LocalDate reviewDate;
    @JsonProperty("requestFurtherInformationReasonList")
    private List<RequestFurtherInformationReasonEnum> requestFurtherInformationReasonEnum;
}
