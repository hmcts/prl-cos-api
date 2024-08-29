package uk.gov.hmcts.reform.prl.enums.uploadadditionalapplication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
@Getter
public enum DaRespondentOtherApplicationType {

    @JsonProperty("FL403_EXTEND_AN_ORDER")
    FL403_EXTEND_AN_ORDER(
        "FL403_EXTEND_AN_ORDER",
        "FL403 - Application to vary, discharge or extend an order"
    ),
    @JsonProperty("EX740_CROSS_EXAMINATION_VICTIM")
    EX740_CROSS_EXAMINATION_VICTIM(
            "EX740_CROSS_EXAMINATION_VICTIM",
            "EX740 - Application to prohibit cross examination (victim)"
    ),
    @JsonProperty("EX741_CROSS_EXAMINATION_PERPETRATOR")
    EX741_CROSS_EXAMINATION_PERPETRATOR(
        "EX741_CROSS_EXAMINATION_PERPETRATOR",
        "EX741 - Application to prohibit cross examination (perpetrator)"
    ),
    @JsonProperty("FP25_WITNESS_SUMMONS")
    FP25_WITNESS_SUMMONS(
        "FP25_WITNESS_SUMMONS",
        "FP25 - Witness summonss"
    ),
    @JsonProperty("N161_APPELLANT_NOTICE")
    N161_APPELLANT_NOTICE(
        "N161_APPELLANT_NOTICE",
        "N161 - Appellant’s notice"
    ),
    @JsonProperty("D89_COURT_BAILIFF")
    D89_COURT_BAILIFF(
        "D89_COURT_BAILIFF",
        "D89 - Request for personal service by a court bailiff"
    ),
    @JsonProperty("FC600_COMMITTAL_APPLICATION")
    FC600_COMMITTAL_APPLICATION(
        "FC600_COMMITTAL_APPLICATION",
        "FC600 - Committal applicationt"
    );
    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static DaRespondentOtherApplicationType getValue(String key) {
        return DaRespondentOtherApplicationType.valueOf(key);
    }

}
