package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum C2ApplicationTypeEnum {
    @CCD(
            label = "Application with notice. The other party will be notified about this application, even if there is no hearing"
    )
    @JsonProperty("applicationWithNotice")
    applicationWithNotice(
        "applicationWithNotice",
        "Application with notice. The other party will be notified about this application, even if there is no hearing"
    ),
    @CCD(
            label = "Application by consent or without notice. No notice will be sent to the other party if the application is without notice, even if there is a hearing"
    )
    @JsonProperty("applicationWithoutNotice")
    applicationWithoutNotice(
        "applicationWithoutNotice",
        "Application by consent or without notice. No notice will be sent to the other party if the application "
            + "is without notice, even if there is a hearing"
    );


    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static C2ApplicationTypeEnum getValue(String key) {
        return C2ApplicationTypeEnum.valueOf(key);
    }
}

