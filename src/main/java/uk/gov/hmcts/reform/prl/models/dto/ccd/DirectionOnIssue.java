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
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.PartyNameDA;

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

    private final List<Element<PartyNameDA>> dioPartiesRaisedAbuseCollection;
    @JsonProperty("dioNextStepsAfterSecondGK")
    private final String dioNextStepsAfterSecondGK;
    @JsonProperty("dioNextStepsAllocationTo")
    private final DioNextStepsAllocationEnum dioNextStepsAllocationTo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime dioUrgentHearingDate;
    @JsonProperty("dioUrgentHearingTimeEstimate")
    private final String dioUrgentHearingTimeEstimate;
    private final DynamicList dioUrgentHearingCourtDynamicList;
    @JsonProperty("dioHearingUrgentCheckList")
    private final List<DioHearingUrgentCheckListEnum> dioHearingUrgentCheckList;
    @JsonProperty("dioHearingUrgentDetails")
    private final String dioHearingUrgentDetails;
    @JsonProperty("dioHearingUrgentCourtConsider")
    private final String dioHearingUrgentCourtConsider;
    @JsonProperty("dioHearingUrgentTimeShortened")
    private final String dioHearingUrgentTimeShortened;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioHearingUrgentMustBeServedBy;
    @JsonProperty("dioHearingUrgentByWayOf")
    private final DioRemoteHearingEnum dioHearingUrgentByWayOf;
    @JsonProperty("dioHearingNotNeeded")
    private final String dioHearingNotNeeded;
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

    @JsonProperty("dioMiamAttendingPerson")
    private final String dioMiamAttendingPerson;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime dioPermissionHearingOn;
    @JsonProperty("dioPermissionHearingTimeEstimate")
    private final String dioPermissionHearingTimeEstimate;
    @JsonProperty("dioPermissionHearingBeforeAList")
    private final DioBeforeAEnum dioPermissionHearingBeforeAList;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime dioDirectionsDraStartDateAndTime;
    @JsonProperty("dioDirectionsDraHearing")
    private final String dioDirectionsDraHearing;
    private final DynamicList dioDirectionsDraCourtDynamicList;
    @JsonProperty("dioDirectionsDraHearingByWayOf")
    private final DioRemoteHearingEnum dioDirectionsDraHearingByWayOf;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime dioSettlementConferenceDateTime;
    @JsonProperty("dioSettlementConferenceTimeEstimate")
    private final String dioSettlementConferenceTimeEstimate;
    private final DynamicList dioSettlementConferenceCourtDynamicList;
    @JsonProperty("dioSettlementConferenceByWayOf")
    private final DioRemoteHearingEnum dioSettlementConferenceByWayOf;
    @JsonProperty("dioJoiningInstructionsForRH")
    private final String dioJoiningInstructionsForRH;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioAllegationsDeadlineDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioWrittenResponseDeadlineDate;
    @JsonProperty("dioHearingMaximumPages")
    private final String dioHearingMaximumPages;
    @JsonProperty("dioUpdateContactDetails")
    private final String dioUpdateContactDetails;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioCafcassFileAndServe;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioCafcassCymruFileAndServe;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioNewPartnersToCafcass;
    @JsonProperty("dioNameOfCouncil")
    private final String dioNameOfCouncil;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioCafcassCymruReportSentByDate;
    @JsonProperty("dioLocalAuthorityName")
    private final String dioLocalAuthorityName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioLocalAuthorityReportSubmitByDate;
    private final DynamicList dioTransferApplicationCourtDynamicList;
    @JsonProperty("dioTransferApplicationSpecificReason")
    private final String dioTransferApplicationSpecificReason;
    private final DynamicList dioCrossExaminationCourtDynamicList;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime dioCrossExaminationStartDateTime;
    @JsonProperty("dioCrossExaminationCourtHavingHeard")
    private final String dioCrossExaminationCourtHavingHeard;
    @JsonProperty("dioCrossExaminationEx740")
    private final String dioCrossExaminationEx740;
    @JsonProperty("dioCrossExaminationQualifiedLegal")
    private final String dioCrossExaminationQualifiedLegal;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioWitnessStatementsDeadlineDate;
    @JsonProperty("dioWitnessStatementsMaximumPages")
    private final String dioWitnessStatementsMaximumPages;
    @JsonProperty("dioSpecifiedDocuments")
    private final String dioSpecifiedDocuments;
    private final DynamicList dioInstructionsFilingPartiesDynamicList;
    @JsonProperty("dioSpipAttendance")
    private final String dioSpipAttendance;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioHospitalRecordsDeadlineDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioLetterFromGpDeadlineDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dioLetterFromSchoolDeadlineDate;
    @JsonProperty("dioParentWithCare")
    private final String dioParentWithCare;
}
