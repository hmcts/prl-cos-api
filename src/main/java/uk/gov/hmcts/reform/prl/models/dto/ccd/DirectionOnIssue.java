package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.dio.DioBeforeAEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioCourtEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioDisclosureDirectionEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioHearingUrgentCheckListEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioLocalAuthorityDirectionEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioNextStepsAllocationEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioOtherDirectionEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioOtherEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioRemoteHearingEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioTransferCourtDirectionEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioUrgentHearingRefusedCheckListEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioWithoutNoticeFirstHearingCheckListEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioWithoutNoticeHearingRefusedCheckListEnum;
import uk.gov.hmcts.reform.prl.enums.dio.MiamOtherDirectionEnum;
import uk.gov.hmcts.reform.prl.enums.dio.OtherDirectionPositionStatementEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoTransferApplicationReasonEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoWrittenStatementEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.MiamAttendingPersonName;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio.DioApplicationToApplyPermission;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio.SdoDioProvideOtherDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.PartyNameDA;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.SdoDisclosureOfPapersCaseNumber;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.SdoLanguageDialect;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectionOnIssue {

    @CCD(
            label = "Preambles",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioPreamblesList")
    private final List<DioPreamblesEnum> dioPreamblesList;
    @CCD(
            label = "Hearings and next steps",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioHearingsAndNextStepsList")
    private final List<DioHearingsAndNextStepsEnum> dioHearingsAndNextStepsList;
    @CCD(
            label = "Cafcass/Cafcass Cymru",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioCafcassOrCymruList")
    private final List<DioCafcassOrCymruEnum> dioCafcassOrCymruList;
    @CCD(
            label = "Local Authority",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioLocalAuthorityList")
    private final List<DioLocalAuthorityEnum> dioLocalAuthorityList;
    @CCD(
            label = "Court",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioCourtList")
    private final List<DioCourtEnum> dioCourtList;
    @CCD(
            label = "Other",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioOtherList")
    private final List<DioOtherEnum> dioOtherList;

    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioRightToAskCourt")
    private final String dioRightToAskCourt;
    @CCD(
            label = "Name of party raising domestic abuse issue",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioPartiesRaisedAbuseCollection")
    private final List<Element<PartyNameDA>> dioPartiesRaisedAbuseCollection;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioCaseReviewAtSecondGateKeeping")
    private final String dioCaseReviewAtSecondGateKeeping;
    @CCD(
            label = "Allocate to",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioNextStepsAllocationTo")
    private final DioNextStepsAllocationEnum dioNextStepsAllocationTo;
    @CCD(
            label = "The application is reserved to",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioApplicationIsReservedTo")
    private final String dioApplicationIsReservedTo;
    @CCD(
            label = "List for hearing on",
            hint = "Please enter date and time",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime dioPermissionHearingOn;
    @CCD(
            label = "Time estimate",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioPermissionHearingTimeEstimate")
    private final String dioPermissionHearingTimeEstimate;
    @CCD(
            label = "Select from the list of courts",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioPermissionHearingCourtDynamicList")
    private final DynamicList dioPermissionHearingCourtDynamicList;
    @CCD(
            label = "This will be before a",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioPermissionHearingBeforeAList")
    private final DioBeforeAEnum dioPermissionHearingBeforeAList;
    @CCD(
            label = "Hearing date",
            hint = "Please enter date and time",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime dioUrgentFirstHearingDate;
    @CCD(
            label = "Select all that apply",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioHearingUrgentCheckList")
    private final List<DioHearingUrgentCheckListEnum> dioHearingUrgentCheckList;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioFirstHearingUrgencyDetails")
    private final String dioFirstHearingUrgencyDetails;
    @CCD(
            label = "At that hearing the court will consider",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioHearingUrgentCourtConsider")
    private final String dioHearingUrgentCourtConsider;
    @CCD(
            label = "Hour",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioHearingUrgentTimeShortened")
    private final String dioHearingUrgentTimeShortened;
    @CCD(
            label = "Day",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioHearingUrgentDayShortened")
    private final String dioHearingUrgentDayShortened;
    @CCD(
            label = "Application and notice of hearing must be served by",
            hint = "Please enter date and time",
            searchable = false,
            typeOverride = FieldType.DateTime,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioHearingUrgentMustBeServedBy;
    @CCD(
            label = "This will be a remote hearing by way of",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioHearingUrgentByWayOf")
    private final DioRemoteHearingEnum dioHearingUrgentByWayOf;
    @CCD(
            label = "Select all that apply",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioUrgentHearingRefusedCheckList")
    private final List<DioUrgentHearingRefusedCheckListEnum> dioUrgentHearingRefusedCheckList;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioHearingUrgencyRefusedDetails")
    private final String dioHearingUrgencyRefusedDetails;
    @CCD(
            label = "Select all that apply",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioWithoutNoticeFirstHearingCheckList")
    private final List<DioWithoutNoticeFirstHearingCheckListEnum> dioWithoutNoticeFirstHearingCheckList;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioWithoutNoticeFirstHearingDetails")
    private final String dioWithoutNoticeFirstHearingDetails;
    @CCD(
            label = "Select all that apply",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioWithoutNoticeHearingRefusedCheckList")
    private final List<DioWithoutNoticeHearingRefusedCheckListEnum> dioWithoutNoticeHearingRefusedCheckList;
    @CCD(
            label = "Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioWithoutNoticeHearingRefusedDetails")
    private final String dioWithoutNoticeHearingRefusedDetails;
    @CCD(
            label = "A first hearing dispute resolution will occur at",
            hint = "Please enter date and time",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime dioFhdraStartDateTime;
    @CCD(
            label = "Select from the list of courts",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    private final DynamicList dioFhdraCourtDynamicList;
    @CCD(
            label = "This will be before a",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioFhdraBeforeAList")
    private final DioBeforeAEnum dioFhdraBeforeAList;
    @CCD(
            label = "This will be a remote hearing by way",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioFhdraByWayOf")
    private final DioRemoteHearingEnum dioFhdraByWayOf;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioParticipationDirections")
    private final String dioParticipationDirections;
    @CCD(
            label = "Deadline to send the report",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioPositionStatementDeadlineDate;

    @CCD(
            label = "Written statement also sent to",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioPositionStatementWritten")
    private final SdoWrittenStatementEnum dioPositionStatementWritten;
    @CCD(
            label = "Name or person to attend MIAM",
            searchable = false,
            typeOverride = FieldType.Text,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioMiamAttendingPerson")
    private final List<Element<MiamAttendingPersonName>> dioMiamAttendingPerson;

    @CCD(
            label = "Name of the person who requires an interpreter",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioPersonWhoRequiresInterpreter")
    private final String dioPersonWhoRequiresInterpreter;
    @CCD(
            label = "Language or dialect required",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioInterpreterDialectRequired")
    private final List<Element<SdoLanguageDialect>> dioInterpreterDialectRequired;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioUpdateContactDetails")
    private final String dioUpdateContactDetails;

    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioCafcassSafeguardingIssue")
    private final String dioCafcassSafeguardingIssue;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioCafcassCymruSafeguardingIssue")
    private final String dioCafcassCymruSafeguardingIssue;

    @CCD(
            label = "Court location",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioTransferApplicationCourtDynamicList")
    private final DynamicList dioTransferApplicationCourtDynamicList;
    @CCD(
            label = "The reason for transfer is:",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioTransferApplicationReason")
    private final List<SdoTransferApplicationReasonEnum> dioTransferApplicationReason;
    @CCD(
            label = "Specify reason",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioTransferApplicationSpecificReason")
    private final String dioTransferApplicationSpecificReason;
    @CCD(
            label = "Name of local authority",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioLocalAuthorityName")
    private final String dioLocalAuthorityName;
    @CCD(
            label = "Deadline to submit the report",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioLocalAuthorityReportSubmitByDate;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioDisclosureOfPapersCaseNumbers")
    private final List<Element<SdoDisclosureOfPapersCaseNumber>> dioDisclosureOfPapersCaseNumbers;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioParentWithCare")
    private final String dioParentWithCare;
    @CCD(
            label = "Edit your selection",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    @JsonProperty("dioApplicationToApplyPermission")
    private final List<Element<DioApplicationToApplyPermission>> dioApplicationToApplyPermission;

    @CCD(
            label = "List for the court to consider",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioPermissionHearingDirections")
    private final String dioPermissionHearingDirections;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioCaseReviewHearingDetails")
    private final HearingData dioCaseReviewHearingDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioFhdraHearingDetails")
    private final HearingData dioFhdraHearingDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioPermissionHearingDetails")
    private final HearingData dioPermissionHearingDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioUrgentFirstHearingDetails")
    private final HearingData dioUrgentFirstHearingDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioUrgentHearingDetails")
    private final HearingData dioUrgentHearingDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioWithoutNoticeHearingDetails")
    private final HearingData dioWithoutNoticeHearingDetails;

    @CCD(
            label = "Edit your selection",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioPositionStatementDetails")
    private final String dioPositionStatementDetails;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "OtherDirectionPositionStatementEnum",
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioPositionStatementOtherCheckDetails")
    private final OtherDirectionPositionStatementEnum dioPositionStatementOtherCheckDetails;
    @CCD(
            label = "Give details",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioPositionStatementOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> dioPositionStatementOtherDetails;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "MiamOtherDirectionEnum",
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioMiamOtherCheckDetails")
    private final MiamOtherDirectionEnum dioMiamOtherCheckDetails;
    @CCD(
            label = "Give details",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioMiamOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> dioMiamOtherDetails;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DioOtherDirectionEnum",
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioInterpreterOtherDetailsCheck")
    private final DioOtherDirectionEnum dioInterpreterOtherDetailsCheck;
    @CCD(
            label = "Give details",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioInterpreterOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> dioInterpreterOtherDetails;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DioLocalAuthorityDirectionEnum",
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioLocalAuthorityDetailsCheck")
    private final DioLocalAuthorityDirectionEnum dioLocalAuthorityDetailsCheck;
    @CCD(
            label = "Give details",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioLocalAuthorityDetails")
    private final List<Element<SdoDioProvideOtherDetails>> dioLocalAuthorityDetails;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DioTransferCourtDirectionEnum",
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioTransferCourtDetailsCheck")
    private final DioTransferCourtDirectionEnum dioTransferCourtDetailsCheck;
    @CCD(
            label = "Give details",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioTransferCourtDetails")
    private final List<Element<SdoDioProvideOtherDetails>> dioTransferCourtDetails;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "DioDisclosureDirectionEnum",
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioDisclosureOtherDetailsCheck")
    private final DioDisclosureDirectionEnum dioDisclosureOtherDetailsCheck;
    @CCD(
            label = "Give details",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("dioDisclosureOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> dioDisclosureOtherDetails;
}
