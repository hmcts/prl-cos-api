package uk.gov.hmcts.reform.prl.utils;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class TestConstants {

    public static final String TEST_CASE_ID = "1234567890";
    public static final String TEST_EMAIL = "test@example.com";
    public static final String TEST_SOLICITOR_EMAIL = "prl_caseworker_solicitor@mailinator.com";
    public static final String TEST_PETITIONER_FIRST_NAME = "Adam";
    public static final String TEST_PETITIONER_LAST_NAME = "Kowalski";
    public static final String TEST_PETITIONER_NAME = TEST_PETITIONER_FIRST_NAME + " " + TEST_PETITIONER_LAST_NAME;
    public static final String TEST_RESPONDENT_FIRST_NAME = "Zdzislaw";
    public static final String TEST_RESPONDENT_LAST_NAME = "Nowakowski";
    public static final String TEST_RESPONDENT_NAME = TEST_RESPONDENT_FIRST_NAME + " " + TEST_RESPONDENT_LAST_NAME;
    public static final String WALES_POSTCODE_NATIONALCODE = "W";
    public static final String PRL_CASE_TYPE = "PRLAPPS";
    public static final String CAFCASS_TEST_AUTHORISATION_TOKEN = "authorisationToken";
    public static final String CAFCASS_TEST_SERVICE_AUTHORISATION_TOKEN = "serviceAuthorisationToken";
    public static final String EMPTY_STRING = "";
    public static final String TEST_AUTHORIZATION = "testAuthorisation";
    public static final String TEST_CAFCASS_DOWNLOAD_FILENAME = "CafcassDownloadFile.doc";
    public static final String TEST_SERVICE_AUTHORIZATION = "testServiceAuthorisation";
    public static final String JURISDICTION = "PRIVATELAW";
    public static final String CAFCASS_USER_ROLE = "caseworker-privatelaw-cafcass";

}
