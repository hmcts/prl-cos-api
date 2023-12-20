package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.SpecialArrangementEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildReasonableAdjustmentsElements;
import uk.gov.hmcts.reform.prl.models.complextypes.InterpreterNeed;
import uk.gov.hmcts.reform.prl.models.complextypes.WelshNeed;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.PartyEnum.applicant;
import static uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum.both;
import static uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum.spoken;
import static uk.gov.hmcts.reform.prl.enums.SpokenOrWrittenWelshEnum.written;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.appropriateLighting;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.communicationHelp;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.communicationHelpOther;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.differentTypeChair;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.documentHelpOther;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.documentsHelp;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.extraSupport;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.feelComfortableSupport;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.feelComportableOther;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.friendFamilyMember;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.helpTravellingMovingBuildingSupport;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.intermediary;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.largePrintDocuments;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.noSupportRequired;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.parkingSpace;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.signLanguageInterpreter;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.specifiedColorDocuments;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.supportCourtOther;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.supportWorkerCarer;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.therapyAnimal;
import static uk.gov.hmcts.reform.prl.enums.citizen.DisabilityRequirementEnum.travellingCourtOther;
import static uk.gov.hmcts.reform.prl.enums.citizen.SpecialArrangementEnum.noSafetyRequirements;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataMapper.COMMA_SEPARATOR;

public class CaseDataReasonableAdjustmentsElementsMapper {

    private CaseDataReasonableAdjustmentsElementsMapper() {
    }

    private static final String NEED_INTERPRETER = "needInterpreterInCertainLanguage";
    private static final String SPEAK_WELSH = "speakInWelsh";
    private static final String READ_WRITE_WELSH = "readAndWriteInWelsh";
    private static final String OPEN_BRACKET = "(";
    private static final String CLOSE_BRACKET = ")";
    private static final String COLON = ": ";

    public static void updateReasonableAdjustmentsElementsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                      C100RebuildReasonableAdjustmentsElements c100RebuildReasonableAdjustmentsElements) {
        List<String> specialArrangementList = Arrays.stream(c100RebuildReasonableAdjustmentsElements.getSpecialArrangements())
                .toList();

        List<String> disabilityRequirementsList = Arrays.stream(c100RebuildReasonableAdjustmentsElements.getDisabilityRequirements())
                .toList();

        List<String> languageList = Arrays.stream(c100RebuildReasonableAdjustmentsElements.getLanguageNeeds())
                .toList();

        List<String> communicationHelps = nonNull(c100RebuildReasonableAdjustmentsElements.getCommunicationHelp())
                ? Arrays.stream(c100RebuildReasonableAdjustmentsElements.getCommunicationHelp())
                .toList() : Collections.emptyList();

        YesOrNo isWelshRequired = isWelshRequired(languageList);

        caseDataBuilder
            .attendHearing(AttendHearing.builder()
                               .isWelshNeeded(buildIsWelshNeeded(languageList))
                               .welshNeeds(buildWelshNeeds(languageList))
                               .isInterpreterNeeded(buildInterpreterNeeded(languageList))
                               .interpreterNeeds(buildInterpreterNeeds(languageList, c100RebuildReasonableAdjustmentsElements
                                   .getNeedInterpreterInCertainLanguageDetails()))
                               .isIntermediaryNeeded(communicationHelps.contains(intermediary.name()) ? YesOrNo.Yes : YesOrNo.No)
                               .isSpecialArrangementsRequired(buildSpecialArrangementRequired(specialArrangementList))
                               .specialArrangementsRequired(buildSpecialArrangementList(specialArrangementList,
                                                                                        c100RebuildReasonableAdjustmentsElements
                                                                                            .getSpecialArrangementsOtherSubField()))
                               .isDisabilityPresent(buildIsDisabilityPresent(disabilityRequirementsList))
                               .adjustmentsRequired(buildAdjustmentRequired(disabilityRequirementsList,
                                                                            c100RebuildReasonableAdjustmentsElements))
                               .build())
            //PRL-3382 - Update Welsh language requirements for c100 citizen application
            .welshLanguageRequirement(isWelshRequired)
            .welshLanguageRequirementApplication(YesOrNo.Yes.equals(isWelshRequired)
                                                     ? LanguagePreference.welsh : LanguagePreference.english)
            .welshLanguageRequirementApplicationNeedEnglish(YesOrNo.Yes);
    }

    private static YesOrNo isWelshRequired(List<String> languageList) {
        return !languageList.isEmpty()
            && languageList.contains(READ_WRITE_WELSH) ? YesOrNo.Yes : YesOrNo.No;
    }

    private static List<Element<InterpreterNeed>> buildInterpreterNeeds(List<String> languageList,
                                                                        String needInterpreterInCertainLanguageDetails) {
        if (languageList.contains(NEED_INTERPRETER)) {
            InterpreterNeed interpreterNeed = InterpreterNeed.builder()
                    .party(List.of(applicant)).otherAssistance(needInterpreterInCertainLanguageDetails).build();
            return List.of(Element.<InterpreterNeed>builder().value(interpreterNeed).build());
        }
        return Collections.emptyList();
    }

    private static YesOrNo buildInterpreterNeeded(List<String> languageList) {
        if (languageList.contains(NEED_INTERPRETER)) {
            return YesOrNo.Yes;
        }
        return YesOrNo.No;
    }

    private static List<Element<WelshNeed>> buildWelshNeeds(List<String> languageList) {
        List<SpokenOrWrittenWelshEnum> spokenOrWrittenWelshEnums = new ArrayList<>();
        if (languageList.contains(SPEAK_WELSH) && languageList.contains(READ_WRITE_WELSH)) {
            spokenOrWrittenWelshEnums.add(both);
        } else if (languageList.contains(SPEAK_WELSH)) {
            spokenOrWrittenWelshEnums.add(spoken);
        } else if (languageList.contains(READ_WRITE_WELSH)) {
            spokenOrWrittenWelshEnums.add(written);
        }
        WelshNeed welshNeed = WelshNeed.builder().whoNeedsWelsh("Applicant").spokenOrWritten(spokenOrWrittenWelshEnums).build();
        return List.of(Element.<WelshNeed>builder().value(welshNeed).build());
    }

    private static YesOrNo buildIsWelshNeeded(List<String> languageList) {
        if (languageList.contains(SPEAK_WELSH) || languageList.contains(READ_WRITE_WELSH)) {
            return YesOrNo.Yes;
        }
        return YesOrNo.No;
    }

    private static String buildAdjustmentRequired(List<String> disabilityRequirementsList,
                          C100RebuildReasonableAdjustmentsElements c100RebuildReasonableAdjustmentsElements) {
        StringBuilder adjustmentRequired = new StringBuilder();
        String documentInformation;
        String communicationHelpDetails;
        String extraSupportDetails;
        String feelComfortableSupportDetails;
        String helpTravellingMovingBuildingSupportDetails;
        if (disabilityRequirementsList.contains(noSupportRequired.name())) {
            return noSupportRequired.getDisplayedValue();
        }
        if (disabilityRequirementsList.contains(documentsHelp.name())) {
            documentInformation = buildDocumentInformation(c100RebuildReasonableAdjustmentsElements
                    .getDocumentInformation(), c100RebuildReasonableAdjustmentsElements);
            adjustmentRequired.append(documentsHelp.getDisplayedValue()).append(COLON).append(documentInformation);
        }
        if (disabilityRequirementsList.contains(communicationHelp.name())) {
            communicationHelpDetails = buildCommunicationHelp(c100RebuildReasonableAdjustmentsElements
                    .getCommunicationHelp(), c100RebuildReasonableAdjustmentsElements);
            adjustmentRequired.append(COMMA_SEPARATOR).append(communicationHelp.getDisplayedValue()).append(COLON)
                    .append(communicationHelpDetails);
        }
        if (disabilityRequirementsList.contains(extraSupport.name())) {
            extraSupportDetails = buildExtraSupport(c100RebuildReasonableAdjustmentsElements
                    .getSupportCourt(), c100RebuildReasonableAdjustmentsElements);
            adjustmentRequired.append(COMMA_SEPARATOR).append(extraSupport.getDisplayedValue()).append(COLON)
                    .append(extraSupportDetails);
        }
        if (disabilityRequirementsList.contains(feelComfortableSupport.name())) {
            feelComfortableSupportDetails = buildFeelComfortableSupport(c100RebuildReasonableAdjustmentsElements
                    .getFeelComfortable(), c100RebuildReasonableAdjustmentsElements);
            adjustmentRequired.append(COMMA_SEPARATOR).append(feelComfortableSupport.getDisplayedValue()).append(COLON)
                    .append(feelComfortableSupportDetails);
        }
        if (disabilityRequirementsList.contains(helpTravellingMovingBuildingSupport.name())) {
            helpTravellingMovingBuildingSupportDetails = buildHelpTravellingMovingBuildingSupport(c100RebuildReasonableAdjustmentsElements
                    .getTravellingCourt(), c100RebuildReasonableAdjustmentsElements);
            adjustmentRequired.append(COMMA_SEPARATOR).append(helpTravellingMovingBuildingSupport.getDisplayedValue()).append(COLON)
                    .append(helpTravellingMovingBuildingSupportDetails);
        }
        return String.valueOf(adjustmentRequired);
    }

    private static String buildHelpTravellingMovingBuildingSupport(String[] travellingCourt,
                                                                   C100RebuildReasonableAdjustmentsElements c100RaElements) {
        return Arrays.stream(travellingCourt).toList().stream()
                .map(element -> buildTravellingCourtElement(element,
                        c100RaElements.getParkingSpaceSubField(),
                        c100RaElements.getDifferentTypeChairSubField(),
                        c100RaElements.getTravellingCourtOtherSubField()))
                .collect(Collectors.joining(COMMA_SEPARATOR));
    }

    private static String buildTravellingCourtElement(String element, String parkingSpaceSubField,
                                                      String differentTypeChairSubField, String travellingCourtOtherSubField) {
        if (parkingSpace.name().equalsIgnoreCase(element)) {
            return parkingSpace.getDisplayedValue() + OPEN_BRACKET + parkingSpaceSubField + CLOSE_BRACKET;
        } else if (differentTypeChair.name().equalsIgnoreCase(element)) {
            return differentTypeChair.getDisplayedValue() + OPEN_BRACKET + differentTypeChairSubField + CLOSE_BRACKET;
        } else if (travellingCourtOther.name().equalsIgnoreCase(element)) {
            return travellingCourtOther.getDisplayedValue() + OPEN_BRACKET + travellingCourtOtherSubField + CLOSE_BRACKET;
        } else {
            return DisabilityRequirementEnum.valueOf(element).getDisplayedValue();
        }
    }

    private static String buildFeelComfortableSupport(String[] feelComfortable,
                                                      C100RebuildReasonableAdjustmentsElements c100RaElements) {
        return Arrays.stream(feelComfortable).toList().stream()
                .map(element -> buildFeelComfortableElement(element,
                        c100RaElements.getAppropriateLightingSubField(),
                        c100RaElements.getFeelComfortableOtherSubField()))
                .collect(Collectors.joining(COMMA_SEPARATOR));
    }

    private static String buildFeelComfortableElement(String element, String appropriateLightingSubField,
                                                      String feelComfortableOtherSubField) {
        if (appropriateLighting.name().equalsIgnoreCase(element)) {
            return appropriateLighting.getDisplayedValue() + OPEN_BRACKET + appropriateLightingSubField + CLOSE_BRACKET;
        } else if (feelComportableOther.name().equalsIgnoreCase(element)) {
            return feelComportableOther.getDisplayedValue() + OPEN_BRACKET + feelComfortableOtherSubField + CLOSE_BRACKET;
        } else {
            return DisabilityRequirementEnum.valueOf(element).getDisplayedValue();
        }
    }

    private static String buildExtraSupport(String[] supportCourt, C100RebuildReasonableAdjustmentsElements
            c100RaElements) {
        return Arrays.stream(supportCourt).toList().stream()
                .map(element -> buildSupportCourtElement(element,
                        c100RaElements.getSupportWorkerCarerSubField(),
                        c100RaElements.getFriendFamilyMemberSubField(),
                        c100RaElements.getTherapyAnimalSubField(),
                        c100RaElements.getSupportCourtOtherSubField()))
                .collect(Collectors.joining(COMMA_SEPARATOR));
    }

    private static String buildSupportCourtElement(String element, String supportWorkerCarerSubField,
                                                   String friendFamilyMemberSubField, String therapyAnimalSubField,
                                                   String supportCourtOtherSubField) {
        if (supportWorkerCarer.name().equalsIgnoreCase(element)) {
            return supportWorkerCarer.getDisplayedValue() + OPEN_BRACKET + supportWorkerCarerSubField + CLOSE_BRACKET;
        } else if (friendFamilyMember.name().equalsIgnoreCase(element)) {
            return friendFamilyMember.getDisplayedValue() + OPEN_BRACKET + friendFamilyMemberSubField + CLOSE_BRACKET;
        } else if (therapyAnimal.name().equalsIgnoreCase(element)) {
            return therapyAnimal.getDisplayedValue() + OPEN_BRACKET + therapyAnimalSubField + CLOSE_BRACKET;
        } else if (supportCourtOther.name().equalsIgnoreCase(element)) {
            return supportCourtOther.getDisplayedValue() + OPEN_BRACKET + supportCourtOtherSubField + CLOSE_BRACKET;
        } else {
            return DisabilityRequirementEnum.valueOf(element).getDisplayedValue();
        }
    }

    private static String buildCommunicationHelp(String[] communicationHelp, C100RebuildReasonableAdjustmentsElements
            c100RaElements) {
        return Arrays.stream(communicationHelp).toList().stream()
                .map(element -> buildCommunicationHelpElement(element,
                        c100RaElements.getSignLanguageInterpreterDetails(),
                        c100RaElements.getCommunicationHelpOtherDetails()))
                .collect(Collectors.joining(COMMA_SEPARATOR));
    }

    private static String buildCommunicationHelpElement(String element, String signLanguageInterpreterDetails,
                                                        String communicationHelpOtherDetails) {
        if (signLanguageInterpreter.name().equalsIgnoreCase(element)) {
            return signLanguageInterpreter.getDisplayedValue() + OPEN_BRACKET + signLanguageInterpreterDetails + CLOSE_BRACKET;
        } else if (communicationHelpOther.name().equalsIgnoreCase(element)) {
            return communicationHelpOther.getDisplayedValue() + OPEN_BRACKET + communicationHelpOtherDetails + CLOSE_BRACKET;
        } else {
            return DisabilityRequirementEnum.valueOf(element).getDisplayedValue();
        }
    }

    private static String buildDocumentInformation(String[] documentInformation, C100RebuildReasonableAdjustmentsElements
            c100RaElements) {
        return Arrays.stream(documentInformation).toList().stream()
                .map(element -> buildDocumentInformationElement(element,
                        c100RaElements.getSpecifiedColorDocumentsDetails(), c100RaElements.getLargePrintDocumentsDetails(),
                        c100RaElements.getOtherDetails()))
                .collect(Collectors.joining(COMMA_SEPARATOR));
    }

    private static String buildDocumentInformationElement(String element, String specifiedColorDocumentsDetails,
                                                          String largePrintDocumentsDetails, String otherDetails) {
        if (specifiedColorDocuments.name().equalsIgnoreCase(element)) {
            return specifiedColorDocuments.getDisplayedValue() + OPEN_BRACKET + specifiedColorDocumentsDetails + CLOSE_BRACKET;
        } else if (largePrintDocuments.name().equalsIgnoreCase(element)) {
            return largePrintDocuments.getDisplayedValue() + OPEN_BRACKET + largePrintDocumentsDetails + CLOSE_BRACKET;
        } else if (documentHelpOther.name().equalsIgnoreCase(element)) {
            return documentHelpOther.getDisplayedValue() + OPEN_BRACKET + otherDetails + CLOSE_BRACKET;
        } else {
            return DisabilityRequirementEnum.valueOf(element).getDisplayedValue();
        }
    }

    private static YesOrNo buildIsDisabilityPresent(List<String> disabilityRequirementsList) {
        return disabilityRequirementsList.contains(noSupportRequired.name()) ? YesOrNo.No : YesOrNo.Yes;
    }

    private static String buildSpecialArrangementList(List<String> specialArrangementList, String otherSubField) {
        String specialArrangement = specialArrangementList.stream().map(element -> SpecialArrangementEnum.valueOf(element)
                .getDisplayedValue()).collect(Collectors.joining(COMMA_SEPARATOR));
        if (isNotEmpty(otherSubField)) {
            return specialArrangement + OPEN_BRACKET + otherSubField + CLOSE_BRACKET;
        }
        return specialArrangement;
    }

    private static YesOrNo buildSpecialArrangementRequired(List<String> specialArrangementList) {
        return specialArrangementList.contains(noSafetyRequirements.name()) ? YesOrNo.No : YesOrNo.Yes;
    }
}
