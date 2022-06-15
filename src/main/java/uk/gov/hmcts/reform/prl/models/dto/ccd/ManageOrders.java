package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildSelectorEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.UnderTakingEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404b;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManageOrders {

    private final String childListForSpecialGuardianship;
    @JsonProperty("cafcassOfficeDetails")
    private final String cafcassOfficeDetails;
    @JsonProperty("cafcassEmailAddress")
    private final List<Element<String>> cafcassEmailAddress;
    @JsonProperty("otherEmailAddress")
    private final List<Element<String>> otherEmailAddress;
    @JsonProperty("isCaseWithdrawn")
    private final YesOrNo isCaseWithdrawn;
    @JsonProperty("recitalsOrPreamble")
    private final String recitalsOrPreamble;
    @JsonProperty("orderDirections")
    private final String orderDirections;
    @JsonProperty("furtherDirectionsIfRequired")
    private final String furtherDirectionsIfRequired;

    //N117
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

    private final YesOrNo isTheOrderByConsent;
    private final JudgeOrMagistrateTitleEnum judgeOrMagistrateTitle;

    private Document manageOrdersDocumentToAmend;
    private Document manageOrdersAmendedOrder;
    private DynamicList amendOrderDynamicList;


    @JsonProperty("childSelectorOption1")
    private final List<ChildSelectorEnum> childSelectorOption1;
    @JsonProperty("childSelectorOption2")
    private final List<ChildSelectorEnum> childSelectorOption2;
    @JsonProperty("childSelectorOption3")
    private final List<ChildSelectorEnum> childSelectorOption3;
    @JsonProperty("childSelectorOption4")
    private final List<ChildSelectorEnum> childSelectorOption4;
    @JsonProperty("childSelectorOption5")
    private final List<ChildSelectorEnum> childSelectorOption5;
    @JsonProperty("childSelectorOption6")
    private final List<ChildSelectorEnum> childSelectorOption6;
    @JsonProperty("childSelectorOption7")
    private final List<ChildSelectorEnum> childSelectorOption7;
    @JsonProperty("childSelectorOption8")
    private final List<ChildSelectorEnum> childSelectorOption8;
    @JsonProperty("childSelectorOption9")
    private final List<ChildSelectorEnum> childSelectorOption9;
    @JsonProperty("childSelectorOption10")
    private final List<ChildSelectorEnum> childSelectorOption10;
    @JsonProperty("childSelectorOption11")
    private final List<ChildSelectorEnum> childSelectorOption11;
    @JsonProperty("childSelectorOption12")
    private final List<ChildSelectorEnum> childSelectorOption12;
    @JsonProperty("childSelectorOption13")
    private final List<ChildSelectorEnum> childSelectorOption13;
    @JsonProperty("childSelectorOption14")
    private final List<ChildSelectorEnum> childSelectorOption14;
    @JsonProperty("childSelectorOption15")
    private final List<ChildSelectorEnum> childSelectorOption15;

    //FL402
    private final String manageOrdersFl402CourtName;
    @JsonIgnore
    private final Address manageOrdersFl402CourtAddress;
    private final String manageOrdersFl402CaseNo;
    private final String manageOrdersFl402Applicant;
    private final String manageOrdersFl402ApplicantRef;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersDateOfhearing;
    private final String dateOfHearingTime;
    private final String dateOfHearingTimeEstimate;
    private final String fl402HearingCourtname;
    @JsonIgnore
    private final Address fl402HearingCourtAddress;

    @JsonProperty("fl404bCustomFields")
    private final FL404b fl404bCustomFields;

}
