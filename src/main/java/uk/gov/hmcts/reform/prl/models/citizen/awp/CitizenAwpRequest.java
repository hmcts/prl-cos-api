package uk.gov.hmcts.reform.prl.models.citizen.awp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.FeeType;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CitizenAwpRequest {

    @JsonProperty("awpType")
    private String awpType;
    @JsonProperty("awpReason")
    private String awpReason;
    @JsonProperty("partyId")
    private String partyId;
    @JsonProperty("partyName")
    private String partyName;
    @JsonProperty("partyType")
    private String partyType;
    @JsonProperty("feeType")
    private FeeType feeType;
    @JsonProperty("awp_completedForm")
    private YesOrNo completedForm;
    @JsonProperty("awp_agreementForRequest")
    private YesOrNo agreementForRequest;
    @JsonProperty("awp_informOtherParties")
    private YesOrNo informOtherParties;
    @JsonProperty("awp_reasonCantBeInformed")
    private String reasonCantBeInformed;
    @JsonProperty("awp_need_hwf")
    private YesOrNo needHwf;
    @JsonProperty("awp_have_hwfReference")
    private YesOrNo haveHwfReference;
    @JsonProperty("awp_hwf_referenceNumber")
    private String hwfReferenceNumber;
    @JsonProperty("awp_uploadedApplicationForms")
    private List<Document> uploadedApplicationForms;
    @JsonProperty("awp_hasSupportingDocuments")
    private YesOrNo hasSupportingDocuments;
    @JsonProperty("awp_supportingDocuments")
    private List<Document> supportingDocuments;
    @JsonProperty("awp_isThereReasonForUrgentRequest")
    private YesOrNo urgencyInFiveDays;
    @JsonProperty("awp_urgentRequestReason")
    private String urgencyInFiveDaysReason;
    @JsonProperty("awp_cancelDelayHearing")
    private String hearingToDelayCancel;

}
