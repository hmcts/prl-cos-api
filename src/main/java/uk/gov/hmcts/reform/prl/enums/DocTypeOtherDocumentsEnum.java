package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.api.ComplexType;

@ComplexType(name = "docTypeOtherDocumentsEnum", generate = true)
@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DocTypeOtherDocumentsEnum {

    @CCD(
            label = "Applicant statement - for example photographic evidence, witness statement, \nmobile phone screenshot"
    )
    @JsonProperty("applicantStatement")
    applicantStatement("applicantStatement", "Applicant statement - for example photographic evidence, witness statement, mobile phone screenshot"),
    @CCD(label = "Cafcass reports")
    @JsonProperty("cafcassReports")
    cafcassReports("cafcassReports", "Cafcass reports"),
    @CCD(label = "Expert reports")
    @JsonProperty("expertReports")
    expertReports("expertReports", "Expert reports"),
    @CCD(label = "Respondent reports")
    @JsonProperty("respondentReports")
    respondentReports("respondentReports", "Respondent reports"),
    @CCD(label = "Other reports")
    @JsonProperty("otherReports")
    otherReports("otherReports", "Other reports"),
    @CCD(label = "Draft orders")
    @JsonProperty("draftOrders")
    draftOrders("draftOrders", "Draft orders"),

    @CCD(label = "Finalised order")
    @JsonProperty("approvedOrders")
    approvedOrders("approvedOrders", "Finalised order"),
    @CCD(label = "Transcripts and judgments")
    @JsonProperty("transcriptsOfJudgements")
    transcriptsOfJudgements("transcriptsOfJudgements","Transcripts and judgments"),
    @CCD(label = "Magistrates facts and reasons")
    @JsonProperty("magistratesFactsAndReasons")
    magistratesFactsAndReasons("magistratesFactsAndReasons", "Magistrates facts and reasons"),
    @CCD(label = "Judge notes from hearing")
    @JsonProperty("judgeNotesFromHearing")
    judgeNotesFromHearing("judgeNotesFromHearing","Judge notes from hearing"),
    @CCD(label = "Position statements")
    @JsonProperty("positionStatements")
    positionStatements("positionStatements","Position statements"),
    @CCD(label = "FM5 statement on NCDR")
    @JsonProperty("fm5Statements")
    fm5Statements("fm5Statements", "FM5 statement on NCDR"),
    @CCD(label = "Applicant application")
    @JsonProperty("applicantApplication")
    applicantApplication("applicantApplication","Applicant application"),
    @CCD(label = "Applicant C1A application")
    @JsonProperty("applicantC1AApplication")
    applicantC1AApplication("applicantC1AApplication", "Applicant C1A application"),
    @CCD(label = "Applicant C1A response")
    @JsonProperty("applicantC1AResponse")
    applicantC1AResponse("applicantC1AResponse","Applicant C1A response"),
    @CCD(label = "Applications within proceedings")
    @JsonProperty("applicationsWithinProceedings")
    applicationsWithinProceedings("applicationsWithinProceedings", "Applications within proceedings"),
    @CCD(label = "MIAM certificate/Exemption")
    @JsonProperty("MIAMCertificate")
    MIAMCertificate("MIAMCertificate","MIAM certificate/Exemption"),
    @CCD(label = "Orders from other proceedings")
    @JsonProperty("previousOrdersSubmittedWithApplication")
    previousOrdersSubmittedWithApplication("previousOrdersSubmittedWithApplication", "Orders from other proceedings"),
    @CCD(label = "Respondent application")
    @JsonProperty("respondentApplication")
    respondentApplication("respondentApplication","Respondent application"),
    @CCD(label = "Respondent C1A application")
    @JsonProperty("respondentC1AApplication")
    respondentC1AApplication("respondentC1AApplication","Respondent C1A application"),
    @CCD(label = "Respondent C1A response")
    @JsonProperty("respondentC1AResponse")
    respondentC1AResponse("respondentC1AResponse","Respondent C1A response"),
    @CCD(label = "Applications within proceedings")
    @JsonProperty("applicationsFromOtherProceedings")
    applicationsFromOtherProceedings("applicationsFromOtherProceedings","Applications within proceedings"),
    @CCD(label = "Orders from other proceedings")
    @JsonProperty("ordersFromOtherProceedings")
    ordersFromOtherProceedings("ordersFromOtherProceedings","Orders from other proceedings"),
    @CCD(label = "Applicant's statements")
    @JsonProperty("applicantStatements")
    applicantStatements("applicantStatements","Applicant's statements"),
    @CCD(label = "Respondent's statements")
    @JsonProperty("respondentStatements")
    respondentStatements("respondentStatements","Respondent's statements"),
    @CCD(label = "Other witness statements")
    @JsonProperty("otherWitnessStatements")
    otherWitnessStatements("otherWitnessStatements","Other witness statements"),
    @CCD(label = "Pathfinder")
    @JsonProperty("pathfinder")
    pathfinder("pathfinder","Pathfinder"),
    @CCD(label = "Child Impact Report 1")
    @JsonProperty("childImpactReport1")
    childImpactReport1("childImpactReport1", "Child Impact Report 1"),
    @CCD(label = "Child Impact Report 2")
    @JsonProperty("childImpactReport2")
    childImpactReport2("childImpactReport2", "Child Impact Report 2"),
    @CCD(label = "Safeguarding letter/Safeguarding Enquiries Report (SER)")
    @JsonProperty("safeguardingLetter")
    safeguardingLetter("safeguardingLetter","Safeguarding letter/Safeguarding Enquiries Report (SER)"),
    @CCD(label = "Section 7 report/Child Impact Analysis")
    @JsonProperty("section7Report")
    section7Report("section7Report","Section 7 report/Child Impact Analysis"),
    @CCD(label = "Section 37 report")
    @JsonProperty("section37Report")
    section37Report("section37Report","Section 37 report"),
    @CCD(label = "16a risk assessment")
    @JsonProperty("16ariskAssessment")
    riskAssessment("16aRiskAssessment","16a risk assessment"),
    @JsonProperty("cirTransferRequest")
    cirTransferRequest("cirTransferRequest","CIR Transfer Request"),
    @JsonProperty("cirExtensionRequest")
    cirExtensionRequest("cirExtensionRequest","CIR Extension Request"),
    @CCD(label = "Guardian report")
    @JsonProperty("guardianReport")
    guardianReport("guardianReport","Guardian report"),
    @CCD(label = "Special guardianship report")
    @JsonProperty("specialGuardianshipReport")
    specialGuardianshipReport("specialGuardianshipReport","Special guardianship report"),
    @CCD(label = "Cafcass/Cafcass Cymru other documents")
    @JsonProperty("otherDocs")
    otherDocs("otherDocs", "Cafcass/Cafcass Cymru other documents"),
    @CCD(label = "Section 37 report")
    @JsonProperty("sec37Report")
    sec37Report("sec37Report","Section 37 report"),
    @CCD(label = "Local Authority other documents")
    @JsonProperty("localAuthorityOtherDoc")
    localAuthorityOtherDoc("localAuthorityOtherDoc","Local Authority other documents"),
    @CCD(label = "Child Impact Report 1")
    @JsonProperty("childImpactReport1La")
    childImpactReport1La("childImpactReport1La","Child Impact Report 1"),
    @CCD(label = "Child Impact Report 2")
    @JsonProperty("childImpactReport2La")
    childImpactReport2La("childImpactReport2La","Child Impact Report 2"),
    @CCD(label = "Section 7 report")
    @JsonProperty("section7ReportLa")
    section7ReportLa("section7ReportLa","Section 7 report"),
    @CCD(label = "Section 7 addendum report")
    @JsonProperty("section7AddendumReportLa")
    section7AddendumReportLa("section7AddendumReportLa","Section 7 addendum report"),
    @CCD(label = "Local Authority involvement letter")
    @JsonProperty("localAuthorityInvolvementLa")
    localAuthorityInvolvementLa("localAuthorityInvolvementLa","Local Authority involvement letter"),
    @CCD(label = "Section 47 enquiry")
    @JsonProperty("section47La")
    section47La("section47La","Section 47 enquiry"),
    @JsonProperty("cirExtensionRequestLa")
    cirExtensionRequestLa("cirExtensionRequestLa", "CIR extension request"),
    @JsonProperty("cirTransferRequestLa")
    cirTransferRequestLa("cirTransferRequestLa", "CIR transfer request"),
    @CCD(label = "Medical reports")
    @JsonProperty("medicalReports")
    medicalReports("medicalReports", "Medical reports"),
    @CCD(label = "DNA reports")
    @JsonProperty("DNAReports_expertReport")
    DNAReports_expertReport("DNAReports_expertReport","DNA reports"),
    @CCD(label = "Results of hair strand/blood tests")
    @JsonProperty("resultsOfHairStrandBloodTests")
    resultsOfHairStrandBloodTests("resultsOfHairStrandBloodTests", "Results of hair strand/blood tests"),
    @CCD(label = "Police disclosures")
    @JsonProperty("policeDisclosures")
    policeDisclosures("policeDisclosures","Police disclosures"),
    @CCD(label = "Medical records")
    @JsonProperty("medicalRecords")
    medicalRecords("medicalRecords","Medical records"),
    @CCD(label = "Drug and alcohol test (toxicology)")
    @JsonProperty("drugAndAlcoholTest(toxicology)")
    drugAndAlcoholTest("drugAndAlcoholTest(toxicology)","Drug and alcohol test (toxicology)"),
    @CCD(label = "Police report")
    @JsonProperty("policeReport")
    policeReport("policeReport","Police report"),
    @CCD(label = "Emails to request hearings adjourned")
    @JsonProperty("emailsToCourtToRequestHearingsAdjourned")
    emailsToCourtToRequestHearingsAdjourned("emailsToCourtToRequestHearingsAdjourned", "Emails to request hearings adjourned"),
    @CCD(label = "Public funding certificates")
    @JsonProperty("publicFundingCertificates")
    publicFundingCertificates("publicFundingCertificates", "Public funding certificates"),
    @CCD(label = "Notices of acting/discharge")
    @JsonProperty("noticesOfActingDischarge")
    noticesOfActingDischarge("noticesOfActingDischarge","Notices of acting/discharge"),
    @CCD(label = "Request for FAS forms to be changed")
    @JsonProperty("requestForFASFormsToBeChanged")
    requestForFASFormsToBeChanged("requestForFASFormsToBeChanged","Request for FAS forms to be changed"),
    @CCD(label = "Witness availability")
    @JsonProperty("witnessAvailability")
    witnessAvailability("witnessAvailability","Witness availability"),
    @CCD(label = "Letters of complaint")
    @JsonProperty("lettersOfComplaint")
    lettersOfComplaint("lettersOfComplaint","Letters of complaint"),
    @CCD(label = "SPIP referral requests")
    @JsonProperty("SPIPReferralRequests")
    SPIPReferralRequests("SPIPReferralRequests","SPIP referral requests"),
    @CCD(label = "Home Office/ DWP responses")
    @JsonProperty("homeOfficeDWPResponses")
    homeOfficeDWPResponses("homeOfficeDWPResponses","Home Office/ DWP responses"),
    @CCD(label = "Internal correspondence")
    @JsonProperty("internalCorrespondence")
    internalCorrespondence("internalCorrespondence","Internal correspondence"),
    @CCD(label = "Important information about your address and contact details")
    @JsonProperty("importantInfoAboutAddressAndContact")
    importantInfoAboutAddressAndContact("importantInfoAboutAddressAndContact","Important information about your address and contact details"),
    @CCD(label = "Privacy notice")
    @JsonProperty("privacyNotice")
    privacyNotice("privacyNotice", "Privacy notice"),
    @CCD(label = "Reasonable adjustments and special measures")
    @JsonProperty("specialMeasures")
    specialMeasures("specialMeasures","Reasonable adjustments and special measures"),
    @CCD(label = "Any other documents")
    @JsonProperty("anyOtherDoc")
    anyOtherDoc("anyOtherDoc","Any other documents"),
    @CCD(label = "Notice of hearing")
    @JsonProperty("noticeOfHearing")
    noticeOfHearing("noticeOfHearing","Notice of hearing"),
    @CCD(label = "Court bundle")
    @JsonProperty("courtBundle")
    courtBundle("courtBundle","Court bundle"),
    @CCD(label = "Case summary")
    @JsonProperty("caseSummary")
    caseSummary("caseSummary", "Case summary"),
    @CCD(label = "Confidential")
    @JsonProperty("confidential")
    confidential("confidential", "Confidential");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static DocTypeOtherDocumentsEnum getValue(String key) {
        if (key.equals("16aRiskAssessment")) {
            key = "riskAssessment";
        } else if (key.equals("drugAndAlcoholTest(toxicology)")) {
            key = "drugAndAlcoholTest";
        }
        return DocTypeOtherDocumentsEnum.valueOf(key);
    }
}
