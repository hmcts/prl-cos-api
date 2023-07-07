package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("cafcassOrCymruNeedToProvideReport")
    private final YesOrNo cafcassOrCymruNeedToProvideReport;
    @JsonProperty("cafcassCymruDocuments")
    private final List<CafcassCymruDocumentsEnum> cafcassCymruDocuments;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate whenReportsMustBeFiled;
    @JsonProperty("orderEndsInvolvementOfCafcassOrCymru")
    private final YesOrNo orderEndsInvolvementOfCafcassOrCymru;
    @JsonProperty("doYouWantToServeOrder")
    private final YesOrNo doYouWantToServeOrder;
    @JsonProperty("whatDoWithOrder")
    private final WhatToDoWithOrderEnum whatDoWithOrder;
}
