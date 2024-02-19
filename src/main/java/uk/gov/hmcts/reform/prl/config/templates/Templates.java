package uk.gov.hmcts.reform.prl.config.templates;


public class Templates {
    //Docmosis templates
    public static String ANNEX_ENG_Y = "PRL-ENG-ANNEX-Y.docx";
    public static String ANNEX_ENG_Z = "PRL-ENG-ANNEX-Z.docx";
    public static String MEDIATION_VOUCHER_ENG = "PRL-ENG-MEDIATION-VOUCHER.docx";
    public static String SAFETY_PROTECTION_ENG = "PRL-ENG-SAFETY-PROTECTION.docx";
    public static String FL416_ENG = "PRL-ENG-FL416.docx";
    public static String PRIVACY_NOTICE_ENG = "PRL-PRIVACY-NOTICE-DOCUMENT.docx";
    public static String AP6_LETTER = "PRL-LET-ENG-C100-AP6.docx";
    public static String PRL_LET_ENG_AP2 = "PRL-LET-ENG-FL401-AP2.docx";
    public static String PRL_LET_ENG_AP8 = "PRL-LET-ENG-C100-AP8.docx";
    public static String PRL_LET_ENG_AP7 = "PRL-LET-ENG-C100-AP7.docx";
    public static String PRL_LET_ENG_RE5 = "PRL-LET-ENG-C100-RE5.docx";
    public static String PRL_LET_ENG_FL401_RE1 = "PRL-LET-ENG-FL401-RE1.docx";
    public static String PRL_LET_ENG_FL401_RE2 = "PRL-LET-ENG-FL401-RE2.docx";
    public static String PRL_LET_ENG_FL401_RE3 = "PRL-LET-ENG-FL401-RE3.docx";
    public static String PRL_LET_ENG_FL401_RE4 = "PRL-LET-ENG-FL401-RE4.docx";
    public static String PRL_LET_ENG_C100_RE6 = "PRL-LET-ENG-C100-RE6.docx";

    public static String PRL_LET_ENG_LIST_WITHOUT_NOTICE = "PRL-LET-ENG-LIST-WITHOUT-NOTICE.docx";

    //Emails

    public static String NEW_ORDER_TITLE = "<b>A new order has been issued</b>";

    public static String FINAL_ORDER_TITLE = "<b>Final court order issued for this case</b>";

    public static String RESPONDENT_SOLICITOR_SERVE_ORDER_EMAIL_BODY =
            "<br>"
            + "A new order has been issued for this case and is attached to this email.<br>";

    public static String RESPONDENT_SOLICITOR_FINAL_ORDER_EMAIL_BODY =
            "<br>"
                    + "A final court order has been issued for this case and is attached to this email.<br>";

    public static String EMAIL_START = ""
            + "<br>"
            + "<br>"
            + "Case name: %s <br>"
            + "Case number: %s<br>"
            + "<br>"
            + "<br>"
            + "Dear %s,<br>";

    public static String EMAIL_END =
            "<br>"
                    + "If you have access to the Private Law digital service, you can view the order with this link:<br>"
                    + "<br>"
                    + "%s<br>"
                    + "<br>"
                    + "HM Courts and Tribunals Service (HMCTS) <br>"
                    + "<br>";
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
