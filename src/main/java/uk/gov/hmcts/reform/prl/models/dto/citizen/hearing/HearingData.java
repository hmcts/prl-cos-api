package uk.gov.hmcts.reform.prl.models.dto.citizen.hearing;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;

import java.util.List;


@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder(toBuilder = true)
public class HearingData implements MappableObject {

    private final String hmctsServiceCode;

    private final String caseRef;

    private final List<CaseHearing> caseHearings;

}
