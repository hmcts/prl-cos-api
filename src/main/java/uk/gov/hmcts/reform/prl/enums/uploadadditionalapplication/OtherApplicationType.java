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
public enum OtherApplicationType {

    @JsonProperty("C1_CHILD_ORDER")
    C1_CHILD_ORDER(
        "C1_CHILD_ORDER",
        "C1 - Apply for certain orders under the Children Act"
    ),
    @JsonProperty("C3_CHILD_ORDER")
    C3_CHILD_ORDER(
        "C3_CHILD_ORDER",
        "C3 - Application for an order authorizing search and taking charge of a child"
    ),
    @JsonProperty("C4_CHILD_ORDER")
    C4_CHILD_ORDER(
        "C4_CHILD_ORDER",
        "C4 - Application for an order for disclosure of a child’s whereabouts"
    ),
    @JsonProperty("C79_CHILD_ORDER")
    C79_CHILD_ORDER(
        "C79_CHILD_ORDER",
        "C79 - Application to enforce a child arrangements order"
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
        "FP25 - Witness summons"
    ),
    @JsonProperty("FC600_COMMITTAL_APPLICATION")
    FC600_COMMITTAL_APPLICATION(
        "FC600_COMMITTAL_APPLICATION",
        "FC600 - Committal application"
    ),
    @JsonProperty("N161_APPELLANT_NOTICE")
    N161_APPELLANT_NOTICE(
        "N161_APPELLANT_NOTICE",
        "N161 - Appellant’s notice"
    ),
    @JsonProperty("FL403_EXTEND_AN_ORDER")
    FL403_EXTEND_AN_ORDER(
        "FL403_EXTEND_AN_ORDER",
                "FL403 - Application to vary, discharge or extend an order"
    ),
    @JsonProperty("FL407_ARREST_WARRANT")
    FL407_ARREST_WARRANT(
        "FL407_ARREST_WARRANT",
                "FL407 - Application for a warrant of arrest"
    ),
    @JsonProperty("D89_COURT_BAILIFF")
    D89_COURT_BAILIFF(
        "D89_COURT_BAILIFF",
        "D89 - Request for personal service by a court bailiff"
    );

    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static OtherApplicationType getValue(String key) {
        return OtherApplicationType.valueOf(key);
    }

}
