package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContactDetail {

    private YesOrNo canProvideEmail;
    private String emailAddress;
    private String canNotProvideEmailReason;
    private YesOrNo canProvideTelephoneNumber;
    private String telephoneNumber;
    private String canNotProvideTelephoneNumberReason;
    private YesOrNo canLeaveVoiceMail;
    private String applicantContactPreferences;
}
