package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.hmcts.ccd.sdk.type.TTL;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.reopenclosedcases.ValidReopenClosedCasesStatusEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.complextypes.RemovableDocument;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.documents.ResponseDocuments;
import uk.gov.hmcts.reform.prl.models.complextypes.confidentiality.ApplicantConfidentialityDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.refuge.RefugeConfidentialDocuments;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.closingcases.ClosingCaseOptions;
import uk.gov.hmcts.reform.prl.models.dto.ccd.restrictedcaseaccessmanagement.CaseAccessStatusAndReason;
import uk.gov.hmcts.reform.prl.models.serviceofdocuments.ServiceOfDocuments;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCaseworkerPrivatelawReadonlyRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCitizenCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesYfkopjAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCuCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.LASOCIALWORKERRPlus3RolesNmemgtAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORCREATORCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCourtnavCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCudPlus2RolesMjidosAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawBulkscanCrudPlus1RolesIkmnbhAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverRPlus11RolesKytgmvAccess;
import uk.gov.hmcts.reform.prl.ccd.access.GSProfileRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaCaseworkerPrivatelawSolicitorRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverCaseworkerCaaRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess;
import uk.gov.hmcts.reform.prl.ccd.access.HearingCentreTeamLeaderCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORRPlus9RolesSpmfxdAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrdAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassRPlus1RolesAgacroAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess;
import uk.gov.hmcts.reform.prl.ccd.access.TTLProfileCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.LASOLICITORRPlus13RolesTxshhlAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerCaaCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCudCitizenCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus1RolesQuzmdnAccess;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
@SuperBuilder(toBuilder = true)
public class BaseCaseData {

