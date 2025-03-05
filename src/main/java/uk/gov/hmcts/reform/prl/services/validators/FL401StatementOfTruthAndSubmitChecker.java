package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.FL401OrderTypeEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_APPLICANT_FAMILY_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_HOME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.RELATIONSHIP_TO_RESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_BEHAVIOUR;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.WELSH_LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.WITHOUT_NOTICE_ORDER;

@Service
@RequiredArgsConstructor(onConstructor = @__({@Autowired, @Lazy}))
public class FL401StatementOfTruthAndSubmitChecker implements EventChecker {

    private final EventsChecker eventsChecker;

    @Override
    public boolean isFinished(CaseData caseData) {
        return hasMandatoryCompleted(caseData);
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return false;
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {
        EnumMap<Event, EventChecker> mandatoryEvents = new EnumMap<>(Event.class);

        mandatoryEvents.put(FL401_CASE_NAME, eventsChecker.getCaseNameChecker());
        mandatoryEvents.put(FL401_TYPE_OF_APPLICATION, eventsChecker.getFl401ApplicationTypeChecker());
        mandatoryEvents.put(WITHOUT_NOTICE_ORDER, eventsChecker.getWithoutNoticeOrderChecker());
        mandatoryEvents.put(APPLICANT_DETAILS, eventsChecker.getApplicantsChecker());
        mandatoryEvents.put(RESPONDENT_DETAILS, eventsChecker.getRespondentsChecker());
        mandatoryEvents.put(RELATIONSHIP_TO_RESPONDENT, eventsChecker.getRespondentRelationshipChecker());
        mandatoryEvents.put(FL401_APPLICANT_FAMILY_DETAILS, eventsChecker.getFl401ApplicantFamilyChecker());

        populateManadatoryEvents(caseData, mandatoryEvents);

        boolean mandatoryFinished;

        for (Map.Entry<Event, EventChecker> e : mandatoryEvents.entrySet()) {
            mandatoryFinished = e.getValue().isFinished(caseData) || e.getValue().hasMandatoryCompleted(caseData);
            if (!mandatoryFinished) {
                return false;
            }
        }

        EnumMap<Event, EventChecker> optionalEvents = new EnumMap<>(Event.class);

        optionalEvents.put(FL401_OTHER_PROCEEDINGS, eventsChecker.getFl401OtherProceedingsChecker());
        optionalEvents.put(ATTENDING_THE_HEARING, eventsChecker.getAttendingTheHearingChecker());
        optionalEvents.put(WELSH_LANGUAGE_REQUIREMENTS, eventsChecker.getWelshLanguageRequirementsChecker());

        boolean optionalFinished;

        for (Map.Entry<Event, EventChecker> e : optionalEvents.entrySet()) {
            optionalFinished = e.getValue().isFinished(caseData) || !(e.getValue().isStarted(caseData));
            if (!optionalFinished) {
                return false;
            }
        }

        return true;
    }

    private void populateManadatoryEvents(CaseData caseData, EnumMap<Event, EventChecker> mandatoryEvents) {
        Optional<TypeOfApplicationOrders> ordersOptional = ofNullable(caseData.getTypeOfApplicationOrders());

        if (ordersOptional.isEmpty() || (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)
            && ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder))) {
            mandatoryEvents.put(RESPONDENT_BEHAVIOUR, eventsChecker.getRespondentBehaviourChecker());
            mandatoryEvents.put(FL401_HOME, eventsChecker.getHomeChecker());
        } else {
            if (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.nonMolestationOrder)) {
                mandatoryEvents.put(RESPONDENT_BEHAVIOUR, eventsChecker.getRespondentBehaviourChecker());
            }
            if (ordersOptional.get().getOrderType().contains(FL401OrderTypeEnum.occupationOrder)) {
                mandatoryEvents.put(FL401_HOME, eventsChecker.getHomeChecker());
            }
        }
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }
}
