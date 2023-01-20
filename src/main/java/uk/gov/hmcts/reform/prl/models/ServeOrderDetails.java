package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.CafcassCymruDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class ServeOrderDetails {

    private final List<Element<Document>> additionalDocuments;
    private final YesOrNo serveOnRespondent;
    private final ServingRespondentsEnum servingRespondent;
    private final YesOrNo cafcassServed;
    private final String cafcassEmail;
    private final YesOrNo otherPartiesServed;
    private final List<Element<PostalInformation>> postalInformation;
    private final List<Element<EmailInformation>> emailInformation;

    private final YesOrNo cafcassOrCymruNeedToProvideReport;
    private final List<CafcassCymruDocumentsEnum> cafcassCymruDocuments;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate whenReportsMustBeFiled;
    private final YesOrNo orderEndsInvolvementOfCafcassOrCymru;
    private final YesOrNo doYouWantToServeOrder;
    private final WhatToDoWithOrderEnum whatDoWithOrder;
}
