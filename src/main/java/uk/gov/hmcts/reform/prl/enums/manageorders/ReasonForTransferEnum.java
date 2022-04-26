package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.ApplicantOrChildren;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;


@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum ReasonForTransferEnum {

    @JsonProperty("transferReason1")
    transferReason1("transferReason1", "The transfer will significantly accelerate the determination of the proceedings."),
    @JsonProperty("transferReason2")
    transferReason2("transferReason2", "There is a real possibility of difficulty in resolving conflicts in the evidence of witnesses."),
    @JsonProperty("transferReason3")
    transferReason3("transferReason3", "There is a real possibility of conflict in the evidence of two or more experts."),
    @JsonProperty("transferReason4")
    transferReason4("transferReason4", "There is a novel or difficult point of law."),
    @JsonProperty("transferReason5")
    transferReason5("transferReason5", "There are proceedings concerning the child in another jurisdiction or there are international law issues."),
    @JsonProperty("transferReason6")
    transferReason6("transferReason6", "There is a real possibility that enforcement proceedings may be necessary and the method" +
        " of enforcement or the likely penalty is beyond the powers of a magistrates court."),
    @JsonProperty("transferReason7")
    transferReason7("transferReason7", "There is another good reason for the proceedings to be transferred.");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ReasonForTransferEnum getValue(String key) {
        return ReasonForTransferEnum.valueOf(key);
    }

}
