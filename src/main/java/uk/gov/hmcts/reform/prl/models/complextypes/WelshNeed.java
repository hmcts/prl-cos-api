package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;

import java.util.List;

@Data
@Builder
public class WelshNeed {

    private final String whoNeedsWelsh;
    private final List<SpokenOrWrittenWelshEnum> spokenOrWritten;

}
