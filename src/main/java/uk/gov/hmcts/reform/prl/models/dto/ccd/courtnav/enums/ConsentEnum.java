package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ConsentEnum {

    @JsonProperty("applicantConfirm")
    applicantConfirm("applicantConfirm", "I believe that the facts stated in this form and any continuation\n"
                         + "sheets are true. "),

    @JsonProperty("legalAidConfirm")
    legalAidConfirm("legalAidConfirm", "The applicant believes that the facts stated in this form and any continuation "
                     + "sheets are true. I am authorised by the applicant to sign this statement. ");

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
    public static ConsentEnum getValue(String key) {
        return ConsentEnum.valueOf(key);
    }
}
