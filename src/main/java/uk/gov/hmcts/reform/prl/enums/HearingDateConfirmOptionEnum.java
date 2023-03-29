package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum HearingDateConfirmOptionEnum {


    @JsonProperty("dateReservedWithListAssit")
    dateReservedWithListAssit("dateReservedWithListAssit", "The date is reserved with List Assist"),
    @JsonProperty("dateConfirmedInHearingsTab")
    dateConfirmedInHearingsTab("dateConfirmedInHearingsTab", "The date is confirmed in the Hearings tab"),
    @JsonProperty("dateConfirmedByListingTeam")
    dateConfirmedByListingTeam("dateConfirmedByListingTeam", "The date needs to be confirmed by the listing team before service"),
    @JsonProperty("dateToBeFixed")
    dateToBeFixed("dateToBeFixed", "This order will be served with the 'date to be fixed'");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static HearingDateConfirmOptionEnum getValue(String key) {
        return HearingDateConfirmOptionEnum.valueOf(key);
    }
}
