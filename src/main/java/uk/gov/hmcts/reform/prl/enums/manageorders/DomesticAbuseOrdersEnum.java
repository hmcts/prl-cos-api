package uk.gov.hmcts.reform.prl.enums.manageorders;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DomesticAbuseOrdersEnum {

    @CCD(label = "Non-molestation order (FL404A)")
    @JsonProperty("nonMolestationOrderFL401A")
    nonMolestationOrderFL401A("nonMolestationOrderFL401A", "Non-molestation order (FL404A)"),
    @CCD(label = "Occupation order (FL404)")
    @JsonProperty("occupationOrder")
    occupationOrder("occupationOrder", "Occupation order (FL404)"),
    @CCD(label = "Power of arrest (FL406)")
    @JsonProperty("powerOfArrest")
    powerOfArrest("powerOfArrest", "Power of arrest (FL406)"),
    @CCD(label = "Blank order (FL404B)")
    @JsonProperty("blankOrder")
    blankOrder("blankOrder", "Blank order (FL404B)"),
    @CCD(label = "Amended, discharged or varied order (FL404B)")
    @JsonProperty("amendedDischargedVariedOrder")
    amendedDischargedVariedOrder("amendedDischargedVariedOrder", "Amended, discharged or varied order (FL404B)"),
    @CCD(label = "General form of undertaking (N117)")
    @JsonProperty("generalFormOfUndertaking")
    generalFormOfUndertaking("generalFormOfUndertaking", "General form of undertaking (N117)"),
    @CCD(label = "Warrant of arrest (FL408)")
    @JsonProperty("warrantOfArrest")
    warrantOfArrest("warrantOfArrest", "Warrant of arrest (FL408)"),
    @CCD(label = "Remand order (FL409)")
    @JsonProperty("remandOrder")
    remandOrder("remandOrder", "Remand order (FL409)"),
    @CCD(label = "Form for taking of recognizance (FL410)")
    @JsonProperty("recognizance")
    recognizance("recognizance", "Form for taking of recognizance (FL410)"),
    @CCD(label = "Form for taking of surety (FL411)")
    @JsonProperty("surveyForm")
    surveyForm("surveyForm", "Form for taking of surety (FL411)"),
    @CCD(label = "Bail notice (FL412)")
    @JsonProperty("bailNotice")
    bailNotice("bailNotice", "Bail notice (FL412)"),
    @CCD(label = "Hospital order (FL413)")
    @JsonProperty("hospitalOrder")
    hospitalOrder("hospitalOrder", "Hospital order (FL413)"),
    @CCD(label = "Guardianship order (FL414)")
    @JsonProperty("guardianshipOrder")
    guardianshipOrder("guardianshipOrder", "Guardianship order (FL414)"),
    @CCD(label = "Statement of service (FL415)")
    @JsonProperty("statementOfService")
    statementOfService("statementOfService", "Statement of service (FL415)"),
    @CCD(label = "Blank order (FL415)")
    @JsonProperty("blankOrderFL415")
    blankOrderFL415("blankOrderFL415", "Blank order (FL415)"),
    @CCD(label = "Notice to landlord or mortgage company (FL416)")
    @JsonProperty("landlordOrMortgageNotice")
    landlordOrMortgageNotice("landlordOrMortgageNotice", "Notice to landlord or mortgage company (FL416)");

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
