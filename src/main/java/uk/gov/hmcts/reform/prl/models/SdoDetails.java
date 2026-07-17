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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class SdoDetails {

    @CCD(label = " ", searchable = false)
    private final List<SdoPreamblesEnum> sdoPreamblesList;
    @CCD(label = " ", searchable = false)
    private final List<SdoHearingsAndNextStepsEnum> sdoHearingsAndNextStepsList;
    @CCD(label = " ", searchable = false)
    private final List<SdoCafcassOrCymruEnum> sdoCafcassOrCymruList;
    @CCD(label = " ", searchable = false)
    private final List<SdoLocalAuthorityEnum> sdoLocalAuthorityList;
    @CCD(label = " ", searchable = false)
    private final List<SdoCourtEnum> sdoCourtList;
    @CCD(label = " ", searchable = false)
    private final List<SdoDocumentationAndEvidenceEnum> sdoDocumentationAndEvidenceList;
    @CCD(label = " ", searchable = false)
    private final List<SdoFurtherInstructionsEnum> sdoFurtherList;
    @CCD(label = " ", searchable = false)
    private final List<SdoOtherEnum> sdoOtherList;

    @CCD(label = " ", searchable = false)
    private final String sdoRightToAskCourt;
    @CCD(label = " ", searchable = false)
    private final List<Element<PartyNameDA>> sdoPartiesRaisedAbuseCollection;
    @CCD(label = " ", searchable = false)
    private final String sdoNextStepsAfterSecondGK;
    @CCD(label = " ", searchable = false)
    private final SdoNextStepsAllocationEnum sdoNextStepsAllocationTo;
    @CCD(label = " ", searchable = false)
    private final List<SdoHearingUrgentCheckListEnum> sdoHearingUrgentCheckList;
    @CCD(label = " ", searchable = false)
    private final String sdoHearingUrgentAnotherReason;
    @CCD(label = " ", searchable = false)
    private final String sdoHearingUrgentCourtConsider;
    @CCD(label = " ", searchable = false)
    private final String sdoHearingUrgentTimeShortened;
    @CCD(label = " ", searchable = false)
    private final LocalDate sdoHearingUrgentMustBeServedBy;
    @CCD(label = " ", searchable = false)
    private final String sdoHearingNotNeeded;
    @CCD(label = " ", searchable = false)
    private final String sdoParticipationDirections;
    @CCD(label = " ", searchable = false)
    private final LocalDate sdoPositionStatementDeadlineDate;
    @CCD(label = " ", searchable = false)
    private final SdoWrittenStatementEnum sdoPositionStatementWritten;
    @CCD(label = " ", searchable = false)
    private final List<Element<MiamAttendingPersonName>> sdoMiamAttendingPerson;
    @CCD(label = " ", searchable = false)
    private final String sdoJoiningInstructionsForRH;
    //Not required - start
    @CCD(label = " ", searchable = false)
    private final List<SdoApplicantRespondentEnum> sdoHearingAllegationsMadeBy;
    @CCD(label = " ", searchable = false)
    private final List<SdoCourtRequestedEnum> sdoHearingCourtHasRequested;
    //Not required - end
    @CCD(label = " ", searchable = false)
    private final HearingData sdoDirectionsForFactFindingHearingDetails;
    @CCD(label = " ", searchable = false)
    private final SdoCourtRequestedEnum sdoHearingCourtRequests;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicMultiSelectList)
    private final DynamicMultiSelectList sdoWhoMadeAllegationsList;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicMultiSelectList)
    private final DynamicMultiSelectList sdoWhoNeedsToRespondAllegationsList;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoAllegationsDeadlineDate;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoWrittenResponseDeadlineDate;
    @CCD(label = " ", searchable = false)
    private final List<SdoReportsAlsoSentToEnum> sdoHearingReportsAlsoSentTo;
    @CCD(label = " ", searchable = false)
    private final String sdoWhoMadeAllegationsText;
    @CCD(label = " ", searchable = false)
    private final String sdoWhoNeedsToRespondAllegationsText;
    @CCD(label = " ", searchable = false)
    private final String sdoHearingMaximumPages;
    @CCD(label = " ", searchable = false)
    private final int sdoHearingHowManyWitnessEvidence;
    @CCD(label = " ", searchable = false)
    private final List<FactFindingOtherDirectionEnum> sdoFactFindingOtherCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoFactFindingOtherDetails;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoLanguageDialect>> sdoInterpreterDialectRequired;
    @CCD(label = " ", searchable = false)
    private final String sdoUpdateContactDetails;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoCafcassFileAndServe;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String sdoCafcassNextStepEditContent;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoCafcassCymruFileAndServe;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String sdoCafcassCymruNextStepEditContent;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoNewPartnersToCafcass;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoNewPartnersToCafcassCymru;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String sdoSection7EditContent;
    @CCD(label = " ", searchable = false)
    private final List<SdoSection7ImpactAnalysisEnum> sdoSection7ImpactAnalysisOptions;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String sdoSection7FactsEditContent;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String sdoSection7daOccuredEditContent;
    @CCD(label = " ", searchable = false)
    private final SdoReportSentByEnum sdoSection7ChildImpactAnalysis;
    @CCD(label = " ", searchable = false)
    private final String sdoNameOfCouncil;
    @CCD(label = " ", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoCafcassCymruReportSentByDate;
    @CCD(label = " ", searchable = false)
    private final String sdoLocalAuthorityName;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String sdoLocalAuthorityTextArea;
    @CCD(label = " ", searchable = false)
    private final LocalDate sdoLocalAuthorityReportSubmitByDate;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicList)
    private final DynamicList sdoTransferApplicationCourtDynamicList;
    @CCD(label = " ", searchable = false)
    private final List<SdoTransferApplicationReasonEnum> sdoTransferApplicationReason;
    @CCD(label = " ", searchable = false)
    private final String sdoTransferApplicationSpecificReason;
    @CCD(label = " ", searchable = false)
    private final String sdoCrossExaminationCourtHavingHeard;
    @CCD(label = " ", searchable = false)
    private final String sdoCrossExaminationEx740;
    @CCD(label = " ", searchable = false)
    private final String sdoCrossExaminationEx741;
    @CCD(label = " ", searchable = false)
    private final String sdoCrossExaminationQualifiedLegal;
    @CCD(label = " ", searchable = false)
    private final LocalDate sdoWitnessStatementsDeadlineDate;
    @CCD(label = " ", searchable = false)
    private final SdoWitnessStatementsSentToEnum sdoWitnessStatementsSentTo;
    @CCD(label = " ", searchable = false)
    private final SdoReportsSentToEnum sdoWitnessStatementsCopiesSentTo;
    @CCD(label = " ", searchable = false)
    private final String sdoWitnessStatementsMaximumPages;
    @CCD(label = " ", searchable = false)
    private final String sdoSpecifiedDocuments;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicList)
    private final DynamicList sdoInstructionsFilingPartiesDynamicList;
    @CCD(label = " ", searchable = false)
    private final String sdoSpipAttendance;

    @CCD(label = " ", searchable = false)
    private final LocalDate sdoHospitalRecordsDeadlineDate;
    @CCD(label = " ", searchable = false)
    private final List<SdoApplicantRespondentEnum> sdoMedicalDisclosureUploadedBy;
    @CCD(label = " ", searchable = false)
    private final LocalDate sdoLetterFromGpDeadlineDate;
    @CCD(label = " ", searchable = false)
    private final List<SdoApplicantRespondentEnum> sdoLetterFromGpUploadedBy;
    @CCD(label = " ", searchable = false)
    private final LocalDate sdoLetterFromSchoolDeadlineDate;
    @CCD(label = " ", searchable = false)
    private final List<SdoApplicantRespondentEnum> sdoLetterFromSchoolUploadedBy;
    @CCD(label = " ", searchable = false)
    private final List<SdoScheduleOfAllegationsEnum> sdoScheduleOfAllegationsOption;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDisclosureOfPapersCaseNumber>> sdoDisclosureOfPapersCaseNumbers;
    @CCD(label = " ", searchable = false)
    private final String sdoParentWithCare;

    @CCD(label = " ", searchable = false)
    private final String sdoPermissionHearingDirections;
    @CCD(label = " ", searchable = false)
    private final HearingData sdoPermissionHearingDetails;
    @CCD(label = " ", searchable = false)
    private final HearingData sdoSecondHearingDetails;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.JudicialUser)
    private final JudicialUser sdoNextStepJudgeName;
    @CCD(label = " ", searchable = false)
    private final AllocateOrReserveJudgeEnum sdoAllocateOrReserveJudge;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.JudicialUser)
    private final JudicialUser sdoAllocateOrReserveJudgeName;
    @CCD(label = " ", searchable = false)
    private final HearingData sdoUrgentHearingDetails;
    @CCD(label = " ", searchable = false)
    private final HearingData sdoFhdraHearingDetails;
    @CCD(label = " ", searchable = false)
    private final List<OtherDirectionPositionStatementEnum> sdoPositionStatementOtherCheckDetails;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoPositionStatementOtherDetails;
    @CCD(label = " ", searchable = false)
    private final List<MiamOtherDirectionEnum> sdoMiamOtherCheckDetails;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoMiamOtherDetails;
    @CCD(label = " ", searchable = false)
    private final HearingData sdoDraHearingDetails;
    @CCD(label = " ", searchable = false)
    private final HearingData sdoSettlementHearingDetails;
    @CCD(label = " ", searchable = false)
    private final List<DioOtherDirectionEnum> sdoInterpreterOtherDetailsCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoInterpreterOtherDetails;
    @CCD(label = " ", searchable = false)
    private final List<SdoCafcassFileAndServeCheckEnum> sdoCafcassFileAndServeCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoCafcassFileAndServeDetails;
    @CCD(label = " ", searchable = false)
    private final List<SdoSafeguardingCafcassCymruEnum> safeguardingCafcassCymruCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> safeguardingCafcassCymruDetails;
    @CCD(label = " ", searchable = false)
    private final List<SdoPartyToProvideDetailsEnum> sdoPartyToProvideDetailsCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoPartyToProvideDetails;
    @CCD(label = " ", searchable = false)
    private final List<SdoNewPartnersToCafcassEnum> sdoNewPartnersToCafcassCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoNewPartnersToCafcassDetails;
    @CCD(label = " ", searchable = false)
    private final List<SdoSection7CheckEnum> sdoSection7Check;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoSection7CheckDetails;
    @CCD(label = " ", searchable = false)
    private final List<SdoLocalAuthorityCheckEnum> sdoLocalAuthorityCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoLocalAuthorityDetails;
    @CCD(label = " ", searchable = false)
    private final List<DioTransferCourtDirectionEnum> sdoTransferCourtDetailsCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoTransferCourtDetails;
    @CCD(label = " ", searchable = false)
    private final List<SdoCrossExaminationCourtCheckEnum> sdoCrossExaminationCourtCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoCrossExaminationCourtDetails;
    @CCD(label = " ", searchable = false)
    private final List<SdoWitnessStatementsCheckEnum> sdoWitnessStatementsCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoWitnessStatementsCheckDetails;
    @CCD(label = " ", searchable = false)
    private final List<SdoInstructionsFilingPartiesCheck> sdoInstructionsFilingCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoInstructionsFilingDetails;

    @CCD(label = " ", searchable = false)
    private final List<Element<SdoNameOfApplicant>> sdoMedicalDiscApplicantName;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoNameOfRespondent>> sdoMedicalDiscRespondentName;
    @CCD(label = " ", searchable = false)
    private final List<SdoMedicalDiscCheckEnum> sdoMedicalDiscFilingCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoMedicalDiscFilingDetails;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoNameOfApplicant>> sdoGpApplicantName;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoNameOfRespondent>> sdoGpRespondentName;
    @CCD(label = " ", searchable = false)
    private final List<SdoLetterFromDiscGpCheckEnum> sdoLetterFromDiscGpCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoLetterFromGpDetails;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoNameOfApplicant>> sdoLsApplicantName;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoNameOfRespondent>> sdoLsRespondentName;
    @CCD(label = " ", searchable = false)
    private final List<SdoLetterFromSchoolCheck> sdoLetterFromSchoolCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoLetterFromSchoolDetails;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoScheduleOfAllegationsDetails;
    @CCD(label = " ", searchable = false)
    private final List<SdoDisClosureProceedingCheck> sdoDisClosureProceedingCheck;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoDioProvideOtherDetails>> sdoDisClosureProceedingDetails;
    @CCD(label = " ", searchable = false)
    private final List<Element<SdoFurtherDirections>> sdoFurtherDirectionDetails;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String sdoCrossExaminationEditContent;
    @CCD(label = " ", searchable = false)
    private final int sdoDocsEvidenceWitnessEvidence;

    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String sdoAfterSecondGatekeeping;
    @CCD(label = " ", searchable = false)
    private final List<Element<AddNewPreamble>> sdoAddNewPreambleCollection;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.TextArea)
    private final String sdoNextStepsAfterGatekeeping;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicMultiSelectList)
    private final DynamicMultiSelectList sdoNewPartnerPartiesCafcass;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.DynamicMultiSelectList)
    private final DynamicMultiSelectList sdoNewPartnerPartiesCafcassCymru;
}
