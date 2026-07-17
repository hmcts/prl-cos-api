package uk.gov.hmcts.reform.prl.models.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SosUploadedByEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class StmtOfServiceAddRecipient {

    @CCD(label = "Who was served?", searchable = false, typeOverride = FieldType.DynamicList)
    private DynamicList respondentDynamicList;
    //This is only used to capture date & time from date picker in XUI
    @CCD(label = "When were they served?", hint = "For example: 16 4 2021, 10:09", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final LocalDateTime servedDateTimeOption;
    @CCD(label = "Upload document(PDF, .doc) ", categoryID = "anyOtherDoc", searchable = false)
    private final Document stmtOfServiceDocument;
    @CCD(label = "Selected Party Id", showCondition = "servedDateTimeOption = \"DO_NOT_SHOW\"", searchable = false)
    private final String selectedPartyId;
    @CCD(label = "Who was served?", searchable = false)
    private final String selectedPartyName;
    @CCD(
            label = "Served by",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SosUploadedByEnum"
    )
    private SosUploadedByEnum uploadedBy;
    @CCD(label = "Submitted date time", showCondition = "selectedPartyName = \"DO_NOT_SHOW\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final LocalDateTime submittedDateTime;
    //Date - citizen sos, date & time - court staff/solicitor sos
    @CCD(label = "When were they served?", searchable = false)
    private final String partiesServedDateTime;
    //NOT IN USE, DO NOT USE THESE
    @CCD(label = "Who was served?", searchable = false)
    private final String citizenPartiesServedList;
    @CCD(label = "When were they served?", searchable = false)
    private final String citizenPartiesServedDate;
    @CCD(label = "Upload document(PDF, .doc) ", searchable = false)
    private final List<Document> citizenSosDocs;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "**Statement of service document**", searchable = false, typeOverride = FieldType.Label)
  private String stmtOfServiceDocumentLabel;
  // ==== end synthesised definition-only fields ====
}
