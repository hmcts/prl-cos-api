package uk.gov.hmcts.reform.prl.enums.localauthority;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum TypeOfSocialWorkerEventEnum {

    @JsonProperty("addSocialWorker")
    addSocialWorker("addSocialWorker", "Add Social Worker"),
    @JsonProperty("removeSocialWorker")
    removeSocialWorker("removeSocialWorker", "Remove Social Worker"),;

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static TypeOfSocialWorkerEventEnum getValue(String key) {
        return TypeOfSocialWorkerEventEnum.valueOf(key);
    }

}
