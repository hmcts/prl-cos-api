package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.editandapprove.OrderApprovalDecisionsForCourtAdminOrderEnum;
import uk.gov.hmcts.reform.prl.enums.editandapprove.OrderApprovalDecisionsForSolicitorOrderEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.AmendOrderCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ApplicantOccupationEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DeliveryByEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrLegalAdvisorCheckEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrganisationOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.RespondentOccupationEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ServeOtherPartiesOptions;
import uk.gov.hmcts.reform.prl.enums.manageorders.UnderTakingEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.WithDrawTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.judicial.JudicialUser;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404b;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.EmailInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.PostalInformation;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.ServeOrgDetails;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ManageOrders implements MappableObject {

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
    @JsonProperty("furtherInformationIfRequired")
    private final String furtherInformationIfRequired;
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
    @JsonProperty("courtDeclares2")
    private final List<ApplicantOccupationEnum> courtDeclares2;
    private final String homeRights;
    private final String applicantInstructions;
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
    @JsonProperty("underTakingExpiryDateTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final LocalDateTime underTakingExpiryDateTime;
    private final YesOrNo underTakingFormSign;

    private final YesOrNo isTheOrderByConsent;
    private final JudgeOrMagistrateTitleEnum judgeOrMagistrateTitle;

    private Document manageOrdersDocumentToAmend;
    private Document manageOrdersAmendedOrder;
    private DynamicList amendOrderDynamicList;

    /**
     * C45A.
     */
    @JsonProperty("parentName")
    private String parentName;

    @JsonProperty("fl404CustomFields")
    private final FL404 fl404CustomFields;
    //FL402
    private final String manageOrdersFl402CourtName;
    private final Address manageOrdersFl402CourtAddress;
    private final String manageOrdersFl402CaseNo;
    private final String manageOrdersFl402Applicant;
    private final String manageOrdersFl402ApplicantRef;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersDateOfhearing;
    private final String dateOfHearingTime;
    private final String dateOfHearingTimeEstimate;
    private final String fl402HearingCourtname;
    private final Address fl402HearingCourtAddress;

    @JsonProperty("fl404bCustomFields")
    private final FL404b fl404bCustomFields;

    @Builder.Default
    @JsonProperty("childOption")
    private final DynamicMultiSelectList childOption;

    private final YesOrNo daOrderForCaCase;

    @Builder.Default
    @JsonProperty("serveOrderDynamicList")
    private DynamicMultiSelectList serveOrderDynamicList;
    @JsonProperty("serveOrderAdditionalDocuments")
    private final List<Element<Document>> serveOrderAdditionalDocuments;

    private final YesOrNo serveToRespondentOptions;
    @JsonProperty("servingOptionsForNonLegalRep")
    private final SoaCitizenServingRespondentsEnum servingOptionsForNonLegalRep;
    private final SoaSolicitorServingRespondentsEnum personallyServeRespondentsOptions;
    private final DynamicMultiSelectList recipientsOptions;
    private final DynamicMultiSelectList otherParties;
    private final YesOrNo cafcassServedOptions;
    private final String cafcassEmailId;
    private final YesOrNo cafcassCymruServedOptions;
    private final String cafcassCymruEmail;
    @JsonProperty("serveOtherPartiesCA")
    private final List<OtherOrganisationOptions> serveOtherPartiesCA;
    @JsonProperty("serveOrgDetailsList")
    private final List<Element<ServeOrgDetails>> serveOrgDetailsList;
    private final List<ServeOtherPartiesOptions> serveOtherPartiesDA;

    @JsonProperty("withdrawnOrRefusedOrder")
    private final WithDrawTypeOfOrderEnum withdrawnOrRefusedOrder;
    @JsonProperty("ordersNeedToBeServed")
    private final YesOrNo ordersNeedToBeServed;
    @JsonProperty("isTheOrderAboutChildren")
    private final YesOrNo isTheOrderAboutChildren;
    @JsonProperty("isTheOrderAboutAllChildren")
    private final YesOrNo isTheOrderAboutAllChildren;
    @JsonProperty("loggedInUserType")
    private final String loggedInUserType;
    @JsonProperty("judgeDirectionsToAdminAmendOrder")
    private final String judgeDirectionsToAdminAmendOrder;

    @JsonProperty("amendOrderSelectCheckOptions")
    private final AmendOrderCheckEnum amendOrderSelectCheckOptions;
    @JsonProperty("amendOrderSelectJudgeOrLa")
    private final JudgeOrLegalAdvisorCheckEnum amendOrderSelectJudgeOrLa;
    @JsonProperty("nameOfJudgeAmendOrder")
    private final String nameOfJudgeAmendOrder;
    @JsonProperty("nameOfLaAmendOrder")
    private final String nameOfLaAmendOrder;
    @JsonProperty("nameOfJudgeToReviewOrder")
    private final JudicialUser nameOfJudgeToReviewOrder;
    @JsonProperty("nameOfLaToReviewOrder")
    private final DynamicList nameOfLaToReviewOrder;

    @JsonProperty("previewUploadedOrder")
    private Document previewUploadedOrder;
    @JsonProperty("orderUploadedAsDraftFlag")
    private YesOrNo orderUploadedAsDraftFlag;
    @JsonProperty("makeChangesToUploadedOrder")
    private YesOrNo makeChangesToUploadedOrder;
    @JsonProperty("editedUploadOrderDoc")
    private Document editedUploadOrderDoc;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime currentOrderCreatedDateTime;

    @JsonProperty("c21OrderOptions")
    private final C21OrderOptionsEnum c21OrderOptions;
    @JsonProperty("typeOfC21Order")
    private String typeOfC21Order;

    @JsonProperty("ordersHearingDetails")
    @JsonUnwrapped
    @Builder.Default
    private List<Element<HearingData>> ordersHearingDetails;

    @JsonProperty("solicitorOrdersHearingDetails")
    @JsonUnwrapped
    @Builder.Default
    private List<Element<HearingData>> solicitorOrdersHearingDetails;

    @JsonProperty("hasJudgeProvidedHearingDetails")
    private YesOrNo hasJudgeProvidedHearingDetails;

    @JsonProperty("markedToServeEmailNotification")
    private YesOrNo markedToServeEmailNotification;

    //PRL-3254 - Added for populating hearing dropdown
    private DynamicList hearingsType;

    //PRL-4216 - serve order additional documents
    @JsonProperty("additionalOrderDocuments")
    private List<Element<AdditionalOrderDocument>> additionalOrderDocuments;

    @JsonProperty("whatToDoWithOrderSolicitor")
    private OrderApprovalDecisionsForSolicitorOrderEnum whatToDoWithOrderSolicitor;
    @JsonProperty("whatToDoWithOrderCourtAdmin")
    private OrderApprovalDecisionsForCourtAdminOrderEnum whatToDoWithOrderCourtAdmin;
    @JsonProperty("instructionsToLegalRepresentative")
    private String instructionsToLegalRepresentative;

    private Object rejectedOrdersDynamicList;
    private String editOrderTextInstructions;

    @JsonProperty("displayLegalRepOption")
    private String displayLegalRepOption;

    private final DeliveryByEnum deliveryByOptionsCaOnlyC47a;
    @JsonProperty("emailInformationCaOnlyC47a")
    private final List<Element<EmailInformation>> emailInformationCaOnlyC47a;
    @JsonProperty("postalInformationCaOnlyC47a")
    private final List<Element<PostalInformation>> postalInformationCaOnlyC47a;
    private final DeliveryByEnum deliveryByOptionsDA;
    @JsonProperty("emailInformationDA")
    private final List<Element<EmailInformation>> emailInformationDA;
    @JsonProperty("postalInformationDA")
    private final List<Element<PostalInformation>> postalInformationDA;
    @JsonProperty("serveOtherPartiesCaOnlyC47a")
    private final List<OtherOrganisationOptions> serveOtherPartiesCaOnlyC47a;
    private final DeliveryByEnum deliveryByOptionsCA;
    @JsonProperty("emailInformationCA")
    private final List<Element<EmailInformation>> emailInformationCA;
    @JsonProperty("postalInformationCA")
    private final List<Element<PostalInformation>> postalInformationCA;

    /*
    * Unused fields
    * */
    private final YesOrNo isOnlyC47aOrderSelectedToServe;
    private final YesOrNo otherPeoplePresentInCaseFlag;
    private final YesOrNo serveToRespondentOptionsOnlyC47a;
    private final SoaSolicitorServingRespondentsEnum servingRespondentsOptionsCaOnlyC47a;
    private final DynamicMultiSelectList recipientsOptionsOnlyC47a;
    private final DynamicMultiSelectList otherPartiesOnlyC47a;
    private final SoaSolicitorServingRespondentsEnum servingRespondentsOptionsCA;
    private final SoaSolicitorServingRespondentsEnum servingRespondentsOptionsDA;
}
