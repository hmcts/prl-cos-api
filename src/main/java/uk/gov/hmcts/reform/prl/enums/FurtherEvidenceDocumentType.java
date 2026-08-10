package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "docTypeFurtherEvidence", generate = true)
@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum FurtherEvidenceDocumentType {

    @CCD(label = "Consent order")
    @JsonProperty("consentOrder")
    consentOrder("consentOrder", "Consent order"),
    @CCD(label = "MIAM certficate")
    @JsonProperty("miamCertificate")
    miamCertificate("miamCertificate", "MIAM certificate"),
    @CCD(label = "Previous orders")
    @JsonProperty("previousOrders")
    previousOrders("previousOrders", "Previous orders");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static FurtherEvidenceDocumentType getValue(String key) {
        return FurtherEvidenceDocumentType.valueOf(key);
    }
}
