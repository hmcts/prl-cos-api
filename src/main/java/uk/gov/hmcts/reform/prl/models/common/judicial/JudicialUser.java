package uk.gov.hmcts.reform.prl.models.common.judicial;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class JudicialUser {
    private String idamId;

    public String getIdamId() {
        return idamId;
    }

    public String getPersonalCode() {
        return personalCode;
    }

    private String personalCode;
}
