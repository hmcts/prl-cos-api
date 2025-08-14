package uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.SpecialMeasuresEnum;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoingToCourt {
    private Boolean isInterpreterRequired;
    private String interpreterLanguage;
    private String interpreterDialect;
    private boolean anyDisabilityNeeds;
    private String disabilityNeedsDetails;
    private List<SpecialMeasuresEnum> anySpecialMeasures;
}
