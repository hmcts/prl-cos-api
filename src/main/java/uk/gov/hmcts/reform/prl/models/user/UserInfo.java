package uk.gov.hmcts.reform.prl.models.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.ccd.sdk.api.CCD;

@Data
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {
    @CCD(label = " ", searchable = false)
    private String idamId;
    @CCD(label = " ", searchable = false)
    private String firstName;
    @CCD(label = " ", searchable = false)
    private String lastName;
    @CCD(label = " ", searchable = false)
    private String role;
    @CCD(label = " ", searchable = false)
    private String emailAddress;
}
