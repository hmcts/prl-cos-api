package uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.safetyconcerns;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.SafetyConcernsAboutEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.TypesOfAbusesEnum;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
public class SafetyConcerns {
    @CCD(label = " ", searchable = false)
    private final AbuseTypes child;
    @CCD(label = " ", searchable = false)
    private final AbuseTypes applicant;
    @CCD(label = " ", searchable = false)
    private final AbuseTypes respondent;
    @CCD(label = " ", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo haveSafetyConcerns;
    @CCD(label = " ", searchable = false)
    private final List<SafetyConcernsAboutEnum> safetyConcernAbout;
    @CCD(label = " ", searchable = false)
    private final List<TypesOfAbusesEnum> concernAboutChild;
    @CCD(label = " ", searchable = false)
    private final List<TypesOfAbusesEnum> concernAboutRespondent;
    @CCD(label = " ", searchable = false)
    private final OtherConcerns otherconcerns;
    @CCD(label = " ", searchable = false)
    private final Abductions abductions;
}
