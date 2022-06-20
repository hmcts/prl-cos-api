package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ApplicantOccupationEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildSelectorEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.RespondentOccupationEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.UnderTakingEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404b;
import uk.gov.hmcts.reform.prl.models.documents.Document;

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
    private final String courtName1;
    private final Address courtAddress;
    private final String caseNumber;
    private final String applicantName1;
    private final String applicantReference;
    private final String respondentReference;
    private final String orderRespondentName;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentDateOfBirth;
    private final Address respondentAddress;
    private final Address addressTheOrderAppliesTo;
    private final String courtDeclares;
    @JsonProperty("courtDeclares2")
    private final List<ApplicantOccupationEnum> courtDeclares2;
    private final String homeRights;
    private final String applicantInstructions;
    private final String theRespondent;
    @JsonProperty("theRespondent2")
    private final List<RespondentOccupationEnum> theRespondent2;
    private final YesOrNo powerOfArrest1;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentDay1;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentDay2;
    private String respondentStartTime;
    private String respondentEndTime;
    private final YesOrNo powerOfArrest2;
    private final String whenTheyLeave;
    private final YesOrNo powerOfArrest3;
    private final String moreDetails;
    private final YesOrNo powerOfArrest4;
    private final String instructionRelating;
    private final YesOrNo powerOfArrest5;
    private final YesOrNo powerOfArrest6;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOrderMade1;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOrderEnds;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate datePlaceHearing;
    private String datePlaceHearingTime;
    private String dateOrderEndsTime;
    private final String courtName2;
    private final Address ukPostcode2;
    private final String applicantCost;
    private final String orderNotice;
    private final String hearingTimeEstimate;

    /**
     * C43.
     */
    @JsonProperty("childArrangementsOrdersToIssue")
    private final List<OrderTypeEnum> childArrangementsOrdersToIssue;
    @JsonProperty("selectChildArrangementsOrder")
    private final ChildArrangementOrderTypeEnum selectChildArrangementsOrder;


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

    /**
     * C45A.
     */
    @JsonProperty("parentName")
    private String parentName;

    @JsonProperty("fl404CustomFields")
    private final FL404 fl404CustomFields;
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
