package uk.gov.hmcts.reform.prl.config.templates;


public class Templates {
    //Docmosis templates
    public static String ANNEX_ENG_Y = "PRL-ENG-ANNEX-Y.docx";
    public static String ANNEX_ENG_Z = "PRL-ENG-ANNEX-Z.docx";
    public static String MEDIATION_VOUCHER_ENG = "PRL-ENG-MEDIATION-VOUCHER.docx";
    public static String SAFETY_PROTECTION_ENG = "PRL-ENG-SAFETY-PROTECTION.docx";
    public static String FL416_ENG = "PRL-ENG-FL416.docx";
    public static String PRIVACY_NOTICE_ENG = "PRL-PRIVACY-NOTICE-DOCUMENT.docx";


    //Emails
    public static String EMAIL_BODY = ""
        + "\n"
        + "\n"
        + "Case name: %s \n"
        + "Case number: %s\n"
        + "\n"
        + "\n"
        + "Dear %s,\n"
        + "\n"
        + "We have made a decision on how to progress this case. The application has now been served on the parties in the case.\n"
        + "\n"
        + "The case documents are attached to this email.\n"
        + "\n"
        + "You can also view the documents when you sign into your MyHMCTS account. A sign in link will be sent in a separate email.\n"
        + "\n"
        + "HM Courts and Tribunals Service (HMCTS) \n"
        + "\n";


    public static String SPECIAL_INSTRUCTIONS_EMAIL_BODY = ""
        + "\n"
        + "\n"
        + "Case name: %s \n"
        + "Case number: %s\n"
        + "\n"
        + "\n"
        + "Dear %s,\n"
        + "\n"
        + "Please serve the below list of documents to the respondent/s"
        + "\n"
        + "1.C100\n"
        + "2.C1A\n"
        + "3.C1A response form (new document)\n"
        + "4.Blank C7 acknowledgement\n"
        + "5.Privacy Notice\n"
        + "6.Annex Z if respondent’s contact details are confidential\n"
        + "7.Notice of safety, protection and support\n"
        + "8.Mediation voucher note\n"
        + "9.CB7 leaflet\n"
        + "10.Any orders ticked as part of first screen\n"
        + "11.PD36Q/Y information\n"
        + "12.Special arrangements letter\n"
        + "13.Additional documents"
        + "\n"
        + "\n"
        + "You can also view the final order when you sign into your MyHMCTS account. A sign in link will be sent in a separate email. \n"
        + "\n"
        + "\n"
        + "HM Courts and Tribunals Service (HMCTS) \n"
        + "\n";

}
