package uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
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
    @JsonUnwrapped
    @Builder.Default
    private final DynamicMultiSelectList applicantsList;
    @JsonUnwrapped
    @Builder.Default
    private final DynamicMultiSelectList respondentsList;
    @JsonUnwrapped
    @Builder.Default
    private final DynamicMultiSelectList applicantSolicitorList;
    @JsonUnwrapped
    @Builder.Default
    private final DynamicMultiSelectList respondentSolicitorList;
    @JsonUnwrapped
    @Builder.Default
    private final DynamicMultiSelectList otherPeopleList;
    private final List<CafcassServiceApplicationEnum> cafcassEmailOptionChecked;
    private final List<OtherEnum> otherEmailOptionChecked;
    private final List<Element<String>> cafcassEmailAddressList;
    private final List<Element<String>> otherEmailAddressList;
}
