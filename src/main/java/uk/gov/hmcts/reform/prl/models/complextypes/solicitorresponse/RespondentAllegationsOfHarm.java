package uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class RespondentAllegationsOfHarm {

    private final YesOrNo respondentDomesticAbuse;
    private final YesOrNo respondentChildAbuse;
    private final YesOrNo isRespondentChildAbduction;
    private final YesOrNo respondentDrugOrAlcoholAbuse;
    private final String respondentDrugOrAlcoholAbuseDetails;
    private final YesOrNo respondentOtherSafetyConcerns;
    private final String respondentOtherSafetyConcernsDetails;
    private final YesOrNo respondentNonMolestationOrder;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentNonMolestationOrderIssueDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentNonMolestationOrderEndDate;
    private final YesOrNo respondentNonMolestationOrderIsCurrent;
    private final String respondentNonMolestationOrderCourt;
    private final String respondentNonMolestationOrderCaseNumber;
    private final Document respondentNonMolestationOrderDocument;
    private final YesOrNo respondentOccupationOrder;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentOccupationOrderIssueDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentOccupationOrderEndDate;
    private final YesOrNo respondentOccupationOrderIsCurrent;
    private final String respondentOccupationOrderCourt;
    private final String respondentOccupationOrderCaseNumber;
    private final Document respondentOccupationOrderDocument;

    private final YesOrNo respondentForcedMarriageOrder;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentForcedMarriageIssueDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentForcedMarriageEndDate;
    private final YesOrNo respondentForcedMarriageIsCurrent;
    private final String respondentForcedMarriageCourt;
    private final String respondentForcedMarriageCaseNumber;
    private final Document respondentForcedMarriageDocument;

    private final YesOrNo respondentRestrainingOrder;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentRestrainingIssueDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentRestrainingEndDate;
    private final YesOrNo respondentRestrainingIsCurrent;
    private final String respondentRestrainingCourt;
    private final String respondentRestrainingCaseNumber;
    private final Document respondentRestrainingDocument;

    private final YesOrNo respondentOtherInjunctiveOrder;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentOtherInjunctiveIssueDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentOtherInjunctiveEndDate;
    private final YesOrNo respondentOtherInjunctiveIsCurrent;
    private final String respondentOtherInjunctiveCourt;
    private final String respondentOtherInjunctiveCaseNumber;
    private final Document respondentOtherInjunctiveDocument;

    private final YesOrNo respondentUndertakingOrder;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentUndertakingIssueDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentUndertakingEndDate;
    private final YesOrNo respondentUndertakingIsCurrent;
    private final String respondentUndertakingCourt;
    private final String respondentUndertakingCaseNumber;
    private final Document respondentUndertakingDocument;


}
