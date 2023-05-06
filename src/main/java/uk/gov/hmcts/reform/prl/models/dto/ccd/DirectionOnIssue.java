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
import uk.gov.hmcts.reform.prl.enums.dio.DioHearingUrgentCheckListEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioNextStepsAllocationEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioOtherEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioRemoteHearingEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioUrgentHearingRefusedCheckListEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioWithoutNoticeFirstHearingCheckListEnum;
import uk.gov.hmcts.reform.prl.enums.dio.DioWithoutNoticeHearingRefusedCheckListEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoTransferApplicationReasonEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoWrittenStatementEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.dio.DioApplicationToApplyPermission;
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
    private final String dioMiamAttendingPerson;

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

}
