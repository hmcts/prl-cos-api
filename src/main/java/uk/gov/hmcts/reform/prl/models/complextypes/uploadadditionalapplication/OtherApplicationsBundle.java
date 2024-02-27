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

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class OtherApplicationsBundle {
    @JsonProperty("applicantName")
    private final String applicantName;
    @JsonProperty("caApplicantApplicationType")
    private final CaOtherApplicationType caApplicantApplicationType;
    @JsonProperty("caRespondentApplicationType")
    private final CaOtherApplicationType caRespondentApplicationType;
    @JsonProperty("daApplicantApplicationType")
    private final DaApplicantOtherApplicationType daApplicantApplicationType;
    @JsonProperty("daRespondentApplicationType")
    private final DaRespondentOtherApplicationType daRespondentApplicationType;
    @JsonProperty("applicationType")
    private final OtherApplicationType applicationType;
    @JsonProperty("document")
    private final Document document;
    @JsonProperty("finalDocument")
    private final List<Element<Document>> finalDocument;
    @JsonProperty("documentAcknowledge")
    private final List<DocumentAcknowledge> documentAcknowledge;
    @JsonProperty("urgencyTimeFrameType")
    private final UrgencyTimeFrameType urgencyTimeFrameType;
    @JsonProperty("supplementsBundle")
    private final List<Element<Supplement>> supplementsBundle;
    @JsonProperty("supportingEvidenceBundle")
    private final List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;
    @JsonProperty("uploadedDateTime")
    private final String uploadedDateTime;
    @JsonProperty("author")
    private final String author;
    @JsonProperty("urgency")
    private final Urgency urgency;
    @JsonProperty("documentRelatedToCase")
    private final YesOrNo documentRelatedToCase;
    private final String applicationStatus;

}
