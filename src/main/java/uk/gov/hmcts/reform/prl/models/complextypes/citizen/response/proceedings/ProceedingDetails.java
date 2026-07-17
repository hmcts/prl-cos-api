package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.proceedings;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

import java.time.LocalDate;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class ProceedingDetails {
    @CCD(label = "Case number", searchable = false)
    private final String caseNumber;
    @CCD(ignore = true)
    private final LocalDate orderMadeOn;
    @CCD(ignore = true)
    private final String howLongWasTheOrder;
    @CCD(ignore = true)
    private final YesOrNo isCurrentOrder;
    @CCD(ignore = true)
    private final String nameOfCourtIssuedOrder;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "*Are these previous or ongoing proceedings?", searchable = false)
  private ProceedingsEnum previousOrOngoingProceedings;
  @CCD(label = "Date started", searchable = false)
  private java.time.LocalDate dateStarted;
  @CCD(label = "Date ended", searchable = false)
  private java.time.LocalDate dateEnded;
  @CCD(label = "*Which orders were made?", searchable = false)
  private java.util.Set<TypeOfOrderEnum> typeOfOrder;
  @CCD(label = "*Type of orders", searchable = false)
  private String otherTypeOfOrder;
  @CCD(label = "Name of judge ", searchable = false)
  private String nameOfJudge;
  @CCD(label = "Name of the court where proceedings heard", searchable = false)
  private String nameOfCourt;
  @CCD(label = "Names of children involved", searchable = false, typeOverride = FieldType.TextArea)
  private String nameOfChildrenInvolved;
  @CCD(label = "Name of Guardian", searchable = false)
  private String nameOfGuardian;
  @CCD(label = "Name and office (if known) of Cafcass/CAFCASS CYMRU officer", searchable = false)
  private String nameAndOffice;
  @CCD(
          label = "Other proceedings order document",
          categoryID = "previousOrdersSubmittedWithApplication",
          searchable = false
  )
  private uk.gov.hmcts.ccd.sdk.type.Document uploadRelevantOrder;
  @CCD(label = "## Add new proceeding", searchable = false, typeOverride = FieldType.Label)
  private String addNewProceedingLabel;
  // ==== end synthesised definition-only fields ====
}
