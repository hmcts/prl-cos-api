package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.QuarantineLegalDoc;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.managedocuments.ManageDocuments;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class DocumentManagementDetails {

    @JsonProperty("isC8DocumentPresent")
    private String isC8DocumentPresent;

    @JsonProperty("legalProfQuarantineDocsList")
    private List<Element<QuarantineLegalDoc>> legalProfQuarantineDocsList;
    @JsonProperty("courtStaffQuarantineDocsList")
    private List<Element<QuarantineLegalDoc>> courtStaffQuarantineDocsList;
    @JsonProperty("cafcassQuarantineDocsList")
    private List<Element<QuarantineLegalDoc>> cafcassQuarantineDocsList;
    @JsonProperty("localAuthorityQuarantineDocsList")
    private List<Element<QuarantineLegalDoc>> localAuthorityQuarantineDocsList;
    @JsonProperty("citizenQuarantineDocsList")
    private List<Element<QuarantineLegalDoc>> citizenQuarantineDocsList;
    @JsonProperty("tempQuarantineDocumentList")
    List<Element<QuarantineLegalDoc>> tempQuarantineDocumentList;

    @JsonProperty("courtNavQuarantineDocumentList")
    List<Element<QuarantineLegalDoc>> courtNavQuarantineDocumentList;

    //NOT IN USE
    @JsonProperty("citizenUploadQuarantineDocsList")
    private List<Element<UploadedDocuments>> citizenUploadQuarantineDocsList;

    //PRL-3562 - manage document enhancements
    @JsonProperty("manageDocuments")
    private List<Element<ManageDocuments>> manageDocuments;
    private String manageDocumentsTriggeredBy;
    private String manageDocumentsRestrictedFlag;

    @JsonIgnore
    public List<Element<QuarantineLegalDoc>> getRemovableDocuments() {
        return Stream.of(
            legalProfQuarantineDocsList,
            courtStaffQuarantineDocsList,
            cafcassQuarantineDocsList,
            citizenQuarantineDocsList,
            courtNavQuarantineDocumentList
        )
          .filter(Objects::nonNull)
          .flatMap(List::stream)
          .toList();
    }

}
