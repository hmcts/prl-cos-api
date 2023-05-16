package uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.CafcassServiceApplicationEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;

import java.util.List;

@Data
@Builder
public class ConfirmRecipients {
    @JsonProperty("applicantsList")
    private final DynamicMultiSelectList applicantsList;
    @JsonProperty("respondentsList")
    private final DynamicMultiSelectList respondentsList;
    @JsonProperty("applicantSolicitorList")
    private final DynamicMultiSelectList applicantSolicitorList;
    @JsonProperty("respondentSolicitorList")
    private final DynamicMultiSelectList respondentSolicitorList;
    @JsonProperty("otherPeopleList")
    private final DynamicMultiSelectList otherPeopleList;
    private final List<CafcassServiceApplicationEnum> cafcassEmailOptionChecked;
    private final List<OtherEnum> otherEmailOptionChecked;
    private final String cafcassEmailAddressForNotifications;
    private final List<Element<String>> otherEmailAddressList;
}
