package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.CaOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DaApplicantOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DaRespondentOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DocumentAcknowledge;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.OtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.SupportingEvidenceBundle;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherApplicationsBundle {
    @CCD(label = "Applicant", searchable = false)
    @JsonProperty("applicantName")
    private final String applicantName;
    @CCD(
            label = "Application type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CaOtherApplicationType"
    )
    @JsonProperty("caApplicantApplicationType")
    private final CaOtherApplicationType caApplicantApplicationType;
    @CCD(
            label = "Application type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CaOtherApplicationType"
    )
    @JsonProperty("caRespondentApplicationType")
    private final CaOtherApplicationType caRespondentApplicationType;
    @CCD(
            label = "Application type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DaApplicantOtherApplicationType"
    )
    @JsonProperty("daApplicantApplicationType")
    private final DaApplicantOtherApplicationType daApplicantApplicationType;
    @CCD(
            label = "Application type",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DaRespondentOtherApplicationType"
    )
    @JsonProperty("daRespondentApplicationType")
    private final DaRespondentOtherApplicationType daRespondentApplicationType;
    @CCD(
            label = "Reason for application",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "OtherApplicationType"
    )
    @JsonProperty("applicationType")
    private final OtherApplicationType applicationType;
    @CCD(label = " ", regex = ".doc,.docx,.pdf", searchable = false)
    @JsonProperty("document")
    private final Document document;
    @CCD(label = "Application", regex = ".doc,.docx,.pdf", searchable = false)
    @JsonProperty("finalDocument")
    private final List<Element<Document>> finalDocument;
    @CCD(label = "Tick to confirm this document is related to this case", searchable = false)
    @JsonProperty("documentAcknowledge")
    private final List<DocumentAcknowledge> documentAcknowledge;
    @CCD(label = "Please state how soon you want the judge to consider your application?", searchable = false)
    @JsonProperty("urgencyTimeFrameType")
    private final UrgencyTimeFrameType urgencyTimeFrameType;
    @CCD(label = "Supplements", searchable = false)
    @JsonProperty("supplementsBundle")
    private final List<Element<Supplement>> supplementsBundle;
    @CCD(label = "Supporting documents", searchable = false)
    @JsonProperty("supportingEvidenceBundle")
    private final List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;
    @CCD(label = "Date submitted", searchable = false)
    @JsonProperty("uploadedDateTime")
    private final String uploadedDateTime;
    @CCD(label = "Uploaded by", searchable = false)
    @JsonProperty("author")
    private final String author;
    @CCD(label = "How soon does the judge need to consider the application?", searchable = false)
    @JsonProperty("urgency")
    private final Urgency urgency;
    @CCD(
            label = "Document is related to Familyman Case number/Names/CCD",
            showCondition = "documentAcknowledge=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("documentRelatedToCase")
    private final YesOrNo documentRelatedToCase;
    @CCD(label = "Application status", searchable = false)
    private final String applicationStatus;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = "Supporting documents", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> supportingEvidenceLA;
  @CCD(label = "Supporting documents", searchable = false)
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SupportingEvidenceBundle>> supportingEvidenceNC;
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabel;
  // ==== end synthesised definition-only fields ====
}
