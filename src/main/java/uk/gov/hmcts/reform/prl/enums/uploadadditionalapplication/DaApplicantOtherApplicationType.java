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
public enum DaApplicantOtherApplicationType {

    @CCD(label = "FL403 - Application to vary, discharge or extend an order")
    @JsonProperty("FL403_EXTEND_AN_ORDER")
    FL403_EXTEND_AN_ORDER(
        "FL403_EXTEND_AN_ORDER",
        "FL403 - Application to vary, discharge or extend an order"
    ),
    @CCD(label = "EX740 - Application to prohibit cross examination (victim)")
    @JsonProperty("EX740_CROSS_EXAMINATION_VICTIM")
    EX740_CROSS_EXAMINATION_VICTIM(
        "EX740_CROSS_EXAMINATION_VICTIM",
        "EX740 - Application to prohibit cross examination (victim)"
    ),
    @CCD(label = "EX741 - Application to prohibit cross examination (perpetrator)")
    @JsonProperty("EX741_CROSS_EXAMINATION_PERPETRATOR")
    EX741_CROSS_EXAMINATION_PERPETRATOR(
        "EX741_CROSS_EXAMINATION_PERPETRATOR",
        "EX741 - Application to prohibit cross examination (perpetrator)"
    ),
    @CCD(label = "FP25 - Witness summons")
    @JsonProperty("FP25_WITNESS_SUMMONS")
    FP25_WITNESS_SUMMONS(
        "FP25_WITNESS_SUMMONS",
        "FP25 - Witness summonss"
    ),
    @CCD(label = "FL407 - Application for a warrant of arrest")
    @JsonProperty("FL407_ARREST_WARRANT")
    FL407_ARREST_WARRANT(
        "FL407_ARREST_WARRANT",
        "FL407 - Application for a warrant of arrest"
    ),
    @CCD(label = "FC600 - Committal application")
    @JsonProperty("FC600_COMMITTAL_APPLICATION")
    FC600_COMMITTAL_APPLICATION(
        "FC600_COMMITTAL_APPLICATION",
        "FC600 - Committal applicationt"
    ),
    @CCD(label = "N161 - Appellant’s notice")
    @JsonProperty("N161_APPELLANT_NOTICE")
    N161_APPELLANT_NOTICE(
        "N161_APPELLANT_NOTICE",
        "N161 - Appellant’s notice"
    ),
    @CCD(label = "D89 - Request for personal service by a court bailiff")
    @JsonProperty("D89_COURT_BAILIFF")
    D89_COURT_BAILIFF(
        "D89_COURT_BAILIFF",
        "D89 - Request for personal service by a court bailiff"
    );
    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static DaApplicantOtherApplicationType getValue(String key) {
        return DaApplicantOtherApplicationType.valueOf(key);
    }

}
