package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoNotSure;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReviewDocuments {

    private DynamicList reviewDocsDynamicList;
    private YesNoNotSure reviewDecisionYesOrNo;
    private String docToBeReviewed;
    private Document reviewDoc;

    //NOT IN USE
    private List<Element<QuarantineLegalDoc>> legalProfUploadDocListConfTab;
    private List<Element<QuarantineLegalDoc>> cafcassUploadDocListConfTab;
    private List<Element<QuarantineLegalDoc>> courtStaffUploadDocListConfTab;
    private List<Element<QuarantineLegalDoc>> bulkScannedDocListConfTab;
    //private List<Element<UploadedDocuments>> citizenUploadDocListConfTab;
    //private List<Element<UploadedDocuments>> citizenUploadedDocListDocTab;


    private List<Element<QuarantineLegalDoc>> legalProfUploadDocListDocTab;
    private List<Element<QuarantineLegalDoc>> cafcassUploadDocListDocTab;
    private List<Element<QuarantineLegalDoc>> courtStaffUploadDocListDocTab;
    private List<Element<QuarantineLegalDoc>> bulkScannedDocListDocTab;
    private List<Element<QuarantineLegalDoc>> citizenUploadedDocListDocTab;
    private List<Element<QuarantineLegalDoc>> courtNavUploadedDocListDocTab;


    //PRL-4320 - manage docs redesign
    private List<Element<QuarantineLegalDoc>> restrictedDocuments;
    private List<Element<QuarantineLegalDoc>> confidentialDocuments;

    public static String[] reviewDocTempFields() {
        return new String[]{
            "reviewDocsDynamicList", "docToBeReviewed", "reviewDoc", "tempQuarantineDocumentList"
        };
    }

    @JsonIgnore
    public List<Element<QuarantineLegalDoc>> getRemovableDocuments() {
        return Stream.of(
                legalProfUploadDocListDocTab,
                cafcassUploadDocListDocTab,
                courtStaffUploadDocListDocTab,
                bulkScannedDocListDocTab,
                citizenUploadedDocListDocTab,
                courtNavUploadedDocListDocTab,
                restrictedDocuments,
                confidentialDocuments
            )
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
    }
}
