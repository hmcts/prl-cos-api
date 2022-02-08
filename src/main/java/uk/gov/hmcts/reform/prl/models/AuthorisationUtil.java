package uk.gov.hmcts.reform.prl.models;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class AuthorisationUtil {

    String token;
    Long caseId;

    public void setTokenAndId(String token, Long id) {
        this.token = token;
        this.caseId = id;
    }

}
