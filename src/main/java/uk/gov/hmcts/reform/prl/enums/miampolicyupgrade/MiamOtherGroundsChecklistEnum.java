package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamOtherGroundsChecklistEnum {

    @JsonProperty("miamPolicyUpgradeOtherGrounds_Value_1")
    miamPolicyUpgradeOtherGrounds_Value_1(
        "miamPolicyUpgradeOtherGrounds_Value_1",
        "The application would be made without notice"
            + " (Paragraph 5.1 of Practice Direction 18A sets out the circumstances"
            + " in which applications may be made without notice)"
    ),
    @JsonProperty("miamPolicyUpgradeOtherGrounds_Value_2")
    miamPolicyUpgradeOtherGrounds_Value_2(
        "miamPolicyUpgradeOtherGrounds_Value_2",
        "A child is one of the prospective parties."
    ),
    @JsonProperty("miamPolicyUpgradeOtherGrounds_Value_3")
    miamPolicyUpgradeOtherGrounds_Value_3(
        "miamPolicyUpgradeOtherGrounds_Value_3",
        "(i) The prospective applicant is not able to attend a MIAM online"
            + " or by video-link and an explanation of why this is the case is provided to"
            + " the court using the text box provided; and (ii) the prospective applicant"
            + " has contacted as many authorised family mediators as have an office within"
            + " 15 miles of his or her home (or 5 of them if there are 5 or more mediators),"
            + " and all of them have stated that they are not available to conduct a MIAM within"
            + " 15 business days of the date of contact."
    ),
    @JsonProperty("miamPolicyUpgradeOtherGrounds_Value_4")
    miamPolicyUpgradeOtherGrounds_Value_4(
        "miamPolicyUpgradeOtherGrounds_Value_4",
        "(i) The prospective applicant is not able to attend a MIAM online or by"
            + " video-link and an explanation of why this is the case is provided to the court using the text box provided; and"
            + " (ii) the prospective applicant is subject to a disability or other inability that would prevent attendance"
            + " in person at a MIAM unless appropriate facilities can be offered by an authorised mediator; and"
            + " (iii) the prospective applicant has contacted as many authorised family mediators as have an"
            + " office within 15 miles of his or her home (or 5 of them if there are 5 or more mediators),"
            + " and all have stated that they are unable to provide such facilities."
    ),
    @JsonProperty("miamPolicyUpgradeOtherGrounds_Value_5")
    miamPolicyUpgradeOtherGrounds_Value_5(
        "miamPolicyUpgradeOtherGrounds_Value_5",
        "(i) The prospective applicant is not able to attend a MIAM online or by video-link; and"
            + " (ii) there is no authorised family mediator with an office within 15 miles of the prospective applicant’s home."
    ),
    @JsonProperty("miamPolicyUpgradeOtherGrounds_Value_6")
    miamPolicyUpgradeOtherGrounds_Value_6(
        "miamPolicyUpgradeOtherGrounds_Value_6",
        "The prospective applicant cannot attend a MIAM because the prospective applicant is"
            + " (i) in prison or any other institution in which the prospective applicant is required to be"
            + " detained and facilities cannot be made available for them to attend a MIAM online or by video link;"
            + " or (ii) subject to conditions of bail that prevent contact with the other person; or"
            + " (iii) subject to a licence with a prohibited contact requirement in relation to"
            + " the other person."
    );

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MiamOtherGroundsChecklistEnum getValue(String key) {
        return MiamOtherGroundsChecklistEnum.valueOf(key);
    }

}
