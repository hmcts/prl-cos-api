package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.CafcassServiceApplicationEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceOfApplication {
    @JsonProperty("soaApplicantsList")
    private final DynamicMultiSelectList soaApplicantsList;
    @JsonProperty("soaRespondentsList")
    private final DynamicMultiSelectList soaRespondentsList;
    @JsonProperty("soaOtherPeopleList")
    private final DynamicMultiSelectList soaOtherPeopleList;
    private final List<CafcassServiceApplicationEnum> soaCafcassEmailOptionChecked;
    private final List<OtherEnum> soaOtherEmailOptionChecked;
    private final List<Element<String>> soaCafcassEmailAddressList;
    private final List<Element<String>> soaOtherEmailAddressList;
}
