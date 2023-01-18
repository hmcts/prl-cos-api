package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.SafetyConcernsAboutEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.TypesOfAbusesEnum;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SafetyConcerns {
    private final AbuseTypes child;
    private final AbuseTypes applicant;
    private final AbuseTypes respondent;
    private final YesOrNo haveSafetyConcerns;
    private final List<SafetyConcernsAboutEnum> safetyConcernAbout;
    private final List<TypesOfAbusesEnum> concernAboutChild;
    private final List<TypesOfAbusesEnum> concernAboutRespondent;
    private final OtherConcerns otherconcerns;
    private final Abductions abductions;
}
