package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoIDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class KeepDetailsPrivate {
    private final YesNoIDontKnow otherPeopleKnowYourContactDetails;
    private final YesOrNo confidentiality;
    private List<ConfidentialityListEnum> confidentialityList;
}
