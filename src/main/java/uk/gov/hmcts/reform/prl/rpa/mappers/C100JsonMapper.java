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
import javax.json.stream.JsonCollectors;

import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.CHILD_ARRANGEMENT_CASE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C100JsonMapper {

    private final ChildrenMapper childrenMapper;
    private final TypeOfApplicationMapper typeOfApplicantionMapper;
    private final HearingUrgencyMapper hearingUrgencyMapper;
    private final MiamMapper miamMapper;
    private final AllegationsOfHarmMapper allegationOfHarmMapper;
    private final OtherPeopleInTheCaseMapper otherPeopleInTheCaseMapper;
    private final OtherProceedingsMapper otherproceedingsMapper;
    private final AttendingTheHearingMapper attendingTheHearingMapper;
    private final InternationalElementMapper internationalElementMapper;
    private final LitigationCapacityMapper litigationCapacityMapper;
    private final CombinedMapper combinedMapper;

    public JsonObject map(CaseData caseData) {
        return new NullAwareJsonObjectBuilder()
            .add("solicitor", combinedMapper.map(caseData))
            .add("header", getHeader(caseData.getCourtId(), caseData.getCourtName(), caseData.getId()))
            .add("id", caseData.getId())
            .add("children", childrenMapper.map(caseData.getChildren()))
            .add("applicants", combinedMapper.getApplicantArray())
            .add("respondents", combinedMapper.getRespondentArray())
            .add("typeOfApplication", typeOfApplicantionMapper.map(caseData))
            .add("hearingUrgency", hearingUrgencyMapper.map(caseData))
            .add("miam", miamMapper.map(caseData))
            .add("allegationsOfHarm", allegationOfHarmMapper.map(caseData))
            .add("otherPeopleInTheCase", otherPeopleInTheCaseMapper.map(caseData.getOthersToNotify()))
            .add("otherProceedings", otherproceedingsMapper.map(caseData))
            .add("attendingTheHearing", attendingTheHearingMapper.map(caseData))
            .add("internationalElement", internationalElementMapper.map(caseData))
            .add("litigationCapacity", litigationCapacityMapper.map(caseData))
            .add("feeAmount", caseData.getFeeAmount())
            .add("familyManNumber", caseData.getFamilymanCaseNumber())
            .add("others", getOthers(caseData.getDateSubmitted()))
            .add("events", getEvents(caseData))
            .build();
    }

    private JsonArray getEvents(CaseData caseData) {

        List<EventsData> listEvents = new ArrayList<EventsData>();
        listEvents.add(EventsData.builder().dateReceived(String.valueOf(LocalDateTime.now())).eventCode("001").eventDetails(
            "caseIssued").eventSequence("1").build());
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
