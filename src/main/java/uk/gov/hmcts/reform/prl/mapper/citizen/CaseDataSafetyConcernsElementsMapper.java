package uk.gov.hmcts.reform.prl.mapper.citizen;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.prl.enums.NewPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.AbuseDto;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ApplicantSafteConcernDto;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildSafetyConcernsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildSafetyConcernsDto;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.allegationsofharmrevised.ChildAbuseBehaviour;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AllegationOfHarmRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.ChildPassportDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
public class CaseDataSafetyConcernsElementsMapper {

    private static final String Applicant = "applicant";
    private static final String Children = "children";
    private static final String Child_Abduction = "Abduction";
    private static final String All_Children = "All of the children in the application";
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
            .domesticBehaviours((!c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getApplicant().equals(null))
                                    ? buildDomesticAbuseBehavioursDetails(c100RebuildSafetyConcernsElements) : null)
            //.childAbuseBehavioursDocmosis(buildChildAbuseBehavioursDetails(c100RebuildSafetyConcernsElements))
            .newPreviousAbductionThreats(isNotEmpty(c100RebuildSafetyConcernsElements.getC1APreviousAbductionsShortDesc())
             ? YesOrNo.No : Yes)
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
        List<NewPassportPossessionEnum> possessionChildrenPassport = new ArrayList<>();
        for (String possession : c100RebuildSafetyConcernsElements.getC1APossessionChildrenPassport()) {
            if (possession.equalsIgnoreCase("Mother")) {
                possessionChildrenPassport.add(NewPassportPossessionEnum.mother);
            }
            if (possession.equalsIgnoreCase("father")) {
                possessionChildrenPassport.add(NewPassportPossessionEnum.father);
            }
            if (possession.equalsIgnoreCase("other")) {
                possessionChildrenPassport.add(NewPassportPossessionEnum.otherPerson);

            }
        }
        return ChildPassportDetails.builder().newChildPassportPossession(possessionChildrenPassport)
            .newChildPassportPossessionOtherDetails(isNotEmpty(c100RebuildSafetyConcernsElements.getC1AProvideOtherDetails())
             ? c100RebuildSafetyConcernsElements.getC1AProvideOtherDetails() : null)
            .newChildHasMultiplePassports(c100RebuildSafetyConcernsElements.getC1AChildrenMoreThanOnePassport())
            .build();

    }


    private static List<Element<ChildAbuseBehaviour>> buildChildAbuseBehavioursDetails(
        C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {
        List<Element<ChildAbuseBehaviour>> childElements = new ArrayList<>();
        ChildSafetyConcernsDto childAbuse = c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild();

        if (isNotEmpty(childAbuse.getPhysicalAbuse())) {
            childElements.add(mapToChildAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1, childAbuse.getPhysicalAbuse()));
        }
        if (isNotEmpty(childAbuse.getEmotionalAbuse())) {
            childElements.add(mapToChildAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_4, childAbuse.getEmotionalAbuse()));
        }
        if (isNotEmpty(childAbuse.getFinancialAbuse())) {
            childElements.add(mapToChildAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_5, childAbuse.getFinancialAbuse()));
        }
        if (isNotEmpty(childAbuse.getSexualAbuse())) {
            childElements.add(mapToChildAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_3, childAbuse.getSexualAbuse()));
        }
        if (isNotEmpty(childAbuse.getPsychologicalAbuse())) {
            childElements.add(mapToChildAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_2, childAbuse.getPsychologicalAbuse()));
        }


        return childElements;
    }

    private static List<Element<DomesticAbuseBehaviours>> buildDomesticAbuseBehavioursDetails(
        C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {
        List<Element<DomesticAbuseBehaviours>> applicantElements = new ArrayList<>();
        ApplicantSafteConcernDto applicantAbuse = c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getApplicant();

        if (isNotEmpty(applicantAbuse.getPhysicalAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1, applicantAbuse.getPhysicalAbuse()));
        }
        if (isNotEmpty(applicantAbuse.getEmotionalAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_4, applicantAbuse.getEmotionalAbuse()));
        }
        if (isNotEmpty(applicantAbuse.getFinancialAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_5, applicantAbuse.getFinancialAbuse()));
        }
        if (isNotEmpty(applicantAbuse.getSexualAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_3, applicantAbuse.getSexualAbuse()));
        }
        if (isNotEmpty(applicantAbuse.getPsychologicalAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_2, applicantAbuse.getPsychologicalAbuse()));
        }

        return applicantElements;

    }

    private static Element<ChildAbuseBehaviour> mapToChildAbuse(TypeOfAbuseEnum typeOfAbuseEnum, AbuseDto abuseDto) {

        YesOrNo allChildrenAreRisk = All_Children
            .equalsIgnoreCase(String.valueOf(abuseDto.getChildrenConcernedAbout()))
                                  ? YesOrNo.Yes : YesOrNo.No;
        String whichChildrenAreRisk = (allChildrenAreRisk == YesOrNo.No)
            ? StringUtils.join(abuseDto.getChildrenConcernedAbout(), ",") : null;

        return Element.<ChildAbuseBehaviour>builder().value(ChildAbuseBehaviour.builder()
                                                                 .typeOfAbuse(typeOfAbuseEnum.getDisplayedValue())
                                                                 .newAbuseNatureDescription(abuseDto.getBehaviourDetails())
                                                                 .newBehavioursApplicantSoughtHelp(abuseDto.getSeekHelpFromPersonOrAgency())
                                                                 .newBehavioursStartDateAndLength(abuseDto.getBehaviourStartDate())
                                                                 .newBehavioursApplicantHelpSoughtWho(abuseDto.getSeekHelpDetails())
                                                                 .allChildrenAreRisk(allChildrenAreRisk)
                                                                 .whichChildrenAreRisk(whichChildrenAreRisk)
                                                                 .build()).build();

    }

    private static Element<DomesticAbuseBehaviours> mapToDomesticAbuse(TypeOfAbuseEnum typeOfAbuseEnum, AbuseDto abuseDto) {
        return Element.<DomesticAbuseBehaviours>builder().value(DomesticAbuseBehaviours.builder()
                                                                    .typeOfAbuse(typeOfAbuseEnum)
                                                                    .newAbuseNatureDescription(abuseDto.getBehaviourDetails())
                                                                    .newBehavioursApplicantSoughtHelp(abuseDto.getSeekHelpFromPersonOrAgency())
                                                                    .newBehavioursStartDateAndLength(abuseDto.getBehaviourStartDate())
                                                                    .newBehavioursApplicantHelpSoughtWho(abuseDto.getSeekHelpDetails())
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
