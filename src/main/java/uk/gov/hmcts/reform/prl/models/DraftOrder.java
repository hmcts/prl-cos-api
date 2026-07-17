package uk.gov.hmcts.reform.prl.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.C21OrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.JudgeOrMagistrateTitleEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.UnderTakingEnum;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.FL404;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;


@Slf4j
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder(toBuilder = true)
public class DraftOrder {
    @CCD(label = "Final General Interim ?", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String typeOfOrder;
    @CCD(label = "Order type id", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private CreateSelectOrderOptionsEnum orderType;
    @CCD(label = "Type of order", searchable = false)
    private String orderTypeId;
    @CCD(label = "English document", categoryID = "draftOrders", searchable = false)
    private Document orderDocument;
    @CCD(label = "Welsh document", categoryID = "draftOrders", searchable = false)
    private Document orderDocumentWelsh;
    @CCD(label = "Other details", searchable = false)
    private OtherDraftOrderDetails otherDetails;
    @CCD(label = "Notes", searchable = false)
    private String judgeNotes;
    @CCD(label = "Court admin notes", searchable = false)
    private String adminNotes;
    @CCD(label = "Custom fields", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private FL404 fl404CustomFields;
    @CCD(
            label = "Is the order by consent?",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isTheOrderByConsent;
    @CCD(
            label = "Was the order approved at a hearing?",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo wasTheOrderApprovedAtHearing;
    @CCD(
            label = "Select or amend the title of the Judge or magistrate",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false
    )
    private final JudgeOrMagistrateTitleEnum judgeOrMagistrateTitle;
    @CCD(label = "Last name", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String judgeOrMagistratesLastName;
    @CCD(
            label = "Justice's Legal Adviser's full name",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false
    )
    private final String justiceLegalAdviserFullName;
    @CCD(label = "Magistrate's full name", searchable = false)
    @JsonProperty("magistrateLastName")
    private final List<Element<MagistrateLastName>> magistrateLastName;
    @CCD(label = "Date order made", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOrderMade;
    @CCD(label = "Approval date", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate approvalDate;
    @CCD(label = "Is the order about all the children?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isTheOrderAboutAllChildren;
    @CCD(label = "Add recitals or preamble", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String recitalsOrPreamble;
    @CCD(label = "Add directions", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("orderDirections")
    private final String orderDirections;
    @CCD(
            label = "Add further directions if these are required",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false
    )
    @JsonProperty("furtherDirectionsIfRequired")
    private final String furtherDirectionsIfRequired;
    @CCD(
            label = "Add further information if this is required",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false
    )
    @JsonProperty("furtherInformationIfRequired")
    private final String furtherInformationIfRequired;
    @CCD(label = "Full name", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String parentName;
    @CCD(
            label = "Guardian Full Name",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "appointedGuardianFullName"
    )
    private List<Element<AppointedGuardianFullName>> appointedGuardianName;
    @CCD(label = "Court name", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String manageOrdersFl402CourtName;
    @CCD(
            label = "Court address",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.AddressUK
    )
    private final Address manageOrdersFl402CourtAddress;
    @CCD(label = "Case number", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String manageOrdersFl402CaseNo;
    @CCD(label = "Applicant name", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String manageOrdersFl402Applicant;
    @CCD(label = "Applicant reference", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String manageOrdersFl402ApplicantRef;
    @CCD(label = "Court name for hearing", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String fl402HearingCourtname;
    @CCD(
            label = "Court address for hearing",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.AddressUK
    )
    private final Address fl402HearingCourtAddress;
    @CCD(label = "Date and place of hearing", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersDateOfhearing;
    @CCD(label = "Time", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String dateOfHearingTime;
    @CCD(label = "Time estimate", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String dateOfHearingTimeEstimate;

    /**
     * C43.
     */
    @CCD(label = "Select orders to issue", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("childArrangementsOrdersToIssue")
    private final List<OrderTypeEnum> childArrangementsOrdersToIssue;
    @CCD(
            label = "Select type of child arrangements order",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false
    )
    @JsonProperty("selectChildArrangementsOrder")
    private final ChildArrangementOrderTypeEnum selectChildArrangementsOrder;

    /**
     * C47A.
     */
    @CCD(label = "Add details", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("cafcassOfficeDetails")
    private final String cafcassOfficeDetails;

    /**
     * N117.
     */
    @CCD(label = "Court name", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String manageOrdersCourtName;
    @CCD(
            label = "Court address",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.AddressUK
    )
    @JsonProperty("manageOrdersCourtAddress")
    private final Address manageOrdersCourtAddress;
    @CCD(label = "Case number", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String manageOrdersCaseNo;
    @CCD(label = "Applicant name", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("manageOrdersApplicant")
    private final String manageOrdersApplicant;
    @CCD(label = "Applicant reference", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("manageOrdersApplicantReference")
    private final String manageOrdersApplicantReference;
    @CCD(label = "Respondent name", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("manageOrdersRespondent")
    private final String manageOrdersRespondent;
    @CCD(label = "Respondent reference", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("manageOrdersRespondentReference")
    private final String manageOrdersRespondentReference;
    @CCD(label = "Respondent date of birth", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersRespondentDob;
    @CCD(
            label = "Respondent address",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.AddressUK
    )
    @JsonProperty("manageOrdersRespondentAddress")
    private final Address manageOrdersRespondentAddress;
    @CCD(
            label = "Is the person giving the undertaking represented?",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo manageOrdersUnderTakingRepr;
    @CCD(
            label = "Are they represented by solicitor or counsel?",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false
    )
    private final UnderTakingEnum underTakingSolicitorCounsel;
    @CCD(label = "Person giving undertaking", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String manageOrdersUnderTakingPerson;
    @CCD(
            label = "Address of person giving undertaking",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.AddressUK
    )
    @JsonProperty("manageOrdersUnderTakingAddress")
    private final Address manageOrdersUnderTakingAddress;
    @CCD(
            label = "Terms of undertaking",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.TextArea
    )
    private final String manageOrdersUnderTakingTerms;
    @CCD(label = "Date of undertaking", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersDateOfUnderTaking;
    @CCD(label = "Expiry date of undertaking", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate underTakingDateExpiry;
    @CCD(label = "Time", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final String underTakingExpiryTime;
    @CCD(label = " ", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("underTakingExpiryDateTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final LocalDateTime underTakingExpiryDateTime;
    @CCD(
            label = "Must the person giving the undertaking sign the statement on the form?",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo underTakingFormSign;

    @CCD(label = "Order options", showCondition = "orderSelectionType=\"DO_NOT_SHOW\"", searchable = false)
    private final String orderSelectionType;
    @CCD(label = "Order options", showCondition = "orderCreatedBy=\"DO_NOT_SHOW\"", searchable = false)
    private final String orderCreatedBy;
    //Below field not only for admin and judge but also holds solicitors,
    // naming convention was wrong due to the requirement change
    @CCD(
            label = " ",
            showCondition = "orderSelectionType=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("isOrderUploadedByJudgeOrAdmin")
    private final YesOrNo isOrderUploadedByJudgeOrAdmin;
    @CCD(label = "Children list ", searchable = false)
    private final String childrenList;
    @CCD(label = " ", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    @JsonProperty("manageOrderHearingDetails")
    private List<Element<HearingData>> manageOrderHearingDetails;
    @CCD(label = "Is the order about the children?", searchable = false, typeOverride = FieldType.YesOrNo)
    private final YesOrNo isTheOrderAboutChildren;
    @CCD(
            label = " ",
            showCondition = "orderSelectionType=\"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList
    )
    @JsonProperty("childOption")
    private final DynamicMultiSelectList childOption;
    @CCD(label = "Type of c21 order", showCondition = "orderTypeId = \"DO_NOT_SHOW\"", searchable = false)
    private final C21OrderOptionsEnum c21OrderOptions;
    //PRL-3318 - Added for storing hearing dropdown
    @CCD(
            label = "At which hearing was the order approved?",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.DynamicList
    )
    private DynamicList hearingsType;
    @CCD(
            label = " ",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    @JsonProperty("hasJudgeProvidedHearingDetails")
    private YesOrNo hasJudgeProvidedHearingDetails;

    @CCD(label = "SDO details", showCondition = "orderSelectionType=\"DO_NOT_SHOW\"", searchable = false)
    private final SdoDetails sdoDetails;
    @CCD(
            label = "Is the order created by solicitor ?",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private final YesOrNo isOrderCreatedBySolicitor;

    @CCD(
            label = " ",
            showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
            searchable = false,
            typeOverride = FieldType.YesOrNo
    )
    private YesOrNo isAutoHearingReqPending;

    @JsonIgnore
    public String getLabelForOrdersDynamicList() {
        return String.format(
            "%s - %s",
            this.orderTypeId,
            this.getOtherDetails().getDateCreated().format(DateTimeFormatter.ofPattern(
                PrlAppsConstants.D_MMM_YYYY_HH_MM,
                Locale.ENGLISH
            ))
        );
    }

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @CCD(
          label = "Is the  request to withdraw the application approved?",
          showCondition = "orderTypeId = \"DO_NOT_SHOW\"",
          searchable = false
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isCaseWithdrawn;
  // ==== end synthesised definition-only fields ====
}
