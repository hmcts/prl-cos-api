package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class KeepDetailsPrivate {
    private final YesNoDontKnow otherPeopleKnowYourContactDetails;
    private final YesOrNo confidentiality;
    private List<ConfidentialityEnum> confidentialityList;
}
