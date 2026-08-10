package uk.gov.hmcts.reform.prl.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@RequiredArgsConstructor
@Getter
@JsonSerialize(using = CustomEnumSerializer.class)
public enum State {

    @CCD(
            label = "Draft",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}"
    )
    AWAITING_SUBMISSION_TO_HMCTS("AWAITING_SUBMISSION_TO_HMCTS", "Draft"),
    AWAITING_FL401_SUBMISSION_TO_HMCTS("AWAITING_FL401_SUBMISSION_TO_HMCTS", "Draft"),
    @CCD(
            label = "Pending",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}",
            description = "C100 application payment pending"
    )
    SUBMITTED_NOT_PAID("SUBMITTED_NOT_PAID", "Pending"),
    @CCD(
            label = "Submitted",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}",
            description = "C100 application payment successful"
    )
    SUBMITTED_PAID("SUBMITTED_PAID", "Submitted"),
    @CCD(
            label = "Returned",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}",
            description = "C100 application returned"
    )
    AWAITING_RESUBMISSION_TO_HMCTS("AWAITING_RESUBMISSION_TO_HMCTS", "Returned"),
    @CCD(
            label = "Case Issued",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}",
            description = "C100 Case Issue"
    )
    CASE_ISSUED("CASE_ISSUED", "Case Issued"),
    @CCD(
            label = "Withdrawn",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}"
    )
    CASE_WITHDRAWN("CASE_WITHDRAWN", "Withdrawn"),
    @CCD(
            label = "Gatekeeping",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}"
    )
    JUDICIAL_REVIEW("JUDICIAL_REVIEW", "Gatekeeping"),
    @CCD(
            label = "Closed",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}",
            description = "All final orders issued"
    )
    ALL_FINAL_ORDERS_ISSUED("ALL_FINAL_ORDERS_ISSUED", "Closed"),
    @CCD(
            label = "Hearing",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}",
            description = "Prepare for hearing"
    )
    PREPARE_FOR_HEARING_CONDUCT_HEARING("PREPARE_FOR_HEARING_CONDUCT_HEARING","Hearing"),
    @CCD(label = "Deleted", description = "Deleted application")
    DELETED("DELETED", "Deleted"),
    @CCD(
            label = "Requested for deletion",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}"
    )
    REQUESTED_FOR_DELETION("REQUESTED_FOR_DELETION", "Requested for deletion"),
    @CCD(
            label = "Ready for deletion",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}"
    )
    READY_FOR_DELETION("READY_FOR_DELETION", "Ready for deletion"),
    @CCD(
            label = "Hearing Outcome",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}",
            description = "Hearing outcome"
    )
    DECISION_OUTCOME("DECISION_OUTCOME","Hearing Outcome"),
    @CCD(
            label = "Proceeding in offline mode in familyman system",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}"
    )
    PROCEEDS_IN_HERITAGE_SYSTEM("PROCEEDS_IN_HERITAGE_SYSTEM",
                                "Proceeding in offline mode in familyman system"),
    @CCD(
            label = "Awaiting Information",
            hint = "## ${applicantCaseName}\n ## FamilyMan ID: ${familymanCaseNumber}\n ## Casenumber: ${[CASE_REFERENCE]}",
            description = "Move Case to Awaiting Information (Manual Event) — CTSC / HCA Admin Workflow"
    )
    AWAITING_INFORMATION("AWAITING_INFORMATION",
                                    "Awaiting information"),
    EXIT_AWAITING_INFORMATION("EXIT_AWAITING_INFORMATION",
                             "Exit Awaiting information");

    private final String value;
    private final String label;

    State(String value) {
        this.value = value;
        this.label = value;
    }

    public static State fromValue(final String value) {
        return tryFromValue(value)
            .orElseThrow(() -> new NoSuchElementException("Unable to map " + value + " to a case state"));
    }

    public static Optional<State> tryFromValue(final String value) {
        return Stream.of(values())
            .filter(state -> state.value.equalsIgnoreCase(value))
            .findFirst();
    }

    @JsonValue
    public String getLabel() {
        return label;
    }

    @JsonCreator
    public static State getValue(String key) {
        return State.valueOf(key);
    }

}
