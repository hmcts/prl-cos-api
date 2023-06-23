package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum CaRespondentOtherApplicationType {

    @JsonProperty("C1_APPLY_FOR_CERTAIN_ORDERS_UNDER_THE_CHILDREN_ACT")
    C1_APPLY_FOR_CERTAIN_ORDERS_UNDER_THE_CHILDREN_ACT(
        "C1_APPLY_FOR_CERTAIN_ORDERS_UNDER_THE_CHILDREN_ACT",
        "C1 - Apply for certain orders under the Children Act"
    ),
    @JsonProperty("C3_SEARCH_AND_TAKING_CHARGE_OF_A_CHILD")
    C3_SEARCH_AND_TAKING_CHARGE_OF_A_CHILD(
        "C3_SEARCH_AND_TAKING_CHARGE_OF_A_CHILD",
        "C3 - Application for an order authorizing search and taking charge of a child"
    ),
    @JsonProperty("C4_WHEREABOUTS_OF_A_MISSING_CHILD")
    C4_WHEREABOUTS_OF_A_MISSING_CHILD(
        "C4_WHEREABOUTS_OF_A_MISSING_CHILD",
        "C4 - Application for an order for disclosure of a child’s whereabouts"
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
    @JsonProperty("FP25_WITNESS_SUMMONS")
    FP25_WITNESS_SUMMONS(
        "FP25_WITNESS_SUMMONS",
        "FP25 - Witness summons"
    ),
    @JsonProperty("N161_APPELLANT_NOTICE")
    N161_APPELLANT_NOTICE(
        "N161_APPELLANT_NOTICE",
        "N161 - Appellant’s notice"
    ),
    @JsonProperty("D89_BAILIFF")
    D89_BAILIFF(
        "D89_BAILIFF",
        "D89 - Bailiff"
    );

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonValue
    public String getId() {
        return id;
    }

    @JsonCreator
    public static CaRespondentOtherApplicationType getValue(String key) {
        return CaRespondentOtherApplicationType.valueOf(key);
    }

}
