package uk.gov.hmcts.reform.prl.config.templates;

public class TransferCaseTemplate {

    public static String TRANSFER_CASE_EMAIL_BODY = ""
        + "<h2>A case has been transferred to your court</h2>"
        + "<br/>"
        + "Case number: %s <br/>"
        + "<br/>"
        + "Case name: %s <br/>"
        + "<br/>"
        + "Issue date: %s <br/>"
        + "<br/>"
        + "Application type: %s<br/>"
        + "<br/>"
        + "%s <br/>"
        + "<br/>"
        + "This case has been transferred to your court from %s.<br/>"
        + "<br/>"
        + "All case documents are attached to this email."
        + "<br/>"
        + "<br/>"
        + "HM Courts and Tribunals Service (HMCTS) <br/>"
        + "<br/>";
}