    @CCD(
            label = " ",
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class, CaseworkerPrivatelawLaCaseworkerPrivatelawReadonlyRAccess.class, CaseworkerPrivatelawCourtadminCruAccess.class}
    )
    private long id;

    @CCD(
            label = "Case State",
            typeOverride = FieldType.Text,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCitizenCruAccess.class}
    )
    private State state;

    @CCD(label = " ", searchable = false, access = {CitizenCuAccess.class})
    private String taskListVersion;

    @CCD(
            label = "Created date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdDate;

    @CCD(
            label = "Last modified date",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime lastModifiedDate;

    @CCD(
            label = " ",
            typeOverride = FieldType.Date,
            access = {CaseworkerPrivatelawSolicitorCaseworkerPrivatelawSystemupdateCudAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class, CaseworkerPrivatelawCourtadminCitizenCudAccess.class}
    )
    private String dateSubmitted;

    @CCD(
            label = "Submitted date and time",
            typeOverride = FieldType.DateTime,
            access = {CaseworkerApproverRCaseworkerPrivatelawSystemupdateCuAccess.class, CaseworkerPrivatelawCourtadminCuPlus3RolesYfkopjAccess.class}
    )
    private String caseSubmittedTimeStamp;

    @CCD(
            label = "Court Seal",
            searchable = false,
            access = {CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    private String courtSeal;

    @CCD(
            label = "C1A Draft Document",
            categoryID = "applicantC1AApplication",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawSolicitorCuCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCuAccess.class, CaseworkerWaTaskConfigurationCuAccess.class}
    )
    @JsonProperty("c1ADraftDocument")
    private  Document c1ADraftDocument;
    @CCD(
            label = "C1A Draft Document (Welsh)",
            categoryID = "applicantC1AApplication",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawSolicitorCuCitizenCruAccess.class, CaseworkerPrivatelawCourtadminCuAccess.class, CaseworkerWaTaskConfigurationCuAccess.class}
    )
    @JsonProperty("c1AWelshDraftDocument")
    private  Document c1AWelshDraftDocument;

    @CCD(
            label = " C8 Archived Documents",
            categoryID = "c8ArchivedDocuments",
            access = {LASOCIALWORKERRPlus3RolesNmemgtAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess.class, CitizenCrudAccess.class}
    )
    @JsonProperty("c8ArchivedDocuments")
    private List<Element<Document>> c8ArchivedDocuments;

    @CCD(
            label = "test",
            searchable = false,
            typeOverride = FieldType.Collection,
            typeParameterOverride = "Text",
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class, CaseworkerPrivatelawCourtadminCrudCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSuperuserCrudAccess.class}
    )
    private List<Element<String>> cirDocumentsRequested;

    /**
     * Case Type Of Application.
     */
    @CCD(
            label = "Type of Case",
            access = {CaseworkerPrivatelawSolicitorCudPlus2RolesMjidosAccess.class, CourtnavCuAccess.class}
    )
    private String selectedCaseTypeID;
    /**
     * Case Type Of Application.
     */
    @CCD(
            label = " ",
            typeOverride = FieldType.FixedRadioList,
            typeParameterOverride = "CaseTypeOfApplicationEnum",
            access = {CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawBulkscanCrudPlus1RolesIkmnbhAccess.class, CaseworkerPrivatelawExternaluserViewonlyRAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("caseTypeOfApplication")
    private String caseTypeOfApplication;

    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCrudPlus1RolesOunurxAccess.class, CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess.class, CaseworkerPrivatelawBulkscanCrudPlus1RolesIkmnbhAccess.class, CaseworkerPrivatelawExternaluserViewonlyRAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("CaseAccessCategory")
    private String caseAccessCategory;
    /**
     * Case name.
     */
    @CCD(
            label = "Case Name",
            hint = "Enter the eldest child’s full name. For example, John Smith",
            access = {CaseworkerApproverRPlus11RolesKytgmvAccess.class}
    )
    @JsonAlias({"applicantCaseName", "applicantOrRespondentCaseName"})
    private String applicantCaseName;
    @CCD(
            label = "case Name Hmcts Internal",
            access = {CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, GSProfileRAccess.class, CaseworkerPrivatelawCourtadminRAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class, CitizenCuAccess.class, CourtnavCuAccess.class}
    )
    private String caseNameHmctsInternal;

    //FPET-567 - Added for hiding fields for SDO
    @CCD(
            label = "Is SDO or DIO Selected",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawJudgeCruAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("isSdoSelected")
    private YesOrNo isSdoSelected;

    @CCD(
            label = "Is this a Pathfinder case?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class}
    )
    @JsonProperty("isPathfinderCase")
    private YesOrNo isPathfinderCase;

    @JsonUnwrapped
    private DocumentsNotifications documentsNotifications;

    @CCD(
            label = " ",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRPlus1RolesSjmhdiAccess.class, CaseworkerPrivatelawLaCaseworkerPrivatelawSolicitorRAccess.class, CitizenCrudAccess.class}
    )
    private YesOrNo hwfRequestedForAdditionalApplicationsFlag;
    @CCD(
            label = "AwP task name will be derived from this field",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRAccess.class}
    )
    private String awpWaTaskName;
    @CCD(
            label = "AWP help with fees ref number",
            searchable = false,
            access = {CaseworkerPrivatelawLaRPlus3RolesHjqxmqAccess.class, CaseworkerPrivatelawSolicitorCruPlus2RolesHjbuaiAccess.class, CaseworkerPrivatelawCourtadminCaseworkerPrivatelawJudgeRAccess.class, CaseworkerPrivatelawExternaluserViewonlyRAccess.class}
    )
    private String awpHwfRefNo;

    /**
     * Process urgent help with fees.
     */
    @JsonUnwrapped
    private ProcessUrgentHelpWithFees processUrgentHelpWithFees;

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawSolicitorCitizenRAccess.class, CaseworkerPrivatelawSuperuserCruPlus2RolesVqbfmsAccess.class}
    )
    @JsonProperty("isApplicantRepresented")
    private String isApplicantRepresented;


    @CCD(
            label = "Refuge documents",
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerApproverRAccess.class, CitizenCudAccess.class}
    )
    @JsonProperty("refugeDocuments")
    private List<Element<RefugeConfidentialDocuments>> refugeDocuments;

    @CCD(
            label = "Historical refuge documents",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerApproverCaseworkerCaaRAccess.class, CaseworkerPrivatelawSystemupdateCitizenCrudAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    @JsonProperty("historicalRefugeDocuments")
    private List<Element<RefugeConfidentialDocuments>> historicalRefugeDocuments;

    @JsonUnwrapped
    private CaseAccessStatusAndReason caseAccessStatusAndReason;

    @JsonUnwrapped
    private ClosingCaseOptions closingCaseOptions;

    //PRL-6191 - Added for Record final decision
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess.class}
    )
    private String finalCaseClosedDate;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess.class}
    )
    private YesOrNo caseClosed;

    //PRL-6262 - Reopening closed cases
    @CCD(
            label = "Change status",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess.class, HearingCentreTeamLeaderCruAccess.class}
    )
    private ValidReopenClosedCasesStatusEnum changeStatusOptions;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesBmwiooAccess.class, CaseworkerPrivatelawCourtadminCruPlus3RolesBbdbyrAccess.class, HearingCentreTeamLeaderCruAccess.class}
    )
    private String reopenStateTo;

    @JsonUnwrapped
    private ServiceOfDocuments serviceOfDocuments;

    @CCD(
            label = "Next hearing date",
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminCaseworkerWaTaskConfigurationCruAccess.class}
    )
    @JsonProperty("nextHearingDate")
    private LocalDate nextHearingDate;

    @JsonUnwrapped
    private HearingTaskData hearingTaskData;

    @CCD(
            label = " ",
            searchable = false,
            access = {APPLICANTSOLICITORCREATORCruAccess.class, CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess.class, CaseworkerPrivatelawCourtadminCitizenCruAccess.class, CaseworkerPrivatelawSystemupdateCruAccess.class}
    )
    private String isNonWorkAllocationEnabledCourtSelected;

    @CCD(
            label = " ",
            searchable = false,
            access = {C100RESPONDENTSOLICITOR1CruPlus6RolesZsmmpaAccess.class, APPLICANTSOLICITORRPlus9RolesSpmfxdAccess.class, CitizenRAccess.class}
    )
    @JsonProperty("respondentSolicitorName")
    private String respondentSolicitorName;

    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, CaseworkerPrivatelawJudgeCruAccess.class, CourtnavCruAccess.class}
    )
    @JsonProperty("loggedInUserRole")
    private String loggedInUserRole;

    @CCD(
            label = "Applicant contact instructions",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class, CaseworkerPrivatelawSuperuserCuAccess.class, CitizenCudAccess.class, CourtnavCuAccess.class}
    )
    @JsonProperty("daApplicantContactInstructions")
    private String daApplicantContactInstructions;

    @CCD(
            label = "Documents that can be removed",
            searchable = false,
            access = {CaseworkerPrivatelawSuperuserCrdAccess.class}
    )
    private List<Element<RemovableDocument>> removableDocuments;
    @CCD(
            label = "Documents about to be removed",
            hint = "If these do not look correct, please cancel and try again.",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawSuperuserCrudAccess.class}
    )
    private String documentsToBeRemoved;

    @CCD(
            label = " ",
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawSystemupdateCourtnavCrudAccess.class, CaseworkerApproverCaseworkerCaaRAccess.class, CaseworkerPrivatelawCafcassRPlus1RolesAgacroAccess.class, CaseworkerPrivatelawSuperuserCrudCaseworkerWaTaskConfigurationRAccess.class}
    )
    private AllocatedBarrister allocatedBarrister;

    @CCD(
            label = "DFJ Area :",
            typeOverride = FieldType.FixedList,
            typeParameterOverride = "DFJArea",
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
    )
    private String dfjArea;

    @CCD(
            label = "Send Or Reply Option",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class}
    )
    private String optionSendOrReply;

    @CCD(
            label = "Identifier of the message associated with task",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus3RolesMtnfliAccess.class, CaseworkerPrivatelawExternaluserViewonlyCruPlus1RolesHakzzsAccess.class}
    )
    private String messageIdentifier;

    @CCD(
            label = "Setup TTL",
            searchable = false,
            access = {TTLProfileCruAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    @JsonProperty("TTL")
    private TTL retainAndDisposeTimeToLive;

    @CCD(label = " ", searchable = false, access = {CaseworkerPrivatelawSystemupdateCitizenCrudAccess.class})
    private List<Element<Document>> miamDocumentsCopy;

    @CCD(
            label = "*Is there an order under section 91(14) Children Act 1989, a limited civil restraint order, a general civil restraint order or an extended civil restraint order in force which means you need permission to make this application?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private YesOrNo orderInPlacePermissionRequired;
    @CCD(
            label = "Provide case number and name of the court",
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private String orderDetailsForPermissions;
    @CCD(
            label = "Upload file",
            categoryID = "previousOrdersSubmittedWithApplication",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus5RolesCymekuAccess.class, CaseworkerPrivatelawReadonlyRCaseworkerWaTaskConfigurationCruAccess.class}
    )
    private Document uploadOrderDocForPermission;

    /* Local authority policies */
    @CCD(label = " ", access = {LASOLICITORRPlus13RolesTxshhlAccess.class})
    @JsonProperty("localAuthoritySolicitorOrganisationPolicy")
    private OrganisationPolicy localAuthoritySolicitorOrganisationPolicy;

    @CCD(
            label = "Local Authority",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerPrivatelawSolicitorCitizenCudAccess.class, CaseworkerApproverCrudAccess.class, CaseworkerCaaCudAccess.class, CaseworkerPrivatelawSuperuserCudAccess.class, CourtnavCudAccess.class}
    )
    private LocalAuthority localAuthority;

    @JsonUnwrapped
    private RenameDocument renameDocument;
    @JsonUnwrapped
    @Getter(AccessLevel.NONE)
    private DocumentRemovalWrapper documentRemovalWrapper;

    @JsonIgnore
    public DocumentRemovalWrapper getDocumentRemovalWrapper() {
        if (documentRemovalWrapper == null) {
            this.documentRemovalWrapper = new DocumentRemovalWrapper();
        }
        return documentRemovalWrapper;
    }

    @CCD(
            label = "Other people",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class, CaseworkerWaTaskConfigurationCudCitizenCuAccess.class}
    )
    private List<Element<ApplicantConfidentialityDetails>> otherPeopleConfidentialDetails;

    @CCD(
            label = "Other parties C8 documents",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCaseworkerPrivatelawSystemupdateCuAccess.class}
    )
    private List<Element<ResponseDocuments>> otherPartyC8Documents;
    @CCD(
            label = "Other parties archived C8 documents",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeRPlus2RolesFfpcmmAccess.class, CaseworkerPrivatelawCourtadminCruPlus1RolesQuzmdnAccess.class, CaseworkerPrivatelawCafcassRAccess.class}
    )
    private List<Element<ResponseDocuments>> otherPartyC8DocumentsArchived;
}
