package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class C100RebuildData {

    private String c100RebuildInternationalElements;
    private String c100RebuildReasonableAdjustments;
    private String c100RebuildTypeOfOrder;
    private String c100RebuildHearingWithoutNotice;
    private String c100RebuildHearingUrgency;
    private String c100RebuildOtherProceedings;
    private String c100RebuildReturnUrl;

    private String c100RebuildMaim;
    private String c100RebuildChildDetails;
    private String c100RebuildApplicantDetails;
    private String c100RebuildOtherChildrenDetails;
    private String c100RebuildRespondentDetails;
    private String c100RebuildOtherPersonsDetails;

    private String c100RebuildSafetyConcerns;
    private String c100RebuildScreeningQuestions;
    private String c100RebuildHelpWithFeesDetails;
    private String c100RebuildStatementOfTruth;
    private String helpWithFeesReferenceNumber;
    private String c100RebuildChildPostCode;
    private String c100RebuildConsentOrderDetails;
    private String applicantPcqId;
}
