package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Builder
@Data
public class LocalAuthority {

    @JsonProperty("isLocalAuthorityInvolvedInCase")
    private YesOrNo isLocalAuthorityInvolvedInCase;

    @JsonProperty("localAuthoritySolicitorOrganisationName")
    private String localAuthoritySolicitorOrganisationName;
}
