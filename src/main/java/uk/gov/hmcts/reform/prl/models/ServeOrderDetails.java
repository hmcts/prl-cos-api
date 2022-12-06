package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class ServeOrderDetails {

    private final List<Document> additionalDocuments;
    private final YesOrNo serveOnRespondent;
    private final ServingRespondentsEnum servingRespondent;
    private final YesOrNo cafcassServed;
    private final YesOrNo otherPartiesServed;
    private final PostalInformation postalInformation;
    private final EmailInformation emailInformation;

}
