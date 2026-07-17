package uk.gov.hmcts.reform.prl.models.dto.ccd;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.WhoChildrenLiveWith;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.TierOfJudiciaryEnum;
import uk.gov.hmcts.reform.prl.enums.SubmitConsentEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.SendToGatekeeperTypeEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoWitnessStatementsSentToEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoReportsSentToEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoRemoteHearingEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoJudgeLaDecideByEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoCrossExaminationSittingBeforeEnum;
import uk.gov.hmcts.reform.prl.enums.sdo.SdoBeforeAEnum;
import uk.gov.hmcts.reform.prl.enums.PassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherOrderRecipientsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.OtherEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OrderAppliedFor;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.ListOnNoticeReasonsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.HearingTypeEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CustomOrderNameOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CreateSelectOrderOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CaseTransferOptionsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.CafcassServiceApplicationEnum;
import uk.gov.hmcts.reform.prl.enums.manageorders.CafcassEnum;
import uk.gov.hmcts.reform.prl.enums.dio.AllocateOrReserveJudgeEnum;
import uk.gov.hmcts.reform.prl.enums.gatekeeping.AllocatedJudgeTypeEnum;

import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCruCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCrAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCrAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverRPlus3RolesYpnufeAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCrudCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawBulkscanCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawBulkscansystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawBulkscanCrudPlus1RolesIkmnbhAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverCaseworkerCaaRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRPlus1RolesUypxmrAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORCREATORCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCuCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCaseworkerPrivatelawReadonlyRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRPlus2RolesTimnnxAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCrudCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCrAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawLaRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORRPlus6RolesRnyqqlAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCruPlus1RolesViswowAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus1RolesEicidaAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR1CruPlus4RolesHmcngqAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORCREATORCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerCaaCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyCourtnavRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.AllocatedMagistrateCruPlus12RolesRpldvsAccess;
import uk.gov.hmcts.reform.prl.ccd.access.LASOCIALWORKERRPlus3RolesNmemgtAccess;
import uk.gov.hmcts.reform.prl.ccd.access.LASOCIALWORKERLASOLICITORRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassCtscTeamLeaderCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.LASOCIALWORKERLASOLICITORCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORCruPlus14RolesQztdkkAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CREATORCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORRPlus19RolesLrlmmbAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTBARRISTER1RPlus4RolesTrhpdrAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CREATORRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRCaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.HearingCentreTeamLeaderCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CREATORRCaseworkerPrivatelawSuperuserCruCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRPlus3RolesWkowzeAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudPlus3RolesCmdeveAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCrudPlus3RolesCdcrteAccess;
import uk.gov.hmcts.reform.prl.ccd.access.GSProfileRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesPgdngkAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassCaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus2RolesGtbivmAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCrudPlus3RolesIslhzwAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawBulkscanCrudPlus5RolesYfhrueAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCruCourtnavRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesKaqxnzAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCourtnavCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus2RolesTbxcefAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCrudPlus3RolesWisbigAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverCaseworkerCaaCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTBARRISTER1RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTSOLICITOR1RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTBARRISTER2RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTSOLICITOR2RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTBARRISTER3RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTSOLICITOR3RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTBARRISTER4RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTSOLICITOR4RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTBARRISTER5RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTSOLICITOR5RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTBARRISTER1RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR1RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR2RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR3RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR4RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR5RAccess;
import uk.gov.hmcts.reform.prl.ccd.access.FL401APPLICANTSOLICITORRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.FL401RESPONDENTSOLICITORRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCaseworkerPrivatelawSolicitorRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.PaymentsCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTBARRISTER1CruPlus5RolesTjdifxAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORRPlus9RolesSpmfxdAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRPlus2RolesIqgqdqAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTBARRISTER1C100RESPONDENTSOLICITOR1CuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR2CuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR3CuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR4CuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR5CuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.PuiCaseManagerCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORRPlus31RolesVrdqykAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCourtnavRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORCruPlus30RolesEzpiauAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverRPlus6RolesXmoyviAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AddCaseNoteType;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.HearingUrgency;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Applicant;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Respondent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Declaration;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Fl401TypeOfApplication;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.AllegationsOfHarmOverview;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.AllegationsOfHarmRevisedOverview;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Miam;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.MiamExemptions;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.MiamPolicyUpgrade;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.MiamPolicyUpgradeExemptions;
import uk.gov.hmcts.reform.prl.models.c100rebuild.OtherProceedings;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherProceedingsDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.InternationalElement;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.AttendingTheHearing;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.LitigationCapacity;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.WelshLanguageRequirements;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.AllegationsOfHarmOrders;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.AllegationsOfHarmRevisedOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationsOfHarmDomesticAbuse;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationsOfHarmChildAbduction;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharm.AllegationsOfHarmOtherConcerns;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationsOfHarmRevisedChildAbduction;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.AllegationsOfHarmRevisedOtherConcerns;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.AllegationsOfHarmRevisedChildContact;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherPersonInTheCase;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherPersonInTheCaseRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.OtherChildNotInTheCase;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildrenAndApplicants;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildrenAndRespondents;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildrenAndOtherPeople;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.ChildDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildDetailsRevisedExtraInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildExtraInfo;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.ApplicantFamily;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildToBeProtected;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.FL401Applicant;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.FL401SolicitorDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.HomeDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.RespondentBehaviour;
import uk.gov.hmcts.reform.prl.models.complextypes.WithoutNoticeOrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.FL401Respondent;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.RelationshipToRespondent;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Fl401OtherProceedingsDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.FL401ApplicantChildDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationsOfHarmRevisedDA;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationsOfHarmRevisedCA;
import uk.gov.hmcts.reform.prl.models.dto.ccd.BulkScanEnvelope;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CreateBundleTransitionDetailsObject;
import uk.gov.hmcts.reform.prl.models.complextypes.WelshNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.Fl401ChildConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ConfidentialityCheck;
import uk.gov.hmcts.reform.prl.models.dto.ccd.SwanseaDFJCourts;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HumbersideDFJCourts;
import uk.gov.hmcts.reform.prl.models.dto.ccd.EssexAndSuffolkDFJCourts;
import uk.gov.hmcts.reform.prl.models.dto.ccd.WolverhamptonDFJCourts;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DeletionConsent;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DioFurtherInstructionsEnum;
import uk.gov.hmcts.reform.prl.models.complextypes.MiamAttendingPersonName;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ExitAwaitingInformationDetailsType;
import uk.gov.hmcts.reform.prl.models.complextypes.StatementOfTruth;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;
import uk.gov.hmcts.reform.prl.models.complextypes.FurtherEvidence;
import uk.gov.hmcts.reform.prl.models.complextypes.Correspondence;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.OtherDocuments;
import uk.gov.hmcts.reform.prl.models.dto.ccd.TransferReasonEnum;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DateOrderEndsTimeEnum2;
import uk.gov.hmcts.reform.prl.models.dto.ccd.SchoolDirectionsDetails;
import uk.gov.hmcts.reform.prl.models.dto.judicial.FinalisationDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CustomC21OrderDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CustomC43OrderDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.AppointedGuardianFullName;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestFurtherInformationDetailsType;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RequestOrderHearingTracking;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarm;
import uk.gov.hmcts.reform.prl.models.complextypes.Behaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentChildAbduction;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentOtherConcerns;
import uk.gov.hmcts.reform.prl.models.complextypes.respondentsolicitor.documents.RespondentDocs;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ConfidentialityDisclaimerObject;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.ConfirmRecipients;
import uk.gov.hmcts.reform.prl.models.dto.ccd.SolicitorStopRepresentingDisclaimerEnum;
import uk.gov.hmcts.reform.prl.models.dto.gatekeeping.AllocatedJudge;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.refuge.RefugeCase;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseStatus;
import uk.gov.hmcts.reform.prl.models.dto.cafcass.ConfidentialDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.Urgency;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.ApplicationTypeDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.SpecialArrangements;
import uk.gov.hmcts.reform.prl.models.dto.ccd.SummaryTabOtherProceedings;
import uk.gov.hmcts.reform.prl.models.dto.ccd.DateSubmittedToHMCTS;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.OtherProceedingEmptyTable;
import uk.gov.hmcts.reform.prl.models.complextypes.tab.summarytab.summary.CaseClosedDate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.UpdateHearingActualTaskTracking;

/**
 * Synthesised definition-only fields for the retrofit that would otherwise push the root
 * case-data class past the JVM/Lombok all-args-constructor limit. Added to the root as a
 * prefix-less {@code @JsonUnwrapped} member, so these members flatten to the same CCD field
 * IDs. Generated by ccd-definition-converter (retrofit).
 *
 * <p>ccd-definition-converter:retrofit-overflow-companion
 */
