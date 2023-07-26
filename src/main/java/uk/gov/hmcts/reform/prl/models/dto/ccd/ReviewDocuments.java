package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewDocuments {

    private DynamicList reviewDocsDynamicList;
    private YesNoDontKnow reviewDecisionYesOrNo;
    private String docToBeReviewed;
    private Document reviewDoc;

    private List<Element<QuarantineLegalDoc>> legalProfUploadDocListConfTab;
    private List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTab;
    private List<Element<QuarantineLegalDoc>> cafcassUploadDocListConfTab;
    private List<Element<QuarantineLegalDoc>> cafcassUploadDocListDocTab;
    private List<Element<QuarantineLegalDoc>> courtStaffUploadDocListConfTab;
    private List<Element<QuarantineLegalDoc>> courtStaffUploadDocListDocTab;
    private List<Element<QuarantineLegalDoc>> citizenUploadDocListConfTab;
    private List<Element<QuarantineLegalDoc>> citizenUploadedDocListDocTab;

    public static String[] reviewDocTempFields() {
        return new String[]{
            "reviewDocsDynamicList", "docToBeReviewed", "reviewDoc"
        };
    }
}
