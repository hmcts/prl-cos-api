package uk.gov.hmcts.reform.prl.models.cafcass.Hearing;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder(builderMethodName = "hearingsWith")
@NoArgsConstructor
@AllArgsConstructor
public class Hearings {
    private String hmctsServiceCode;

    private String caseRef;

    private List<CaseHearing> caseHearings;

}
