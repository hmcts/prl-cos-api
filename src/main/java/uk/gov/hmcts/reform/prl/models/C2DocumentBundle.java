package uk.gov.hmcts.reform.prl.models;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication.*;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.Supplement;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.SupportingEvidenceBundle;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.UploadApplicationDraftOrder;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class C2DocumentBundle {

    private final Document document;
    private final List<DocumentAcknowledge> documentAcknowledge;
    private final List<C2AdditionalOrdersRequested> c2AdditionalOrdersRequested;
    private final ParentalResponsibilityType parentalResponsibilityType;
    private final DynamicList hearingList;
    private final UrgencyTimeFrameType urgencyTimeFrameType;
    private final List<Element<Supplement>> supplementsBundle;
    private final List<Element<UploadApplicationDraftOrder>> uploadApplicationDraftOrdersBundle;
    private final List<Element<SupportingEvidenceBundle>> supportingEvidenceBundle;

}
