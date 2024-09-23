package uk.gov.hmcts.reform.prl.enums.serviceofdocuments;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum SodSolicitorServingRespondentsEnum {

    @JsonProperty("applicantLegalRepresentative")
    applicantLegalRepresentative("applicantLegalRepresentative", "Applicant's legal representative");

    private final String id;
    private final String displayedValue;

    public String getId() {
        return id;
    }

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static SodSolicitorServingRespondentsEnum getValue(String key) {
        return SodSolicitorServingRespondentsEnum.valueOf(key);
    }
}
