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

@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class C2DocumentBundle {

    private final String applicantName;
    private final Document document;
    private final List<Element<Document>> finalDocument;
    private final List<DocumentAcknowledge> documentAcknowledge;
    private final List<C2AdditionalOrdersRequestedCa> caReasonsForC2Application;
    private final List<C2AdditionalOrdersRequestedCa> daReasonsForC2Application;
    private final List<CombinedC2AdditionalOrdersRequested> combinedReasonsForC2Application;
    private final String otherReasonsFoC2Application;
    private final ParentalResponsibilityType parentalResponsibilityType;
    private final DynamicList hearingList;
    private final UrgencyTimeFrameType urgencyTimeFrameType;
    private final List<Element<Supplement>> supplementsBundle;
    private final List<Element<UploadApplicationDraftOrder>> additionalDraftOrdersBundle;
    private final List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;
    private final String uploadedDateTime;
    private final String author;
    private final C2ApplicationTypeEnum type;
    private final Urgency urgency;
    private final C2ApplicationDetails c2ApplicationDetails;
    private final YesOrNo documentRelatedToCase;
    private final String requestedHearingToAdjourn;
    private final String applicationStatus;

}
