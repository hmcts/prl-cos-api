package uk.gov.hmcts.reform.prl.models.dto.ccd;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.InterpreterNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.WelshNeed;

import java.util.List;
import uk.gov.hmcts.ccd.sdk.api.CCD;
import uk.gov.hmcts.ccd.sdk.type.FieldType;
import uk.gov.hmcts.reform.prl.ccd.access.DefaultAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCuCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCuAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CitizenCrudAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerWaTaskConfigurationCourtnavCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSolicitorCitizenCruAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess;
import uk.gov.hmcts.reform.prl.ccd.access.CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AttendHearing {
    @CCD(
            label = "*Will the applicant, or anyone else attending court, want to speak Welsh or read and write in Welsh during the proceedings?",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class, CaseworkerPrivatelawSolicitorCuCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final YesOrNo isWelshNeeded;
    @CCD(
            label = "Welsh needs",
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class, CaseworkerPrivatelawSolicitorCuAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class, CitizenCrudAccess.class}
    )
    @JsonAlias({"welshNeeds", "fl401WelshNeeds"})
    private final List<Element<WelshNeed>> welshNeeds;
    @CCD(
            label = "*Do you know if an interpreter will be needed in the court to explain information in a certain language?",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final YesOrNo isInterpreterNeeded;
    @CCD(
            label = "Interpreter needs",
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCuAccess.class, CaseworkerPrivatelawSolicitorCuCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final List<Element<InterpreterNeed>> interpreterNeeds;
    @CCD(
            label = "*Does the applicant, or anyone else attending the court, have a disability?",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final YesOrNo isDisabilityPresent;
    @CCD(
            label = " ",
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final String adjustmentsRequired;
    @CCD(
            label = "*Will the court need to make special arrangements for the applicant, or any child involved in the case?",
            typeOverride = FieldType.YesOrNo,
            access = {CaseworkerPrivatelawCourtadminCrudPlus3RolesYwsmkiAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCrudPlus1RolesYyzdhqAccess.class}
    )
    private final YesOrNo isSpecialArrangementsRequired;
    @CCD(
            label = " ",
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final String specialArrangementsRequired;
    @CCD(
            label = "*Do you know if an intermediary will be required?",
            typeOverride = FieldType.YesOrNo,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final YesOrNo isIntermediaryNeeded;
    @CCD(
            label = "*Set out the reasons that an intermediary is required.",
            typeOverride = FieldType.TextArea,
            access = {DefaultAccess.class, CaseworkerWaTaskConfigurationCourtnavCruAccess.class, CaseworkerPrivatelawSolicitorCitizenCruAccess.class, CaseworkerPrivatelawSuperuserCruAccess.class}
    )
    private final String reasonsForIntermediary;
}
