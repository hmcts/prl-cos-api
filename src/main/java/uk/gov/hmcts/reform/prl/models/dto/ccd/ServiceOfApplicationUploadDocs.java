package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.documents.Document;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruCourtnavCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorRCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCafcassCaseworkerPrivatelawSolicitorRAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceOfApplicationUploadDocs {

    @CCD(
            label = "PD36ZE letter",
            hint = "Upload the PD36ZE letter that is used by your court. This letter explains how the court manages child arrangements order cases",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawCourtadminCruCourtnavCuAccess.class, CaseworkerPrivatelawSuperuserRAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class}
    )
    @JsonProperty("pd36qLetter")
    private final Document pd36qLetter;
    @CCD(
            label = "Special arrangements letter",
            hint = "Upload the special arrangement template letter that is used by your court. The letter explains what measures can be put in place by the court to support people in the case",
            categoryID = "anyOtherDoc",
            searchable = false,
            access = {CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawCourtadminCruCourtnavCuAccess.class, CaseworkerPrivatelawSuperuserRAccess.class, CaseworkerPrivatelawSystemupdateCuAccess.class}
    )
    @JsonProperty("specialArrangementsLetter")
    private final Document specialArrangementsLetter;
    @CCD(
            label = "Additional documents",
            hint = "Upload any additional documents in your service pack that are due to be sent to both the applicant and the respondent.",
            categoryID = "anyOtherDoc",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCuPlus3RolesStmhzpAccess.class, CourtnavCuAccess.class}
    )
    @JsonProperty("additionalDocuments")
    private final Document additionalDocuments;
    @CCD(
            label = "Upload additional documents",
            hint = "Upload any additional documents in your service pack that are due to be sent to both the applicant and the respondent.",
            categoryID = "anyOtherDoc",
            searchable = false,
            access = {CaseworkerPrivatelawSolicitorRPlus3RolesWeuluwAccess.class, CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    @JsonProperty("additionalDocumentsList")
    private final List<Element<Document>> additionalDocumentsList;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawJudgeCruPlus2RolesZuwvtyAccess.class, CaseworkerPrivatelawCourtadminCruPlus1RolesWonkkkAccess.class, CaseworkerPrivatelawSolicitorRCourtnavCruAccess.class}
    )
    @JsonProperty("sentDocumentPlaceHolder")
    private final String sentDocumentPlaceHolder;
    @CCD(
            label = "Upload notice of safety letter",
            hint = "Upload the version that is used by your court.",
            searchable = false,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerPrivatelawSuperuserCaseworkerWaTaskConfigurationRAccess.class, CaseworkerPrivatelawSystemupdateCitizenCrudAccess.class, CaseworkerPrivatelawCafcassCaseworkerPrivatelawSolicitorRAccess.class}
    )
    @JsonProperty("noticeOfSafetySupportLetter")
    private final Document noticeOfSafetySupportLetter;
}
