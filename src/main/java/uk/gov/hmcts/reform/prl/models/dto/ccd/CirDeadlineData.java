package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CirDeadlineData {

    /**
     * Set by enterCirDueDate event when court admin records the CIR due date.
     */
    @JsonProperty("whenReportsMustBeFiledByLocalAuthority")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate whenReportsMustBeFiledByLocalAuthority;

    /**
     * Set to Yes by the system when a CIR document is uploaded on or before the due date.
     * Read by the CirDeadlineCheckTask scheduler to decide whether to create a WA task.
     */
    @JsonProperty("cirReceivedByDeadline")
    private final YesOrNo cirReceivedByDeadline;

    /**
     * Date the CIR document was uploaded.
     */
    @JsonProperty("cirUploadedDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate cirUploadedDate;
}
