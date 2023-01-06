package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoApplicantRespondentEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoBeforeAEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCafcassOrCymruEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCourtRequestedEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoDocumentationAndEvidenceEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingUrgentCheckListEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoHearingsAndNextStepsEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoJudgeLaDecideByEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoLocalAuthorityEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoNextStepsAllocationEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoOtherEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoPreamblesEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoRemoteHearingEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoReportsAlsoSentToEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoReportsSentToEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoWrittenStatementEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.PartyNameDA;
import uk.gov.hmcts.reform.prl.models.complextypes.draftorder.sdo.SdoLanguageDialect;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StandardDirectionOrder {

    @JsonProperty("sdoPreamblesList")
    private final List<SdoPreamblesEnum> sdoPreamblesList;
    @JsonProperty("sdoHearingsAndNextStepsList")
    private final List<SdoHearingsAndNextStepsEnum> sdoHearingsAndNextStepsList;
    @JsonProperty("sdoCafcassOrCymruList")
    private final List<SdoCafcassOrCymruEnum> sdoCafcassOrCymruList;
    @JsonProperty("sdoLocalAuthorityList")
    private final List<SdoLocalAuthorityEnum> sdoLocalAuthorityList;
    @JsonProperty("sdoCourtList")
    private final List<SdoCourtEnum> sdoCourtList;
    @JsonProperty("sdoDocumentationAndEvidenceList")
    private final List<SdoDocumentationAndEvidenceEnum> sdoDocumentationAndEvidenceList;
    @JsonProperty("sdoOtherList")
    private final List<SdoOtherEnum> sdoOtherList;

    @JsonProperty("sdoRightToAskCourt")
    private final String sdoRightToAskCourt;
    private final List<Element<PartyNameDA>> sdoPartiesRaisedAbuseCollection;
    @JsonProperty("sdoNextStepsAfterSecondGK")
    private final String sdoNextStepsAfterSecondGK;
    @JsonProperty("sdoNextStepsAllocationTo")
    private final SdoNextStepsAllocationEnum sdoNextStepsAllocationTo;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime sdoUrgentHearingDate;
    @JsonProperty("sdoUrgentHearingTimeEstimate")
    private final String sdoUrgentHearingTimeEstimate;
    private final DynamicList sdoUrgentHearingCourtDynamicList;
    @JsonProperty("sdoHearingUrgentCheckList")
    private final List<SdoHearingUrgentCheckListEnum> sdoHearingUrgentCheckList;
    @JsonProperty("sdoHearingUrgentDetails")
    private final String sdoHearingUrgentDetails;
    @JsonProperty("sdoHearingUrgentCourtConsider")
    private final String sdoHearingUrgentCourtConsider;
    @JsonProperty("sdoHearingUrgentTimeShortened")
    private final String sdoHearingUrgentTimeShortened;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoHearingUrgentMustBeServedBy;
    @JsonProperty("sdoHearingUrgentByWayOf")
    private final SdoRemoteHearingEnum sdoHearingUrgentByWayOf;
    @JsonProperty("sdoHearingNotNeeded")
    private final String sdoHearingNotNeeded;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime sdoFhdraStartDateTime;
    private final DynamicList sdoFhdraCourtDynamicList;
    @JsonProperty("sdoFhdraBeforeAList")
    private final SdoBeforeAEnum sdoFhdraBeforeAList;
    @JsonProperty("sdoFhdraByWayOf")
    private final SdoRemoteHearingEnum sdoFhdraByWayOf;
    @JsonProperty("sdoParticipationDirections")
    private final String sdoParticipationDirections;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoPositionStatementDeadlineDate;
    @JsonProperty("sdoPositionStatementWritten")
    private final SdoWrittenStatementEnum sdoPositionStatementWritten;
    @JsonProperty("sdoMiamAttendingPerson")
    private final String sdoMiamAttendingPerson;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime sdoPermissionHearingOn;
    @JsonProperty("sdoPermissionHearingTimeEstimate")
    private final String sdoPermissionHearingTimeEstimate;
    @JsonProperty("sdoPermissionHearingBeforeAList")
    private final SdoBeforeAEnum sdoPermissionHearingBeforeAList;
    @JsonProperty("sdoDirectionsDraDecideBy")
    private final SdoJudgeLaDecideByEnum sdoDirectionsDraDecideBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime sdoDirectionsDraStartDateAndTime;
    @JsonProperty("sdoDirectionsDraHearing")
    private final String sdoDirectionsDraHearing;
    private final DynamicList sdoDirectionsDraCourtDynamicList;
    @JsonProperty("sdoDirectionsDraHearingByWayOf")
    private final SdoRemoteHearingEnum sdoDirectionsDraHearingByWayOf;
    @JsonProperty("sdoSettlementConferenceList")
    private final SdoJudgeLaDecideByEnum sdoSettlementConferenceList;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    private final LocalDateTime sdoSettlementConferenceDateTime;
    @JsonProperty("sdoSettlementConferenceTimeEstimate")
    private final String sdoSettlementConferenceTimeEstimate;
    private final DynamicList sdoSettlementConferenceCourtDynamicList;
    @JsonProperty("sdoSettlementConferenceByWayOf")
    private final SdoRemoteHearingEnum sdoSettlementConferenceByWayOf;
    @JsonProperty("sdoJoiningInstructionsForRH")
    private final String sdoJoiningInstructionsForRH;
    @JsonProperty("sdoHearingAllegationsMadeBy")
    private final List<SdoApplicantRespondentEnum> sdoHearingAllegationsMadeBy;
    @JsonProperty("sdoHearingCourtHasRequested")
    private final List<SdoCourtRequestedEnum> sdoHearingCourtHasRequested;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoAllegationsDeadlineDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate sdoWrittenResponseDeadlineDate;
    @JsonProperty("sdoHearingReportsSentTo")
    private final SdoReportsSentToEnum sdoHearingReportsSentTo;
    @JsonProperty("sdoHearingReportsAlsoSentTo")
    private final List<SdoReportsAlsoSentToEnum> sdoHearingReportsAlsoSentTo;
    @JsonProperty("sdoHearingMaximumPages")
    private final String sdoHearingMaximumPages;
    private final List<Element<SdoLanguageDialect>> sdoInterpreterDialectRequired;
    @JsonProperty("sdoUpdateContactDetails")
    private final String sdoUpdateContactDetails;
}
