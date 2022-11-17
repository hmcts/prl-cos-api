package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.YesNoNotRequiredEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class DraftOrder {
    private final String typeOfOrder;
    private String orderType;
    private String orderTypeId;
    private Document orderDocument;
    private OtherDraftOrderDetails otherDetails;
    private String judgeNotes;
    private String adminNotes;
    private FL404 fl404CustomFields;
    @JsonProperty("isOrderDrawnForCafcass")
    private final YesOrNo isOrderDrawnForCafcass;
    private final YesOrNo isCaseWithdrawn;
    private final YesOrNo isTheOrderByConsent;
    private final SelectTypeOfOrderEnum selectTypeOfOrder;
    private final YesOrNo doesOrderClosesCase;
    private final YesOrNo wasTheOrderApprovedAtHearing;
    private final JudgeOrMagistrateTitleEnum judgeOrMagistrateTitle;
    private final String judgeOrMagistratesLastName;
    private final String justiceLegalAdviserFullName;
    @JsonProperty("magistrateLastName")
    private final List<Element<MagistrateLastName>> magistrateLastName;
    private final LocalDate dateOrderMade;
    private final YesNoNotRequiredEnum isTheOrderAboutAllChildren;
    private final String recitalsOrPreamble;
    @JsonProperty("orderDirections")
    private final String orderDirections;
    @JsonProperty("furtherDirectionsIfRequired")
    private final String furtherDirectionsIfRequired;

    @JsonIgnore
    public String getLabelForOrdersDynamicList() {
        log.info("orderTypeId {},orderTypeId {}", this.orderType, this.orderTypeId);
        return String.format(
            "%s",
            this.orderTypeId
        );
    }
}
