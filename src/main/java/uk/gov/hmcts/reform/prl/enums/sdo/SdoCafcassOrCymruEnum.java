package uk.gov.hmcts.reform.prl.enums.sdo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SdoCafcassOrCymruEnum {

    @CCD(label = "Safeguarding checks: next steps Cafcass")
    @JsonProperty("safeguardingCafcassOnly")
    safeguardingCafcassOnly("safeguardingCafcassOnly", "Safeguarding checks: next steps Cafcass"),
    @CCD(label = "Safeguarding checks: next steps Cafcass Cymru")
    @JsonProperty("safeguardingCafcassCymru")
    safeguardingCafcassCymru("safeguardingCafcassCymru", "Safeguarding checks: next steps Cafcass Cymru"),
    @CCD(label = "Party to provide details of new partner to Cafcass")
    @JsonProperty("partyToProvideDetailsOnly")
    partyToProvideDetailsOnly("partyToProvideDetailsOnly", "Party to provide details of new partner to Cafcass"),
    @CCD(label = "Party to provide details of new partner to Cafcass Cymru")
    @JsonProperty("partyToProvideDetailsCmyru")
    partyToProvideDetailsCmyru("partyToProvideDetailsCmyru", "Party to provide details of new partner to Cafcass Cymru"),
    @CCD(label = "Child Impact Report 1")
    @JsonProperty("childImpactReport1")
    childImpactReport1("childImpactReport1", "Child Impact Report 1"),
    @CCD(label = "Child Impact Report 2")
    @JsonProperty("childImpactReport2")
    childImpactReport2("childImpactReport2", "Child Impact Report 2"),
    @CCD(label = "Section 7 report/Child impact analysis")
    @JsonProperty("section7Report")
    section7Report("section7Report", "Section 7 report/Child impact analysis");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SdoCafcassOrCymruEnum getValue(String key) {
        return SdoCafcassOrCymruEnum.valueOf(key);
    }

}

