package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.reform.prl.enums.CantFindCourtEnum;
import uk.gov.hmcts.reform.prl.enums.CaseCreatedBy;
import uk.gov.hmcts.reform.prl.enums.CaseNoteDetails;
import uk.gov.hmcts.reform.prl.enums.ChildArrangementOrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.ConfidentialityChecksDisclaimerEnum;
import uk.gov.hmcts.reform.prl.enums.ConfidentialityStatementDisclaimerEnum;
import uk.gov.hmcts.reform.prl.enums.DocumentCategoryEnum;
import uk.gov.hmcts.reform.prl.enums.FL401RejectReasonEnum;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.PermissionRequiredEnum;
import uk.gov.hmcts.reform.prl.enums.RejectReasonEnum;
import uk.gov.hmcts.reform.prl.enums.TransferToAnotherCourtReasonDaEnum;
import uk.gov.hmcts.reform.prl.enums.TransferToAnotherCourtReasonEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.manageorders.ChildArrangementOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DomesticAbuseOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.DraftOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.FcOrdersEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.ManageOrdersOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrdersOptionEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.SelectTypeOfOrderEnum;
import uk.gov.hmcts.reform.prl.enums.sendmessages.SendOrReply;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.CaseLinksElement;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrderDetails;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildData;
import uk.gov.hmcts.reform.prl.models.c100respondentsolicitor.RespondentC8;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseinvite.CaseInvite;
import uk.gov.hmcts.reform.prl.models.caselink.CaseLink;
import uk.gov.hmcts.reform.prl.models.common.MappableObject;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantFamilyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.complextypes.CaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.ConfidentialityDisclaimer;
import uk.gov.hmcts.reform.prl.models.complextypes.Correspondence;
import uk.gov.hmcts.reform.prl.models.complextypes.FL401OtherProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.GatekeeperEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.Home;
import uk.gov.hmcts.reform.prl.models.complextypes.LinkToCA;
import uk.gov.hmcts.reform.prl.models.complextypes.LocalCourtAdminEmail;
import uk.gov.hmcts.reform.prl.models.complextypes.MagistrateLastName;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDetailsOfWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ProceedingDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ReasonForWithoutNoticeOrder;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBailConditionDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationDateInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationObjectType;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentRelationOptionsInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.ScannedDocument;
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.complextypes.TypeOfApplicationOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.WithdrawApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.addcafcassofficer.ChildAndCafcassOfficer;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.UploadedDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.respondentsolicitor.documents.RespondentDocs;
import uk.gov.hmcts.reform.prl.models.complextypes.uploadadditionalapplication.AdditionalApplicationsBundle;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.SendOrReplyDto;
import uk.gov.hmcts.reform.prl.models.dto.bundle.BundlingInformation;
import uk.gov.hmcts.reform.prl.models.dto.ccd.c100respondentsolicitor.RespondentSolicitorData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.courtnav.enums.ApplicantAge;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.Fl401ListOnNotice;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.GatekeepingDetails;
import uk.gov.hmcts.reform.prl.models.dto.hearingmanagement.NextHearingDetails;
import uk.gov.hmcts.reform.prl.models.dto.payment.CitizenAwpPayment;
import uk.gov.hmcts.reform.prl.models.noticeofchange.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeAnswersData;
import uk.gov.hmcts.reform.prl.models.sendandreply.Message;
import uk.gov.hmcts.reform.prl.models.sendandreply.SendOrReplyMessage;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.StatementOfService;
import uk.gov.hmcts.reform.prl.models.user.UserInfo;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesYfkopjAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesUmkkcsAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerCaaCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus1RolesQuzmdnAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCudCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruCourtnavCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCudPlus2RolesMjidosAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCuCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudPlus1RolesWyirhlAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverCaseworkerCaaRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRPlus1RolesAgacroAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus2RolesQnvhwhAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCrudCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCrudPlus2RolesMfcouzAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCitizenCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCrudPlus3RolesCdcrteAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassCaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus2RolesGtbivmAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCruPlus1RolesViswowAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverCaseworkerCaaCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.GSProfileRPlus12RolesOwgytsAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCaseworkerPrivatelawReadonlyRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCudCitizenCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus1RolesEicidaAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesPgdngkAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawLaRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassUPlus9RolesVgpuqgAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCrudPlus3RolesWisbigAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRPlus1RolesUypxmrAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawBulkscanCrudPlus1RolesIkmnbhAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRCitizenCruCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassUAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyUAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenUAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRudPlus9RolesIycrivAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORCrudPlus5RolesZisnxkAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORRPlus31RolesVrdqykAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTBARRISTER1CudPlus11RolesHrwtotAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCuAccess;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder(toBuilder = true)
public class CaseData extends BaseCaseData implements MappableObject {

    @CCD(ignore = true)
    @JsonProperty("LanguagePreferenceWelsh")
    private final YesOrNo languagePreferenceWelsh;

