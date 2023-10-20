package uk.gov.hmcts.reform.prl.utils.caseflags;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.caseflags.PartyFlags;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.lang.reflect.Field;
import java.util.Collections;

@Slf4j
@Component
public class PartyLevelCaseFlagsGenerator {
    public PartyFlags generatePartyFlags(String partyName, String caseDataField, String roleOnCase) {
        final Flags partyInternalFlag = Flags
            .builder()
            .partyName(partyName)
            .roleOnCase(roleOnCase)
            .visibility("Internal")
            .groupId(caseDataField)
            .details(Collections.emptyList())
            .build();
        final Flags partyExternalFlag = Flags
            .builder()
            .partyName(partyName)
            .roleOnCase(roleOnCase)
            .visibility("External")
            .groupId(caseDataField)
            .details(Collections.emptyList())
            .build();
        PartyFlags partyFlags = PartyFlags.builder()
            .partyInternalFlags(partyInternalFlag)
            .partyExternalFlags(partyExternalFlag)
            .build();
        log.info("Party flag is now generated for ::" + partyName);
        return partyFlags;
    }

    public CaseData generatePartyFlags(CaseData caseData, String partyName, String caseDataField, String roleOnCase) {
        PartyFlags partyFlags = generatePartyFlags(partyName, caseDataField, roleOnCase);
        if (caseData.getAllPartyFlags() == null) {
            AllPartyFlags allPartyFlags = AllPartyFlags.builder().build();
            caseData = caseData.toBuilder().allPartyFlags(allPartyFlags).build();
        }

        switch (caseDataField) {
            case "caApplicant1Flags" -> {
                caseData = caseData.toBuilder()
                    .allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant1Flags(partyFlags).build())
                    .build();
                break;
            }
            case "caApplicant2Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant2Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caApplicant3Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant3Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caApplicant4Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant4Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caApplicant5Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicant5Flags(
                    partyFlags).build()).build();
                break;
            }

            case "caApplicantSolicitor1Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor1Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caApplicantSolicitor2Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor2Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caApplicantSolicitor3Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor3Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caApplicantSolicitor4Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor4Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caApplicantSolicitor5Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caApplicantSolicitor5Flags(
                    partyFlags).build()).build();
                break;
            }

            case "caRespondent1Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent1Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caRespondent2Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent2Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caRespondent3Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent3Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caRespondent4Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent4Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caRespondent5Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondent5Flags(
                    partyFlags).build()).build();
                break;
            }

            case "caRespondentSolicitor1Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor1Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caRespondentSolicitor2Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor2Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caRespondentSolicitor3Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor3Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caRespondentSolicitor4Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor4Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caRespondentSolicitor5Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caRespondentSolicitor5Flags(
                    partyFlags).build()).build();
                break;
            }

            case "caOtherParty1Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty1Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caOtherParty2Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty2Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caOtherParty3Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty3Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caOtherParty4Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty4Flags(
                    partyFlags).build()).build();
                break;
            }
            case "caOtherParty5Flags" -> {
                caseData = caseData.toBuilder().allPartyFlags(caseData.getAllPartyFlags().toBuilder().caOtherParty5Flags(
                    partyFlags).build()).build();
                break;
            }
            default -> {
                break;
            }
        }
        return caseData;
    }

    public CaseData generatePartyFlagsAlternative(CaseData caseData, String partyName, String caseDataField, String roleOnCase) {
        try {
            PartyFlags partyFlags = generatePartyFlags(partyName, caseDataField, roleOnCase);

            Field field = CaseData.class.getField(caseDataField);

            PartyFlags objectInstance = new PartyFlags();

            Object value = field.get(objectInstance);

            field.set(objectInstance, value);
        } catch (NoSuchFieldException noSuchFieldException) {
            log.error("Error no such field");
        } catch (IllegalAccessException illegalAccessException) {
            log.error("Setting the object stated exception");
        }

        return caseData;
    }
}
