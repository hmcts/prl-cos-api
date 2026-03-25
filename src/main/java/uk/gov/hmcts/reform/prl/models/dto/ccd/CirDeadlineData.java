package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@Jacksonized
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CirDeadlineData {

    @JsonProperty("whenReportsMustBeFiledByLocalAuthority")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate whenReportsMustBeFiledByLocalAuthority;

    @JsonProperty("cirReceivedByDeadline")
    private final YesOrNo cirReceivedByDeadline;

    /**
     * Date the CIR document was uploaded.
     */
    @JsonProperty("cirUploadedDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate cirUploadedDate;
}
