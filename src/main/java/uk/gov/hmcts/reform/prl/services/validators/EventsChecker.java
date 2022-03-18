package uk.gov.hmcts.reform.prl.services.validators;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.Event;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.EnumMap;
import java.util.Map;
import javax.annotation.PostConstruct;

import static uk.gov.hmcts.reform.prl.enums.Event.ALLEGATIONS_OF_HARM;
import static uk.gov.hmcts.reform.prl.enums.Event.APPLICANT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.ATTENDING_THE_HEARING;
import static uk.gov.hmcts.reform.prl.enums.Event.CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.CHILD_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_APPLICANT_FAMILY_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_CASE_NAME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_HOME;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_SOT_AND_SUBMIT;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.FL401_UPLOAD_DOCUMENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.HEARING_URGENCY;
import static uk.gov.hmcts.reform.prl.enums.Event.INTERNATIONAL_ELEMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.LITIGATION_CAPACITY;
import static uk.gov.hmcts.reform.prl.enums.Event.MIAM;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PEOPLE_IN_THE_CASE;
import static uk.gov.hmcts.reform.prl.enums.Event.OTHER_PROCEEDINGS;
import static uk.gov.hmcts.reform.prl.enums.Event.RELATIONSHIP_TO_RESPONDENT;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_BEHAVIOUR;
import static uk.gov.hmcts.reform.prl.enums.Event.RESPONDENT_DETAILS;
import static uk.gov.hmcts.reform.prl.enums.Event.SUBMIT_AND_PAY;
import static uk.gov.hmcts.reform.prl.enums.Event.TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.enums.Event.VIEW_PDF_DOCUMENT;
import static uk.gov.hmcts.reform.prl.enums.Event.WELSH_LANGUAGE_REQUIREMENTS;
import static uk.gov.hmcts.reform.prl.enums.Event.WITHOUT_NOTICE_ORDER;

@Getter
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
    FL401OtherProceedingsChecker fl401OtherProceedingsChecker;

    private Map<Event, EventChecker> eventStatus = new EnumMap<>(Event.class);

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
        eventStatus.put(FL401_UPLOAD_DOCUMENTS, pdfChecker);
        eventStatus.put(FL401_OTHER_PROCEEDINGS, fl401OtherProceedingsChecker);
        eventStatus.put(FL401_SOT_AND_SUBMIT, fl401StatementOfTruthAndSubmitChecker);

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

    public Map<Event, EventChecker> getEventStatus() {
        return eventStatus;
    }
}