@Data
public class CaseDataExtra {

  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawSolicitorCrudAccess.class}
  )
  private String testCaseName;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCruCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String addCaseNoteHeaderCaseNameText;
  @CCD(
          label = "${addCaseNoteHeaderCaseNameText}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCrAccess.class, CaseworkerPrivatelawJudgeCrAccess.class, CaseworkerPrivatelawLaCrAccess.class, CaseworkerPrivatelawSuperuserCrAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
  )
  private String addCaseNoteHeaderCaseName;
  @CCD(
          label = "[Add case note](/cases/case-details/${[CASE_REFERENCE]}/trigger/addCaseNote)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String addCaseNoteLink;
  @CCD(
          label = "Case notes",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class, CaseworkerWaTaskConfigurationCrudAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<AddCaseNoteType>> addCaseNoteTable;
  @CCD(
          label = "## Select a legal representative to remove",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String removeLegalRepHeader;
  @CCD(
          label = "### Once you’ve submitted this request \n\n- The legal representative will no longer have access to this case\n- If the case has been shared with other legal representatives, they will also lose access\n-Linked cases are not affected. To remove a legal representative from a linked case, go to that case and repeat this action",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String removeLegalRepInfo;
  @CCD(
          label = "If you fill out this section you do not need to send a separate C1A form.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String newAllegationsOfHarmHint;
  @CCD(
          label = "Are there allegations that the child(ren) or applicants(s) have experienced, or are at\nrisk of experiencing, harm from any of the following by any person who has had\ncontact with the child?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String newAllegationsOfHarmLabel;
  @CCD(
          label = "### Orders",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String newAllegationOfHarmOrdersLabel;
  @CCD(
          label = "Has the applicant had (or does the applicant currently have) any of the these\norders?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String newAllegationOfHarmOrdersLabelDetail;
  @CCD(
          label = "## Domestic abuse - Behaviours",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String domesticAbuseBehavioursLabel;
  @CCD(
          label = "## Child abuse - Behaviours",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String childAbuseBehavioursLabel;
  @CCD(
          label = "## Child abduction",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String childAbductionLabel;
  @CCD(
          label = "## Other concerns",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String othersConcernsLabel;
  @CCD(
          label = "Give a short description of what happened and any relevant information so the court\ncan decide what needs to be done. ",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String childAbuseBehavioursSubLabel;
  @CCD(
          label = "Give a short description of what happened and any relevant information so the court\ncan decide what needs to be done.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String domesticAbuseBehavioursSubLabel;
  @CCD(
          label = "### Child contact",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String newAllegationsOfHarmChildContactLabel;
  @CCD(
          label = "## Child abuse - Physical abuse",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String childPhysicalAbuseLabel;
  @CCD(
          label = "Give short description of what happened and any relevant information so the court \n can decide what needs to be done.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String childPhysicalAbuseSubLabel;
  @CCD(
          label = "## Child abuse - Psychological abuse",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String childPsychologicalAbuseLabel;
  @CCD(
          label = "Give short description of what happened and any relevant information so the court \n can decide what needs to be done.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String childPsychologicalAbuseSubLabel;
  @CCD(
          label = "## Child abuse - Sexual abuse",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String childSexualAbuseLabel;
  @CCD(
          label = "Give short description of what happened and any relevant information so the court \n can decide what needs to be done.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String childSexualAbuseSubLabel;
  @CCD(
          label = "## Child abuse - Emotional abuse",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String childEmotionalAbuseLabel;
  @CCD(
          label = "Give short description of what happened and any relevant information so the court \n can decide what needs to be done.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String childEmotionalAbuseSubLabel;
  @CCD(
          label = "## Child abuse - Financial abuse",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String childFinancialAbuseLabel;
  @CCD(
          label = "Give short description of what happened and any relevant information so the court \n can decide what needs to be done.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class}
  )
  private String childFinancialAbuseSubLabel;
  @CCD(
          label = "## Allocated judge",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String allocatedJudeLabel;
  @CCD(
          label = "You can update this at any point in the case",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String allocatedJudgeInfo;
  @CCD(
          label = "Do you want to allocate a specific judge or legal adviser?",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isSpecificJudgeOrLegalAdviserNeeded;
  @CCD(
          label = "Please select judge or legal adviser",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private AllocatedJudgeTypeEnum isJudgeOrLegalAdviser;
  @CCD(
          label = "Name of judge",
          searchable = false,
          typeOverride = FieldType.JudicialUser,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String judgeNameAndEmail;
  @CCD(
          label = "Select the tier of judiciary",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private TierOfJudiciaryEnum tierOfJudiciary;
  @CCD(label = "Tier of judge", searchable = false, access = {CaseworkerPrivatelawSystemupdateCruAccess.class})
  private String tierOfJudge;
  @JsonProperty("ApplicationPaymentLinkLabel")
  @CCD(
          label = "<a class=\"govuk-link\" href=\"/cases/case-details/${[CASE_REFERENCE]}/#Service Request\" target=\"_self\">Click here</a> to pay for the application",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRAccess.class}
  )
  private String applicationPaymentLinkLabel;
  @CCD(
          label = "# Applications",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabLabel;
  @CCD(
          label = "${applicantCaseName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String applicationTabCaseNameLabel;
  @CCD(
          label = "## Court",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String courtLabel;
  @CCD(label = "Court", searchable = false, access = {CaseworkerPrivatelawSystemupdateCudAccess.class})
  private Court courtDetails;
  @CCD(
          label = "## Hearing Urgency",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabHearingUrgencyLabel;
  @CCD(
          label = "Hearing Urgency",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private HearingUrgency hearingUrgencyTable;
  @CCD(
          label = "## Applicant Details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabApplicantDetailsLabel;
  @CCD(
          label = "Applicant",
          searchable = false,
          min = 1,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class, CitizenCuAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Applicant>> applicantTable;
  @CCD(
          label = "## Respondent Details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabRespondentDetailsLabel;
  @CCD(
          label = "Respondents",
          searchable = false,
          min = 1,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class, CitizenCuAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Respondent>> respondentTable;
  @CCD(
          label = "## Declaration",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabDeclarationLabel;
  @CCD(
          label = "Declaration",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private Declaration declarationTable;
  @CCD(
          label = "## Child details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabChildLabel;
  @CCD(
          label = "## Child details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCudAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class, CaseworkerWaTaskConfigurationCudAccess.class}
  )
  private String applicationTabChildRevisedLabel;
  @CCD(
          label = "## Type of application",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabTypeOfApplicationLabel;
  @CCD(
          label = "Type of Application",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private Fl401TypeOfApplication fl401TypeOfApplicationTable;
  @CCD(
          label = "## Allegations of harm",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabAllegationsOfHarmLabel;
  @CCD(
          label = "## Allegations of harm",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabAllegationsOfHarmRevisedLabel;
  @CCD(
          label = "Allegations of harm",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private AllegationsOfHarmOverview allegationsOfHarmOverviewTable;
  @CCD(label = "Allegations of harm", access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class})
  private AllegationsOfHarmRevisedOverview allegationsOfHarmRevisedOverviewTable;
  @CCD(
          label = "## MIAM",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabMiamLabel;
  @CCD(label = "MIAM", searchable = false, access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class})
  private Miam miamTable;
  @CCD(
          label = "Miam exemption details",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private MiamExemptions miamExemptionsTable;
  @CCD(
          label = "MIAM",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerPrivatelawSolicitorCitizenCudAccess.class}
  )
  private MiamPolicyUpgrade miamPolicyUpgradeTable;
  @CCD(
          label = "Miam exemption details",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerPrivatelawSolicitorCitizenCudAccess.class}
  )
  private MiamPolicyUpgradeExemptions miamPolicyUpgradeExemptionsTable;
  @CCD(
          label = "## Other proceedings",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class, CitizenCudAccess.class}
  )
  private String applicationTabOtherProceedingsLabel;
  @CCD(
          label = "Other proceedings",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private OtherProceedings otherProceedingsTable;
  @CCD(
          label = "Other proceedings",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<OtherProceedingsDetails>> otherProceedingsDetailsTable;
  @CCD(
          label = "## International element",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabInternationalElementLabel;
  @CCD(
          label = "International element",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private InternationalElement internationalElementTable;
  @CCD(
          label = "## Attending the hearing",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabAttendingTheHearingLabel;
  @CCD(
          label = "Attending the hearing",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private AttendingTheHearing attendingTheHearingTable;
  @CCD(
          label = "## Litigation capacity",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabLitigationCapacityLabel;
  @CCD(
          label = "Litigation capacity",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private LitigationCapacity litigationCapacityTable;
  @CCD(
          label = "## Welsh language requirements",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabWelshLanguageRequirementsLabel;
  @CCD(
          label = "Welsh language requirements",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private WelshLanguageRequirements welshLanguageRequirementsTable;
  @CCD(
          label = "## Allegations of harm details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabAllegationsOfHarmDetailsLabel;
  @CCD(label = "Orders", searchable = false, access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class})
  private AllegationsOfHarmOrders allegationsOfHarmOrdersTable;
  @CCD(label = "Orders", access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class})
  private AllegationsOfHarmRevisedOrders allegationsOfHarmRevisedOrdersTable;
  @CCD(
          label = "Any form of domestic abuse",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private AllegationsOfHarmDomesticAbuse allegationsOfHarmDomesticAbuseTable;
  @CCD(
          label = "Child abduction",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private AllegationsOfHarmChildAbduction allegationsOfHarmChildAbductionTable;
  @CCD(
          label = "Other concerns",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private AllegationsOfHarmOtherConcerns allegationsOfHarmOtherConcernsTable;
  @CCD(label = "Child abduction", access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class})
  private AllegationsOfHarmRevisedChildAbduction allegationsOfHarmRevisedChildAbductionTable;
  @CCD(label = "Other concerns", access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class})
  private AllegationsOfHarmRevisedOtherConcerns allegationsOfHarmRevisedOtherConcernsTable;
  @CCD(
          label = "## Other people in the case",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String applicationTabOtherPeopleInTheCaseLabel;
  @CCD(
          label = "## Other people in the case",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private String applicationTabOtherPeopleRevisedInTheCaseLabel;
  @CCD(label = "Child contact", access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class})
  private AllegationsOfHarmRevisedChildContact allegationsOfHarmRevisedChildContactTable;
  @CCD(
          label = "Other people in the case",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<OtherPersonInTheCase>> otherPeopleInTheCaseTable;
  @CCD(
          label = "Other people in the case",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<OtherPersonInTheCaseRevised>> otherPeopleInTheCaseRevisedTable;
  @CCD(
          label = "## Other child not in the case",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private String applicationTabOtherChildNotInTheCaseLabel;
  @CCD(
          label = "Other child not in the case",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<OtherChildNotInTheCase>> otherChildNotInTheCaseTable;
  @CCD(
          label = "## Applicant relation to child",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private String applicationTabChildAndApplicantsRelationLabel;
  @CCD(
          label = "Applicant relation to child",
          searchable = false,
          min = 1,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<ChildrenAndApplicants>> childAndApplicantsRelationTable;
  @CCD(
          label = "## Respondent relation to child",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private String applicationTabChildAndRespondentRelationLabel;
  @CCD(
          label = "Respondent relation to child",
          searchable = false,
          min = 1,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<ChildrenAndRespondents>> childAndRespondentRelationsTable;
  @CCD(
          label = "## Other people in the application's relation to child",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private String applicationTabChildAndOtherPeopleRelationLabel;
  @CCD(
          label = "Other people in the application's relation to child",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerPrivatelawSolicitorCitizenCudAccess.class, CaseworkerWaTaskConfigurationCudAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<ChildrenAndOtherPeople>> childAndOtherPeopleRelationsTable;
  @CCD(label = "Child", searchable = false, access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class})
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<ChildDetails>> childDetailsTable;
  @CCD(label = "Child", searchable = false, access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class})
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<ChildDetailsRevised>> childDetailsRevisedTable;
  @CCD(
          label = "Additional questions",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private ChildDetailsRevisedExtraInfo childDetailsRevisedExtraTable;
  @CCD(
          label = "Additional questions",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class}
  )
  private ChildExtraInfo childDetailsExtraTable;
  @CCD(
          label = "## Applicant’s family",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, CaseworkerPrivatelawSolicitorCudAccess.class}
  )
  private String applicantFamilyTableLabel;
  @CCD(
          label = "Applicant’s family",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private ApplicantFamily applicantFamilyTable;
  @CCD(
          label = "## Child",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeRAccess.class}
  )
  private String childInfoTableLabel;
  @CCD(
          label = "Child",
          searchable = false,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private ChildInfo childInfoTable;
  @CCD(
          label = "## Child to be protected",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeRAccess.class}
  )
  private String childToBeProtectedTableLabel;
  @CCD(
          label = "Child to be protected",
          searchable = false,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private ChildToBeProtected childToBeProtectedTable;
  @CCD(
          label = "Applicant",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class, CitizenCuAccess.class}
  )
  private FL401Applicant fl401ApplicantTable;
  @CCD(
          label = "## Legal representative's details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, CaseworkerPrivatelawSolicitorCudAccess.class}
  )
  private String fl401SolicitorLabel;
  @CCD(
          label = "Legal representative's details",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private FL401SolicitorDetails fl401SolicitorDetailsTable;
  @CCD(
          label = "The home",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private HomeDetails homeDetailsTable;
  @CCD(
          label = "## The home",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, CaseworkerPrivatelawSolicitorCudAccess.class}
  )
  private String homeDetailsTableLabel;
  @CCD(
          label = "## Respondent’s behaviour",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, CaseworkerPrivatelawSolicitorCudAccess.class}
  )
  private String respondentBehaviourTableLabel;
  @CCD(
          label = "Respondent’s behaviour",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CitizenCudAccess.class}
  )
  private RespondentBehaviour respondentBehaviourTable;
  @CCD(
          label = "## Without notice order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, CaseworkerPrivatelawSolicitorCudAccess.class}
  )
  private String withoutNoticeOrderTableLabel;
  @CCD(
          label = "Without notice order",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private WithoutNoticeOrderDetails withoutNoticeOrderTable;
  @CCD(
          label = "Respondent’s details",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CitizenCuAccess.class}
  )
  private FL401Respondent fl401RespondentTable;
  @CCD(
          label = "## Relationship to  respondent ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, CaseworkerPrivatelawSolicitorCudAccess.class}
  )
  private String relationshipToRespondentTableLabel;
  @CCD(
          label = "Relationship to  respondent",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private RelationshipToRespondent relationshipToRespondentTable;
  @CCD(
          label = "Proceeding details",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Fl401OtherProceedingsDetails>> fl401OtherProceedingsDetailsTable;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private String isHomeEntered;
  @CCD(
          label = "## Children ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private String fl401ChildDetailsTableLabel;
  @CCD(
          label = "Child",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CourtnavCuAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<FL401ApplicantChildDetails>> fl401ChildDetailsTable;
  @CCD(
          label = "## Statement of truth",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String fL401ApplicationTabDeclarationLabel;
  @CCD(
          label = "## Help with Fees details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRPlus3RolesYpnufeAccess.class}
  )
  private String helpWithFeesDetails;
  @CCD(label = "Domestic abuse", access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class})
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<AllegationsOfHarmRevisedDA>> allegationsOfHarmRevisedDATable;
  @CCD(label = "Behaviour", access = {CaseworkerPrivatelawSolicitorCudPlus2RolesIvixvvAccess.class})
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<AllegationsOfHarmRevisedCA>> allegationsOfHarmRevisedCATable;
  @CCD(
          label = "Cover letter",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawCafcassRAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document coverLetter;
  @CCD(
          label = "Exception Record Reference",
          searchable = false,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawBulkscanCruAccess.class, CaseworkerPrivatelawBulkscansystemupdateCruAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String bulkScanCaseReference;
  @CCD(
          label = "Supplementary evidence handled",
          searchable = false,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawBulkscanCrudPlus1RolesIkmnbhAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo evidenceHandled;
  @CCD(
          label = "Bulk Scan Envelopes",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess.class, CaseworkerPrivatelawBulkscanCrudPlus1RolesIkmnbhAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<BulkScanEnvelope>> bulkScanEnvelopes;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String c100RebuildConfidentiality;
  @CCD(
          label = "C8 Archived Document",
          categoryID = "c8ArchivedDocuments",
          searchable = false,
          access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerApproverCaseworkerCaaRAccess.class, CaseworkerPrivatelawCafcassRCitizenCruAccess.class, CaseworkerPrivatelawExternaluserViewonlyRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document c8ArchivedDocument;
  @CCD(
          label = "Cafcass Date time",
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCrudCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private java.time.LocalDateTime cafcassDateTime;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus1RolesUypxmrAccess.class}
  )
  private CreateBundleTransitionDetailsObject createBundleTransitionDetails;
  @CCD(
          label = "<div class='govuk-warning-text'><span class='govuk-warning-text__icon' aria-hidden='true'>!</span><strong class='govuk-warning-text__text'>Please check all areas of the digital case management system, as documents may be stored in different sections and not be available in the created bundle</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label
  )
  private String bundleHintText;
  @CCD(
          label = "<div class='govuk-box-highlight'><strong><h1>Please continue and select CreateBundle button in the next page for generating the bundle </h1></strong></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus1RolesUypxmrAccess.class}
  )
  private String createBundleSubmitLabel;
  @CCD(
          label = "## What happens next",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus1RolesUypxmrAccess.class}
  )
  private String bundleCreationSubmittedWhatHappensNext;
  @CCD(
          label = "Please wait for sometime and refresh the page manually to see the generated bundle if StitchedDocument field is not populated in the bundles Tab after clicking on CreateBundle button in the next page",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus1RolesUypxmrAccess.class}
  )
  private String bundleCreationSubmittedWhatHappensNextLabel;
  @CCD(
          label = "Case name",
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, APPLICANTSOLICITORCREATORCitizenRAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
  )
  private String taskList;
  @CCD(
          label = "${taskList}",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, APPLICANTSOLICITORCREATORCitizenRAccess.class, CaseworkerApproverCaseworkerCaaRAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
  )
  private String taskListLabel;
  @CCD(
          label = "Case name",
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, APPLICANTSOLICITORCREATORCitizenRAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
  )
  private String taskListReturn;
  @CCD(
          label = "${taskListReturn}",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, APPLICANTSOLICITORCREATORCitizenRAccess.class, CaseworkerApproverCaseworkerCaaRAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
  )
  private String taskListReturnLabel;
  @CCD(
          label = "History",
          typeOverride = FieldType.CaseHistoryViewer,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String caseHistory;
  @CCD(
          label = "**Please upload documents that relate to this application:**",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String labelPleaseUploadDocuments;
  @CCD(
          label = "Documents required based on your answers",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String labelDocumentsRequired;
  @CCD(
          label = "A contact or residence order made within proceedings for a divorce or dissolution of a civil partnership",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String labelContactOrResidenceOrder;
  @CCD(
          label = "--- \n C8 form for confidentiality of the applicant",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String labelC8FormForConfidentiality;
  @CCD(
          label = "--- \n Upload other documents",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String labelUploadOtherDocuments;
  @CCD(
          label = "### Language requirements",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String labelLanguageRequirements;
  @CCD(
          label = "Welsh needs",
          min = 1,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class, CaseworkerPrivatelawSolicitorCuCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<WelshNeed>> fl401WelshNeeds;
  @CCD(
          label = "### Accessibility",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String labelAccessibility;
  @CCD(
          label = "*Describe the adjustments that the court needs to make.\n\nFor example - someone with a hearing impairment may need an induction loop to be fitted in the courtroom.",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String labelAdjustmentsRequired;
  @CCD(
          label = "### Special arrangements",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String labelSpecialArrangements;
  @CCD(
          label = "In some cases, the court can make special arrangements for an adult or child involved in the case.\n\nFor example - the court may provide a separate waiting room that is set apart from the respondent.",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String labelSpecialArrangementsDescription;
  @CCD(
          label = "*Give details of the special arrangements that are required.\n\nFor example, a screen to separate the applicant from the respondent.",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String labelSpecialArrangementsRequired;
  @CCD(
          label = "The court can appoint an intermediary for vulnerable applicants.\n\nThe intermediary helps the applicant to communicate and give evidence during the case.",
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String labelIntermediaryDescription;
  @CCD(
          label = "A copy of the draft order must be uploaded in the **Upload a document** section",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String paraConsentOrderNotification;
  @CCD(
          label = "Provide more information on the type of order you are requesting.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateRAccess.class, CaseworkerPrivatelawLaCaseworkerPrivatelawReadonlyRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
  )
  private String natureOfOrderLabel;
  @CCD(
          label = "### Why are you making this application?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String paraWhyMakingApplication;
  @CCD(
          label = "### *Why are you making this application?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String paraWhyMakingApplication2;
  @CCD(
          label = "Provide brief details of:\r\n* any previous formal (or informal) parenting plans, and how these have broken down\r\n* the reason for bringing this application to court\r\n* what you are asking the court to do\r\n* reasons given by the respondent(s) for their actions in relation to this application",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String paraApplicationDetails;
  @CCD(
          label = "Any form of domestic abuse?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo domesticAbuse;
  @CCD(
          label = "Child abduction?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childAbduction;
  @CCD(
          label = "Child abuse?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childAbuse;
  @CCD(
          label = "Drugs, alcohol or substance abuse?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo drugsAlcoholSubstanceAbuse;
  @CCD(
          label = "Other safety or welfare concerns?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo safetyWelfareConcerns;
  @CCD(
          label = "### Child abduction",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String paraChildAbduction;
  @CCD(
          label = "Are the children at risk of being abducted?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childAtRiskOfAbduction;
  @CCD(
          label = "Have the police been notified?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo policeNotified;
  @CCD(
          label = "Do any of the children have a passport?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childHasPassport;
  @CCD(
          label = "Have the children been abducted or kept outside the UK without your consent before?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childAbductedBefore;
  @CCD(
          label = "*Do the children have more than one passport?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childHasMultiplePassports;
  @CCD(
          label = "*Who is in possession of the children's passports?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private java.util.Set<PassportPossessionEnum> childPassportPossession;
  @CCD(
          label = "Give details of the other person",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childPassportPossessionOtherDetails;
  @CCD(
          label = "Provide details of previous abductions",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childAbductionDetails;
  @CCD(
          label = "Were the police, private investigators or any Organisation involved?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo abductionPoliceInvolved;
  @CCD(
          label = "Provide more details",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String abductionPoliceInvolvedDetails;
  @CCD(
          label = "Why do you think the children may be abducted or kept outside the UK without the Applicant's consent?",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childAtRiskOfAbductionReason;
  @CCD(
          label = "Where are the children now?",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childWhereabouts;
  @CCD(
          label = "### Child abuse",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String paraChildAbuse;
  @CCD(
          label = "Have the children ever been sexually abused?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childAbuseSexually;
  @CCD(
          label = "Nature of behaviour / what happened?",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childAbuseSexuallyDetails;
  @CCD(
          label = "When did the behaviour start?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private java.time.LocalDate childAbuseSexuallyStartDate;
  @CCD(
          label = "Is the behaviour still ongoing?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childAbuseSexuallyOngoing;
  @CCD(
          label = "Was help sought? If so, who from?",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childAbuseSexuallyHelpSought;
  @CCD(
          label = "Have the children ever been physically abused?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childAbusePhysically;
  @CCD(
          label = "Nature of behaviour / what happened?",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childAbusePhysicallyDetails;
  @CCD(
          label = "When did the behaviour start?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private java.time.LocalDate childAbusePhysicallyStartDate;
  @CCD(
          label = "Is the behaviour still ongoing?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childAbusePhysicallyOngoing;
  @CCD(
          label = "Was help sought? If so, who from?",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childAbusePhysicallyHelpSought;
  @CCD(
          label = "Have the children ever been financially abused?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childAbuseFinancially;
  @CCD(
          label = "Nature of behaviour / what happened?",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childAbuseFinanciallyDetails;
  @CCD(
          label = "When did the behaviour start?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private java.time.LocalDate childAbuseFinanciallyStartDate;
  @CCD(
          label = "Is the behaviour still ongoing?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childAbuseFinanciallyOngoing;
  @CCD(
          label = "Was help sought? If so, who from?",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childAbuseFinanciallyHelpSought;
  @CCD(
          label = "Any form of domestic abuse?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childAbuseDomestic;
  @CCD(
          label = "Nature of behaviour / what happened?",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childAbuseDomesticDetails;
  @CCD(
          label = "When did the behaviour start?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private java.time.LocalDate childAbuseDomesticStartDate;
  @CCD(
          label = "Is the behaviour still ongoing?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childAbuseDomesticOngoing;
  @CCD(
          label = "Was help sought? If so, who from?",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childAbuseDomesticHelpSought;
  @CCD(
          label = "Drugs, alcohol or substance abuse?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childDrugsAlcoholSubstanceAbuse;
  @CCD(
          label = "Nature of behaviour / what happened?",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childDrugsAlcoholSubstanceAbuseDetails;
  @CCD(
          label = "When did the behaviour start?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private java.time.LocalDate childDrugsAlcoholSubstanceAbuseStartDate;
  @CCD(
          label = "Is the behaviour still ongoing?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo childDrugsAlcoholSubstanceAbuseOngoing;
  @CCD(
          label = "Was help sought? If so, who from?",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String childDrugsAlcoholSubstanceAbuseHelpSought;
  @CCD(
          label = "Other safety or welfare concerns?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo otherSafetyOrWelfareConcerns;
  @CCD(
          label = "Other safety or welfare concerns details",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
  )
  private String otherSafetyOrWelfareConcernsDetails;
  @CCD(
          label = "**Where do the children live?**",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String labelWhereChildrenLive;
  @CCD(
          label = "**The child(ren)**",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String labelChildren;
  @CCD(
          label = "**The child(ren) additional questions**",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String labelChildrenAdditionalQuestions;
  @CCD(
          label = "Are any of the children known to the local authority children's services?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private YesNoDontKnow isChildrenKnownToAuthority;
  @CCD(
          label = "*If Yes please state which child and the name of the Local Authority and Social worker (if known)",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String childAndLocalAuthority;
  @CCD(
          label = "Are any of the children the subject of a child protection plan?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private YesNoDontKnow isChildrenUnderChildProtection;
  @CCD(
          label = "Do all the children have the same parents?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private YesNoDontKnow isChildrenWithSameParents;
  @CCD(
          label = "If No, please give details of each parent and their children involved in this application",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String parentsAndTheirChildren;
  @CCD(
          label = "Please state everyone who has parental responsibility for each child and how they have parental responsibility (e.g. 'child's mother', 'child's father and was married to the mother when the child was born' etc.)",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String parentalResponsibilities;
  @CCD(
          label = "Who do the children currently live with?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private WhoChildrenLiveWith whoChildrenLiveWith;
  @CCD(
          label = "If other, please give the full address of the child, the names of any adults living with the children and their relationship to or involvement with the child.",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String childAddressAndAdultsLivingWith;
  @CCD(
          label = "**Other court cases which concern the children listed**",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String labelOtherCourtCases;
  @CCD(
          label = "Are there previous or ongoing proceedings for the child(ren)?",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isExistingProceedings;
  @CCD(
          label = "**The Applicant(s)**",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String labelTheApplicants;
  @CCD(
          label = " ",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class}
  )
  private String typeOfApplication;
  @CCD(
          label = "**The Respondent(s)**",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String labelTheRespondents;
  @CCD(
          label = "**Others who should be given notice**",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String labelOthersToNotify;
  @CCD(
          label = "**Other children not part of the application**",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String labelOtherChildren;
  @CCD(
          label = "## *Has the applicant attended a Mediation information & Assessment Meeting (MIAM)?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String applicantAttendedMIAMLabel;
  @CCD(
          label = "## *Is the applicant claiming exemption from the requirement to attend a MIAM ?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String claimingExemptionMIAMLabel;
  @CCD(
          label = "## MIAM Exemptions : what is the reason(s) for the applicant not attending a MIAM?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String miamExemptionsLabel;
  @CCD(
          label = "## *Has a family mediator informed the applicant that a mediator’s exemption applies, and they do not need to attend a MIAM ?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String familyMediatorMIAMLabel;
  @CCD(
          label = "*Select all that apply",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String miamExemptionsSelectAll;
  @CCD(
          label = "## MIAM Evidence : What evidence of domestic violence or abuse does the applicant have ?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String miamDomesticViolenceLabel;
  @CCD(
          label = "*Select all that apply",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String miamDomesticViolenceSelectAll;
  @CCD(
          label = "## MIAM Evidence: What reason does the applicant have for child protection concerns?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String miamChildProtectionConcernLabel;
  @CCD(
          label = "## MIAM Evidence: what reason does the applicant have for the application to be made urgently?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String miamUrgencyReasonLabel;
  @CCD(
          label = "*Select all that apply",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String miamUrgencyReasonSelectAll;
  @CCD(
          label = "## MIAM Evidence : Previous MIAM attendance or MIAM exemption",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String miamPreviousAttendanceLabel;
  @CCD(
          label = "## MIAM Evidence : What other grounds of exemption apply?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String miamOtherGroundsLabel;
  @CCD(
          label = "## Enter details of MIAM certification",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus2RolesTimnnxAccess.class}
  )
  private String miamCertificationPageLabel;
  @CCD(
          label = "**You should have a document signed by the mediator confirming this. Upload this here.**",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus2RolesTimnnxAccess.class}
  )
  private String miamCertificationPageMessage;
  @CCD(
          label = "## Enter details of MIAM certification",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String miamCertificationPageLabel1;
  @CCD(
          label = "**You should have a document signed by the mediator confirming this. Upload this here.**",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String miamCertificationPageMessage1;
  @CCD(
          label = "If the applicant has applied for Help with Fees, you should email or post this application to the court instead.\n\nIf you continue online, you will still have to pay the fee. You can apply for a refund by contacting the court and providing the applicant's Help with Fees reference.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String helpWithFeesNotAvailable;
  @CCD(
          label = "Case Name",
          hint = "Enter the full name of the applicant and respondent. For example, Jo Davis & Jon Smith",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CitizenCrudCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawSolicitorCrAccess.class}
  )
  private String applicantOrRespondentCaseName;
  @CCD(
          label = "## Type of application\nWhich application are you applying for ?\n\n### You have 28 days to submit your application from the date you started it, or it will be deleted and you will need to start the application again. This is to keep your information secure.\n\n### You can review all your answers before you submit your application.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess.class}
  )
  private String caseTypeOfApplicationLabel;
  @CCD(
          label = "*Did you receive the case from CourtNav?",
          hint = "Questions marked with a * need to be completed before you can create a case",
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo caseFromCourtNav;
  @CCD(
          label = "## Confidentiality Statement\n\n**When completing this form, you have the option to mark information as  \nconfidential and you do not need to complete a C8 form.**\n\nYou should do this if you wish to keep certain information private.\n\n### Who will have access to the confidential information\n\nIf you mark information as confidential, it will only be accessible to:\n- the court\n- the judiciary\n\nThe information will not be revealed to anyone else, unless ordered by the court.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess.class}
  )
  private String fl401ConfidentialityDisclaimerLabel1;
  @CCD(
          label = "## Confidentiality Statement\n\nWhen completing this form, you have the option to mark information as confidential. \n\nYou should do this if you wish to keep certain information private.\n\n### Who will have access to the confidential information\n\nIf you mark information as confidential, it will only be accessible to:\n- the court\n- the judiciary\n- the Children and Family Court Advisory and Support Service (CAFCASS)\n- CAFCASS Cymru\n\nThe information will not be revealed to anyone else, unless ordered by the court.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess.class}
  )
  private String c100ConfidentialityDisclaimerLabel1;
  @CCD(
          label = "## Select the family court",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCruAccess.class}
  )
  private String c100SelectFamilyCourtLabel1;
  @CCD(
          label = "**Additional questions**",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
  )
  private String childDetailsAdditionalQuestionsLabel;
  @CCD(
          label = "If you fill out this section you do not need to send a separate C1A form.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRAccess.class}
  )
  private String allegationsOfHarmHint;
  @CCD(
          label = "Are there allegations that the child(ren) have experienced, or are at risk of experiencing, harm from any of the following by any person who has had contact with the child?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String allegationsOfHarmLabel;
  @CCD(
          label = "*Do you have any other concerns about your child(ren)'s safety and wellbeing? ",
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo abductionOtherSafetyConcerns;
  @CCD(
          label = "*Give details",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String abductionOtherSafetyConcernsDetails;
  @CCD(
          label = "*What steps or orders do you want the court to take or make to protect the safety of the child(ren) and/or yourself?",
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String abductionCourtStepsRequested;
  @CCD(
          label = "### Orders",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawLaRAccess.class}
  )
  private String allegationOfHarmOrdersLabel;
  @CCD(
          label = "Has the applicant had or does the applicant currently have any of the following orders?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawLaRAccess.class}
  )
  private String allegationOfHarmOrdersLabelDetail;
  @CCD(
          label = "### Other concerns",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawLaRAccess.class}
  )
  private String allegationsOfHarmOtherConcernsLabel;
  @CCD(
          label = "### Child contact",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String allegationsOfHarmChildContactLabel;
  @CCD(
          label = "Other court cases which concern the children listed",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, APPLICANTSOLICITORRPlus6RolesRnyqqlAccess.class, CitizenRAccess.class}
  )
  private String otherProceedingsLabel;
  @CCD(
          label = "### Download Application",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawLaRAccess.class}
  )
  private String downloadApplicationLabel;
  @CCD(
          label = "Use this link to download and check the application.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String linkToDownloadApplicationLabel;
  @CCD(
          label = "### C100 draft",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String c100draftOrderDocLabel;
  @CCD(
          label = "### FL401 draft",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String fl401draftOrderDocLabel;
  @CCD(
          label = "### Declaration",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String submitAndPayDeclaration;
  @CCD(
          label = "I understand that proceedings for contempt of court may be brought against anyone who makes, or causes to be made, a false statement in a document verified by a statement of truth without an honest belief in its truth.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String submitAndPayAgreeStmtLabel;
  @CCD(
          label = "The applicant believes that the facts stated in this form and any continuation sheets are true. ${solicitorName}  is authorised by the applicant to sign this statement. ",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String submitAndPayAgreeSignStmtLabel;
  @CCD(
          label = " ",
          hint = "This option should only be selected if the legal representative is signing the form on behalf of the applicant.",
          searchable = false,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerWaTaskConfigurationRAccess.class}
  )
  private java.util.Set<SubmitConsentEnum> payAgreeStatement;
  @CCD(
          label = " ",
          hint = "This option should only be selected if the legal representative is signing the form on behalf of the applicant.",
          searchable = false,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class}
  )
  private java.util.Set<SubmitConsentEnum> submitAgreeStatement;
  @CCD(
          label = "### Next step - Submit the application \n\n After you have submitted your application, you will be asked to pay the application fee.\n\n### Help with Fees is not yet available in the Family Private Law digital service.\n\n### If the applicant has applied for Help with Fees, you should email or post this application to the court instead.\n\n### If you continue online, you will still have to pay the fee. You can apply for a refund by contacting the court and providing the applicant's Help with Fees reference.\n\n### Application fee due to be paid",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String submitAndPayNextPage;
  @CCD(
          label = "When you submit the application you will be asked to pay the application fee.\n\n If you have applied to get help with court fees, you will be asked to provide your Help with Fees reference number.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String submitAndPayNextPageDesc;
  @CCD(
          label = "### Application fee due to be paid",
          hint = "£${feeAmount}",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String submitAndPayFee;
  @CCD(
          label = "### Final checks \n\n Download and check the application before you proceed. Make sure that all details are correct. \n\n ### C100 draft",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String submitAndPayDownloadApplication;
  @CCD(
          label = "Download and check the application before you proceed. Make sure that all details are correct.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String submitAndPayDownloadApplicationLinkLabel;
  @CCD(
          label = "### C100 draft",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String c100submitAndPayDownloadApplicationLinkLabel;
  @CCD(
          label = "${viewPDFlinkLabelText}",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String viewPDFlinkLabel;
  @CCD(
          label = "Service Request",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String serviceRequest;
  @CCD(
          label = "### Select the reason for rejection",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String rejectReasonLabel;
  @CCD(
          label = "### Returning an application",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String returningAnApplicationLabel;
  @CCD(
          label = "Select the reason(s) for the return of the application, this will generate a letter with instructions.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String returningAnApplicationText;
  @CCD(
          label = "### Return message",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruPlus1RolesViswowAccess.class}
  )
  private String returnMessageLabel;
  @CCD(
          label = "Let the local court admin know there’s a new case \n\n## ${courtName} is currently chosen",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String localCourtHint;
  @CCD(
          label = "Child confidential details",
          access = {CaseworkerPrivatelawCourtadminCudPlus1RolesEicidaAccess.class, CaseworkerApproverRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Fl401ChildConfidentialityDetails>> fl401ChildrenConfidentialDetails;
  @JsonProperty("TestField")
  @CCD(
          label = "FamilyMan case number",
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String testField;
  @CCD(
          label = "You can save and return to this page at any time. Questions marked with a * need to be completed before you can send your application.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, C100RESPONDENTSOLICITOR1CruPlus4RolesHmcngqAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, CaseworkerPrivatelawSuperuserCitizenCruAccess.class}
  )
  private String submissionRequiredFieldsInfo1;
  @CCD(
          label = "You can save and return to this page at any time. Questions marked with a * need to be completed before you can send your application.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String submissionRequiredFieldsInfo2;
  @CCD(
          label = "You can save and return to this page at any time. Questions marked with a * need to be completed before you can send your application.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String submissionRequiredFieldsInfo3;
  @CCD(
          label = "You can save and return to this page at any time. Questions marked with a * need to be completed before you can send your application.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String submissionRequiredFieldsInfo4;
  @CCD(
          label = "You can save and return to this page at any time. Questions marked with a * need to be completed before you can send your application.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String submissionRequiredFieldsInfo5;
  @CCD(
          label = "You can save and return to this page at any time. Questions marked with a * need to be completed before you can send your application.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class}
  )
  private String submissionRequiredFieldsInfo6;
  @CCD(
          label = "You can save and return to this page at any time. Questions marked with a * need to be completed before you can send your application.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus2RolesTimnnxAccess.class}
  )
  private String submissionRequiredFieldsInfo7;
  @CCD(
          label = "You can save and return to this page at any time. Questions marked with a * need to be completed before you can send your application.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class}
  )
  private String submissionRequiredFieldsInfo8;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess.class}
  )
  private ConfidentialityCheck fl401ConfidentialityCheck;
  @CCD(
          label = "The court will review the application and make a decision on the urgency of the application based on the details you provide",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class}
  )
  private String hearingUrgencyLabel;
  @CCD(
          label = "# Select the family court",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeRAccess.class}
  )
  private String submitCountyCourtSelectionLabel;
  @CCD(
          label = "Submitted date and time",
          searchable = false,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerApproverCruAccess.class, CaseworkerCaaCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class, CourtnavCruAccess.class}
  )
  private String dateSubmittedAndTime;
  @CCD(
          label = "[Upload additional applications](/case/${[JURISDICTION]}/${[CASE_TYPE]}/${[CASE_REFERENCE]}/trigger/uploadAdditionalApplications)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSystemupdateCuAccess.class, CitizenCuAccess.class}
  )
  private String uploadC2Link;
  @CCD(
          label = "### Next step - Submit the application \n\n After you have submitted your application, you will be asked to pay the application fee.\n\n### Help with Fees is not yet available in the Family Private Law digital service.\n\n### If the applicant has applied for Help with Fees, you should email or post this application to the court instead.\n\n### If you continue online, you will still have to pay the fee. You can apply for a refund by contacting the court and providing the applicant's Help with Fees reference.\n\n### Application fee\n\n ${additionalApplicationFeesToPay}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String additionalApplicationFeesToPayText;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawExternaluserViewonlyCourtnavRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isInHearingState;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesGlmraeAccess.class, CourtnavRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isInvokedFromTask;
  @CCD(
          label = "Case note collection id",
          searchable = false,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCAccess.class, CitizenCrudAccess.class}
  )
  private String caseNoteId;
  @CCD(
          label = "Pre-migration case name backup",
          searchable = false,
          access = {CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private String caseNameHmctsInternalBackup;
  @CCD(
          label = "Task Assignee Idam id",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
  )
  private String taskAssigneeIdamId;
  @CCD(
          label = "Component Launcher",
          typeOverride = FieldType.ComponentLauncher,
          access = {AllocatedMagistrateCruPlus12RolesRpldvsAccess.class}
  )
  private String componentLauncher;
  @CCD(
          label = "Orders",
          categoryID = "orders",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> orders;
  @CCD(
          label = "Draft orders",
          categoryID = "draftOrders",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> ordersSubmittedWithApplication;
  @CCD(
          label = "Finalised order",
          categoryID = "approvedOrders",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> approvedOrders;
  @CCD(
          label = "Standard directions order",
          categoryID = "draftOrders",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> standardDirectionsOrder;
  @CCD(
          label = "Transcripts and judgments",
          categoryID = "transcriptsOfJudgements",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> transcriptsOfJudgements;
  @CCD(
          label = "Magistrates facts and reasons",
          categoryID = "magistratesFactsAndReasons",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> magistratesFactsAndReasons;
  @CCD(
          label = "Judge notes from hearing",
          categoryID = "judgeNotesFromHearing",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> judgeNotesFromHearing;
  @CCD(
          label = "Preliminary Documents",
          categoryID = "preliminaryDocuments",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> preliminaryDocuments;
  @CCD(
          label = "Position statements",
          categoryID = "positionStatements",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> positionStatements;
  @CCD(
          label = "FM5 statement on NCDR",
          categoryID = "fm5Statements",
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> fm5Statements;
  @CCD(
          label = "Applications",
          categoryID = "applications",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> applications;
  @CCD(
          label = "Applicant documents",
          categoryID = "applicantDocuments",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> applicantDocuments;
  @CCD(
          label = "Applicant application",
          categoryID = "applicantApplication",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> applicantApplication;
  @CCD(
          label = "Applicant C1A application",
          categoryID = "applicantC1AApplication",
          access = {DefaultAccess.class, LASOCIALWORKERLASOLICITORRAccess.class, CaseworkerPrivatelawCafcassCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> applicantC1AApplication;
  @CCD(
          label = "Applicant C1A response",
          categoryID = "applicantC1AResponse",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> applicantC1AResponse;
  @CCD(
          label = "Applications within proceedings",
          categoryID = "applicationsWithinProceedings",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> applicationsWithinProceedings;
  @CCD(label = "Applications within proceedings", categoryID = "applicationsWithinProceedingsRes")
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> applicationsWithinProceedingsRes;
  @CCD(
          label = "MIAM certificate",
          categoryID = "MIAMCertificate",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> MIAMCertificate;
  @CCD(
          label = "Orders from other proceedings",
          categoryID = "previousOrdersSubmittedWithApplication",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> prevOrdersSubmittedWithAppl;
  @CCD(
          label = "Respondent documents",
          categoryID = "respondentDocuments",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> respondentDocuments;
  @CCD(
          label = "Respondent application",
          categoryID = "respondentApplication",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> respondentApplication;
  @CCD(
          label = "Orders from other proceedings",
          categoryID = "ordersFromOtherProceedings",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> ordersFromOtherProceedings;
  @CCD(
          label = "Respondent C1A application",
          categoryID = "respondentC1AApplication",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> respondentC1AApplication;
  @CCD(
          label = "Respondent C1A response",
          categoryID = "respondentC1AResponse",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> respondentC1AResponse;
  @CCD(
          label = "Applications from other proceedings",
          categoryID = "applicationsFromOtherProceedings",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> applicationsFromOtherProceedings;
  @CCD(
          label = "Witness statement and evidence",
          categoryID = "witnessStatementAndEvidence",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> witnessStatementAndEvidence;
  @CCD(
          label = "Applicant's statements",
          categoryID = "applicantStatements",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> applicantStatements;
  @CCD(
          label = "Respondent's statements",
          categoryID = "respondentStatements",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> respondentStatements;
  @CCD(
          label = "Other witness statements",
          categoryID = "otherWitnessStatements",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> otherWitnessStatements;
  @CCD(
          label = "Pathfinder",
          categoryID = "pathfinder",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> pathfinder;
  @CCD(
          label = "Cafcass/Cafcass Cymru report and Guardian",
          categoryID = "CAFCASSReportAndGuardian",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> CAFCASSReportAndGuardian;
  @CCD(
          label = "Child Impact Report 1",
          categoryID = "childImpactReport1",
          access = {LASOCIALWORKERRPlus3RolesNmemgtAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> childImpactReport1;
  @CCD(
          label = "Child Impact Report 2",
          categoryID = "childImpactReport2",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> childImpactReport2;
  @CCD(
          label = "Safeguarding letter",
          categoryID = "safeguardingLetter",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> safeguardingLetter;
  @CCD(
          label = "Section 7 report",
          categoryID = "section7Report",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> section7Report;
  @JsonProperty("16aRiskAssessment")
  @CCD(
          label = "16a risk assessment",
          categoryID = "16aRiskAssessment",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> _16aRiskAssessment;
  @CCD(
          label = "CIR Transfer Request",
          categoryID = "cirTransferRequest",
          access = {CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess.class, CaseworkerPrivatelawCafcassCtscTeamLeaderCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class, CaseworkerPrivatelawExternaluserViewonlyRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> cirTransferRequest;
  @CCD(
          label = "CIR Extension Request",
          categoryID = "cirExtensionRequest",
          access = {CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess.class, CaseworkerPrivatelawCafcassCtscTeamLeaderCruAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class, CaseworkerPrivatelawExternaluserViewonlyRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> cirExtensionRequest;
  @CCD(
          label = "Guardian report",
          categoryID = "guardianReport",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> guardianReport;
  @CCD(
          label = "Special guardianship report",
          categoryID = "specialGuardianshipReport",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> specialGuardianshipReport;
  @CCD(
          label = "Other documents",
          categoryID = "otherDocs",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> otherDocs;
  @CCD(
          label = "Local Authority Documents",
          categoryID = "localAuthorityDocuments",
          access = {DefaultAccess.class, CaseworkerPrivatelawCafcassCtscTeamLeaderCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> localAuthorityDocuments;
  @CCD(
          label = "Section 37 report",
          categoryID = "section37Report",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> section37Report;
  @CCD(
          label = "Child Impact Report 1",
          categoryID = "childImpactReport1La",
          access = {LASOCIALWORKERLASOLICITORCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> childImpactReport1La;
  @CCD(
          label = "Child Impact Report 2",
          categoryID = "childImpactReport2La",
          access = {LASOCIALWORKERLASOLICITORCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> childImpactReport2La;
  @CCD(
          label = "Local Authority Section 37 report",
          categoryID = "sec37Report",
          access = {DefaultAccess.class, LASOCIALWORKERLASOLICITORCruAccess.class, CaseworkerPrivatelawCafcassCtscTeamLeaderCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> sec37Report;
  @CCD(label = "Section 7 report", categoryID = "section7ReportLa", access = {LASOCIALWORKERLASOLICITORCruAccess.class})
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> section7ReportLa;
  @CCD(
          label = "Section 7 addendum report",
          categoryID = "section7AddendumReportLa",
          access = {LASOCIALWORKERLASOLICITORCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> section7AddendumReportLa;
  @CCD(
          label = "Local Authority involvement letter",
          categoryID = "localAuthorityInvolvementLa",
          access = {LASOCIALWORKERLASOLICITORCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> localAuthorityInvolvementLa;
  @CCD(label = "Section 47 enquiry", categoryID = "section47La", access = {LASOCIALWORKERLASOLICITORCruAccess.class})
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> section47La;
  @CCD(
          label = "CIR extension request",
          categoryID = "cirExtensionRequestLa",
          access = {LASOCIALWORKERLASOLICITORCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> cirExtensionRequestLa;
  @CCD(
          label = "CIR transfer request",
          categoryID = "cirTransferRequestLa",
          access = {LASOCIALWORKERLASOLICITORCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> cirTransferRequestLa;
  @CCD(
          label = "Local Authority other documents",
          categoryID = "localAuthorityOtherDoc",
          access = {DefaultAccess.class, LASOCIALWORKERLASOLICITORCruAccess.class, CaseworkerPrivatelawCafcassCtscTeamLeaderCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> localAuthorityOtherDoc;
  @CCD(
          label = "Expert report",
          categoryID = "expertReport",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> expertReport;
  @CCD(
          label = "Medical reports",
          categoryID = "medicalReports",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> medicalReports;
  @CCD(
          label = "DNA reports",
          categoryID = "DNAReports_expertReport",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> DNAReports_expertReport;
  @CCD(
          label = "Results of hair strand/blood tests",
          categoryID = "resultsOfHairStrandBloodTests",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> resultsOfHairStrandBloodTests;
  @CCD(
          label = "Police disclosures",
          categoryID = "policeDisclosures",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> policeDisclosures;
  @CCD(
          label = "Medical records",
          categoryID = "medicalRecords",
          access = {DefaultAccess.class, CaseworkerPrivatelawCafcassCtscTeamLeaderCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> medicalRecords;
  @JsonProperty("drugAndAlcoholTest(toxicology)")
  @CCD(
          label = "Drug and alcohol test (toxicology)",
          categoryID = "drugAndAlcoholTest(toxicology)",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> drugAndAlcoholTest_toxicology_;
  @CCD(
          label = "Police report",
          categoryID = "policeReport",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> policeReport;
  @CCD(
          label = "Correspondence to and from court",
          categoryID = "correspondentToAndFromCourt",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> correspondentToAndFromCourt;
  @CCD(
          label = "Emails to request hearings adjourned",
          categoryID = "emailsToCourtToRequestHearingsAdjourned",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> emailsToCourt;
  @CCD(
          label = "Public funding certificates",
          categoryID = "publicFundingCertificates",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> publicFundingCertificates;
  @CCD(
          label = "Notices of acting/discharge",
          categoryID = "noticesOfActingDischarge",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> noticesOfActingDischarge;
  @CCD(
          label = "Request for FAS forms to be changed",
          categoryID = "requestForFASFormsToBeChanged",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> requestForFASFormsToChange;
  @CCD(
          label = "Witness availability",
          categoryID = "witnessAvailability",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> witnessAvailability;
  @CCD(
          label = "Letters of complaint",
          categoryID = "lettersOfComplaint",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> lettersOfComplaint;
  @CCD(
          label = "SPIP referral requests",
          categoryID = "SPIPReferralRequests",
          access = {DefaultAccess.class, CaseworkerPrivatelawCafcassCtscTeamLeaderCruAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> SPIPReferralRequests;
  @CCD(
          label = "Home Office/ DWP responses",
          categoryID = "homeOfficeDWPResponses",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> homeOfficeDWPResponses;
  @CCD(
          label = "Internal correspondence",
          categoryID = "internalCorrespondence",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> internalCorrespondence;
  @CCD(
          label = "Other documents",
          categoryID = "otherDocments",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> otherDocments;
  @CCD(
          label = "Imp info about address and contact",
          categoryID = "importantInfoAboutAddressAndContact",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> impInfoAboutAddrContact;
  @CCD(
          label = "Privacy notice",
          categoryID = "privacyNotice",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> privacyNotice;
  @CCD(
          label = "Reasonable adjustments and special measures",
          categoryID = "specialMeasures",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> specialMeasures;
  @CCD(
          label = "Attending the Hearing",
          categoryID = "attendingTheHearing",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> attendingTheHearing;
  @CCD(
          label = "Notice of hearing",
          categoryID = "noticeOfHearing",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> noticeOfHearing;
  @CCD(
          label = "Court bundle",
          categoryID = "courtBundle",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> courtBundle;
  @CCD(
          label = "Case summary",
          categoryID = "caseSummary",
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> caseSummary;
  @CCD(
          label = "Confidential",
          categoryID = "confidential",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> confidential;
  @CCD(
          label = "Any other documents",
          categoryID = "anyOtherDoc",
          access = {DefaultAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> anyOtherDoc;
  @CCD(
          label = "Draft Orders",
          categoryID = "draftOrders",
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, LASOCIALWORKERRPlus3RolesNmemgtAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> draftOrders;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, LASOCIALWORKERLASOLICITORRAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String caseLinksTabTitle;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String maintainCaseLinksFlag;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String caseLinksFlag;
  @JsonProperty("LinkedCasesComponentLauncher")
  @CCD(
          label = "Component Launcher (for displaying Linked Cases data)",
          typeOverride = FieldType.ComponentLauncher,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String linkedCasesComponentLauncher;
  @CCD(
          label = "## If any applicants aren't displayed, you will need to add them to the application using 'Applicants details' section.",
          typeOverride = FieldType.Label,
          access = {APPLICANTSOLICITORCREATORCruAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class, CaseworkerPrivatelawCourtadminCitizenCruAccess.class, CaseworkerPrivatelawJudgeCrudAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
  )
  private String childAndApplicantRelationsLabel;
  @CCD(
          label = "Do not use 'Add new' button to include applicants.",
          typeOverride = FieldType.Label,
          access = {APPLICANTSOLICITORCREATORCruAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class, CaseworkerPrivatelawCourtadminCitizenCruAccess.class, CaseworkerPrivatelawJudgeCrudAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
  )
  private String childAndApplicantRelationsSubLabel;
  @CCD(
          label = "## If any other parties aren't displayed, you will need to add them to the application using 'Other people in the case' section.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, CaseworkerPrivatelawSuperuserCitizenCruAccess.class}
  )
  private String childAndOtherPeopleRelationsLabel;
  @CCD(
          label = "Do not use 'Add new' button to include additional parties.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, CaseworkerPrivatelawSuperuserCitizenCruAccess.class}
  )
  private String childAndOtherPeopleRelationsSubLabel;
  @CCD(
          label = "## If any respondents aren't displayed, you will need to add them to the application using 'Respondent details' section.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, CaseworkerPrivatelawSuperuserCitizenCruAccess.class}
  )
  private String childAndRespondentRelationsLabel;
  @CCD(
          label = "Do not use 'Add new' button to include respondents.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, CaseworkerPrivatelawSuperuserCitizenCruAccess.class}
  )
  private String childAndRespondentRelationsSubLabel;
  @CCD(
          label = "Record which children on case have final order or other resolution.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawExternaluserViewonlyCourtnavRAccess.class}
  )
  private String recordChildrenLabel;
  @CCD(
          label = "If all the children have final orders or resolutions, you can close the case.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawExternaluserViewonlyCourtnavRAccess.class}
  )
  private String closeCaseLabel;
  @CCD(
          label = "## Add decision details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess.class}
  )
  private String addDecisionLabel;
  @CCD(
          label = "### Select final outcome for:",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess.class}
  )
  private String finalOutComeLabel;
  @CCD(
          label = "Application pack approved after confidential check?",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
  )
  private String isC8CheckApproved;
  @CCD(
          label = "<div class='govuk-warning-text'><span class='govuk-warning-text__icon' aria-hidden='true'>!</span><strong class='govuk-warning-text__text'>You need to check the confidential details tab and review the service packs in the service of application tab before continuing.</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
  )
  private String confidentialityCheckWarningText;
  @CCD(
          label = "Respondent 1 English c8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respAC8EngDocument;
  @CCD(
          label = "Respondent 1 Welsh c8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respAC8WelDocument;
  @CCD(
          label = "Respondent 2 English c8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respBC8EngDocument;
  @CCD(
          label = "Respondent 2 Welsh c8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respBC8WelDocument;
  @CCD(
          label = "Respondent 3 English c8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respCC8EngDocument;
  @CCD(
          label = "Respondent 3 Welsh c8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respCC8WelDocument;
  @CCD(
          label = "Respondent 4 English c8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respDC8EngDocument;
  @CCD(
          label = "Respondent 4 Welsh c8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respDC8WelDocument;
  @CCD(
          label = "Respondent 5 English c8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respEC8EngDocument;
  @CCD(
          label = "Respondent 5 Welsh c8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respEC8WelDocument;
  @CCD(
          label = "Applicant 1 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document appAC8RefugeDocument;
  @CCD(
          label = "Applicant 2 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document appBC8RefugeDocument;
  @CCD(
          label = "Applicant 3 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document appCC8RefugeDocument;
  @CCD(
          label = "Applicant 4 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document appDC8RefugeDocument;
  @CCD(
          label = "Applicant 5 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document appEC8RefugeDocument;
  @CCD(
          label = "Respondent 1 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respAC8RefugeDocument;
  @CCD(
          label = "Respondent 2 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respBC8RefugeDocument;
  @CCD(
          label = "Respondent 3 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respCC8RefugeDocument;
  @CCD(
          label = "Respondent 4 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respDC8RefugeDocument;
  @CCD(
          label = "Respondent 5 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document respEC8RefugeDocument;
  @CCD(
          label = "Other Person 1 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document otherAC8RefugeDocument;
  @CCD(
          label = "Other Person 2 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document otherBC8RefugeDocument;
  @CCD(
          label = "Other Person 3 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document otherCC8RefugeDocument;
  @CCD(
          label = "Other Person 4 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document otherDC8RefugeDocument;
  @CCD(
          label = "Other Person 5 Refuge C8 Document",
          categoryID = "confidential",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document otherEC8RefugeDocument;
  @CCD(
          label = "<div class='govuk-warning-text'><span class='govuk-warning-text__icon' aria-hidden='true'>!</span><strong class='govuk-warning-text__text'>You must have served any relevant orders before making this transfer as you will lose access to this case.</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class}
  )
  private String transferCourtWarning;
  @CCD(
          label = "## ${courtName} is currently chosen",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
  )
  private String transferCourtChangeWarning;
  @CCD(label = "## Court details", searchable = false, typeOverride = FieldType.Label, access = {DefaultAccess.class})
  private String courtDetailsLabel;
  @CCD(
          label = "Fl401 document1",
          categoryID = "applicantApplication",
          searchable = false,
          access = {CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document fl401Doc1;
  @CCD(
          label = "Fl401 document2",
          categoryID = "applicantApplication",
          searchable = false,
          access = {CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document fl401Doc2;
  @CCD(
          label = "swanseaDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "SwanseaDFJCourts",
          access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
  )
  private SwanseaDFJCourts swanseaDFJCourt;
  @CCD(
          label = "humbersideDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "HumbersideDFJCourts",
          access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
  )
  private HumbersideDFJCourts humbersideDFJCourt;
  @CCD(
          label = "essexAndSuffolkDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "EssexAndSuffolkDFJCourts",
          access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
  )
  private EssexAndSuffolkDFJCourts essexAndSuffolkDFJCourt;
  @CCD(
          label = "wolverhamptonDFJCourt :",
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "WolverhamptonDFJCourts",
          access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
  )
  private WolverhamptonDFJCourts wolverhamptonDFJCourt;
  @CCD(
          label = "**Note: Once you have deleted your application it cannot be resumed**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
  )
  private String deleteApplicationNote;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
  )
  private java.util.Set<DeletionConsent> deletionConsent;
  @CCD(
          label = "Child arrangements, Specific issue, Prohibited steps (C43)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, APPLICANTSOLICITORCruPlus14RolesQztdkkAccess.class, CaseworkerPrivatelawSuperuserRCaseworkerWaTaskConfigurationCruAccess.class, CaseworkerPrivatelawCafcassCitizenRAccess.class, CREATORCruAccess.class}
  )
  private String c43Label;
  @CCD(
          label = "## Child arrangements, Specific issue, Prohibited steps (C43)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPLICANTSOLICITORRPlus19RolesLrlmmbAccess.class, C100APPLICANTBARRISTER1RPlus4RolesTrhpdrAccess.class, CaseworkerPrivatelawSuperuserRCaseworkerWaTaskConfigurationCruAccess.class, CaseworkerPrivatelawCafcassCitizenRAccess.class, CREATORRAccess.class}
  )
  private String c43LabelBold;
  @CCD(
          label = "Draft collection id",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
  )
  private String draftOrderCollectionId;
  @CCD(
          label = "## CAFCASS/CAFCASS Cymru",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioCafcassOrCafcassCymruLabel;
  @CCD(
          label = "**CAFCASS safeguarding on issue**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioSafeGuardingOnIssueLabel;
  @CCD(
          label = "**CAFCASS Cymru safeguarding on issue**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioSafeGuardingOnIssueCymruLabel;
  @CCD(
          label = "Select the directions the case needs",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioDirectionsCaseNeedsLabel;
  @CCD(
          label = "Further directions",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private java.util.Set<DioFurtherInstructionsEnum> dioFurtherList;
  @CCD(
          label = "## Directions on issue",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioListLabel;
  @CCD(
          label = "## Court",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioCourtLabel;
  @CCD(
          label = "**Transfer application to another family court**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioTransferApplicationLabel;
  @CCD(
          label = "## Hearings and next steps",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioHearingsAndNextStepsLabel;
  @CCD(
          label = "**Case review at second gatekeeping appointment (PD36Y)**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioCaseReviewLabel;
  @CCD(
          label = "**Allocation decision**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioAllocationDecisionLabel;
  @CCD(
          label = "**Allocate or reserve to a named judge**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioAllocateNamedJudgeLabel;
  @CCD(
          label = "**Permission hearing for direction 91 (14)**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioHearingPermissionLabel;
  @CCD(
          label = "**Urgent first hearing**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioUrgentFirstHearingLabel;
  @CCD(
          label = "An urgent hearing will take place at ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioUrgentFirstHearingPlaceLabel;
  @CCD(
          label = "The hearing is urgent because",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioHearingUrgentBecauseLabel;
  @CCD(
          label = "Time for service of the application is shortened to",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioHearingUrgentTimeShortenedLabel;
  @CCD(
          label = "**The urgent hearing is refused**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioUrgentHearingRefusedLabel;
  @CCD(
          label = "The court does not consider the test is met to hear the application urgently. In reaching this decision the court has had regard to the factors set out in the observations below:",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioUrgentHearingRefusedCourtLabel;
  @CCD(
          label = "**Without notice first hearing**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioWithoutNoticeFirstHearingLabel;
  @CCD(
          label = "Application for without notice approved due to the following observations:",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioWithoutNoticeApprovedApplicationLabel;
  @CCD(
          label = "**Without notice hearing refused**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioWithoutNoticeHearingRefusedLabel;
  @CCD(
          label = "Application for without notice is not approved due to the following observations:",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioWithoutNoticeNotApprovedLabel;
  @CCD(
          label = "**First hearing dispute resolution (FHDRA)**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioFhdraLabel;
  @CCD(
          label = "A first hearing dispute resolution will occur at",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioFhdraHearingDisputeLabel;
  @CCD(
          label = "**Participation directions**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioParticipationDirectionsLabel;
  @CCD(
          label = "**Position statement**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioPositionStatementLabel;
  @CCD(
          label = "**Mediation Information and Assessment Meeting (MIAM)**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioAttendanceAtMiamLabel;
  @CCD(
          label = "**Court to arrange interpreters**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioCourtToInterpretersLabel;
  @CCD(
          label = "**Update your contact details**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioUpdateContactDetailsLabel;
  @CCD(
          label = "Name of judge",
          searchable = false,
          typeOverride = FieldType.JudicialUser,
          access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
  )
  private String dioNextStepJudgeName;
  @CCD(
          label = "Allocate or reserve to a named judge",
          searchable = false,
          access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
  )
  private AllocateOrReserveJudgeEnum dioAllocateOrReserveJudge;
  @CCD(
          label = "Name of judge",
          searchable = false,
          typeOverride = FieldType.JudicialUser,
          access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
  )
  private String dioAllocateOrReserveJudgeName;
  @CCD(
          label = "**Magistrates**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioAllocationMagistrateLabel;
  @CCD(
          label = "**District judge**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioAllocationDistJudgeLabel;
  @CCD(
          label = "**Circuit judge**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioAllocationCircuitJudgeLabel;
  @CCD(
          label = "**Allocated to**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioAllocatedToJudgeLabel;
  @CCD(
          label = "**Reserved to**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioReservedToJudgeLabel;
  @CCD(
          label = "**Another reason that has not been listed**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioFirstHearingUrgencyDetailLabel;
  @CCD(
          label = "**Urgent hearing**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioUrgentHearingLabel;
  @CCD(
          label = "**Another reason that has not been listed**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioUrgentHearingRefusedAnotherReasonLabel;
  @CCD(
          label = "**Another reason that has not been listed**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioWithoutNoticeAnotherReasonLabel;
  @CCD(
          label = "**Another reason that has not been listed**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioWithoutNoticeHearingRefusedReasonLabel;
  @CCD(
          label = "**Other direction for position statement**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioPositionStatementOtherCheckDetailsLabel;
  @CCD(
          label = "Name of the person to attend MIAM",
          searchable = false,
          access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<MiamAttendingPersonName>> dioMiamAttendingPersonName;
  @CCD(
          label = "**Other direction for MIAM attendance**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioMiamOtherDetailsLabel;
  @CCD(
          label = "**Other direction for the court to arrange interpreters**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioInterpreterOtherDetailsLabel;
  @CCD(
          label = "**Other direction for Local Authority letter**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioLocalAuthorityDetailsLabel;
  @CCD(
          label = "**Other direction for transfer application to another family court**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioTransferCourtDetailsLabel;
  @CCD(
          label = "## Local Authority",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioLocalAuthorityLabel;
  @CCD(
          label = "**Local Authority letter**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioLocalAuthorityLetterLabel;
  @CCD(
          label = "## Other",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioOtherLabel;
  @CCD(
          label = "**Disclosure of papers from previous proceedings**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioDisclosureOfPapersLabel;
  @CCD(
          label = "**Parent with care can apply to transfer**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioParentWithCareLabel;
  @CCD(
          label = "**Application to apply for permission to instruct an expert require permission**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioApplicationToApplyPermissionLabel;
  @CCD(
          label = "**Other direction for disclosure of papers from previous proceedings**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String dioDisclosureOtherDetailsLabel;
  @CCD(
          label = "## Preambles",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioPreamblesLabel;
  @CCD(
          label = "**Right to ask court to reconsider this order**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioRightToAskCourtLabel;
  @CCD(
          label = "**Party/parties raised domestic abuse issues**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String dioPartiesRaisedAbuseLabel;
  @CCD(
          label = "## Cafcass or Cafcass Cymru",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
  )
  private String sdoCafcassOrCafcassCymruLabel;
  @CCD(
          label = "**Safeguarding checks: next steps Cafcass**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoSafeGuardingNextStepsLabel;
  @CCD(
          label = "**Safeguarding checks: next steps Cafcass Cymru**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoSafeGuardingNextStepsCymruLabel;
  @CCD(
          label = "**Party to provide details of new partner to Cafcass**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoSafeGuardingNewPartnerLabel;
  @CCD(
          label = "**Party to provide details of new partner to Cafcass Cymru**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoSafeGuardingNewPartnerCymruLabel;
  @CCD(
          label = "**Section 7 report/ Child impact analysis**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoSectionReportOrChildImpactLabel;
  @CCD(
          label = "For interim orders before determination of facts",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String sdoSection7FactsLabel;
  @CCD(
          label = "In all cases where domestic abuse has been found to have occurred",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String sdoSection7daOccuredLabel;
  @CCD(
          label = "**Other direction for party to provide details of new partner to Cafcass**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoPartyToProvideDetailsLabel;
  @CCD(
          label = "**Other direction for Party to provide details of new partner to Cafcass Cymru**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoNewPartnersToCafcassLabel;
  @CCD(
          label = "**Other direction for Section 7 report/ Child impact analysis**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String sdoSection7CheckLabel;
  @CCD(
          label = "Select the directions the case needs",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String sdoDirectionsCaseNeedsLabel;
  @CCD(
          label = "## Court",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
  )
  private String sdoCourtLabel;
  @CCD(
          label = "**Transfer application to another family court**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
  )
  private String sdoTransferApplicationLabel;
  @CCD(
          label = "**Cross-examination prohibition applies**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoCrossExaminationProhibitionLabel;
  @CCD(
          label = "Sitting before a",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private SdoCrossExaminationSittingBeforeEnum sdoCrossExaminationSittingBeforeOptions;
  @CCD(
          label = "Select from the list of courts",
          searchable = false,
          typeOverride = FieldType.DynamicList,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private String sdoCrossExaminationCourtDynamicList;
  @CCD(
          label = "Start date and time",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private java.time.LocalDateTime sdoCrossExaminationStartDateTime;
  @CCD(
          label = "**Cross-examination prohibition: EX740**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoCrossExaminationEx740Label;
  @CCD(
          label = "**Cross-examination prohibition:Qualified legal representative to be appointed**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoCrossExaminationQualifiedLegalLabel;
  @CCD(
          label = "**Other direction for transfer application to another family court**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoTransferCourtDetailsLabel;
  @CCD(
          label = "**Other direction for Cross-examination prohibition applies**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoCrossExaminationCourtCheckLabel;
  @CCD(
          label = "**Cross-examination prohibition: EX741**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoCrossExaminationEx741Label;
  @CCD(
          label = "## Documentation and evidence",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
  )
  private String sdoDocumentationAndEvidenceLabel;
  @CCD(
          label = "**Witness statements**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
  )
  private String sdoWitnessStatementsLabel;
  @CCD(
          label = "Copies of statements sent to",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private java.util.Set<SdoWitnessStatementsSentToEnum> sdoWitnessStatementsCopiesSentToCafcass;
  @CCD(
          label = "**Only specified documents to be filed**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoSpecifiedDocumentsLabel;
  @CCD(
          label = "**Instruction on filing bundles**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoInstructionsFilingLabel;
  @CCD(
          label = "**Planning Together for Children/Working Together for Children (WT4C)**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoSpipAttendanceLabel;
  @CCD(
          label = "**Medical disclosure**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoMedicalDisclosureLabel;
  @CCD(
          label = "**Letter from GP**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoLetterFromGpLabel;
  @CCD(
          label = "**Letter from school**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoLetterFromSchoolLabel;
  @CCD(
          label = "**Example schedule of allegations and responses for fact-finding**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoScheduleOfAllegationsLabel;
  @CCD(
          label = "**Other direction for Witness statements**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoWitnessStatementsCheckLabel;
  @CCD(
          label = "**Other direction for Instructions on filing bundles**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoInstructionsDetailsLabel;
  @CCD(
          label = "**Applicant**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoApplicantNameLabel;
  @CCD(
          label = "**Respondent**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoRespondentNameLabel;
  @CCD(
          label = "**Other direction for medical disclosure**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
  )
  private String sdoMedicalDiscDetailsLabel;
  @CCD(
          label = "**Applicant**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoGPApplicantNameLabel;
  @CCD(
          label = "**Respondent**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoGPRespondentNameLabel;
  @CCD(
          label = "**Other direction for letter from GP**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoLetterFromDiscGpLabel;
  @CCD(
          label = "**Applicant**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoLSApplicantNameLabel;
  @CCD(
          label = "**Respondent**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoLSRespondentNameLabel;
  @CCD(
          label = "**Other direction for letter from school**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoLetterFromSchoolCheckLabel;
  @CCD(
          label = "**Other direction for example schedule of allegations and responses for fact-finding**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoScheduleOfAllegationsCheckLabel;
  @CCD(
          label = "**Other direction for disclosure of papers from previous proceedings**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoDisClosureProceedingLabel;
  @CCD(
          label = "## Hearings and next steps",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoHearingsAndNextStepsLabel;
  @CCD(
          label = "**Allocation decision**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoAllocationDecisionLabel;
  @CCD(
          label = "**Urgent hearing**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoUrgentHearingLabel;
  @CCD(
          label = "An urgent hearing will take place at ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String sdoUrgentHearingPlaceLabel;
  @CCD(
          label = "**First hearing dispute resolution appointment (FHDRA)**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoFhdraLabel;
  @CCD(
          label = "**Position statement**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoPositionStatementLabel;
  @CCD(
          label = "**Mediation Information and Assessment Meeting (MIAM) attendance**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoMiamAttendanceLabel;
  @CCD(
          label = "**Permission hearing for direction 91(14)**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoPermissionHearingLabel;
  @CCD(
          label = "**Directions for dispute resolution appointment (DRA)**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoDirectionsForDraLabel;
  @CCD(
          label = "**Settlement conference**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoSettlementConferenceLabel;
  @CCD(
          label = "**Joining instructions for remote hearing**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String sdoJoiningInstructionsLabel;
  @CCD(
          label = "**Directions for fact finding hearing**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoDirectionsForFactFindingHearingLabel;
  @CCD(
          label = "**Court to arrange interpreters**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoArrangeInterpretersLabel;
  @CCD(
          label = "**Update your contact details**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoUpdateContactDetailsLabel;
  @CCD(
          label = "**Case review at second gatekeeping appointment (PD36Y)**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoNextStepsAfterSecondGKLabel;
  @CCD(
          label = "**Hearing is not needed**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoHearingNotNeededLabel;
  @CCD(
          label = "**Participation directions**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoParticipationDirectionsLabel;
  @CCD(
          label = "****Joining instructions for remote hearing****",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoJoiningInstructionsForRHLabel;
  @CCD(
          label = "Hearing date",
          hint = "Please enter date and time",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private java.time.LocalDateTime sdoUrgentHearingDate;
  @CCD(
          label = "Time estimate",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private String sdoUrgentHearingTimeEstimate;
  @CCD(
          label = "Select from the list of courts",
          searchable = false,
          typeOverride = FieldType.DynamicList,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private String sdoUrgentHearingCourtDynamicList;
  @CCD(
          label = "The hearing is urgent because",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String sdoHearingUrgentBecauseLabel;
  @CCD(
          label = "Give details",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private String sdoHearingUrgentDetails;
  @CCD(
          label = "This will be a remote hearing by way of",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private SdoRemoteHearingEnum sdoHearingUrgentByWayOf;
  @CCD(
          label = "A first hearing dispute resolution will occur at",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String sdoFhdraHearingDisputeLabel;
  @CCD(
          label = "Start date and time",
          hint = "Please enter date and time",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private java.time.LocalDateTime sdoFhdraStartDateTime;
  @CCD(
          label = "Select from the list of courts",
          searchable = false,
          typeOverride = FieldType.DynamicList,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private String sdoFhdraCourtDynamicList;
  @CCD(
          label = "This will be before a",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private SdoBeforeAEnum sdoFhdraBeforeAList;
  @CCD(
          label = "This will be a remote hearing by way",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private SdoRemoteHearingEnum sdoFhdraByWayOf;
  @CCD(
          label = "List for hearing on",
          hint = "Please enter date and time",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private java.time.LocalDateTime sdoPermissionHearingOn;
  @CCD(
          label = "Time estimate",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private String sdoPermissionHearingTimeEstimate;
  @CCD(
          label = "This will be before a",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private SdoBeforeAEnum sdoPermissionHearingBeforeAList;
  @CCD(
          label = "Matter to be listed for a hearing decided by ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private SdoJudgeLaDecideByEnum sdoDirectionsDraDecideBy;
  @CCD(
          label = "Start date and time",
          hint = "Please enter date and time",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private java.time.LocalDateTime sdoDirectionsDraStartDateAndTime;
  @CCD(
          label = "Duration of hearing ",
          hint = "Hours",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private String sdoDirectionsDraHearing;
  @CCD(
          label = "Select from the list of courts",
          searchable = false,
          typeOverride = FieldType.DynamicList,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private String sdoDirectionsDraCourtDynamicList;
  @CCD(
          label = "This will be a remote hearing by way of",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private SdoRemoteHearingEnum sdoDirectionsDraHearingByWayOf;
  @CCD(
          label = "Matter to be listed for a hearing has been decided by a",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private SdoJudgeLaDecideByEnum sdoSettlementConferenceList;
  @CCD(
          label = "Start date and time",
          hint = "Use 24 hour format",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private java.time.LocalDateTime sdoSettlementConferenceDateTime;
  @CCD(
          label = "Time estimate",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private String sdoSettlementConferenceTimeEstimate;
  @CCD(
          label = "Select from the list of courts",
          searchable = false,
          typeOverride = FieldType.DynamicList,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private String sdoSettlementConferenceCourtDynamicList;
  @CCD(
          label = "This will be a remote hearing by way of CVP/Teams /BT meet me telephone",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private SdoRemoteHearingEnum sdoSettlementConferenceByWayOf;
  @CCD(
          label = "Report also sent to",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private java.util.Set<SdoReportsSentToEnum> sdoHearingReportsSentTo;
  @CCD(
          label = "Name of the person who requires an interpreter",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class}
  )
  private String sdoPersonNeedsInterpreter;
  @CCD(
          label = "**District judge**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoAllocationDistJudgeLabel;
  @CCD(
          label = "**Circuit judge**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoAllocationCircuitJudgeLabel;
  @CCD(
          label = "**Magistrates**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoAllocationMagistratesLabel;
  @CCD(
          label = "**Reserved to**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private String sdoReservedToLabel;
  @CCD(
          label = "**Allocated to**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private String sdoAllocatedToLabel;
  @CCD(
          label = "**Other direction for position statement**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoPositionStatementOtherCheckDetailsLabel;
  @CCD(
          label = "**Other direction for MIAM attendance**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private String sdoMiamOtherDetailsLabel;
  @CCD(
          label = "**Other direction for the 'directions for fact-finding' hearing**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoFactFindingOtherCheckLabel;
  @CCD(
          label = "**Other direction for the court to arrange interpreters**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoInterpreterOtherDetailsLabel;
  @CCD(
          label = "**Other direction for safeguarding next steps Cafcass**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoCafcassFileAndServeDetailsLabel;
  @CCD(
          label = "**Other direction for safeguarding next steps Cafcass Cymru**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String safeguardingCafcassCymruDetailsLabel;
  @CCD(
          label = "**Allocate or reserve to a named judge**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoAllocateOrReserveJudgeLabel;
  @CCD(
          label = "**Next steps after second gatekeeping appointment**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoNextStepsAfterGatekeepingLabel;
  @CCD(
          label = "If a fact-finding hearing is needed, include details to go in the order.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
  )
  private String sdoDirectionsForFactFindingHearingLabel2;
  @CCD(
          label = "## Local Authority",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoLocalAuthorityLabel;
  @CCD(
          label = "**Local Authority letter**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoLocalAuthorityLetterLabel;
  @CCD(
          label = "**Other direction for Local Authority letter**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String sdoLocalAuthorityDetailsLabel;
  @CCD(
          label = "## Other",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavCruAccess.class}
  )
  private String sdoOtherLabel;
  @CCD(
          label = "**Disclosure of papers from previous proceedings**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoDisclosureOfPapersLabel;
  @CCD(
          label = "**Parent with care can apply to transfer**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoParentWithCareLabel;
  @CCD(
          label = "**Further Directions**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoAdditionalDetailsLabel;
  @CCD(
          label = "## Standard directions order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class}
  )
  private String sdoSdoLabel;
  @CCD(
          label = "## Standard directions order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoSdoListLabel;
  @CCD(
          label = "## Preambles",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoPreamblesLabel;
  @CCD(
          label = "**Right to ask court to reconsider this order**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoRightToAskCourtLabel;
  @CCD(
          label = "**Party or parties raised domestic abuse issues**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoPartiesRaisedAbuseLabel;
  @CCD(
          label = "**After second gatekeeping appointment**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
  )
  private String sdoAfterSecondGatekeepingLabel;
  @CCD(
          label = "**Add a new preamble**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class}
  )
  private String sdoAddNewPreambleLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoFactFindingFlag;
  @CCD(
          label = "${sdoFactFindingFlag}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
  )
  private String sdoFactFindingHintText;
  @CCD(
          label = "Preview the draft order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, APPLICANTSOLICITORRPlus19RolesLrlmmbAccess.class, C100APPLICANTBARRISTER1RPlus4RolesTrhpdrAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class, CREATORRAccess.class}
  )
  private String previewDraftOrderLabel;
  @CCD(
          label = " ",
          categoryID = "draftOrders",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document previewDraftOrder;
  @CCD(
          label = " ",
          categoryID = "draftOrders",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document previewDraftOrderWelsh;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRCaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String legalRepInstructionsPlaceHolder;
  @CCD(
          label = "${legalRepInstructionsPlaceHolder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateRAccess.class, CaseworkerPrivatelawSolicitorRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
  )
  private String instructionsToLegalRepresentativeLabel;
  @CCD(
          label = "### Your instructions to the legal representative",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateRAccess.class, CaseworkerPrivatelawSolicitorRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
  )
  private String yourInstructionsToLrLabel;
  @CCD(
          label = "Once this order is complete, can the application be served?",
          searchable = false,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class, CourtnavRAccess.class, HearingCentreTeamLeaderCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isOrderCompleteToServe;
  @CCD(
          label = "Directions to admin ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCitizenCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
  )
  private String instructionsFromJudge;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class}
  )
  private String manageOrderOptionType;
  @CCD(
          label = "Download the order ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String downloadOrderLabel;
  @CCD(
          label = "## Check the order ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateRAccess.class, CitizenRAccess.class}
  )
  private String checkTheOrderLabel;
  @CCD(
          label = "### Open the order and review the content",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateRAccess.class, CitizenRAccess.class}
  )
  private String openOrderAndReviewContentLabel;
  @CCD(
          label = "Directions to admin ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCourtnavCruAccess.class}
  )
  private String uploadOrAmendDirectionsFromJudge;
  @CCD(
          label = "Upload amended order ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String uploadAmendedOrderLabel;
  @CCD(
          label = "### Amended, discharged or varied order (FL404B)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPLICANTSOLICITORRPlus19RolesLrlmmbAccess.class, C100APPLICANTBARRISTER1RPlus4RolesTrhpdrAccess.class, CREATORRCaseworkerPrivatelawSuperuserCruCitizenRAccess.class}
  )
  private String amendedOrderLabel;
  @CCD(
          label = "## Blank order (FL404B) ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPLICANTSOLICITORRPlus19RolesLrlmmbAccess.class, C100APPLICANTBARRISTER1RPlus4RolesTrhpdrAccess.class, CREATORRCaseworkerPrivatelawSuperuserCruCitizenRAccess.class}
  )
  private String blankOrderLabel;
  @CCD(
          label = "## Occupation Order (FL404)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPLICANTSOLICITORRPlus19RolesLrlmmbAccess.class, C100APPLICANTBARRISTER1RPlus4RolesTrhpdrAccess.class, CREATORRCaseworkerPrivatelawSuperuserCruCitizenRAccess.class}
  )
  private String fl404OccupationLabel;
  @CCD(
          label = "## Non-molestation order (FL404A)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, APPLICANTSOLICITORRPlus19RolesLrlmmbAccess.class, C100APPLICANTBARRISTER1RPlus4RolesTrhpdrAccess.class, CREATORRCaseworkerPrivatelawSuperuserCruCitizenRAccess.class}
  )
  private String fl404nonMolestationLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class}
  )
  private String judgeNotesEmptyUploadJourney;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class}
  )
  private String judgeNotesEmptyDraftJourney;
  @CCD(
          label = "${editOrderTextInstructions}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, APPLICANTSOLICITORRPlus19RolesLrlmmbAccess.class, C100APPLICANTBARRISTER1RPlus4RolesTrhpdrAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String editOrderTextInstructionsLabel;
  @CCD(
          label = "Return to a previous state",
          searchable = false,
          access = {APPLICANTSOLICITORRPlus19RolesLrlmmbAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawCafcassRPlus3RolesWkowzeAccess.class, CaseworkerPrivatelawSuperuserCrudPlus3RolesCmdeveAccess.class}
  )
  private ExitAwaitingInformationDetailsType exitAwaitingInformationDetails;
  @CCD(
          label = "### Completing this form will generate a draft FL404B directions order",
          hint = "These reasons will be shared with the applicant",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeCrudPlus3RolesCdcrteAccess.class}
  )
  private String fl401ListOnNoticeDocumentHintLabel;
  @CCD(
          label = "### Completing this form will generate a draft FL404B directions order",
          hint = "These reasons will be shared with the applicant",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeCrudPlus3RolesCdcrteAccess.class}
  )
  private String fl401ListOnNoticeDocumentHintLabel1;
  @CCD(
          label = "## On Notice",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeCrudPlus3RolesCdcrteAccess.class}
  )
  private String fl401OnNoticeLabel;
  @CCD(
          label = "Provide reasons why the without notice application should be heard on notice to the respondent",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeCrudPlus3RolesCdcrteAccess.class}
  )
  private String fl401WithOutNoticeReasonToRespondentLabel;
  @CCD(
          label = "### These reasons will be shared with the applicant",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeCrudPlus3RolesCdcrteAccess.class}
  )
  private String withOutNoticeReasonToShareApplicantLabel;
  @CCD(
          label = "## Reject a list without notice hearing request",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
  )
  private String fl401RejectListWithoutNoticeHearingRequestLabel;
  @CCD(
          label = "Give reasons why the list without notice request is rejected.",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
  )
  private String fl401ReasonsForListWithoutNoticeRequested;
  @CCD(
          label = "## Next hearing details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
  )
  private String fl401ListOnNoticeHearingInstructionLabel;
  @CCD(
          label = "Give admin hearing instructions.",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawJudgeCrudPlus5RolesDpastvAccess.class, CaseworkerPrivatelawLaCrudAccess.class}
  )
  private String fl401listOnNoticeHearingInstruction;
  @CCD(
          label = "### Uploaded documents ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class}
  )
  private String fl401UploadDocumentsDetails;
  @CCD(
          label = "**Upload any relevant documents that support this application.**\n\nFiles should be:\n\n- a maximum of 100MB in size (larger files must be split)\n- labelled clearly, e.g. Draft_Order.pdf\n- in a word or PDF format",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class}
  )
  private String fl401UploadDocumentWitnessDuplicate;
  @CCD(
          label = "Upload document",
          categoryID = "applicantApplication",
          searchable = false,
          min = 1,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<uk.gov.hmcts.ccd.sdk.type.Document>> fl401UploadedDocuments;
  @CCD(
          label = "**Upload any relevant documents that support this application.**\n\nFiles should be:\n\n- a maximum of 100MB in size (larger files must be split)\n- labelled clearly, e.g. Draft_Order.pdf\n- in a word or PDF format",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus1RolesUypxmrAccess.class}
  )
  private String fl401UploadDocumentWitness;
  @CCD(
          label = "### 1. Upload witness statement ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCruCaseworkerPrivatelawJudgeRAccess.class}
  )
  private String fl401UploadDocumentWitnessLabel;
  @CCD(
          label = "###  2. Upload any other supporting documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus1RolesUypxmrAccess.class}
  )
  private String fl401UploadDocumentSupport;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class}
  )
  private StatementOfTruth fl401StmtOfTruthResubmit;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class}
  )
  private ConfidentialityCheck fl401ConfidentialityCheckResubmit;
  @JsonProperty("SearchCriteria")
  @CCD(
          label = "Search Criteria",
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, GSProfileRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.SearchCriteria searchCriteria;
  @CCD(
          label = "case Management Category ",
          typeOverride = FieldType.DynamicList,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, GSProfileRAccess.class}
  )
  private String caseManagementCategory;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
  )
  private String hearingListed;
  @CCD(
          label = "### Before you start",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class}
  )
  private String beforeYouStart;
  @CCD(
          label = "Check the help with fees portal and update Paybubble",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class}
  )
  private String updatePayBubble;
  @CCD(
          label = "If the application needs to move forward without payment being made, you will need to get approval from a delivery manager or a senior manager.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class}
  )
  private String approveDeliveryManagerOrSeniorManager;
  @JsonProperty("Checkthehelpwithfeesapplication")
  @CCD(
          label = "### Check the help with fees application",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class}
  )
  private String checkthehelpwithfeesapplication;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class}
  )
  private String hwfApplicationDynamicData;
  @CCD(
          label = "${hwfApplicationDynamicData}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class}
  )
  private String applicationHelpwithfeesreferenceApplicantApplication;
  @CCD(
          label = "Provide reasons why the without notice application should be heard on notice",
          searchable = false,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeCrudPlus3RolesCdcrteAccess.class}
  )
  private java.util.Set<ListOnNoticeReasonsEnum> selectedReasonsForListOnNotice;
  @CCD(
          label = "Edit your message",
          hint = "You can include additional reasons which may not have been previously listed.",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeCrudPlus3RolesCdcrteAccess.class}
  )
  private String selectedAndAdditionalReasons;
  @CCD(
          label = "Hearing",
          searchable = false,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeCrudPlus3RolesCdcrteAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<HearingData>> listWithoutNoticeHearingDetails;
  @CCD(
          label = "## Next hearing details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawJudgeRAccess.class, CaseworkerWaTaskConfigurationRAccess.class}
  )
  private String listWithoutNoticeHearingInstructionLabel;
  @CCD(
          label = "## Further evidence - further application document",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesPgdngkAccess.class}
  )
  private String furtherEvidenceLabel;
  @CCD(
          label = "Documents",
          searchable = false,
          min = 1,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess.class, CaseworkerPrivatelawCafcassCaseworkerPrivatelawJudgeRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<FurtherEvidence>> mainApplicationDocument;
  @CCD(
          label = "## Correspondence",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesPgdngkAccess.class}
  )
  private String correspondenceLabel;
  @CCD(
          label = "## Other documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesPgdngkAccess.class}
  )
  private String otherDocumentsLabel;
  @CCD(
          label = "Documents",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCudPlus2RolesGtbivmAccess.class, CaseworkerPrivatelawExternaluserViewonlyCudAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<FurtherEvidence>> mainAppNotConf;
  @CCD(label = "Supporting documents", searchable = false, access = {CaseworkerPrivatelawSystemupdateCudAccess.class})
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Correspondence>> corrNotConf;
  @CCD(
          label = "Supporting documents",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCudPlus2RolesGtbivmAccess.class, CaseworkerPrivatelawExternaluserViewonlyCudAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<OtherDocuments>> otherDocNotConf;
  @CCD(
          label = "<div class='govuk-warning-text'><span class='govuk-warning-text__icon' aria-hidden='true'>!</span><strong class='govuk-warning-text__text'>You cannot delete or edit a document once it is submitted.</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess.class, CaseworkerPrivatelawJudgeCrudPlus3RolesIslhzwAccess.class}
  )
  private String manageDocumentsWarningText;
  @CCD(
          label = "<div class='govuk-warning-text'><span class='govuk-warning-text__icon' aria-hidden='true'>!</span><strong class='govuk-warning-text__text'>There is confidential information in this case.</strong><p></p><p>Before you add a document, remove any confidential details. You cannot delete or edit a document after you submit it.</p</div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus2RolesAmczvyAccess.class, CaseworkerPrivatelawJudgeCrudPlus3RolesIslhzwAccess.class}
  )
  private String manageDocumentsWarningText2;
  @CCD(
          label = "test",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawBulkscanCrudPlus5RolesYfhrueAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<String>> manageDocUploadedCategory;
  @CCD(
          label = "## Serve saved orders",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel21;
  @CCD(
          label = "### Upload additional documents (Optional)",
          hint = "Upload any additional documents that you plan to serve",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess.class, CaseworkerPrivatelawCourtadminCrudAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String serveOrderAdditionalDocumentsLabel;
  @CCD(
          label = "## Serve the order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel22;
  @CCD(
          label = "(you must serve the order)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruCourtnavRAccess.class}
  )
  private String courtAdminText;
  @CCD(
          label = "(you must arrange for them to serve the order)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruCourtnavRAccess.class}
  )
  private String courtBailiffText;
  @CCD(
          label = "## Serve the order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel23;
  @CCD(
          label = "## Serve the order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String manageOrderHeaderLabel24;
  @CCD(
          label = "${typeOfC21Order}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
  )
  private String typeOfC21OrderLabel;
  @CCD(
          label = "${typeOfC21Order}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
  )
  private String selectedOrderC21HearingLabel;
  @CCD(
          label = "${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserRCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String selectedOrder13;
  @CCD(
          label = "${selectedC21Order}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserRCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String selectedC21OrderLabel;
  @CCD(
          label = "${typeOfC21Order}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserRCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String selectedC21OrderLabel1;
  @CCD(
          label = "${typeOfC21Order}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserRCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String selectedC21OrderLabel2;
  @CCD(
          label = "${typeOfC21Order}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String selectedOrderC21SolicitorLabel;
  @CCD(
          label = "## Add order details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String addOrderDetailsLabel;
  @CCD(
          label = "## ${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String selectedOrder3;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel11;
  @CCD(
          label = "## Who has been given parental responsibility?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
  )
  private String parentalResponsibility;
  @CCD(
          label = "## ${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String selectedOrder7;
  @CCD(
          label = "## Name of Cafcass or Cafcass Cymru officer",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
  )
  private String caffcassOfficeName;
  @CCD(
          label = "## Name of Cafcass Cymru officer",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
  )
  private String caffcassCymruOfficeName;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel12;
  @CCD(
          label = "## Create an order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationCruAccess.class, CaseworkerPrivatelawJudgeCruAccess.class}
  )
  private String createAnOrderLabel;
  @CCD(
          label = "In a closed case you can still:\n* add a case note\n* upload a document\n* vacate a hearing",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
  )
  private String closeCaseDoableActions;
  @CCD(
          label = "Which hearing?",
          searchable = false,
          typeOverride = FieldType.FixedList,
          typeParameterOverride = "HearingTypeEnum",
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
  )
  private HearingTypeEnum hearingType;
  @CCD(
          label = "Order made by",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String orderMadeByLabel;
  @CCD(
          label = "**Judge or Magistrate's title**",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String judgeLabel;
  @CCD(
          label = "## Transfer case to another court",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
  )
  private String transferCase;
  @CCD(
          label = "Reason for transfer",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
  )
  private java.util.Set<TransferReasonEnum> reasonsForTransfer;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class}
  )
  private CaseTransferOptionsEnum caseTransferOptions;
  @CCD(
          label = "## Preview the order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String checkYourOrder;
  @CCD(
          label = "If you want to make further changes, go back to the previous screen.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String checkYourOrderLabel;
  @CCD(
          label = "Check if there are restrictions on who should receive the order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String checkOrderRestrictionsLabel;
  @CCD(
          label = "## Confirm recipients",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String orderRecipientsLabel;
  @CCD(
          label = "Which other people in the case should the order be sent to?",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private java.util.Set<OtherOrderRecipientsEnum> otherOrderRecipients;
  @CCD(
          label = " ",
          hint = "For example, DWP or local authority",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private java.util.Set<CafcassEnum> cafcassRecipient;
  @CCD(
          label = " ",
          hint = "For example, DWP or local authority",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private java.util.Set<OtherEnum> otherRecipient;
  @CCD(
          label = "${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String selectedOrderLabel;
  @CCD(
          label = "${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String selectedOrderHearingLabel;
  @CCD(
          label = "${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String selectedOrderSummaryLabel;
  @CCD(
          label = "${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String selectedOrderSolicitorLabel;
  @CCD(
          label = "${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String selectedOrder1;
  @CCD(
          label = "${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserRCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String selectedOrder5;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeader1;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel2;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel3;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel4;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel5;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel7;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel6;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel8;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel9;
  @CCD(
          label = "${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel10;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel13;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel14;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel19;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String amendOrderHeaderLabel1;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String amendOrderCheckOrderLabel;
  @CCD(
          label = "## ${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserRCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String selectedOrder10;
  @CCD(
          label = "Full name",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
  )
  private String guardianTextBox;
  @CCD(
          label = "${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String selectedOrderLabel1;
  @CCD(
          label = "### ${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String selectedOrderLabel12;
  @CCD(
          label = "## Who's included in the order?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String childDetailsManageOrderLabel;
  @CCD(
          label = "## Who is the appointed special guardian?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String appointedGuardianLabel;
  @CCD(
          label = "How long will the order be in force?",
          searchable = false,
          access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
  )
  private DateOrderEndsTimeEnum2 customOrderDateEndsOptions;
  @CCD(
          label = "Date order ends",
          hint = "Please enter date and time",
          searchable = false,
          access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
  )
  private java.time.LocalDateTime customOrderDateEnds;
  @CCD(
          label = "Costs of this application",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String applicationCost;
  @CCD(
          label = "Select order",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
  )
  private CreateSelectOrderOptionsEnum orderType;
  @CCD(
          label = "Order date made",
          searchable = false,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private java.time.LocalDate dateUploadOrderMade;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel20;
  @CCD(
          label = "## When do you want to serve the order?",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String whenToServeOrderLabel;
  @JsonProperty("fl404SchoolDirections&Details")
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SchoolDirectionsDetails>> fl404SchoolDirections_Details;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String orderName;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String orderNameLabel;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, APPLICANTSOLICITORRPlus19RolesLrlmmbAccess.class, C100APPLICANTBARRISTER1RPlus4RolesTrhpdrAccess.class, CREATORRCaseworkerPrivatelawSuperuserCruCitizenRAccess.class}
  )
  private String orderNameEditScreenLabel;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String orderNameLabel7;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String orderNameLabel8;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String orderNameLabel9;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String orderNameLabel10;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String orderNameLabel11;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String orderNameLabel13;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String orderNameSolicitorLabel;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String orderNameSummaryLabel;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String orderNameHearingLabel;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String orderNameDirectionsToAdminLabel;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String orderNameInstructionsFromJudgeLabel;
  @CCD(
          label = "Selected hearings dropdown value",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
  )
  private String selectedHearingType;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CourtnavCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isHearingPageNeeded;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String performingUser;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String performingAction;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String judgeLaReviewRequired;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String orderNameForWA;
  @CCD(
          label = "<div class=\"govuk-inset-text\"> If the order you need is not on the list, go back to the previous page to upload it.</div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
  )
  private String uploadHintLabel;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String orderNameForSolicitorCreatedOrder;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String orderNameForJudgeApproved;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String orderNameForAdminCreatedOrder;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String orderNameForJudgeCreatedOrder;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String isHearingTaskNeeded;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String hearingOptionSelected;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String isOrderApproved;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String whoApprovedTheOrder;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String isMultipleHearingSelected;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String judgeLaManagerReviewRequired;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavRAccess.class}
  )
  private String requestSafeGuardingLetterUpdate;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CourtnavRAccess.class}
  )
  private String safeGuardingLetterUploadDueDate;
  @CCD(label = " ")
  private FinalisationDetails finalisationDetails;
  @CCD(
          label = "${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CourtnavRAccess.class}
  )
  private String customOrderSelectedHearingLabel;
  @CCD(
          label = "Was the order approved at a hearing?",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CourtnavRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo customOrderWasApprovedAtHearing;
  @CCD(
          label = "At which hearing was the order approved?",
          searchable = false,
          typeOverride = FieldType.DynamicList,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CourtnavRAccess.class}
  )
  private String customOrderHearingsType;
  @CCD(
          label = "${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String selectedOrder4;
  @CCD(
          label = "### Creating a hearing is required",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesKaqxnzAccess.class}
  )
  private String creatingHearingRequiredLabel;
  @CCD(
          label = "you can only create one hearing",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesKaqxnzAccess.class}
  )
  private String createOneHearingLabel;
  @CCD(
          label = "### Creating a hearing is optional",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesKaqxnzAccess.class}
  )
  private String creatingHearingOptionalLabel;
  @CCD(
          label = "you can create multiple hearings",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesKaqxnzAccess.class}
  )
  private String createMultipleHearingLabel;
  @CCD(
          label = "Hearing",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class, CaseworkerPrivatelawSolicitorCrudAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<HearingData>> ordersHearingDetails;
  @CCD(
          label = "Hearing",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class, CaseworkerPrivatelawSolicitorCrudAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<HearingData>> solicitorOrdersHearingDetails;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class, CaseworkerPrivatelawSolicitorCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isOrderCreatedBySolicitor;
  @CCD(
          label = "## ${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
  )
  private String selectedOrderLabel19;
  @CCD(
          label = "### Creating a hearing is required \nYou can only create one hearing",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
  )
  private String createHearingRequiredLabel;
  @CCD(
          label = "### Creating a hearing is optional \nYou can create multiple hearings",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
  )
  private String createHearingOptionalLabel;
  @CCD(
          label = "### Creating a hearing is required \nYou can only create one hearing",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
  )
  private String solicitorCreateHearingRequiredLabel;
  @CCD(
          label = "### Creating a hearing is optional \nYou can create multiple hearings",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCourtnavCruAccess.class}
  )
  private String solicitorCreateHearingOptionalLabel;
  @CCD(
          label = "${manageOrderHeader1}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String manageOrderHeaderLabel15;
  @CCD(
          label = "${selectedOrder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String selectedOrder2;
  @CCD(
          label = "C21 Order Details",
          searchable = false,
          access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
  )
  private CustomC21OrderDetails customC21OrderDetails;
  @CCD(
          label = "C43 Order Details",
          searchable = false,
          access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCruAccess.class}
  )
  private CustomC43OrderDetails customC43OrderDetails;
  @CCD(
          label = "Full name",
          searchable = false,
          min = 1,
          access = {CaseworkerPrivatelawCourtadminCrudPlus2RolesTbxcefAccess.class, CaseworkerPrivatelawSolicitorCrudPlus3RolesWisbigAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<AppointedGuardianFullName>> customAppointedGuardianName;
  @CCD(
          label = "## Download the order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String amendOrderDownloadOrderLabel;
  @CCD(
          label = "Open the attached order in PDF-Xchange Editor to make changes.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String manageOrdersDocumentToAmendLabel;
  @CCD(
          label = "## Upload amended order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String amendOrderReplaceOrderLabel;
  @CCD(
          label = "Upload the amended order. It will then be dated and stamped as amended.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String manageOrdersAmendedOrderLabel;
  @CCD(
          label = "This will go to a manager to be checked",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String managerCheckAmendOrder;
  @CCD(
          label = "Once this order is complete, can the application be served?",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isOrderCompleteToServeAmendOrder;
  @CCD(
          label = "Select order name",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private CustomOrderNameOptionsEnum customOrderNameOption;
  @CCD(
          label = "## Preview of order header",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCafcassRPlus3RolesWkowzeAccess.class, CaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String customOrderHeaderPreviewLabel;
  @CCD(
          label = "Transformed custom order",
          categoryID = "anyOtherDoc",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document customOrderTransformedDoc;
  @CCD(
          label = "### Upload an order \n\n Select an order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String uploadAnOrder;
  @CCD(
          label = "Is the order by consent?",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCaseworkerWaTaskConfigurationCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isTheOrderUploadedByConsent;
  @CCD(
          label = "# Messages",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String messagesLabel;
  @CCD(
          label = "[Send and reply to messages](/cases/case-details/${[CASE_REFERENCE]}/trigger/sendOrReplyToMessages/sendOrReplyToMessages1)",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String sendOrReplyEventLink;
  @CCD(
          label = "## Open messages",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String openMessageLabel;
  @CCD(
          label = "## Closed messages",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String closedMessageLabel;
  @CCD(
          label = "## Mediation Information and Assessment Meeting (MIAM) exemption?",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRAccess.class}
  )
  private String miamPolicyUpgradeExemptionsLabel;
  @CCD(
          label = "## MIAM exemption",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus2RolesTimnnxAccess.class}
  )
  private String miamExemptionLabel1;
  @CCD(
          label = "## Add evidence",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus2RolesTimnnxAccess.class}
  )
  private String mpuAddEvidenceLabel;
  @CCD(
          label = "You can save and return to this page at any time. Questions marked with a * need to be completed before you can send your application.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus2RolesTimnnxAccess.class}
  )
  private String submissionRequiredFieldsInfo9;
  @CCD(
          label = "## MIAM exemption",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus2RolesTimnnxAccess.class}
  )
  private String miamExemptionLabel5;
  @CCD(
          label = "## MIAM exemption",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus2RolesTimnnxAccess.class}
  )
  private String miamExemptionLabel2;
  @CCD(
          label = "## MIAM exemption",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus2RolesTimnnxAccess.class}
  )
  private String miamExemptionLabel3;
  @CCD(
          label = "## MIAM exemption",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminRPlus2RolesTimnnxAccess.class}
  )
  private String miamExemptionLabel4;
  @CCD(
          label = " ",
          access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class, C100APPLICANTBARRISTER1RAccess.class, C100APPLICANTSOLICITOR1RAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy caApplicant1Policy;
  @CCD(
          label = " ",
          access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class, C100APPLICANTBARRISTER2RAccess.class, C100APPLICANTSOLICITOR2RAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy caApplicant2Policy;
  @CCD(
          label = " ",
          access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class, C100APPLICANTBARRISTER3RAccess.class, C100APPLICANTSOLICITOR3RAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy caApplicant3Policy;
  @CCD(
          label = " ",
          access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class, C100APPLICANTBARRISTER4RAccess.class, C100APPLICANTSOLICITOR4RAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy caApplicant4Policy;
  @CCD(
          label = " ",
          access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class, C100APPLICANTBARRISTER5RAccess.class, C100APPLICANTSOLICITOR5RAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy caApplicant5Policy;
  @CCD(
          label = " ",
          access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class, C100RESPONDENTBARRISTER1RAccess.class, C100RESPONDENTSOLICITOR1RAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy caRespondent1Policy;
  @CCD(
          label = " ",
          access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class, C100RESPONDENTSOLICITOR2RAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy caRespondent2Policy;
  @CCD(
          label = " ",
          access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class, C100RESPONDENTSOLICITOR3RAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy caRespondent3Policy;
  @CCD(
          label = " ",
          access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class, C100RESPONDENTSOLICITOR4RAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy caRespondent4Policy;
  @CCD(
          label = " ",
          access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class, C100RESPONDENTSOLICITOR5RAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy caRespondent5Policy;
  @CCD(
          label = " ",
          access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class, FL401APPLICANTSOLICITORRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy daApplicantPolicy;
  @CCD(
          label = " ",
          access = {DefaultAccess.class, CaseworkerApproverCaseworkerCaaCitizenCruAccess.class, FL401RESPONDENTSOLICITORRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.OrganisationPolicy daRespondentPolicy;
  @CCD(
          label = "# Parties",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRAccess.class}
  )
  private String partiesTabLabel;
  @CCD(
          label = "## Applicants",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String partiesTabApplicantsLabel;
  @CCD(
          label = "## Children",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String partiesTabChildrenLabel;
  @CCD(
          label = "## Respondents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String partiesTabRespondentsLabel;
  @CCD(
          label = "## Other parties",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String partiesTabOtherPartiesLabel;
  @CCD(
          label = "## Respondent relation to child",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminRPlus2RolesTimnnxAccess.class, CaseworkerPrivatelawLaCaseworkerPrivatelawSolicitorRAccess.class, CaseworkerPrivatelawJudgeRAccess.class}
  )
  private String partiesTabOtherChildAndRespondentPartiesLabel;
  @CCD(
          label = "## Other people in the case ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String partiesTabOtherPartiesRevisedLabel;
  @CCD(
          label = "## Other children not part of this application",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String partiesTabOtherChildNotInThePartiesLabel;
  @CCD(
          label = "## Applicant relation to child",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String partiesTabChildAndApplicantPartiesLabel;
  @CCD(
          label = "## Respondent relation to child",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawLaCaseworkerPrivatelawSolicitorRAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String partiesTabChildAndRespondentPartiesLabel;
  @CCD(
          label = "##  Other people in this application’s relation to child",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String partiesTabOtherChildAndOtherPeoplePartiesLabel;
  @JsonProperty("PaymentHistory")
  @CCD(
          label = "A history of payments associated with a the case",
          searchable = false,
          typeOverride = FieldType.CasePaymentHistoryViewer,
          access = {CaseworkerApproverRAccess.class, PaymentsCruAccess.class}
  )
  private String paymentHistory;
  @CCD(
          label = "${orderName}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String removeOrderNameLabel;
  @CCD(
          label = "<div class='panel panel-border-wide'><h2>Check you're removing the right order</h2><p>You will not be able to reinstate it after it’s removed</p></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSuperuserCruAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String checkTheRemoveOrderLabel;
  @CCD(label = "## Select documents to be renamed", searchable = false, typeOverride = FieldType.Label)
  private String renameSelectDocumentsLabel;
  @CCD(
          label = "<div class=\"govuk-inset-text\"> Current state: Closed</div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess.class, HearingCentreTeamLeaderCruAccess.class}
  )
  private String currentStatusLabel;
  @CCD(
          label = "The case can be progressed to other case states through case issued or hearing state if required",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess.class, HearingCentreTeamLeaderCruAccess.class}
  )
  private String otherStatusMsg;
  @CCD(
          label = "## Awaiting Information Details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {APPLICANTSOLICITORRPlus19RolesLrlmmbAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawCafcassRPlus3RolesWkowzeAccess.class, CaseworkerPrivatelawSuperuserCrudPlus3RolesCmdeveAccess.class}
  )
  private String requestFurtherInformationDetailsLabel;
  @CCD(
          label = "Awaiting Information Details",
          searchable = false,
          access = {APPLICANTSOLICITORRPlus19RolesLrlmmbAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawSuperuserCrudPlus3RolesCmdeveAccess.class}
  )
  private RequestFurtherInformationDetailsType requestFurtherInformationDetails;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<RequestOrderHearingTracking>> requestOrderTaskTrackingByHearing;
  @CCD(
          label = "Reason for pin reset",
          hint = "You will be resetting the pin - please provide a reason",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorRCaseworkerPrivatelawSuperuserCruAccess.class, CaseworkerWaTaskConfigurationCruAccess.class}
  )
  private String resetAccessCodeReason;
  @CCD(
          label = "If you fill out this section you do not need to send a separate C1A form.",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, C100RESPONDENTBARRISTER1CruPlus5RolesTjdifxAccess.class}
  )
  private String respAohHint;
  @CCD(
          label = "Are there allegations that the child(ren) or respondent(s) have experienced, or are at\nrisk of experiencing, harm from any of the following by any person who has had\ncontact with the child?",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, C100RESPONDENTBARRISTER1CruPlus5RolesTjdifxAccess.class}
  )
  private String respAohLabel;
  @CCD(
          label = "### Orders",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, C100RESPONDENTBARRISTER1CruPlus5RolesTjdifxAccess.class}
  )
  private String respAohOrdersLabel;
  @CCD(
          label = "Has the respondent had (or does the respondent currently have) any of the these\norders?",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, C100RESPONDENTBARRISTER1CruPlus5RolesTjdifxAccess.class}
  )
  private String respAohOrdersLabelDetail;
  @CCD(
          label = "## Domestic abuse - Behaviours",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respDomesticAbuseBehavioursLabel;
  @CCD(
          label = "## Child abuse - Behaviours",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respChildAbuseBehavioursLabel;
  @CCD(
          label = "## Child abduction",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respChildAbductionLabel;
  @CCD(
          label = "## Other concerns",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respOthersConcernsLabel;
  @CCD(
          label = "Give a short description of what happened and any relevant information so the court\ncan decide what needs to be done. ",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respChildAbuseBehavioursSubLabel;
  @CCD(
          label = "Give a short description of what happened and any relevant information so the court\ncan decide what needs to be done.",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respDomesticAbuseBehavioursSubLabel;
  @CCD(
          label = "### Child contact",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respAllegationsOfHarmChildContactLabel;
  @CCD(
          label = "## Child abuse - Physical abuse",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respChildPhysicalAbuseLabel;
  @CCD(
          label = "Give short description of what happened and any relevant information so the court \n can decide what needs to be done.",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respChildPhysicalAbuseSubLabel;
  @CCD(
          label = "## Child abuse - Psychological abuse",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respChildPsychologicalAbuseLabel;
  @CCD(
          label = "Give short description of what happened and any relevant information so the court \n can decide what needs to be done.",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respChildPsychologicalAbuseSubLabel;
  @CCD(
          label = "## Child abuse - Sexual abuse",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respChildSexualAbuseLabel;
  @CCD(
          label = "Give short description of what happened and any relevant information so the court \n can decide what needs to be done.",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respChildSexualAbuseSubLabel;
  @CCD(
          label = "## Child abuse - Emotional abuse",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respChildEmotionalAbuseLabel;
  @CCD(
          label = "Give short description of what happened and any relevant information so the court \n can decide what needs to be done.",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respChildEmotionalAbuseSubLabel;
  @CCD(
          label = "## Child abuse - Financial abuse",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respChildFinancialAbuseLabel;
  @CCD(
          label = "Give short description of what happened and any relevant information so the court \n can decide what needs to be done.",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CitizenCruAccess.class}
  )
  private String respChildFinancialAbuseSubLabel;
  @CCD(
          label = "*Are there allegations of harm?",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo respondentAohYesNo;
  @CCD(
          label = " ",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawCafcassRPlus2RolesIqgqdqAccess.class}
  )
  private RespondentAllegationsOfHarm respondentAllegationsOfHarm;
  @CCD(
          label = "### Domestic abuse ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String respondentDomesticAbuseLabel;
  @CCD(
          label = "Give a short description of what happened and any relevant information so the court can decide what needs to be done.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String respondentDomesticAbuseDescLabel;
  @CCD(
          label = "Behaviour",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Behaviours>> respondentDomesticAbuseBehaviour;
  @CCD(
          label = "### Child abuse",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String respondentChildAbuseLabel;
  @CCD(
          label = "Give a short description of what happened and any relevant information so the court can decide what needs to be done.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String respondentChildAbuseDescLabel;
  @CCD(
          label = "Behaviour",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<Behaviours>> respondentChildAbuseBehaviour;
  @CCD(
          label = "### Child abduction",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, APPLICANTSOLICITORCREATORCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String respondentChildAbductionLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, APPLICANTSOLICITORCREATORCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private RespondentChildAbduction respondentChildAbduction;
  @CCD(
          label = "### Other concerns",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, APPLICANTSOLICITORCREATORCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String respondentOtherConcernsLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, APPLICANTSOLICITORCREATORCitizenRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private RespondentOtherConcerns respondentOtherConcerns;
  @CCD(
          label = "### Domestic Abuse Act 2021",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String respAttendingTheCourtDaActLabel;
  @CCD(
          label = "Provisions in the Domestic Abuse Act 2021 have the effect of preventing an individual accused of abuse from questioning in person a party or witness in the case who is the victim of the abuse, and also prevents a victim of abuse from questioning in person the accused individual in specified circumstances.\n\nIf the court directs that the proceedings be listed for a hearing where oral evidence may be given, form EX740 (person making the abuse accusation) or form EX741 (person accused of abuse) ‘Application and information needed by the court to consider whether to prevent (prohibit) questioning (cross-examination) in person’ may need to be completed so that the court can consider whether questioning in person should be prevented. The court will send the appropriate form with the court order.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String domesticAbuseProvisionLabel;
  @CCD(
          label = "Respondent documents",
          searchable = false,
          access = {C100RESPONDENTBARRISTER1C100RESPONDENTSOLICITOR1CuAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private RespondentDocs respondentADocumentsList;
  @CCD(
          label = "Respondent documents",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR2CuAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private RespondentDocs respondentBDocumentsList;
  @CCD(
          label = "Respondent documents",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR3CuAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private RespondentDocs respondentCDocumentsList;
  @CCD(
          label = "Respondent documents",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR4CuAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private RespondentDocs respondentDDocumentsList;
  @CCD(
          label = "Respondent documents",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR5CuAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private RespondentDocs respondentEDocumentsList;
  @CCD(
          label = "### The court will keep respondent contact details private ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String keepDetailsPrivateSummaryHeading;
  @CCD(
          label = "You have told us you want to keep these contact details private: ${confidentialListDetails}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String keepDetailsPrivateSummary;
  @CCD(
          label = "###  What the court will do",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String courtActionHeading;
  @CCD(
          label = "The court will hold this information securely and will not share it with anyone except Cafcass (Children and Family Court Advisory and Support Service) or Cafcass Cymru unless it is by order of the court.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String courtAction;
  @CCD(
          label = "### The court will not keep your contact details private",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String keepDetailsPrivateNoHeading;
  @CCD(
          label = "You have told us you do not want to keep your contact details private from the other people in this application.\n",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String noNeedOfPrivateDetailsLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private Miam respondentSolicitorHaveYouAttendedMiam;
  @CCD(
          label = "${whatIsMiamPlaceHolder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String whatIsMiamLabel;
  @CCD(
          label = "${helpMiamCostsExemptionsPlaceHolder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String helpMiamCostsExemptionsLabel;
  @CCD(
          label = "You can find a copy of the allegations of harm with the application that was served on you.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class, APPLICANTSOLICITORRAccess.class, CREATORRAccess.class, CaseworkerPrivatelawLaRAccess.class, CitizenCruAccess.class}
  )
  private String responseToAllegationsOfHarmLabel;
  @CCD(
          label = "### Respondent name: ${respondentNameForResponse}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String respondentNameForResponseLabel;
  @CCD(
          label = "### Respondent name: ${respondentNameForResponse}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String respondentNameForResponseLabel1;
  @CCD(
          label = "### Respondent name: ${respondentNameForResponse}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String respondentNameForResponseLabel2;
  @CCD(
          label = "### Respondent name: ${respondentNameForResponse}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String respondentNameForResponseLabel3;
  @CCD(
          label = "### Respondent name: ${respondentNameForResponse}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String respondentNameForResponseLabel4;
  @CCD(
          label = "### Respondent name: ${respondentNameForResponse}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTBARRISTER1CruPlus9RolesUgiqlgAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String respondentNameForResponseLabel5;
  @CCD(
          label = "### Active respondent name: ${respondentNameForResponse}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus4RolesHmcngqAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String respondentNameForResponseLabel6;
  @CCD(
          label = "### Active respondent name: ${respondentNameForResponse}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus4RolesHmcngqAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String respondentNameForResponseLabel7;
  @CCD(
          label = "### Active respondent name: ${respondentNameForResponse}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus4RolesHmcngqAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String respondentNameForResponseLabel8;
  @CCD(
          label = "### Active respondent name: ${respondentNameForResponse}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus4RolesHmcngqAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String respondentNameForResponseLabel9;
  @CCD(
          label = "### Active respondent name: ${respondentNameForResponse}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus4RolesHmcngqAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String respondentNameForResponseLabel10;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class}
  )
  private ConfidentialityDisclaimerObject resSolConfidentialityDisclaimerSubmit;
  @CCD(
          label = "The respondent believes that the facts stated in this form and any continuation sheets are true. ${respondentSolicitorName}  is authorised by the respondent to sign this statement. ",
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String c100ResSolSubmitAndPayAgreeSignStmtLabel;
  @CCD(
          label = "<div class='govuk-box-highlight'><strong><h1>Response Submitted</h1></strong> \n #${[CASE_REFERENCE]}</div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String respondentSolSuccessLabel;
  @CCD(
          label = "Your response is now submitted.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String respondentSolResponseStateLabel;
  @CCD(
          label = "You can contact your local court at ${[courtEmailAddress]}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String respondentSolResponseCourtLabel;
  @CCD(
          label = "## Download a copy of your response",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String respondentSolResponseDownloadLabel;
  @CCD(
          label = "Case name",
          searchable = false,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String respondentTaskList;
  @CCD(
          label = "${respondentTaskList}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String respondentTaskListLabel;
  @CCD(
          label = "Case name",
          searchable = false,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, C100RESPONDENTBARRISTER1C100RESPONDENTSOLICITOR1CuAccess.class}
  )
  private String respondentTaskListA;
  @CCD(
          label = "${respondentTaskListA}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, C100RESPONDENTBARRISTER1C100RESPONDENTSOLICITOR1CuAccess.class, CitizenCuAccess.class}
  )
  private String respondentTaskListLabelA;
  @CCD(
          label = "Case name",
          searchable = false,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, C100RESPONDENTSOLICITOR2CuAccess.class}
  )
  private String respondentTaskListB;
  @CCD(
          label = "${respondentTaskListB}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, C100RESPONDENTSOLICITOR2CuAccess.class}
  )
  private String respondentTaskListLabelB;
  @CCD(
          label = "Case name",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR3CuAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String respondentTaskListC;
  @CCD(
          label = "${respondentTaskListC}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, C100RESPONDENTSOLICITOR3CuAccess.class}
  )
  private String respondentTaskListLabelC;
  @CCD(
          label = "Case name",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR4CuAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String respondentTaskListD;
  @CCD(
          label = "${respondentTaskListD}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, C100RESPONDENTSOLICITOR4CuAccess.class}
  )
  private String respondentTaskListLabelD;
  @CCD(
          label = "Case name",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR5CuAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private String respondentTaskListE;
  @CCD(
          label = "${respondentTaskListE}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, C100RESPONDENTSOLICITOR5CuAccess.class}
  )
  private String respondentTaskListLabelE;
  @CCD(
          label = "## View PDF response",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String viewPdfResponseLabel;
  @CCD(
          label = "### Download response ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyRAccess.class, CitizenCruAccess.class}
  )
  private String linkToDownloadC7Response;
  @CCD(
          label = "Use this link to download and check the response.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawReadonlyCitizenRAccess.class}
  )
  private String linkToDownloadC7ResponseLabel;
  @CCD(
          label = " ",
          categoryID = "respondentApplication",
          searchable = false,
          access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CaseworkerPrivatelawCafcassRPlus2RolesIqgqdqAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.Document draftC7WelshResponseDoc;
  @CCD(
          label = "## Before you start \n\nPrivate cases will appear in search results. \n\nThey can only be accessed by people who have been given the right permissions.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
  )
  private String markAsPrivateDisclaimer;
  @CCD(
          label = "### Enter the reasons to mark this case as private",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
  )
  private String markAsPrivateReasonLabel;
  @CCD(
          label = "## Private case",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class}
  )
  private String reasonsToPrivateTabLabel;
  @CCD(
          label = "Reasons to private this case",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class}
  )
  private String reasonsToPrivateTab;
  @CCD(
          label = "## Before you start \n\nPublic cases will appear in search results. \n\nBy making a case public, any previous access restrictions that applied to this case will be removed.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
  )
  private String markAsPublicDisclaimer;
  @CCD(
          label = "### Enter the reasons to make this case as public",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
  )
  private String markAsPublicReasonLabel;
  @CCD(
          label = "## Before you start \n\n Restricted cases will not appear in search results. \n\nThey can only be accessed by people who have been given the right permissions.",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
  )
  private String markAsRestrictedDisclaimer;
  @CCD(
          label = "### Enter the reasons to restrict this case",
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
  )
  private String markAsRestrictedReasonLabel;
  @CCD(
          label = "## Restricted case",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class}
  )
  private String reasonsToRestrictTabLabel;
  @CCD(
          label = "Reasons to restrict this case",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class}
  )
  private String reasonsToRestrictTab;
  @CCD(label = " ", searchable = false, access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class})
  private String assignedUserDetailsText;
  @CCD(
          label = "${assignedUserDetailsText}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class}
  )
  private String assignedUserDetailsLabel;
  @CCD(
          label = "<div class='govuk-box-highlight'><strong><h1>Press save and continue to proceed to return case to Hearing state </h1></strong></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawLaRAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
  )
  private String returnToPreviousStateLabel;
  @CCD(
          label = "### Review documents for sensitive or confidential information",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess.class, CaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String reviewDocsLabel1;
  @CCD(
          label = "Once you have selected a document to review, you will be asked on the next \n page if it needs to be restricted.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess.class, CaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String reviewDocsLabel2;
  @CCD(
          label = "### Review documents for sensitive or confidential information",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess.class, CaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String reviewDocsLabel3;
  @CCD(
          label = "Check for sensitive or confidential information. The document will be visible to all parties if you do not restrict access.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess.class, CaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String reviewDocsLabel4;
  @CCD(
          label = "${docToBeReviewed}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess.class, CaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String docToBeReviewedLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess.class}
  )
  private String quarantineInformation;
  @CCD(
          label = "${quarantineInformation}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminRPlus3RolesPlsldnAccess.class, CaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String quarantineInformationLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess.class}
  )
  private String docLabel;
  @CCD(
          label = "${docLabel}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess.class}
  )
  private String showLabel;
  @CCD(
          label = "### Let the gatekeepers know there’s a new case",
          hint = "This will send the case to the gatekeepers for your court or area, or to a specific gatekeeper.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String letGateKeepersKnowLabel;
  @CCD(
          label = "This will send the case to the gatekeepers for your court or area, or to a specific gatekeeper.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String sendToGateKeeperHint;
  @CCD(
          label = "Do you want to send this case to a specific gatekeeper?",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isSpecificGateKeeperNeeded;
  @CCD(
          label = "Judge or legal adviser?",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private SendToGatekeeperTypeEnum isJudgeOrLegalAdviserGatekeeping;
  @CCD(
          label = "Name of the judge",
          searchable = false,
          typeOverride = FieldType.JudicialUser,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String judgeName;
  @CCD(
          label = "Name of the legal adviser",
          searchable = false,
          typeOverride = FieldType.DynamicList,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String legalAdvisorList;
  @CCD(
          label = "### Select orders",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private String selectOrdersLabel1;
  @CCD(
          label = "${sentDocumentPlaceHolder}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
  )
  private String sentDocumentsLabel;
  @CCD(
          label = "## Select and upload orders and documents to be served",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
  )
  private String serveTheseOrdersLabel;
  @CCD(
          label = "Some documents are automatically sent out by the system to the people in the case. You do not need to upload them.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
  )
  private String automaticDocumentsLabel;
  @CCD(
          label = "### Upload PD36ZE letter",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
  )
  private String pd36qLetterLabel;
  @CCD(
          label = "### Upload special arrangements letter",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
  )
  private String specialArrangementsLetterLabel;
  @CCD(
          label = "### Upload  additional documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
  )
  private String additionalDocumentsLabel;
  @CCD(
          label = " ",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorRCourtnavCruAccess.class}
  )
  private String serviceOfApplicationHeader;
  @CCD(
          label = "${serviceOfApplicationHeader}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSolicitorRAccess.class}
  )
  private String headerLabel;
  @CCD(
          label = "## Serve the order",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private String soaHeaderLabel22;
  @CCD(
          label = "Does Cafcass need to be served?",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo soaCafcassServedOptions;
  @CCD(
          label = "Is the orders list empty?",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo soaIsOrderListEmpty;
  @CCD(
          label = "<div class='govuk-warning-text'><span class='govuk-warning-text__icon' aria-hidden='true'>!</span><strong class='govuk-warning-text__text'>There are confidential details on the case.</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String confidentialDetailsArePresentBanner;
  @CCD(
          label = "### Choose the documents to be served on the local authority",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String addDocumentsForLaLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private String missingAddressWarningText;
  @CCD(
          label = "${missingAddressWarningText}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private String missingAddressWarningTextCA;
  @CCD(
          label = "${missingAddressWarningText}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateRAccess.class}
  )
  private String missingAddressWarningTextLabel;
  @CCD(
          label = "${missingAddressWarningText}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private String missingAddressWarningTextDA;
  @CCD(
          label = "Confidential check needed?",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String isC8CheckNeeded;
  @CCD(
          label = "Responsible for serving respondent",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String responsibleForService;
  @CCD(
          label = "Is occupation order selected?",
          searchable = false,
          typeOverride = FieldType.TextArea,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String isOccupationOrderSelected;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
  )
  private String productHearingBundleOn;
  @CCD(label = "# Service of application", searchable = false, typeOverride = FieldType.Label)
  private String serviceOfApplicationLabel;
  @CCD(label = "## Unserved pack", searchable = false, typeOverride = FieldType.Label)
  private String unServedPackLabel;
  @CCD(label = "## Served pack", searchable = false, typeOverride = FieldType.Label)
  private String servedPackLabel;
  @CCD(label = "## Confidential check failed", searchable = false, typeOverride = FieldType.Label)
  private String confidentialCheckFailedLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
  )
  private ConfirmRecipients confirmRecipients;
  @CCD(
          label = "Check if there are restrictions on who should receive the order.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess.class}
  )
  private String soaCheckRestrictionsLabel;
  @CCD(
          label = "Confirm recipients",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess.class}
  )
  private String soaConfirmRecipientsLabel;
  @CCD(
          label = "If the applicant or respondent are represented by a solicitor, then the order is sent to the solicitor directly.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess.class}
  )
  private String soaOrderSentToSolicitorLabel;
  @CCD(
          label = "Respondent(s)",
          searchable = false,
          typeOverride = FieldType.DynamicMultiSelectList,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess.class}
  )
  private String soaRespondentsList;
  @CCD(
          label = "Which other people in the case should receive the order? (optional)",
          searchable = false,
          typeOverride = FieldType.DynamicMultiSelectList,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess.class}
  )
  private String soaOtherPeopleList;
  @CCD(
          label = " ",
          hint = "Add the email address of the Cafcass support officer.",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess.class}
  )
  private java.util.Set<CafcassServiceApplicationEnum> soaCafcassEmailOptionChecked;
  @CCD(
          label = " ",
          hint = "For example, add the email address of a local authority representative.",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess.class}
  )
  private java.util.Set<OtherEnum> soaOtherEmailOptionChecked;
  @CCD(
          label = "Email address",
          hint = "Add the email address of the Cafcass support officer.",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<String>> soaCafcassEmailAddressList;
  @CCD(
          label = "Email address",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<String>> soaOtherEmailAddressList;
  @CCD(label = "## Select documents to be served", searchable = false, typeOverride = FieldType.Label)
  private String sodSelectDocumentsLabel;
  @CCD(label = "## Upload additional documents to be served", searchable = false, typeOverride = FieldType.Label)
  private String sodAdditionalDocumentsLabel;
  @CCD(
          label = "<div class='govuk-warning-text'><span class='govuk-warning-text__icon' aria-hidden='true'>!</span><strong class='govuk-warning-text__text'>You need to check the confidential details tab and review the unserved documents in the service of documents tab before continuing.</strong></div>",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
  )
  private String serviceOfDocumentsConfCheckWarningText;
  @CCD(
          label = "# Service of documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class}
  )
  private String serviceOfDocumentsLabel;
  @CCD(
          label = "## Unserved documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class}
  )
  private String unServedDocumentsLabel;
  @CCD(
          label = "## Served documents",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class}
  )
  private String servedDocumentsLabel;
  @JsonProperty("ServiceRequest")
  @CCD(
          label = "Service Request",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class, CaseworkerApproverRAccess.class, CaseworkerPrivatelawSolicitorCuAccess.class, CaseworkerPrivatelawSuperuserCuAccess.class, CaseworkerWaTaskConfigurationCuAccess.class, PuiCaseManagerCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.WaysToPay serviceRequest2;
  @CCD(
          label = "### If you're no longer representing a client \n\n- You will no longer have access to this case\n- If the case had been shared with any colleagues, they will also loose access\n-Linked cases are not affected. To remove a legal representative from a \nlinked case, go to that case and repeat this action",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String solStopRepWarningText;
  @CCD(
          label = "## Select party to stop representing",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private String solStopRepHeader;
  @CCD(label = "By continuing:", searchable = false, access = {APPLICANTSOLICITORRPlus31RolesVrdqykAccess.class})
  private java.util.Set<SolicitorStopRepresentingDisclaimerEnum> solStopRepDisclaimer;
  @CCD(
          label = "Add recipient ",
          hint = "Do this if there are multiple recipients",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCrudCitizenRAccess.class, CaseworkerPrivatelawSuperuserCourtnavRAccess.class}
  )
  private String stmtOfServiceAddRecipientLabel;
  @CCD(
          label = "## Statement of service ",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerPrivatelawSolicitorCudAccess.class}
  )
  private String stmtOfServiceLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerPrivatelawJudgeRAccess.class}
  )
  private ConfidentialityDisclaimerObject confidentialityDisclaimerSubmit;
  @CCD(
          label = "# Summary",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private String summaryLabel;
  @CCD(
          label = "## Allocated judge",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private String allocatedJudgeLabel;
  @CCD(
          label = "## Pathfinder case",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class}
  )
  private String pathfinderLabel;
  @CCD(
          label = "Allocated judge",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private AllocatedJudge allocatedJudgeDetails;
  @CCD(
          label = "## Refuge case",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class, CaseworkerPrivatelawSolicitorCuAccess.class}
  )
  private String refugeLabel;
  @CCD(
          label = "Anyone living in a refuge?",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class, CaseworkerPrivatelawSolicitorCuAccess.class}
  )
  private RefugeCase refugeCase;
  @CCD(
          label = "## Case Status",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private String caseStatusLabel;
  @CCD(
          label = "Case Status",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class, CitizenCuAccess.class}
  )
  private CaseStatus caseStatus;
  @CCD(
          label = "## Confidential details",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private String confidentialityDetailsLabel;
  @CCD(
          label = "Confidential details",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private ConfidentialDetails confidentialDetails;
  @CCD(
          label = "## Urgency",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private String urgencyLabel;
  @CCD(
          label = "Urgency",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private Urgency urgencyDetails;
  @CCD(
          label = "## Type of application",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private String applicationTypeLabel;
  @CCD(
          label = "Type of application",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private ApplicationTypeDetails applicationTypeDetails;
  @CCD(
          label = "## Allegations of harm",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private String allegationOfHarmLabel;
  @CCD(
          label = "## Special arrangements",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private String specialArrangmentLabel;
  @CCD(
          label = "Special arrangements",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private SpecialArrangements specialArrangement;
  @CCD(
          label = "## Order applied for",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private String orderAppliedForLabel;
  @CCD(
          label = "Order applied for",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private OrderAppliedFor summaryTabForOrderAppliedFor;
  @CCD(
          label = "## Other proceedings",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private String otherProceedingsLabelForSummaryTab;
  @CCD(
          label = "Other proceeding",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<SummaryTabOtherProceedings>> otherProceedingsForSummaryTab;
  @CCD(
          label = "## Date submitted to HMCTS",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerApproverRAccess.class}
  )
  private String dateOfSubmissionLabel;
  @CCD(
          label = "Date submitted to HMCTS",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private DateSubmittedToHMCTS dateOfSubmission;
  @CCD(
          label = "Other proceedings",
          searchable = false,
          access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class}
  )
  private OtherProceedingEmptyTable otherProceedingEmptyTable;
  @CCD(
          label = "Close the case",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess.class}
  )
  private CaseClosedDate caseClosedDate;
  @CCD(
          label = "## Local Authority",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerApproverRAccess.class}
  )
  private String localAuthorityLabel;
  @CCD(
          label = " ",
          searchable = false,
          access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class}
  )
  private java.util.List<uk.gov.hmcts.ccd.sdk.type.ListValue<UpdateHearingActualTaskTracking>> updateHearingActualTracking;
  @CCD(
          label = "AWP Task to be created?",
          searchable = false,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
  )
  private String awpWaTaskToBeCreated;
  @CCD(
          label = "Additional Application id",
          searchable = false,
          retainHiddenValue = true,
          access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawSuperuserRAccess.class, CourtnavCruAccess.class}
  )
  private String additionalApplicationsBundleId;
  @CCD(
          label = "Launch the Reasonable adjustment support screen",
          searchable = false,
          typeOverride = FieldType.FlagLauncher,
          access = {APPLICANTSOLICITORCruPlus30RolesEzpiauAccess.class}
  )
  private String flagLauncherExternal;
  @CCD(
          label = "Launch the case flags screen",
          searchable = false,
          typeOverride = FieldType.FlagLauncher,
          access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, CaseworkerPrivatelawExternaluserViewonlyRAccess.class}
  )
  private String flagLauncherInternal;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, CaseworkerPrivatelawExternaluserViewonlyRAccess.class}
  )
  private AddCaseNoteType selectedReviewLangAndSmReq;
  @CCD(
          label = "Have you reviewed the support request above? You can't progress without reviewing",
          searchable = false,
          access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, CaseworkerPrivatelawExternaluserViewonlyRAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo isReviewLangAndSmReqReviewed;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private YesNoDontKnow otherPeopleKnowYourContactDetails;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo confidentiality;
  @CCD(
          label = " ",
          searchable = false,
          access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
  )
  private java.util.Set<ConfidentialityListEnum> confidentialityList;
  @CCD(
          label = "### Sending messages",
          hint = "You can send internal messages to HMCTS staff including the judiciary.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class}
  )
  private String sendingMessagesLabel;
  @CCD(
          label = "The message you enter here will be sent in notification to the external parties you've selected. This will be presented as a letter that they can access online in their account. and posted if they've chosen to receive physical letters\n\n You can also attach documents from the case that will be in the notification",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class}
  )
  private String externalMessagesHint;
  @CCD(
          label = "You can send internal messages to HMCTS staff including the judiciary or to external parties.",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class}
  )
  private String sendingMessagesHint;
  @CCD(
          label = "This is a hidden field",
          hint = "This is a hidden field",
          searchable = false,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class}
  )
  private uk.gov.hmcts.ccd.sdk.type.YesOrNo taskAssociatedWithMessage;
  @CCD(
          label = "${messageReplyTable}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class}
  )
  private String messageReplyTableLabel;
  @CCD(
          label = "${messageReplyTable}",
          searchable = false,
          typeOverride = FieldType.Label,
          access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class}
  )
  private String messageReplyTableLabel2;
}
