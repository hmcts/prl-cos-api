package uk.gov.hmcts.reform.prl.models.dto.citizen;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.CustomEnumSerializer;

@Getter
@RequiredArgsConstructor
@JsonSerialize(using = CustomEnumSerializer.class)
public enum DocumentCategory {

    POSITION_STATEMENTS("POSITION_STATEMENTS", "positionStatements", "Position statements", "position_statements"),
    WITNESS_STATEMENTS_APPLICANT("WITNESS_STATEMENTS_APPLICANT", "applicantStatements", "Witness statements", "witness_statements"),
    WITNESS_STATEMENTS_RESPONDENT("WITNESS_STATEMENTS_RESPONDENT", "respondentStatements", "Witness statements", "witness_statements"),
    OTHER_WITNESS_STATEMENTS("OTHER_WITNESS_STATEMENTS", "otherWitnessStatements", "Other witness Statements", "other_witness_statements"),
    MEDICAL_RECORDS("MEDICAL_RECORDS", "medicalRecords", "Medical Records", "medical_records"),
    MEDICAL_REPORTS("MEDICAL_REPORTS", "medicalReports", "Medical reports", "medical_reports"),
    MAIL_SCREENSHOTS_MEDIA_FILES("MAIL_SCREENSHOTS_MEDIA_FILES",
                                 Constants.ANY_OTHER_DOC, Constants.ANY_OTHER_DOCUMENTS, "media_files"),
    LETTERS_FROM_SCHOOL("LETTERS_FROM_SCHOOL",
                        Constants.ANY_OTHER_DOC, Constants.ANY_OTHER_DOCUMENTS, "letter_from_school"),
    TENANCY_MORTGAGE_AGREEMENTS("TENANCY_MORTGAGE_AGREEMENTS",
                                Constants.ANY_OTHER_DOC, Constants.ANY_OTHER_DOCUMENTS, "tenancy_mortgage_agreements"),
    PREVIOUS_ORDERS_SUBMITTED_APPLICANT("PREVIOUS_ORDERS_SUBMITTED_APPLICANT",
                                        "previousOrdersSubmittedWithApplication",
                                        "Prev orders submitted with application", "previous_orders_submitted"),
    PREVIOUS_ORDERS_SUBMITTED_RESPONDENT("PREVIOUS_ORDERS_SUBMITTED_RESPONDENT",
                                         "applicationsFromOtherProceedings",
                                         "Applications from other proceedings", "previous_orders_submitted"),
    PATERNITY_TEST_REPORTS("PATERNITY_TEST_REPORTS", "DNAReports_expertReport", "DNA reports", "paternity_test_reports"),
    DRUG_AND_ALCOHOL_TESTS("DRUG_AND_ALCOHOL_TESTS",
                           "drugAndAlcoholTest(toxicology)", "Drug and alcohol test (toxicology)", "drug_and_alcohol_tests"),
    POLICE_REPORTS("POLICE_REPORTS", "policeReport", "Police report", "police_reports"),
    OTHER_DOCUMENTS("OTHER_DOCUMENTS", "otherDocments", "Other Documents", "other_documents"),
    FM5_STATEMENTS("FM5_STATEMENTS", "fm5Statements", "FM5 Statements", "fm5_statements");

    private final String id;
    private final String categoryId;
    private final String displayedValue;
    private final String fileNamePrefix;

    @JsonValue
    public String getDisplayedValue() {
        return displayedValue;
    }

    @JsonValue
    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    @JsonCreator
    public static DocumentCategory getValue(String id) {
        return DocumentCategory.valueOf(id);
    }

    private static class Constants {
        public static final String ANY_OTHER_DOC = "anyOtherDoc";
        public static final String ANY_OTHER_DOCUMENTS = "Any Other Documents";
    }
}
