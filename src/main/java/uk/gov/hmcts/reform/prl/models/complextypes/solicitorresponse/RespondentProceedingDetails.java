package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.RespondentProceedingsEnum;
import uk.gov.hmcts.reform.prl.enums.respondentsolicitor.RespondentTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
public class RespondentProceedingDetails {

    @CCD(label = "*Are these previous or ongoing proceedings?", searchable = false)
    private final RespondentProceedingsEnum previousOrOngoingProceedings;
    @CCD(label = "Case number", searchable = false)
    private final String caseNumber;
    @CCD(label = "Date started", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateStarted;
    @CCD(label = "Date ended", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateEnded;
    @CCD(label = "Which orders were made?", searchable = false)
    private final List<RespondentTypeOfOrderEnum> typeOfOrder;
    @CCD(label = "Type of orders", searchable = false)
    private final String otherTypeOfOrder;
    @CCD(label = "Name of judge ", searchable = false)
    private final String nameOfJudge;
    @CCD(label = "Name of the court where proceedings heard", searchable = false)
    private final String nameOfCourt;
    @CCD(label = "Names of children involved", searchable = false, typeOverride = FieldType.TextArea)
    private final String nameOfChildrenInvolved;
    @CCD(label = "Name of guardian", searchable = false)
    private final String nameOfGuardian;
    @CCD(label = "Name and office (if known) of Cafcass/Cafcass Cymru officer", searchable = false)
    private final String nameAndOffice;
    @CCD(label = "Upload relevant order(s)", categoryID = "ordersFromOtherProceedings", searchable = false)
    private final Document uploadRelevantOrder;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "You can save and return to this page at any time. Questions marked with a * need to be completed before you can send your application.",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String fieldHint;
  // ==== end synthesised definition-only fields ====
}
