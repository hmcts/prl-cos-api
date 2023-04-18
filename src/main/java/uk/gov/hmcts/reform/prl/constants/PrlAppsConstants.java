package uk.gov.hmcts.reform.prl.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.State;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PrlAppsConstants {
    public static final String JURISDICTION = "PRIVATELAW";
    public static final String CASE_TYPE = "PRLAPPS";

    public static final String C100_CASE_TYPE = "C100";
    public static final String FL401_CASE_TYPE = "FL401";

    public static final String NEXT_HEARING_DETAILS = "nextHearingDetails";
    public static final String STATE = "state";


    public static final String YES = "Yes";
    public static final String NO = "No";

    public static final String APPLICATION_NOTICE_EFFORTS = "ApplicationNoticeEfforts";
    public static final String IS_APPLICATION_URGENT = "IsApplicationUrgent";
    public static final String APPLICATION_CONSIDERED_IN_DAYS_AND_HOURS = "ApplicationConsideredInDaysAndHours";
    public static final String DAYS = "days";
    public static final String HOURS = "hours";

    public static final String APPLICANT_ATTENDED_MIAM = "applicantAttendedMiam";
    public static final String CLAIMING_EXEMPTION_MIAM = "claimingExemptionMiam";

    // Fee and Pay related constants
    public static final String FEE_VERSION = "1";
    public static final Integer FEE_VOLUME = 1;
    public static final String PAYMENT_ACTION = "payment";

    public static final String BAIL_DETAILS = "bailDetails";
    public static final String APPLICANT_HAS_BAIL_END_DATE = "isRespondentAlreadyInBailCondition";
    public static final String APPLICANT_BAIL_END_DATE = "bailConditionEndDate";
    public static final String NOT_KNOWN_BAIL_END_DATE = "dontKnowBailEndDate";

    public static final String YES_SMALL = "yes";
    public static final String NO_SMALL = "no";

    public static final String DOCUMENT_FIELD_C1A = "c1ADocument";
    public static final String DOCUMENT_FIELD_DRAFT_C1A = "c1ADraftDocument";
    public static final String DOCUMENT_FIELD_C8 = "c8Document";
    public static final String DOCUMENT_FIELD_DRAFT_C8 = "c8DraftDocument";
    public static final String DOCUMENT_FIELD_FINAL = "finalDocument";
    public static final String DRAFT_DOCUMENT_FIELD = "draftOrderDoc";
    public static final String DRAFT_DOCUMENT_WELSH_FIELD = "draftOrderDocWelsh";

    public static final String C7_FINAL_ENGLISH = "c7FinalEng";

    public static final String C7_FINAL_WELSH = "c7FinalWelsh";

    public static final String COURT_NAME_FIELD = "courtName";
    public static final String COURT_SEAL_FIELD = "courtSeal";
    public static final String COURT_ID_FIELD = "courtId";
    public static final String COURT_EMAIL_ADDRESS_FIELD = "courtEmailAddress";
    public static final String FINAL_DOCUMENT_FIELD = "finalDocument";
    public static final String ISSUE_DATE_FIELD = "issueDate";
    public static final String DATE_SUBMITTED_FIELD = "dateSubmitted";
    public static final String CASE_DATE_AND_TIME_SUBMITTED_FIELD = "caseSubmittedTimeStamp";
    public static final String STATE_FIELD = "state";

    public static final String DOCUMENT_FIELD_C7 = "c7Document";
    public static final String C7_HINT = "C7";

    public static final String THIS_INFORMATION_IS_CONFIDENTIAL = "This information is to be kept confidential";

    public static final String SEARCH_RESULTS_POSTCODE_POSTCODE_SERVICE_AREA = "search/results?postcode={postcode}&serviceArea=";
    public static final String CHILD_ARRANGEMENTS_POSTCODE_URL = SEARCH_RESULTS_POSTCODE_POSTCODE_SERVICE_AREA + "childcare-arrangements";
    public static final String COURT_DETAILS_URL = "courts/{court-slug}";
    public static final String DOMESTIC_ABUSE_POSTCODE_URL = SEARCH_RESULTS_POSTCODE_POSTCODE_SERVICE_AREA + "domestic-abuse";

    public static final String DOCUMENT_FIELD_C1A_WELSH = "c1AWelshDocument";
    public static final String DOCUMENT_FIELD_C1A_DRAFT_WELSH = "c1AWelshDraftDocument";
    public static final String DOCUMENT_FIELD_C8_WELSH = "c8WelshDocument";
    public static final String DOCUMENT_FIELD_C8_DRAFT_WELSH = "c8WelshDraftDocument";
    public static final String DOCUMENT_FIELD_FINAL_WELSH = "finalWelshDocument";

    public static final String CHILD_ARRANGEMENT_CASE = "CHILD ARRANGEMENT CASE";
    public static final String ISSUE_EVENT_CODE = "001";
    public static final String ISSUE_EVENT_SEQUENCE = "1";
    public static final String BLANK_STRING = "";
    public static final String WITHOUT_NOTICE = "Without notice";

    public static final String WITH_NOTICE = "With notice";

    public static final String DRAFT_STATE = State.AWAITING_SUBMISSION_TO_HMCTS.getValue();
    public static final String RETURN_STATE = State.AWAITING_RESUBMISSION_TO_HMCTS.getValue();
    public static final String WITHDRAWN_STATE = State.CASE_WITHDRAWN.getValue();
    public static final String SUBMITTED_STATE = State.SUBMITTED_PAID.getValue();
    public static final String PENDING_STATE = State.SUBMITTED_NOT_PAID.getValue();
    public static final String ISSUED_STATE = State.CASE_ISSUED.getValue();
    public static final String JUDICIAL_REVIEW_STATE = State.JUDICIAL_REVIEW.getValue();

    public static final String C8_HINT = "C8";
    public static final String C1A_HINT = "C1A";
    public static final String C8_DRAFT_HINT = "C8_DRAFT";
    public static final String C1A_DRAFT_HINT = "C1A_DRAFT";
    public static final String FINAL_HINT = "FINAL";
    public static final String DRAFT_HINT = "DRAFT";
    public static final String DOCUMENT_COVER_SHEET_HINT = "DOC_COVER_SHEET";
    public static final String DOCUMENT_C7_DRAFT_HINT = "DOCUMENT_C7_DRAFT";
    public static final String DOCUMENT_C8_BLANK_HINT = "DOCUMENT_C8_BLANK";
    public static final String DOCUMENT_C1A_BLANK_HINT = "DOCUMENT_C1A_BLANK";
    public static final String DOCUMENT_PRIVACY_NOTICE_HINT = "PRIVACY_NOTICE";

    public static final String TEMPLATE = "template";
    public static final String FILE_NAME = "fileName";

    public static final String FINAL_TEMPLATE_NAME = "finalTemplateName";
    public static final String GENERATE_FILE_NAME = "generateFileName";

    public static final String DRAFT_TEMPLATE_WELSH = "draftTemplateWelsh";
    public static final String DRAFT_WELSH_FILE_NAME = "draftWelshFileName";

    public static final String FINAL_TEMPLATE_WELSH = "finaltemplateWelsh";
    public static final String WELSH_FILE_NAME = "welshFileName";

    public static final String URL_STRING = "/";
    public static final String D_MMMM_YYYY = "d MMMM yyyy";

    public static final String APPOINTED_GUARDIAN_FULL_NAME = "appointedGuardianFullName";

    public static final String APPLICANT_SOLICITOR = " (Applicant's Solicitor)";
    public static final String RESPONDENT_SOLICITOR = " (Respondent's Solicitor)";
    public static final String COURT_NAME = "courtName";

    public static final List<String> ROLES = List.of("caseworker-privatelaw-courtadmin",
                                                     "caseworker-privatelaw-judge",
                                                     "caseworker-privatelaw-la");

    public static final String PREVIOUS_OR_ONGOING_PROCEEDINGS = "previousOrOngoingProceedings";

    public static final String FORMAT = "%s %s";

    public static final String CITIZEN_UPLOADED_DOCUMENT = "citizenUploadedDocument";

    public static final String REVIEW_AND_SUBMIT = "reviewAndSubmit";

    public static final String CITIZEN_HINT = "CITIZEN";

    public static final String APPLICANT_SOLICITOR_EMAIL_ADDRESS = "applicantSolicitorEmailAddress";
    public static final String YOUR_POSITION_STATEMENTS = "Your position statements";
    public static final String YOUR_WITNESS_STATEMENTS = "Your witness statements";
    public static final String OTHER_WITNESS_STATEMENTS = "Other people's witness statements";
    public static final String MAIL_SCREENSHOTS_MEDIA_FILES = "Emails, screenshots, images and other media files";
    public static final String MEDICAL_RECORDS = "Medical records";

    public static final String  RESPONSE_TO_REQUEST_FOR_CA = "Reponse To Request For CA";

    public static final String LETTERS_FROM_SCHOOL = "Letters from school";
    public static final String TENANCY_MORTGAGE_AGREEMENTS = "Tenancy and mortgage agreements";
    public static final String PREVIOUS_ORDERS_SUBMITTED = "Previous orders submitted with application";
    public static final String MEDICAL_REPORTS = "Medical reports";
    public static final String PATERNITY_TEST_REPORTS = "Paternity test reports";
    public static final String DRUG_AND_ALCOHOL_TESTS = "Drug and alcohol tests (toxicology)";
    public static final String POLICE_REPORTS = "Police reports";
    public static final String OTHER_DOCUMENTS = "Other documents";

    public static final String CASE_ID = "caseId";
    public static final String DOCUMENT_TYPE = "documentType";
    public static final String PARTY_NAME = "partyName";
    public static final String DOCUMENT_ID = "documentId";
    public static final String SUBMITTED_PDF = "_submitted.pdf";
    public static final String PARENT_DOCUMENT_TYPE = "parentDocumentType";
    public static final String PARTY_ID = "partyId";
    public static final String IS_APPLICANT = "isApplicant";
    public static final String DOCUMENT_REQUEST = "documentRequestedByCourt";

    public static final String CITIZEN_UPLOAD_DOC_DATE_FORMAT = "dd-MMM-yyyy";

    public static final String CITIZEN_PRL_CREATE_EVENT = "citizenCreate";
    public static final String SERVICE_AUTHORIZATION_HEADER = "ServiceAuthorization";
    public static final String CITIZEN_ROLE = "citizen";

    public static final String OTHER = "Other";
    public static final String ENGDOCGEN = "isEngDocGen";

    public static final String SERVICE_ID = "ABA5";
    public static final String FAMILY_COURT_TYPE_ID = "18";

    public static final String RIGHT_TO_ASK_COURT = "As the direction has been made without hearing may ask the court to reconsider this order. "
        + "You must do that within seven days of receiving the order by writing to the court"
        + "(and notifying any other party) and asking the court to reconsider. "
        + "Alternatively, the court may reconsider the directions at the first hearing";
    public static final String SAFE_GUARDING_LETTER = "The court has considered the safeguarding letter from Cafcass or Cafcass Cymru "
        + "and made a decision on how to progress your case.";
    public static final String HEARING_NOT_NEEDED = "A[Judge/justices' legal adviser] has decided that appropriate directions "
        + "can be given to progress the matter without the need for a hearing";
    public static final String PARTICIPATION_DIRECTIONS = "If they not already done so, any part who considers that specific "
        + "measures need to be taken to enable a party or witness to understand the"
        + "proceedings and their role in them when in court, put their views to the  "
        + "court, instruct their representatives before, during, and after the hearing "
        + "or attend the hearing without significant distress should file an application "
        + "notice and include the following information as far as practicable:"
        + System.lineSeparator() + System.lineSeparator()
        + "a. why the party or witness would benefit from assistance;"
        + System.lineSeparator() + System.lineSeparator()
        + "b. the measure or measures that would be likely to maximise as fas as practicable the "
        + "quality of their evidence or participation and why;"
        + System.lineSeparator() + System.lineSeparator()
        + "c.written confirmations from any relevant witness of his/her views.";
    public static final String JOINING_INSTRUCTIONS = "Joining instructions";
    public static final String UPDATE_CONTACT_DETAILS = "The parties must, if their contact details have changed or missing from "
        + "the applications, contact Cafcass or Cafcass Cymru quoting the case "
        + "number at [CafcassCymruCAT@gov.wales/ privatelawapplications@cafcass.gov.uk]"
        + "The email must include telephone contact details and email address so that they "
        + "may be contacted for safeguarding purposes."
        + System.lineSeparator() + System.lineSeparator()
        + "Alternatively if any party is managing their case using the online dashboard, "
        + "they can update their contact details on the and donot have to also contact "
        + "Cafcass or Cafcass Cymru.";
    public static final String CROSS_EXAMINATION_EX740 = "Under Section 31U of the MFPA, it appears to the court that the quality of "
        + "the party's evidence on cross-examination is likely to be diminshed if the "
        + "cross examination is conducted in person, or if the conduct of cross-examination "
        + "in person would cause significant distress to a party and it would not be "
        + "contrary of justice to make the direction."
        + System.lineSeparator() + System.lineSeparator()
        + "It is ordered that:"
        + System.lineSeparator() + System.lineSeparator()
        + "a. The applicant and respondent(delete as appropriate) must notify the court by "
        + "4pm on[date] whether they intend to appoint their own qualified legal representative."
        + System.lineSeparator()
        + "b. If the applicant/respondent does not intend to appoint their own qulaified leagl "
        + "representative, they (whichever party is the (alleged) victim of domestic abuse) "
        + "must complete form EX740 (name the form) and return it to the court by 4pm on [date]";
    public static final String CROSS_EXAMINATION_QUALIFIED_LEGAL = "Should a qualified legal representative be appointed by the court "
        + System.lineSeparator()
        + "The court has considered whether it necessary in the interest of justice for the "
        + "witness(es) to be cross-examined by a qualified legal representative(s) and concluded "
        + "that it is neccessary to appoint such a qualified legal representative(s) to conduct "
        + "the cross examination."
        + System.lineSeparator() + System.lineSeparator()
        + "1. The court is to appoint a qualified leagal representaive on behalf of [name/s] "
        + "for the hearing listed on [date] at [time] at [name of court].";
    public static final String SPECIFIED_DOCUMENTS = "No document other than a document specified in an order or filled in accordance "
        + "with the Rules or any Practice Direction shall be filled without the court's permission.";
    public static final String SPIP_ATTENDANCE = "Both parents must attend the following programme at dates and at times to be confirmed "
        + "by the activity provider."
        + System.lineSeparator() + System.lineSeparator()
        + "a. The required activity is a Separated Parenting Information programme"
        + System.lineSeparator() + System.lineSeparator()
        + "b. The Court shall spend this order to Cafcass/Cafcass Cymeru with parties contact details"
        + System.lineSeparator() + System.lineSeparator()
        + "c. The activity provider must notify the Court whether the course was at the conclusion of the activity directed";
    public static final String PARENT_WITHCARE = "The application is transferred to the Family Court at [place]"
        + System.lineSeparator() + System.lineSeparator()
        + "The reason for transfer is another court is in the the area where the child usually lives / there "
        + "are on-going proceedings in another court / free-text box reason";

    public static final String DIO_RIGHT_TO_ASK =   "As the direction has been made without hearing may ask the court to reconsider this order. "
        + "You must do that within seven days of receiving the order by writing to the court"
        + "(and notifying any other party) and asking the court to reconsider. "
        + "Alternatively, the court may reconsider the directions at the first hearing";

    public static final String DIO_CASE_REVIEW =  "The case is adjourned for a case management review and directions "
        + "in the absence of the parties on [date]. You should NOT attend court on this date. On this date a judge or "
        + "legal adviser will review the file and the safeguarding letter from Cafcass/ Cafcass Cymru and decide what "
        + "type of hearing/further directions are needed to deal with the issues in the case. A copy of the directions "
        + "will be sent to the parties following the case management review with details of the next hearing and any directions. ";
    public static final String DIO_UPDATE_CONTACT_DETAILS = "The parties must, if their contact details have changed or "
        + "are missing from the application, contact Cafcass or Cafcass Cymru quoting the case number at "
        + "[CafcassCymruCAT@gov.wales / privatelawapplications@cafcass.gov.uk] The email must include telephone"
        + " contact details and email address so that they may be contacted for safeguarding purposes.  "
        + System.lineSeparator()
        + "Alternatively if any party is managing their case using the online dashboard, they can update "
        + "their contact details on the dashboard and do not have to also contact Cafcass or Cafcass Cymru.";
    public static final String DIO_SAFEGUARDING_CAFCASS = "The Court directs that Cafcass shall carry out safeguarding "
        + "checks in relation to the child(ren) and parties and file a safeguarding letter within "
        + "[number of working days)/by(insert date or time period]  of receipt of this order.  "
        + "A copy of the letter shall also be served on the parties unless, in the opinion of the "
        + "letter's author, this would create a risk of harm to a party or child.\n"
        + System.lineSeparator()
        + "A copy of this order to be served on Cafcass\n"
        + System.lineSeparator()
        + "The court has asked Cafcass to undertake some enquires to see if there are risk issues "
        + "about a child that the court needs to consider when making any decisions. Cafcass will "
        + "contact you by telephone to discuss your child's circumstances and talk about any such risk issues, "
        + "before writing a short safeguarding letter to the court to provide further advice.  "
        + "Cafcass can advise on risk issues only at this stage and so may not be able to discuss all aspects of "
        + "your case, and they won't be able to talk to your child(ren) at this stage.\n"
        + System.lineSeparator()
        + "More information about Cafcass and the work they do can be found on their website:\n"
        + System.lineSeparator()
        + "www.cafcass.gov.uk";
    public static final String DIO_SAFEGUARING_CAFCASS_CYMRU = "The Court directs that Cafcass Cymru shall carry "
        + "out safeguarding checks in relation to the child(ren) and parties and file a safeguarding letter within "
        + "[number working days})/by(insert date or time period] of receipt of this order.  A copy of the letter "
        + "shall also be served on the parties unless, in the opinion of the letter's author, this would create a "
        + "risk of harm to a party or child.\n"
        + System.lineSeparator()
        + "A copy of this order to be served on Cafcass\n"
        + System.lineSeparator()
        + "The court has asked Cafcass to undertake some enquires to see if there are risk issues about a child "
        + "that the court needs to consider when making any decisions. Cafcass will contact you by telephone to "
        + "discuss your child's circumstances and talk about any such risk issues, before writing a short safeguarding "
        + "letter to the court to provide further advice.  Cafcass can advise on risk issues only at this stage and so "
        + "may not be able to discuss all aspects of your case, and they won't be able to talk to your child(ren) at this stage.\n"
        + System.lineSeparator()
        + "More information about Cafcass and the work they do can be found on their website:\n"
        + System.lineSeparator()
        + "www.cafcass.gov.uk";
    public static final String DIO_PARENT_WITHCARE = " The parent with care lives outside this Court’s area and may "
        + "require proceedings to be transferred to their local Family Court to accommodate childcare responsibilities.\n"
        + System.lineSeparator()
        + "Any application for transfer should be made to the Court in writing within 5 working days of receipt of these directions.";
    public static final String DIO_APPLICATION_TO_APPLY_PERMISSION = " Any party intending at the FHDRA to apply for permission"
         + " to instruct an expert must first comply fully with the requirements of Practice Direction 25C of the Family Procedure Rules.";
    public static final String APPLICANT_CASE_NAME = "applicantCaseName";

    public static final String APPLICANT_OR_RESPONDENT_CASE_NAME = "applicantOrRespondentCaseName";

    public static final String CAFCASS_REPORTS = "Cafcass reports";
    public static final String EXPERT_REPORTS = "Expert reports";
    public static final String APPLICANT_STATMENT =
        "Applicant statement - for example photographic evidence, witness statement, mobile phone screenshot";


    public static final String IS_ENG_DOC_GEN = "isEngDocGen";

    public static final String IS_WELSH_DOC_GEN = "isWelshDocGen";

    public static final String INVALID_CLIENT = "Invalid Client";

    public static final String WAITING_TO_BE_LISTED = "WAITING_TO_BE_LISTED";
    public static final String LISTED = "LISTED";
    public static final String AWAITING_HEARING_DETAILS = "AWAITING_HEARING_DETAILS";
    public static final String CANCELLED = "CANCELLED";
    public static final String ADJOURNED = "ADJOURNED";
    public static final String POSTPONED = "POSTPONED";
    public static final String COMPLETED = "COMPLETED";

    public static final String DISTRICT_JUDGE = "districtJudge";

    public static final String MAGISTRATES = "magistrates";

    public static final String CIRCUIT_JUDGE = "circuitJudge";
    public static final String HIGHCOURT_JUDGE = "highCourtJudge";

    public static final String EMPTY_SPACE_STRING = " ";

    public static final String SERVICENAME = "PRIVATELAW";
    public static final String STAFFSORTCOLUMN = "lastName";
    public static final String STAFFORDERASC = "ASC";
    public static final String LEGALOFFICE = "Legal office";

    public static final String TIER_OF_JUDICIARY = "tierOfJudiciary";

    public static final String IS_JUDGE_OR_LEGAL_ADVISOR = "isJudgeOrLegalAdviser";

    public static final String JUDGE_NAME_EMAIL = "judgeNameAndEmail";
    public static final String IS_JUDGE_OR_LEGAL_ADVISOR_GATEKEEPING =  "isJudgeOrLegalAdviserGatekeeping";
    public static final String JUDGE_NAME = "judgeName";

    public static final String JUDGE = "Judge";
    public static final String CASEWORKER = "CaseWorker";

    public static final long CASE_SUBMISSION_THRESHOLD = 28;
    public static final String HEARINGTYPE = "HearingType";
    public static final String IS_HEARINGCHILDREQUIRED_Y = "Y";
    public static final String IS_HEARINGCHILDREQUIRED_N = "N";
    public static final String HEARINGCHANNEL = "HearingChannel";
    public static final String VIDEOPLATFORM = "Video";
    public static final String TELEPHONEPLATFORM = "Telephone";
    public static final String VIDEOSUBCHANNELS = "videoSubChannels";
    public static final String TELEPHONESUBCHANNELS = "telephoneSubChannels";
    public static final String IS_CAFCASS = "isCafcass";

    public static final String ORDER_HEARING_DETAILS = "ordersHearingDetails";

    public static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";
    public static final String CHILD_AND_CAFCASS_OFFICER_DETAILS = "childAndCafcassOfficers";
    public static final String CHILD_NAME = "Child name: ";
    public static final String CHILD_DETAILS_TABLE = "childDetailsTable";

    public static final String CHILDREN = "children";

    public static final String CASE_NAME_HMCTS_INTERNAL = "caseNameHmctsInternal";
    //C100 default court details
    public static final String C100_DEFAULT_COURT_NAME = "STOKE ON TRENT TRIBUNAL HEARING CENTRE";
    public static final String CITIZEN_DASHBOARD = "/dashboard";

    public static final String DATE_OF_SUBMISSION = "dateOfSubmission";
    public static final String FL_401_STMT_OF_TRUTH = "fl401StmtOfTruth";
    public static final String TESTING_SUPPORT_LD_FLAG_ENABLED = "testing-support";

    public static final String CASE_DATA_ID = "id";
    public static final String LISTWITHOUTNOTICE_HEARINGDETAILS = "listWithoutNoticeHearingDetails";

    public static final String DATE_CONFIRMED_IN_HEARINGS_TAB = "dateConfirmedInHearingsTab";
    public static final String HEARING_DATE_CONFIRM_OPTION_ENUM = "hearingDateConfirmOptionEnum";
    public static final String CONFIRMED_HEARING_DATES = "confirmedHearingDates";
    public static final String APPLICANT_HEARING_CHANNEL = "applicantHearingChannel";
    public static final String APPLICANT_SOLICITOR_HEARING_CHANNEL = "applicantSolicitorHearingChannel";
    public static final String RESPONDENT_HEARING_CHANNEL = "respondentHearingChannel";
    public static final String RESPONDENT_SOLICITOR_HEARING_CHANNEL = "respondentSolicitorHearingChannel";
    public static final String CAFCASS_HEARING_CHANNEL = "cafcassHearingChannel";
    public static final String CAFCASS_CYMRU_HEARING_CHANNEL = "cafcassCymruHearingChannel";
    public static final String HEARING_LISTED_LINKED_CASES = "hearingListedLinkedCases";
    public static final String LOCAL_AUTHORITY_HEARING_CHANNEL = "localAuthorityHearingChannel";
    public static final String COURT_LIST = "courtList";
    public static final String HEARING_VIDEO_CHANNELS = "hearingVideoChannels";
    public static final String HEARING_TELEPHONE_CHANNELS = "hearingTelephoneChannels";
    public static final String HEARING_DATE_TIMES = "hearingDateTimes";

    public static final String HEARING_ESTIMATED_HOURS = "hearingEstimatedHours";
    public static final String HEARING_ESTIMATED_MINUTES = "hearingEstimatedMinutes";
    public static final String HEARING_ESTIMATED_DAYS = "hearingEstimatedDays";
    public static final String ALL_PARTIES_ATTEND_HEARING_IN_THE_SAME_WAY = "allPartiesAttendHearingSameWayYesOrNo";

    public static final String HEARING_AUTHORITY = "hearingAuthority";
    public static final String HEARING_CHANNELS_ENUM = "hearingChannelsEnum";

    public static final String HEARING_JUDGE_NAME_AND_EMAIL  = "hearingJudgeNameAndEmail";
    public static final String HEARING_SPECIFIC_DATES_OPTIONS_ENUM = "hearingSpecificDatesOptionsEnum";
    public static final String FIRST_DATE_OF_THE_HEARING = "firstDateOfTheHearing";
    public static final String HEARING_MUST_TAKE_PLACE_AT_HOUR = "hearingMustTakePlaceAtHour";
    public static final String HEARING_MUST_TAKE_PLACE_AT_MINUTE = "hearingMustTakePlaceAtMinute";

    public static final String EARLIEST_HEARING_DATE = "earliestHearingDate";
    public static final String LATEST_HEARING_DATE = "latestHearingDate";
    public static final String HEARING_PRIORITY_TYPE_ENUM = "hearingPriorityTypeEnum";
    public static final String CUSTOM_DETAILS = "customDetails";
    public static final String[] EMPTY_ARRAY = {};
    public static final String COLON_SEPERATOR = ":";

    //PRL-3504 - pagination for RD staff data
    public static final String RD_STAFF_TOTAL_RECORDS_HEADER = "total_records";
    public static final int RD_STAFF_PAGE_SIZE = 50;
    public static final int RD_STAFF_FIRST_PAGE = 0;
    public static final int RD_STAFF_SECOND_PAGE = 1;
}
