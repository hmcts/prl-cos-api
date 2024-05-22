package uk.gov.hmcts.reform.prl.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestConstants {

    public static final String AUTHORISATION_HEADER = "Authorization";
    public static final String CAFCASS_END_DATE_PARAM = "end_date";
    public static final String CAFCASS_END_DATE_PARAM_VALUE = "2022-08-26T11:00:54.055";
    public static final String CAFCASS_START_DATE_PARAM = "start_date";
    public static final String CAFCASS_START_DATE_PARAM_VALUE = "2022-08-22T10:44:43.49";
    public static final String CREATE_SERVICE_RESPONSE = "classpath:response/cafcass-search-response.json";
    public static final String SEARCH_CASE_ENDPOINT = "/cases/searchCases";
    public static final String SERVICE_AUTHORISATION_HEADER = "ServiceAuthorization";
    public static final String TEST_AUTH_TOKEN = "authorisation";
    public static final String TEST_SERVICE_AUTH_TOKEN = "serviceauthorisation";
    public static final String CAFCASS_DOCUMENT_DOWNLOAD_ENDPOINT = "/cases/documents/{documentId}/binary";
    public static final String CAFCASS_DUMMY_UPLOAD_FILE = "classpath:courtnav/Dummy_pdf_file.pdf";
}
