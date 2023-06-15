package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;


@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceOfApplication {
    @JsonProperty("soaApplicantsList")
    private final DynamicMultiSelectList soaApplicantsList;
    @JsonProperty("coverPageAddress")
    private final Address coverPageAddress;
    @JsonProperty("coverPagePartyName")
    private final String coverPagePartyName;
    @JsonProperty("soaOtherPeoplePresentInCaseFlag")
    private final YesOrNo soaOtherPeoplePresentInCaseFlag;
    @JsonProperty
    private  final YesOrNo isCafcass;



    private final YesOrNo soaServeToRespondentOptions;
    private final ServingRespondentsEnum soaServingRespondentsOptionsCA;
    private final ServingRespondentsEnum soaServingRespondentsOptionsDA;
    @JsonProperty("soaRecipientsOptions")
    private final DynamicMultiSelectList soaRecipientsOptions;
    private final DynamicMultiSelectList soaOtherParties;
    private final YesOrNo soaCafcassServedOptions;
    private final String soaCafcassEmailId;
    private final YesOrNo soaCafcassCymruServedOptions;
    private final String soaCafcassCymruEmail;
}
