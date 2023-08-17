package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.EventsData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.json.stream.JsonCollectors;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CHILD_ARRANGEMENT_CASE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_EVENT_CODE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ISSUE_EVENT_SEQUENCE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C100JsonMapper {

    private final ChildrenMapper childrenMapper;

    private final ChildDetailsRevisedMapper childDetailsRevisedMapper;
    private final TypeOfApplicationMapper typeOfApplicantionMapper;
    private final HearingUrgencyMapper hearingUrgencyMapper;
    private final MiamMapper miamMapper;
    private final AllegationsOfHarmMapper allegationOfHarmMapper;
    private final AllegationsOfHarmRevisedMapper allegationsOfHarmRevisedMapper;
    private final OtherPeopleInTheCaseMapper otherPeopleInTheCaseMapper;

    private final OtherPeopleInTheCaseRevisedMapper otherPeopleInTheCaseRevisedMapper;
    private final OtherChildrenNotInTheCaseMapper otherChildrenNotInTheCaseMapper;
    private final OtherProceedingsMapper otherproceedingsMapper;
    private final AttendingTheHearingMapper attendingTheHearingMapper;
    private final InternationalElementMapper internationalElementMapper;
    private final LitigationCapacityMapper litigationCapacityMapper;
    private final CombinedMapper combinedMapper;
    private final ChildrenAndApplicantsMapper childrenAndApplicantsMapper;
    private final ChildrenAndRespondentsMapper childrenAndRespondentsMapper;
    private final ChildrenAndOtherPeopleMapper childrenAndOtherPeopleMapper;

    public JsonObject map(CaseData caseData) {
        return new NullAwareJsonObjectBuilder()
            .add("solicitor", combinedMapper.map(caseData))
            .add("header", getHeader(caseData.getCourtCodeFromFact(), caseData.getCourtName(), caseData.getId()))
            .add("id", caseData.getId())
            .add("children", TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
                ? childDetailsRevisedMapper.map(caseData.getNewChildDetails()) : childrenMapper.map(caseData.getChildren()))
                .add("childAndApplicantRelations", TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
                        ? childrenAndApplicantsMapper.map(caseData.getRelations().getChildAndApplicantRelations()) : JsonValue.EMPTY_JSON_ARRAY)
                .add("childAndRespondentRelations", TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
                        ? childrenAndRespondentsMapper.map(caseData.getRelations().getChildAndRespondentRelations()) : JsonValue.EMPTY_JSON_ARRAY)
            .add("childAndOtherPeopleRelations", TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
                ? childrenAndOtherPeopleMapper.map(caseData.getRelations().getChildAndOtherPeopleRelations()) : JsonValue.EMPTY_JSON_ARRAY)
            .add("applicants", combinedMapper.getApplicantArray())
            .add("respondents", combinedMapper.getRespondentArray())
            .add("typeOfApplication", typeOfApplicantionMapper.map(caseData))
            .add("hearingUrgency", hearingUrgencyMapper.map(caseData))
            .add("miam", miamMapper.map(caseData))
            .add("otherPeopleInTheCase", TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
                ? otherPeopleInTheCaseRevisedMapper.map(caseData.getOtherPartyInTheCaseRevised())
                : otherPeopleInTheCaseMapper.map(caseData.getOthersToNotify()))
            .add("otherChildrenNotPartOfTheApplication", otherChildrenNotInTheCaseMapper.map(caseData.getChildrenNotInTheCase()))
            .add("allegationsOfHarm", TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion()) ? allegationsOfHarmRevisedMapper
                    .map(caseData) : allegationOfHarmMapper.map(caseData))
            .add("otherProceedings", otherproceedingsMapper.map(caseData))
            .add("attendingTheHearing", attendingTheHearingMapper.map(caseData))
            .add("internationalElement", internationalElementMapper.map(caseData))
            .add("litigationCapacity", litigationCapacityMapper.map(caseData))
            .add("feeAmount", caseData.getFeeAmount())
            .add("familyManNumber", caseData.getFamilymanCaseNumber())
            .add("others", getOthers(caseData.getDateSubmitted()))
            .add("events", getEvents())
            .build();
    }

    private JsonArray getEvents() {
        List<EventsData> listEvents = new ArrayList<>();
        listEvents.add(EventsData.builder().dateReceived(String.valueOf(LocalDateTime.now())).eventCode(ISSUE_EVENT_CODE).eventDetails(
            "caseIssued").eventSequence(ISSUE_EVENT_SEQUENCE).build());
        return listEvents.stream().map(eventsData -> new NullAwareJsonObjectBuilder()
            .add("eventSequence", eventsData.getEventSequence())
            .add("eventCode", eventsData.getEventCode())
            .add("dateReceived", eventsData.getDateReceived())
            .add("eventDetails", eventsData.getEventDetails()).build()
        ).collect(JsonCollectors.toJsonArray());

    }

    private JsonObject getHeader(String courtId, String courtName, long id) {
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", id)
            .add("courtCode", courtId)
            .add("courtName", courtName)
            .add("caseType", CHILD_ARRANGEMENT_CASE)
            .build();
    }

    private JsonObject getOthers(String dateSubmitted) {
        return new NullAwareJsonObjectBuilder()
            .add("createObligation", "No")
            .add("isChildAParty", "No")
            .add("proceedingsIssued", dateSubmitted)
            .build();
    }

}
