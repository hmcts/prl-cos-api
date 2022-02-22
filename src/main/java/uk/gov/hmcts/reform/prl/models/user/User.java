package uk.gov.hmcts.reform.prl.models.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private final String authorisation;
    private final UserInfo userInfo;
}
