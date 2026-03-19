package uk.gov.hmcts.reform.prl.constants.cafcass;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class CafcassAppConstants {

    public static final String ENGLAND_POSTCODE_NATIONALCODE = "E";
    public static final String CAFCASS_USER_ROLE = "caseworker-privatelaw-cafcass";
    public static final String INVALID_DOCUMENT_TYPE = "Un acceptable format/type of document %s";

    public static final String CIR_OVERDUE_TASK_CREATED = "cirOverdueTaskCreated";
    public static final String CIR_DOC_UPLOADED = "cirDocUploaded";
    public static final String CIR_RECEIVED_BY_DEADLINE = "cirReceivedByDeadline";
    public static final String CIR_UPLOADED_DATE = "cirUploadedDate";
    public static final String CIR_DUE_DATE = "whenReportsMustBeFiledByLocalAuthority";

}
