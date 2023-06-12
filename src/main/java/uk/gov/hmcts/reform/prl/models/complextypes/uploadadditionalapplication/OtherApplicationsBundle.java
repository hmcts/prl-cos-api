package uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.CaOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DaOtherApplicationType;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.DocumentAcknowledge;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.UrgencyTimeFrameType;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Builder
@Data
public class OtherApplicationsBundle {
    @JsonProperty("applicantName")
    private final String applicantName;
    @JsonProperty("caApplicationType")
    private final CaOtherApplicationType caApplicationType;
    @JsonProperty("daApplicationType")
    private final DaOtherApplicationType daApplicationType;
    @JsonProperty("document")
    private final Document document;
    @JsonProperty("documentAcknowledge")
    private final List<DocumentAcknowledge> documentAcknowledge;
    @JsonProperty("urgencyTimeFrameType")
    private final UrgencyTimeFrameType urgencyTimeFrameType;
    @JsonProperty("supplementsBundle")
    private final List<Element<Supplement>> supplementsBundle;
    @JsonProperty("supportingEvidenceBundle")
    private final List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;
    private final String uploadedDateTime;
    private final String author;

}
