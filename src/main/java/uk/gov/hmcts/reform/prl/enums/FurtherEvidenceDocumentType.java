package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FurtherEvidenceDocumentType {

    @JsonProperty("consentOrder")
    FEMALE("consentOrder", "Consent order"),
    @JsonProperty("miamCertificate")
    MALE("miamCertificate", "MIAM certificate"),
    @JsonProperty("previousOrders")
    OTHER("previousOrders", "Previous orders");

    private final String id;
    private final String displayedValue;
}
