package uk.gov.hmcts.reform.prl.enums.editandapprove;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum OrderApprovalDecisionsForSolicitorOrderEnum {
    @CCD(label = "Send to admin to serve")
    @JsonProperty("sendToAdminToServe")
    sendToAdminToServe("sendToAdminToServe", "Send to admin to serve"),
    @CCD(label = "Give admin further directions then serve")
    @JsonProperty("giveAdminFurtherDirectionsAndServe")
    giveAdminFurtherDirectionsAndServe("giveAdminFurtherDirectionsAndServe", "Give admin further directions then serve"),
    @CCD(label = "Edit the order myself and send to admin to serve")
    @JsonProperty("editTheOrderAndServe")
    editTheOrderAndServe("editTheOrderAndServe", "Edit the order myself and send to admin to serve"),
    @CCD(label = "Ask the legal representative to make changes")
    @JsonProperty("askLegalRepToMakeChanges")
    askLegalRepToMakeChanges("askLegalRepToMakeChanges", "Ask the legal representative to make changes");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static OrderApprovalDecisionsForSolicitorOrderEnum getValue(String key) {
        return OrderApprovalDecisionsForSolicitorOrderEnum.valueOf(key);
    }
}
