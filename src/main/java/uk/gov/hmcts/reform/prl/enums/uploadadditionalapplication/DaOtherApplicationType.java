package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DaOtherApplicationType {

    @JsonProperty("FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER")
    FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER(
        "FL403_APPLICATION_TO_VARY_DISCHARGE_OR_EXTEND_AN_ORDER",
        "FL403 - Application to vary, discharge or extend an order"
    ),
    @JsonProperty("EX740_APPLICATION_TO_PROHIBIT_CROSS_EXAMINATION_VICTIM")
    EX740_APPLICATION_TO_PROHIBIT_CROSS_EXAMINATION_VICTIM(
        "EX740_APPLICATION_TO_PROHIBIT_CROSS_EXAMINATION_VICTIM",
        "EX740 - Application to prohibit cross examination (victim)"
    ),
    @JsonProperty("EX741_APPLICATION_TO_PROHIBIT_CROSS_EXAMINATION_PERPETRATOR")
    EX741_APPLICATION_TO_PROHIBIT_CROSS_EXAMINATION_PERPETRATOR(
        "EX741_APPLICATION_TO_PROHIBIT_CROSS_EXAMINATION_PERPETRATOR",
        "EX741 - Application to prohibit cross examination (perpetrator)"
    ),
    @JsonProperty("C2_MAKE_AN_APPLICATION_IN_EXISTING_PROCEEDINGS")
    C2_MAKE_AN_APPLICATION_IN_EXISTING_PROCEEDINGS(
        "C2_MAKE_AN_APPLICATION_IN_EXISTING_PROCEEDINGS",
        "C2 - Make an application in existing proceedings"
    ),
    @JsonProperty("FP25_WITNESS_SUMMONS")
    FP25_WITNESS_SUMMONS(
        "FP25_WITNESS_SUMMONS",
        "FP25 - Witness summonss"
    ),
    @JsonProperty("FL407_APPLICATION_FOR_A_WARRANT_OF_ARREST")
    FL407_APPLICATION_FOR_A_WARRANT_OF_ARREST(
        "FL407_APPLICATION_FOR_A_WARRANT_OF_ARREST",
        "FL407 - Application for a warrant of arrest"
    ),
    @JsonProperty("FC600_COMMITTAL_APPLICATION")
    FC600_COMMITTAL_APPLICATION(
        "FC600_COMMITTAL_APPLICATION",
        "FC600 - Committal applicationt"
    ),
    @JsonProperty("N161_APPELLANT_NOTICE")
    N161_APPELLANT_NOTICE(
        "N161_APPELLANT_NOTICE",
        "N161 - Appellant’s notic"
    );
    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DaOtherApplicationType getValue(String key) {
        return DaOtherApplicationType.valueOf(key);
    }

}
