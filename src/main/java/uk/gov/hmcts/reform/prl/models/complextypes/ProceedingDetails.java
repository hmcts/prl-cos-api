package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder
public class ProceedingDetails {

    @CCD(label = "*Are these previous or ongoing proceedings?", searchable = false)
    private final ProceedingsEnum previousOrOngoingProceedings;
    @CCD(label = "Case number", searchable = false)
    private final String caseNumber;
    @CCD(label = "Date started", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateStarted;
    @CCD(label = "Date ended", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateEnded;
    @CCD(label = "*Which orders were made?", searchable = false)
    private final List<TypeOfOrderEnum> typeOfOrder;
    @CCD(label = "*Type of orders", searchable = false)
    private final String otherTypeOfOrder;
    @CCD(label = "Name of judge ", searchable = false)
    private final String nameOfJudge;
    @CCD(label = "Name of the court where proceedings heard", searchable = false)
    private final String nameOfCourt;
    @CCD(label = "Names of children involved", searchable = false, typeOverride = FieldType.TextArea)
    private final String nameOfChildrenInvolved;
    @CCD(label = "Name of Guardian", searchable = false)
    private final String nameOfGuardian;
    @CCD(label = "Name and office (if known) of Cafcass/CAFCASS CYMRU officer", searchable = false)
    private final String nameAndOffice;
    @CCD(
            label = "Other proceedings order document",
            categoryID = "previousOrdersSubmittedWithApplication",
            searchable = false
    )
    private final Document uploadRelevantOrder;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "## Add new proceeding", searchable = false, typeOverride = FieldType.Label)
  private String addNewProceedingLabel;
  // ==== end synthesised definition-only fields ====
}
