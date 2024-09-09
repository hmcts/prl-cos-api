package uk.gov.hmcts.reform.prl.enums.bundle;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum BundlingDocGroupEnum {
    @JsonProperty("applicantApplication")
    applicantApplication("applicantApplication", "applicantApplication"),

    @JsonProperty("applicantC1AApplication")
    applicantC1AApplication("applicantC1AApplication", "applicantC1AApplication"),

    @JsonProperty("applicantC1AResponse")
    applicantC1AResponse("applicantC1AResponse", "applicantC1AResponse"),

    @JsonProperty("applicantAppicationsWithinProceedings")
    applicantAppicationsWithinProceedings("applicantAppicationsWithinProceedings", "applicantAppicationsWithinProceedings"),

    @JsonProperty("applicantMiamCertificate")
    applicantMiamCertificate("applicantMiamCertificate", "applicantMiamCertificate"),

    @JsonProperty("applicantPreviousOrdersSubmittedWithApplication")
    applicantPreviousOrdersSubmittedWithApplication("applicantPreviousOrdersSubmittedWithApplication",
        "applicantPreviousOrdersSubmittedWithApplication"),

    @JsonProperty("respondentApplication")
    respondentApplication("respondentApplication", "respondentApplication"),

    @JsonProperty("respondentC1AApplication")
    respondentC1AApplication("respondentC1AApplication", "respondentC1AApplication"),

    @JsonProperty("respondentC1AResponse")
    respondentC1AResponse("respondentC1AResponse", "respondentC1AResponse"),

    @JsonProperty("respondentAppicationsFromOtherProceedings")
    respondentAppicationsFromOtherProceedings("respondentAppicationsFromOtherProceedings", "respondentAppicationsFromOtherProceedings"),

    @JsonProperty("ordersSubmittedWithApplication")
    ordersSubmittedWithApplication("ordersSubmittedWithApplication", "ordersSubmittedWithApplication"),

    @JsonProperty("approvedOrders")
    approvedOrders("approvedOrders", "approvedOrders"),

    @JsonProperty("positionStatements")
    positionStatements("positionStatements", "positionStatements"),

    @JsonProperty("applicantWitnessStatements")
    applicantWitnessStatements("applicantWitnessStatements", "applicantWitnessStatements"),

    @JsonProperty("respondentWitnessStatements")
    respondentWitnessStatements("respondentWitnessStatements", "respondentWitnessStatements"),

    @JsonProperty("applicantLettersFromSchool")
    applicantLettersFromSchool("applicantLettersFromSchool", "applicantLettersFromSchool"),

    @JsonProperty("respondentLettersFromSchool")
    respondentLettersFromSchool("respondentLettersFromSchool", "respondentLettersFromSchool"),

    @JsonProperty("otherWitnessStatements")
    otherWitnessStatements("otherWitnessStatements", "otherWitnessStatements"),

    @JsonProperty("applicantEmailsOrScreenshotsOrImagesOrOtherMediaFiles")
    applicantEmailsOrScreenshotsOrImagesOrOtherMediaFiles("applicantEmailsOrScreenshotsOrImagesOrOtherMediaFiles",
        "applicantEmailsOrScreenshotsOrImagesOrOtherMediaFiles"),

    @JsonProperty("respondentEmailsOrScreenshotsOrImagesOrOtherMediaFiles")
    respondentEmailsOrScreenshotsOrImagesOrOtherMediaFiles("respondentEmailsOrScreenshotsOrImagesOrOtherMediaFiles",
        "respondentEmailsOrScreenshotsOrImagesOrOtherMediaFiles"),

    @JsonProperty("expertMedicalReports")
    expertMedicalReports("expertMedicalReports", "expertMedicalReports"),

    @JsonProperty("expertMedicalRecords")
    expertMedicalRecords("expertMedicalRecords", "expertMedicalRecords"),

    @JsonProperty("dnaReports")
    dnaReports("dnaReports", "dnaReports"),

    @JsonProperty("reportsForDrugAndAlcoholTest")
    reportsForDrugAndAlcoholTest("reportsForDrugAndAlcoholTest", "reportsForDrugAndAlcoholTest"),

    @JsonProperty("resultsOfHairStrandBloodTests")
    resultsOfHairStrandBloodTests("resultsOfHairStrandBloodTests", "resultsOfHairStrandBloodTests"),

    @JsonProperty("policeReports")
    policeReports("policeReports", "policeReports"),

    @JsonProperty("policeDisclosures")
    policeDisclosures("policeDisclosures", "policeDisclosures"),

    @JsonProperty("expertReportsUploadedByCourtAdmin")
    expertReportsUploadedByCourtAdmin("expertReportsUploadedByCourtAdmin", "expertReportsUploadedByCourtAdmin"),

    @JsonProperty("cafcassReportsUploadedByCourtAdmin")
    cafcassReportsUploadedByCourtAdmin("cafcassReportsUploadedByCourtAdmin", "cafcassReportsUploadedByCourtAdmin"),

    @JsonProperty("applicantStatementDocsUploadedByCourtAdmin")
    applicantStatementDocsUploadedByCourtAdmin("applicantStatementDocsUploadedByCourtAdmin", "applicantStatementDocsUploadedByCourtAdmin"),

    @JsonProperty("applicantStatementSupportingEvidence")
    applicantStatementSupportingEvidence("applicantStatementSupportingEvidence", "applicantStatementSupportingEvidence"),


    @JsonProperty("c7Documents")
    c7Documents("c7Documents", "c7Documents"),

    @JsonProperty("caseSummary")
    caseSummary("caseSummary", "caseSummary"),

    @JsonProperty("transcriptsOfJudgements")
    transcriptsOfJudgements("transcriptsOfJudgements", "transcriptsOfJudgements"),

    @JsonProperty("magistratesFactsAndReasons")
    magistratesFactsAndReasons("magistratesFactsAndReasons", "magistratesFactsAndReasons"),

    @JsonProperty("safeguardingLetter")
    safeguardingLetter("safeguardingLetter", "safeguardingLetter"),

    @JsonProperty("section7Report")
    section7Report("section7Report", "section7Report"),

    @JsonProperty("cafcassSection37Report")
    cafcassSection37Report("cafcassSection37Report", "cafcassSection37Report"),

    @JsonProperty("sixteenARiskAssessment")
    sixteenARiskAssessment("sixteenARiskAssessment", "sixteenARiskAssessment"),

    @JsonProperty("guardianReport")
    guardianReport("guardianReport", "guardianReport"),

    @JsonProperty("specialGuardianshipReport")
    specialGuardianshipReport("specialGuardianshipReport", "specialGuardianshipReport"),

    @JsonProperty("cafcassOtherDocuments")
    cafcassOtherDocuments("cafcassOtherDocuments", "cafcassOtherDocuments"),

    @JsonProperty("laSection37Report")
    laSection37Report("laSection37Report", "laSection37Report"),

    @JsonProperty("laOtherDocuments")
    laOtherDocuments("laOtherDocuments", "laOtherDocuments"),

    @JsonProperty("respondentPreviousOrdersSubmittedWithApplication")
    respondentPreviousOrdersSubmittedWithApplication(
        "respondentPreviousOrdersSubmittedWithApplication",
        "respondentPreviousOrdersSubmittedWithApplication"
    ),

    @JsonProperty("anyOtherDocuments")
    anyOtherDocuments("anyOtherDocuments", "anyOtherDocuments"),

    @JsonProperty("notRequiredGroup")
    notRequiredGroup("notRequiredGroup", "notRequiredGroup");

    private final String id;
    private final String displayedValue;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonCreator
    public static BundlingDocGroupEnum getValue(String key) {
        return BundlingDocGroupEnum.valueOf(key);
    }
}
