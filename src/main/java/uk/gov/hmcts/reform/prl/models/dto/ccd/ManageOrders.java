package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoNotApplicable;
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
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCourtnavCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudPlus1RolesWyirhlAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassCaseworkerPrivatelawSolicitorRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCruCourtnavRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRPlus2RolesOwnrpcAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRCaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORCruPlus33RolesFipgzgAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCrudAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManageOrders implements MappableObject {

    @CCD(
            label = "For which child?",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
    )
    private final String childListForSpecialGuardianship;
    @CCD(
            label = "Add name of officer",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("cafcassOfficeDetails")
    private final String cafcassOfficeDetails;
    @CCD(
            label = "Email address",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Email",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class, CaseworkerWaTaskConfigurationCrudAccess.class}
    )
    @JsonProperty("cafcassEmailAddress")
    private final List<Element<String>> cafcassEmailAddress;
    @CCD(
            label = "Email address",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Email",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class, CaseworkerWaTaskConfigurationCrudAccess.class}
    )
    @JsonProperty("otherEmailAddress")
    private final List<Element<String>> otherEmailAddress;
    @CCD(
            label = "Is the  request to withdraw the application approved?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("isCaseWithdrawn")
    private final YesOrNo isCaseWithdrawn;
    @CCD(
            label = "Add recitals or preamble",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("recitalsOrPreamble")
    private final String recitalsOrPreamble;
    @CCD(
            label = "Add directions",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("orderDirections")
    private final String orderDirections;
    @CCD(
            label = "Add further directions, if these are required",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("furtherDirectionsIfRequired")
    private final String furtherDirectionsIfRequired;
    @CCD(
            label = "Add further information if this is required",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("furtherInformationIfRequired")
    private final String furtherInformationIfRequired;
    @CCD(
            label = "Court name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final String courtName1;
    @CCD(
            label = "Court address",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final Address courtAddress;
    @CCD(
            label = "Case number",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final String caseNumber;
    @CCD(
            label = "Applicant name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final String applicantName1;
    @CCD(
            label = "Applicant reference",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final String applicantReference;
    @CCD(
            label = "Respondent reference",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final String respondentReference;
    @CCD(
            label = "Respondent name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final String orderRespondentName;
    @CCD(
            label = "Respondent date of birth",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentDateOfBirth;
    @CCD(
            label = "Respondent address",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final Address respondentAddress;
    @CCD(
            label = "Address the order applies to",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final Address addressTheOrderAppliesTo;
    @CCD(
            label = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "courtDeclaresEnum",
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonProperty("courtDeclares2")
    private final List<ApplicantOccupationEnum> courtDeclares2;
    @CCD(
            label = "Add details about home rights",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final String homeRights;
    @CCD(
            label = "Add another instruction relating to the applicant",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final String applicantInstructions;
    @CCD(
            label = "Select all that apply",
            searchable = false,
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "theRespondentEnum",
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonProperty("theRespondent2")
    private final List<RespondentOccupationEnum> theRespondent2;
    @CCD(
            label = "Is a power of arrest attached to this paragraph?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final YesOrNo powerOfArrest1;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentDay1;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate respondentDay2;
    @CCD(
            label = "Time",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private String respondentStartTime;
    @CCD(
            label = "Time",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private String respondentEndTime;
    @CCD(
            label = "Is a power of arrest attached to this paragraph?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final YesOrNo powerOfArrest2;
    @CCD(
            label = "Add when they shall leave",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final String whenTheyLeave;
    @CCD(
            label = "Is a power of arrest attached to this paragraph?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final YesOrNo powerOfArrest3;
    @CCD(
            label = "Add more details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRCourtnavCruAccess.class}
    )
    private final String moreDetails;
    @CCD(
            label = "Is a power of arrest attached to this paragraph?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final YesOrNo powerOfArrest4;
    @CCD(
            label = "Add another instruction relating to the respondent",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRCourtnavCruAccess.class}
    )
    private final String instructionRelating;
    @CCD(
            label = "Is a power of arrest attached to this paragraph?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final YesOrNo powerOfArrest5;
    @CCD(
            label = "Is a power of arrest attached to this paragraph?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final YesOrNo powerOfArrest6;
    @CCD(
            label = "Date order made",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOrderMade1;
    @CCD(
            label = "Date order ends and time",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOrderEnds;
    @CCD(
            label = "Date and  place of hearing",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate datePlaceHearing;
    @CCD(
            label = "Time",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class, CaseworkerPrivatelawLaCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private String datePlaceHearingTime;
    @CCD(
            label = "Time",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private String dateOrderEndsTime;
    @CCD(
            label = "Court name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final String courtName2;
    @CCD(
            label = "Enter UK postcode",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final Address ukPostcode2;
    @CCD(
            label = "is this order made with or without notice",
            searchable = false,
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "OrderNoticeEnum",
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final String orderNotice;
    @CCD(
            label = "Time estimate",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private final String hearingTimeEstimate;

    /**
     * C43.
     */
    @CCD(
            label = "Select orders to issue",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("childArrangementsOrdersToIssue")
    private final List<OrderTypeEnum> childArrangementsOrdersToIssue;
    @CCD(
            label = "Select type of child arrangements order",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("selectChildArrangementsOrder")
    private final ChildArrangementOrderTypeEnum selectChildArrangementsOrder;


    //N117
    @CCD(
            label = "Court name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String manageOrdersCourtName;
    @CCD(
            label = "Court address",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("manageOrdersCourtAddress")
    private final Address manageOrdersCourtAddress;
    @CCD(
            label = "Case number",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String manageOrdersCaseNo;
    @CCD(
            label = "Applicant name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String manageOrdersApplicant;
    @CCD(
            label = "Applicant reference",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String manageOrdersApplicantReference;
    @CCD(
            label = "Respondent name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String manageOrdersRespondent;
    @CCD(
            label = "Respondent reference",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String manageOrdersRespondentReference;
    @CCD(
            label = "Respondent date of birth",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersRespondentDob;
    @CCD(
            label = "Respondent address",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("manageOrdersRespondentAddress")
    private final Address manageOrdersRespondentAddress;
    @CCD(
            label = "Is the person giving the undertaking represented?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final YesOrNo manageOrdersUnderTakingRepr;
    @CCD(
            label = "Are they represented by solicitor or counsel?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final UnderTakingEnum underTakingSolicitorCounsel;
    @CCD(
            label = "Person giving undertaking",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String manageOrdersUnderTakingPerson;
    @CCD(
            label = "Address of person giving undertaking",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("manageOrdersUnderTakingAddress")
    private final Address manageOrdersUnderTakingAddress;
    @CCD(
            label = "Terms of undertaking",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String manageOrdersUnderTakingTerms;
    @CCD(
            label = "Date of undertaking",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersDateOfUnderTaking;
    @CCD(
            label = "Expiry date of undertaking",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate underTakingDateExpiry;
    @CCD(
            label = "Time",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String underTakingExpiryTime;
    @CCD(
            label = "Expiry date of undertaking",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesWyirhlAccess.class, CaseworkerWaTaskConfigurationCrudAccess.class}
    )
    @JsonProperty("underTakingExpiryDateTime")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private final LocalDateTime underTakingExpiryDateTime;
    @CCD(
            label = "Must the person giving the undertaking sign the statement on the form?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final YesOrNo underTakingFormSign;

    @CCD(
            label = "Is the order by consent?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final YesOrNo isTheOrderByConsent;
    @CCD(
            label = "Select or amend the title of the Judge or magistrate",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final JudgeOrMagistrateTitleEnum judgeOrMagistrateTitle;

    @CCD(
            label = " ",
            categoryID = "draftOrders",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCafcassCaseworkerPrivatelawSolicitorRAccess.class}
    )
    private Document manageOrdersDocumentToAmend;
    @CCD(
            label = " ",
            regex = ".pdf",
            categoryID = "draftOrders",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCafcassCaseworkerPrivatelawSolicitorRAccess.class}
    )
    private Document manageOrdersAmendedOrder;
    @CCD(
            label = "Select the order",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    private DynamicList amendOrderDynamicList;

    /**
     * C45A.
     */
    @CCD(
            label = "Full name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("parentName")
    private String parentName;

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    @JsonProperty("fl404CustomFields")
    private final FL404 fl404CustomFields;
    //FL402
    @CCD(
            label = "Court name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String manageOrdersFl402CourtName;
    @CCD(
            label = "Court address",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final Address manageOrdersFl402CourtAddress;
    @CCD(
            label = "Case number",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String manageOrdersFl402CaseNo;
    @CCD(
            label = "Applicant name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String manageOrdersFl402Applicant;
    @CCD(
            label = "Applicant reference",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String manageOrdersFl402ApplicantRef;
    @CCD(
            label = "Date and place of hearing",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate manageOrdersDateOfhearing;
    @CCD(
            label = "Time",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String dateOfHearingTime;
    @CCD(
            label = "Time estimate",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String dateOfHearingTimeEstimate;
    @CCD(
            label = "Court name for hearing",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final String fl402HearingCourtname;
    @CCD(
            label = "Court address for hearing",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private final Address fl402HearingCourtAddress;

    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
    )
    @JsonProperty("fl404bCustomFields")
    private final FL404b fl404bCustomFields;

    @CCD(
            label = "Which children are included in the order?",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @Builder.Default
    @JsonProperty("childOption")
    private final DynamicMultiSelectList childOption;

    @CCD(
            label = "DA Order for CA case",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CourtnavCruAccess.class}
    )
    private final YesOrNo daOrderForCaCase;

    @CCD(
            label = "Select the orders you plan to serve :",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess.class}
    )
    @Builder.Default
    @JsonProperty("serveOrderDynamicList")
    private DynamicMultiSelectList serveOrderDynamicList;
    @CCD(
            label = "Serve order additional documents",
            hint = "Upload additional documents that you plan to serve",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class}
    )
    @JsonProperty("serveOrderAdditionalDocuments")
    private final List<Element<Document>> serveOrderAdditionalDocuments;

    @CCD(
            label = "Does this order need to be personally served on the respondent?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
    )
    private final YesNoNotApplicable serveToRespondentOptions;
    @CCD(
            label = "Who is responsible for serving the respondent?",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruCourtnavRAccess.class}
    )
    @JsonProperty("servingOptionsForNonLegalRep")
    private final SoaCitizenServingRespondentsEnum servingOptionsForNonLegalRep;
    @CCD(
            label = "Who is responsible for serving the respondent?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus2RolesOwnrpcAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    private final SoaSolicitorServingRespondentsEnum personallyServeRespondentsOptions;
    @CCD(
            label = "Confirm Recipients",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess.class}
    )
    private final DynamicMultiSelectList recipientsOptions;
    @CCD(
            label = "Which other people in the case should the order be sent to?",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess.class}
    )
    private final DynamicMultiSelectList otherParties;
    @CCD(
            label = "Provide Cafcass access to this case?",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
    )
    private final YesOrNo cafcassServedOptions;
    @CCD(
            label = "Cafcass email address",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class}
    )
    private final String cafcassEmailId;
    @CCD(
            label = "Does Cafcass Cymru need to be served?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
    )
    private final YesOrNo cafcassCymruServedOptions;
    @CCD(
            label = "Cafcass Cymru email address",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
    )
    private final String cafcassCymruEmail;
    @CCD(
            label = " ",
            hint = "For example, DWP or the local authority",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess.class}
    )
    @JsonProperty("serveOtherPartiesCA")
    private final List<OtherOrganisationOptions> serveOtherPartiesCA;
    @CCD(
            label = "Organisation",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCrudAccess.class, CourtnavRAccess.class}
    )
    @JsonProperty("serveOrgDetailsList")
    private final List<Element<ServeOrgDetails>> serveOrgDetailsList;
    @CCD(
            label = "Any other party who needs to be served with this application",
            hint = "For example, DWP or the local authority",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess.class}
    )
    private final List<ServeOtherPartiesOptions> serveOtherPartiesDA;

    @CCD(
            label = "WithDrawn / Refused / No order made? ",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "WithDrawTypeOfOrderEnum",
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CitizenRAccess.class}
    )
    @JsonProperty("withdrawnOrRefusedOrder")
    private final WithDrawTypeOfOrderEnum withdrawnOrRefusedOrder;
    @CCD(
            label = "Order served flag",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("ordersNeedToBeServed")
    private final YesOrNo ordersNeedToBeServed;
    @CCD(
            label = "Is the order about the children?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("isTheOrderAboutChildren")
    private final YesOrNo isTheOrderAboutChildren;
    @CCD(
            label = "Is the order about all the children?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("isTheOrderAboutAllChildren")
    private final YesOrNo isTheOrderAboutAllChildren;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonProperty("loggedInUserType")
    private final String loggedInUserType;
    @CCD(
            label = "Directions to admin:",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonProperty("judgeDirectionsToAdminAmendOrder")
    private final String judgeDirectionsToAdminAmendOrder;

    @CCD(
            label = "Does someone need to check the order?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonProperty("amendOrderSelectCheckOptions")
    private final AmendOrderCheckEnum amendOrderSelectCheckOptions;
    @CCD(
            label = "Select judge or legal advisor",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonProperty("amendOrderSelectJudgeOrLa")
    private final JudgeOrLegalAdvisorCheckEnum amendOrderSelectJudgeOrLa;
    @CCD(
            label = "Name of judge",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonProperty("nameOfJudgeAmendOrder")
    private final String nameOfJudgeAmendOrder;
    @CCD(
            label = "Name of legal adviser",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonProperty("nameOfLaAmendOrder")
    private final String nameOfLaAmendOrder;
    @CCD(
            label = "Name of judge",
            searchable = false,
            typeOverride = FieldType.JudicialUser,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class, CaseworkerPrivatelawSolicitorRCaseworkerWaTaskConfigurationCruAccess.class}
    )
    @JsonProperty("nameOfJudgeToReviewOrder")
    private final JudicialUser nameOfJudgeToReviewOrder;
    @CCD(
            label = "Name of legal adviser",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonProperty("nameOfLaToReviewOrder")
    private final DynamicList nameOfLaToReviewOrder;

    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    @JsonProperty("previewUploadedOrder")
    private Document previewUploadedOrder;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class}
    )
    @JsonProperty("orderUploadedAsDraftFlag")
    private YesOrNo orderUploadedAsDraftFlag;
    @CCD(
            label = "Do you want to make changes and upload a new version of the order?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class}
    )
    @JsonProperty("makeChangesToUploadedOrder")
    private YesOrNo makeChangesToUploadedOrder;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    @JsonProperty("editedUploadOrderDoc")
    private Document editedUploadOrderDoc;
    @CCD(
            label = "Upload custom order template (.docx only)",
            hint = "Please upload a .docx file. Other formats will be rejected.",
            regex = ".docx",
            categoryID = "anyOtherDoc",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    @JsonProperty("customOrderDoc")
    private Document customOrderDoc;

    @CCD(
            label = "Current Order Created Date Time",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private final LocalDateTime currentOrderCreatedDateTime;

    @CCD(
            label = "Select the type of order",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    @JsonProperty("c21OrderOptions")
    private final C21OrderOptionsEnum c21OrderOptions;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
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

    @CCD(
            label = "Has the judge provided you with the hearing details?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    @JsonProperty("hasJudgeProvidedHearingDetails")
    private YesOrNo hasJudgeProvidedHearingDetails;

    @CCD(
            label = "Serve Email Notification flag",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
    )
    @JsonProperty("markedToServeEmailNotification")
    private YesOrNo markedToServeEmailNotification;

    //PRL-3254 - Added for populating hearing dropdown
    @CCD(
            label = "At which hearing was the order approved?",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    private DynamicList hearingsType;

    //PRL-4216 - serve order additional documents
    @CCD(
            label = "Serve order additional documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class, CaseworkerPrivatelawSolicitorCitizenCuAccess.class, CaseworkerWaTaskConfigurationCuAccess.class}
    )
    @JsonProperty("additionalOrderDocuments")
    private List<Element<AdditionalOrderDocument>> additionalOrderDocuments;

    @CCD(
            label = "What do you want to do with this order?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRCaseworkerPrivatelawSuperuserCruAccess.class}
    )
    @JsonProperty("whatToDoWithOrderSolicitor")
    private OrderApprovalDecisionsForSolicitorOrderEnum whatToDoWithOrderSolicitor;
    @CCD(
            label = "What do you want to do with this order?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRCaseworkerPrivatelawSuperuserCruAccess.class}
    )
    @JsonProperty("whatToDoWithOrderCourtAdmin")
    private OrderApprovalDecisionsForCourtAdminOrderEnum whatToDoWithOrderCourtAdmin;
    @CCD(
            label = "Give instructions to the legal representative",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRCaseworkerPrivatelawSuperuserCruAccess.class}
    )
    @JsonProperty("instructionsToLegalRepresentative")
    private String instructionsToLegalRepresentative;

    @CCD(
            label = "Select the order",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {APPLICANTSOLICITORCruPlus33RolesFipgzgAccess.class}
    )
    private Object rejectedOrdersDynamicList;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {APPLICANTSOLICITORCruPlus33RolesFipgzgAccess.class}
    )
    private String editOrderTextInstructions;

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruCourtnavRAccess.class}
    )
    @JsonProperty("displayLegalRepOption")
    private String displayLegalRepOption;

    @CCD(
            label = "Served by",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final DeliveryByEnum deliveryByOptionsCaOnlyC47a;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class}
    )
    @JsonProperty("emailInformationCaOnlyC47a")
    private final List<Element<EmailInformation>> emailInformationCaOnlyC47a;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class}
    )
    @JsonProperty("postalInformationCaOnlyC47a")
    private final List<Element<PostalInformation>> postalInformationCaOnlyC47a;
    @CCD(
            label = "Served by",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
    )
    private final DeliveryByEnum deliveryByOptionsDA;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class}
    )
    @JsonProperty("emailInformationDA")
    private final List<Element<EmailInformation>> emailInformationDA;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class}
    )
    @JsonProperty("postalInformationDA")
    private final List<Element<PostalInformation>> postalInformationDA;
    @CCD(
            label = " ",
            hint = "For example, DWP or the local authority",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCrudAccess.class}
    )
    @JsonProperty("serveOtherPartiesCaOnlyC47a")
    private final List<OtherOrganisationOptions> serveOtherPartiesCaOnlyC47a;
    @CCD(
            label = "Served by",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
    )
    private final DeliveryByEnum deliveryByOptionsCA;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class}
    )
    @JsonProperty("emailInformationCA")
    private final List<Element<EmailInformation>> emailInformationCA;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class}
    )
    @JsonProperty("postalInformationCA")
    private final List<Element<PostalInformation>> postalInformationCA;

    @CCD(
            label = "Check for Automated Hearing Request flag",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavRAccess.class}
    )
    @JsonProperty("checkForAutomatedHearing")
    private YesOrNo checkForAutomatedHearing;

    private final YesOrNo isOrderCreatedBySolicitor;

    /*
    * Unused fields
    * */
    @CCD(
            label = "Is Only C47a Order Selected To Serve",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final YesOrNo isOnlyC47aOrderSelectedToServe;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final YesOrNo otherPeoplePresentInCaseFlag;
    @CCD(
            label = "Does this order need to be personally served on the respondent?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final YesOrNo serveToRespondentOptionsOnlyC47a;
    @CCD(
            label = "Who is responsible for serving the respondent?",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final SoaSolicitorServingRespondentsEnum servingRespondentsOptionsCaOnlyC47a;
    @CCD(
            label = "Confirm Recipients",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCrudAccess.class}
    )
    private final DynamicMultiSelectList recipientsOptionsOnlyC47a;
    @CCD(
            label = "Which other people in the case should the order be sent to?",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCrudAccess.class}
    )
    private final DynamicMultiSelectList otherPartiesOnlyC47a;
    @CCD(
            label = "Who is responsible for serving the respondent?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
    )
    private final SoaSolicitorServingRespondentsEnum servingRespondentsOptionsCA;
    @CCD(
            label = "Who is responsible for serving the respondent?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
    )
    private final SoaSolicitorServingRespondentsEnum servingRespondentsOptionsDA;

}
