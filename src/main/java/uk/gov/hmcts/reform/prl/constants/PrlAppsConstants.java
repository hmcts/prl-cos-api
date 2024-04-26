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

    public static final String MPU_CHILD_INVOLVED_IN_MIAM = "mpuChildInvolvedInMiam";
    public static final String MPU_APPLICANT_ATTENDED_MIAM = "mpuApplicantAttendedMiam";
    public static final String MPU_CLAIMING_EXEMPTION_MIAM = "mpuClaimingExemptionMiam";

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

    public static final String C7_FINAL_RESPONDENT = "c7FinalEng";

    public static final String DRAFT_APPLICATION_DOCUMENT_FIELD = "submitAndPayDownloadApplicationLink";

    public static final String DRAFT_APPLICATION_DOCUMENT_WELSH_FIELD = "submitAndPayDownloadApplicationWelshLink";

    public static final String C7_FINAL_ENGLISH = "c7FinalEng";

    public static final String C7_FINAL_WELSH = "c7FinalWelsh";

    public static final String COURT_NAME_FIELD = "courtName";
    public static final String COURT_SEAL_FIELD = "courtSeal";
    public static final String TRANSFERRED_COURT_FROM = "transferredCourtFrom";
    public static final String COURT_ID_FIELD = "courtId";
    public static final String COURT_EMAIL_ADDRESS_FIELD = "courtEmailAddress";
    public static final String FINAL_DOCUMENT_FIELD = "finalDocument";
    public static final String ISSUE_DATE_FIELD = "issueDate";
    public static final String DATE_SUBMITTED_FIELD = "dateSubmitted";
    public static final String CASE_DATE_AND_TIME_SUBMITTED_FIELD = "caseSubmittedTimeStamp";
    public static final String STATE_FIELD = "state";
    public  static final String SERVED_PARTY_APPLICANT = "Applicant";
    public  static final String SERVED_PARTY_RESPONDENT = "Respondent";
    public  static final String SERVED_PARTY_RESPONDENT_SOLICITOR = "Respondent solicitor";
    public  static final String SERVED_PARTY_OTHER = "Other party";
    public  static final String SERVED_PARTY_CAFCASS = "cafcass";
    public  static final String SERVED_PARTY_APPLICANT_SOLICITOR = "Applicant solicitor";
    public  static final String SERVED_PARTY_CAFCASS_CYMRU = "Cafcass cymru";
    public  static final String SERVED_PARTY_LOCAL_AUTHORITY = "Local Authority";
    public  static final String SERVED_PARTY_OTHER_ORGANISATION = "Other organisation";
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
    public static final String READY_FOR_DELETION_STATE = State.READY_FOR_DELETION.getValue();
    public static final String SUBMITTED_STATE = State.SUBMITTED_PAID.getValue();
    public static final String PENDING_STATE = State.SUBMITTED_NOT_PAID.getValue();
    public static final String ISSUED_STATE = State.CASE_ISSUED.getValue();
    public static final String JUDICIAL_REVIEW_STATE = State.JUDICIAL_REVIEW.getValue();

    public static final String C8_HINT = "C8";
    public static final String C1A_HINT = "C1A";
    public static final String C8_DRAFT_HINT = "C8_DRAFT";
    public static final String C8_RESP_DRAFT_HINT = "C8_RESPONDENT_DRAFT";
    public static final String C8_RESP_FINAL_HINT = "C8_RESPONDENT_FINAL";
    public static final String C8_RESP_FL401_FINAL_HINT = "C8_RESPONDENT_FL401_FINAL";
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
    public static final String D_MMM_YYYY = "d MMM yyyy";

    public static final String APPOINTED_GUARDIAN_FULL_NAME = "appointedGuardianFullName";

    public static final String APPLICANT_SOLICITOR = " (Applicant's Solicitor)";
    public static final String RESPONDENT_SOLICITOR = " (Respondent's Solicitor)";
    public static final String COURT_NAME = "courtName";

    public static final String COURT_ADMIN_ROLE = "caseworker-privatelaw-courtadmin";

    public static final String JUDGE_ROLE = "caseworker-privatelaw-judge";

    public static final String LEGAL_ADVISER_ROLE = "caseworker-privatelaw-la";

    public static final List<String> ROLES = List.of(COURT_ADMIN_ROLE,
                                                     JUDGE_ROLE,
                                                     LEGAL_ADVISER_ROLE);
    public static final String PREVIOUS_OR_ONGOING_PROCEEDINGS = "previousOrOngoingProceedings";

    public static final String FORMAT = "%s %s";

    public static final String CITIZEN_UPLOADED_DOCUMENT = "citizenUploadedDocument";

    public static final String REVIEW_AND_SUBMIT = "reviewAndSubmit";

    public static final String CITIZEN_HINT = "CITIZEN";

    public static final String APPLICANT_SOLICITOR_EMAIL_ADDRESS = "applicantSolicitorEmailAddress";
    public static final String YOUR_POSITION_STATEMENTS = "Your position statements";
    public static final String POSITION_STATEMENTS = "Position statements";
    public static final String YOUR_WITNESS_STATEMENTS = "Your witness statements";
    public static final String OTHER_WITNESS_STATEMENTS = "Other people's witness statements";

    public static final String OTHER_WITNESS_STATEMENTS_DOCUMENT = "Other witness Statements";
    public static final String MAIL_SCREENSHOTS_MEDIA_FILES = "Emails, screenshots, images and other media files";
    public static final String MEDICAL_RECORDS = "Medical records";

    public static final String MEDICAL_RECORDS_DOCUMENT = "Medical Records";

    public static final String  RESPONSE_TO_REQUEST_FOR_CA = "Reponse To Request For CA";

    public static final String LETTERS_FROM_SCHOOL = "Letters from school";
    public static final String TENANCY_MORTGAGE_AGREEMENTS = "Tenancy and mortgage agreements";
    public static final String PREVIOUS_ORDERS_SUBMITTED = "Previous orders submitted with application";
    public static final String MEDICAL_REPORTS = "Medical reports";
    public static final String PATERNITY_TEST_REPORTS = "Paternity test reports";
    public static final String DRUG_AND_ALCOHOL_TESTS = "Drug and alcohol tests (toxicology)";

    public static final String DRUG_AND_ALCOHOL_TESTS_DOCUMENT = "Drug and alcohol test (toxicology)";
    public static final String POLICE_REPORTS = "Police reports";

    public static final String POLICE_REPORT_DOCUMENT = "Police report";
    public static final String OTHER_DOCUMENTS = "Other documents";

    public static final String CASE_ID = "caseId";
    public static final String DOCUMENT_TYPE = "documentType";
    public static final String PARTY_NAME = "partyName";
    public static final String PARTY_TYPE = "partyType";
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
    public static final String[] HEARING_PAGE_NEEDED_ORDER_IDS = {"noticeOfProceedings","childArrangementsSpecificProhibitedOrder",
        "occupation","nonMolestation","amendDischargedVaried","noticeOfProceedingsNonParties","noticeOfProceedingsParties", "blank"};
    public static final String RIGHT_TO_ASK_COURT = "As the direction has been made without a hearing,"
        + " you may ask the court to reconsider this order. "
        + "You must do that within 7 days of receiving this order by writing to the court"
        + "(and notifying any other party) and asking the court to reconsider."
        + System.lineSeparator() + System.lineSeparator()
        + "Alternatively, the court may reconsider the directions at the first hearing.";

    public static final String SAFE_GUARDING_LETTER = "The case is adjourned for a case management review and directions in the "
        + "absence of the parties on (date). You should NOT attend court on this date. On this date a judge or legal adviser will "
        + "review the file and the safeguarding letter from Cafcass/ Cafcass Cymru and decide what type of hearing/further directions "
        + "are needed to deal with the issues in the case. A copy of the directions will be sent to the parties following the case "
        + "management review with details of the next hearing and any directions. ";
    public static final String HEARING_NOT_NEEDED = "A (Judge/justices' legal adviser) has decided that appropriate directions "
        + "can be given to progress the matter without the need for a hearing.";
    public static final String PARTICIPATION_DIRECTIONS = "If they have not already done so, any party who considers special "
        + "measures need to be taken should file an application notice."
        + System.lineSeparator() + System.lineSeparator()
        + "Special measures may need to be taken to enable a party or witness to:"
        + System.lineSeparator() + System.lineSeparator()
        + "a) understand the proceedings, and their role in them when in court"
        + System.lineSeparator()
        + "b) put their views to the court"
        + System.lineSeparator()
        + "c) instruct their representatives before, during, and after the hearing"
        + System.lineSeparator()
        + "d) attend the hearing without significant distress"
        + System.lineSeparator() + System.lineSeparator()
        + "The application notice should include:"
        + System.lineSeparator() + System.lineSeparator()
        + "a) details of why the party or witness would benefit from assistance"
        + System.lineSeparator()
        + "b) the measure or measures that would be likely to maximise (as far as practicable) "
        + "the quality of their evidence or participation"
        + System.lineSeparator()
        + "c) written confirmation from any relevant witness of his or her views";

    public static final String JOINING_INSTRUCTIONS = "Joining instructions";
    public static final String UPDATE_CONTACT_DETAILS = "The parties must, if their contact details have changed or missing from "
        + "the applications, contact Cafcass or Cafcass Cymru quoting the case "
        + "number at [CafcassCymruCAT@gov.wales/ privatelawapplications@cafcass.gov.uk]"
        + System.lineSeparator() + System.lineSeparator()
        + "The email must include telephone contact details and an email address so that they "
        + "may be contacted for safeguarding purposes."
        + System.lineSeparator() + System.lineSeparator()
        + "Alternatively if any party is managing their case using the online dashboard, "
        + "they can update their contact details on the dashboard and do not have to also contact "
        + "Cafcass or Cafcass Cymru.";
    public static final String CROSS_EXAMINATION_EX740 = "Under Section 31U of the Matrimonial and Family Proceedings Act 1984 (MFPA),"
        + " it appears to the court that the quality of "
        + "the party's evidence on cross-examination is likely to be diminished if:"
        + System.lineSeparator()
        + "a) the cross-examination is conducted in person, or"
        + "b) if the conduct of cross-examination "
        + "in person would cause significant distress to a party and it would not be "
        + "contrary of justice to make the direction."
        + System.lineSeparator() + System.lineSeparator()
        + "It is ordered that:"
        + System.lineSeparator() + System.lineSeparator()
        + "a. The ((applicant / respondent / both)) must notify the court by "
        + "4pm ((date that the deadline has been set)) whether they intend to appoint their own qualified legal representative."
        + System.lineSeparator()
        + "b. If the ((applicant / respondent)) does not intend to appoint their own qualified legal "
        + "representative, they (whichever party is the (alleged) victim of domestic abuse) "
        + "must complete form EX740 and return it to the court by 4pm on ((date that the deadline has been set))";
    public static final String CROSS_EXAMINATION_QUALIFIED_LEGAL = "Should a qualified legal representative be appointed by the court "
        + System.lineSeparator()
        + "The court has considered whether it necessary in the interest of justice for the "
        + "witness(es) to be cross-examined by a qualified legal representative(s) and concluded "
        + "that it is necessary to appoint such a qualified legal representative(s) to conduct "
        + "the cross-examination."
        + System.lineSeparator() + System.lineSeparator()
        + "The court is to appoint a qualified legal representative on behalf of [name/s] "
        + "for the hearing listed on [date] at [time] at [name of court].";
    public static final String SPECIFIED_DOCUMENTS = "No document other than a document specified in an order or filed in accordance "
        + "with the Rules or any Practice Direction shall be filed without the court's permission.";
    public static final String SPIP_ATTENDANCE = "Both parents must attend the following programme, at dates and at times to be confirmed "
        + "by the activity provider."
        + System.lineSeparator() + System.lineSeparator()
        + "Note that:"
        + System.lineSeparator() + System.lineSeparator()
        + "a. the required activity is a Planning Together for Children/Working Together for Children (WT4C)"
        + System.lineSeparator() + System.lineSeparator()
        + "b. a working together for children course (WT4C) - name for SPIP in Wales."
        + System.lineSeparator() + System.lineSeparator()
        + "c. the court shall send this order to Cafcass or Cafcass Cymru with the parties’ contact details"
        + System.lineSeparator() + System.lineSeparator()
        + "d. the activity provider must notify the Court whether the course was completed at the conclusion of the activity directed";
    public static final String PARENT_WITHCARE = "The application is transferred to the Family Court at [place]."
        + System.lineSeparator() + System.lineSeparator()
        + "The reason for transfer is another court is in the area where the child usually lives / there are on-going "
        + "proceedings in another court / or other reason";

    public static final String DIO_RIGHT_TO_ASK =   "As the direction has been made without hearing, you may ask the court to reconsider this order. "
        + "You must do that within seven days of receiving the order by writing to the court"
        + "(and notifying any other party) and asking the court to reconsider. "
        + "Alternatively, the court may reconsider the directions at the first hearing";

    public static final String LOCAL_AUTHORUTY_LETTER =   "The Local Authority are directed to file a brief report "
        + "confirming whether the children are "
        + System.lineSeparator()
        + "(a) on the child protection register, or " + System.lineSeparator()
        + "(b) open to a care and support plan or any other support from the Local Authority. or " + System.lineSeparator()
        + "(c) any current work / child protection plans being undertaken, " + System.lineSeparator()
        + "(d) any recommendations regarding interim arrangements for the child, " + System.lineSeparator()
        + "but only where a recent assessment has been undertaken and current issues have been considered.";

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
    public static final String DIO_PARTICIPATION_DIRECTION = "If they have not already done so, any party who considers that specific "
        + "measures need to be taken to enable a party or witness to understand the proceedings and their "
        + "role in them when in court, put their views to the court, instruct their representatives before, "
        + "during, and after the hearing or attend the hearing without significant distress should file an application "
        + "notice and include the following information as far as practicable:"
        + System.lineSeparator()
        + "a. why the party or witness would benefit from assistance; "
        + System.lineSeparator()
        + "b.the measure or measures that would be likely to maximise as far as practicable the quality of their evidence or participation and why;"
        + System.lineSeparator()
        + "c.written confirmation from any relevant witness of his/her views";
    public static final String DIO_PERMISSION_HEARING_DIRECTION = "a. Whether the application should be dismissed;"
        + System.lineSeparator()
        + "b. If the application is not dismissed, further directions;"
        + System.lineSeparator()
        + "c. Whether an order under section 91 (14) of the Children Act 1989 should be made (such orders"
        + " prevent the making of further applications without the court’s permission)."
        + System.lineSeparator()
        + "The Court or Applicant’s solicitor must serve the respondent by personal service/if unable to do "
        + "this text messaging or email. In order to ensure that notice is given and ensure the hearing is "
        + "effective.Please note that the Court will not consider making a Child Arrangements Order at that hearing "
        + "without CAFCASS or CAFCASS Cymru safeguarding information.The parties and/or their respective legal representatives are "
        + "required by the court to attend, remotely if necessary, a pre hearing discussions/negotiations to enable an effective "
        + "commencement of the hearing at the allotted time.If either party fails to attend orders could be made in their absence.";

    public static final String DIO_POSITION_STATEMENT_DIRECTION = "Each party’s statement must set out:"
        + System.lineSeparator()
        + "a. Any relevant background information;"
        + System.lineSeparator()
        + "b.The party’s position in relation to the Section 7 / Section 37 report"
        + System.lineSeparator()
        + "c. Any relevant facts they are asking the court to decide about matters which are not agreed;"
        + System.lineSeparator()
        + "d. And the party’s proposals for the arrangements for the child[ren]";

    public static final String SDO_PERMISSION_HEARING = "List for hearing before district judge on ((date of hearing))"
        + " for ((hours of hearing)) before ((tier of judge)) for the court to consider:"
        + System.lineSeparator()
        + System.lineSeparator()
        + "a) whether the application should be dismissed"
        + System.lineSeparator()
        + "b) if the application is not dismissed, any further directions "
        + System.lineSeparator()
        + "c) whether an order under section 91 (14) of the Children Act 1989 should be made. "
        + "Such orders prevent the making of further applications without the court’s permission";

    public static final String SDO_CROSS_EXAMINATION_EX741 = "Under Section 31U of the MFPA, it appears to the court "
        + "that the quality of the party’s evidence on cross-examination is likely to be diminished if the "
        + "cross-examination is conducted in person, or if the conduct of cross-examination in person would cause "
        + "significant distress to a party, and it would not be contrary to the interests of justice to make the direction."
        + System.lineSeparator()
        + System.lineSeparator()
        + "It is ordered that:"
        + System.lineSeparator()
        + System.lineSeparator()
        + "a. The applicant and respondent (delete as appropriate) must notify the court by 4pm on [date] "
        + "whether they intend to appoint their own qualified legal representative.\n"
        + System.lineSeparator()
        + "b. If the applicant/respondent does not intend to appoint their own qualified legal representative, "
        + "they (whichever party is the (alleged) perpetrator of domestic abuse) must complete form EX741 (name the form) "
        + "and return it to the court by 4pm on [date]";

    public static final String CAFCASS_NEXT_STEPS_CONTENT = "a)  a completed letter  "
        + System.lineSeparator()
        + "b)  enhanced checks in respect of ((party’s / parties’ names separated by commas))"
        + System.lineSeparator()
        + "c)  a risk assessment in respect of ((party’s / parties’ names separated by commas))";

    public static final String CAFCASS_CYMRU_NEXT_STEPS_CONTENT = "CAFCASS Cymru must send a safeguarding enquiries report "
        + "to the court and to the parties [and their solicitors if details are available], unless considered inappropriate by "
        + "CAFCASS Cymru, by no later than 6 weeks after receipt of this order.";

    public static final String SECTION7_EDIT_CONTENT = "a)  with whom the ((child or children)) should live"
        + System.lineSeparator()
        + "b)  whether the ((child or children)) should see the other parent [or ((name of guardian))"
        + System.lineSeparator()
        + "c)  how often and for how long the ((child or children)) should see the other parent [or ((name of guardian))"
        + System.lineSeparator()
        + "d)  the wishes and feelings of the ((child or children)) so far as they can be ascertained"
        + System.lineSeparator()
        + "e)  the home conditions and suitability of the accommodation of ((name of parent or guardian))"
        + System.lineSeparator()
        + "f)  the concerns of ((name of applicant)) with regard to ((name of respondent))"
        + System.lineSeparator()
        + "g)  whether or not the ((child or children))’s ((physical / emotional / educational)) needs are being met by "
        + "((the parents OR insert name of guardian))."
        + System.lineSeparator()
        + "h)  how the ((child or children)) would be affected by the proposed change of ((insert what is changing e.g. "
        + "change of living arrangements))."
        + System.lineSeparator()
        + "i)  whether or not it appears that the ((child or children)) ((has OR have)) suffered or are at risk of "
        + "suffering ((any harm OR the harm alleged by)) ((name of person who made allegations of harm))."
        + System.lineSeparator()
        + "j)  the parenting capacity of ((name of parent)) with regard to the allegations or findings made by "
        + "(( name of other party))"
        + System.lineSeparator()
        + "k)  whether the local authority should be requested to report under section 37 of the Children Act 1989";

    public static final String SECTION7_INTERIM_ORDERS_FACTS = "With regard to the allegations of domestic abuse if proved"
        + System.lineSeparator()
        + "a)  the impact on the ((child or children)) and on the care given by the ((parent OR guardian)) making the "
        + "allegation of domestic abuse, if a contact order is made"
        + System.lineSeparator()
        + "b)  the risk of harm if a contact order is made - whether physical, emotional or psychological"
        + System.lineSeparator()
        + "c)  whether contact between the ((child or children)) and the ((mother or father)) can take place safely "
        + "(physically, emotionally and psychologically) for the child and the parent with whom the child is living"
        + System.lineSeparator()
        + "d)  whether contact should be supervised or supported and, if so, where and by whom and the availability of "
        + "resources for that purpose"
        + System.lineSeparator()
        + "e)  if direct contact is not appropriate, whether there should be indirect contact and, if so, in what form";

    public static final String SECTION7_DA_OCCURED = "With regard to the findings of fact made as set out in the schedule to "
        + "((this order / the order made on)) ((date that the order was made)):"
        + System.lineSeparator()
        + "a)  any harm suffered by the ((child or children)) and the parent with whom the ((child or children)) are "
        + "living as a consequence of the domestic abuse found"
        + System.lineSeparator()
        + "b)  any harm which the ((child or children)) and the parent with whom the ((child or children)) are living is"
        + " at risk of suffering if a contact order is made"
        + System.lineSeparator()
        + "c)  information about the facilities available locally (including domestic abuse support services) to assist"
        + " any party or the ((child or children))"
        + System.lineSeparator()
        + "d) recommendations in respect of arrangements for the ((child or children)) including stepped arrangements "
        + "with a view to a final order if possible";


    public static final String CROSS_EXAMINATION_PROHIBITION = "a)  Section 31R the vulnerable party is the victim of a"
        + " specified domestic abuse offence perpetrated by the other party"
        + System.lineSeparator()
        + "b)  Section 31S the vulnerable party is protected by an on-notice protective injunction against the other party"
        + System.lineSeparator()
        + "c)  Section 31T the vulnerable party adduces specified evidence that they are a victim of domestic abuse perpetrated by the other party";


    public static final String AFTER_SECOND_GATEKEEPING = "The court has considered the safeguarding letter from Cafcass or Cafcass "
        + System.lineSeparator()
        + "Cymru and made a decision on how to progress your case.";

    public static final String SECTION_7_FACTS_EDIT_CONTENT = "a) the impact on the ((child or children)) and on the care given by the "
        + "((parent OR guardian)) making the allegation of domestic abuse, if a contact order is made"
        + System.lineSeparator()
        + "b) the risk of harm if a contact order is made - whether physical, emotional or psychological"
        + System.lineSeparator()
        + "c) whether contact between the ((child or children)) and the ((mother or father)) can take place safely (physically,"
        + " emotionally and psychologically) for the child and the parent with whom the child is living"
        + System.lineSeparator()
        + "d) whether contact should be supervised or supported and, if so, where and by whom and the availability of resources for that purpose"
        + System.lineSeparator()
        + "e) if direct contact is not appropriate, whether there should be indirect contact and, if so, in what form";

    public static final String SECTION_7_DA_OCCURED_EDIT_CONTENT = "a) any harm suffered by the ((child or children)) and "
        + "the parent with whom the ((child or children)) are living as a consequence "
        + "of the domestic abuse found"
        + System.lineSeparator()
        + "b) any harm which the ((child or children)) and the parent with whom the ((child or children)) "
        + "are living is at risk of suffering if a contact order is made"
        + System.lineSeparator()
        + "c) information about the facilities available locally (including domestic abuse support services) to assist any party "
        + "or the ((child or children))"
        + System.lineSeparator()
        + "d) recommendations in respect of arrangements for the ((child or children)) including stepped arrangements with a view "
        + "to a final order if possible";

    public static final String WHO_MADE_ALLEGATIONS_TEXT = "a) The list must be signed and dated. Each incident must be numbered and set "
        + "out in date order, giving the date each incident happened so far as possible and giving a brief description of what "
        + "happened and where. If someone else was there at the time of the incident, and/or the incident was reported to the police "
        + "or medical attention was sought, details should be given."
        + System.lineSeparator()
        + "b) If possible, the list of allegations shall be set out in the form of a table with headings. An example table is attached to this order."
        + System.lineSeparator()
        + "c) The list must end with a statement that it is true, and be signed and dated."
        + System.lineSeparator()
        + "d) A statement must be sent with the list, giving more details of the allegations, referring to each allegation by its number in the list "
        + "and attaching all evidence relied upon in support."
        + System.lineSeparator()
        + "e) If there were other witnesses to the alleged incidents, or there are documents on which a party intends to rely, that should be "
        + "referred to in the last paragraph of the statement but no additional witnesses or documents should be sent at this stage."
        + System.lineSeparator()
        + "f) In addition, the party making the allegations and any witnesses must make written statements of the evidence they will give to "
        + "the court about the disputed facts, attaching copies of any documents they wish to use to support their evidence. Copies of the "
        + "statements and any documents must be sent with the lists.";

    public static final String WHO_NEEDS_TO_RESPOND_ALLEGATIONS_TEXT = "a) The written response to each of the allegations should use the same "
        + "table if possible. It should set out brief details of what the party who made the allegations says happened, or if the incident is "
        + "denied. The response must end with a statement that it is true, and must be signed and dated."
        + System.lineSeparator()
        + "b) A statement giving more details of the responses, referring to each allegation by its number in the list."
        + System.lineSeparator()
        + "c) If there were other witnesses to the alleged incidents, or there are documents on which a party intends to rely, that should be "
        + "referred to in the last paragraph of the statement but no additional witnesses or documents should be sent at this stage."
        + System.lineSeparator()
        + "d) In addition, the party who needs to respond to the allegations made and any witnesses must make written statements of the evidence "
        + "they will give to the court about the disputed facts, attaching any documents they wish to use to support their evidence. Copies of the"
        + " statements and any documents must be sent with the lists.";


    public static final String APPLICANT_CASE_NAME = "applicantCaseName";

    public static final String APPLICANT_OR_RESPONDENT_CASE_NAME = "applicantOrRespondentCaseName";

    public static final String CAFCASS_REPORTS = "Cafcass reports";
    public static final String EXPERT_REPORTS = "Expert reports";
    public static final String APPLICANTS_STATEMENTS = "Applicant's statements";
    public static final String RESPONDENTS_STATEMENTS = "Respondent's statements";

    public static final String APPLICANT_STATMENT =
        "Applicant statement - for example photographic evidence, witness statement, mobile phone screenshot";

    public static final String IS_ENG_DOC_GEN = "isEngDocGen";

    public static final String IS_WELSH_DOC_GEN = "isWelshDocGen";

    public static final String INVALID_CLIENT = "Invalid Client";

    public static final String WAITING_TO_BE_LISTED = "WAITING_TO_BE_LISTED";
    public static final String LISTED = "LISTED";
    public static final String AWAITING_HEARING_DETAILS = "AWAITING_ACTUALS";
    public static final String CANCELLED = "CANCELLED";
    public static final String ADJOURNED = "ADJOURNED";
    public static final String POSTPONED = "POSTPONED";
    public static final String COMPLETED = "COMPLETED";

    public static final String COURTNAV = "COURTNAV";
    public static final String NA_COURTNAV = "NA_COURTNAV";

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

    public static final String IS_SPECIFIC_JUDGE_OR_LA_NEEDED = "isSpecificJudgeOrLegalAdviserNeeded";

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
    public static final String DIO_CASEREVIEW_HEARING_DETAILS = "dioCaseReviewHearingDetails";
    public static final String DIO_FHDRA_HEARING_DETAILS = "dioFhdraHearingDetails";
    public static final String DIO_PERMISSION_HEARING_DETAILS = "dioPermissionHearingDetails";
    public static final String DIO_URGENT_FIRST_HEARING_DETAILS = "dioUrgentFirstHearingDetails";
    public static final String DIO_URGENT_HEARING_DETAILS = "dioUrgentHearingDetails";
    public static final String DIO_WITHOUT_NOTICE_HEARING_DETAILS = "dioWithoutNoticeHearingDetails";
    public static final String CASE_TYPE_OF_APPLICATION = "caseTypeOfApplication";
    public static final String CHILD_AND_CAFCASS_OFFICER_DETAILS = "childAndCafcassOfficers";
    public static final String CHILD_NAME = "Child name: ";
    public static final String CHILD_DETAILS_TABLE = "childDetailsTable";

    public static final String CHILDREN = "children";

    public static final String CASE_NAME_HMCTS_INTERNAL = "caseNameHmctsInternal";
    //C100 default court details
    public static final String C100_DEFAULT_COURT_NAME = "STOKE ON TRENT TRIBUNAL HEARING CENTRE";
    public static final String SOLICITOR_C7_DRAFT_DOCUMENT = "SOLICITOR_C7_DRAFT";
    public static final String SOLICITOR_C7_FINAL_DOCUMENT = "SOLICITOR_C7_FINAL";
    public static final String SOLICITOR_C7_WELSH_DRAFT_DOCUMENT = "SOLICITOR_C7_WELSH_DRAFT";
    public static final String SOLICITOR_C7_WELSH_FINAL_DOCUMENT = "SOLICITOR_C7_WELSH_FINAL";
    public static final String SOLICITOR_C1A_DRAFT_DOCUMENT = "SOLICITOR_C1A_DRAFT";
    public static final String SOLICITOR_C1A_FINAL_DOCUMENT = "SOLICITOR_C1A_FINAL";
    public static final String SOLICITOR_C1A_WELSH_DRAFT_DOCUMENT = "SOLICITOR_C1A_WELSH_DRAFT";
    public static final String SOLICITOR_C1A_WELSH_FINAL_DOCUMENT = "SOLICITOR_C1A_WELSH_FINAL";
    public static final String CITIZEN_DASHBOARD = "/dashboard";

    public static final String DATE_OF_SUBMISSION = "dateOfSubmission";
    public static final String FL_401_STMT_OF_TRUTH = "fl401StmtOfTruth";
    public static final String TESTING_SUPPORT_LD_FLAG_ENABLED = "testing-support";

    public static final String HYPHEN_SEPARATOR = " - ";

    public static final String CASE_DATA_ID = "id";

    public static final String LISTWITHOUTNOTICE_HEARINGDETAILS = "listWithoutNoticeHearingDetails";
    public static final String FL401_LISTONNOTICE_HEARINGDETAILS = "fl401ListOnNoticeHearingDetails";
    public static final String LINKED_CASES_LIST = "linkedCaCasesList";
    public static final String LEGAL_ADVISER_LIST = "legalAdviserList";
    public static final String FL401_CASE_WITHOUT_NOTICE = "isFl401CaseCreatedForWithOutNotice";
    public static final String FL401_LIST_ON_NOTICE_DOCUMENT = "fl401ListOnNoticeDocument";

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
    public static final String IS_HEARING_PAGE_NEEDED = "isHearingPageNeeded";
    public static final String IS_ORDER_CREATED_BY_SOLICITOR = "isOrderCreatedBySolicitor";

    public static final String EARLIEST_HEARING_DATE = "earliestHearingDate";
    public static final String LATEST_HEARING_DATE = "latestHearingDate";
    public static final String HEARING_PRIORITY_TYPE_ENUM = "hearingPriorityTypeEnum";
    public static final String CUSTOM_DETAILS = "customDetails";
    public static final String[] EMPTY_ARRAY = {};
    public static final String LIST_ON_NOTICE_REASONS_SELECTED = "selectedReasonsForListOnNotice";

    public static final String SELECTED_AND_ADDITIONAL_REASONS = "selectedAndAdditionalReasons";
    public static final String SUBJECT = "subject";
    public static final String CASE_NOTE = "caseNote";

    public static final String REASONS_SELECTED_FOR_LIST_ON_NOTICE = "List without notice";

    public static final String CASE_NOTES = "caseNotes";
    public static final String COLON_SEPERATOR = ":";
    public static final String DA_LIST_ON_NOTICE_FL404B_DOCUMENT = "DA_LIST_ON_NOTICE_FL404B_DOCUMENT";
    public static final String SDO_PERMISSION_HEARING_DETAILS = "sdoPermissionHearingDetails";
    public static final String SDO_SECOND_HEARING_DETAILS = "sdoSecondHearingDetails";
    public static final String SDO_URGENT_HEARING_DETAILS = "sdoUrgentHearingDetails";
    public static final String SDO_FHDRA_HEARING_DETAILS = "sdoFhdraHearingDetails";
    public static final String SDO_DRA_HEARING_DETAILS = "sdoDraHearingDetails";
    public static final String SDO_SETTLEMENT_HEARING_DETAILS = "sdoSettlementHearingDetails";

    public static final String SDO_DIRECTIONS_FOR_FACT_FINDING_HEARING_DETAILS = "sdoDirectionsForFactFindingHearingDetails";

    public static final String C100_RESPONDENTS = "respondents";
    public static final String C100_RESPONDENT_TABLE = "respondentTable";

    public static final String C100_APPLICANTS = "applicants";
    public static final String C100_APPLICANT_TABLE = "applicantTable";

    public static final String FL401_RESPONDENTS = "respondentsFL401";
    public static final String FL401_RESPONDENT_TABLE = "fl401RespondentTable";

    public static final String FL401_APPLICANTS = "applicantsFL401";
    public static final String FL401_APPLICANT_TABLE = "fl401ApplicantTable";
    //PRL-3504 - pagination for RD staff data
    public static final String RD_STAFF_TOTAL_RECORDS_HEADER = "total_records";
    public static final int RD_STAFF_PAGE_SIZE = 250;
    public static final int RD_STAFF_FIRST_PAGE = 0;
    public static final int RD_STAFF_SECOND_PAGE = 1;

    //PRL-3254 - hearing status - Completed
    public static final String HMC_STATUS_COMPLETED = "COMPLETED";

    public static final String CAFCASS = "Cafcass";
    public static final String APPLICANTS = "applicants";
    public static final String RESPONDENTS = "respondents";
    public static final String COMMA = ",";
    public static final String EMPTY_STRING = "";

    public static final String CITIZEN_HOME = "/citizen-home";

    public static final String VERIFY_CASE_NUMBER_ADDED = "isAddCaseNumberAdded";

    public static final String CURRENCY_SIGN_POUND = "£";

    public static final String ADDITIONAL_APPLICATION_FEES_TO_PAY = "additionalApplicationFeesToPay";

    public static final String ADDITIONAL_APPLICATIONS_HELP_WITH_FEES = "additionalApplicationsHelpWithFees";

    public static final String A = "A";

    public static final String B = "B";

    public static final String C = "C";

    public static final String D = "D";

    public static final String E = "E";

    public static final String F = "F";

    public static final String G = "G";
    public static final String H = "H";
    public static final String I = "I";
    public static final String J = "J";
    public static final String K = "K";
    public static final String L = "L"; //CA other org pack
    public static final String M = "M";
    public static final String N = "N";
    public static final String O = "O";
    public static final String P = "P";
    public static final String Q = "Q";
    public static final String R = "R";
    public static final String S = "S";

    public static final String HI = "HI";
    public static final String Z = "Z"; //CA other org pack
    public static final String SWANSEA_COURT_NAME = "Swansea Civil And Family Justice Centre";
    public static final String CAFCASS_CAN_VIEW_ONLINE = "Cafcass/Cafcass CYMRU can login to system to view documents, "
        + "hence not sending any documents ";
    public static final String CITIZEN_CAN_VIEW_ONLINE = "Citizen can login to dashboard to view documents, "
        + "hence not attaching any documents in the email ";
    public static final String SOA_RECIPIENT_OPTIONS = "soaRecipientsOptions";
    public static final String SOA_OTHER_PEOPLE_PRESENT_IN_CASE = "soaOtherPeoplePresentInCaseFlag";
    public static final String SOA_CYMRU_EMAIL = "soaCafcassCymruEmail";
    public static final String SOA_OTHER_PARTIES = "soaOtherParties";
    public static final String SOA_APPLICATION_SCREEN_1 = "serviceOfApplicationScreen1";
    public static final String SOA_ORDER_LIST_EMPTY = "soaIsOrderListEmpty";
    public static final String SOA_DOCUMENT_PLACE_HOLDER = "sentDocumentPlaceHolder";
    public static final String CASE_CREATED_BY = "caseCreatedBy";
    public static final String SOA_CONFIDENTIAL_DETAILS_PRESENT = "isConfidential";
    public static final String COURT_CODE_FROM_FACT = "courtCodeFromFact";

    public static final String D_MMMM_UUUU = "d MMMM uuuu";

    public static final String ENG_STATIC_DOCS_PATH = "staticdocs/";

    public static final String PRIVACY_DOCUMENT_FILENAME = "Privacy_Notice.pdf";
    public static final String PRIVACY_DOCUMENT_FILENAME_WELSH = "Privacy_Notice_Welsh.pdf";
    public static final String C9_DOCUMENT_FILENAME = "C9_personal_service.pdf";
    public static final String C1A_BLANK_DOCUMENT_FILENAME = "C1A_Blank.pdf";
    public static final String C1A_BLANK_DOCUMENT_WELSH_FILENAME = "C1A_Blank_Welsh.pdf";
    public static final String C7_BLANK_DOCUMENT_FILENAME = "Blank_C7.pdf";
    public static final String SOA_C6A_OTHER_PARTIES_ORDER = "Notice_Of_Proceeding_Order_C6a.pdf";
    public static final String SOA_C6A_OTHER_PARTIES_ORDER_WELSH = "Welsh_Notice_Of_Proceeding_Order_C6a.pdf";

    public static final String SOA_MEDIATION_VOUCHER_FILENAME = "Mediation-voucher.pdf";
    public static final String SOA_NOTICE_SAFETY = "Notice-safety.pdf";
    public static final String SOA_C9_PERSONAL_SERVICE_FILENAME = "C9_personal_service.pdf";
    public static final String SOA_FL415_FILENAME = "FL415.pdf";
    public static final String SOA_FL416_FILENAME = "FL416.pdf";

    public static final String SOA_MULTIPART_FILE = "files";
    public static final String SOA_CITIZEN = "CITIZEN";
    public static final String SOA_SOLICITOR = "SOLICITOR";
    public static final String COURT_BAILIFF = "Court bailiff";
    public static final String COURT_ADMIN = "Court admin";
    public static final String JUDICIARY = "Judiciary";
    public static final String LEGAL_ADVISER = "Legal adviser";

    public static final String SOLICITOR_ROLE = "caseworker-privatelaw-solicitor";
    public static final String CAFCASS_ROLE = "caseworker-privatelaw-externaluser-viewonly";
    public static final String SOLICITOR = "Solicitor";
    public static final String COURT_STAFF = "Staff";

    public static final String BULK_SCAN = "Bulk scan";
    public static final String LEGAL_PROFESSIONAL = "Legal professional";
    public static final String LONDON_TIME_ZONE = "Europe/London";

    public static final String CA_APPLICANT = "CAAPPLICANT";
    public static final String DA_APPLICANT = "DAAPPLICANT";
    public static final String CA_RESPONDENT = "CARESPONDENT";
    public static final String DA_RESPONDENT = "DARESPONDENT";
    public static final String SOS_CONFIRMATION_HEADER = "# Application was served";
    public static final String SOS_CONFIRMATION_BODY_PREFIX = "### What happens next \n\n "
        + System.lineSeparator()
        + "The court will contact the people in the case with details of the first court hearing. "
        + System.lineSeparator()
        + "If Cafcass are involved in the case, they will provide the court with a safeguarding letter. ";
    public static final String ALL_RESPONDENTS = "All respondents";
    public static final String TEST_UUID = "00000000-0000-0000-0000-000000000000";
    public static final String ADD_PEOPLE_TO_THE_CASE = "Add people to the case";
    public static final String ONLY_COMPLETE_IF_RELEVANT = "Only complete if relevant";
    public static final String  ADD_APPLICATION_DETAILS = "Add application details";
    public static final String  ADD_ADDITIONAL_INFORMATION = "Add additional information";

    public static final String  TASK_LIST_VERSION_V2 = "v2";
    public static final String  TASK_LIST_VERSION_V3 = "v3";

    public static final String  PHYSICAL_ABUSE = "physicalAbuse";
    public static final String  PSYCHOLOGICAL_ABUSE = "psychologicalAbuse";
    public static final String  SEXUAL_ABUSE = "sexualAbuse";
    public static final String  EMOTIONAL_ABUSE = "emotionalAbuse";
    public static final String  FINANCIAL_ABUSE = "financialAbuse";
    //Soa letter types
    public static final String SOA_RES_LET_RE5 = "RE5";

    public static final String EUROPE_LONDON_TIME_ZONE = "Europe/London";
    public static final String DD_MMM_YYYY_HH_MM_SS = "dd MMM yyyy HH:mm:ss";
    public static final String OTHER_PERSON = "otherPerson";
    public static final String UNDERSCORE = "_";
    public static final String AWP_STATUS_SUBMITTED = "Submitted";
    public static final String AWP_STATUS_IN_REVIEW = "In review";
    public static final String AWP_STATUS_CLOSED = "Closed";
    public static final String AWP_ADDTIONAL_APPLICATION_BUNDLE = "additionalApplicationsBundle";
    public static final String AWP_OTHER_APPLICATION_SNR_CODE = "OT";
    public static final String AWP_C2_APPLICATION_SNR_CODE = "C2";
    public static final String D_MMM_YYYY_HH_MM = "d MMM yyyy, hh:mm a";

    public static final String CASE_NUMBER = "caseNumber";
    public static final String CASE_NAME = "caseName";
    public static final String APPLICATION_TYPE = "applicationType";
    public static final String CONFIDENTIALITY_TEXT = "confidentialityText";
    public static final String CONTENT = "content";
    public static final String ATTACHMENT_TYPE = "attachmentType";
    public static final String DISPOSITION = "disposition";
    public static final String MANDATORY_JUDGE =
            "Full name of Justices' Legal Adviser is mandatory, when the Judge's title is selected as Justices' Legal Adviser";
    public static final String DUMMY = "Dummy";


    public static final String DATE_TIME_PATTERN = "dd MMM yyyy, hh:mm:ss a";
    public static final String AM_LOWER_CASE = "am";
    public static final String AM_UPPER_CASE = "AM";
    public static final String PM_LOWER_CASE = "pm";
    public static final String PM_UPPER_CASE = "PM";

    public static final String CAFCASS_PARTY = "CAFCASS";
    public static final String MANDATORY_MAGISTRATE = "Full name of a Magistrate is mandatory, when the Judge's title is selected as"
            + " Magistrates";
    public static final String HEARING_SCREEN_ERRORS = "hearingScreenErrors";
    public static final String OCCUPATIONAL_SCREEN_ERRORS = "occupationalScreenErrors";

    public static final String ORDER_NOT_AVAILABLE_C100 = "This order is not available to be created for C100 cases";
    public static final String ORDER_NOT_AVAILABLE_FL401 = "This order is not available to be created for FL401 cases";

    public static final String ORDER_COLLECTION = "orderCollection";

    public static final String WA_PERFORMING_USER = "performingUser";
    public static final String WA_PERFORMING_ACTION = "performingAction";
    public static final String WA_JUDGE_LA_REVIEW_REQUIRED = "judgeLaReviewRequired";
    public static final String WA_ORDER_NAME = "orderNameForWA";
    public static final String WA_ORDER_NAME_JUDGE_APPROVED = "orderNameForJudgeApproved";
    public static final String WA_ORDER_NAME_JUDGE_CREATED = "orderNameForJudgeCreatedOrder";
    public static final String WA_ORDER_NAME_SOLICITOR_CREATED = "orderNameForSolicitorCreatedOrder";
    public static final String WA_ORDER_NAME_ADMIN_CREATED = "orderNameForAdminCreatedOrder";
    public static final String ALLOCATE_JUDGE_ROLE = "allocated-judge";
    public static final String HEARING_JUDGE_ROLE = "hearing-judge";
    public static final String GATEKEEPING_JUDGE_ROLE = "gatekeeping-judge";
    public static final String IS_SPECIFIC_JUDGE_OR_LEGAL_ADVISOR_NEEDED = "isSpecificJudgeOrLegalAdviserNeeded";

    // PRL-5025
    public static final String WA_IS_ORDER_APPROVED = "isOrderApproved";
    public static final String WA_WHO_APPROVED_THE_ORDER = "whoApprovedTheOrder";
    public static final String WA_JUDGE_LA_MANAGER_REVIEW_REQUIRED = "judgeLaManagerReviewRequired";
    public static final String WA_IS_MULTIPLE_HEARING_SELECTED = "isMultipleHearingSelected";
    public static final String WA_HEARING_OPTION_SELECTED = "hearingOptionSelected";
    public static final String WA_IS_HEARING_TASK_NEEDED = "isHearingTaskNeeded";
    public static final String WA_MULTIPLE_OPTIONS_SELECTED_VALUE = "multipleOptionSelected";

    public static final String WITHOUT_NOTICE_REJECTION = "Without notice rejection";
    public static final String OTHER_PEOPLE_SELECTED_C6A_MISSING_ERROR = "You can only serve other people in the case if there is a C6A."
        + " Go back to the previous page to select it. If the C6A is not there, you will need to create it in manage orders.";

    public static final String BUFF_CHILD_AND_OTHER_PEOPLE_RELATIONS = "buffChildAndOtherPeopleRelations";
    public static final String BUFF_CHILD_AND_RESPONDENT_RELATIONS = "buffChildAndRespondentRelations";

    public static final String INTERNAL_CORRESPONDENCE_LABEL = "Internal correspondence";
    public static final String INTERNAL_CORRESPONDENCE_CATEGORY_ID = "internalCorrespondence";
    public static final String RESTRICTED_DOCUMENTS = "restrictedDocuments";
    public static final String CONFIDENTIAL_DOCUMENTS = "confidentialDocuments";
    public static final String MISSING_ADDRESS_WARNING_TEXT = "missingAddressWarningText";

    public static final String WARNING_TEXT_DIV = "<div class='govuk-warning-text'>"
        + "<span class='govuk-warning-text__icon' aria-hidden='true'>!";

    public static final String STAFF = "STAFF";
    public static final String TRUE = "True";
    public static final String WA_SOA_C8_CHECK_NEEDED = "isC8CheckNeeded";
    public static final String WA_SOA_RESPONSIBLE_FOR_SERVICE = "responsibleForService";
    public static final String WA_SOA_OCCUPATION_ORDER_SELECTED = "isOccupationOrderSelected";
    public static final String WA_SOA_C8_CHECK_APPROVED = "isC8CheckApproved";

    public static final String HEARINGS_TYPE = "hearingsType";

    public static final String CITIZEN = "Citizen";
    public static final String SUCCESS = "Success";
    public static final String DOCUMENT_COVER_SHEET_SERVE_ORDER_HINT = "DOCUMENT_COVER_SHEET_SERVE_ORDER";

    public static final String FL401_LIST_ON_NOTICE_HEARING_INSTRUCTION = "fl401listOnNoticeHearingInstruction";

    public static final String LIST_ON_NOTICE_HEARING_INSTRUCTIONS = "List on notice - hearing instructions";

    public static final String REJECT_WITHOUT_NOTICE_REASONS = "Reject without notice - reasons";

    public static final String FL401_REASONS_FOR_LIST_WITHOUT_NOTICE_REQUESTED = "fl401ReasonsForListWithoutNoticeRequested";

    public static final String RESP_CHILD_ABUSES_DOCMOSIS = "respChildAbuseBehavioursDocmosis";

}
