package uk.gov.hmcts.reform.prl.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MIAMExemptionsChecklistEnum {

    domesticViolence("Domestic violence"),
    urgency("Urgency"),
    previousMIAMattendance("Previous MIAM attendance or previous MIAM exemption"),
    other("Other");


    private final String displayedValue;

}
