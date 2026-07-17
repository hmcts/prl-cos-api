package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class OtherProceedingDetails {
    @CCD(label = "order details", searchable = false)
    private final String orderDetail;
    @CCD(label = "Case number", searchable = false)
    @JsonProperty("caseNo")
    private final String caseNumber;
    @CCD(label = "Order date", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate orderDate;
    @CCD(label = "Current order", searchable = false, typeOverride = FieldType.YesOrNo)
    @JsonProperty("currentOrder")
    private final YesOrNo isCurrentOrder;
    @CCD(label = "Order end date", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate orderEndDate;
    @CCD(label = "Copy of order", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo orderCopy;
    @CCD(label = " ", searchable = false)
    private final Document orderDocument;

}
