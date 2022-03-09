package uk.gov.hmcts.reform.prl.models.complextypes;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;

import java.util.List;

@Data
@Builder
public class WelshNeed {
    private final String whoNeedsWelsh;
    private  List<SpokenOrWrittenWelshEnum> spokenOrWritten;
    private  List<SpokenOrWrittenWelshEnum> fl401SpokenOrWritten;
}
