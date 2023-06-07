package uk.gov.hmcts.reform.prl.config.templates;


public class Templates {
    //Docmosis templates
    public static String ANNEX_ENG_Y = "PRL-ENG-ANNEX-Y.docx";
    public static String ANNEX_ENG_Z = "PRL-ENG-ANNEX-Z.docx";
    public static String MEDIATION_VOUCHER_ENG = "PRL-ENG-MEDIATION-VOUCHER.docx";
    public static String SAFETY_PROTECTION_ENG = "PRL-ENG-SAFETY-PROTECTION.docx";
    public static String FL416_ENG = "PRL-ENG-FL416.docx";


    //Emails
    public static String EMAIL_BODY = ""
        + "<b>A final order has been issued</b>"
        + "\n"
        + "\n"
        + "<b>Case name:</b> %s \n"
        + "<b>Case number:</b> %s\n"
        + "\n"
        + "\n"
        + "Dear %s,\n"
        + "\n"
        + "<b>A final order has been issued for this case and is attached to this email.<b>\n"
        + "\n"
        + "You can also view the final order when you sign into your MyHMCTS account. A sign in link will be sent in a separate email. \n"
        + "\n"
        + "HM Courts and Tribunals Service (HMCTS) \n"
        + "\n";

}
