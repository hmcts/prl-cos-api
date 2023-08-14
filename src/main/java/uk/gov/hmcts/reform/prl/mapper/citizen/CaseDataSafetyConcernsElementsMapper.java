package uk.gov.hmcts.reform.prl.mapper.citizen;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.NewPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.AbuseDto;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ApplicantSafteConcernDto;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildSafetyConcernsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildDetail;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildSafetyConcernsDto;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuse;
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
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
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
                                                               C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                               List<ChildDetail> childDetails) {
        caseDataBuilder.allegationOfHarmRevised(buildAllegationOfHarmRevised(c100RebuildSafetyConcernsElements,childDetails));
        System.out.println(buildAllegationOfHarmRevised(c100RebuildSafetyConcernsElements,childDetails));
    }

    private static AllegationOfHarmRevised buildAllegationOfHarmRevised(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                                        List<ChildDetail> childDetails) {
        List<String> whoConcernAboutList = Arrays.stream(c100RebuildSafetyConcernsElements.getWhoConcernAbout())
            .collect(Collectors.toList());
        List<String> c1AConcernAboutChild = Arrays.stream(c100RebuildSafetyConcernsElements.getC1AConcernAboutChild())
            .collect(Collectors.toList());
        return AllegationOfHarmRevised
            .builder()
            .newAllegationsOfHarmYesNo(c100RebuildSafetyConcernsElements.getHaveSafetyConcerns())
            .newAllegationsOfHarmDomesticAbuseYesNo(buildApplicantConcernAbout(whoConcernAboutList))
            .newAllegationsOfHarmChildAbuseYesNo(buildChildConcernAbout(whoConcernAboutList))
            .newAllegationsOfHarmSubstanceAbuseYesNo(c100RebuildSafetyConcernsElements.getC1AOtherConcernsDrugs())
            .newAllegationsOfHarmSubstanceAbuseDetails(isNotEmpty(c100RebuildSafetyConcernsElements.getC1AOtherConcernsDrugsDetails())
                                                           ? c100RebuildSafetyConcernsElements.getC1AOtherConcernsDrugsDetails() : null)
            .newAllegationsOfHarmOtherConcerns(c100RebuildSafetyConcernsElements.getC1AChildSafetyConcerns())
            .newAllegationsOfHarmOtherConcernsDetails(isNotEmpty(c100RebuildSafetyConcernsElements.getC1AChildSafetyConcernsDetails())
                                                          ? c100RebuildSafetyConcernsElements.getC1AChildSafetyConcernsDetails() : null)
            .newAllegationsOfHarmOtherConcernsCourtActions(c100RebuildSafetyConcernsElements.getC1AKeepingSafeStatement())

            .domesticBehaviours((c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getApplicant() != null)
                                    ? buildDomesticAbuseBehavioursDetails(c100RebuildSafetyConcernsElements) : null)

            .childPhysicalAbuse((c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null)
                                    ? buildChildAbuseDetails(c100RebuildSafetyConcernsElements,ChildAbuseEnum.physicalAbuse) : null)
            .allChildrenAreRiskPhysicalAbuse(
                (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null
                    && c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getPhysicalAbuse() != null)
                                             ? isAllChildrenAreRiskAbused(c100RebuildSafetyConcernsElements,
                                                                        childDetails,
                                                                        ChildAbuseEnum.physicalAbuse) : null)
            .whichChildrenAreRiskPhysicalAbuse(
                (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null
                    && c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getPhysicalAbuse() != null)
                    ? whichChildrenAreRiskAbuse(c100RebuildSafetyConcernsElements,
                                                childDetails, ChildAbuseEnum.physicalAbuse) : null)

            .childPsychologicalAbuse((c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null)
                                         ? buildChildAbuseDetails(c100RebuildSafetyConcernsElements,ChildAbuseEnum.psychologicalAbuse) : null)
            .allChildrenAreRiskPsychologicalAbuse(
                (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null
                    && c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getPsychologicalAbuse() != null)
                    ? isAllChildrenAreRiskAbused(c100RebuildSafetyConcernsElements,
                                                 childDetails,
                                                 ChildAbuseEnum.psychologicalAbuse) : null)
            .whichChildrenAreRiskPsychologicalAbuse(
                (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null
                    && c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getPsychologicalAbuse() != null)
                    ? whichChildrenAreRiskAbuse(c100RebuildSafetyConcernsElements,
                                                childDetails, ChildAbuseEnum.psychologicalAbuse) : null)

            .childSexualAbuse((c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null)
                                  ? buildChildAbuseDetails(c100RebuildSafetyConcernsElements,ChildAbuseEnum.sexualAbuse) : null)
            .allChildrenAreRiskSexualAbuse(
                (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null
                    && c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getSexualAbuse() != null)
                    ? isAllChildrenAreRiskAbused(c100RebuildSafetyConcernsElements,
                                                 childDetails,
                                                 ChildAbuseEnum.sexualAbuse) : null)
            .whichChildrenAreRiskSexualAbuse(
                (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null
                    && c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getSexualAbuse() != null)
                    ? whichChildrenAreRiskAbuse(c100RebuildSafetyConcernsElements,
                                                childDetails, ChildAbuseEnum.sexualAbuse) : null)


            .childEmotionalAbuse((c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null)
                                     ? buildChildAbuseDetails(c100RebuildSafetyConcernsElements,ChildAbuseEnum.emotionalAbuse) : null)
            .allChildrenAreRiskEmotionalAbuse(
                (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null
                    && c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getEmotionalAbuse() != null)
                    ? isAllChildrenAreRiskAbused(c100RebuildSafetyConcernsElements,
                                                 childDetails,
                                                 ChildAbuseEnum.emotionalAbuse) : null)
            .whichChildrenAreRiskEmotionalAbuse(
                (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null
                    && c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getEmotionalAbuse() != null)
                    ? whichChildrenAreRiskAbuse(c100RebuildSafetyConcernsElements,
                                                childDetails, ChildAbuseEnum.emotionalAbuse) : null)

            .childFinancialAbuse((c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null)
                                     ? buildChildAbuseDetails(c100RebuildSafetyConcernsElements,ChildAbuseEnum.financialAbuse) : null)
            .allChildrenAreRiskFinancialAbuse(
                (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null
                    && c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getFinancialAbuse() != null)
                    ? isAllChildrenAreRiskAbused(c100RebuildSafetyConcernsElements,
                                                 childDetails,
                                                 ChildAbuseEnum.financialAbuse) : null)
            .whichChildrenAreRiskFinancialAbuse(
                (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() != null
                    && c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getFinancialAbuse() != null)
                    ? whichChildrenAreRiskAbuse(c100RebuildSafetyConcernsElements,
                                                childDetails, ChildAbuseEnum.financialAbuse) : null)

            .newAllegationsOfHarmChildAbductionYesNo(buildChildAbduction(c1AConcernAboutChild))
            .newPreviousAbductionThreats(isNotEmpty(c100RebuildSafetyConcernsElements.getC1AChildAbductedBefore())
             ? Yes : YesOrNo.No)
            .newPreviousAbductionThreatsDetails(c100RebuildSafetyConcernsElements.getC1APreviousAbductionsShortDesc())
            .newChildrenLocationNow(c100RebuildSafetyConcernsElements.getC1AChildsCurrentLocation())
            .newAbductionPassportOfficeNotified(c100RebuildSafetyConcernsElements.getC1AAbductionPassportOfficeNotified())
            .newAbductionChildHasPassport(c100RebuildSafetyConcernsElements.getC1AAbductionPassportOfficeNotified())
            .newAbductionChildHasPassport(c100RebuildSafetyConcernsElements.getC1APassportOffice())
            .newAbductionPreviousPoliceInvolvement(c100RebuildSafetyConcernsElements.getC1APoliceOrInvestigatorInvolved())
            .newAbductionPreviousPoliceInvolvementDetails(c100RebuildSafetyConcernsElements.getC1APoliceOrInvestigatorOtherDetails())
            .newChildAbductionReasons(c100RebuildSafetyConcernsElements.getC1AAbductionReasonOutsideUk())

            .childPassportDetails((c100RebuildSafetyConcernsElements.getC1APossessionChildrenPassport() != null)
                                      ? buildChildPassportDetails(c100RebuildSafetyConcernsElements) : null)

            .newAgreeChildUnsupervisedTime((c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails() != null)
                                               ? buildChildUnSupervisedTime(c100RebuildSafetyConcernsElements) : null)

            .newAgreeChildSupervisedTime((c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails() != null)
                                               ? buildChildSupervisedTime(c100RebuildSafetyConcernsElements) : null)

            .newAgreeChildOtherContact(c100RebuildSafetyConcernsElements.getC1AAgreementOtherWaysDetails())
            .build();

    }

    private static YesOrNo buildChildSupervisedTime(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {

        if ("No".equalsIgnoreCase(c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails())) {
            return YesOrNo.No;
        } else if ("Yes".equalsIgnoreCase(c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails())
            || Supervised.equalsIgnoreCase(c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails())) {
            return YesOrNo.Yes;
        }

        return YesOrNo.No;
    }

    private static YesOrNo buildChildUnSupervisedTime(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {

        if ("No".equalsIgnoreCase(c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails())
            || Supervised.equalsIgnoreCase(c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails())) {
            return YesOrNo.No;
        } else if ("Yes".equalsIgnoreCase(c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails())) {
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

    private static ChildAbuse buildChildAbuseDetails(
        C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,ChildAbuseEnum abuseType) {
        ChildSafetyConcernsDto childAbuse = c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild();

        if (isNotEmpty(childAbuse.getPhysicalAbuse()) && abuseType.equals(ChildAbuseEnum.physicalAbuse)) {
            return mapToChildAbuseIndividually(ChildAbuseEnum.physicalAbuse, childAbuse.getPhysicalAbuse());
        }

        if (isNotEmpty(childAbuse.getPsychologicalAbuse()) && abuseType.equals(ChildAbuseEnum.psychologicalAbuse)) {
            return mapToChildAbuseIndividually(ChildAbuseEnum.psychologicalAbuse, childAbuse.getPsychologicalAbuse());
        }

        if (isNotEmpty(childAbuse.getSexualAbuse()) && abuseType.equals(ChildAbuseEnum.sexualAbuse)) {
            return mapToChildAbuseIndividually(ChildAbuseEnum.sexualAbuse, childAbuse.getSexualAbuse());
        }

        if (isNotEmpty(childAbuse.getEmotionalAbuse()) && abuseType.equals(ChildAbuseEnum.emotionalAbuse)) {
            return mapToChildAbuseIndividually(ChildAbuseEnum.emotionalAbuse, childAbuse.getEmotionalAbuse());
        }

        if (isNotEmpty(childAbuse.getFinancialAbuse()) && abuseType.equals(ChildAbuseEnum.financialAbuse)) {
            return mapToChildAbuseIndividually(ChildAbuseEnum.financialAbuse, childAbuse.getFinancialAbuse());
        }

        return null;

    }

    private static ChildAbuse mapToChildAbuseIndividually(ChildAbuseEnum abuseType, AbuseDto abuseDto) {

        return ChildAbuse.builder()
            .abuseNatureDescription(abuseDto.getBehaviourDetails())
            .typeOfAbuse(abuseType)
            .behavioursApplicantSoughtHelp(abuseDto.getSeekHelpFromPersonOrAgency())
            .behavioursStartDateAndLength(abuseDto.getBehaviourStartDate() + isBehaviourOngoing(abuseDto))
            .behavioursApplicantHelpSoughtWho(abuseDto.getSeekHelpDetails())
            .build();

    }

    private static YesOrNo isAllChildrenAreRiskAbused(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                             List<ChildDetail> childDetails,ChildAbuseEnum abuseType) {
        YesOrNo isAllChildrenAreRiskAbuse = YesOrNo.No;

        if (abuseType.equals(ChildAbuseEnum.physicalAbuse)) {
            String[] physicallyAbusedChildren = c100RebuildSafetyConcernsElements.getC100SafetyConcerns()
                .getChild().getPhysicalAbuse().getChildrenConcernedAbout();
            isAllChildrenAreRiskAbuse = isAllChildrenAreRiskAbuses(physicallyAbusedChildren, childDetails);
        }

        if (abuseType.equals(ChildAbuseEnum.psychologicalAbuse)) {
            String[] psychologicallyAbusedChildren = c100RebuildSafetyConcernsElements.getC100SafetyConcerns()
                .getChild().getPsychologicalAbuse().getChildrenConcernedAbout();
            isAllChildrenAreRiskAbuse = isAllChildrenAreRiskAbuses(psychologicallyAbusedChildren, childDetails);
        }

        if (abuseType.equals(ChildAbuseEnum.sexualAbuse)) {
            String[] sexuallyAbusedChildren = c100RebuildSafetyConcernsElements.getC100SafetyConcerns()
                .getChild().getSexualAbuse().getChildrenConcernedAbout();
            isAllChildrenAreRiskAbuse = isAllChildrenAreRiskAbuses(sexuallyAbusedChildren, childDetails);
        }

        if (abuseType.equals(ChildAbuseEnum.emotionalAbuse)) {
            String[] emotionallyAbusedChildren = c100RebuildSafetyConcernsElements.getC100SafetyConcerns()
                .getChild().getEmotionalAbuse().getChildrenConcernedAbout();
            isAllChildrenAreRiskAbuse = isAllChildrenAreRiskAbuses(emotionallyAbusedChildren, childDetails);
        }

        if (abuseType.equals(ChildAbuseEnum.financialAbuse)) {
            String[] financiallyAbusesChildren = c100RebuildSafetyConcernsElements.getC100SafetyConcerns()
                .getChild().getFinancialAbuse().getChildrenConcernedAbout();
            isAllChildrenAreRiskAbuse = isAllChildrenAreRiskAbuses(financiallyAbusesChildren, childDetails);
        }

        return isAllChildrenAreRiskAbuse;

    }

    private static YesOrNo isAllChildrenAreRiskAbuses(String[] abusedChildren, List<ChildDetail> childDetails) {
        YesOrNo isAllChildrenAreRiskAbuse = YesOrNo.No;
        if (abusedChildren.length == childDetails.size()) {
            isAllChildrenAreRiskAbuse = YesOrNo.Yes;
        }
        return isAllChildrenAreRiskAbuse;
    }

    private static DynamicMultiSelectList whichChildrenAreRiskAbuse(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                                            List<ChildDetail> childDetails, ChildAbuseEnum abuseType) {
        DynamicMultiSelectList whichChildrenAreRiskAbuse = null;

        if (abuseType.equals(ChildAbuseEnum.physicalAbuse)) {
            String[] physicallyAbusedChildren = c100RebuildSafetyConcernsElements.getC100SafetyConcerns()
                .getChild().getPhysicalAbuse().getChildrenConcernedAbout();
            whichChildrenAreRiskAbuse = buildWhichChildrenAreRiskAbuses(physicallyAbusedChildren, childDetails);
        }

        if (abuseType.equals(ChildAbuseEnum.psychologicalAbuse)) {
            String[] psychologicallyAbusedChildren = c100RebuildSafetyConcernsElements.getC100SafetyConcerns()
                .getChild().getPsychologicalAbuse().getChildrenConcernedAbout();
            whichChildrenAreRiskAbuse = buildWhichChildrenAreRiskAbuses(psychologicallyAbusedChildren, childDetails);
        }

        if (abuseType.equals(ChildAbuseEnum.sexualAbuse)) {
            String[] sexuallyAbusedChildren = c100RebuildSafetyConcernsElements.getC100SafetyConcerns()
                .getChild().getSexualAbuse().getChildrenConcernedAbout();
            whichChildrenAreRiskAbuse = buildWhichChildrenAreRiskAbuses(sexuallyAbusedChildren, childDetails);
        }

        if (abuseType.equals(ChildAbuseEnum.emotionalAbuse)) {
            String[] emotionallyAbusedChildren = c100RebuildSafetyConcernsElements.getC100SafetyConcerns()
                .getChild().getEmotionalAbuse().getChildrenConcernedAbout();
            whichChildrenAreRiskAbuse = buildWhichChildrenAreRiskAbuses(emotionallyAbusedChildren, childDetails);
        }

        if (abuseType.equals(ChildAbuseEnum.financialAbuse)) {
            String[] financiallyAbusedChildren = c100RebuildSafetyConcernsElements.getC100SafetyConcerns()
                .getChild().getFinancialAbuse().getChildrenConcernedAbout();
            whichChildrenAreRiskAbuse = buildWhichChildrenAreRiskAbuses(financiallyAbusedChildren, childDetails);
        }

        return whichChildrenAreRiskAbuse;

    }


    private static  DynamicMultiSelectList buildWhichChildrenAreRiskAbuses(String[] abusedChildren, List<ChildDetail> childDetails) {

        if (abusedChildren.length != childDetails.size()) {
            List<DynamicMultiselectListElement> valueElements = new ArrayList<>();
            List<DynamicMultiselectListElement> listItemsElements = new ArrayList<>();
            childDetails.forEach(s -> {
                boolean contains = Arrays.stream(abusedChildren).anyMatch(s.getId()::equals);
                if (contains) {
                    valueElements.add(DynamicMultiselectListElement.builder()
                                          .code(s.getId()).label(s.getFirstName()
                                                                     + EMPTY_SPACE_STRING + s.getLastName()).build());
                }
                listItemsElements.add(DynamicMultiselectListElement.builder()
                                          .code(s.getId()).label(s.getFirstName()
                                                                     + EMPTY_SPACE_STRING  + s.getLastName()).build());
            });
            return DynamicMultiSelectList.builder().value(valueElements).listItems(listItemsElements).build();
        }
        return null;
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

    private static String isBehaviourOngoing(AbuseDto abuseDto) {
        return abuseDto.getIsOngoingBehaviour().equals(Yes) ? " - Behaviour is ongoing" : " - Behaviour is not ongoing";
    }

    private static Element<DomesticAbuseBehaviours> mapToDomesticAbuse(TypeOfAbuseEnum typeOfAbuseEnum, AbuseDto abuseDto) {

        return Element.<DomesticAbuseBehaviours>builder().value(DomesticAbuseBehaviours.builder()
                                                                    .typeOfAbuse(typeOfAbuseEnum)
                                                                    .newAbuseNatureDescription(abuseDto.getBehaviourDetails())
                                                                    .newBehavioursApplicantSoughtHelp(abuseDto.getSeekHelpFromPersonOrAgency())
                                                                    .newBehavioursStartDateAndLength(abuseDto.getBehaviourStartDate()
                                                                                                         + isBehaviourOngoing(abuseDto))
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
