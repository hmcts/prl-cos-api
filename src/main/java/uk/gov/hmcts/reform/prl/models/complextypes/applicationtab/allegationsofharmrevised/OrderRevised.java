package uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "RevisedOrder", generate = true)
@Data
@Builder
public class OrderRevised {
    @CCD(label = "Date issued", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate dateIssued;
    @CCD(label = "Date ended", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    @CCD(label = "Is the order current?", searchable = false, typeOverride = FieldType.YesOrNo)
    private YesOrNo orderCurrent;
    @CCD(label = "Name of Court", searchable = false)
    private String courtName;
    @CCD(label = "Case number", searchable = false)
    private String caseNumber;

}


