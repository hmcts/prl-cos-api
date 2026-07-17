package uk.gov.hmcts.reform.prl.models.serviceofdocuments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesNoNotApplicable;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.AdditionalRecipients;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.ServiceOfDocumentsCheckEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.SodCitizenServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.enums.serviceofdocuments.SodSolicitorServingRespondentsEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.DocumentsDynamicList;
import uk.gov.hmcts.reform.prl.models.complextypes.manageorders.serveorders.ServeOrgDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.serviceofdocuments.SodPack;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.serviceofapplication.ServedApplicationDetails;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceOfDocuments {

    @CCD(
            label = "Notifications",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class}
    )
    @JsonProperty("servedDocumentsDetailsList")
    private List<Element<ServedApplicationDetails>> servedDocumentsDetailsList;

    @CCD(
            label = "Documents",
            hint = "Select a document to reference",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class}
    )
    @JsonProperty("sodDocumentsList")
    private List<Element<DocumentsDynamicList>> sodDocumentsList;

    @CCD(
            label = "Upload additional documents",
            hint = "Please upload cover letter as first document if any, that is to be sent to both the applicant and the respondent.",
            categoryID = "anyOtherDoc",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class}
    )
    @JsonProperty("sodAdditionalDocumentsList")
    private List<Element<Document>> sodAdditionalDocumentsList;

    @CCD(
            label = "Select additional recipients who needs to be served",
            hint = "For example, Cafcass, Local authority and Other organization",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    @JsonProperty("sodAdditionalRecipients")
    private List<AdditionalRecipients> sodAdditionalRecipients;

    @CCD(
            label = "Recipient",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserRPlus1RolesKswuslAccess.class}
    )
    @JsonProperty("sodAdditionalRecipientsList")
    private List<Element<ServeOrgDetails>> sodAdditionalRecipientsList;

    @CCD(
            label = "Does someone need to check the documents?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    @JsonProperty("sodDocumentsCheckOptions")
    private ServiceOfDocumentsCheckEnum sodDocumentsCheckOptions;

    @CCD(
            label = "Who is responsible for serving the respondent?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    private final SodSolicitorServingRespondentsEnum sodSolicitorServingRespondentsOptions;
    @CCD(
            label = "Who is responsible for serving the respondent?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    private final SodCitizenServingRespondentsEnum sodCitizenServingRespondentsOptions;

    @CCD(
            label = "Unserved documents",
            showCondition = "servedBy=\"DO_NOT_SHOW\"",
            searchable = false,
            access = {DefaultAccess.class}
    )
    private final SodPack sodUnServedPack;

    @CCD(
            label = "Does this document need to be personally served on the respondent?",
            searchable = false,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    @JsonProperty("sodServeToRespondentOptions")
    private final YesNoNotApplicable sodServeToRespondentOptions;

    @CCD(
            label = "Can the documents be served?",
            searchable = false,
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerPrivatelawSuperuserRAccess.class}
    )
    private final YesOrNo canDocumentsBeServed;

}
