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
public enum ChildArrangementOrdersEnum {

    @CCD(label = "Standard directions order")
    @JsonProperty("standardDirectionsOrder")
    standardDirectionsOrder("standardDirectionsOrder", "Standard directions order"),
    @CCD(label = "Blank order or directions (C21)")
    @JsonProperty("blankOrderOrDirections")
    blankOrderOrDirections("blankOrderOrDirections", "Blank order or directions (C21)"),
    @CCD(label = "Child arrangements, specific issue or prohibited steps order (C43)")
    @JsonProperty("caSpecificProhibitedOrder")
    caSpecificProhibitedOrder("caSpecificProhibitedOrder", "Child arrangements, specific issue or prohibited steps order (C43)"),
    @CCD(label = "Parental responsibility order (C45A)")
    @JsonProperty("parentalResponsibilityOrder")
    parentalResponsibilityOrder("parentalResponsibilityOrder", "Parental responsibility order (C45A)"),
    @CCD(label = "Special guardianship order (C43A)")
    @JsonProperty("splGuardianshipOrder")
    splGuardianshipOrder("splGuardianshipOrder", "Special guardianship order (C43A)"),
    @CCD(label = "Declaration of parentage order (C63A)")
    @JsonProperty("declarationOfParentageOrder")
    declarationOfParentageOrder("declarationOfParentageOrder", "Declaration of parentage order (C63A)"),
    @JsonProperty("transferOfCaseOrder")
    transferOfCaseOrder("transferOfCaseOrder", "Transfer of case to another court (C49)"),
    @CCD(label = "Order to disclose information about whereabouts of a child (C30)")
    @JsonProperty("discloseOrder")
    discloseOrder("discloseOrder", "Order to disclose information about whereabouts of a child (C30)"),
    @CCD(label = "Authority for search, taking charge and delivery of a child (C31)")
    @JsonProperty("authorityC31")
    authorityC31("authorityC31", "Authority for search, taking charge and delivery of a child (C31)"),
    @CCD(label = "Family assistance order (C42)")
    @JsonProperty("familyAssistOrder")
    familyAssistOrder("familyAssistOrder", "Family assistance order (C42)"),
    @CCD(label = "Leave to change a child's surname or remove from the jurisdiction (C44)")
    @JsonProperty("leaveToChangeC44")
    leaveToChangeC44("leaveToChangeC44", "Leave to change a child's surname or remove from the jurisdiction (C44)"),
    @CCD(label = "Appointment of a guardian (C47A)")
    @JsonProperty("guardianAppointmentC47A")
    guardianAppointmentC47A("guardianAppointmentC47A", "Appointment of a guardian (C47A)"),
    @CCD(label = "Appointment of a solicitor for the child (C48A)")
    @JsonProperty("solicitorAppointmentC48A")
    solicitorAppointmentC48A("solicitorAppointmentC48A", "Appointment of a solicitor for the child (C48A)"),
    @CCD(label = "Enforcement of a child arrangements order (C80)")
    @JsonProperty("caEnforcementC80")
    caEnforcementC80("caEnforcementC80", "Enforcement of a child arrangements order (C80)"),
    @CCD(label = "Financial compensation order following C79 enforcement application (C82)")
    @JsonProperty("financialCompensationC82")
    financialCompensationC82("financialCompensationC82", "Financial compensation order following C79 enforcement application (C82)"),
    @CCD(label = "Summons to appear at court for directions in contempt proceedings (FC601)")
    @JsonProperty("summonsFC601")
    summonsFC601("summonsFC601", "Summons to appear at court for directions in contempt proceedings (FC601)"),
    @CCD(label = "Warrant to secure attendance at court (FC602)")
    @JsonProperty("secureAttendanceFC602")
    secureAttendanceFC602("secureAttendanceFC602", "Warrant to secure attendance at court (FC602)"),
    @CCD(label = "Order on determination of proceedings for contempt of court (FC603)")
    @JsonProperty("fc603Order")
    fc603Order("fc603Order", "Order on determination of proceedings for contempt of court (FC603)"),
    @CCD(label = "Warrant of committal (FC604)")
    @JsonProperty("committalWarrantFC604")
    committalWarrantFC604("committalWarrantFC604", "Warrant of committal (FC604)"),
    @JsonProperty("directionOnIssue")
    directionOnIssueOrder("directionOnIssue", "Directions on issue");


    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static ChildArrangementOrdersEnum getValue(String key) {
        return ChildArrangementOrdersEnum.valueOf(key);
    }
}
