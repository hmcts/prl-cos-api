package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamDomesticAbuseChecklistEnum {

    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_1")
    miamDomesticAbuseChecklistEnum_Value_1(
        "miamDomesticAbuseChecklistEnum_Value_1",
        "Evidence that a prospective party has been arrested for a relevant domestic abuse offence."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_2")
    miamDomesticAbuseChecklistEnum_Value_2(
        "miamDomesticAbuseChecklistEnum_Value_2",
        "Evidence of a relevant police caution for a domestic abuse offence."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_3")
    miamDomesticAbuseChecklistEnum_Value_3(
        "miamDomesticAbuseChecklistEnum_Value_3",
        "Evidence of relevant criminal proceedings for a domestic abuse offence which have not concluded."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_4")
    miamDomesticAbuseChecklistEnum_Value_4(
        "miamDomesticAbuseChecklistEnum_Value_4",
        "Evidence of a relevant conviction for a domestic abuse offence."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_5")
    miamDomesticAbuseChecklistEnum_Value_5(
        "miamDomesticAbuseChecklistEnum_Value_5",
        "A court order binding a prospective party over in connection with a domestic abuse offence."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_6")
    miamDomesticAbuseChecklistEnum_Value_6(
        "miamDomesticAbuseChecklistEnum_Value_6",
        "A domestic violence protection notice issued under section 24"
            + " of the Crime and Security Act 2010 against a prospective party."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_7")
    miamDomesticAbuseChecklistEnum_Value_7(
        "miamDomesticAbuseChecklistEnum_Value_7",
        "A domestic abuse protection notice given under section 22"
            + " of the Domestic Abuse Act 2021 against a prospective party."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_8")
    miamDomesticAbuseChecklistEnum_Value_8(
        "miamDomesticAbuseChecklistEnum_Value_8",
        "A relevant protective injunction."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_9")
    miamDomesticAbuseChecklistEnum_Value_9(
        "miamDomesticAbuseChecklistEnum_Value_9",
        "An undertaking given in England and Wales under section 46"
            + " or 63E of the Family Law Act 1996 (or given in Scotland or Northern Ireland"
            + " in place of a protective injunction) by a prospective party, provided that a cross- undertaking"
            + " relating to domestic violence or domestic abuse was not given by another prospective party."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_10")
    miamDomesticAbuseChecklistEnum_Value_10(
        "miamDomesticAbuseChecklistEnum_Value_10",
        "A copy of a finding of fact, made in proceedings in the United Kingdom,"
            + " that there has been domestic abuse by a prospective party."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_11")
    miamDomesticAbuseChecklistEnum_Value_11(
        "miamDomesticAbuseChecklistEnum_Value_11",
        "An expert report produced as evidence in proceedings in the United Kingdom"
            + " for the benefit of a court or tribunal confirming that a person with whom a prospective"
            + " party is or was personally connected, was assessed as being, or at risk of being,"
            + " a victim of domestic abuse by that prospective party."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_12")
    miamDomesticAbuseChecklistEnum_Value_12(
        "miamDomesticAbuseChecklistEnum_Value_12",
        "A letter or report from an appropriate health professional confirming that-"
            + " (i) that professional, or another appropriate health professional, has examined a prospective party in person,"
            + " by telephone or by video conferencing; and (ii) in the reasonable professional judgment of the author or the"
            + " examining appropriate health professional, that prospective party has, or has had, injuries or a"
            + " condition consistent with being a victim of domestic abuse."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_13")
    miamDomesticAbuseChecklistEnum_Value_13(
        "miamDomesticAbuseChecklistEnum_Value_13",
        "A letter or report from - (i) the appropriate health professional who made the referral"
            + " described below; (ii) an appropriate health professional who has access to the medical"
            + " records of the prospective party referred to below; or (iii) the person to whom the referral described"
            + " below was made; confirming that there was a referral by an appropriate health professional of a prospective"
            + " party to a person who provides specialist support or assistance for victims of, or those at risk of, domestic abuse."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_14")
    miamDomesticAbuseChecklistEnum_Value_14(
        "miamDomesticAbuseChecklistEnum_Value_14",
        "A letter from any person who is a member of a multi-agency risk assessment conference (or other suitable"
            + " local safeguarding forum) confirming that a prospective party, or a person with whom that prospective party"
            + " is personally connected, is or has been at risk of harm from domestic abuse by another prospective party."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_15")
    miamDomesticAbuseChecklistEnum_Value_15(
        "miamDomesticAbuseChecklistEnum_Value_15",
        "A letter from an independent domestic violence advisor (IDVA) confirming that they are providing or have provided"
            + " support to a prospective party."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_16")
    miamDomesticAbuseChecklistEnum_Value_16(
        "miamDomesticAbuseChecklistEnum_Value_16",
        "A letter from an independent sexual violence advisor (ISVA) confirming that they are providing or have provided"
            + " support to a prospective party relating to sexual violence by another prospective party."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_17")
    miamDomesticAbuseChecklistEnum_Value_17(
        "miamDomesticAbuseChecklistEnum_Value_17",
        "A letter from an officer employed by a local authority or housing association (or their equivalent in Scotland or Northern Ireland)"
            + " for the purpose of supporting tenants containing- (i) a statement to the effect that, in their reasonable professional"
            + " judgment, a person with whom a prospective party is or has been personally connected to is, or is at risk of being,"
            + " a victim of domestic abuse by that prospective party; (ii) a description of the specific matters relied upon to support "
            + "that judgment; and (iii) a description of the support they provided to the victim of domestic abuse or the person at risk of "
            + "domestic abuse by that prospective party."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_18")
    miamDomesticAbuseChecklistEnum_Value_18(
        "miamDomesticAbuseChecklistEnum_Value_18",
        "A letter which- (i) is from an organisation providing domestic abuse support services, which letter confirms that it-"
            + " (a) is situated in the United Kingdom, (b) has been operating for an uninterrupted period of six months or more; and"
            + " (c) provided a prospective party with support in relation to that person’s"
            + " needs as a victim, or a person at risk, of domestic abuse; and (ii) contains-"
            + " (a) a statement to the effect that, in the reasonable professional judgment of the author of the letter, the prospective party"
            + " is, or is at risk of being, a victim of domestic abuse; (b) a description of the specific matters relied upon to support that"
            + " judgment; (c) a description of the support provided to the prospective party; and (d) a statement of the reasons why"
            + " the prospective party needed that support."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_19")
    miamDomesticAbuseChecklistEnum_Value_19(
        "miamDomesticAbuseChecklistEnum_Value_19",
        "A letter or report from an organisation providing domestic abuse support services in the United Kingdom confirming-"
            + " (i) that a person with whom a prospective party is or was personally connected was refused admission to a refuge; (ii) the"
            + " date on which they were refused admission to the refuge; and (iii) they sought admission to the refuge because of allegations"
            + " of domestic abuse by the prospective party referred to in paragraph (i)."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_20")
    miamDomesticAbuseChecklistEnum_Value_20(
        "miamDomesticAbuseChecklistEnum_Value_20",
        "A letter from a public authority confirming that a person with whom a prospective party is or was personally connected,"
            + " was assessed as being, or at risk of being, a victim of domestic abuse by that prospective party (or a copy of that assessment)."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_21")
    miamDomesticAbuseChecklistEnum_Value_21(
        "miamDomesticAbuseChecklistEnum_Value_21",
        "A letter from the Secretary of State for the Home Department confirming that a prospective party has been granted leave"
            + " to remain in the United Kingdom as a victim of domestic abuse."
    ),
    @JsonProperty("miamDomesticAbuseChecklistEnum_Value_22")
    miamDomesticAbuseChecklistEnum_Value_22(
        "miamDomesticAbuseChecklistEnum_Value_22",
        "Evidence which demonstrates that a prospective party has been, or is at risk of being,"
            + " the victim of domestic abuse by another prospective party in the form of abuse which relates to financial matters."
    );

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MiamDomesticAbuseChecklistEnum getValue(String key) {
        return MiamDomesticAbuseChecklistEnum.valueOf(key);
    }

}
