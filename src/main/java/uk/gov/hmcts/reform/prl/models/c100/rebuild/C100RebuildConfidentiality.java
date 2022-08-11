package uk.gov.hmcts.reform.prl.models.c100.rebuild;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;

@Data
@Builder(toBuilder = true)
public class C100RebuildConfidentiality {

    @JsonProperty
    private final String doesRespondentKnowApplicantContactDetail;
    @JsonProperty
    private final String doesApplicantDetailKeptPrivateFromRespondent;
    @JsonProperty
    private final String[] applicantDetailKeptPrivateFromRespondentList;
    @JsonProperty
    private final String doesApplicantDetailKeptPrivateFromOtherApplicant;
    @JsonProperty
    private final String[] applicantDetailKeptPrivateFromOtherApplicantList;

}
