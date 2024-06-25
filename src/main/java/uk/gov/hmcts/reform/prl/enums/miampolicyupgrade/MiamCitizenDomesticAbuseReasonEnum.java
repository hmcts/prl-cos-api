package uk.gov.hmcts.reform.prl.enums.miampolicyupgrade;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum MiamCitizenDomesticAbuseReasonEnum {

    @JsonProperty("policeInvolvement")
    policeInvolvement("policeInvolvement", "mpuPoliceInvolvement"),
    @JsonProperty("courtInvolvement")
    courtInvolvement("courtInvolvement", "mpuCourtInvolvement"),
    @JsonProperty("letterOfBeingVictim")
    letterOfBeingVictim("letterOfBeingVictim", "mpuLetterOfBeingVictim"),
    @JsonProperty("letterFromAuthority")
    letterFromAuthority("letterFromAuthority", "mpuletterFromAuthority"),
    @JsonProperty("letterFromSupportService")
    letterFromSupportService("letterFromSupportService", "mpuLetterFromSupportService"),


    @JsonProperty("evidenceOfSomeoneArrest")
    evidenceOfSomeoneArrest("evidenceOfSomeoneArrest", "miamDomesticAbuseChecklistEnum_Value_1"),
    @JsonProperty("evidenceOfPolice")
    evidenceOfPolice("evidenceOfPolice", "miamDomesticAbuseChecklistEnum_Value_2"),
    @JsonProperty("evidenceOfOnGoingCriminalProceeding")
    evidenceOfOnGoingCriminalProceeding("evidenceOfOnGoingCriminalProceeding",
                                        "miamDomesticAbuseChecklistEnum_Value_3"),
    @JsonProperty("evidenceOfConviction")
    evidenceOfConviction("evidenceOfConviction", "miamDomesticAbuseChecklistEnum_Value_4"),
    @JsonProperty("boundedByCourtAction")
    boundedByCourtAction("boundedByCourtAction","miamDomesticAbuseChecklistEnum_Value_5"),
    @JsonProperty("evidenceOfSection24Notice")
    evidenceOfSection24Notice(
        "evidenceOfSection24Notice", "miamDomesticAbuseChecklistEnum_Value_6"),
    @JsonProperty("evidenceOfSection22Notice")
    evidenceOfSection22Notice(
        "evidenceOfSection22Notice", "miamDomesticAbuseChecklistEnum_Value_7"),
    @JsonProperty("protectionInjuction")
    protectionInjuction("protectionInjuction","miamDomesticAbuseChecklistEnum_Value_8"),
    @JsonProperty("undertaking")
    undertaking(
        "undertaking", "miamDomesticAbuseChecklistEnum_Value_9"),
    @JsonProperty("ukDomesticViolence")
    ukDomesticViolence("ukDomesticViolence", "miamDomesticAbuseChecklistEnum_Value_10"),
    @JsonProperty("ukPotentialVictim")
    ukPotentialVictim("ukPotentialVictim", "miamDomesticAbuseChecklistEnum_Value_11"),
    @JsonProperty("letterFromHealthProfessional")
    letterFromHealthProfessional("letterFromHealthProfessional","miamDomesticAbuseChecklistEnum_Value_12"),
    @JsonProperty("referralLetterFromHealthProfessional")
    referralLetterFromHealthProfessional("referralLetterFromHealthProfessional",
                                         "miamDomesticAbuseChecklistEnum_Value_13"),
    @JsonProperty("letterFromMultiAgencyMember")
    letterFromMultiAgencyMember("letterFromMultiAgencyMember", "miamDomesticAbuseChecklistEnum_Value_14"),
    @JsonProperty("letterFromDomesticViolenceAdvisor")
    letterFromDomesticViolenceAdvisor("letterFromDomesticViolenceAdvisor","miamDomesticAbuseChecklistEnum_Value_15"),
    @JsonProperty("letterFromSexualViolenceAdvisor")
    letterFromSexualViolenceAdvisor(
        "letterFromSexualViolenceAdvisor", "miamDomesticAbuseChecklistEnum_Value_16"),
    @JsonProperty("letterFromOfficer")
    letterFromOfficer(
        "letterFromOfficer","miamDomesticAbuseChecklistEnum_Value_17"),
    @JsonProperty("letterFromOrgDomesticViolenceSupport")
    letterFromOrgDomesticViolenceSupport("letterFromOrgDomesticViolenceSupport",
                                         "miamDomesticAbuseChecklistEnum_Value_18"),
    @JsonProperty("letterFromOrgDomesticViolenceInUk")
    letterFromOrgDomesticViolenceInUk(
        "letterFromOrgDomesticViolenceInUk", "miamDomesticAbuseChecklistEnum_Value_19"),
    @JsonProperty("letterFromPublicAuthority")
    letterFromPublicAuthority("letterFromPublicAuthority","miamDomesticAbuseChecklistEnum_Value_20"),
    @JsonProperty("ILRDuetoDomesticAbuse")
    ILRDuetoDomesticAbuse("ILRDuetoDomesticAbuse","miamDomesticAbuseChecklistEnum_Value_21"),
    @JsonProperty("financialAbuse")
    financialAbuse("financialAbuse", "miamDomesticAbuseChecklistEnum_Value_22");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static MiamCitizenDomesticAbuseReasonEnum getValue(String key) {
        return MiamCitizenDomesticAbuseReasonEnum.valueOf(key);
    }

}
