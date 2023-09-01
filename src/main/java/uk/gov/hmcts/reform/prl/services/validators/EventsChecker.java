package uk.gov.hmcts.reform.prl.services.validators;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.tasklist.TaskState;

import java.util.EnumMap;
import java.util.Map;
import javax.annotation.PostConstruct;

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
public class EventsChecker {

    @Autowired
    private CaseNameChecker caseNameChecker;

    @Autowired
    private ApplicationTypeChecker applicationTypeChecker;

    @Autowired
    private HearingUrgencyChecker hearingUrgencyChecker;

    @Autowired
    private ApplicantsChecker applicantsChecker;

    @Autowired
    private ChildChecker childChecker;

    @Autowired
    private ChildDetailsRevisedChecker childDetailsRevisedChecker;

    @Autowired
    private RespondentsChecker respondentsChecker;

    @Autowired
    private RespondentBehaviourChecker respondentBehaviourChecker;

    @Autowired
    private MiamChecker miamChecker;

    @Autowired
    private AllegationsOfHarmChecker allegationsOfHarmChecker;

    @Autowired
    private AllegationsOfHarmRevisedChecker allegationsOfHarmRevisedChecker;

    @Autowired
    private OtherPeopleInTheCaseChecker otherPeopleInTheCaseChecker;
    @Autowired
    private OtherPeopleInTheCaseRevisedChecker otherPeopleInTheCaseRevisedChecker;
    @Autowired
    private OtherProceedingsChecker otherProceedingsChecker;

    @Autowired
    private AttendingTheHearingChecker attendingTheHearingChecker;

    @Autowired
    private InternationalElementChecker internationalElementChecker;

    @Autowired
    private LitigationCapacityChecker litigationCapacityChecker;

    @Autowired
    private WelshLanguageRequirementsChecker welshLanguageRequirementsChecker;

    @Autowired
    private PdfChecker pdfChecker;

    @Autowired
    private SubmitAndPayChecker submitAndPayChecker;

    @Autowired
    private HomeChecker homeChecker;

    @Autowired
    private RespondentRelationshipChecker respondentRelationshipChecker;

    @Autowired
    private FL401ApplicationTypeChecker fl401ApplicationTypeChecker;

    @Autowired
    private FL401ApplicantFamilyChecker fl401ApplicantFamilyChecker;

    @Autowired
    private FL401StatementOfTruthAndSubmitChecker fl401StatementOfTruthAndSubmitChecker;

    @Autowired
    private WithoutNoticeOrderChecker withoutNoticeOrderChecker;

    @Autowired
    private FL401OtherProceedingsChecker fl401OtherProceedingsChecker;

    @Autowired
    private SubmitChecker submitChecker;

    @Autowired
    private FL401ResubmitChecker fl401ResubmitChecker;

    @Autowired
    private ChildrenAndApplicantsChecker childrenAndApplicantsChecker;

    @Autowired
    private OtherChildrenNotPartOfTheApplicationChecker otherChildrenNotPartOfTheApplicationChecker;

    @Autowired
    private ChildrenAndRespondentsChecker childrenAndRespondentsChecker;

    @Autowired
    private ChildrenAndOtherPeopleInThisApplicationChecker childrenAndOtherPeopleInThisApplicationChecker;

    private Map<Event, EventChecker> eventStatus = new EnumMap<>(Event.class);

    @PostConstruct
    public void init() {
        eventStatus.put(CASE_NAME, caseNameChecker);
        eventStatus.put(TYPE_OF_APPLICATION, applicationTypeChecker);
        eventStatus.put(HEARING_URGENCY, hearingUrgencyChecker);
        eventStatus.put(APPLICANT_DETAILS, applicantsChecker);
        eventStatus.put(CHILD_DETAILS, childChecker);
        eventStatus.put(CHILD_DETAILS_REVISED, childDetailsRevisedChecker);
        eventStatus.put(RESPONDENT_DETAILS, respondentsChecker);
        eventStatus.put(MIAM, miamChecker);
        eventStatus.put(ALLEGATIONS_OF_HARM, allegationsOfHarmChecker);
        eventStatus.put(ALLEGATIONS_OF_HARM_REVISED, allegationsOfHarmRevisedChecker);
        eventStatus.put(OTHER_PEOPLE_IN_THE_CASE, otherPeopleInTheCaseChecker);
        eventStatus.put(OTHER_PEOPLE_IN_THE_CASE_REVISED, otherPeopleInTheCaseRevisedChecker);
        eventStatus.put(OTHER_PROCEEDINGS, otherProceedingsChecker);
        eventStatus.put(ATTENDING_THE_HEARING, attendingTheHearingChecker);
        eventStatus.put(INTERNATIONAL_ELEMENT, internationalElementChecker);
        eventStatus.put(LITIGATION_CAPACITY, litigationCapacityChecker);
        eventStatus.put(WELSH_LANGUAGE_REQUIREMENTS, welshLanguageRequirementsChecker);
        eventStatus.put(VIEW_PDF_DOCUMENT, pdfChecker);
        eventStatus.put(SUBMIT_AND_PAY, submitAndPayChecker);
        eventStatus.put(SUBMIT, submitChecker);

        eventStatus.put(OTHER_CHILDREN_NOT_PART_OF_THE_APPLICATION, otherChildrenNotPartOfTheApplicationChecker);
        eventStatus.put(CHILDREN_AND_APPLICANTS, childrenAndApplicantsChecker);
        eventStatus.put(CHILDREN_AND_RESPONDENTS, childrenAndRespondentsChecker);
        eventStatus.put(CHILDREN_AND_OTHER_PEOPLE_IN_THIS_APPLICATION, childrenAndOtherPeopleInThisApplicationChecker);

        eventStatus.put(FL401_CASE_NAME, caseNameChecker);
        eventStatus.put(FL401_HOME, homeChecker);
        eventStatus.put(RELATIONSHIP_TO_RESPONDENT, respondentRelationshipChecker);
        eventStatus.put(FL401_TYPE_OF_APPLICATION, fl401ApplicationTypeChecker);
        eventStatus.put(RESPONDENT_BEHAVIOUR, respondentBehaviourChecker);
        eventStatus.put(WITHOUT_NOTICE_ORDER, withoutNoticeOrderChecker);
        eventStatus.put(FL401_APPLICANT_FAMILY_DETAILS, fl401ApplicantFamilyChecker);
        eventStatus.put(FL401_UPLOAD_DOCUMENTS, pdfChecker);
        eventStatus.put(FL401_OTHER_PROCEEDINGS, fl401OtherProceedingsChecker);
        eventStatus.put(FL401_SOT_AND_SUBMIT, fl401StatementOfTruthAndSubmitChecker);
        eventStatus.put(FL401_RESUBMIT, fl401ResubmitChecker);

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

    public TaskState getDefaultState(Event event,CaseData caseData) {
        return eventStatus.get(event).getDefaultTaskState(caseData);
    }

    public Map<Event, EventChecker> getEventStatus() {
        return eventStatus;
    }
}
