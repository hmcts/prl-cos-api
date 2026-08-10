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
public enum SupplementType {

    @CCD(label = "C13A - Special guardianship order")
    @JsonProperty("C13A_SPECIAL_GUARDIANSHIP")
    C13A_SPECIAL_GUARDIANSHIP("C13A_SPECIAL_GUARDIANSHIP", "C13A - Special guardianship order"),
    @CCD(label = "C14 - Authority to refuse contact with a child in care")
    @JsonProperty("C14_AUTHORITY_TO_REFUSE_CONTACT_WITH_CHILD")
    C14_AUTHORITY_TO_REFUSE_CONTACT_WITH_CHILD(
        "C14_AUTHORITY_TO_REFUSE_CONTACT_WITH_CHILD",
        "C14 - Authority to refuse contact with a child in care"
    ),
    @CCD(label = "C15 - Contact with a child in care")
    @JsonProperty("C15_CONTACT_WITH_CHILD_IN_CARE")
    C15_CONTACT_WITH_CHILD_IN_CARE("C15_CONTACT_WITH_CHILD_IN_CARE", "C15 - Contact with a child in cares"),
    @CCD(label = "C16 - Child assessment")
    @JsonProperty("C16_CHILD_ASSESSMENT")
    C16_CHILD_ASSESSMENT("C16_CHILD_ASSESSMENT", "C16 - Child assessment"),
    @CCD(label = "C18 - Recovery order")
    @JsonProperty("C18_RECOVERY_ORDER")
    C18_RECOVERY_ORDER("C18_RECOVERY_ORDER", "C18 - Recovery order"),
    @CCD(label = "C20 - Secure accommodation")
    @JsonProperty("C20_SECURE_ACCOMMODATION")
    C20_SECURE_ACCOMMODATION("C20_SECURE_ACCOMMODATION", "C20 - Secure accommodation");

    private final String id;
    private final String displayedValue;

    @JsonCreator
    public static SupplementType getValue(String key) {
        return SupplementType.valueOf(key);
    }

}
