package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DocTypeOtherDocumentsEnum {

    @JsonProperty("applicantStatement")
    applicantStatement("applicantStatement", "Applicant statement - for example photographic evidence, witness statement, mobile phone screenshot"),
    @JsonProperty("cafcassReports")
    cafcassReports("cafcassReports", "Cafcass reports"),
    @JsonProperty("expertReports")
    expertReports("expertReports", "Expert reports"),
    @JsonProperty("respondentReports")
    respondentReports("respondentReports", "Respondent reports"),
    @JsonProperty("otherReports")
    otherReports("otherReports", "Other reports"),
    @JsonProperty("draftOrders")
    draftOrders("draftOrders", "Draft orders"),

    @JsonProperty("approvedOrders")
    approvedOrders("approvedOrders", "Finalised order"),
    @JsonProperty("transcriptsOfJudgements")
    transcriptsOfJudgements("transcriptsOfJudgements","Transcripts of judgements"),
    @JsonProperty("magistratesFactsAndReasons")
    magistratesFactsAndReasons("magistratesFactsAndReasons", "Magistrates facts and reasons"),
    @JsonProperty("judgeNotesFromHearing")
    judgeNotesFromHearing("judgeNotesFromHearing","Judge notes from hearing"),
    @JsonProperty("positionStatements")
    positionStatements("positionStatements","Position statements"),
    @JsonProperty("fm5Statements")
    fm5Statements("fm5Statements", "FM5 statement on NCDR"),
    @JsonProperty("applicantApplication")
    applicantApplication("applicantApplication","Applicant application"),
    @JsonProperty("applicantC1AApplication")
    applicantC1AApplication("applicantC1AApplication", "Applicant C1A application"),
    @JsonProperty("applicantC1AResponse")
    applicantC1AResponse("applicantC1AResponse","Applicant C1A response"),
    @JsonProperty("applicationsWithinProceedings")
    applicationsWithinProceedings("applicationsWithinProceedings", "Applications within proceedings"),
    @JsonProperty("MIAMCertificate")
    MIAMCertificate("MIAMCertificate","MIAM certificate/Exemption"),
    @JsonProperty("previousOrdersSubmittedWithApplication")
    previousOrdersSubmittedWithApplication("previousOrdersSubmittedWithApplication", "Orders from other proceedings"),
    @JsonProperty("respondentApplication")
    respondentApplication("respondentApplication","Respondent application"),
    @JsonProperty("respondentC1AApplication")
    respondentC1AApplication("respondentC1AApplication","Respondent C1A application"),
    @JsonProperty("respondentC1AResponse")
    respondentC1AResponse("respondentC1AResponse","Respondent C1A response"),
    @JsonProperty("applicationsFromOtherProceedings")
    applicationsFromOtherProceedings("applicationsFromOtherProceedings","Applications within proceedings"),
    @JsonProperty("ordersFromOtherProceedings")
    ordersFromOtherProceedings("ordersFromOtherProceedings","Orders from other proceedings"),
    @JsonProperty("applicantStatements")
    applicantStatements("applicantStatements","Applicant's statements"),
    @JsonProperty("respondentStatements")
    respondentStatements("respondentStatements","Respondent's statements"),
    @JsonProperty("otherWitnessStatements")
    otherWitnessStatements("otherWitnessStatements","Other witness statements"),
    @JsonProperty("pathfinder")
    pathfinder("pathfinder","Pathfinder"),
    @JsonProperty("safeguardingLetter")
    safeguardingLetter("safeguardingLetter","Safeguarding letter/Safeguarding Enquiries Report (SER)"),
    @JsonProperty("section7Report")
    section7Report("section7Report","Section 7 report/Child Impact Analysis"),
    @JsonProperty("section37Report")
    section37Report("section37Report","Section 37 report"),
    @JsonProperty("16ariskAssessment")
    riskAssessment("16aRiskAssessment","16a risk assessment"),
    @JsonProperty("guardianReport")
    guardianReport("guardianReport","Guardian report"),
    @JsonProperty("specialGuardianshipReport")
    specialGuardianshipReport("specialGuardianshipReport","Special guardianship report"),
    @JsonProperty("otherDocs")
    otherDocs("otherDocs", "Cafcass/Cafcass Cymru other documents"),
    @JsonProperty("sec37Report")
    sec37Report("sec37Report","Section 37 report"),
    @JsonProperty("localAuthorityOtherDoc")
    localAuthorityOtherDoc("localAuthorityOtherDoc","Local Authority other documents"),
    @JsonProperty("medicalReports")
    medicalReports("medicalReports", "Medical reports"),
    @JsonProperty("DNAReports_expertReport")
    DNAReports_expertReport("DNAReports_expertReport","DNA reports"),
    @JsonProperty("resultsOfHairStrandBloodTests")
    resultsOfHairStrandBloodTests("resultsOfHairStrandBloodTests", "Results of hair strand/blood tests"),
    @JsonProperty("policeDisclosures")
    policeDisclosures("policeDisclosures","Police disclosures"),
    @JsonProperty("medicalRecords")
    medicalRecords("medicalRecords","Medical records"),
    @JsonProperty("drugAndAlcoholTest(toxicology)")
    drugAndAlcoholTest("drugAndAlcoholTest(toxicology)","Drug and alcohol test (toxicology)"),
    @JsonProperty("policeReport")
    policeReport("policeReport","Police report"),
    @JsonProperty("emailsToCourtToRequestHearingsAdjourned")
    emailsToCourtToRequestHearingsAdjourned("emailsToCourtToRequestHearingsAdjourned", "Emails to request hearings adjourned"),
    @JsonProperty("publicFundingCertificates")
    publicFundingCertificates("publicFundingCertificates", "Public funding certificates"),
    @JsonProperty("noticesOfActingDischarge")
    noticesOfActingDischarge("noticesOfActingDischarge","Notices of acting/discharge"),
    @JsonProperty("requestForFASFormsToBeChanged")
    requestForFASFormsToBeChanged("requestForFASFormsToBeChanged","Request for FAS forms to be changed"),
    @JsonProperty("witnessAvailability")
    witnessAvailability("witnessAvailability","Witness availability"),
    @JsonProperty("lettersOfComplaint")
    lettersOfComplaint("lettersOfComplaint","Letters of complaint"),
    @JsonProperty("SPIPReferralRequests")
    SPIPReferralRequests("SPIPReferralRequests","SPIP referral requests"),
    @JsonProperty("homeOfficeDWPResponses")
    homeOfficeDWPResponses("homeOfficeDWPResponses","Home Office/ DWP responses"),
    @JsonProperty("internalCorrespondence")
    internalCorrespondence("internalCorrespondence","Internal correspondence"),
    @JsonProperty("importantInfoAboutAddressAndContact")
    importantInfoAboutAddressAndContact("importantInfoAboutAddressAndContact","Important information about your address and contact details"),
    @JsonProperty("privacyNotice")
    privacyNotice("privacyNotice", "Privacy notice"),
    @JsonProperty("specialMeasures")
    specialMeasures("specialMeasures","Reasonable adjustments and special measures"),
    @JsonProperty("anyOtherDoc")
    anyOtherDoc("anyOtherDoc","Any other documents"),
    @JsonProperty("noticeOfHearing")
    noticeOfHearing("noticeOfHearing","Notice of hearing"),
    @JsonProperty("courtBundle")
    courtBundle("courtBundle","Court bundle"),
    @JsonProperty("caseSummary")
    caseSummary("caseSummary", "Case summary"),
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
