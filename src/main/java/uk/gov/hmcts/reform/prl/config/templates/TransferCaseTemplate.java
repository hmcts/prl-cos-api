package uk.gov.hmcts.reform.prl.config.templates;

public class TransferCaseTemplate {

    public static String TRANSFER_CASE_EMAIL_BODY = ""
        + "<h2>Case transferred</h2>"
        + "<br/>"
        + "<br/>"
        + "Case name: %s <br/>"
        + "Issue date: %s <br/>"
        + "Case number: %s<br/>"
        + "<br/>"
        + "This case has been transferred to your court.<br/>"
        + "<br/>"
        + "The case has been marked as %s.<br/>"
        + "<br/>"
        + "Case details:<br/>"
        + "%s"
        + "<br/>"
        + "<br/>"
        + "HM Courts and Tribunals Service (HMCTS) <br/>"
        + "<br/>";
}
