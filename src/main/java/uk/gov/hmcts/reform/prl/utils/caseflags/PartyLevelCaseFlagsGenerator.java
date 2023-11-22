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
    public Flags generateExternalPartyFlags(String partyName, String roleOnCase, String groupId) {
        return Flags
                .builder()
                .partyName(partyName)
                .roleOnCase(roleOnCase)
                .visibility("External")
                .groupId(groupId)
                .details(Collections.emptyList())
                .build();
    }

    public Flags generateInternalPartyFlags(String partyName, String roleOnCase, String groupId) {
        return Flags
                .builder()
                .partyName(partyName)
                .roleOnCase(roleOnCase)
                .visibility("Internal")
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
            log.info("all party flags is none");
            AllPartyFlags allPartyFlags = AllPartyFlags.builder().build();
            caseData = caseData.toBuilder().allPartyFlags(allPartyFlags).build();
        }
        log.info("party flags set");
        if (internalFlag) {
            log.info("setting internal flags");
            Flags partyInternalFlag = generateInternalPartyFlags(partyName, roleOnCase, groupId);
            log.info("flag generated {}", partyInternalFlag);
            switch (caseDataField) {
                case "caApplicant1InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder()
                            .allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant1InternalFlags(
                                    partyInternalFlag).build())
                            .build();
                    break;
                }
                case "caApplicant2InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant2InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicant3InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant3InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicant4InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant4InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicant5InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant5InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }

                case "caApplicantSolicitor1InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor1InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor2InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor2InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor3InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor3InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor4InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor4InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor5InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor5InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }

                case "caRespondent1InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent1InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondent2InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent2InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondent3InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent3InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondent4InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent4InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondent5InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent5InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }

                case "caRespondentSolicitor1InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor1InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor2InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor2InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor3InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor3InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor4InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor4InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor5InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor5InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }

                case "caOtherParty1InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty1InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caOtherParty2InternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty2InternalFlags(
                            partyInternalFlag).build()).build();
                    break;
                }
                case "caOtherParty3InternalFlags": {
                    log.info("case is :: {}", caseDataField);
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
                default: {
                    break;
                }
            }
        } else {
            log.info("setting partyExternalFlag flags", caseDataField);
            Flags partyExternalFlag = generateExternalPartyFlags(partyName, roleOnCase, groupId);
            log.info("flag generated {}", partyExternalFlag);
            switch (caseDataField) {
                case "caApplicant1ExternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder()
                            .allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant1ExternalFlags(
                                    partyExternalFlag).build())
                            .build();
                    break;
                }
                case "caApplicant2ExternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant2ExternalFlags(
                            partyExternalFlag).build()).build();
                    break;
                }
                case "caApplicant3ExternalFlags": {
                    log.info("case is :: {}", caseDataField);
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
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor1ExternalFlags(
                            partyExternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor2ExternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor2ExternalFlags(
                            partyExternalFlag).build()).build();
                    break;
                }
                case "caApplicantSolicitor3ExternalFlags": {
                    log.info("case is :: {}", caseDataField);
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
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent1ExternalFlags(
                            partyExternalFlag).build()).build();
                    break;
                }
                case "caRespondent2ExternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent2ExternalFlags(
                            partyExternalFlag).build()).build();
                    break;
                }
                case "caRespondent3ExternalFlags": {
                    log.info("case is :: {}", caseDataField);
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
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor1ExternalFlags(
                            partyExternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor2ExternalFlags": {
                    log.info("case is :: {}", caseDataField);
                    caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor2ExternalFlags(
                            partyExternalFlag).build()).build();
                    break;
                }
                case "caRespondentSolicitor3ExternalFlags": {
                    log.info("case is :: {}", caseDataField);
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
                default: {
                    break;
                }
            }
        }
        return caseData;
    }
}