    /**
     * Case created by.
     */
    @CCD(
            label = " ",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "CaseCreatedByEnum",
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class, CaseworkerPrivatelawSolicitorCitizenCuAccess.class}
    )
    private CaseCreatedBy caseCreatedBy;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class}
    )
    @JsonProperty("isCafcass")
    private YesOrNo isCafcass;

    @CCD(
            label = "Applicant name",
            access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, CaseworkerPrivatelawCourtadminCuPlus3RolesYfkopjAccess.class}
    )
    private String applicantName;

    @CCD(
            label = "Respondent name",
            access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, CaseworkerPrivatelawCourtadminCuPlus3RolesYfkopjAccess.class}
    )
    private String respondentName;


    @CCD(
            label = "Child name",
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesUmkkcsAccess.class, CaseworkerCaaCuAccess.class, CaseworkerPrivatelawSolicitorCuAccess.class}
    )
    private String childName;

    /**
     * Confidential Disclaimer.
     */
    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess.class, CourtnavCruAccess.class}
    )
    private final List<ConfidentialityStatementDisclaimerEnum> confidentialityStatementDisclaimer;
    @CCD(ignore = true)
    private final List<ConfidentialityChecksDisclaimerEnum> confidentialityChecksDisclaimer;

    /**
     * C100 Confidential Disclaimer.
     */
    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess.class}
    )
    private final List<ConfidentialityStatementDisclaimerEnum> c100ConfidentialityStatementDisclaimer;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess.class}
    )
    private final ConfidentialityDisclaimer confidentialityDisclaimer;

    /**
     * C100 Help with Fees.
     */
    @CCD(
            label = "Has the applicant applied for Help with Fees?",
            hint = "You must select 'No' to continue with your application.",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess.class, CaseworkerPrivatelawCourtadminCruPlus1RolesQuzmdnAccess.class, CourtnavRAccess.class}
    )
    private final YesOrNo helpWithFees;
    @CCD(
            label = "Enter the Help with Fees reference number",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawSolicitorCudCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCruCourtnavCuAccess.class}
    )
    @JsonProperty("helpWithFeesReferenceNumber")
    private final String helpWithFeesNumber;

    /**
     * Upload documents.
     */

    @CCD(
            label = "Contact order",
            categoryID = "draftOrders",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCudPlus2RolesMjidosAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final List<Document> contactOrderDocumentsUploaded;
    @CCD(
            label = "C8 form",
            categoryID = "confidential",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCudPlus2RolesMjidosAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final List<Document> c8FormDocumentsUploaded;
    @CCD(
            label = "Other documents",
            categoryID = "anyOtherDoc",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCudPlus2RolesMjidosAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final List<Document> otherDocumentsUploaded;

    /**
     * People in the case.
     */

    @CCD(
            label = " ",
            typeOverride = FieldType.AddressUK,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
    )
    private final Address childrenAddress;
    @CCD(
            label = "Name of child(ren)",
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
    )
    private final String childrenInProceeding;
    @CCD(
            label = "Child",
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
    )
    private final List<Element<Child>> otherChildren;

    // people feature FPET-325
    @JsonUnwrapped
    private final Relations relations;

    /**
     * Type of application.
     */
    @CCD(
            label = "*What order(s) are you applying for?",
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess.class, CaseworkerWaTaskConfigurationCuAccess.class}
    )
    private List<OrderTypeEnum> ordersApplyingFor;
    @CCD(
            label = "*Select type of child arrangements order",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private ChildArrangementOrderTypeEnum typeOfChildArrangementsOrder;
    @CCD(
            label = "*For example-does the order detail who the child will live with, or how often they will spend type with a parent.",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private String natureOfOrder;
    @CCD(
            label = "*Do you have a draft consent order?",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class}
    )
    private final YesOrNo consentOrder;
    @CCD(
            label = "*Draft Consent Order",
            hint = "Upload a scanned PDF or DOCX of the draft consent order, signed by both parties. Wherever possible, documents should be scanned in greyscale.",
            regex = ".pdf,.docx",
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCuCitizenCruAccess.class}
    )
    private final Document draftConsentOrderFile;
    @CCD(
            label = "*Have you applied to the court for permission to make this application?",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private final PermissionRequiredEnum applicationPermissionRequired;
    @CCD(
            label = "*Give details of why permission is required from the court.",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private final String applicationPermissionRequiredReason;
    @CCD(
            label = " ",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private final String applicationDetails;

    /**
     * Hearing urgency.
     */
    @CCD(
            label = "*Is this case urgent?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    private YesOrNo isCaseUrgent;
    @CCD(
            label = "*Set out how soon the case needs to be heard in days and hours, and give the reason for the urgency.",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private String caseUrgencyTimeAndReason;
    @CCD(
            label = "*What efforts have you made to notify each respondent of the application?",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private String effortsMadeWithRespondents;
    @CCD(
            label = "*Do you need a without notice hearing?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private YesOrNo doYouNeedAWithoutNoticeHearing;
    @CCD(
            label = "*Set out the reasons for the application to be considered without notice. This\ninformation must be provided - if reasons are not given, the case will not be\nheard without notice.",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private String reasonsForApplicationWithoutNotice;
    @CCD(
            label = "*Do you require a hearing with reduced notice?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private YesOrNo doYouRequireAHearingWithReducedNotice;
    @CCD(
            label = "*Set out the reasons below",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private String setOutReasonsBelow;
    @CCD(
            label = "*Are respondents aware of proceedings?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private YesOrNo areRespondentsAwareOfProceedings;

    /**
     * Applicant details.
     */
    @CCD(
            label = "*Applicant",
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "PartyDetailsApplicant",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCudCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesWyirhlAccess.class}
    )
    private final List<Element<PartyDetails>> applicants;
    @CCD(
            label = "Applicant",
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class, CaseworkerApproverCaseworkerCaaRAccess.class, CaseworkerPrivatelawCafcassRPlus1RolesAgacroAccess.class}
    )
    @JsonProperty("applicantsFL401")
    private final PartyDetails applicantsFL401;

    /**
     * caseNotes details.
     */
    @CCD(
            label = "Case notes",
            searchable = false,
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "addCaseNoteType",
            access = {CaseworkerPrivatelawCourtadminCudPlus2RolesQnvhwhAccess.class, CaseworkerPrivatelawJudgeCudAccess.class, CaseworkerPrivatelawLaCudAccess.class, CaseworkerPrivatelawSolicitorCAccess.class, CaseworkerPrivatelawSuperuserCudAccess.class, CitizenCudAccess.class}
    )
    private List<Element<CaseNoteDetails>> caseNotes;
    //@JsonProperty("caseNoteDetails")
    //private final CaseNoteDetails caseNoteDetails;
    @CCD(
            label = "Subject",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
    )
    private final String subject;
    @CCD(
            label = "case note",
            hint = "Add note detail, including relevant dates and people involved",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class, CaseworkerWaTaskConfigurationCrudAccess.class}
    )
    private final String caseNote;


    /**
     * Child Details Revised.
     */
    @CCD(
            label = "*Child",
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "NewChildDetails",
            access = {CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawSolicitorCudCitizenCrudAccess.class, CaseworkerPrivatelawJudgeCrudAccess.class, CaseworkerPrivatelawLaCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class, CaseworkerWaTaskConfigurationCudAccess.class}
    )
    private List<Element<ChildDetailsRevised>> newChildDetails;


    /**
     * Children are not in the case but related to this case.
     */
    @CCD(
            label = "*Child",
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "ChildrenNotInTheCase",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCudAccess.class, CaseworkerPrivatelawSuperuserRAccess.class, CaseworkerWaTaskConfigurationCudAccess.class, CitizenRAccess.class}
    )
    private List<Element<OtherChildrenNotInTheCase>> childrenNotInTheCase;

    @CCD(
            label = "Do you or respondents have other children who are not part of this application?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class, CaseworkerWaTaskConfigurationCrudAccess.class}
    )
    private YesOrNo childrenNotPartInTheCaseYesNo;

    /**
     * Child details.
     */
    @CCD(
            label = "*Child",
            min = 1,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess.class}
    )
    private List<Element<Child>> children;
    @CCD(
            label = "*Are any of the children known to the local authority children's services?",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private YesNoDontKnow childrenKnownToLocalAuthority;
    @CCD(
            label = "*State which child and the name of the Local Authority and Social Worker (if known)",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private String childrenKnownToLocalAuthorityTextArea;
    @CCD(
            label = "*Are any of the children the subject of a child protection plan?",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private YesNoDontKnow childrenSubjectOfChildProtectionPlan;

    /**
     * Respondent details.
     */
    @CCD(
            label = "*Respondent",
            min = 1,
            access = {CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawSolicitorCudCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final List<Element<PartyDetails>> respondents;
    @CCD(
            label = "Respondent",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class, CaseworkerPrivatelawCafcassRPlus1RolesAgacroAccess.class}
    )
    @JsonProperty("respondentsFL401")
    private final PartyDetails respondentsFL401;


    /**
     * MIAM.
     */
    @JsonUnwrapped
    private final MiamDetails miamDetails;

    /**
     * MIAM.
     */
    @JsonUnwrapped
    private final MiamPolicyUpgradeDetails miamPolicyUpgradeDetails;

    /**
     * Allegations of harm.
     */

    @JsonUnwrapped
    private final AllegationOfHarm allegationOfHarm;


    @JsonUnwrapped
    private final AllegationOfHarmRevised allegationOfHarmRevised;

    /**
     * Other people in the case.
     */
    @CCD(
            label = "Person",
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawSolicitorCudCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCrudAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final List<Element<PartyDetails>> othersToNotify;


    /**
     * Other people in the case.
     */
    @CCD(
            label = "Person",
            access = {CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class, CaseworkerPrivatelawSolicitorCudCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private final List<Element<PartyDetails>> otherPartyInTheCaseRevised;



    /**
     * Other proceedings.
     */

    @CCD(
            label = "Are there previous or ongoing proceedings for the child(ren)?",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesNoDontKnow previousOrOngoingProceedingsForChildren;
    @CCD(
            label = "Other proceedings",
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCuCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final List<Element<ProceedingDetails>> existingProceedings;

    /**
     * Attending the hearing.
     */
    @JsonUnwrapped
    private final AttendHearing attendHearing;

    /**
     * International element.
     */
    @CCD(
            label = "Do you have any reason to believe that any child, parent or potentially significant adult in the child's life may be habitually resident in another country abroad or in Scotland or Northern Ireland?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawLaCrudPlus2RolesMfcouzAccess.class, CaseworkerPrivatelawSolicitorCudCitizenCrudAccess.class}
    )
    private YesOrNo habitualResidentInOtherState;
    @CCD(
            label = "*Give reason",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawLaCrudPlus2RolesMfcouzAccess.class}
    )
    private String habitualResidentInOtherStateGiveReason;
    @CCD(
            label = "Do you have any reason to believe that there may be an issue as to jurisdiction, relating to a country abroad or to Scotland or Northern Ireland, in this case?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawLaCrudPlus2RolesMfcouzAccess.class}
    )
    private YesOrNo jurisdictionIssue;
    @CCD(
            label = "*Give reason",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawLaCrudPlus2RolesMfcouzAccess.class}
    )
    private String jurisdictionIssueGiveReason;
    @CCD(
            label = "Has a request been made or should a request be made to a Central Authority or other competent authority in a foreign state or a consular authority in England and Wales?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawLaCrudPlus2RolesMfcouzAccess.class}
    )
    private YesOrNo requestToForeignAuthority;
    @CCD(
            label = "*Give reason",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawLaCrudPlus2RolesMfcouzAccess.class}
    )
    private String requestToForeignAuthorityGiveReason;

    /**
     * Litigation capacity.
     */
    @CCD(
            label = "Give details of any factors affecting litigation capacity",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus4RolesXqdtnaAccess.class}
    )
    private final String litigationCapacityFactors;
    @CCD(
            label = "Provide details of any referral to or assessment by the Adult Learning Disability team, and/or any adult health service, where known, together with the outcome",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String litigationCapacityReferrals;
    @CCD(
            label = "Are you aware of any other factors which may affect the ability of the person concerned to take part in the proceedings?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final YesOrNo litigationCapacityOtherFactors;
    @CCD(
            label = "*Give Details",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private final String litigationCapacityOtherFactorsDetails;

    /**
     * Welsh language requirements.
     */
    @CCD(
            label = "Does any person in this case need orders or documents in Welsh?",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCuCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class, CourtnavCuAccess.class}
    )
    private final YesOrNo welshLanguageRequirement;
    @CCD(
            label = "*Which language are you using to complete this application?",
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class}
    )
    private final LanguagePreference welshLanguageRequirementApplication;
    @CCD(
            label = "*Does this application need to be translated into Welsh?",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class}
    )
    private final YesOrNo languageRequirementApplicationNeedWelsh;
    @CCD(
            label = "*Does this application need to be translated into English?",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class}
    )
    private final YesOrNo welshLanguageRequirementApplicationNeedEnglish;

    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
    )
    private final CcdPaymentServiceRequestUpdate paymentCallbackServiceRequestUpdate;
    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawSolicitorCudPlus2RolesMjidosAccess.class, CaseworkerPrivatelawCourtadminCudAccess.class}
    )
    @JsonProperty("paymentServiceRequestReferenceNumber")
    private final String paymentServiceRequestReferenceNumber;

    @CCD(
            label = " ",
            typeOverride = FieldType.Text,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate issueDate;

    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerWaTaskConfigurationRAccess.class}
    )
    @JsonProperty("solicitorName")
    private final String solicitorName;
    @CCD(
            label = " ",
            hint = "${feeAmount}",
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerWaTaskConfigurationRAccess.class}
    )
    @JsonProperty("feeAmount")
    private final String feeAmount;
    @CCD(ignore = true)
    @JsonProperty("feeCode")
    private final String feeCode;
    @CCD(
            label = "Draft application",
            categoryID = "draftOrders",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class, CaseworkerPrivatelawCourtadminCitizenCudAccess.class}
    )
    @JsonProperty("draftOrderDoc")
    private final Document draftOrderDoc;
    @CCD(
            label = "Draft application welsh",
            categoryID = "draftOrders",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCrudAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    @JsonProperty("draftOrderDocWelsh")
    private final Document draftOrderDocWelsh;
    @CCD(
            label = "C8 Document",
            categoryID = "confidential",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class, CaseworkerApproverRAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class, CitizenCuAccess.class}
    )
    @JsonProperty("c8Document")
    private final Document c8Document;
    @CCD(
            label = "C8 Document (Welsh)",
            categoryID = "confidential",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCrudPlus3RolesCdcrteAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class, CaseworkerApproverRAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class, CitizenCudAccess.class}
    )
    @JsonProperty("c8WelshDocument")
    private final Document c8WelshDocument;
    @CCD(
            label = "C1A Document",
            categoryID = "applicantC1AApplication",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class, CaseworkerPrivatelawSolicitorCitizenCuAccess.class}
    )
    @JsonProperty("c1ADocument")
    private final Document c1ADocument;
    @CCD(
            label = "C1A Document (Welsh)",
            categoryID = "applicantC1AApplication",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class, CaseworkerPrivatelawSolicitorCuAccess.class, CitizenCudAccess.class}
    )
    @JsonProperty("c1AWelshDocument")
    private final Document c1AWelshDocument;

    @CCD(
            label = "C8 Draft Document",
            categoryID = "confidential",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess.class, CitizenCuAccess.class}
    )
    @JsonProperty("c8DraftDocument")
    private final Document c8DraftDocument;
    @CCD(
            label = "C8 Draft Document (Welsh)",
            categoryID = "confidential",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesUmkkcsAccess.class}
    )
    @JsonProperty("c8WelshDraftDocument")
    private final Document c8WelshDraftDocument;

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("isEngDocGen")
    private final String isEngDocGen;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
    )
    @JsonProperty("isWelshDocGen")
    private final String isWelshDocGen;

    @CCD(
            label = "Draft Document",
            categoryID = "applicantApplication",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawSolicitorCudCitizenCrudAccess.class}
    )
    @JsonProperty("submitAndPayDownloadApplicationLink")
    private final Document submitAndPayDownloadApplicationLink;
    @CCD(
            label = "Draft Document (Welsh)",
            categoryID = "applicantApplication",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawSolicitorCudCitizenCrudAccess.class}
    )
    @JsonProperty("submitAndPayDownloadApplicationWelshLink")
    private final Document submitAndPayDownloadApplicationWelshLink;

    /**
     * Add case number.
     */
    @CCD(
            label = "FamilyMan case number",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCudAccess.class, CaseworkerApproverRAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    private final String familymanCaseNumber;
    @JsonIgnore
    private final String fl401FamilymanCaseNumber; //field is no longer in use

    /**
     * Manage Documents.
     */
    @CCD(
            label = "Choose a document category",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess.class, CaseworkerPrivatelawJudgeRAccess.class}
    )
    private final DocumentCategoryEnum documentCategoryChecklist;
    @CCD(
            label = "Documents",
            searchable = false,
            min = 1,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess.class, CaseworkerPrivatelawCafcassCaseworkerPrivatelawJudgeRAccess.class}
    )
    private final List<Element<FurtherEvidence>> furtherEvidences;
    @CCD(
            label = "*Give details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess.class, CaseworkerPrivatelawJudgeRAccess.class}
    )
    @JsonProperty("giveDetails")
    private final String giveDetails;

    @CCD(
            label = "Supporting documents",
            searchable = false,
            min = 1,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess.class, CaseworkerPrivatelawCafcassCaseworkerPrivatelawJudgeRAccess.class}
    )
    private final List<Element<Correspondence>> correspondence;
    @CCD(
            label = "Supporting documents",
            searchable = false,
            min = 1,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess.class, CaseworkerPrivatelawCafcassCaseworkerPrivatelawJudgeRAccess.class}
    )
    private final List<Element<OtherDocuments>> otherDocuments;

    @CCD(
            label = "Documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus2RolesGtbivmAccess.class, CaseworkerPrivatelawExternaluserViewonlyCudAccess.class}
    )
    private final List<Element<FurtherEvidence>> mainAppDocForTabDisplay;
    @CCD(label = "Supporting documents", searchable = false, access = {CaseworkerPrivatelawSystemupdateCudAccess.class})
    private final List<Element<Correspondence>> correspondenceForTabDisplay;
    @CCD(
            label = "Supporting documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus2RolesGtbivmAccess.class, CaseworkerPrivatelawExternaluserViewonlyCudAccess.class}
    )
    private final List<Element<OtherDocuments>> otherDocumentsForTabDisplay;

    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
    )
    private List<Element<UserInfo>> userInfo;

    /**
     * Return Application.
     */
    @CCD(
            label = " ",
            typeOverride = FieldType.MultiSelectList,
            typeParameterOverride = "rejectReasonEnum",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruPlus1RolesViswowAccess.class}
    )
    private final List<RejectReasonEnum> rejectReason;
    @CCD(label = " ", access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class})
    private final List<FL401RejectReasonEnum> fl401RejectReason;
    @CCD(
            label = "Return message will be this",
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruPlus1RolesViswowAccess.class}
    )
    private final String returnMessage;

    @CCD(
            label = "Petitioner solicitor's firm",
            hint = "Petitioner solicitor's firm",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerApproverCaseworkerCaaCrudAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesWyirhlAccess.class}
    )
    @JsonProperty("applicantOrganisationPolicy")
    private OrganisationPolicy applicantOrganisationPolicy;

    /**
     * Without Notice Order.
     */
    @CCD(
            label = " ",
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class, CaseworkerPrivatelawJudgeCruAccess.class, CaseworkerPrivatelawLaRAccess.class}
    )
    @JsonProperty("orderWithoutGivingNoticeToRespondent")
    private final WithoutNoticeOrderDetails orderWithoutGivingNoticeToRespondent;
    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class}
    )
    @JsonProperty("reasonForOrderWithoutGivingNotice")
    private final ReasonForWithoutNoticeOrder reasonForOrderWithoutGivingNotice;
    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class}
    )
    @JsonProperty("bailDetails")
    private final RespondentBailConditionDetails bailDetails;
    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class}
    )
    @JsonProperty("anyOtherDtailsForWithoutNoticeOrder")
    private final OtherDetailsOfWithoutNoticeOrder anyOtherDtailsForWithoutNoticeOrder;

    /**
     * Home Situation DA.
     */
    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCrudAccess.class}
    )
    private final Home home;

    /**
     * FL401 Respondents relationship.
     */
    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCrudCaseworkerWaTaskConfigurationRAccess.class}
    )
    private final RespondentRelationObjectType respondentRelationObject;
    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class}
    )
    private final RespondentRelationDateInfo respondentRelationDateInfoObject;
    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class}
    )
    private final RespondentRelationOptionsInfo respondentRelationOptions;

    /**
     * FL401 Type of Application.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class}
    )
    @JsonProperty("typeOfApplicationOrders")
    private final TypeOfApplicationOrders typeOfApplicationOrders;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class}
    )
    @JsonProperty("typeOfApplicationLinkToCA")
    private final LinkToCA typeOfApplicationLinkToCA;

    /**
     * Respondent Behaviour.
     */
    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class}
    )
    private final RespondentBehaviour respondentBehaviourData;
    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class}
    )
    @JsonProperty("applicantFamilyDetails")
    private final ApplicantFamilyDetails applicantFamilyDetails;
    @CCD(
            label = "Child",
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "FL401ApplicantChildDetails",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class}
    )
    @JsonProperty("applicantChildDetails")
    private final List<Element<ApplicantChild>> applicantChildDetails;

    /**
     * Issue and send to local court'.
     */
    @CCD(
            label = "Local Court Admin",
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "localCourtAdminEmail",
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private final List<Element<LocalCourtAdminEmail>> localCourtAdmin;
    @CCD(
            label = "Select from the list of courts",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private final DynamicList courtList;
    @CCD(label = "case Management Location ", access = {GSProfileRPlus12RolesOwgytsAccess.class})
    private final CaseManagementLocation caseManagementLocation;

    /**
     * This field contains Application Submitter solicitor email address.
     */
    @CCD(
            label = "FamilyMan case number",
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCrudAccess.class}
    )
    private final String applicantSolicitorEmailAddress;
    @CCD(ignore = true)
    private final String respondentSolicitorEmailAddress;
    @CCD(
            label = "FamilyMan case number",
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCrudAccess.class}
    )
    private final String caseworkerEmailAddress;

    /**
     * Court details.
     */

    @CCD(
            label = "Court name",
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawLaCaseworkerPrivatelawReadonlyRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    private String courtName;
    @CCD(
            label = "Court code",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerWaTaskConfigurationRAccess.class, CourtnavCruAccess.class}
    )
    private String courtId;
    @CCD(
            label = "Enter email address",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class, CaseworkerPrivatelawLaRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    private String courtEmailAddress;
    @CCD(
            label = "Reason for amending court details",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerWaTaskConfigurationRAccess.class}
    )
    private String reasonForAmendCourtDetails;
    @CCD(label = "Reason for transfer", searchable = false, access = {DefaultAccess.class})
    private List<TransferToAnotherCourtReasonEnum> reasonForTransferToAnotherCourt;
    @CCD(label = "Reason for transfer", searchable = false, access = {DefaultAccess.class})
    private List<TransferToAnotherCourtReasonDaEnum> reasonForTransferToAnotherCourtDa;
    @CCD(label = " ", searchable = false, access = {DefaultAccess.class})
    private List<CantFindCourtEnum> cantFindCourtCheck;
    @CCD(label = "Enter court's name", searchable = false, access = {DefaultAccess.class})
    private final String anotherCourt;
    @CCD(label = "Previous court name", searchable = false, access = {DefaultAccess.class})
    private final String transferredCourtFrom;
    @CCD(
            label = "State why this case should be transferred to another court.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class}
    )
    private String anotherReasonToTransferDetails;

    /**
     * Final document. (C100)
     */

    @CCD(
            label = "Final Document",
            categoryID = "applicantApplication",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerWaTaskConfigurationCudCitizenCuAccess.class, CourtnavCuAccess.class}
    )
    @JsonProperty("finalDocument")
    private final Document finalDocument;

    /**
     * Send and reply to messages.
     */

    @JsonUnwrapped
    SendOrReplyDto sendOrReplyDto;

    @CCD(
            label = "Enter your message",
            hint = "Explain what you're requesting and why. Include answers and decisions you need.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess.class, CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess.class}
    )
    String messageContent;
    @CCD(
            label = "Your messages",
            hint = "Message subject comprises application type and urgency, if relevant , and date requested.",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class}
    )
    Object replyMessageDynamicList;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class}
    )
    Message messageReply;
    @CCD(
            label = "Do you want to send or reply to a message?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
    )
    SendOrReply chooseSendOrReply;

    public static String[] temporaryFields() {
        return new String[]{
            "replyMessageDynamicList", "messageReply", "messageContent",
            "messageReply", "messageMetaData"
        };
    }


    @CCD(
            label = "Final Document (Welsh)",
            categoryID = "applicantApplication",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCuAccess.class, CaseworkerPrivatelawCourtadminCudPlus1RolesEicidaAccess.class, CaseworkerPrivatelawSolicitorCitizenCuAccess.class}
    )
    @JsonProperty("finalWelshDocument")
    private final Document finalWelshDocument;

    /**
     * Confidentiality details.
     */
    @CCD(
            label = "Applicant",
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerApproverCruAccess.class, CaseworkerCaaCuAccess.class, CaseworkerWaTaskConfigurationCudAccess.class, CitizenCudAccess.class}
    )
    private final List<Element<ApplicantConfidentialityDetails>> applicantsConfidentialDetails;
    @CCD(
            label = "Respondent",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerWaTaskConfigurationCudCitizenCuAccess.class}
    )
    private final List<Element<ApplicantConfidentialityDetails>> respondentConfidentialDetails;
    @CCD(
            label = "Child confidential details",
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerWaTaskConfigurationCudAccess.class}
    )
    private final List<Element<ChildConfidentialityDetails>> childrenConfidentialDetails;

    @CCD(
            label = "Type of Application",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
    )
    private final Map<String, Object> typeOfApplicationTable;

    /**
     * Withdraw Application.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesPgdngkAccess.class}
    )
    private final WithdrawApplication withDrawApplicationData;

    /**
     * FL401 Upload Documents.
     */
    @CCD(
            label = "Witness statement",
            categoryID = "otherWitnessStatements",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawSolicitorCudCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    private final List<Element<Document>> fl401UploadWitnessDocuments;
    @CCD(
            label = "Supporting documents",
            categoryID = "anyOtherDoc",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCuCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess.class}
    )
    private final List<Element<Document>> fl401UploadSupportDocuments;
    /**
     * Send to Gatekeeper.
     */
    @CCD(ignore = true)
    private final List<Element<GatekeeperEmail>> gatekeeper;

    /**
     * FL401 Other Proceedings.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class, CaseworkerPrivatelawSolicitorCuCitizenCruAccess.class}
    )
    private final FL401OtherProceedingDetails fl401OtherProceedingDetails;

    /**
     * FL401 Statement Of truth and submit.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess.class}
    )
    @JsonProperty("fl401StmtOfTruth")
    private StatementOfTruth fl401StmtOfTruth;

    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawLaRAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonProperty("viewPDFlinkLabelText")
    private String viewPdfLinkLabelText;

    @CCD(
            label = "Case Invite",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private List<Element<CaseInvite>> caseInvites;


    /**
     * FL401 submit status flags.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
    )
    private String isCourtEmailFound;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
    )
    private String isDocumentGenerated;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
    )
    private String isNotificationSent;


    @CCD(
            label = "Child arrangement orders",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private ChildArrangementOrdersEnum childArrangementOrders;
    @CCD(
            label = "Domestic abuse orders",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private DomesticAbuseOrdersEnum domesticAbuseOrders;
    @CCD(
            label = "FC orders",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private FcOrdersEnum fcOrders;
    @CCD(
            label = "Any other order",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private OtherOrdersOptionEnum otherOrdersOption;
    // customOrderNameOption - read from raw map due to constructor param limit (see CustomOrderService)
    @CCD(
            label = "Name of order",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private String nameOfOrder;

    /**
     * Manage Orders.
     */

    @CCD(label = "Orders", access = {CaseworkerPrivatelawCafcassUPlus9RolesVgpuqgAccess.class})
    private final List<Element<OrderDetails>> orderCollection;

    @CCD(
            label = "Approval Date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate approvalDate;
    @CCD(
            label = "Upload Order",
            categoryID = "anyOtherDoc",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private Document uploadOrderDoc;
    @CCD(
            label = " ",
            categoryID = "anyOtherDoc",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private Document previewOrderDoc;
    @CCD(
            label = " ",
            categoryID = "anyOtherDoc",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private Document previewOrderDocWelsh;

    @CCD(
            label = "What do you want to do?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private final ManageOrdersOptionsEnum manageOrdersOptions;
    @CCD(
            label = "Select the type of order",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private final CreateSelectOrderOptionsEnum createSelectOrderOptions;
    @CCD(
            label = "If the applicant or respondent are represented by a solicitor the order is sent to solicitor directly",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
    )
    private final List<OrderRecipientsEnum> orderRecipients;
    @CCD(
            label = "What type of order is this?",
            searchable = false,
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "SelectTypeOfOrderEnum",
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
    )
    @JsonProperty("selectTypeOfOrder")
    private final SelectTypeOfOrderEnum selectTypeOfOrder;

    @CCD(
            label = "Does this order close the case?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class}
    )
    @JsonProperty("doesOrderClosesCase")
    private final YesOrNo doesOrderClosesCase;
    @CCD(
            label = "Was the order approved at a hearing?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("wasTheOrderApprovedAtHearing")
    private final YesOrNo wasTheOrderApprovedAtHearing;
    @CCD(
            label = "Judge's full name",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
    )
    @JsonProperty("judgeOrMagistratesLastName")
    private final String judgeOrMagistratesLastName;
    @CCD(
            label = "Full name of Justices' Legal Adviser",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
    )
    @JsonProperty("justiceLegalAdviserFullName")
    private final String justiceLegalAdviserFullName;
    @CCD(
            label = "Date order made",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private final LocalDate dateOrderMade;

    @CCD(
            label = "Children list",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
    )
    @JsonProperty("childrenList")
    private final String childrenList;
    @CCD(
            label = " ",
            searchable = false,
            max = 3,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCrudPlus3RolesWisbigAccess.class}
    )
    @JsonProperty("magistrateLastName")
    private final List<Element<MagistrateLastName>> magistrateLastName;

    @CCD(
            label = "Full name",
            searchable = false,
            min = 1,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "appointedGuardianFullName",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCrudPlus3RolesWisbigAccess.class}
    )
    private List<Element<AppointedGuardianFullName>> appointedGuardianName;

    @JsonUnwrapped
    private final ManageOrders manageOrders;

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
    )
    @JsonProperty("childrenListForDocmosis")
    private List<Element<Child>> childrenListForDocmosis;

    @CCD(ignore = true)
    @JsonProperty("applicantChildDetailsForDocmosis")
    private List<Element<ApplicantChild>> applicantChildDetailsForDocmosis;

    @JsonUnwrapped
    private final StandardDirectionOrder standardDirectionOrder;

    @JsonUnwrapped
    private final DirectionOnIssue directionOnIssue;

    @JsonUnwrapped
    private final ServiceOfApplicationUploadDocs serviceOfApplicationUploadDocs;


    /**
     * Solicitor Details.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminRPlus1RolesUypxmrAccess.class}
    )
    private String caseSolicitorName;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminRPlus1RolesUypxmrAccess.class}
    )
    private String caseSolicitorOrgName;
    @CCD(
            label = " ",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private String selectedOrder;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserRCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private String selectedC21Order;

    /**
     * FL401 Court details for Pilot.
     */
    @CCD(
            label = "Select the email address of the family court you want this application to go to",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
    )
    @JsonProperty("submitCountyCourtSelection")
    private final DynamicList submitCountyCourtSelection;

    public CaseData setDateSubmittedDate() {
        ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"));
        return this.toBuilder()
            .dateSubmitted(DateTimeFormatter.ISO_LOCAL_DATE.format(zonedDateTime))
            .build();
    }

    public CaseData setIssueDate() {
        this.toBuilder()
            .issueDate(LocalDate.now())
            .build();

        return this;
    }


    /**
     * Withdraw request flag.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawLaRAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    private String isWithdrawRequestSent;

    /**
     * Courtnav uploaded files.
     */

    @CCD(
            label = "CourtNav uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawCafcassRPlus1RolesAgacroAccess.class}
    )
    @JsonProperty("courtNavUploadedDocs")
    private final List<Element<UploadedDocuments>> courtNavUploadedDocs;
    @CCD(
            label = "Is case created via courtnav ?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerWaTaskConfigurationCourtnavCuAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess.class}
    )
    private YesOrNo isCourtNavCase;

    /**
     * Service Of Application.
     */
    @CCD(
            label = "Select orders",
            hint = "Select the orders and notices that you want to serve on parties.",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawReadonlyRCourtnavCruAccess.class, CaseworkerPrivatelawJudgeCrudAccess.class, CaseworkerPrivatelawLaCruAccess.class}
    )
    private DynamicMultiSelectList serviceOfApplicationScreen1;

    @CCD(
            label = "Citizen uploaded documents",
            searchable = false,
            access = {CaseworkerPrivatelawSystemupdateCudAccess.class, CitizenCudAccess.class}
    )
    @JsonProperty("citizenUploadedDocumentList")
    private final List<Element<UploadedDocuments>> citizenUploadedDocumentList;

    @CCD(
            label = "Citizen Response C7 documents",
            searchable = false,
            access = {CaseworkerPrivatelawSystemupdateCudAccess.class, CitizenCudAccess.class}
    )
    @JsonProperty("citizenResponseC7DocumentList")
    private final List<Element<ResponseDocuments>> citizenResponseC7DocumentList;

    @CCD(
            label = "Scanned Documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawBulkscanCrudPlus1RolesIkmnbhAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
    )
    @JsonProperty("scannedDocuments")
    private List<Element<ScannedDocument>> scannedDocuments;

    /**
     * Courtnav.
     */
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerWaTaskConfigurationRAccess.class}
    )
    @JsonProperty("applicantAge")
    private final ApplicantAge applicantAge;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerWaTaskConfigurationRAccess.class}
    )
    private final String specialCourtName;
    @CCD(
            label = "Courtnav approved? ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess.class, CourtnavCuAccess.class}
    )
    private YesOrNo courtNavApproved;
    @CCD(
            label = "Has draft order?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess.class, CourtnavCuAccess.class}
    )
    private YesOrNo hasDraftOrder;
    @CCD(
            label = "Case originated by ",
            access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess.class, CourtnavCuAccess.class}
    )
    private String caseOrigin;
    @CCD(
            label = "Total no of attachments from courtnav ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess.class, CourtnavCuAccess.class}
    )
    private String numberOfAttachments;

    @CCD(ignore = true)
    private String previewDraftAnOrder;

    @CCD(ignore = true)
    private String citizenUploadedStatement;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminRCitizenCruCourtnavCruAccess.class}
    )
    @JsonProperty("paymentReferenceNumber")
    private final String paymentReferenceNumber;

    /**
     * Respondent Solicitor.
     */
    @JsonUnwrapped
    private final RespondentSolicitorData respondentSolicitorData;

    @CCD(
            label = "Cafcass uploaded documents",
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCafcassCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
    )
    @JsonProperty("cafcassUploadedDocs")
    private final List<Element<UploadedDocuments>> cafcassUploadedDocs;

    // C100 Rebuild
    @JsonUnwrapped
    private final C100RebuildData c100RebuildData;

    @CCD(
            label = "Draft orders",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerPrivatelawCafcassUAccess.class, CaseworkerPrivatelawReadonlyUAccess.class, CitizenUAccess.class, CourtnavCuAccess.class}
    )
    private final List<Element<DraftOrder>> draftOrderCollection;
    @CCD(
            label = "Select the order",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CourtnavCruAccess.class}
    )
    private Object draftOrdersDynamicList;

    @JsonUnwrapped
    private final NoticeOfChangeAnswersData noticeOfChangeAnswersData = NoticeOfChangeAnswersData.builder().build();
    @CCD(
            label = "Bundle Details",
            hint = "Bundle Details",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCitizenCudAccess.class, CaseworkerPrivatelawJudgeCudAccess.class, CaseworkerPrivatelawLaCudAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class}
    )
    @JsonProperty("bundleInformation")
    private BundlingInformation bundleInformation;

    @CCD(
            label = "Directions to admin:",
            hint = "Give any further directions, for example if there are listing requirements or special measures needed.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    private String judgeDirectionsToAdmin;
    @CCD(
            label = "Do you want to edit the order?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
    )
    private YesOrNo doYouWantToEditTheOrder;
    @CCD(
            label = "Admin notes ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CourtnavCruAccess.class}
    )
    private String courtAdminNotes;

    @JsonUnwrapped
    private final ServeOrderData serveOrderData;

    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class}
    )
    private final List<CaseLinksElement<CaseLink>> caseLinks;

    @CCD(
            label = "Case level Flags",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawExternaluserViewonlyRAccess.class}
    )
    private Flags caseFlags;

    @JsonUnwrapped
    @Builder.Default
    private UploadAdditionalApplicationData uploadAdditionalApplicationData;
    @CCD(
            label = "Additional applications",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus1RolesEicidaAccess.class, CaseworkerPrivatelawSuperuserCudAccess.class, CitizenCudAccess.class}
    )
    private final List<Element<AdditionalApplicationsBundle>> additionalApplicationsBundle;
    @CCD(
            label = "What do you want to do?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class}
    )
    private final DraftOrderOptionsEnum draftOrderOptions;

    @CCD(label = "Child", access = {CaseworkerPrivatelawCourtadminRudPlus9RolesIycrivAccess.class})
    private final List<Element<ChildAndCafcassOfficer>> childAndCafcassOfficers;

    //Added for c100 rebuild
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class, CitizenCruAccess.class, CourtnavCruAccess.class}
    )
    private Long noOfDaysRemainingToSubmitCase;

    @CCD(
            label = "Name of legal adviser",
            searchable = false,
            typeOverride = FieldType.DynamicList,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    private final DynamicList legalAdviserList;

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, APPLICANTSOLICITORCrudPlus5RolesZisnxkAccess.class}
    )
    private AllocatedJudge allocatedJudge;
    @CCD(
            label = "Gatekeeper",
            showCondition = "gatekeepingDetails = \"DO_NOT_SHOW\"",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    @JsonProperty("gatekeepingDetails")
    private GatekeepingDetails gatekeepingDetails;

    @JsonUnwrapped
    private final ListWithoutNoticeDetails listWithoutNoticeDetails;
    @JsonUnwrapped
    private final Fl401ListOnNotice fl401ListOnNotice;

    @CCD(
            label = "Next Hearing Details",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
    )
    private NextHearingDetails nextHearingDetails;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class, CourtnavRAccess.class}
    )
    private final YesOrNo isAddCaseNumberAdded;

    @CCD(
            label = "Change Organisation Request",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverCaseworkerCaaCrudAccess.class, CaseworkerPrivatelawCourtadminCudAccess.class, CaseworkerPrivatelawReadonlyRAccess.class, CaseworkerPrivatelawSolicitorCudAccess.class, CaseworkerPrivatelawSuperuserCudAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    private final ChangeOrganisationRequest changeOrganisationRequestField;

    @JsonUnwrapped
    private final ServiceOfApplication serviceOfApplication;

    @CCD(
            label = "Print and email notifications",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
    )
    @JsonProperty("finalServedApplicationDetailsList")
    private List<Element<ServedApplicationDetails>> finalServedApplicationDetailsList;
    @CCD(
            label = "Select the parties that you will no longer be representing",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {APPLICANTSOLICITORRPlus31RolesVrdqykAccess.class}
    )
    private DynamicMultiSelectList solStopRepChooseParties;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCruPlus1RolesQuzmdnAccess.class}
    )
    private DynamicMultiSelectList removeLegalRepAndPartiesList;

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CourtnavRAccess.class}
    )
    private String courtCodeFromFact;

    @CCD(
            label = "Payment service reference number",
            hint = "Enter the service reference number you want to process for additional application",
            access = {CaseworkerPrivatelawSolicitorCruAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    private String tsPaymentServiceRequestReferenceNumber;
    @CCD(
            label = "Payment status",
            hint = "Paid for confirmation, anything else for Failed",
            access = {CaseworkerPrivatelawSolicitorCruAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    private String tsPaymentStatus;
    @CCD(
            label = "HEF Requested For Additional Applications",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class}
    )
    private YesOrNo hwfRequestedForAdditionalApplications;

    @CCD(
            label = "Respondent documents",
            searchable = false,
            access = {C100RESPONDENTBARRISTER1CudPlus11RolesHrwtotAccess.class}
    )
    private List<Element<RespondentDocs>> respondentDocsList;

    @JsonUnwrapped
    private CitizenResponseDocuments citizenResponseDocuments;

    @JsonUnwrapped
    private RespondentC8Document respondentC8Document;

    @JsonUnwrapped
    private RespondentC8 respondentC8;
    //PRL-3454 - send and reply message enhancements
    @JsonUnwrapped
    private SendOrReplyMessage sendOrReplyMessage;

    @JsonUnwrapped
    private DocumentManagementDetails documentManagementDetails;

    /**
     * Review documents.
     */
    @JsonUnwrapped
    private ReviewDocuments reviewDocuments;

    @JsonUnwrapped
    private StatementOfService statementOfService;

    @JsonUnwrapped
    private AllPartyFlags allPartyFlags;
    /**
     * PRL-4260,4335,4301 - manage orders hearing screen fields show params.
     */
    @JsonUnwrapped
    public OrdersHearingPageFieldShowParams ordersHearingPageFieldShowParams;

    /**
     * PRL-4044 - This is store citizen awp payment data for temp & will be removed once awp is submitted successfully.
     */
    @CCD(
            label = "Citizen AWP payments",
            searchable = false,
            access = {CaseworkerPrivatelawSystemupdateCitizenCrudAccess.class}
    )
    @JsonProperty("citizenAwpPayments")
    private List<Element<CitizenAwpPayment>> citizenAwpPayments;

    //For case documents tab
    @CCD(
            label = "Other proceedings",
            access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess.class, CaseworkerPrivatelawSolicitorCitizenCuAccess.class, CaseworkerPrivatelawLaCuAccess.class, CaseworkerPrivatelawSuperuserCuAccess.class}
    )
    private final List<Element<ProceedingDetails>> existingProceedingsWithDoc;

    @JsonUnwrapped
    private FM5ReminderNotificationDetails fm5ReminderNotificationDetails;

    @JsonUnwrapped
    private RemoveDraftOrderFields removeDraftOrderFields;

    @JsonUnwrapped
    private ReviewAdditionalApplicationWrapper reviewAdditionalApplicationWrapper;

    @JsonUnwrapped
    private ReviewRaRequestWrapper reviewRaRequestWrapper;

  // ==== ccd-definition-converter: synthesised definition-only fields (retrofit) ====
  @JsonUnwrapped private CaseDataExtra caseDataExtra;
  // ==== end synthesised definition-only fields ====
}
