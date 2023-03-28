package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serveorder.CafcassCymruDocumentsEnum;
import uk.gov.hmcts.reform.prl.enums.serveorder.WhatToDoWithOrderEnum;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServeOrderData {
    private final YesOrNo cafcassOrCymruNeedToProvideReport;
    private final List<CafcassCymruDocumentsEnum> cafcassCymruDocuments;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate whenReportsMustBeFiled;
    private final YesOrNo orderEndsInvolvementOfCafcassOrCymru;
    private final YesOrNo doYouWantToServeOrder;
    private final WhatToDoWithOrderEnum whatDoWithOrder;
}
