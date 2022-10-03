package uk.gov.hmcts.reform.prl.models.dto.bundle;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CallbackRequest;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

public class BundleCallbackRequest<T extends CaseData> extends CallbackRequest {

    @JsonProperty("caseTypeId")
    private String caseTypeId;

    @JsonProperty("jurisdictionId")
    private String jurisdictionId;

    public BundleCallbackRequest(CallbackRequest callback) {
        super(callback.getCaseDetails(), callback.getEventId());
        setCaseTypeId(callback.getCaseDetails().getCaseData().getSelectedCaseTypeID());
        setJurisdictionId(callback.getCaseDetails().getCaseData().getCourtId());
    }

    public String getCaseTypeId() {
        return caseTypeId;
    }

    public void setCaseTypeId(String caseTypeId) {
        this.caseTypeId = caseTypeId;
    }

    public String getJurisdictionId() {
        return jurisdictionId;
    }

    public void setJurisdictionId(String jurisdictionId) {
        this.jurisdictionId = jurisdictionId;
    }

}
