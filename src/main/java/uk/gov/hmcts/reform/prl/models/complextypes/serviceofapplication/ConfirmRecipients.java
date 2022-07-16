package uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication;

import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.RestrictToCafcassHmcts;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.CafcassServiceApplicationEnum;
import uk.gov.hmcts.reform.prl.models.Element;

import java.util.List;

@Data
@Builder
public class ConfirmRecipients {
    private final List<OrderRecipientsEnum> applicantsList;
    private final List<OtherOrderRecipientsEnum> otherPeopleList;

    private final List<CafcassServiceApplicationEnum> cafcassEmailOptionChecked;
    private final List<OtherEnum> otherEmailOptionChecked;

    private final List<Element<String>> cafcassEmailAddressList;
    private final List<Element<String>> otherEmailAddressList;
}
