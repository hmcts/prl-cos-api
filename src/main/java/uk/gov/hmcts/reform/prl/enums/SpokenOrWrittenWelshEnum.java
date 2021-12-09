package uk.gov.hmcts.reform.prl.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpokenOrWrittenWelshEnum {

    Spoken("Will need to speak Welsh"),
    Written("Will need to read and write in Welsh"),
    Both("Both");

    private final String displayedValue;

}
