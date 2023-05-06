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

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DirectionOnIssue {

    @JsonProperty("dioPreamblesList")
    private final List<DioPreamblesEnum> dioPreamblesList;
    @JsonProperty("dioHearingsAndNextStepsList")
    private final List<DioHearingsAndNextStepsEnum> dioHearingsAndNextStepsList;
    @JsonProperty("dioCafcassOrCymruList")
    private final List<DioCafcassOrCymruEnum> dioCafcassOrCymruList;
    @JsonProperty("dioLocalAuthorityList")
    private final List<DioLocalAuthorityEnum> dioLocalAuthorityList;
    @JsonProperty("dioCourtList")
    private final List<DioCourtEnum> dioCourtList;
    @JsonProperty("dioOtherList")
    private final List<DioOtherEnum> dioOtherList;

    @JsonProperty("dioRightToAskCourt")
    private final String dioRightToAskCourt;
    @JsonProperty("dioPartiesRaisedAbuseCollection")
    private final List<Element<PartyNameDA>> dioPartiesRaisedAbuseCollection;
    @JsonProperty("dioCaseReviewAtSecondGateKeeping")
    private final String dioCaseReviewAtSecondGateKeeping;
    @JsonProperty("dioNextStepsAllocationTo")
    private final DioNextStepsAllocationEnum dioNextStepsAllocationTo;
    @JsonProperty("dioApplicationIsReservedTo")
    private final String dioApplicationIsReservedTo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime dioPermissionHearingOn;
    @JsonProperty("dioPermissionHearingTimeEstimate")
    private final String dioPermissionHearingTimeEstimate;
    @JsonProperty("dioPermissionHearingCourtDynamicList")
    private final DynamicList dioPermissionHearingCourtDynamicList;
    @JsonProperty("dioPermissionHearingBeforeAList")
    private final DioBeforeAEnum dioPermissionHearingBeforeAList;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime dioUrgentFirstHearingDate;
    @JsonProperty("dioHearingUrgentCheckList")
    private final List<DioHearingUrgentCheckListEnum> dioHearingUrgentCheckList;
    @JsonProperty("dioFirstHearingUrgencyDetails")
    private final String dioFirstHearingUrgencyDetails;
    @JsonProperty("dioHearingUrgentCourtConsider")
    private final String dioHearingUrgentCourtConsider;
    @JsonProperty("dioHearingUrgentTimeShortened")
    private final String dioHearingUrgentTimeShortened;
    @JsonProperty("dioHearingUrgentDayShortened")
    private final String dioHearingUrgentDayShortened;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioHearingUrgentMustBeServedBy;
    @JsonProperty("dioHearingUrgentByWayOf")
    private final DioRemoteHearingEnum dioHearingUrgentByWayOf;
    @JsonProperty("dioUrgentHearingRefusedCheckList")
    private final List<DioUrgentHearingRefusedCheckListEnum> dioUrgentHearingRefusedCheckList;
    @JsonProperty("dioHearingUrgencyRefusedDetails")
    private final String dioHearingUrgencyRefusedDetails;
    @JsonProperty("dioWithoutNoticeFirstHearingCheckList")
    private final List<DioWithoutNoticeFirstHearingCheckListEnum> dioWithoutNoticeFirstHearingCheckList;
    @JsonProperty("dioWithoutNoticeFirstHearingDetails")
    private final String dioWithoutNoticeFirstHearingDetails;
    @JsonProperty("dioWithoutNoticeHearingRefusedCheckList")
    private final List<DioWithoutNoticeHearingRefusedCheckListEnum> dioWithoutNoticeHearingRefusedCheckList;
    @JsonProperty("dioWithoutNoticeHearingRefusedDetails")
    private final String dioWithoutNoticeHearingRefusedDetails;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime dioFhdraStartDateTime;
    private final DynamicList dioFhdraCourtDynamicList;
    @JsonProperty("dioFhdraBeforeAList")
    private final DioBeforeAEnum dioFhdraBeforeAList;
    @JsonProperty("dioFhdraByWayOf")
    private final DioRemoteHearingEnum dioFhdraByWayOf;
    @JsonProperty("dioParticipationDirections")
    private final String dioParticipationDirections;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioPositionStatementDeadlineDate;

    @JsonProperty("dioPositionStatementWritten")
    private final SdoWrittenStatementEnum dioPositionStatementWritten;
    @JsonProperty("dioMiamAttendingPerson")
    private final List<Element<MiamAttendingPersonName>> dioMiamAttendingPerson;

    @JsonProperty("dioPersonWhoRequiresInterpreter")
    private final String dioPersonWhoRequiresInterpreter;
    @JsonProperty("dioInterpreterDialectRequired")
    private final List<Element<SdoLanguageDialect>> dioInterpreterDialectRequired;
    @JsonProperty("dioUpdateContactDetails")
    private final String dioUpdateContactDetails;

    @JsonProperty("dioCafcassSafeguardingIssue")
    private final String dioCafcassSafeguardingIssue;
    @JsonProperty("dioCafcassCymruSafeguardingIssue")
    private final String dioCafcassCymruSafeguardingIssue;

    @JsonProperty("dioTransferApplicationCourtDynamicList")
    private final DynamicList dioTransferApplicationCourtDynamicList;
    @JsonProperty("dioTransferApplicationReason")
    private final List<SdoTransferApplicationReasonEnum> dioTransferApplicationReason;
    @JsonProperty("dioTransferApplicationSpecificReason")
    private final String dioTransferApplicationSpecificReason;
    @JsonProperty("dioLocalAuthorityName")
    private final String dioLocalAuthorityName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioLocalAuthorityReportSubmitByDate;
    @JsonProperty("dioDisclosureOfPapersCaseNumbers")
    private final List<Element<SdoDisclosureOfPapersCaseNumber>> dioDisclosureOfPapersCaseNumbers;
    @JsonProperty("dioParentWithCare")
    private final String dioParentWithCare;
    @JsonProperty("dioApplicationToApplyPermission")
    private final List<Element<DioApplicationToApplyPermission>> dioApplicationToApplyPermission;

    @JsonProperty("dioPermissionHearingDirections")
    private final String dioPermissionHearingDirections;
    @JsonProperty("dioCaseReviewHearingDetails")
    private final HearingData dioCaseReviewHearingDetails;
    @JsonProperty("dioFhdraHearingDetails")
    private final HearingData dioFhdraHearingDetails;
    @JsonProperty("dioPermissionHearingDetails")
    private final HearingData dioPermissionHearingDetails;
    @JsonProperty("dioUrgentFirstHearingDetails")
    private final HearingData dioUrgentFirstHearingDetails;
    @JsonProperty("dioUrgentHearingDetails")
    private final HearingData dioUrgentHearingDetails;
    @JsonProperty("dioWithoutNoticeHearingDetails")
    private final HearingData dioWithoutNoticeHearingDetails;

    @JsonProperty("dioPositionStatementDetails")
    private final String dioPositionStatementDetails;

    @JsonProperty("dioPositionStatementOtherCheckDetails")
    private final OtherDirectionPositionStatementEnum dioPositionStatementOtherCheckDetails;
    @JsonProperty("dioPositionStatementOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> dioPositionStatementOtherDetails;

    @JsonProperty("dioMiamOtherCheckDetails")
    private final MiamOtherDirectionEnum dioMiamOtherCheckDetails;
    @JsonProperty("dioMiamOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> dioMiamOtherDetails;

    @JsonProperty("dioInterpreterOtherDetailsCheck")
    private final DioOtherDirectionEnum dioInterpreterOtherDetailsCheck;
    @JsonProperty("dioInterpreterOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> dioInterpreterOtherDetails;

    @JsonProperty("dioLocalAuthorityDetailsCheck")
    private final DioLocalAuthorityDirectionEnum dioLocalAuthorityDetailsCheck;
    @JsonProperty("dioLocalAuthorityDetails")
    private final List<Element<SdoDioProvideOtherDetails>> dioLocalAuthorityDetails;

    @JsonProperty("dioTransferCourtDetailsCheck")
    private final DioTransferCourtDirectionEnum dioTransferCourtDetailsCheck;
    @JsonProperty("dioTransferCourtDetails")
    private final List<Element<SdoDioProvideOtherDetails>> dioTransferCourtDetails;

    @JsonProperty("dioDisclosureOtherDetailsCheck")
    private final DioDisclosureDirectionEnum dioDisclosureOtherDetailsCheck;
    @JsonProperty("dioDisclosureOtherDetails")
    private final List<Element<SdoDioProvideOtherDetails>> dioDisclosureOtherDetails;
}
