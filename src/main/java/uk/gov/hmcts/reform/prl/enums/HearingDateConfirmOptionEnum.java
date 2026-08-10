package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;


@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum HearingDateConfirmOptionEnum {


    @CCD(label = "The date is reserved with List Assist")
    @JsonProperty("dateReservedWithListAssit")
    dateReservedWithListAssit("dateReservedWithListAssit", "The date is reserved with List Assist"),
    @CCD(label = "The date is confirmed in the Hearings tab")
    @JsonProperty("dateConfirmedInHearingsTab")
    dateConfirmedInHearingsTab("dateConfirmedInHearingsTab", "The date is confirmed in the Hearings tab"),
    @CCD(label = "The date needs to be confirmed by the listing team before service")
    @JsonProperty("dateConfirmedByListingTeam")
    dateConfirmedByListingTeam("dateConfirmedByListingTeam", "The date needs to be confirmed by the listing team before service"),
    @CCD(label = "This order will be served with the 'date to be fixed'")
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
