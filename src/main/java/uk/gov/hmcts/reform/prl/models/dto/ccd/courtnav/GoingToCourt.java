package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SpecialMeasuresEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
@Getter
@Setter
@AllArgsConstructor
public class GoingToCourt {
    private final boolean isInterpreterRequired;
    private final String interpreterLanguage;
    private final String interpreterDialect;
    private final boolean anyDisabilityNeeds;
    private final String disabilityNeedsDetails;
    private final List<SpecialMeasuresEnum> anySpecialMeasures;
    private final String courtSpecialRequirements;
}
