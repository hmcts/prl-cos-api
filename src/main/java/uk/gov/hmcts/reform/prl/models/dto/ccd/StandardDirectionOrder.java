package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
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
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;
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

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StandardDirectionOrder implements MappableObject {

    @JsonProperty("sdoPreamblesList")
    private final List<SdoPreamblesEnum> sdoPreamblesList;
    @JsonProperty("sdoPreamblesTempList")
    private final List<SdoPreamblesEnum> sdoPreamblesTempList;
    @JsonProperty("sdoHearingsAndNextStepsList")
    private final List<SdoHearingsAndNextStepsEnum> sdoHearingsAndNextStepsList;
    @JsonProperty("sdoHearingsAndNextStepsTempList")
    private final List<SdoHearingsAndNextStepsEnum> sdoHearingsAndNextStepsTempList;
    @JsonProperty("sdoCafcassOrCymruList")
    private final List<SdoCafcassOrCymruEnum> sdoCafcassOrCymruList;
    @JsonProperty("sdoCafcassOrCymruTempList")
    private final List<SdoCafcassOrCymruEnum> sdoCafcassOrCymruTempList;
    @JsonProperty("sdoLocalAuthorityList")
    private final List<SdoLocalAuthorityEnum> sdoLocalAuthorityList;
    @JsonProperty("sdoLocalAuthorityTempList")
    private final List<SdoLocalAuthorityEnum> sdoLocalAuthorityTempList;
    @JsonProperty("sdoCourtList")
    private final List<SdoCourtEnum> sdoCourtList;
    @JsonProperty("sdoCourtTempList")
    private final List<SdoCourtEnum> sdoCourtTempList;
    @JsonProperty("sdoDocumentationAndEvidenceList")
    private final List<SdoDocumentationAndEvidenceEnum> sdoDocumentationAndEvidenceList;
    @JsonProperty("sdoDocumentationAndEvidenceTempList")
    private final List<SdoDocumentationAndEvidenceEnum> sdoDocumentationAndEvidenceTempList;
    @JsonProperty("sdoFurtherList")
    private final List<SdoFurtherInstructionsEnum> sdoFurtherList;
    @JsonProperty("sdoOtherList")
    private final List<SdoOtherEnum> sdoOtherList;
    @JsonProperty("sdoOtherTempList")
    private final List<SdoOtherEnum> sdoOtherTempList;

    @JsonProperty("sdoRightToAskCourt")
    private final String sdoRightToAskCourt;
    private final List<Element<PartyNameDA>> sdoPartiesRaisedAbuseCollection;
    @JsonProperty("sdoNextStepsAfterSecondGK")
    private final String sdoNextStepsAfterSecondGK;
    @JsonProperty("sdoNextStepsAllocationTo")
    private final SdoNextStepsAllocationEnum sdoNextStepsAllocationTo;
    @JsonProperty("sdoHearingUrgentCheckList")
    private final List<SdoHearingUrgentCheckListEnum> sdoHearingUrgentCheckList;
    @JsonProperty("sdoHearingUrgentAnotherReason")
    private final String sdoHearingUrgentAnotherReason;
    @JsonProperty("sdoHearingUrgentCourtConsider")
    private final String sdoHearingUrgentCourtConsider;
    @JsonProperty("sdoHearingUrgentTimeShortened")
    private final String sdoHearingUrgentTimeShortened;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoHearingUrgentMustBeServedBy;
    @JsonProperty("sdoHearingNotNeeded")
    private final String sdoHearingNotNeeded;
    @JsonProperty("sdoParticipationDirections")
    private final String sdoParticipationDirections;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoPositionStatementDeadlineDate;
    @JsonProperty("sdoPositionStatementWritten")
    private final SdoWrittenStatementEnum sdoPositionStatementWritten;
    private final List<Element<MiamAttendingPersonName>> sdoMiamAttendingPerson;
    @JsonProperty("sdoJoiningInstructionsForRH")
    private final String sdoJoiningInstructionsForRH;
    //Not required - start
    @JsonProperty("sdoHearingAllegationsMadeBy")
    private final List<SdoApplicantRespondentEnum> sdoHearingAllegationsMadeBy;
    @JsonProperty("sdoHearingCourtHasRequested")
    private final List<SdoCourtRequestedEnum> sdoHearingCourtHasRequested;
    //Not required - end
    @JsonProperty("sdoDirectionsForFactFindingHearingDetails")
    private final HearingData sdoDirectionsForFactFindingHearingDetails;
    @JsonProperty("sdoHearingCourtRequests")
    private final SdoCourtRequestedEnum sdoHearingCourtRequests;
    @JsonProperty("sdoWhoMadeAllegationsList")
    private final DynamicMultiSelectList sdoWhoMadeAllegationsList;
    @JsonProperty("sdoWhoNeedsToRespondAllegationsList")
    private final DynamicMultiSelectList sdoWhoNeedsToRespondAllegationsList;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoAllegationsDeadlineDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoWrittenResponseDeadlineDate;
    @JsonProperty("sdoHearingReportsAlsoSentTo")
    private final List<SdoReportsAlsoSentToEnum> sdoHearingReportsAlsoSentTo;
    @JsonProperty("sdoWhoMadeAllegationsText")
    private final String sdoWhoMadeAllegationsText;
    @JsonProperty("sdoWhoNeedsToRespondAllegationsText")
    private final String sdoWhoNeedsToRespondAllegationsText;
    @JsonProperty("sdoHearingMaximumPages")
    private final String sdoHearingMaximumPages;
    @JsonProperty("sdoHearingHowManyWitnessEvidence")
    private final int sdoHearingHowManyWitnessEvidence;
    @JsonProperty("sdoFactFindingOtherCheck")
    private final List<FactFindingOtherDirectionEnum> sdoFactFindingOtherCheck;
    @JsonProperty("sdoFactFindingOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoFactFindingOtherDetails;
    private final String sdoWhoNeedsToRespondAllegationsListText;
    private final String sdoWhoMadeAllegationsListText;
    @JsonProperty("sdoDocsEvidenceWitnessEvidence")
    private final int sdoDocsEvidenceWitnessEvidence;
    private final List<Element<SdoLanguageDialect>> sdoInterpreterDialectRequired;
    @JsonProperty("sdoUpdateContactDetails")
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

    @JsonProperty("sdoSection7EditContent")
    private final String sdoSection7EditContent;
    @JsonProperty("sdoSection7ImpactAnalysisOptions")
    private final List<SdoSection7ImpactAnalysisEnum> sdoSection7ImpactAnalysisOptions;
    @JsonProperty("sdoSection7FactsEditContent")
    private final String sdoSection7FactsEditContent;
    @JsonProperty("sdoSection7daOccuredEditContent")
    private final String sdoSection7daOccuredEditContent;

    @JsonProperty("sdoSection7ChildImpactAnalysis")
    private final SdoReportSentByEnum sdoSection7ChildImpactAnalysis;
    @JsonProperty("sdoNameOfCouncil")
    private final String sdoNameOfCouncil;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoCafcassCymruReportSentByDate;
    @JsonProperty("sdoLocalAuthorityName")
    private final String sdoLocalAuthorityName;
    @JsonProperty("sdoLocalAuthorityTextArea")
    private final String sdoLocalAuthorityTextArea;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoLocalAuthorityReportSubmitByDate;
    private final DynamicList sdoTransferApplicationCourtDynamicList;
    @JsonProperty("sdoTransferApplicationReason")
    private final List<SdoTransferApplicationReasonEnum> sdoTransferApplicationReason;
    @JsonProperty("sdoTransferApplicationSpecificReason")
    private final String sdoTransferApplicationSpecificReason;
    @JsonProperty("sdoCrossExaminationCourtHavingHeard")
    private final String sdoCrossExaminationCourtHavingHeard;
    @JsonProperty("sdoCrossExaminationEx740")
    private final String sdoCrossExaminationEx740;
    @JsonProperty("sdoCrossExaminationEx741")
    private final String sdoCrossExaminationEx741;
    @JsonProperty("sdoCrossExaminationQualifiedLegal")
    private final String sdoCrossExaminationQualifiedLegal;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoWitnessStatementsDeadlineDate;
    @JsonProperty("sdoWitnessStatementsSentTo")
    private final SdoWitnessStatementsSentToEnum sdoWitnessStatementsSentTo;
    @JsonProperty("sdoWitnessStatementsCopiesSentTo")
    private final SdoReportsSentToEnum sdoWitnessStatementsCopiesSentTo;
    @JsonProperty("sdoWitnessStatementsMaximumPages")
    private final String sdoWitnessStatementsMaximumPages;
    @JsonProperty("sdoSpecifiedDocuments")
    private final String sdoSpecifiedDocuments;
    private final DynamicList sdoInstructionsFilingPartiesDynamicList;
    @JsonProperty("sdoSpipAttendance")
    private final String sdoSpipAttendance;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoHospitalRecordsDeadlineDate;
    @JsonProperty("sdoMedicalDisclosureUploadedBy")
    private final List<SdoApplicantRespondentEnum> sdoMedicalDisclosureUploadedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoLetterFromGpDeadlineDate;
    @JsonProperty("sdoLetterFromGpUploadedBy")
    private final List<SdoApplicantRespondentEnum> sdoLetterFromGpUploadedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoLetterFromSchoolDeadlineDate;
    @JsonProperty("sdoLetterFromSchoolUploadedBy")
    private final List<SdoApplicantRespondentEnum> sdoLetterFromSchoolUploadedBy;
    @JsonProperty("sdoScheduleOfAllegationsOption")
    private final List<SdoScheduleOfAllegationsEnum> sdoScheduleOfAllegationsOption;
    private final List<Element<SdoDisclosureOfPapersCaseNumber>> sdoDisclosureOfPapersCaseNumbers;
    @JsonProperty("sdoParentWithCare")
    private final String sdoParentWithCare;

    @JsonProperty("sdoPermissionHearingDirections")
    private final String sdoPermissionHearingDirections;
    @JsonProperty("sdoPermissionHearingDetails")
    private final HearingData sdoPermissionHearingDetails;
    @JsonProperty("sdoSecondHearingDetails")
    private final HearingData sdoSecondHearingDetails;
    @JsonProperty("sdoNextStepJudgeName")
    private final JudicialUser sdoNextStepJudgeName;
    @JsonProperty("sdoAllocateOrReserveJudge")
    private final AllocateOrReserveJudgeEnum sdoAllocateOrReserveJudge;
    @JsonProperty("sdoAllocateOrReserveJudgeName")
    private final JudicialUser sdoAllocateOrReserveJudgeName;
    @JsonProperty("sdoUrgentHearingDetails")
    private final HearingData sdoUrgentHearingDetails;
    @JsonProperty("sdoFhdraHearingDetails")
    private final HearingData sdoFhdraHearingDetails;
    @JsonProperty("sdoPositionStatementOtherCheckDetails")
    private final List<OtherDirectionPositionStatementEnum> sdoPositionStatementOtherCheckDetails;
    @JsonProperty("sdoPositionStatementOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoPositionStatementOtherDetails;
    @JsonProperty("sdoMiamOtherCheckDetails")
    private final List<MiamOtherDirectionEnum> sdoMiamOtherCheckDetails;
    @JsonProperty("sdoMiamOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoMiamOtherDetails;
    @JsonProperty("sdoDraHearingDetails")
    private final HearingData sdoDraHearingDetails;
    @JsonProperty("sdoSettlementHearingDetails")
    private final HearingData sdoSettlementHearingDetails;
    @JsonProperty("sdoInterpreterOtherDetailsCheck")
    private final List<DioOtherDirectionEnum> sdoInterpreterOtherDetailsCheck;
    @JsonProperty("sdoInterpreterOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoInterpreterOtherDetails;
    @JsonProperty("sdoCafcassFileAndServeCheck")
    private final List<SdoCafcassFileAndServeCheckEnum> sdoCafcassFileAndServeCheck;
    @JsonProperty("sdoCafcassFileAndServeDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoCafcassFileAndServeDetails;
    @JsonProperty("safeguardingCafcassCymruCheck")
    private final List<SdoSafeguardingCafcassCymruEnum> safeguardingCafcassCymruCheck;
    @JsonProperty("safeguardingCafcassCymruDetails")
    private final List<Element<SdoDioProvideOtherDetails>> safeguardingCafcassCymruDetails;
    @JsonProperty("sdoPartyToProvideDetailsCheck")
    private final List<SdoPartyToProvideDetailsEnum> sdoPartyToProvideDetailsCheck;
    @JsonProperty("sdoPartyToProvideDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoPartyToProvideDetails;
    @JsonProperty("sdoNewPartnersToCafcassCheck")
    private final List<SdoNewPartnersToCafcassEnum> sdoNewPartnersToCafcassCheck;
    @JsonProperty("sdoNewPartnersToCafcassDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoNewPartnersToCafcassDetails;
    @JsonProperty("sdoSection7Check")
    private final List<SdoSection7CheckEnum> sdoSection7Check;
    @JsonProperty("sdoSection7CheckDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoSection7CheckDetails;
    @JsonProperty("sdoLocalAuthorityCheck")
    private final List<SdoLocalAuthorityCheckEnum> sdoLocalAuthorityCheck;
    @JsonProperty("sdoLocalAuthorityDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoLocalAuthorityDetails;
    @JsonProperty("sdoTransferCourtDetailsCheck")
    private final List<DioTransferCourtDirectionEnum> sdoTransferCourtDetailsCheck;
    @JsonProperty("sdoTransferCourtDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoTransferCourtDetails;
    @JsonProperty("sdoCrossExaminationCourtCheck")
    private final List<SdoCrossExaminationCourtCheckEnum> sdoCrossExaminationCourtCheck;
    @JsonProperty("sdoCrossExaminationCourtDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoCrossExaminationCourtDetails;
    @JsonProperty("sdoWitnessStatementsCheck")
    private final List<SdoWitnessStatementsCheckEnum> sdoWitnessStatementsCheck;
    @JsonProperty("sdoWitnessStatementsCheckDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoWitnessStatementsCheckDetails;
    @JsonProperty("sdoInstructionsFilingCheck")
    private final List<SdoInstructionsFilingPartiesCheck> sdoInstructionsFilingCheck;
    @JsonProperty("sdoInstructionsFilingDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoInstructionsFilingDetails;

    @JsonProperty("sdoMedicalDiscApplicantName")
    private final List<Element<SdoNameOfApplicant>> sdoMedicalDiscApplicantName;
    @JsonProperty("sdoMedicalDiscRespondentName")
    private final List<Element<SdoNameOfRespondent>> sdoMedicalDiscRespondentName;
    @JsonProperty("sdoMedicalDiscFilingCheck")
    private final List<SdoMedicalDiscCheckEnum> sdoMedicalDiscFilingCheck;
    @JsonProperty("sdoMedicalDiscFilingDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoMedicalDiscFilingDetails;
    @JsonProperty("sdoGpApplicantName")
    private final List<Element<SdoNameOfApplicant>> sdoGpApplicantName;
    @JsonProperty("sdoGpRespondentName")
    private final List<Element<SdoNameOfRespondent>> sdoGpRespondentName;
    @JsonProperty("sdoLetterFromDiscGpCheck")
    private final List<SdoLetterFromDiscGpCheckEnum> sdoLetterFromDiscGpCheck;
    @JsonProperty("sdoLetterFromGpDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoLetterFromGpDetails;
    @JsonProperty("sdoLsApplicantName")
    private final List<Element<SdoNameOfApplicant>> sdoLsApplicantName;
    @JsonProperty("sdoLsRespondentName")
    private final List<Element<SdoNameOfRespondent>> sdoLsRespondentName;
    @JsonProperty("sdoLetterFromSchoolCheck")
    private final List<SdoLetterFromSchoolCheck> sdoLetterFromSchoolCheck;
    @JsonProperty("sdoLetterFromSchoolDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoLetterFromSchoolDetails;
    @JsonProperty("sdoScheduleOfAllegationsDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoScheduleOfAllegationsDetails;
    @JsonProperty("sdoDisClosureProceedingCheck")
    private final List<SdoDisClosureProceedingCheck> sdoDisClosureProceedingCheck;
    @JsonProperty("sdoDisClosureProceedingDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoDisClosureProceedingDetails;
    private final List<Element<SdoFurtherDirections>> sdoFurtherDirectionDetails;
    @JsonProperty("sdoCrossExaminationEditContent")
    private final String sdoCrossExaminationEditContent;
    @JsonProperty("sdoNamedJudgeFullName")
    private String sdoNamedJudgeFullName;

    private final String sdoAfterSecondGatekeeping;
    private final List<Element<AddNewPreamble>> sdoAddNewPreambleCollection;
    private final String sdoNextStepsAfterGatekeeping;
    private final DynamicMultiSelectList sdoNewPartnerPartiesCafcass;
    private final DynamicMultiSelectList sdoNewPartnerPartiesCafcassCymru;
    private final String sdoNewPartnerPartiesCafcassText;
    private final String sdoNewPartnerPartiesCafcassCymruText;
    @JsonProperty("sdoAllocateDecisionJudgeFullName")
    private String sdoAllocateDecisionJudgeFullName;
    @JsonProperty("listElementsSetToDefaultValue")
    private YesOrNo listElementsSetToDefaultValue;
}
