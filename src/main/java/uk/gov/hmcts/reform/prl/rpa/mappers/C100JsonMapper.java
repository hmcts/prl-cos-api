package uk.gov.hmcts.reform.prl.rpa.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseEventDetail;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.InternationalElement;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.LitigationCapacity;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;
import uk.gov.hmcts.reform.prl.services.CaseEventService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonCollectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
           .add("header",getHeader(caseData.getCourtId(),caseData.getCourtName(),caseData.getId()))
            .add("id", caseData.getId())
            .add("children", childrenMapper.map(caseData.getChildren()))
            .add("applicants", applicantsMapper.map(caseData.getApplicants()))
            .add("respondents", respondentsMapper.map(caseData.getRespondents()))
            .add("solicitors", solicitorsMapper.mapSolicitorList(caseData))
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

    private JsonObject getEvents(CaseData caseData) {

        List<CaseEventDetail> eventsForCase = caseEventService.findEventsForCase(String.valueOf(caseData.getId()));
        List<CaseEventDetail> issueEvent= eventsForCase.stream().filter(event -> event.getId().equals("paymentSuccessCallback")).collect(
            Collectors.toList());
        JsonObjectBuilder jsonObjectBuilder = new NullAwareJsonObjectBuilder();
        if(!issueEvent.isEmpty()){
            return new NullAwareJsonObjectBuilder().add("caseIssued",new NullAwareJsonObjectBuilder()
                .add("eventSequence","1")
                .add("eventCode", "001")
                .add("dateReceived",String.valueOf(issueEvent.get(0).getCreatedDate()))
                .add("eventDetails",jsonObjectBuilder.add("caseIssuedText","Case issued in CCD."))
                .add("eventDetailsText","Case issued in CCD.")
                .build()).build();
        }
        return null;
    }

    private JsonObject getHeader(String courtId, String courtName, long id) {
        return new NullAwareJsonObjectBuilder()
            .add("caseNumber", id)
            .add("courtCode", courtId)
            .add("courtName", courtName)
            .add("caseType",CHILD_ARRANGEMENT_CASE)
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
