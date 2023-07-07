package uk.gov.hmcts.reform.prl.models.cafcass.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder(builderMethodName = "hearingsWith")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Hearings {

    private String hmctsServiceCode;

    private String caseRef;

    private List<CaseHearing> caseHearings;

    private String courtTypeId;

    private String courtName;

}
