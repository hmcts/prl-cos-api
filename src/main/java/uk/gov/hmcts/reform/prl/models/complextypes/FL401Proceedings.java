package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "Fl401OtherProceedingsData", generate = true)
@Data
@Builder(toBuilder = true)
public class FL401Proceedings {
    @CCD(label = "Name of the court", searchable = false)
    private final String nameOfCourt;
    @CCD(label = "Case number", searchable = false)
    private final String caseNumber;
    @CCD(label = "*Type of case", searchable = false)
    private final String typeOfCase;
    @CCD(label = "*Any other details", searchable = false, typeOverride = FieldType.TextArea)
    private final String anyOtherDetails;
    @CCD(label = "Upload relevant order(s)", categoryID = "previousOrdersSubmittedWithApplication", searchable = false)
    private final Document uploadRelevantOrder;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "## Add new proceeding", searchable = false, typeOverride = FieldType.Label)
  private String addNewProceedingLabel;
  // ==== end synthesised definition-only fields ====
}
