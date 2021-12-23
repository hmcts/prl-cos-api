package uk.gov.hmcts.reform.prl.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpokenOrWrittenWelshEnum {

    @JsonProperty("spoken")
    Spoken("Will need to speak Welsh"),
    @JsonProperty("written")
    Written("Will need to read and write in Welsh"),
    @JsonProperty("both")
    Both("Both");

    private final String displayedValue;

}
