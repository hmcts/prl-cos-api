package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum FL401RejectReasonEnum {

    @CCD(label = "Consent Order not provided")
    @JsonProperty("consentOrderNotProvided")
    consentOrderNotProvided("consentOrderNotProvided","Consent Order not provided","Consent order not provided\n\n"
        + "Your application is being returned because the document uploaded is not a draft consent order "
        + "and/or is not signed by both parties."
        + "\n\nNext steps"
        + "\n\nPlease upload the correct version of the document and it contains all the relevant details.\n\n\n"),
    @CCD(label = "Witness statement not provided")
    @JsonProperty("witnessStatementNotProvided")
    witnessStatementNotProvided("witnessStatementNotProvided","Witness statement not provided","Witness statement not provided\n\n"
        + "Your application is being returned because the witness statement not provided."
        + "\n\nNext steps\n\n"
        + "Please provide the witness statement \n\n\n"),
    @CCD(label = "Confidential detail listed")
    @JsonProperty("confidentalDetailListed")
    confidentalDetailListed("confidentalDetailListed","Confidential detail listed","Confidential detail listed\n\n"
        + "Your application has been returned because you have listed "
        + "some of your confidential details in the application forms.\n\n"
        + "Next steps\n\n"
        + "Please remove your confidential details from the application.\n\n\n"),
    @CCD(label = "Section 91(14) order in force")
    @JsonProperty("section9114OrderInForce")
    section9114OrderInForce("section9114OrderInForce","Section 91(14) order in force","Section 91(14) order in force\n\n"
        + "Your application has been returned because a Section 91(14) order is in force.\n"
        + "In case number <x> your client is prevented from making a further application until <date>.\n\n"
        + "Next steps\n\n"
        + "Please state why your client should be given permission to make this application.\n\n\n"),
    @CCD(label = "Permission is needed to make application")
    @JsonProperty("permissionIsNeeded")
    permissionIsNeeded("permissionIsNeeded","Permission is needed to make application","Permission is needed to make application\n\n"
        + "Your application has been returned because the reason for permission to make this application has not been provided."
        + "\n\nNext steps\n\n"
        + "Please complete the “Have you applied to the court for permission to make this application?” "
        + "question in the Type of application section in the form.\n\n\n"),
    @CCD(label = "Parental responsibility")
    @JsonProperty("parentalResponsibility")
    parentalResponsibility("parentalResponsibility","Parental responsibility","Parental responsibility\n\n"
        + "Your application has been returned because you have not stated who has parental responsibility for each child."
        + "\n\nNext steps\n\n"
        + "Please check the application and ensure all relevant sections have been completed in full.\n\n\n\n"),
    @CCD(label = "Application incomplete")
    @JsonProperty("applicationIncomplete")
    applicationIncomplete("applicationIncomplete","Application incomplete","Application incomplete\n\n"
        + "Your application has been returned because the application is not complete and does not contain the all required information.\n"
        + "You may need to request additional information to progress the case."
        + "\n\nNext steps\n\n"
        + "Please check the application and ensure all relevant sections have been completed in full.\n\n\n"),
    @CCD(label = "Application incorrect")
    @JsonProperty("applicationIncorrect")
    applicationIncorrect("applicationIncorrect","Application incorrect","Application incorrect\n\n"
        + "Your application has been returned because the application has not been filled out correctly."
        + "\n\nNext steps\n\n"
        + "Please check the application and ensure all relevant sections have been completed correctly.\n\n\n"),
    @CCD(label = "Clarification needed")
    @JsonProperty("clarificationNeeded")
    clarificationNeeded("clarificationNeeded","Clarification needed","Clarification needed\n\n"
        + "Your application has been returned because some details are not clear.\n\n"
        + "Next steps\n\n"
        + "Please check the application and ensure all relevant sections are clear.\n\n\n"),
    @CCD(label = "Other")
    @JsonProperty("otherReason")
    otherReason("otherReason","Other reason","Other reason\n\n\n"),;

    private final String id;
    private final String displayedValue;
    private final String returnMsgText;
}

