package uk.gov.hmcts.reform.prl.utils.caseflags;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;

@Slf4j
@Component
public class PartyLevelCaseFlagsGenerator {

    public static final String VISIBILITY_EXTERNAL = "External";
    public static final String VISIBILITY_INTERNAL = "Internal";

    public Flags generateExternalPartyFlags(String partyName, String roleOnCase, String groupId) {
        return Flags
            .builder()
            .partyName(partyName)
            .roleOnCase(roleOnCase)
            .visibility(VISIBILITY_EXTERNAL)
            .groupId(groupId)
            .details(Collections.emptyList())
            .build();
    }

    public Flags generateInternalPartyFlags(String partyName, String roleOnCase, String groupId) {
        return Flags
            .builder()
            .partyName(partyName)
            .roleOnCase(roleOnCase)
            .visibility(VISIBILITY_INTERNAL)
            .groupId(groupId)
            .details(Collections.emptyList())
            .build();
    }

    public CaseData generatePartyFlags(CaseData caseData,
                                       String partyName,
                                       String caseDataField,
                                       String roleOnCase,
                                       boolean internalFlag,
                                       String groupId) {
        if (caseData.getAllPartyFlags() == null) {
            AllPartyFlags allPartyFlags = AllPartyFlags.builder().build();
            caseData = caseData.toBuilder().allPartyFlags(allPartyFlags).build();
        }
        if (internalFlag) {
            Flags partyInternalFlag = generateInternalPartyFlags(partyName, roleOnCase, groupId);
            switch (caseDataField) {
                case "caApplicant1InternalFlags": {
                    caseData = caseData.toBuilder()
                        .allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant1InternalFlags(
                            partyInternalFlag).build())
                        .build();
                    break;
                }
                case "caApplicant2InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant2InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicant3InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant3InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicant4InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant4InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicant5InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant5InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }

                case "caApplicantSolicitor1InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor1InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor2InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor2InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor3InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor3InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor4InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor4InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor5InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor5InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }

                case "caRespondent1InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent1InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondent2InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent2InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondent3InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent3InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondent4InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent4InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondent5InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent5InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }

                case "caRespondentSolicitor1InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor1InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor2InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor2InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor3InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor3InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor4InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor4InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor5InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor5InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }

                case "caOtherParty1InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty1InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caOtherParty2InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty2InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caOtherParty3InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty3InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caOtherParty4InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty4InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "caOtherParty5InternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty5InternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }

                case "daApplicantInternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().daApplicantInternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "daApplicantSolicitorInternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().daApplicantSolicitorInternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "daRespondentInternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().daRespondentInternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }
                case "daRespondentSolicitorInternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().daRespondentSolicitorInternalFlags(
                        partyInternalFlag).build()).build();
                    break;
                }

                default: {
                    break;
                }
            }
        } else {
            Flags partyExternalFlag = generateExternalPartyFlags(partyName, roleOnCase, groupId);
            switch (caseDataField) {
                case "caApplicant1ExternalFlags": {
                    caseData = caseData.toBuilder()
                        .allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant1ExternalFlags(
                            partyExternalFlag).build())
                        .build();
                    break;
                }
                case "caApplicant2ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant2ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caApplicant3ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant3ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caApplicant4ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant4ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caApplicant5ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant5ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }

                case "caApplicantSolicitor1ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor1ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor2ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor2ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor3ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor3ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor4ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor4ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor5ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor5ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }

                case "caRespondent1ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent1ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caRespondent2ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent2ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caRespondent3ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent3ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caRespondent4ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent4ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caRespondent5ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent5ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }

                case "caRespondentSolicitor1ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor1ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor2ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor2ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor3ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor3ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor4ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor4ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor5ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor5ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }

                case "caOtherParty1ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty1ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caOtherParty2ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty2ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caOtherParty3ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty3ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caOtherParty4ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty4ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "caOtherParty5ExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty5ExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }

                case "daApplicantExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().daApplicantExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "daApplicantSolicitorExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().daApplicantSolicitorExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "daRespondentExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().daRespondentExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }
                case "daRespondentSolicitorExternalFlags": {
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().daRespondentSolicitorExternalFlags(
                        partyExternalFlag).build()).build();
                    break;
                }

                default: {
                    break;
                }
            }
        }
        return caseData;
    }
}
