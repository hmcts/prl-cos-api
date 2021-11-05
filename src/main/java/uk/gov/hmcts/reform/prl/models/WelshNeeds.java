package uk.gov.hmcts.reform.prl.models;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;

@Data
@Builder
public class WelshNeeds {

    private final String whoNeedsWelsh;
    private final SpokenOrWrittenWelshEnum spokenOrWritten;

}
