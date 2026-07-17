package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoNotApplicable;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofapplication.SoaSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.ConfidentialCheckFailed;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofapplication.SoaPack;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.DocumentListForLa;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRPlus2RolesOwnrpcAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceOfApplication {
    @CCD(
            label = "Applicant(s)",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess.class}
    )
    @JsonProperty("soaApplicantsList")
    private final DynamicMultiSelectList soaApplicantsList;
    @CCD(
            label = "Address",
            searchable = false,
            typeOverride = FieldType.AddressUK,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    @JsonProperty("coverPageAddress")
    private final Address coverPageAddress;
    @CCD(
            label = "Cover page party name",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    @JsonProperty("coverPagePartyName")
    private final String coverPagePartyName;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess.class}
    )
    @JsonProperty("soaOtherPeoplePresentInCaseFlag")
    private final YesOrNo soaOtherPeoplePresentInCaseFlag;

    @CCD(
            label = "Does this application need to be personally served on the respondent?",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    @JsonProperty("soaServeToRespondentOptions")
    private final YesNoNotApplicable soaServeToRespondentOptions;
    @CCD(
            label = "Does this application need to be personally served on the respondent?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    @JsonProperty("soaServeToRespondentOptionsDA")
    private final YesOrNo soaServeToRespondentOptionsDA;
    @CCD(
            label = "Who is responsible for serving the respondent?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus2RolesOwnrpcAccess.class}
    )
    private final SoaSolicitorServingRespondentsEnum soaServingRespondentsOptions;
    @CCD(
            label = "Who is responsible for serving the respondent?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus2RolesOwnrpcAccess.class}
    )
    private final SoaCitizenServingRespondentsEnum soaCitizenServingRespondentsOptions;

    @CCD(
            label = "Confirm Recipients",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    @JsonProperty("soaRecipientsOptions")
    private final DynamicMultiSelectList soaRecipientsOptions;
    @CCD(
            label = "Which other people should the notice of proceedings be sent to?",
            searchable = false,
            typeOverride = FieldType.DynamicMultiSelectList,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    private final DynamicMultiSelectList soaOtherParties;
    @CCD(
            label = "Cafcass email address",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    private final String soaCafcassEmailId;
    @CCD(
            label = "Does Cafcass Cymru need to be served?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    private final YesOrNo soaCafcassCymruServedOptions;
    @CCD(
            label = "Cafcass Cymru email address",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    private final String soaCafcassCymruEmail;

    //Not in use anymore as it was added for intermim confidentiality check
    @CCD(
            label = "Do you still want to serve ?",
            hint = "On selection of Yes, you can proceed with serving. On selection of No, documents will be reviewed and served later",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
    )
    private final YesOrNo proceedToServing;

    // Confidentiality check related fields

    @CCD(
            label = "Applicants pack",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess.class, CaseworkerPrivatelawJudgeCaseworkerPrivatelawLaCruAccess.class}
    )
    private final SoaPack unServedApplicantPack;
    @CCD(
            label = "Respondents pack",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess.class, CitizenCuAccess.class}
    )
    private final SoaPack unServedRespondentPack;
    @CCD(
            label = "Respondents pack",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess.class, CitizenCruAccess.class}
    )
    private final SoaPack unservedCitizenRespondentPack;
    @CCD(
            label = "Others pack",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class}
    )
    private final SoaPack unServedOthersPack;
    @CCD(
            label = "Local Authority pack",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class}
    )
    private final SoaPack unServedLaPack;
    @CCD(
            label = "Cafcass cymru",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class}
    )
    private final SoaPack unServedCafcassCymruPack;

    @CCD(
            label = "Can the application be served?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class}
    )
    private final YesOrNo applicationServedYesNo;
    @CCD(
            label = "Give reasons why the application cannot be served",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class}
    )
    private final String rejectionReason;

    @CCD(
            label = "Confidential check failed",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCudPlus3RolesUbwryyAccess.class}
    )
    private List<Element<ConfidentialCheckFailed>> confidentialCheckFailed; //audit of all failed confidential checks

    @CCD(
            label = "Does the local Authority need to be served?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    private final YesOrNo soaServeLocalAuthorityYesOrNo;
    @CCD(
            label = "Email address",
            searchable = false,
            typeOverride = FieldType.Email,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    private final String soaLaEmailAddress;
    @CCD(
            label = "Does the C8 need to be served on the local authority?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    private final YesOrNo soaServeC8ToLocalAuthorityYesOrNo;
    @CCD(
            label = "Document",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    @JsonProperty("soaDocumentDynamicListForLa")
    private List<Element<DocumentListForLa>> soaDocumentDynamicListForLa;
    @CCD(
            label = "Case contains confidential details ?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class}
    )
    @JsonProperty("isConfidential")
    private final YesOrNo isConfidential;

    /*
    * The below fields are no longer used in the SOA.
    * */
    @CCD(
            label = "Who is responsible for serving the respondent?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus2RolesOwnrpcAccess.class}
    )
    private final SoaSolicitorServingRespondentsEnum soaServingRespondentsOptionsCA;
    @CCD(
            label = "Who is responsible for serving the respondent?",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSolicitorRPlus3RolesTpdbmwAccess.class}
    )
    private final SoaSolicitorServingRespondentsEnum soaServingRespondentsOptionsDA;
    @CCD(
            label = "Who is responsible for serving the respondent?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus2RolesOwnrpcAccess.class}
    )
    private final SoaCitizenServingRespondentsEnum soaCitizenServingRespondentsOptionsCA;
    @CCD(
            label = "Who is responsible for serving the respondent?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSolicitorRPlus2RolesOwnrpcAccess.class}
    )
    private final SoaCitizenServingRespondentsEnum soaCitizenServingRespondentsOptionsDA;
}
