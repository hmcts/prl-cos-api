package uk.gov.hmcts.reform.prl.models.complextypes;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;

import java.util.List;

@Data
@Builder
public class WelshNeed {
    private String whoNeedsWelsh;
    private  List<SpokenOrWrittenWelshEnum> spokenOrWritten;
    private  List<SpokenOrWrittenWelshEnum> fl401SpokenOrWritten;
}
