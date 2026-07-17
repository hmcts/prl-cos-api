package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2AdditionalOrdersRequestedCa;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.C2ApplicationTypeEnum;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.CombinedC2AdditionalOrdersRequested;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DocumentAcknowledge;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.ParentalResponsibilityType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class C2DocumentBundle {

    @CCD(label = "Applicant", searchable = false)
    private final String applicantName;
    @CCD(label = " ", regex = ".doc,.docx,.pdf", searchable = false)
    private final Document document;
    @CCD(label = "Application", regex = ".doc,.docx,.pdf", searchable = false)
    private final List<Element<Document>> finalDocument;
    @CCD(label = "Tick to confirm this document is related to this case", searchable = false)
    private final List<DocumentAcknowledge> documentAcknowledge;
    @CCD(label = "Reason for application", hint = "Select all that apply", searchable = false)
    private final List<C2AdditionalOrdersRequestedCa> caReasonsForC2Application;
    @CCD(
            label = "Reason for application",
            hint = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "C2AdditionalOrdersRequestedDa"
    )
    private final List<C2AdditionalOrdersRequestedCa> daReasonsForC2Application;
    @CCD(label = "Reason for application", hint = "Select all that apply", searchable = false)
    private final List<CombinedC2AdditionalOrdersRequested> combinedReasonsForC2Application;
    @CCD(
            label = "Other reasons for C2 Application",
            showCondition = "applicantName=\"DO_NOT_SHOW\"",
            searchable = false
    )
    private final String otherReasonsFoC2Application;
    @CCD(
            label = "Who's seeking parental responsibility?",
            showCondition = "applicantName=\"DO_NOT_SHOW\"",
            searchable = false
    )
    private final ParentalResponsibilityType parentalResponsibilityType;
    @CCD(label = "Hearing List", searchable = false, typeOverride = FieldType.DynamicList)
    private final DynamicList hearingList;
    @CCD(label = "Please state how soon you want the judge to consider your application?", searchable = false)
    private final UrgencyTimeFrameType urgencyTimeFrameType;
    @CCD(label = "Supplements", searchable = false)
    private final List<Element<Supplement>> supplementsBundle;
    @CCD(label = "Draft Orders", searchable = false)
    private final List<Element<UploadApplicationDraftOrder>> additionalDraftOrdersBundle;
    @CCD(label = "Supporting documents", searchable = false)
    private final List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;
    @CCD(label = "Date submitted", searchable = false)
    private final String uploadedDateTime;
    @CCD(label = "Uploaded by", searchable = false)
    private final String author;
    @CCD(label = "Application type", searchable = false)
    private final C2ApplicationTypeEnum type;
    @CCD(label = "How soon does the judge need to consider the application?", searchable = false)
    private final Urgency urgency;
    @CCD(label = "Application details", searchable = false)
    private final C2ApplicationDetails c2ApplicationDetails;
    @CCD(
            label = "Document is related to Familyman Case number/Names/CCD",
            showCondition = "documentAcknowledge=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo documentRelatedToCase;
    @CCD(label = "Hearing to adjourn", searchable = false)
    private final String requestedHearingToAdjourn;
    @CCD(label = "Application status", searchable = false)
    private final String applicationStatus;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(label = " ", searchable = false, typeOverride = FieldType.Label)
  private String documentAcknowledgeLabel;
  // ==== end synthesised definition-only fields ====
}
