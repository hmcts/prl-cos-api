package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamUrgencyReasonChecklistEnum {

    @JsonProperty("miamPolicyUpgradeUrgencyReason_Value_1")
    MIAM_URGENCY_REASON_CHECKLIST_ENUM_CHK_LIST_ENUM_VALUE_1(
        "miamPolicyUpgradeUrgencyReason_Value_1",
        "There is risk to the life, liberty or physical safety of the prospective applicant or his or her family or his or her home."
    ),
    @JsonProperty("miamPolicyUpgradeUrgencyReason_Value_2")
    MIAM_URGENCY_REASON_CHECKLIST_ENUM_CHK_LIST_ENUM_VALUE_2(
        "miamPolicyUpgradeUrgencyReason_Value_2",
        "Any delay caused by attending a MIAM would cause a risk of harm to a child."
    ),
    @JsonProperty("miamPolicyUpgradeUrgencyReason_Value_3")
    MIAM_URGENCY_REASON_CHECKLIST_ENUM_CHK_LIST_ENUM_VALUE_3(
        "miamPolicyUpgradeUrgencyReason_Value_3",
        "Any delay caused by attending a MIAM would cause a risk of unlawful removal"
            + " of a child from the United Kingdom, or a risk of unlawful retention of a child who is currently outside England and Wales."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_4")
    MIAM_URGENCY_REASON_CHECKLIST_ENUM_CHK_LIST_ENUM_VALUE_4(
        "miamDomesticAbuseChecklistEnum_Value_4",
        "Any delay caused by attending a MIAM would cause a significant risk of a miscarriage of justice."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_5")
    MIAM_URGENCY_REASON_CHECKLIST_ENUM_CHK_LIST_ENUM_VALUE_5(
        "miamDomesticAbuseChecklistEnum_Value_5",
        "Any delay caused by attending a MIAM would cause significant financial hardship to the prospective applicant."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_6")
    MIAM_URGENCY_REASON_CHECKLIST_ENUM_CHK_LIST_ENUM_VALUE_6(
        "miamDomesticAbuseChecklistEnum_Value_6",
        "Any delay caused by attending a MIAM would cause irretrievable problems"
            + " in dealing with the dispute (including the irretrievable loss of significant evidence)."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_7")
    MIAM_URGENCY_REASON_CHECKLIST_ENUM_CHK_LIST_ENUM_VALUE_7(
        "miamDomesticAbuseChecklistEnum_Value_7",
        "There is a significant risk that in the period necessary to schedule"
            + " and attend a MIAM, proceedings relating to the dispute will be brought in another"
            + " state in which a valid claim to jurisdiction may exist, such that a court in that"
            + " other State would be seized of the dispute before a court in England and Wales."
    );

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MiamUrgencyReasonChecklistEnum getValue(String key) {
        return MiamUrgencyReasonChecklistEnum.valueOf(key);
    }

}
