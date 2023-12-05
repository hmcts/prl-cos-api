package uk.gov.hmcts.reform.prl.enums.editandapprove;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum JudgeApprovalDecisionsCourtAdminEnum {
    @JsonProperty("sendToAdminToServe")
    SEND_TO_ADMIN_TO_SERVE("sendToAdminToServe", "Send to admin to serve"),
    @JsonProperty("giveAdminFurtherDirectionsAndServe")
    GIVE_ADMIN_FURTHER_DIRECTIONS_AND_SERVE("giveAdminFurtherDirectionsAndServe", "Give admin further directions then serve"),
    @JsonProperty("editTheOrderAndServe")
    EDIT_THE_ORDER_AND_SERVE("editTheOrderAndServe", "Edit the order myself and send to admin to serve");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static JudgeApprovalDecisionsCourtAdminEnum getValue(String key) {
        return JudgeApprovalDecisionsCourtAdminEnum.valueOf(key);
    }
}
