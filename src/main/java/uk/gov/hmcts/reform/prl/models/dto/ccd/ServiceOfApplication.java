package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;

import java.util.List;

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

    private final YesOrNo soaServeToRespondentOptions;
    private final SoaSolicitorServingRespondentsEnum soaServingRespondentsOptionsCA;
    private final SoaSolicitorServingRespondentsEnum soaServingRespondentsOptionsDA;
    private final SoaCitizenServingRespondentsEnum soaCitizenServingRespondentsOptionsCA;
    private final SoaCitizenServingRespondentsEnum soaCitizenServingRespondentsOptionsDA;
    @JsonProperty("soaRecipientsOptions")
    private final DynamicMultiSelectList soaRecipientsOptions;
    private final DynamicMultiSelectList soaOtherParties;
    private final YesOrNo soaCafcassServedOptions;
    private final String soaCafcassEmailId;
    private final YesOrNo soaCafcassCymruServedOptions;
    private final String soaCafcassCymruEmail;
    private final YesOrNo soaServeLocalAuthorityYesOrNo;
    private final String soaLaEmailAddress;
    private final YesOrNo soaServeC8ToLocalAuthorityYesOrNo;
    private final List<Element<DynamicList>> soaDocumentDynamicListForLa;
    private final YesOrNo proceedToServing;
}
