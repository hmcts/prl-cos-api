package uk.gov.hmcts.reform.prl.config.templates;

public class TransferCaseTemplate {

    public static String TRANSFER_CASE_EMAIL_BODY = ""
        + "<h2>Case transferred</h2>"
        + "\n"
        + "\n"
        + "Case name: %s \n"
        + "Issue date: %s \n"
        + "Case number: %s\n"
        + "\n"
        + "\n"
        + "\n"
        + "This case has been transferred to your court.\n"
        + "\n"
        + "The case has been marked as %s.\n"
        + "\n"
        + "Case details:\n"
        + "%s"
        + "\n"
        + "HM Courts and Tribunals Service (HMCTS) \n"
        + "\n";
}
