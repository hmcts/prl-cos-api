package uk.gov.hmcts.reform.prl.models.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {
    private String idamId;
    private String firstName;
    private String lastName;
    private String role;
    private String emailAddress;
}
