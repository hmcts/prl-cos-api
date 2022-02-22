package uk.gov.hmcts.reform.prl.rpa.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.EventsData;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.InternationalElement;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.LitigationCapacity;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonCollectors;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.CHILD_ARRANGEMENT_CASE;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C100JsonMapper {

    private final ChildrenMapper childrenMapper;
    private final ApplicantsMapper applicantsMapper;
    private final RespondentsMapper respondentsMapper;
    private final TypeOfApplicationMapper typeOfApplicantionMapper;
    private final HearingUrgencyMapper hearingUrgencyMapper;
    private final MiamMapper miamMapper;
    private final AllegationsOfHarmMapper allegationOfHarmMapper;
    private final OtherPeopleInTheCaseMapper otherPeopleInTheCaseMapper;
    private final OtherProceedingsMapper otherproceedingsMapper;
    private final AttendingTheHearingMapper attendingTheHearingMapper;
    private final InternationalElementMapper internationalElementMapper;
    private final LitigationCapacityMapper litigationCapacityMapper;
    private final SolicitorsMapper solicitorsMapper;
    private final CaseEventService caseEventService;

    public JsonObject map(CaseData caseData) {
        return new NullAwareJsonObjectBuilder()
            .add("header", getHeader(caseData.getCourtId(), caseData.getCourtName(), caseData.getId()))
            .add("id", caseData.getId())
            .add("solicitors", solicitorsMapper.mapSolicitorList(caseData))
            .add("children", childrenMapper.map(caseData.getChildren()))
            .add("applicants", applicantsMapper.map(caseData.getApplicants()))
            .add("respondents", respondentsMapper.map(caseData.getRespondents(),caseData.getApplicants().size()))
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

    private Map<String, String> fetchSolicitor(CaseData caseData, Map<String, String> appSolMap) {
        Optional<List<Element<PartyDetails>>> applicantElementsCheck = ofNullable(caseData.getApplicants());
        if (applicantElementsCheck.isEmpty()) {
            return null;
        }
        List<PartyDetails> applicantList = caseData.getApplicants().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        AtomicInteger counter = new AtomicInteger(1);
        for (PartyDetails partyDetails : applicantList) {
            //if(partyDetails.getSolicitorOrg() != null && partyDetails.getSolicitorOrg().getOrganisationID() != null){
            appSolMap.put("" + counter.getAndIncrement(), partyDetails.getSolicitorOrg().getOrganisationID());
            // }
        }
        List<PartyDetails> respondentList = caseData.getRespondents().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        for (PartyDetails partyDetails : respondentList) {
            //if(partyDetails.getSolicitorOrg() != null && partyDetails.getSolicitorOrg().getOrganisationID() != null){
            appSolMap.put("SOL_" + counter.getAndIncrement(), partyDetails.getSolicitorOrg().getOrganisationID());
            //}
        }
        System.out.println("*******Map1*******" + appSolMap);
        /*applicantList.stream().map(applicant -> appSolMap.put("SOL_" + counter.getAndIncrement(),
            applicant.getSolicitorOrg() != null ? applicant.getSolicitorOrg().getOrganisationID() : "NO_ORG")).collect(
            Collectors.toList());
        System.out.println("List***8"+applicantList);
        System.out.println("Map2*******"+appSolMap);*/
        return appSolMap;
    }

    private JsonArray getEvents(CaseData caseData) {

        List<EventsData> listEvents= new ArrayList<EventsData>();
        listEvents.add(EventsData.builder().dateReceived(String.valueOf(LocalDateTime.now())).eventCode("001").eventDetails("caseIssued").eventSequence("1").build());
         return listEvents.stream().map(eventsData -> new NullAwareJsonObjectBuilder()
             .add("eventSequence",eventsData.getEventSequence())
             .add("eventCode",eventsData.getEventCode())
             .add("dateReceived",eventsData.getDateReceived())
             .add("eventDetails",eventsData.getEventDetails()).build()
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
