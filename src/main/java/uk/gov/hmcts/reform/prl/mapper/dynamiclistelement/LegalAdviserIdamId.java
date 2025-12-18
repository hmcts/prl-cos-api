package  uk.gov.hmcts.reform.prl.mapper.dynamiclistelement;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LegalAdviserIdamId {
    private String idamId;
    private String email;
    private String fullName;
}
