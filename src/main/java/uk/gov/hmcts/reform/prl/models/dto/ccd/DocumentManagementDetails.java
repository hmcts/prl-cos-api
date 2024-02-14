package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;

import java.util.List;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class DocumentManagementDetails {

    @JsonProperty("isC8DocumentPresent")
    private String isC8DocumentPresent;

    @JsonProperty("legalProfQuarantineDocsList")
    private List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsList;
    //PRL-4328 - To be deleted
    @JsonProperty("courtStaffQuarantineDocsList")
    private List<Element<QuarantineLegalDoc>> courtStaffQuarantineDocsList;
    @JsonProperty("citizenUploadQuarantineDocsList")
    private List<Element<UploadedDocuments>> citizenUploadQuarantineDocsList;

    @JsonProperty("tempQuarantineDocumentList")
    List<Element<QuarantineLegalDoc>> tempQuarantineDocumentList;
    @JsonProperty("cafcassQuarantineDocsList")
    private List<Element<QuarantineLegalDoc>> cafcassQuarantineDocsList;


    //PRL-3562 - manage document enhancements
    @JsonProperty("manageDocuments")
    private List<Element<ManageDocuments>> manageDocuments;
    private String manageDocumentsTriggeredBy;
    private String manageDocumentsRestrictedFlag;
    //PRL-4320 - manage document redesign
    //


    public DocumentManagementDetails() {
    }
}
