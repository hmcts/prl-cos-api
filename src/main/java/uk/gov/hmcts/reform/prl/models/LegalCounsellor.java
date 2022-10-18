package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class LegalCounsellor {

    String firstName;
    String lastName;
    String email;
    String telephoneNumber;
    Organisation organisation;
    String userId;

    @JsonIgnore
    public String getFullName() {
        return firstName + " " + lastName;
    }

}
