package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.UnderTakingEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.YesNoNotRequiredEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.time.LocalDate;
import java.util.List;


@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class DraftOrder {
    private final String typeOfOrder;
    private CreateSelectOrderOptionsEnum orderType;
    private String orderTypeId;
    private Document orderDocument;
    private Document orderDocumentWelsh;
    private OtherDraftOrderDetails otherDetails;
    private String judgeNotes;
    private String adminNotes;
    private FL404 fl404CustomFields;
    private final YesOrNo isTheOrderByConsent;
    private final YesOrNo wasTheOrderApprovedAtHearing;
    private final JudgeOrMagistrateTitleEnum judgeOrMagistrateTitle;
    private final String judgeOrMagistratesLastName;
    private final String justiceLegalAdviserFullName;
    @JsonProperty("magistrateLastName")
    private final List<Element<MagistrateLastName>> magistrateLastName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOrderMade;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate approvalDate;
    private final YesNoNotRequiredEnum isTheOrderAboutAllChildren;
    private final String recitalsOrPreamble;
    @JsonProperty("orderDirections")
    private final String orderDirections;
    @JsonProperty("furtherDirectionsIfRequired")
    private final String furtherDirectionsIfRequired;
    @JsonProperty("furtherInformationIfRequired")
    private final String furtherInformationIfRequired;
    private final String parentName;
    private List<Element<AppointedGuardianFullName>> appointedGuardianName;
    private final String manageOrdersFl402CourtName;
    private final Address manageOrdersFl402CourtAddress;
    private final String manageOrdersFl402CaseNo;
    private final String manageOrdersFl402Applicant;
    private final String manageOrdersFl402ApplicantRef;
    private final String fl402HearingCourtname;
    private final Address fl402HearingCourtAddress;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersDateOfhearing;
    private final String dateOfHearingTime;
    private final String dateOfHearingTimeEstimate;

    /**
     * C43.
     */
    @JsonProperty("childArrangementsOrdersToIssue")
    private final List<OrderTypeEnum> childArrangementsOrdersToIssue;
    @JsonProperty("selectChildArrangementsOrder")
    private final ChildArrangementOrderTypeEnum selectChildArrangementsOrder;

    /**
     * C47A.
     */
    @JsonProperty("cafcassOfficeDetails")
    private final String cafcassOfficeDetails;

    /**
     * N117.
     */
    private final String manageOrdersCourtName;
    @JsonProperty("manageOrdersCourtAddress")
    private final Address manageOrdersCourtAddress;
    private final String manageOrdersCaseNo;
    private final String manageOrdersApplicant;
    private final String manageOrdersApplicantReference;
    private final String manageOrdersRespondent;
    private final String manageOrdersRespondentReference;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersRespondentDob;
    @JsonProperty("manageOrdersRespondentAddress")
    private final Address manageOrdersRespondentAddress;
    private final YesOrNo manageOrdersUnderTakingRepr;
    private final UnderTakingEnum underTakingSolicitorCounsel;
    private final String manageOrdersUnderTakingPerson;
    @JsonProperty("manageOrdersUnderTakingAddress")
    private final Address manageOrdersUnderTakingAddress;
    private final String manageOrdersUnderTakingTerms;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersDateOfUnderTaking;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate underTakingDateExpiry;
    private final String underTakingExpiryTime;
    private final YesOrNo underTakingFormSign;

    private final String orderSelectionType;
    private final String orderCreatedBy;
    @JsonProperty("isOrderUploadedByJudgeOrAdmin")
    private final YesOrNo isOrderUploadedByJudgeOrAdmin;
    private final String childrenList;
    @JsonProperty("manageOrderHearingDetails")
    private final List<Element<HearingData>> manageOrderHearingDetails;
    private final YesOrNo isTheOrderAboutChildren;
    @JsonProperty("childOption")
    private final DynamicMultiSelectList childOption;
    private final C21OrderOptionsEnum c21OrderOptions;
    //PRL-3318 - Added for storing hearing dropdown
    private DynamicList hearingsType;

    @JsonProperty("hasJudgeProvidedHearingDetails")
    private YesOrNo hasJudgeProvidedHearingDetails;

    private final SdoDetails sdoDetails;

    @JsonIgnore
    public String getLabelForOrdersDynamicList() {
        log.info("orderTypeId {},orderTypeId {}", this.orderType, this.orderTypeId);
        return String.format(
            "%s",
            this.orderTypeId
        );
    }
}
