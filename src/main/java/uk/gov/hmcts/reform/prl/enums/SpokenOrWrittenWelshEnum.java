package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpokenOrWrittenWelshEnum {

    @JsonProperty("spoken")
    spoken("Will need to speak Welsh"),
    @JsonProperty("written")
    written("Will need to read and write in Welsh"),
    @JsonProperty("both")
    both("Both");

    private final String displayedValue;

}
