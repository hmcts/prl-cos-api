package uk.gov.hmcts.reform.prl.models.user;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

@Data
@Builder
public class User {
    private final String authorisation;
    private final UserInfo userInfo;
}
