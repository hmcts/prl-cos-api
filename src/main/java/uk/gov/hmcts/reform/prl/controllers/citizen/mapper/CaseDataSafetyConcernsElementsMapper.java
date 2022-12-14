package uk.gov.hmcts.reform.prl.controllers.citizen.mapper;

import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.prl.enums.*;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.*;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildPassportDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

public class CaseDataSafetyConcernsElementsMapper {

    private static final String Applicant = "applicant";
    private static final String Children = "children";
    private static final String Child_Abduction = "Abduction";
    private static final String All_Children="All of the children in the application";
    private static final String Supervised = "Yes, but I prefer that it is supervised";

    private CaseDataSafetyConcernsElementsMapper() {
    }

    public static void updateSafetyConcernsElementsForCaseData(CaseData.CaseDataBuilder caseDataBuilder,
                                                               C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {
        caseDataBuilder.allegationOfHarmRevised(buildAllegationOfHarmRevised(c100RebuildSafetyConcernsElements));
        System.out.println(buildAllegationOfHarmRevised(c100RebuildSafetyConcernsElements));
    }

    private static AllegationOfHarmRevised buildAllegationOfHarmRevised(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {
        List<String> whoConcernAboutList = Arrays.stream(c100RebuildSafetyConcernsElements.getWhoConcernAbout())
            .collect(Collectors.toList());
        List<String> c1AConcernAboutChild = Arrays.stream(c100RebuildSafetyConcernsElements.getC1AConcernAboutChild())
            .collect(Collectors.toList());
        return AllegationOfHarmRevised
            .builder()
            .newAllegationsOfHarmYesNo(c100RebuildSafetyConcernsElements.getHaveSafetyConcerns())
            .newAllegationsOfHarmDomesticAbuseYesNo(buildApplicantConcernAbout(whoConcernAboutList))
            .newAllegationsOfHarmChildAbuseYesNo(buildChildConcernAbout(whoConcernAboutList))
            .newAllegationsOfHarmChildAbductionYesNo(buildChildAbduction(c1AConcernAboutChild))
            .newAllegationsOfHarmSubstanceAbuseYesNo(c100RebuildSafetyConcernsElements.getC1AOtherConcernsDrugs())
            .newAllegationsOfHarmSubstanceAbuseDetails(isNotEmpty(c100RebuildSafetyConcernsElements.getC1AOtherConcernsDrugsDetails())
                                                           ? c100RebuildSafetyConcernsElements.getC1AOtherConcernsDrugsDetails() : null)
            .newAllegationsOfHarmOtherConcerns(c100RebuildSafetyConcernsElements.getC1AChildSafetyConcerns())
            .newAllegationsOfHarmOtherConcernsDetails(isNotEmpty(c100RebuildSafetyConcernsElements.getC1AChildSafetyConcernsDetails())
                                                          ? c100RebuildSafetyConcernsElements.getC1AChildSafetyConcernsDetails() : null)
            .domesticBehaviours(buildDomesticAbuseBehavioursDetails(c100RebuildSafetyConcernsElements))
            .childAbuseBehaviours(buildChildAbuseBehavioursDetails(c100RebuildSafetyConcernsElements))
            .newPreviousAbductionThreats(isNotEmpty(c100RebuildSafetyConcernsElements.getC1APreviousAbductionsShortDesc()) ? YesOrNo.No : Yes)
            .newPreviousAbductionThreatsDetails(c100RebuildSafetyConcernsElements.getC1APreviousAbductionsShortDesc())
            .newChildrenLocationNow(c100RebuildSafetyConcernsElements.getC1AChildsCurrentLocation())
            .newAbductionPassportOfficeNotified(c100RebuildSafetyConcernsElements.getC1AAbductionPassportOfficeNotified())
            .newAbductionChildHasPassport(c100RebuildSafetyConcernsElements.getC1AAbductionPassportOfficeNotified())
            .newAbductionChildHasPassport(c100RebuildSafetyConcernsElements.getC1APassportOffice())
            .childPassportDetails(buildChildPassportDetails(c100RebuildSafetyConcernsElements))
            .newAbductionPreviousPoliceInvolvement(c100RebuildSafetyConcernsElements.getC1APoliceOrInvestigatorInvolved())
            .newAbductionPreviousPoliceInvolvementDetails(c100RebuildSafetyConcernsElements.getC1APoliceOrInvestigatorOtherDetails())
            .newChildAbductionReasons(c100RebuildSafetyConcernsElements.getC1AAbductionReasonOutsideUk())
            .newAllegationsOfHarmOtherConcernsCourtActions(c100RebuildSafetyConcernsElements.getC1AKeepingSafeStatement())
            .newAgreeChildUnsupervisedTime(("Yes".equalsIgnoreCase(c100RebuildSafetyConcernsElements
                                                                       .getC1ASupervisionAgreementDetails()) ? YesOrNo.No : Yes))
            .newAgreeChildSupervisedTime((Supervised.equalsIgnoreCase(
                c100RebuildSafetyConcernsElements
                    .getC1ASupervisionAgreementDetails()) ? YesOrNo.Yes : YesOrNo.No))
            .newAgreeChildOtherContact(c100RebuildSafetyConcernsElements.getC1AAgreementOtherWaysDetails())
            .build();

    }

    private static YesOrNo buildChildSupervisedTime(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {

        if (Supervised.equalsIgnoreCase(c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails())) {
            return YesOrNo.Yes;
        } else if (c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails()
            .equalsIgnoreCase("Yes")) {
            return YesOrNo.Yes;
        }

        return YesOrNo.No;
    }

    private static ChildPassportDetails buildChildPassportDetails(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {
        List<AbductionChildPassportPossessionEnum> possessionChildrenPassport = new ArrayList<>();
        for (String possession : c100RebuildSafetyConcernsElements.getC1APossessionChildrenPassport()) {
            if (possession.equalsIgnoreCase("Mother")) {
                possessionChildrenPassport.add(AbductionChildPassportPossessionEnum.mother);
            }
            if (possession.equalsIgnoreCase("father")) {
                possessionChildrenPassport.add(AbductionChildPassportPossessionEnum.father);
            }
            if (possession.equalsIgnoreCase("other")) {
                possessionChildrenPassport.add(AbductionChildPassportPossessionEnum.other);

            }
        }
        return ChildPassportDetails.builder().newChildPassportPossession(possessionChildrenPassport)
            .newChildPassportPossessionOtherDetails(isNotEmpty(c100RebuildSafetyConcernsElements.getC1AProvideOtherDetails()) ? c100RebuildSafetyConcernsElements.getC1AProvideOtherDetails() : null)
            .newChildHasMultiplePassports(c100RebuildSafetyConcernsElements.getC1AChildrenMoreThanOnePassport())
            .build();

    }


    private static List<Element<ChildAbuseBehaviours>> buildChildAbuseBehavioursDetails(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {
        List<Element<ChildAbuseBehaviours>> childElements = new ArrayList<>();
        ChildSafetyConcernsDto childAbuse = c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild();
        AbuseDTO physicalAbuseDto = childAbuse.getPhysicalAbuse();
        AbuseDTO emotionalAbuse = childAbuse.getEmotionalAbuse();
        AbuseDTO financialAbuse = childAbuse.getFinancialAbuse();
        AbuseDTO sexualAbuse = childAbuse.getSexualAbuse();
        AbuseDTO psychologicalAbuse = childAbuse.getPsychologicalAbuse();

        if (isNotEmpty(physicalAbuseDto)) {
            childElements.add(mapToChildAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1, physicalAbuseDto));
        }
        if (isNotEmpty(emotionalAbuse)) {
            childElements.add(mapToChildAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_4, emotionalAbuse));
        }
        if (isNotEmpty(financialAbuse)) {
            childElements.add(mapToChildAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_5, financialAbuse));
        }
        if (isNotEmpty(sexualAbuse)) {
            childElements.add(mapToChildAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_3, sexualAbuse));
        }
        if (isNotEmpty(psychologicalAbuse)) {
            childElements.add(mapToChildAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_2, psychologicalAbuse));
        }


        return childElements;
    }

