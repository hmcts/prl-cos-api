package uk.gov.hmcts.reform.prl.services.validators.eventschecker;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;
import uk.gov.hmcts.reform.prl.services.validators.EventChecker;

import java.util.EnumMap;
import java.util.Map;

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
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_APPLICANT_FAMILY_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_HOME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_RESUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_SOT_AND_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_UPLOAD_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.prl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM_POLICY_UPGRADE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE_REVISED;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.RELATIONSHIP_TO_RESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_BEHAVIOUR;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT_AND_PAY;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.VIEW_PDF_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.WELSH_LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.WITHOUT_NOTICE_ORDER;

@Getter
@Service
public class EventsChecker extends PartyChecker {

    private Map<Event, EventChecker> eventStatus = new EnumMap<>(Event.class);

    @PostConstruct
    public void init() {
        eventStatus.put(CASE_NAME, this.getCaseNameChecker());
        eventStatus.put(TYPE_OF_APPLICATION, this.getApplicationTypeChecker());
        eventStatus.put(HEARING_URGENCY, this.getHearingUrgencyChecker());
        eventStatus.put(APPLICANT_DETAILS, this.getApplicantsChecker());
        eventStatus.put(CHILD_DETAILS, this.getChildChecker());
        eventStatus.put(CHILD_DETAILS_REVISED, this.getChildDetailsRevisedChecker());
        eventStatus.put(RESPONDENT_DETAILS, this.getRespondentsChecker());
        eventStatus.put(MIAM, this.getMiamChecker());
        eventStatus.put(MIAM_POLICY_UPGRADE, this.getMiamPolicyUpgradeChecker());
        eventStatus.put(ALLEGATIONS_OF_HARM, this.getAllegationsOfHarmChecker());
        eventStatus.put(ALLEGATIONS_OF_HARM_REVISED, this.getAllegationsOfHarmRevisedChecker());
        eventStatus.put(OTHER_PEOPLE_IN_THE_CASE, this.getOtherPeopleInTheCaseChecker());
        eventStatus.put(OTHER_PEOPLE_IN_THE_CASE_REVISED, this.getOtherPeopleInTheCaseRevisedChecker());
        eventStatus.put(OTHER_PROCEEDINGS, this.getOtherProceedingsChecker());
        eventStatus.put(ATTENDING_THE_HEARING, this.getAttendingTheHearingChecker());
        eventStatus.put(INTERNATIONAL_ELEMENT, this.getInternationalElementChecker());
        eventStatus.put(LITIGATION_CAPACITY, this.getLitigationCapacityChecker());
        eventStatus.put(WELSH_LANGUAGE_REQUIREMENTS, this.getWelshLanguageRequirementsChecker());
        eventStatus.put(VIEW_PDF_DOCUMENT, this.getPdfChecker());
        eventStatus.put(SUBMIT_AND_PAY, this.getSubmitAndPayChecker());
        eventStatus.put(SUBMIT, this.getSubmitChecker());

        eventStatus.put(
            OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION,
            this.getOtherChildrenNotPartOfTheApplicationChecker()
        );
        eventStatus.put(CHILDREN_AND_APPLICANTS, this.getChildrenAndApplicantsChecker());
        eventStatus.put(CHILDREN_AND_RESPONDENTS, this.getChildrenAndRespondentsChecker());
        eventStatus.put(
            CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION,
            this.getChildrenAndOtherPeopleInThisApplicationChecker()
        );

        eventStatus.put(FL401_CASE_NAME, this.getCaseNameChecker());
        eventStatus.put(FL401_HOME, this.getHomeChecker());
        eventStatus.put(RELATIONSHIP_TO_RESPONDENT, this.getRespondentRelationshipChecker());
        eventStatus.put(FL401_TYPE_OF_APPLICATION, this.getFl401ApplicationTypeChecker());
        eventStatus.put(RESPONDENT_BEHAVIOUR, this.getRespondentBehaviourChecker());
        eventStatus.put(WITHOUT_NOTICE_ORDER, this.getWithoutNoticeOrderChecker());
        eventStatus.put(FL401_APPLICANT_FAMILY_DETAILS, this.getFl401ApplicantFamilyChecker());
        eventStatus.put(FL401_UPLOAD_DOCUMENTS, this.getPdfChecker());
        eventStatus.put(FL401_OTHER_PROCEEDINGS, this.getFl401OtherProceedingsChecker());
        eventStatus.put(FL401_SOT_AND_SUBMIT, this.getFl401StatementOfTruthAndSubmitChecker());
        eventStatus.put(FL401_RESUBMIT, this.getFl401ResubmitChecker());

    }

    public boolean isFinished(Event event, CaseData caseData) {
        return eventStatus.get(event).isFinished(caseData);
    }

    public boolean isStarted(Event event, CaseData caseData) {
        return eventStatus.get(event).isStarted(caseData);
    }

    public boolean hasMandatoryCompleted(Event event, CaseData caseData) {
        return eventStatus.get(event).hasMandatoryCompleted(caseData);
    }

    public TaskState getDefaultState(Event event, CaseData caseData) {
        return eventStatus.get(event).getDefaultTaskState(caseData);
    }

    public Map<Event, EventChecker> getEventStatus() {
        return eventStatus;
    }


}
