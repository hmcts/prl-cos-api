package uk.gov.hmcts.reform.prl.services.validators;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import javax.annotation.PostConstruct;
import java.util.EnumMap;

import static uk.gov.hmcts.reform.prl.enums.Event.*;

@Service
public class EventsChecker {

    @Autowired
    CaseNameChecker caseNameChecker;

    @Autowired
    ApplicationTypeChecker applicationTypeChecker;

    @Autowired
    HearingUrgencyChecker hearingUrgencyChecker;

    @Autowired
    ApplicantsChecker applicantsChecker;

    @Autowired
    ChildChecker childChecker;

    @Autowired
    RespondentsChecker respondentsChecker;

    @Autowired
    RespondentBehaviourChecker respondentBehaviourChecker;

    @Autowired
    MiamChecker miamChecker;

    @Autowired
    AllegationsOfHarmChecker allegationsOfHarmChecker;

    @Autowired
    OtherPeopleInTheCaseChecker otherPeopleInTheCaseChecker;

    @Autowired
    OtherProceedingsChecker otherProceedingsChecker;

    @Autowired
    AttendingTheHearingChecker attendingTheHearingChecker;

    @Autowired
    InternationalElementChecker internationalElementChecker;

    @Autowired
    LitigationCapacityChecker litigationCapacityChecker;

    @Autowired
    WelshLanguageRequirementsChecker welshLanguageRequirementsChecker;

    @Autowired
    PdfChecker pdfChecker;

    @Autowired
    SubmitAndPayChecker submitAndPayChecker;

    @Autowired
    HomeChecker homeChecker;

    @Autowired
    RespondentRelationshipChecker respondentRelationshipChecker;

    @Autowired
    FL401ApplicationTypeChecker fl401ApplicationTypeChecker;

    @Autowired
    FL401ApplicantFamilyChecker fl401ApplicantFamilyChecker;

    @Autowired
    FL401StatementOfTruthAndSubmitChecker fl401StatementOfTruthAndSubmitChecker;

    @Autowired
    WithoutNoticeOrderChecker withoutNoticeOrderChecker;

    @Autowired
    UploadDocumentChecker uploadDocumentChecker;

    @Autowired
    StatementTruthSubmitChecker statementTruthSubmitChecker;




    @Autowired
    FL401OtherProceedingsChecker fl401OtherProceedingsChecker;


    private EnumMap<Event, EventChecker> eventStatus = new EnumMap<Event, EventChecker>(Event.class);

    @PostConstruct
    public void init() {
        eventStatus.put(CASE_NAME, caseNameChecker);
        eventStatus.put(TYPE_OF_APPLICATION, applicationTypeChecker);
        eventStatus.put(HEARING_URGENCY, hearingUrgencyChecker);
        eventStatus.put(APPLICANT_DETAILS, applicantsChecker);
        eventStatus.put(CHILD_DETAILS, childChecker);
        eventStatus.put(RESPONDENT_DETAILS, respondentsChecker);
        eventStatus.put(MIAM, miamChecker);
        eventStatus.put(ALLEGATIONS_OF_HARM, allegationsOfHarmChecker);
        eventStatus.put(OTHER_PEOPLE_IN_THE_CASE, otherPeopleInTheCaseChecker);
        eventStatus.put(OTHER_PROCEEDINGS, otherProceedingsChecker);
        eventStatus.put(ATTENDING_THE_HEARING, attendingTheHearingChecker);
        eventStatus.put(INTERNATIONAL_ELEMENT, internationalElementChecker);
        eventStatus.put(LITIGATION_CAPACITY, litigationCapacityChecker);
        eventStatus.put(WELSH_LANGUAGE_REQUIREMENTS, welshLanguageRequirementsChecker);
        eventStatus.put(VIEW_PDF_DOCUMENT, pdfChecker);
        eventStatus.put(SUBMIT_AND_PAY, submitAndPayChecker);

        eventStatus.put(FL401_CASE_NAME, caseNameChecker);
        eventStatus.put(FL401_HOME, homeChecker);
        eventStatus.put(RELATIONSHIP_TO_RESPONDENT, respondentRelationshipChecker);
        eventStatus.put(FL401_TYPE_OF_APPLICATION, fl401ApplicationTypeChecker);
        eventStatus.put(RESPONDENT_BEHAVIOUR, respondentBehaviourChecker);
        eventStatus.put(WITHOUT_NOTICE_ORDER, withoutNoticeOrderChecker);
        eventStatus.put(FL401_APPLICANT_FAMILY_DETAILS, fl401ApplicantFamilyChecker);
        eventStatus.put(UPLOAD_DOCUMENTS, uploadDocumentChecker);
        eventStatus.put(FL401_OTHER_PROCEEDINGS, fl401OtherProceedingsChecker);
        eventStatus.put(FL401_STATEMENT_OF_TRUTH, fl401StatementOfTruthAndSubmitChecker);

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

    public EnumMap<Event, EventChecker> getEventStatus() {
        return eventStatus;
    }
}