    private static List<Element<DomesticAbuseBehaviours>> buildDomesticAbuseBehavioursDetails(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {
        List<Element<DomesticAbuseBehaviours>> applicantElements = new ArrayList<>();
        List<ApplicantSafteConcernDTO> abuseTypeList = List.of(c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getApplicant());
        ApplicantSafteConcernDTO applicantAbuse = c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getApplicant();
        AbuseDTO physicalAbuseDto = applicantAbuse.getPhysicalAbuse();
        AbuseDTO emotionalAbuse = applicantAbuse.getEmotionalAbuse();
        AbuseDTO financialAbuse = applicantAbuse.getFinancialAbuse();
        AbuseDTO sexualAbuse = applicantAbuse.getSexualAbuse();
        AbuseDTO psychologicalAbuse = applicantAbuse.getPsychologicalAbuse();

        if (isNotEmpty(physicalAbuseDto)) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1, physicalAbuseDto));
        }
        if (isNotEmpty(emotionalAbuse)) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_4, emotionalAbuse));
        }
        if (isNotEmpty(financialAbuse)) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_5, financialAbuse));
        }
        if (isNotEmpty(sexualAbuse)) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_3, sexualAbuse));
        }
        if (isNotEmpty(psychologicalAbuse)) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_2, psychologicalAbuse));
        }

        return applicantElements;

    }

    private static Element<ChildAbuseBehaviours> mapToChildAbuse(TypeOfAbuseEnum typeOfAbuseEnum, AbuseDTO abuseDTO) {

        YesOrNo allChildrenAreRisk = All_Children
            .equalsIgnoreCase(String.valueOf(abuseDTO.getChildrenConcernedAbout()))
                                  ? YesOrNo.Yes : YesOrNo.No;
        String whichChildrenAreRisk = (allChildrenAreRisk == YesOrNo.No) ?
            StringUtils.join(abuseDTO.getChildrenConcernedAbout(), ",") : null;

        return Element.<ChildAbuseBehaviours>builder().value(ChildAbuseBehaviours.builder()
                                                                 .typeOfAbuse(typeOfAbuseEnum)
                                                                 .newAbuseNatureDescription(abuseDTO.getBehaviourDetails())
                                                                 .newBehavioursApplicantSoughtHelp(abuseDTO.getSeekHelpFromPersonOrAgency())
                                                                 .newBehavioursStartDateAndLength(abuseDTO.getBehaviourStartDate())
                                                                 .newBehavioursApplicantHelpSoughtWho(abuseDTO.getSeekHelpDetails())
                                                                 .allChildrenAreRisk(allChildrenAreRisk)
                                                                 .whichChildrenAreRisk(whichChildrenAreRisk)
                                                                 .build()).build();

    }

    private static Element<DomesticAbuseBehaviours> mapToDomesticAbuse(TypeOfAbuseEnum typeOfAbuseEnum, AbuseDTO abuseDTO) {
        return Element.<DomesticAbuseBehaviours>builder().value(DomesticAbuseBehaviours.builder()
                                                                    .typeOfAbuse(typeOfAbuseEnum)
                                                                    .newAbuseNatureDescription(abuseDTO.getBehaviourDetails())
                                                                    .newBehavioursApplicantSoughtHelp(abuseDTO.getSeekHelpFromPersonOrAgency())
                                                                    .newBehavioursStartDateAndLength(abuseDTO.getBehaviourStartDate())
                                                                    .newBehavioursApplicantHelpSoughtWho(abuseDTO.getSeekHelpDetails())
                                                                    .build()).build();
    }

    private static YesOrNo buildApplicantConcernAbout(List<String> whoConcernsAboutList) {
        if (whoConcernsAboutList.contains(Applicant)) {
            return YesOrNo.Yes;
        }
        return YesOrNo.No;
    }

    private static YesOrNo buildChildConcernAbout(List<String> whoConcernsAboutList) {
        if (whoConcernsAboutList.contains(Children)) {
            return YesOrNo.Yes;
        }
        return YesOrNo.No;
    }

    private static YesOrNo buildChildAbduction(List<String> typeOfBehaviourList) {
        if (typeOfBehaviourList.contains(Child_Abduction)) {
            return YesOrNo.Yes;
        }
        return YesOrNo.No;
    }


}
