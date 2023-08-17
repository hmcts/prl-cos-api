package uk.gov.hmcts.reform.prl.mapper.citizen;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.NewPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.AbuseDto;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ApplicantSafteConcernDto;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildChildDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildSafetyConcernsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildDetail;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildSafetyConcernsDto;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.DomesticAbuseBehaviours;
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

    public static final String HYPHEN_SEPARATOR = " - ";

    private CaseDataSafetyConcernsElementsMapper() {
    }

    public static void updateSafetyConcernsElementsForCaseData(CaseData.CaseDataBuilder caseDataBuilder,
                                                               C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                               C100RebuildChildDetailsElements c100RebuildChildDetailsElements) {
        AllegationOfHarmRevised allegationOfHarmRevised = AllegationOfHarmRevised.builder().build();

        if (YesOrNo.No.equals(c100RebuildSafetyConcernsElements.getHaveSafetyConcerns())) {
            allegationOfHarmRevised = allegationOfHarmRevised.toBuilder()
                .newAllegationsOfHarmYesNo(c100RebuildSafetyConcernsElements.getHaveSafetyConcerns())
                .build();
        } else {
            allegationOfHarmRevised = buildAohBasics(c100RebuildSafetyConcernsElements, allegationOfHarmRevised);
            allegationOfHarmRevised = buildAohSubstancesAndDrugs(c100RebuildSafetyConcernsElements, allegationOfHarmRevised);
            allegationOfHarmRevised = buildAohDomesticAbuses(c100RebuildSafetyConcernsElements, allegationOfHarmRevised);
            allegationOfHarmRevised = buildAohChildAbuses(c100RebuildSafetyConcernsElements,
                                                          c100RebuildChildDetailsElements, allegationOfHarmRevised);
            allegationOfHarmRevised = buildAohAbduction(c100RebuildSafetyConcernsElements, allegationOfHarmRevised);
        }
        caseDataBuilder.allegationOfHarmRevised(allegationOfHarmRevised);

    }

    private static AllegationOfHarmRevised buildAohBasics(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                                           AllegationOfHarmRevised allegationOfHarmRevised) {

        List<String> whoConcernAboutList = Arrays.stream(c100RebuildSafetyConcernsElements.getWhoConcernAbout())
            .collect(Collectors.toList());

        return allegationOfHarmRevised
            .toBuilder()
            .newAllegationsOfHarmYesNo(c100RebuildSafetyConcernsElements.getHaveSafetyConcerns())
            .newAllegationsOfHarmDomesticAbuseYesNo(buildConcernAbout(whoConcernAboutList, Applicant))
            .newAllegationsOfHarmChildAbuseYesNo(buildConcernAbout(whoConcernAboutList, Children))
            .build();

    }

    private static AllegationOfHarmRevised buildAohSubstancesAndDrugs(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                                              AllegationOfHarmRevised allegationOfHarmRevised) {

        return allegationOfHarmRevised.toBuilder()
            .newAllegationsOfHarmSubstanceAbuseYesNo(c100RebuildSafetyConcernsElements.getC1AOtherConcernsDrugs())
            .newAllegationsOfHarmSubstanceAbuseDetails(isNotEmpty(c100RebuildSafetyConcernsElements.getC1AOtherConcernsDrugsDetails())
                                                           ? c100RebuildSafetyConcernsElements.getC1AOtherConcernsDrugsDetails() : null)

            .newAllegationsOfHarmOtherConcerns(c100RebuildSafetyConcernsElements.getC1AChildSafetyConcerns())
            .newAllegationsOfHarmOtherConcernsDetails(isNotEmpty(c100RebuildSafetyConcernsElements.getC1AChildSafetyConcernsDetails())
                                                          ? c100RebuildSafetyConcernsElements.getC1AChildSafetyConcernsDetails() : null)

            .newAllegationsOfHarmOtherConcernsCourtActions(isNotEmpty(c100RebuildSafetyConcernsElements.getC1AKeepingSafeStatement())
                                                               ? c100RebuildSafetyConcernsElements.getC1AKeepingSafeStatement() : null)
            .newAgreeChildUnsupervisedTime((c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails() != null)

                                               ? buildChildUnSupervisedTime(c100RebuildSafetyConcernsElements) : null)

            .newAgreeChildSupervisedTime((c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails() != null)
                                             ? buildChildSupervisedTime(c100RebuildSafetyConcernsElements) : null)
            .build();

    }

    private static AllegationOfHarmRevised buildAohDomesticAbuses(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                                      AllegationOfHarmRevised allegationOfHarmRevised) {

        log.info("first---->  {}", allegationOfHarmRevised.getNewAllegationsOfHarmDomesticAbuseYesNo());
        log.info("second---->  {}", c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getApplicant() == null);

        if (YesOrNo.No.equals(allegationOfHarmRevised.getNewAllegationsOfHarmDomesticAbuseYesNo())
            && c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getApplicant() == null) {
            return allegationOfHarmRevised;
        }

        log.info("DO DOMESTIc");
        return allegationOfHarmRevised.toBuilder()
            .domesticBehaviours(buildDomesticAbuseBehavioursDetails(c100RebuildSafetyConcernsElements))
            .build();
    }



    private static AllegationOfHarmRevised buildAohChildAbuses(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                               C100RebuildChildDetailsElements c100RebuildChildDetailsElements,
                                                               AllegationOfHarmRevised allegationOfHarmRevised) {

        if (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() == null) {
            return allegationOfHarmRevised;
        }

        ChildSafetyConcernsDto childAbuse = c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild();

        if (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getPhysicalAbuse() != null) {
            allegationOfHarmRevised = buildAohChildPhysicalAbuseDetails(c100RebuildSafetyConcernsElements,
                                                                        c100RebuildChildDetailsElements,
                                                                        childAbuse,
                                                                        allegationOfHarmRevised);
        }

        if (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getPsychologicalAbuse() != null) {
            allegationOfHarmRevised = buildAohChildPsychologicalAbuseDetails(c100RebuildSafetyConcernsElements,
                                                                             c100RebuildChildDetailsElements,
                                                                             childAbuse,
                                                                             allegationOfHarmRevised);
        }

        if (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getSexualAbuse() != null) {
            allegationOfHarmRevised = buildAohChildSexualAbuseDetails(c100RebuildSafetyConcernsElements,
                                                                      c100RebuildChildDetailsElements,
                                                                      childAbuse,
                                                                      allegationOfHarmRevised);
        }

        if (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getEmotionalAbuse() != null) {
            allegationOfHarmRevised = buildAohChildEmotionalAbuseDetails(c100RebuildSafetyConcernsElements,
                                                                         c100RebuildChildDetailsElements,
                                                                         childAbuse,
                                                                         allegationOfHarmRevised);
        }

        if (c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild().getFinancialAbuse() != null) {
            allegationOfHarmRevised = buildAohChildFinancialAbuseDetails(c100RebuildSafetyConcernsElements,
                                                                         c100RebuildChildDetailsElements,
                                                                         childAbuse,
                                                                         allegationOfHarmRevised);
        }

        return allegationOfHarmRevised;
    }

    private static AllegationOfHarmRevised buildAohChildPhysicalAbuseDetails(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                                             C100RebuildChildDetailsElements c100RebuildChildDetailsElements,
                                                                             ChildSafetyConcernsDto childAbuse,
                                                                             AllegationOfHarmRevised allegationOfHarmRevised) {

        String[] physicallyAbusedChildren = c100RebuildSafetyConcernsElements
            .getC100SafetyConcerns().getChild()
            .getPhysicalAbuse().getChildrenConcernedAbout();

        return allegationOfHarmRevised.toBuilder()
            .childPhysicalAbuse(mapToChildAbuseIndividually(ChildAbuseEnum.physicalAbuse,childAbuse.getPhysicalAbuse()))
            .allChildrenAreRiskPhysicalAbuse(isAllChildrenAreRiskAbuses(physicallyAbusedChildren,
                                                                        c100RebuildChildDetailsElements))
            .whichChildrenAreRiskPhysicalAbuse(buildWhichChildrenAreRiskAbuses(physicallyAbusedChildren, c100RebuildChildDetailsElements))
            .build();

    }

    private static AllegationOfHarmRevised buildAohChildPsychologicalAbuseDetails(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                                             C100RebuildChildDetailsElements c100RebuildChildDetailsElements,
                                                                             ChildSafetyConcernsDto childAbuse,
                                                                             AllegationOfHarmRevised allegationOfHarmRevised) {

        String[] psychologicallyAbusedChildren = c100RebuildSafetyConcernsElements
            .getC100SafetyConcerns().getChild()
            .getPsychologicalAbuse().getChildrenConcernedAbout();

        return allegationOfHarmRevised.toBuilder()
            .childPsychologicalAbuse(mapToChildAbuseIndividually(ChildAbuseEnum.psychologicalAbuse,childAbuse.getPsychologicalAbuse()))
            .allChildrenAreRiskPsychologicalAbuse(isAllChildrenAreRiskAbuses(psychologicallyAbusedChildren,
                                                                        c100RebuildChildDetailsElements))
            .whichChildrenAreRiskPsychologicalAbuse(buildWhichChildrenAreRiskAbuses(psychologicallyAbusedChildren,
                                                                         c100RebuildChildDetailsElements))
            .build();

    }

    private static AllegationOfHarmRevised buildAohChildSexualAbuseDetails(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                                                  C100RebuildChildDetailsElements c100RebuildChildDetailsElements,
                                                                                  ChildSafetyConcernsDto childAbuse,
                                                                                  AllegationOfHarmRevised allegationOfHarmRevised) {

        String[] sexuallyAbusedChildren = c100RebuildSafetyConcernsElements
            .getC100SafetyConcerns().getChild().getSexualAbuse()
            .getChildrenConcernedAbout();

        return allegationOfHarmRevised.toBuilder()
            .childSexualAbuse(mapToChildAbuseIndividually(ChildAbuseEnum.sexualAbuse,childAbuse.getSexualAbuse()))
            .allChildrenAreRiskSexualAbuse(isAllChildrenAreRiskAbuses(sexuallyAbusedChildren,
                                                                             c100RebuildChildDetailsElements))
            .whichChildrenAreRiskSexualAbuse(buildWhichChildrenAreRiskAbuses(sexuallyAbusedChildren,
                                                                              c100RebuildChildDetailsElements))
            .build();

    }

    private static AllegationOfHarmRevised buildAohChildEmotionalAbuseDetails(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                                           C100RebuildChildDetailsElements c100RebuildChildDetailsElements,
                                                                           ChildSafetyConcernsDto childAbuse,
                                                                           AllegationOfHarmRevised allegationOfHarmRevised) {

        String[] emotionallyAbusedChildren = c100RebuildSafetyConcernsElements
            .getC100SafetyConcerns().getChild()
            .getEmotionalAbuse().getChildrenConcernedAbout();

        return allegationOfHarmRevised.toBuilder()
            .childEmotionalAbuse(mapToChildAbuseIndividually(ChildAbuseEnum.emotionalAbuse,childAbuse.getEmotionalAbuse()))
            .allChildrenAreRiskEmotionalAbuse(isAllChildrenAreRiskAbuses(emotionallyAbusedChildren,
                                                                             c100RebuildChildDetailsElements))
            .whichChildrenAreRiskEmotionalAbuse(buildWhichChildrenAreRiskAbuses(emotionallyAbusedChildren,
                                                                              c100RebuildChildDetailsElements))
            .build();


    }

    private static AllegationOfHarmRevised buildAohChildFinancialAbuseDetails(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                                           C100RebuildChildDetailsElements c100RebuildChildDetailsElements,
                                                                           ChildSafetyConcernsDto childAbuse,
                                                                           AllegationOfHarmRevised allegationOfHarmRevised) {

        String[] financiallyAbusedChildren = c100RebuildSafetyConcernsElements
            .getC100SafetyConcerns().getChild()
            .getFinancialAbuse().getChildrenConcernedAbout();

        return allegationOfHarmRevised.toBuilder()
            .childFinancialAbuse(mapToChildAbuseIndividually(ChildAbuseEnum.financialAbuse,childAbuse.getFinancialAbuse()))
            .allChildrenAreRiskFinancialAbuse(isAllChildrenAreRiskAbuses(financiallyAbusedChildren,
                                                                             c100RebuildChildDetailsElements))
            .whichChildrenAreRiskFinancialAbuse(buildWhichChildrenAreRiskAbuses(financiallyAbusedChildren,
                                                                              c100RebuildChildDetailsElements))
            .build();
    }

    private static AllegationOfHarmRevised buildAohAbduction(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                                  AllegationOfHarmRevised allegationOfHarmRevised) {

        List<String> c1AConcernAboutChild = Arrays.stream(c100RebuildSafetyConcernsElements.getC1AConcernAboutChild())
            .collect(Collectors.toList());

        if (YesOrNo.No.equals(buildChildAbduction(c1AConcernAboutChild))) {
            return allegationOfHarmRevised.toBuilder()
                .newAllegationsOfHarmChildAbductionYesNo(buildChildAbduction(c1AConcernAboutChild))
                .build();
        } else {
            allegationOfHarmRevised = allegationOfHarmRevised.toBuilder()
                .newAllegationsOfHarmChildAbductionYesNo(buildChildAbduction(c1AConcernAboutChild))
                .newChildAbductionReasons(c100RebuildSafetyConcernsElements.getC1AAbductionReasonOutsideUk())
                .newChildrenLocationNow(c100RebuildSafetyConcernsElements.getC1AChildsCurrentLocation())
                .newAbductionChildHasPassport(c100RebuildSafetyConcernsElements.getC1APassportOffice())
                .newPreviousAbductionThreats(isNotEmpty(c100RebuildSafetyConcernsElements.getC1AChildAbductedBefore())
                                                 ? Yes : YesOrNo.No)
                .newPreviousAbductionThreatsDetails(c100RebuildSafetyConcernsElements.getC1APreviousAbductionsShortDesc())
                .newAbductionPreviousPoliceInvolvement(c100RebuildSafetyConcernsElements.getC1APoliceOrInvestigatorInvolved())
                .newAbductionPreviousPoliceInvolvementDetails(c100RebuildSafetyConcernsElements.getC1APoliceOrInvestigatorOtherDetails())
                .newAgreeChildOtherContact(c100RebuildSafetyConcernsElements.getC1AAgreementOtherWaysDetails())
                .build();

            if (Yes.equals(allegationOfHarmRevised.getNewAbductionChildHasPassport())) {
                allegationOfHarmRevised = allegationOfHarmRevised.toBuilder()
                    .childPassportDetails((c100RebuildSafetyConcernsElements.getC1APossessionChildrenPassport() != null)
                                              ? buildChildPassportDetails(c100RebuildSafetyConcernsElements) : null)//
                    .newAbductionPassportOfficeNotified(c100RebuildSafetyConcernsElements.getC1AAbductionPassportOfficeNotified())
                    .build();
            }

            return allegationOfHarmRevised;
        }
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
            if (("mother").equalsIgnoreCase(possession)) {
                possessionChildrenPassport.add(NewPassportPossessionEnum.mother);
            }
            if (("father").equalsIgnoreCase(possession)) {
                possessionChildrenPassport.add(NewPassportPossessionEnum.father);
            }
            if (("other").equalsIgnoreCase(possession)) {
                possessionChildrenPassport.add(NewPassportPossessionEnum.otherPerson);

            }
        }
        return ChildPassportDetails.builder().newChildPassportPossession(possessionChildrenPassport)
            .newChildPassportPossessionOtherDetails(isNotEmpty(c100RebuildSafetyConcernsElements.getC1AProvideOtherDetails())
             ? c100RebuildSafetyConcernsElements.getC1AProvideOtherDetails() : null)
            .newChildHasMultiplePassports(c100RebuildSafetyConcernsElements.getC1AChildrenMoreThanOnePassport())
            .build();

    }

    private static List<Element<DomesticAbuseBehaviours>> buildDomesticAbuseBehavioursDetails(
        C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {
        List<Element<DomesticAbuseBehaviours>> applicantElements = new ArrayList<>();
        ApplicantSafteConcernDto applicantAbuse = c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getApplicant();

        if (isNotEmpty(applicantAbuse.getPhysicalAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1, applicantAbuse.getPhysicalAbuse()));
        }
        if (isNotEmpty(applicantAbuse.getPsychologicalAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_2, applicantAbuse.getPsychologicalAbuse()));
        }
        if (isNotEmpty(applicantAbuse.getSexualAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_3, applicantAbuse.getSexualAbuse()));
        }
        if (isNotEmpty(applicantAbuse.getEmotionalAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_4, applicantAbuse.getEmotionalAbuse()));
        }
        if (isNotEmpty(applicantAbuse.getFinancialAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_5, applicantAbuse.getFinancialAbuse()));
        }
        if (isNotEmpty(applicantAbuse.getSomethingElse())) {
            applicantElements.add(mapToDomesticAbuse(null, applicantAbuse.getSomethingElse()));
        }

        return applicantElements;

    }

    private static ChildAbuse mapToChildAbuseIndividually(ChildAbuseEnum abuseType, AbuseDto abuseDto) {

        return ChildAbuse.builder()
            .abuseNatureDescription(abuseDto.getBehaviourDetails())
            .typeOfAbuse(abuseType)
            .behavioursApplicantSoughtHelp(abuseDto.getSeekHelpFromPersonOrAgency())
            .behavioursStartDateAndLength(buildBehavioursStartDateAndLength(abuseDto))
            .behavioursApplicantHelpSoughtWho(abuseDto.getSeekHelpDetails())
            .build();

    }

    private static String buildBehavioursStartDateAndLength(AbuseDto abuseDto) {

        if (isNotEmpty(abuseDto.getBehaviourStartDate())) {
            if (isNotEmpty(abuseDto.getIsOngoingBehaviour())) {
                return abuseDto.getBehaviourStartDate() + HYPHEN_SEPARATOR + isBehaviourOngoing(abuseDto);
            }
        } else {
            if (isNotEmpty(abuseDto.getIsOngoingBehaviour())) {
                return isBehaviourOngoing(abuseDto);
            }
        }
        return abuseDto.getBehaviourStartDate();
    }

    private static YesOrNo isAllChildrenAreRiskAbuses(String[] abusedChildren, C100RebuildChildDetailsElements c100RebuildChildDetailsElements) {
        YesOrNo isAllChildrenAreRiskAbuse = YesOrNo.No;
        List<ChildDetail> childDetails =  c100RebuildChildDetailsElements.getChildDetails();

        if (childDetails != null && abusedChildren.length == childDetails.size()) {
            isAllChildrenAreRiskAbuse = YesOrNo.Yes;
        }
        return isAllChildrenAreRiskAbuse;
    }

    private static  DynamicMultiSelectList buildWhichChildrenAreRiskAbuses(String[] abusedChildren,
                                                                           C100RebuildChildDetailsElements c100RebuildChildDetailsElements) {

        List<ChildDetail> childDetails = c100RebuildChildDetailsElements.getChildDetails();

        if (childDetails != null && abusedChildren.length != childDetails.size()) {
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


    private static String isBehaviourOngoing(AbuseDto abuseDto) {
        return abuseDto.getIsOngoingBehaviour().equals(Yes) ? "Behaviour is ongoing" : "Behaviour is not ongoing";
    }

    private static Element<DomesticAbuseBehaviours> mapToDomesticAbuse(TypeOfAbuseEnum typeOfAbuseEnum, AbuseDto abuseDto) {

        return Element.<DomesticAbuseBehaviours>builder().value(DomesticAbuseBehaviours.builder()
                                                                    .typeOfAbuse(typeOfAbuseEnum)
                                                                    .newAbuseNatureDescription(abuseDto.getBehaviourDetails())
                                                                    .newBehavioursApplicantSoughtHelp(abuseDto.getSeekHelpFromPersonOrAgency())
                                                                    .newBehavioursStartDateAndLength(buildBehavioursStartDateAndLength(abuseDto))
                                                                    .newBehavioursApplicantHelpSoughtWho(abuseDto.getSeekHelpDetails())
                                                                    .build()).build();
    }

    private static YesOrNo buildConcernAbout(List<String> whoConcernsAboutList, String typeOfCitizen) {
        if (Applicant.equalsIgnoreCase(typeOfCitizen) && whoConcernsAboutList.contains(Applicant)) {
            return YesOrNo.Yes;
        } else if (Children.equalsIgnoreCase(typeOfCitizen) && whoConcernsAboutList.contains(Children)) {
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
