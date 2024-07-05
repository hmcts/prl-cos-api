package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DomesticAbuseOrdersEnum {

    @JsonProperty("nonMolestationOrderFL401A")
    nonMolestationOrderFL401A("nonMolestationOrderFL401A", "Non-molestation order (FL404A)"),
    @JsonProperty("occupationOrder")
    occupationOrder("occupationOrder", "Occupation order (FL404)"),
    @JsonProperty("powerOfArrest")
    powerOfArrest("powerOfArrest", "Power of arrest (FL406)"),
    @JsonProperty("blankOrder")
    blankOrder("blankOrder", "Blank order (FL404B)"),
    @JsonProperty("amendedDischargedVariedOrder")
    amendedDischargedVariedOrder("amendedDischargedVariedOrder", "Amended, discharged or varied order (FL404B)"),
    @JsonProperty("generalFormOfUndertaking")
    generalFormOfUndertaking("generalFormOfUndertaking", "General form of undertaking (N117)"),
    @JsonProperty("warrantOfArrest")
    warrantOfArrest("warrantOfArrest", "Warrant of arrest (FL408)"),
    @JsonProperty("remandOrder")
    remandOrder("remandOrder", "Remand order (FL409)"),
    @JsonProperty("recognizance")
    recognizance("recognizance", "Form for taking of recognizance (FL410)"),
    @JsonProperty("surveyForm")
    surveyForm("surveyForm", "Form for taking of surety (FL411)"),
    @JsonProperty("bailNotice")
    bailNotice("bailNotice", "Bail notice (FL412)"),
    @JsonProperty("hospitalOrder")
    hospitalOrder("hospitalOrder", "Hospital order (FL413)"),
    @JsonProperty("guardianshipOrder")
    guardianshipOrder("guardianshipOrder", "Guardianship order (FL414)"),
    @JsonProperty("statementOfService")
    statementOfService("statementOfService", "Statement of service (FL415)"),
    @JsonProperty("blankOrderFL415")
    blankOrderFL415("blankOrderFL415", "Blank order (FL415)"),
    @JsonProperty("landlordOrMortgageNotice")
    landlordOrMortgageNotice("landlordOrMortgageNotice", "Notice to landlord or mortgage company (FL416)"),
    @JsonProperty("forcedMarriageFl402A")
    forcedMarriageFl402A("forcedMarriageFl402A", "Notice of proceedings Forced Marriage (FL402A)"),
    @JsonProperty("forcedMarriageFmpo")
    forcedMarriageFmpo("forcedMarriageFmpo", "Forced Marriage Protection Order (FMPO)"),
    @JsonProperty("noticeOfProceedingsFgm002")
    noticeOfProceedingsFgm002("noticeOfProceedingsFgm002", "Notice of proceedings FGM (FGM002)"),
    @JsonProperty("fgmProtectionOrderFgmpo")
    fgmProtectionOrderFgmpo("fgmProtectionOrderFgmpo", "FGM Protection Order (FGMPO)");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DomesticAbuseOrdersEnum getValue(String key) {
        return DomesticAbuseOrdersEnum.valueOf(key);
    }
}
