package uk.gov.hmcts.reform.prl.models.common.judicial;

import lombok.Builder;
import lombok.Value;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;

@Value
@Builder(toBuilder = true)
public class JudicialUser implements MappableObject {
    private String idamId;

    public String getIdamId() {
        return idamId;
    }

    public String getPersonalCode() {
        return personalCode;
    }

    private String personalCode;
}
