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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCourtnavCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCrudCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus2RolesTbxcefAccess;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCruCourtnavCrudAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StandardDirectionOrder implements MappableObject {

    @CCD(
            label = "Preambles",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoPreamblesList")
    private final List<SdoPreamblesEnum> sdoPreamblesList;
    @CCD(
            label = "Preambles",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
    )
    @JsonProperty("sdoPreamblesTempList")
    private final List<SdoPreamblesEnum> sdoPreamblesTempList;
    @CCD(
            label = "Hearings and next steps",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoHearingsAndNextStepsList")
    private final List<SdoHearingsAndNextStepsEnum> sdoHearingsAndNextStepsList;
    @CCD(
            label = "Hearings and next steps",
            searchable = false,
            access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesTbxcefAccess.class}
    )
    @JsonProperty("sdoHearingsAndNextStepsTempList")
    private final List<SdoHearingsAndNextStepsEnum> sdoHearingsAndNextStepsTempList;
    @CCD(
            label = "Cafcass or Cafcass Cymru",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoCafcassOrCymruList")
    private final List<SdoCafcassOrCymruEnum> sdoCafcassOrCymruList;
    @CCD(
            label = "Cafcass or Cafcass Cymru",
            searchable = false,
            access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesTbxcefAccess.class}
    )
    @JsonProperty("sdoCafcassOrCymruTempList")
    private final List<SdoCafcassOrCymruEnum> sdoCafcassOrCymruTempList;
    @CCD(
            label = "Local Authority",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoLocalAuthorityList")
    private final List<SdoLocalAuthorityEnum> sdoLocalAuthorityList;
    @CCD(
            label = "Local Authority",
            searchable = false,
            access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesTbxcefAccess.class}
    )
    @JsonProperty("sdoLocalAuthorityTempList")
    private final List<SdoLocalAuthorityEnum> sdoLocalAuthorityTempList;
    @CCD(
            label = "Court",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoCourtList")
    private final List<SdoCourtEnum> sdoCourtList;
    @CCD(
            label = "Court",
            searchable = false,
            access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesTbxcefAccess.class}
    )
    @JsonProperty("sdoCourtTempList")
    private final List<SdoCourtEnum> sdoCourtTempList;
    @CCD(
            label = "Documentation and evidence",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoDocumentationAndEvidenceList")
    private final List<SdoDocumentationAndEvidenceEnum> sdoDocumentationAndEvidenceList;
    @CCD(
            label = "Documentation and evidence",
            searchable = false,
            access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesTbxcefAccess.class}
    )
    @JsonProperty("sdoDocumentationAndEvidenceTempList")
    private final List<SdoDocumentationAndEvidenceEnum> sdoDocumentationAndEvidenceTempList;
    @CCD(
            label = "Further directions",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoFurtherList")
    private final List<SdoFurtherInstructionsEnum> sdoFurtherList;
    @CCD(
            label = "Other",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoOtherList")
    private final List<SdoOtherEnum> sdoOtherList;
    @CCD(
            label = "Other",
            searchable = false,
            access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesTbxcefAccess.class}
    )
    @JsonProperty("sdoOtherTempList")
    private final List<SdoOtherEnum> sdoOtherTempList;

    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoRightToAskCourt")
    private final String sdoRightToAskCourt;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    private final List<Element<PartyNameDA>> sdoPartiesRaisedAbuseCollection;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoNextStepsAfterSecondGK")
    private final String sdoNextStepsAfterSecondGK;
    @CCD(
            label = "Allocate to",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoNextStepsAllocationTo")
    private final SdoNextStepsAllocationEnum sdoNextStepsAllocationTo;
    @CCD(
            label = "The hearing is urgent because:",
            hint = "Select all that apply",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoHearingUrgentCheckList")
    private final List<SdoHearingUrgentCheckListEnum> sdoHearingUrgentCheckList;
    @CCD(
            label = "Another reason that has not been listed",
            hint = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
    )
    @JsonProperty("sdoHearingUrgentAnotherReason")
    private final String sdoHearingUrgentAnotherReason;
    @CCD(
            label = "At that hearing the court will consider",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoHearingUrgentCourtConsider")
    private final String sdoHearingUrgentCourtConsider;
    @CCD(
            label = "Time for service of the application is shortened to (Days)",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoHearingUrgentTimeShortened")
    private final String sdoHearingUrgentTimeShortened;
    @CCD(
            label = "Application and notice of hearing must be served by",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoHearingUrgentMustBeServedBy;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoHearingNotNeeded")
    private final String sdoHearingNotNeeded;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoParticipationDirections")
    private final String sdoParticipationDirections;
    @CCD(
            label = "Deadline to send the report",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoPositionStatementDeadlineDate;
    @CCD(
            label = "Written statement also sent to",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoPositionStatementWritten")
    private final SdoWrittenStatementEnum sdoPositionStatementWritten;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    private final List<Element<MiamAttendingPersonName>> sdoMiamAttendingPerson;
    @CCD(
            label = "Insert joining instructions",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoJoiningInstructionsForRH")
    private final String sdoJoiningInstructionsForRH;
    //Not required - start
    @CCD(
            label = "Allegations made by",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoHearingAllegationsMadeBy")
    private final List<SdoApplicantRespondentEnum> sdoHearingAllegationsMadeBy;
    @CCD(
            label = "Court has requested",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoHearingCourtHasRequested")
    private final List<SdoCourtRequestedEnum> sdoHearingCourtHasRequested;
    //Not required - end
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    @JsonProperty("sdoDirectionsForFactFindingHearingDetails")
    private final HearingData sdoDirectionsForFactFindingHearingDetails;
    @CCD(
            label = "The court requests",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    @JsonProperty("sdoHearingCourtRequests")
    private final SdoCourtRequestedEnum sdoHearingCourtRequests;
    @CCD(
            label = "Select who has made the allegations",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    @JsonProperty("sdoWhoMadeAllegationsList")
    private final DynamicMultiSelectList sdoWhoMadeAllegationsList;
    @CCD(
            label = "Select who needs to respond to the allegations",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    @JsonProperty("sdoWhoNeedsToRespondAllegationsList")
    private final DynamicMultiSelectList sdoWhoNeedsToRespondAllegationsList;
    @CCD(
            label = "List of allegations deadline",
            hint = "When the party must provide the list of allegation by",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoAllegationsDeadlineDate;
    @CCD(
            label = "List of Written response deadline",
            hint = "When the parties must provide their statements by",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoWrittenResponseDeadlineDate;
    @CCD(
            label = "Evidence and/or response to be sent to",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoHearingReportsAlsoSentTo")
    private final List<SdoReportsAlsoSentToEnum> sdoHearingReportsAlsoSentTo;
    @CCD(
            label = "Directions for the party who made the allegations",
            hint = "Edit the clauses you need",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    @JsonProperty("sdoWhoMadeAllegationsText")
    private final String sdoWhoMadeAllegationsText;
    @CCD(
            label = "Directions for the party who needs to respond to the allegations",
            hint = "Edit the clauses you need",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    @JsonProperty("sdoWhoNeedsToRespondAllegationsText")
    private final String sdoWhoNeedsToRespondAllegationsText;
    @CCD(
            label = "Maximum number of pages per statement",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoHearingMaximumPages")
    private final String sdoHearingMaximumPages;
    @CCD(
            label = "Maximum number of witness statements for each party",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorRCourtnavCruAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("sdoHearingHowManyWitnessEvidence")
    private final int sdoHearingHowManyWitnessEvidence;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoFactFindingOtherCheck")
    private final List<FactFindingOtherDirectionEnum> sdoFactFindingOtherCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoFactFindingOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoFactFindingOtherDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class, CourtnavCrudAccess.class}
    )
    private final String sdoWhoNeedsToRespondAllegationsListText;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class, CourtnavCrudAccess.class}
    )
    private final String sdoWhoMadeAllegationsListText;
    @CCD(
            label = "How many witnesses' evidence may each party use?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class, CaseworkerPrivatelawSolicitorRAccess.class, CitizenCruAccess.class}
    )
    @JsonProperty("sdoDocsEvidenceWitnessEvidence")
    private final int sdoDocsEvidenceWitnessEvidence;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    private final List<Element<SdoLanguageDialect>> sdoInterpreterDialectRequired;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoUpdateContactDetails")
    private final String sdoUpdateContactDetails;
    @CCD(
            label = "Cafcass will file and serve by 4pm on",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoCafcassFileAndServe;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
    )
    private final String sdoCafcassNextStepEditContent;
    @CCD(
            label = "Cafcass Cymru will file and serve by 4pm on",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoCafcassCymruFileAndServe;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    private final String sdoCafcassCymruNextStepEditContent;
    @CCD(
            label = "Cafcass are directed to undertake safeguarding checks by",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoNewPartnersToCafcass;
    @CCD(
            label = "Cafcass are directed to undertake safeguarding checks by",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoNewPartnersToCafcassCymru;

    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
    )
    @JsonProperty("sdoSection7EditContent")
    private final String sdoSection7EditContent;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
    )
    @JsonProperty("sdoSection7ImpactAnalysisOptions")
    private final List<SdoSection7ImpactAnalysisEnum> sdoSection7ImpactAnalysisOptions;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
    )
    @JsonProperty("sdoSection7FactsEditContent")
    private final String sdoSection7FactsEditContent;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
    )
    @JsonProperty("sdoSection7daOccuredEditContent")
    private final String sdoSection7daOccuredEditContent;

    @CCD(
            label = "Report to be completed by",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoSection7ChildImpactAnalysis")
    private final SdoReportSentByEnum sdoSection7ChildImpactAnalysis;
    @CCD(
            label = "Name of local authority",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoNameOfCouncil")
    private final String sdoNameOfCouncil;
    @CCD(
            label = "To be filed and served by",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoCafcassCymruReportSentByDate;
    @CCD(
            label = "Name of local authority",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoLocalAuthorityName")
    private final String sdoLocalAuthorityName;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoLocalAuthorityTextArea")
    private final String sdoLocalAuthorityTextArea;
    @CCD(
            label = "Deadline to submit the letter",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoLocalAuthorityReportSubmitByDate;
    @CCD(
            label = "Court location",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruCourtnavCrudAccess.class}
    )
    private final DynamicList sdoTransferApplicationCourtDynamicList;
    @CCD(
            label = "The reason for transfer is:",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoTransferApplicationReason")
    private final List<SdoTransferApplicationReasonEnum> sdoTransferApplicationReason;
    @CCD(
            label = "Specify reason",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoTransferApplicationSpecificReason")
    private final String sdoTransferApplicationSpecificReason;
    @CCD(
            label = "The court having heard",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoCrossExaminationCourtHavingHeard")
    private final String sdoCrossExaminationCourtHavingHeard;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoCrossExaminationEx740")
    private final String sdoCrossExaminationEx740;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoCrossExaminationEx741")
    private final String sdoCrossExaminationEx741;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoCrossExaminationQualifiedLegal")
    private final String sdoCrossExaminationQualifiedLegal;
    @CCD(
            label = "Deadline date to send written statements of evidence",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoWitnessStatementsDeadlineDate;
    @CCD(
            label = "Written statement of evidence also sent to",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoWitnessStatementsSentTo")
    private final SdoWitnessStatementsSentToEnum sdoWitnessStatementsSentTo;
    @CCD(
            label = "Copies of statements sent to",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoWitnessStatementsCopiesSentTo")
    private final SdoReportsSentToEnum sdoWitnessStatementsCopiesSentTo;
    @CCD(
            label = "Maximum number of pages",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoWitnessStatementsMaximumPages")
    private final String sdoWitnessStatementsMaximumPages;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoSpecifiedDocuments")
    private final String sdoSpecifiedDocuments;
    @CCD(
            label = "Bundle prepared by",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    private final DynamicList sdoInstructionsFilingPartiesDynamicList;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoSpipAttendance")
    private final String sdoSpipAttendance;

    @CCD(
            label = "GP and any hospital records deadline",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoHospitalRecordsDeadlineDate;
    @CCD(
            label = "Uploaded by",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoMedicalDisclosureUploadedBy")
    private final List<SdoApplicantRespondentEnum> sdoMedicalDisclosureUploadedBy;
    @CCD(
            label = "Mental health and treatment letter deadline",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoLetterFromGpDeadlineDate;
    @CCD(
            label = "Uploaded by",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoLetterFromGpUploadedBy")
    private final List<SdoApplicantRespondentEnum> sdoLetterFromGpUploadedBy;
    @CCD(
            label = "School letter deadline",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoLetterFromSchoolDeadlineDate;
    @CCD(
            label = "Uploaded by",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoLetterFromSchoolUploadedBy")
    private final List<SdoApplicantRespondentEnum> sdoLetterFromSchoolUploadedBy;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoScheduleOfAllegationsOption")
    private final List<SdoScheduleOfAllegationsEnum> sdoScheduleOfAllegationsOption;
    @CCD(
            label = "Case number",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    private final List<Element<SdoDisclosureOfPapersCaseNumber>> sdoDisclosureOfPapersCaseNumbers;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoParentWithCare")
    private final String sdoParentWithCare;

    @CCD(
            label = "List for the court to consider",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoPermissionHearingDirections")
    private final String sdoPermissionHearingDirections;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoPermissionHearingDetails")
    private final HearingData sdoPermissionHearingDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoSecondHearingDetails")
    private final HearingData sdoSecondHearingDetails;
    @CCD(
            label = "Name of judge",
            searchable = false,
            typeOverride = FieldType.JudicialUser,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoNextStepJudgeName")
    private final JudicialUser sdoNextStepJudgeName;
    @CCD(
            label = "The case is to be",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoAllocateOrReserveJudge")
    private final AllocateOrReserveJudgeEnum sdoAllocateOrReserveJudge;
    @CCD(
            label = "Name of judge",
            searchable = false,
            typeOverride = FieldType.JudicialUser,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoAllocateOrReserveJudgeName")
    private final JudicialUser sdoAllocateOrReserveJudgeName;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoUrgentHearingDetails")
    private final HearingData sdoUrgentHearingDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoFhdraHearingDetails")
    private final HearingData sdoFhdraHearingDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoPositionStatementOtherCheckDetails")
    private final List<OtherDirectionPositionStatementEnum> sdoPositionStatementOtherCheckDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoPositionStatementOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoPositionStatementOtherDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoMiamOtherCheckDetails")
    private final List<MiamOtherDirectionEnum> sdoMiamOtherCheckDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoMiamOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoMiamOtherDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoDraHearingDetails")
    private final HearingData sdoDraHearingDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoSettlementHearingDetails")
    private final HearingData sdoSettlementHearingDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoInterpreterOtherDetailsCheck")
    private final List<DioOtherDirectionEnum> sdoInterpreterOtherDetailsCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoInterpreterOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoInterpreterOtherDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoCafcassFileAndServeCheck")
    private final List<SdoCafcassFileAndServeCheckEnum> sdoCafcassFileAndServeCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoCafcassFileAndServeDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoCafcassFileAndServeDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("safeguardingCafcassCymruCheck")
    private final List<SdoSafeguardingCafcassCymruEnum> safeguardingCafcassCymruCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("safeguardingCafcassCymruDetails")
    private final List<Element<SdoDioProvideOtherDetails>> safeguardingCafcassCymruDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoPartyToProvideDetailsCheck")
    private final List<SdoPartyToProvideDetailsEnum> sdoPartyToProvideDetailsCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoPartyToProvideDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoPartyToProvideDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoNewPartnersToCafcassCheck")
    private final List<SdoNewPartnersToCafcassEnum> sdoNewPartnersToCafcassCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoNewPartnersToCafcassDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoNewPartnersToCafcassDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoSection7Check")
    private final List<SdoSection7CheckEnum> sdoSection7Check;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoSection7CheckDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoSection7CheckDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoLocalAuthorityCheck")
    private final List<SdoLocalAuthorityCheckEnum> sdoLocalAuthorityCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoLocalAuthorityDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoLocalAuthorityDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoTransferCourtDetailsCheck")
    private final List<DioTransferCourtDirectionEnum> sdoTransferCourtDetailsCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoTransferCourtDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoTransferCourtDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoCrossExaminationCourtCheck")
    private final List<SdoCrossExaminationCourtCheckEnum> sdoCrossExaminationCourtCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoCrossExaminationCourtDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoCrossExaminationCourtDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoWitnessStatementsCheck")
    private final List<SdoWitnessStatementsCheckEnum> sdoWitnessStatementsCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoWitnessStatementsCheckDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoWitnessStatementsCheckDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoInstructionsFilingCheck")
    private final List<SdoInstructionsFilingPartiesCheck> sdoInstructionsFilingCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoInstructionsFilingDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoInstructionsFilingDetails;

    @CCD(
            label = "Name of applicant",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoMedicalDiscApplicantName")
    private final List<Element<SdoNameOfApplicant>> sdoMedicalDiscApplicantName;
    @CCD(
            label = "Name of respondent",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoMedicalDiscRespondentName")
    private final List<Element<SdoNameOfRespondent>> sdoMedicalDiscRespondentName;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoMedicalDiscFilingCheck")
    private final List<SdoMedicalDiscCheckEnum> sdoMedicalDiscFilingCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoMedicalDiscFilingDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoMedicalDiscFilingDetails;
    @CCD(
            label = "Name of applicant",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoGpApplicantName")
    private final List<Element<SdoNameOfApplicant>> sdoGpApplicantName;
    @CCD(
            label = "Name of respondent",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoGpRespondentName")
    private final List<Element<SdoNameOfRespondent>> sdoGpRespondentName;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoLetterFromDiscGpCheck")
    private final List<SdoLetterFromDiscGpCheckEnum> sdoLetterFromDiscGpCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoLetterFromGpDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoLetterFromGpDetails;
    @CCD(
            label = "Name of applicant",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoLsApplicantName")
    private final List<Element<SdoNameOfApplicant>> sdoLsApplicantName;
    @CCD(
            label = "Name of respondent",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoLsRespondentName")
    private final List<Element<SdoNameOfRespondent>> sdoLsRespondentName;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    @JsonProperty("sdoLetterFromSchoolCheck")
    private final List<SdoLetterFromSchoolCheck> sdoLetterFromSchoolCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoLetterFromSchoolDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoLetterFromSchoolDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoScheduleOfAllegationsDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoScheduleOfAllegationsDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoDisClosureProceedingCheck")
    private final List<SdoDisClosureProceedingCheck> sdoDisClosureProceedingCheck;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoDisClosureProceedingDetails")
    private final List<Element<SdoDioProvideOtherDetails>> sdoDisClosureProceedingDetails;
    @CCD(
            label = "Add a new direction",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    private final List<Element<SdoFurtherDirections>> sdoFurtherDirectionDetails;
    @CCD(
            label = "Edit selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("sdoCrossExaminationEditContent")
    private final String sdoCrossExaminationEditContent;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoNamedJudgeFullName")
    private String sdoNamedJudgeFullName;

    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    private final String sdoAfterSecondGatekeeping;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    private final List<Element<AddNewPreamble>> sdoAddNewPreambleCollection;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
    )
    private final String sdoNextStepsAfterGatekeeping;
    @CCD(
            label = "Who needs to provide these details?",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    private final DynamicMultiSelectList sdoNewPartnerPartiesCafcass;
    @CCD(
            label = "Who needs to provide these details?",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    private final DynamicMultiSelectList sdoNewPartnerPartiesCafcassCymru;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    private final String sdoNewPartnerPartiesCafcassText;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    private final String sdoNewPartnerPartiesCafcassCymruText;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class}
    )
    @JsonProperty("sdoAllocateDecisionJudgeFullName")
    private String sdoAllocateDecisionJudgeFullName;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CourtnavCrudAccess.class}
    )
    @JsonProperty("listElementsSetToDefaultValue")
    private YesOrNo listElementsSetToDefaultValue;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CourtnavCrudAccess.class}
    )
    @JsonProperty("editedOrderHasDefaultCaseFields")
    private YesOrNo editedOrderHasDefaultCaseFields;
}
