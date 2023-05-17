package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.*;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.CafcassServiceApplicationEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404b;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceOfApplication {
    @JsonProperty("applicantsList")
    private final DynamicMultiSelectList soaApplicantsList;
    @JsonProperty("respondentsList")
    private final DynamicMultiSelectList soaRespondentsList;
    @JsonProperty("otherPeopleList")
    private final DynamicMultiSelectList soaOtherPeopleList;
    private final List<CafcassServiceApplicationEnum> soaCafcassEmailOptionChecked;
    private final List<OtherEnum> soaOtherEmailOptionChecked;
    private final List<Element<String>> soaCafcassEmailAddressList;
    private final List<Element<String>> soaOtherEmailAddressList;
}
