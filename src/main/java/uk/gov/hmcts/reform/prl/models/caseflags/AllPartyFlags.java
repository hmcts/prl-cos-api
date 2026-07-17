package uk.gov.hmcts.reform.prl.models.caseflags;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverRPlus6RolesXmoyviAccess;
import uk.gov.hmcts.reform.prl.ccd.access.APPLICANTSOLICITORCREATORCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTBARRISTER1C100APPLICANTSOLICITOR1CruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTBARRISTER2C100APPLICANTSOLICITOR2CruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTBARRISTER3C100APPLICANTSOLICITOR3CruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTBARRISTER4C100APPLICANTSOLICITOR4CruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100APPLICANTBARRISTER5C100APPLICANTSOLICITOR5CruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTBARRISTER1C100RESPONDENTSOLICITOR1CruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTBARRISTER2C100RESPONDENTSOLICITOR2CruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTBARRISTER3C100RESPONDENTSOLICITOR3CruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTBARRISTER4C100RESPONDENTSOLICITOR4CruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.C100RESPONDENTBARRISTER5C100RESPONDENTSOLICITOR5CruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.FL401APPLICANTBARRISTERFL401APPLICANTSOLICITORCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.FL401RESPONDENTBARRISTERFL401RESPONDENTSOLICITORCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerApproverCaseworkerCaaRAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSystemupdateCrudAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class AllPartyFlags {
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER1C100APPLICANTSOLICITOR1CruAccess.class, CitizenCruAccess.class}
    )
    private Flags caApplicant1ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER2C100APPLICANTSOLICITOR2CruAccess.class, CitizenCruAccess.class}
    )
    private Flags caApplicant2ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER3C100APPLICANTSOLICITOR3CruAccess.class, CitizenCruAccess.class}
    )
    private Flags caApplicant3ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER4C100APPLICANTSOLICITOR4CruAccess.class, CitizenCruAccess.class}
    )
    private Flags caApplicant4ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER5C100APPLICANTSOLICITOR5CruAccess.class, CitizenCruAccess.class}
    )
    private Flags caApplicant5ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER1C100APPLICANTSOLICITOR1CruAccess.class}
    )
    private Flags caApplicantSolicitor1ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER2C100APPLICANTSOLICITOR2CruAccess.class}
    )
    private Flags caApplicantSolicitor2ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER3C100APPLICANTSOLICITOR3CruAccess.class}
    )
    private Flags caApplicantSolicitor3ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER4C100APPLICANTSOLICITOR4CruAccess.class}
    )
    private Flags caApplicantSolicitor4ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER5C100APPLICANTSOLICITOR5CruAccess.class}
    )
    private Flags caApplicantSolicitor5ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER1C100APPLICANTSOLICITOR1CruAccess.class}
    )
    private Flags caApplicantBarrister1ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER2C100APPLICANTSOLICITOR2CruAccess.class}
    )
    private Flags caApplicantBarrister2ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER3C100APPLICANTSOLICITOR3CruAccess.class}
    )
    private Flags caApplicantBarrister3ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER4C100APPLICANTSOLICITOR4CruAccess.class}
    )
    private Flags caApplicantBarrister4ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, C100APPLICANTBARRISTER5C100APPLICANTSOLICITOR5CruAccess.class}
    )
    private Flags caApplicantBarrister5ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER1C100RESPONDENTSOLICITOR1CruAccess.class, CitizenCruAccess.class}
    )
    private Flags caRespondent1ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER2C100RESPONDENTSOLICITOR2CruAccess.class, CitizenCruAccess.class}
    )
    private Flags caRespondent2ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER3C100RESPONDENTSOLICITOR3CruAccess.class, CitizenCruAccess.class}
    )
    private Flags caRespondent3ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER4C100RESPONDENTSOLICITOR4CruAccess.class, CitizenCruAccess.class}
    )
    private Flags caRespondent4ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER5C100RESPONDENTSOLICITOR5CruAccess.class, CitizenCruAccess.class}
    )
    private Flags caRespondent5ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER1C100RESPONDENTSOLICITOR1CruAccess.class}
    )
    private Flags caRespondentSolicitor1ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER2C100RESPONDENTSOLICITOR2CruAccess.class}
    )
    private Flags caRespondentSolicitor2ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER3C100RESPONDENTSOLICITOR3CruAccess.class}
    )
    private Flags caRespondentSolicitor3ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER4C100RESPONDENTSOLICITOR4CruAccess.class}
    )
    private Flags caRespondentSolicitor4ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER5C100RESPONDENTSOLICITOR5CruAccess.class}
    )
    private Flags caRespondentSolicitor5ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER1C100RESPONDENTSOLICITOR1CruAccess.class}
    )
    private Flags caRespondentBarrister1ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER2C100RESPONDENTSOLICITOR2CruAccess.class}
    )
    private Flags caRespondentBarrister2ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER3C100RESPONDENTSOLICITOR3CruAccess.class}
    )
    private Flags caRespondentBarrister3ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER4C100RESPONDENTSOLICITOR4CruAccess.class}
    )
    private Flags caRespondentBarrister4ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, C100RESPONDENTBARRISTER5C100RESPONDENTSOLICITOR5CruAccess.class}
    )
    private Flags caRespondentBarrister5ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class}
    )
    private Flags caOtherParty1ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class}
    )
    private Flags caOtherParty2ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class}
    )
    private Flags caOtherParty3ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class}
    )
    private Flags caOtherParty4ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class}
    )
    private Flags caOtherParty5ExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, FL401APPLICANTBARRISTERFL401APPLICANTSOLICITORCruAccess.class, CitizenCruAccess.class}
    )
    private Flags daApplicantExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, FL401APPLICANTBARRISTERFL401APPLICANTSOLICITORCruAccess.class}
    )
    private Flags daApplicantSolicitorExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, APPLICANTSOLICITORCREATORCruAccess.class, FL401APPLICANTBARRISTERFL401APPLICANTSOLICITORCruAccess.class}
    )
    private Flags daApplicantBarristerExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, FL401RESPONDENTBARRISTERFL401RESPONDENTSOLICITORCruAccess.class, CitizenCruAccess.class}
    )
    private Flags daRespondentExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, FL401RESPONDENTBARRISTERFL401RESPONDENTSOLICITORCruAccess.class}
    )
    private Flags daRespondentSolicitorExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class, FL401RESPONDENTBARRISTERFL401RESPONDENTSOLICITORCruAccess.class}
    )
    private Flags daRespondentBarristerExternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicant1InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicant2InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicant3InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicant4InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicant5InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicantSolicitor1InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicantSolicitor2InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicantSolicitor3InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicantSolicitor4InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicantSolicitor5InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicantBarrister1InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicantBarrister2InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicantBarrister3InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicantBarrister4InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caApplicantBarrister5InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondent1InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondent2InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondent3InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondent4InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondent5InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondentSolicitor1InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondentSolicitor2InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondentSolicitor3InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondentSolicitor4InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondentSolicitor5InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondentBarrister1InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondentBarrister2InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondentBarrister3InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondentBarrister4InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caRespondentBarrister5InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caOtherParty1InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caOtherParty2InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caOtherParty3InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caOtherParty4InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags caOtherParty5InternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerApproverCaseworkerCaaRAccess.class, CaseworkerPrivatelawSystemupdateCrudAccess.class}
    )
    private Flags daApplicantInternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags daApplicantSolicitorInternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags daApplicantBarristerInternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags daRespondentInternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags daRespondentSolicitorInternalFlags;
    @CCD(
            label = " ",
            searchable = false,
            retainHiddenValue = true,
            access = {CaseworkerApproverRPlus6RolesXmoyviAccess.class}
    )
    private Flags daRespondentBarristerInternalFlags;
}
