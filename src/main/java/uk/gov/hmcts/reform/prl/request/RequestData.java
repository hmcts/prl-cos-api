package uk.gov.hmcts.reform.prl.request;

import java.util.Set;

public interface RequestData {

    String authorisation();

    String userId();

    Set<String> userRoles();
}
