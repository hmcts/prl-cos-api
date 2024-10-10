package uk.gov.hmcts.reform.prl.models.serviceofdocuments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoNotApplicable;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.AdditionalRecipients;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.ServiceOfDocumentsCheckEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.SodCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.SodSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.DocumentsDynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.ServeOrgDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofdocuments.SodPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;

import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceOfDocuments {

    @JsonProperty("servedDocumentsDetailsList")
    private List<Element<ServedApplicationDetails>> servedDocumentsDetailsList;

    @JsonProperty("sodDocumentsList")
    private List<Element<DocumentsDynamicList>> sodDocumentsList;

    @JsonProperty("sodAdditionalDocumentsList")
    private List<Element<Document>> sodAdditionalDocumentsList;

    @JsonProperty("sodAdditionalRecipients")
    private List<AdditionalRecipients> sodAdditionalRecipients;

    @JsonProperty("sodAdditionalRecipientsList")
    private List<Element<ServeOrgDetails>> sodAdditionalRecipientsList;

    @JsonProperty("sodDocumentsCheckOptions")
    private ServiceOfDocumentsCheckEnum sodDocumentsCheckOptions;

    private final SodSolicitorServingRespondentsEnum sodSolicitorServingRespondentsOptions;
    private final SodCitizenServingRespondentsEnum sodCitizenServingRespondentsOptions;

    private final SodPack sodUnServedPack;

    @JsonProperty("sodServeToRespondentOptions")
    private final YesNoNotApplicable sodServeToRespondentOptions;

    private final YesOrNo canDocumentsBeServed;

}
