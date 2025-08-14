package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum ApplicationStatus {

    SUBMITTED("Submitted"),
    PENDING_ON_PAYMENT("Pending on payment");

    private final String displayedValue;

    @JsonCreator
    public static ApplicationStatus getValue(String key) {
        return ApplicationStatus.valueOf(key);
    }

}
