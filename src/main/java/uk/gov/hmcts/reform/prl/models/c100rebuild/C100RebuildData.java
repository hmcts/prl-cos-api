package uk.gov.hmcts.reform.prl.models.c100rebuild;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCrudCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CourtnavCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawReadonlyCrudAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class C100RebuildData {

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildInternationalElements;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildReasonableAdjustments;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildTypeOfOrder;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildHearingWithoutNotice;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildHearingUrgency;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildOtherProceedings;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildReturnUrl;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class, CitizenCrudAccess.class, CourtnavCrudAccess.class}
    )
    private String c100RebuildMaim;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildChildDetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildApplicantDetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildOtherChildrenDetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildRespondentDetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildOtherPersonsDetails;

    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildSafetyConcerns;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildScreeningQuestions;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildHelpWithFeesDetails;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
    )
    private String c100RebuildStatementOfTruth;
    private String helpWithFeesReferenceNumber;
    @CCD(
            label = " ",
            searchable = false,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class, CaseworkerPrivatelawCourtadminRAccess.class}
    )
    private String c100RebuildChildPostCode;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawReadonlyRCaseworkerPrivatelawSystemupdateCruAccess.class, CitizenCrudCourtnavCruAccess.class}
    )
    private String c100RebuildConsentOrderDetails;
    @CCD(
            label = " ",
            searchable = false,
            typeOverride = FieldType.TextArea,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwluelAccess.class, CaseworkerPrivatelawSolicitorCitizenCrudAccess.class, CaseworkerPrivatelawReadonlyCrudAccess.class, CourtnavCrudAccess.class}
    )
    private String applicantPcqId;
}
