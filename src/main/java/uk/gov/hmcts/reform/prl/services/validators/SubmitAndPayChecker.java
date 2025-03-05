package uk.gov.hmcts.reform.prl.services.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.validators.eventschecker.EventsChecker;

import java.util.EnumMap;
import java.util.Map;

import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V2;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.TASK_LIST_VERSION_V3;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_APPLICANTS;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILDREN_AND_RESPONDENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.prl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM_POLICY_UPGRADE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.WELSH_LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.prl.services.validators.EventCheckerHelper.anyNonEmpty;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SubmitAndPayChecker implements EventChecker {

    @Lazy
    private final EventsChecker eventsChecker;


    @Override
    public boolean isFinished(CaseData caseData) {
        return hasMandatoryCompleted(caseData);
    }

    @Override
    public boolean isStarted(CaseData caseData) {
        return anyNonEmpty(
            caseData.getFl401StmtOfTruth()
        );
    }

    @Override
    public boolean hasMandatoryCompleted(CaseData caseData) {

        EnumMap<Event, EventChecker> mandatoryEvents = getMandatoryEvents(caseData);
        boolean mandatoryFinished;

        EnumMap<Event, EventChecker> optionalEvents = new EnumMap<>(Event.class);
        if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
                || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
            optionalEvents.put(
                OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION,
                eventsChecker.getOtherChildrenNotPartOfTheApplicationChecker()
            );
            optionalEvents.put(
                OTHER_PEOPLE_IN_THE_CASE_REVISED,
                eventsChecker.getOtherPeopleInTheCaseRevisedChecker()
            );
            if (eventsChecker.getOtherPeopleInTheCaseRevisedChecker().hasMandatoryCompleted(caseData)
                || eventsChecker.getOtherPeopleInTheCaseRevisedChecker().isFinished(caseData)) {
                mandatoryEvents.put(
                    CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION,
                    eventsChecker.getChildrenAndOtherPeopleInThisApplicationChecker()
                );

            } else {
                optionalEvents.put(
                    CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION,
                    eventsChecker.getChildrenAndOtherPeopleInThisApplicationChecker()
                );

            }
        } else {
            optionalEvents.put(
                OTHER_PEOPLE_IN_THE_CASE,
                eventsChecker.getOtherPeopleInTheCaseChecker()
            );
        }
        optionalEvents.put(OTHER_PROCEEDINGS, eventsChecker.getOtherProceedingsChecker());
        optionalEvents.put(ATTENDING_THE_HEARING, eventsChecker.getAttendingTheHearingChecker());
        optionalEvents.put(INTERNATIONAL_ELEMENT, eventsChecker.getInternationalElementChecker());
        optionalEvents.put(LITIGATION_CAPACITY, eventsChecker.getLitigationCapacityChecker());
        optionalEvents.put(
            WELSH_LANGUAGE_REQUIREMENTS,
            eventsChecker.getWelshLanguageRequirementsChecker()
        );
        checksForMiam(caseData, optionalEvents);
        boolean optionalFinished;
        for (Map.Entry<Event, EventChecker> e : mandatoryEvents.entrySet()) {
            mandatoryFinished = e.getValue().isFinished(caseData) || e.getValue().hasMandatoryCompleted(caseData);
            if (!mandatoryFinished) {
                return false;
            }
        }

        for (Map.Entry<Event, EventChecker> e : optionalEvents.entrySet()) {
            optionalFinished = e.getValue().isFinished(caseData) || !(e.getValue().isStarted(caseData));
            if (!optionalFinished) {
                return false;
            }
        }
        return true;
    }

    private void checksForMiam(CaseData caseData, EnumMap<Event, EventChecker> optionalEvents) {
        if (TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
            if (YesOrNo.Yes.equals(caseData.getConsentOrder()) && !eventsChecker.getMiamPolicyUpgradeChecker().isStarted(caseData)) {
                optionalEvents.put(MIAM_POLICY_UPGRADE, eventsChecker.getMiamPolicyUpgradeChecker());
            }
        } else {
            if (YesOrNo.Yes.equals(caseData.getConsentOrder()) && !eventsChecker.getMiamChecker().isStarted(caseData)) {
                optionalEvents.put(MIAM, eventsChecker.getMiamChecker());
            }
        }
    }

    private EnumMap<Event, EventChecker> getMandatoryEvents(CaseData caseData) {
        EnumMap<Event, EventChecker> mandatoryEvents = new EnumMap<>(Event.class);

        mandatoryEvents.put(CASE_NAME, eventsChecker.getCaseNameChecker());
        mandatoryEvents.put(TYPE_OF_APPLICATION, eventsChecker.getApplicationTypeChecker());
        mandatoryEvents.put(HEARING_URGENCY, eventsChecker.getHearingUrgencyChecker());
        mandatoryEvents.put(APPLICANT_DETAILS, eventsChecker.getApplicantsChecker());
        if (TASK_LIST_VERSION_V2.equalsIgnoreCase(caseData.getTaskListVersion())
            || TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
            mandatoryEvents.put(CHILD_DETAILS_REVISED, eventsChecker.getChildDetailsRevisedChecker());
            mandatoryEvents.put(
                CHILDREN_AND_APPLICANTS,
                eventsChecker.getChildrenAndApplicantsChecker()
            );
            mandatoryEvents.put(
                CHILDREN_AND_RESPONDENTS,
                eventsChecker.getChildrenAndRespondentsChecker()
            );
            mandatoryEvents.put(
                ALLEGATIONS_OF_HARM_REVISED,
                eventsChecker.getAllegationsOfHarmRevisedChecker()
            );
        } else {
            mandatoryEvents.put(CHILD_DETAILS, eventsChecker.getChildChecker());
            mandatoryEvents.put(ALLEGATIONS_OF_HARM, eventsChecker.getAllegationsOfHarmChecker());
        }
        mandatoryEvents.put(RESPONDENT_DETAILS, eventsChecker.getRespondentsChecker());
        if (YesOrNo.No.equals(caseData.getConsentOrder()) || caseData.getConsentOrder() == null
            || eventsChecker.getMiamPolicyUpgradeChecker().isStarted(caseData)) {
            if (TASK_LIST_VERSION_V3.equalsIgnoreCase(caseData.getTaskListVersion())) {
                mandatoryEvents.put(MIAM_POLICY_UPGRADE, eventsChecker.getMiamPolicyUpgradeChecker());
            } else {
                mandatoryEvents.put(MIAM, eventsChecker.getMiamChecker());
            }
        }
        return mandatoryEvents;
    }

    @Override
    public TaskState getDefaultTaskState(CaseData caseData) {
        return TaskState.NOT_STARTED;
    }
}
