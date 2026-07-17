package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
@AllArgsConstructor
public class BulkPrintOrderDetail {

    @CCD(label = "Bulk print ID", searchable = false)
    private final String bulkPrintId;
    @CCD(label = "Served party ID", showCondition = "partyId=\"DO_NOT_SHOW\"", searchable = false)
    private final String partyId;
    @CCD(label = "Served party name", searchable = false)
    private final String partyName;
    @CCD(label = "Served date time", searchable = false)
    private final String servedDateTime;

}
