package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.FactFindingOtherDirectionEnum;
import uk.gov.hmcts.reform.prl.enums.SdoCafcassFileAndServeCheckEnum;
import uk.gov.hmcts.reform.prl.enums.SdoCrossExaminationCourtCheckEnum;
import uk.gov.hmcts.reform.prl.enums.SdoDisClosureProceedingCheck;
import uk.gov.hmcts.reform.prl.enums.SdoInstructionsFilingPartiesCheck;
import uk.gov.hmcts.reform.prl.enums.SdoLetterFromDiscGpCheckEnum;
import uk.gov.hmcts.reform.prl.enums.SdoLetterFromSchoolCheck;
import uk.gov.hmcts.reform.prl.enums.SdoLocalAuthorityCheckEnum;
import uk.gov.hmcts.reform.prl.enums.SdoMedicalDiscCheckEnum;
import uk.gov.hmcts.reform.prl.enums.SdoNewPartnersToCafcassEnum;
import uk.gov.hmcts.reform.prl.enums.SdoPartyToProvideDetailsEnum;
import uk.gov.hmcts.reform.prl.enums.SdoSafeguardingCafcassCymruEnum;
import uk.gov.hmcts.reform.prl.enums.SdoSection7CheckEnum;
import uk.gov.hmcts.reform.prl.enums.SdoWitnessStatementsCheckEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioOtherDirectionEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioTransferCourtDirectionEnum;
import uk.gov.hmcts.reform.prl.enums.dio.MiamOtherDirectionEnum;
import uk.gov.hmcts.reform.prl.enums.dio.OtherDirectionPositionStatementEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.AllocateOrReserveJudgeEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoApplicantRespondentEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtRequestedEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoFurtherInstructionsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingUrgentCheckListEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoNextStepsAllocationEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoOtherEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoReportSentByEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoReportsAlsoSentToEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoReportsSentToEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoScheduleOfAllegationsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoSection7ImpactAnalysisEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoTransferApplicationReasonEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoWitnessStatementsSentToEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoWrittenStatementEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.MiamAttendingPersonName;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio.SdoDioProvideOtherDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.AddNewPreamble;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.PartyNameDA;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.SdoDisclosureOfPapersCaseNumber;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.SdoLanguageDialect;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.SdoFurtherDirections;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.SdoNameOfApplicant;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.SdoNameOfRespondent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class SdoDetails {

    private final List<SdoPreamblesEnum> sdoPreamblesList;
    private final List<SdoPreamblesEnum> sdoPreamblesTempList;
    private final List<SdoHearingsAndNextStepsEnum> sdoHearingsAndNextStepsList;
    private final List<SdoHearingsAndNextStepsEnum> sdoHearingsAndNextStepsTempList;
    private final List<SdoCafcassOrCymruEnum> sdoCafcassOrCymruList;
    private final List<SdoCafcassOrCymruEnum> sdoCafcassOrCymruTempList;
    private final List<SdoLocalAuthorityEnum> sdoLocalAuthorityList;
    private final List<SdoLocalAuthorityEnum> sdoLocalAuthorityTempList;
    private final List<SdoCourtEnum> sdoCourtList;
    private final List<SdoCourtEnum> sdoCourtTempList;
    private final List<SdoDocumentationAndEvidenceEnum> sdoDocumentationAndEvidenceList;
    private final List<SdoDocumentationAndEvidenceEnum> sdoDocumentationAndEvidenceTempList;
    private final List<SdoFurtherInstructionsEnum> sdoFurtherList;
    private final List<SdoOtherEnum> sdoOtherList;
    private final List<SdoOtherEnum> sdoOtherTempList;

    private final String sdoRightToAskCourt;
    private final List<Element<PartyNameDA>> sdoPartiesRaisedAbuseCollection;
    private final String sdoNextStepsAfterSecondGK;
    private final SdoNextStepsAllocationEnum sdoNextStepsAllocationTo;
    private final List<SdoHearingUrgentCheckListEnum> sdoHearingUrgentCheckList;
    private final String sdoHearingUrgentAnotherReason;
    private final String sdoHearingUrgentCourtConsider;
    private final String sdoHearingUrgentTimeShortened;
    private final LocalDate sdoHearingUrgentMustBeServedBy;
    private final String sdoHearingNotNeeded;
    private final String sdoParticipationDirections;
    private final LocalDate sdoPositionStatementDeadlineDate;
    private final SdoWrittenStatementEnum sdoPositionStatementWritten;
    private final List<Element<MiamAttendingPersonName>> sdoMiamAttendingPerson;
    private final String sdoJoiningInstructionsForRH;
    //Not required - start
    private final List<SdoApplicantRespondentEnum> sdoHearingAllegationsMadeBy;
    private final List<SdoCourtRequestedEnum> sdoHearingCourtHasRequested;
    //Not required - end
    private final HearingData sdoDirectionsForFactFindingHearingDetails;
    private final SdoCourtRequestedEnum sdoHearingCourtRequests;
    private final DynamicMultiSelectList sdoWhoMadeAllegationsList;
    private final DynamicMultiSelectList sdoWhoNeedsToRespondAllegationsList;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoAllegationsDeadlineDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoWrittenResponseDeadlineDate;
    private final List<SdoReportsAlsoSentToEnum> sdoHearingReportsAlsoSentTo;
    private final String sdoWhoMadeAllegationsText;
    private final String sdoWhoNeedsToRespondAllegationsText;
    private final String sdoHearingMaximumPages;
    private final int sdoHearingHowManyWitnessEvidence;
    private final List<FactFindingOtherDirectionEnum> sdoFactFindingOtherCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoFactFindingOtherDetails;
    private final List<Element<SdoLanguageDialect>> sdoInterpreterDialectRequired;
    private final String sdoUpdateContactDetails;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoCafcassFileAndServe;
    private final String sdoCafcassNextStepEditContent;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoCafcassCymruFileAndServe;
    private final String sdoCafcassCymruNextStepEditContent;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoNewPartnersToCafcass;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoNewPartnersToCafcassCymru;
    private final String sdoSection7EditContent;
    private final List<SdoSection7ImpactAnalysisEnum> sdoSection7ImpactAnalysisOptions;
    private final String sdoSection7FactsEditContent;
    private final String sdoSection7daOccuredEditContent;
    private final SdoReportSentByEnum sdoSection7ChildImpactAnalysis;
    private final String sdoNameOfCouncil;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoCafcassCymruReportSentByDate;
    private final String sdoLocalAuthorityName;
    private final String sdoLocalAuthorityTextArea;
    private final LocalDate sdoLocalAuthorityReportSubmitByDate;
    private final DynamicList sdoTransferApplicationCourtDynamicList;
    private final List<SdoTransferApplicationReasonEnum> sdoTransferApplicationReason;
    private final String sdoTransferApplicationSpecificReason;
    private final String sdoCrossExaminationCourtHavingHeard;
    private final String sdoCrossExaminationEx740;
    private final String sdoCrossExaminationEx741;
    private final String sdoCrossExaminationQualifiedLegal;
    private final LocalDate sdoWitnessStatementsDeadlineDate;
    private final SdoWitnessStatementsSentToEnum sdoWitnessStatementsSentTo;
    private final SdoReportsSentToEnum sdoWitnessStatementsCopiesSentTo;
    private final String sdoWitnessStatementsMaximumPages;
    private final String sdoSpecifiedDocuments;
    private final DynamicList sdoInstructionsFilingPartiesDynamicList;
    private final String sdoSpipAttendance;

    private final LocalDate sdoHospitalRecordsDeadlineDate;
    private final List<SdoApplicantRespondentEnum> sdoMedicalDisclosureUploadedBy;
    private final LocalDate sdoLetterFromGpDeadlineDate;
    private final List<SdoApplicantRespondentEnum> sdoLetterFromGpUploadedBy;
    private final LocalDate sdoLetterFromSchoolDeadlineDate;
    private final List<SdoApplicantRespondentEnum> sdoLetterFromSchoolUploadedBy;
    private final List<SdoScheduleOfAllegationsEnum> sdoScheduleOfAllegationsOption;
    private final List<Element<SdoDisclosureOfPapersCaseNumber>> sdoDisclosureOfPapersCaseNumbers;
    private final String sdoParentWithCare;

    private final String sdoPermissionHearingDirections;
    private final HearingData sdoPermissionHearingDetails;
    private final HearingData sdoSecondHearingDetails;
    private final JudicialUser sdoNextStepJudgeName;
    private final AllocateOrReserveJudgeEnum sdoAllocateOrReserveJudge;
    private final JudicialUser sdoAllocateOrReserveJudgeName;
    private final HearingData sdoUrgentHearingDetails;
    private final HearingData sdoFhdraHearingDetails;
    private final List<OtherDirectionPositionStatementEnum> sdoPositionStatementOtherCheckDetails;
    private final List<Element<SdoDioProvideOtherDetails>> sdoPositionStatementOtherDetails;
    private final List<MiamOtherDirectionEnum> sdoMiamOtherCheckDetails;
    private final List<Element<SdoDioProvideOtherDetails>> sdoMiamOtherDetails;
    private final HearingData sdoDraHearingDetails;
    private final HearingData sdoSettlementHearingDetails;
    private final List<DioOtherDirectionEnum> sdoInterpreterOtherDetailsCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoInterpreterOtherDetails;
    private final List<SdoCafcassFileAndServeCheckEnum> sdoCafcassFileAndServeCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoCafcassFileAndServeDetails;
    private final List<SdoSafeguardingCafcassCymruEnum> safeguardingCafcassCymruCheck;
    private final List<Element<SdoDioProvideOtherDetails>> safeguardingCafcassCymruDetails;
    private final List<SdoPartyToProvideDetailsEnum> sdoPartyToProvideDetailsCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoPartyToProvideDetails;
    private final List<SdoNewPartnersToCafcassEnum> sdoNewPartnersToCafcassCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoNewPartnersToCafcassDetails;
    private final List<SdoSection7CheckEnum> sdoSection7Check;
    private final List<Element<SdoDioProvideOtherDetails>> sdoSection7CheckDetails;
    private final List<SdoLocalAuthorityCheckEnum> sdoLocalAuthorityCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoLocalAuthorityDetails;
    private final List<DioTransferCourtDirectionEnum> sdoTransferCourtDetailsCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoTransferCourtDetails;
    private final List<SdoCrossExaminationCourtCheckEnum> sdoCrossExaminationCourtCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoCrossExaminationCourtDetails;
    private final List<SdoWitnessStatementsCheckEnum> sdoWitnessStatementsCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoWitnessStatementsCheckDetails;
    private final List<SdoInstructionsFilingPartiesCheck> sdoInstructionsFilingCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoInstructionsFilingDetails;

    private final List<Element<SdoNameOfApplicant>> sdoMedicalDiscApplicantName;
    private final List<Element<SdoNameOfRespondent>> sdoMedicalDiscRespondentName;
    private final List<SdoMedicalDiscCheckEnum> sdoMedicalDiscFilingCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoMedicalDiscFilingDetails;
    private final List<Element<SdoNameOfApplicant>> sdoGpApplicantName;
    private final List<Element<SdoNameOfRespondent>> sdoGpRespondentName;
    private final List<SdoLetterFromDiscGpCheckEnum> sdoLetterFromDiscGpCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoLetterFromGpDetails;
    private final List<Element<SdoNameOfApplicant>> sdoLsApplicantName;
    private final List<Element<SdoNameOfRespondent>> sdoLsRespondentName;
    private final List<SdoLetterFromSchoolCheck> sdoLetterFromSchoolCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoLetterFromSchoolDetails;
    private final List<Element<SdoDioProvideOtherDetails>> sdoScheduleOfAllegationsDetails;
    private final List<SdoDisClosureProceedingCheck> sdoDisClosureProceedingCheck;
    private final List<Element<SdoDioProvideOtherDetails>> sdoDisClosureProceedingDetails;
    private final List<Element<SdoFurtherDirections>> sdoFurtherDirectionDetails;

    private final String sdoCrossExaminationEditContent;
    private final int sdoDocsEvidenceWitnessEvidence;

    private final String sdoAfterSecondGatekeeping;
    private final List<Element<AddNewPreamble>> sdoAddNewPreambleCollection;
    private final String sdoNextStepsAfterGatekeeping;
    private final DynamicMultiSelectList sdoNewPartnerPartiesCafcass;
    private final DynamicMultiSelectList sdoNewPartnerPartiesCafcassCymru;
}
