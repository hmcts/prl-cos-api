package uk.gov.hmcts.reform.prl.mapper.citizen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.ChildAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.RespPassportPossessionEnum;
import uk.gov.hmcts.reform.prl.enums.TypeOfAbuseEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.AbuseDto;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ApplicantSafteConcernDto;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildSafetyConcernsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildSafetyConcernsDto;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiSelectList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicMultiselectListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.RespChildAbuse;
import uk.gov.hmcts.reform.prl.models.complextypes.RespDomesticAbuseBehaviours;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.RespondentAllegationsOfHarmData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.RespChildPassportDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.EMPTY_SPACE_STRING;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CitizenRespondentAohElementsMapper {

    private static final String RESPONDENT = "respondent";
    private static final String CHILDREN = "children";

    private static final String MOTHER = "mother";

    private static final String FATHER = "father";

    private static final String OTHER = "other";

    private static final String CHILD_ABDUCTION = "abduction";

    private static final String WITNESSING_DOMESTIC_ABUSE = "witnessingDomesticAbuse";


    private static final String SUPERVISED = "Yes, but I prefer that it is supervised";

    public static final String HYPHEN_SEPARATOR = " - ";

    private static final String ONGOING = "Behaviour is ongoing";

    private static final String NOT_ONGOING = "Behaviour is not ongoing";

    public RespondentAllegationsOfHarmData map(String aohData, List<Element<ChildDetailsRevised>> childDetails) {
        if (isNotEmpty(aohData)) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
            // C100 rebuild object reused here as same properties are shared.
            C100RebuildSafetyConcernsElements c100C100RebuildSafetyConcernsElements = null;
            try {
                c100C100RebuildSafetyConcernsElements = mapper
                    .readValue(aohData, C100RebuildSafetyConcernsElements.class);
                return updateRespondentAohElementsForCaseData(c100C100RebuildSafetyConcernsElements,
                                                              childDetails);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse json request {}", e.getMessage());
            }
        }
        return RespondentAllegationsOfHarmData.builder().build();
    }

    private RespondentAllegationsOfHarmData updateRespondentAohElementsForCaseData(
        C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
        List<Element<ChildDetailsRevised>> newChildDetails) {
        RespondentAllegationsOfHarmData respondentAllegationsOfHarmData = RespondentAllegationsOfHarmData.builder().build();

        if (YesOrNo.No.equals(c100RebuildSafetyConcernsElements.getHaveSafetyConcerns())) {
            respondentAllegationsOfHarmData = respondentAllegationsOfHarmData.toBuilder()
                .respAohYesOrNo(c100RebuildSafetyConcernsElements.getHaveSafetyConcerns())
                .build();
        } else {
            respondentAllegationsOfHarmData = buildAohBasics(c100RebuildSafetyConcernsElements, respondentAllegationsOfHarmData);
            respondentAllegationsOfHarmData = buildAohSubstancesAndDrugs(c100RebuildSafetyConcernsElements, respondentAllegationsOfHarmData);
            respondentAllegationsOfHarmData = buildAohDomesticAbuses(c100RebuildSafetyConcernsElements, respondentAllegationsOfHarmData);
            respondentAllegationsOfHarmData = buildAohChildAbuses(c100RebuildSafetyConcernsElements,
                                                          newChildDetails, respondentAllegationsOfHarmData);
            respondentAllegationsOfHarmData = buildAohAbduction(c100RebuildSafetyConcernsElements, respondentAllegationsOfHarmData);
        }
        log.info("allegation of harm data {}", respondentAllegationsOfHarmData);
        return respondentAllegationsOfHarmData;
    }

    private RespondentAllegationsOfHarmData buildAohBasics(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                          RespondentAllegationsOfHarmData allegationOfHarmRevised) {

        List<String> whoConcernAboutList = Arrays.stream(c100RebuildSafetyConcernsElements.getWhoConcernAbout()).toList();
        List<String> c1AConcernAboutChild = Arrays.stream(c100RebuildSafetyConcernsElements.getC1AConcernAboutChild()).toList();
        return allegationOfHarmRevised
            .toBuilder()
            .respAohYesOrNo(c100RebuildSafetyConcernsElements.getHaveSafetyConcerns())
            .respAohDomesticAbuseYesNo(isDomesticAbuse(whoConcernAboutList,c1AConcernAboutChild))
            //It will always be Yes as child abuses present for both applicant child flow.
            .respAohChildAbuseYesNo(Yes)
            .build();

    }

    private YesOrNo isDomesticAbuse(List<String> whoConcernAboutList, List<String> c1AConcernAboutChild) {

        if (Yes.equals(buildConcernAbout(whoConcernAboutList, RESPONDENT))
            || Yes.equals(checkDomesticAbuse(c1AConcernAboutChild))) {
            return Yes;
        }
        return YesOrNo.No;
    }

    private RespondentAllegationsOfHarmData buildAohSubstancesAndDrugs(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                                      RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        return respondentAllegationsOfHarmData.toBuilder()
            .respAohSubstanceAbuseYesNo(c100RebuildSafetyConcernsElements.getC1AOtherConcernsDrugs())
            .respAohSubstanceAbuseDetails(ObjectUtils.isNotEmpty(c100RebuildSafetyConcernsElements.getC1AOtherConcernsDrugsDetails())
                                                           ? c100RebuildSafetyConcernsElements.getC1AOtherConcernsDrugsDetails() : null)

            .respAohOtherConcerns(c100RebuildSafetyConcernsElements.getC1AChildSafetyConcerns())
            .respAohOtherConcernsDetails(ObjectUtils.isNotEmpty(c100RebuildSafetyConcernsElements.getC1AChildSafetyConcernsDetails())
                                                          ? c100RebuildSafetyConcernsElements.getC1AChildSafetyConcernsDetails() : null)

            .respAohOtherConcernsCourtActions(ObjectUtils.isNotEmpty(c100RebuildSafetyConcernsElements.getC1AKeepingSafeStatement())
                                                               ? c100RebuildSafetyConcernsElements.getC1AKeepingSafeStatement() : null)
            .respAgreeChildUnsupervisedTime((c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails() != null)

                                               ? buildChildUnSupervisedTime(c100RebuildSafetyConcernsElements) : null)

            .respAgreeChildSupervisedTime((c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails() != null)
                                             ? buildChildSupervisedTime(c100RebuildSafetyConcernsElements) : null)
            .build();

    }

    private RespondentAllegationsOfHarmData buildAohDomesticAbuses(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                                  RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        if (YesOrNo.No.equals(respondentAllegationsOfHarmData.getRespAohDomesticAbuseYesNo())
            || c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getApplicant() == null) {
            return respondentAllegationsOfHarmData;
        }

        return respondentAllegationsOfHarmData.toBuilder()
            .respDomesticBehaviours(buildDomesticAbuseBehavioursDetails(c100RebuildSafetyConcernsElements))
            .build();
    }



    private RespondentAllegationsOfHarmData buildAohChildAbuses(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                               List<Element<ChildDetailsRevised>> newChildDetails,
                                                               RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        if (YesOrNo.No.equals(respondentAllegationsOfHarmData.getRespAohChildAbuseYesNo())
            || c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild() == null) {
            return respondentAllegationsOfHarmData;
        }

        if (null != c100RebuildSafetyConcernsElements.getC1AConcernAboutChild()) {
            respondentAllegationsOfHarmData = respondentAllegationsOfHarmData.toBuilder()
                .respChildAbuses(getChildAbuses(c100RebuildSafetyConcernsElements.getC1AConcernAboutChild()))
                .build();
        }

        ChildSafetyConcernsDto childAbuse = c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getChild();

        if (childAbuse.getPhysicalAbuse() != null) {
            respondentAllegationsOfHarmData = buildAohChildPhysicalAbuseDetails(
                childAbuse.getPhysicalAbuse(), newChildDetails,
                respondentAllegationsOfHarmData);
        }

        if (childAbuse.getPsychologicalAbuse() != null) {
            respondentAllegationsOfHarmData = buildAohChildPsychologicalAbuseDetails(
                childAbuse.getPsychologicalAbuse(), newChildDetails,
                respondentAllegationsOfHarmData);
        }

        if (childAbuse.getSexualAbuse() != null) {
            respondentAllegationsOfHarmData = buildAohChildSexualAbuseDetails(
                childAbuse.getSexualAbuse(), newChildDetails,
                respondentAllegationsOfHarmData);
        }

        if (childAbuse.getEmotionalAbuse() != null) {
            respondentAllegationsOfHarmData = buildAohChildEmotionalAbuseDetails(
                childAbuse.getEmotionalAbuse(), newChildDetails,
                respondentAllegationsOfHarmData);
        }

        if (childAbuse.getFinancialAbuse() != null) {
            respondentAllegationsOfHarmData = buildAohChildFinancialAbuseDetails(
                childAbuse.getFinancialAbuse(), newChildDetails,
                respondentAllegationsOfHarmData);
        }

        return respondentAllegationsOfHarmData;
    }

    private List<ChildAbuseEnum> getChildAbuses(String[] citizenChildAbuses) {
        return Arrays.stream(citizenChildAbuses)
            .map(abuse -> {
                if (ChildAbuseEnum.physicalAbuse.getId().equals(abuse)) {
                    return ChildAbuseEnum.physicalAbuse;
                } else if (ChildAbuseEnum.psychologicalAbuse.getId().equals(abuse)) {
                    return ChildAbuseEnum.psychologicalAbuse;
                } else if (ChildAbuseEnum.sexualAbuse.getId().equals(abuse)) {
                    return ChildAbuseEnum.sexualAbuse;
                } else if (ChildAbuseEnum.emotionalAbuse.getId().equals(abuse)) {
                    return ChildAbuseEnum.emotionalAbuse;
                } else if (ChildAbuseEnum.financialAbuse.getId().equals(abuse)) {
                    return ChildAbuseEnum.financialAbuse;
                }
                return null;
            })
            .filter(Objects::nonNull)
            .toList();
    }

    private RespondentAllegationsOfHarmData buildAohChildPhysicalAbuseDetails(AbuseDto physicalAbuse,
                                                                             List<Element<ChildDetailsRevised>> newChildDetails,
                                                                             RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        String[] physicallyAbusedChildren = physicalAbuse.getChildrenConcernedAbout();

        return respondentAllegationsOfHarmData.toBuilder()
            .respChildPhysicalAbuse(mapToChildAbuseIndividually(ChildAbuseEnum.physicalAbuse,physicalAbuse))
            .respAllChildrenAreRiskPhysicalAbuse(isAllChildrenAreRiskAbuses(physicallyAbusedChildren,
                                                                            newChildDetails))
            .respWhichChildrenAreRiskPhysicalAbuse(buildWhichChildrenAreRiskAbuses(physicallyAbusedChildren, newChildDetails))
            .build();

    }

    private RespondentAllegationsOfHarmData buildAohChildPsychologicalAbuseDetails(AbuseDto psychologicalAbuse,
                                                                                   List<Element<ChildDetailsRevised>> newChildDetails,
                                                                                  RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        String[] psychologicallyAbusedChildren = psychologicalAbuse.getChildrenConcernedAbout();

        return respondentAllegationsOfHarmData.toBuilder()
            .respChildPsychologicalAbuse(mapToChildAbuseIndividually(ChildAbuseEnum.psychologicalAbuse,psychologicalAbuse))
            .respAllChildrenAreRiskPsychologicalAbuse(isAllChildrenAreRiskAbuses(psychologicallyAbusedChildren,
                                                                                 newChildDetails))
            .respWhichChildrenAreRiskPsychologicalAbuse(buildWhichChildrenAreRiskAbuses(psychologicallyAbusedChildren,
                                                                                        newChildDetails))
            .build();

    }

    private RespondentAllegationsOfHarmData buildAohChildSexualAbuseDetails(AbuseDto sexualAbuse,
                                                                            List<Element<ChildDetailsRevised>> newChildDetails,
                                                                           RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        String[] sexuallyAbusedChildren = sexualAbuse.getChildrenConcernedAbout();

        return respondentAllegationsOfHarmData.toBuilder()
            .respChildSexualAbuse(mapToChildAbuseIndividually(ChildAbuseEnum.sexualAbuse,sexualAbuse))
            .respAllChildrenAreRiskSexualAbuse(isAllChildrenAreRiskAbuses(sexuallyAbusedChildren,
                                                                          newChildDetails))
            .respWhichChildrenAreRiskSexualAbuse(buildWhichChildrenAreRiskAbuses(sexuallyAbusedChildren,
                                                                                 newChildDetails))
            .build();

    }

    private RespondentAllegationsOfHarmData buildAohChildEmotionalAbuseDetails(AbuseDto emotionalAbuse,
                                                                               List<Element<ChildDetailsRevised>> newChildDetails,
                                                                              RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        String[] emotionallyAbusedChildren = emotionalAbuse.getChildrenConcernedAbout();

        return respondentAllegationsOfHarmData.toBuilder()
            .respChildEmotionalAbuse(mapToChildAbuseIndividually(ChildAbuseEnum.emotionalAbuse,emotionalAbuse))
            .respAllChildrenAreRiskEmotionalAbuse(isAllChildrenAreRiskAbuses(emotionallyAbusedChildren,
                                                                             newChildDetails))
            .respWhichChildrenAreRiskEmotionalAbuse(buildWhichChildrenAreRiskAbuses(emotionallyAbusedChildren,
                                                                                    newChildDetails))
            .build();


    }

    private RespondentAllegationsOfHarmData buildAohChildFinancialAbuseDetails(AbuseDto financialAbuse,
                                                                               List<Element<ChildDetailsRevised>> newChildDetails,
                                                                              RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        String[] financiallyAbusedChildren = financialAbuse.getChildrenConcernedAbout();

        return respondentAllegationsOfHarmData.toBuilder()
            .respChildFinancialAbuse(mapToChildAbuseIndividually(ChildAbuseEnum.financialAbuse,financialAbuse))
            .respAllChildrenAreRiskFinancialAbuse(isAllChildrenAreRiskAbuses(financiallyAbusedChildren,
                                                                             newChildDetails))
            .respWhichChildrenAreRiskFinancialAbuse(buildWhichChildrenAreRiskAbuses(financiallyAbusedChildren,
                                                                                    newChildDetails))
            .build();
    }

    private RespondentAllegationsOfHarmData buildAohAbduction(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements,
                                                             RespondentAllegationsOfHarmData respondentAllegationsOfHarmData) {

        List<String> c1AConcernAboutChild = Arrays.stream(c100RebuildSafetyConcernsElements.getC1AConcernAboutChild()).toList();

        if (YesOrNo.No.equals(buildChildAbduction(c1AConcernAboutChild))) {
            return respondentAllegationsOfHarmData.toBuilder()
                .respAohChildAbductionYesNo(buildChildAbduction(c1AConcernAboutChild))
                .build();
        } else {
            respondentAllegationsOfHarmData = respondentAllegationsOfHarmData.toBuilder()
                .respAohChildAbductionYesNo(buildChildAbduction(c1AConcernAboutChild))
                .respChildAbductionReasons(c100RebuildSafetyConcernsElements.getC1AAbductionReasonOutsideUk())
                .respChildrenLocationNow(c100RebuildSafetyConcernsElements.getC1AChildsCurrentLocation())
                .respAbductionChildHasPassport(c100RebuildSafetyConcernsElements.getC1APassportOffice())
                .respPreviousAbductionThreats(c100RebuildSafetyConcernsElements.getC1AChildAbductedBefore())
                .respPreviousAbductionThreatsDetails(c100RebuildSafetyConcernsElements.getC1APreviousAbductionsShortDesc())
                .respAbductionPreviousPoliceInvolvement(c100RebuildSafetyConcernsElements.getC1APoliceOrInvestigatorInvolved())
                .respAbductionPreviousPoliceInvolvementDetails(c100RebuildSafetyConcernsElements.getC1APoliceOrInvestigatorOtherDetails())
                .respAgreeChildOtherContact(c100RebuildSafetyConcernsElements.getC1AAgreementOtherWaysDetails())
                .build();

            if (Yes.equals(respondentAllegationsOfHarmData.getRespAbductionChildHasPassport())) {
                respondentAllegationsOfHarmData = respondentAllegationsOfHarmData.toBuilder()
                    .respChildPassportDetails(buildChildPassportDetails(c100RebuildSafetyConcernsElements))
                    .respAbductionPassportOfficeNotified(c100RebuildSafetyConcernsElements.getC1AAbductionPassportOfficeNotified())
                    .build();
            }
            return respondentAllegationsOfHarmData;
        }
    }

    private YesOrNo buildChildSupervisedTime(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {

        if ("No".equalsIgnoreCase(c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails())) {
            return YesOrNo.No;
        } else if ("Yes".equalsIgnoreCase(c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails())
            || SUPERVISED.equalsIgnoreCase(c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails())) {
            return YesOrNo.Yes;
        }

        return YesOrNo.No;
    }

    private YesOrNo buildChildUnSupervisedTime(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {

        if ("No".equalsIgnoreCase(c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails())
            || SUPERVISED.equalsIgnoreCase(c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails())) {
            return YesOrNo.No;
        } else if ("Yes".equalsIgnoreCase(c100RebuildSafetyConcernsElements.getC1ASupervisionAgreementDetails())) {
            return YesOrNo.Yes;
        }
        return YesOrNo.No;
    }

    private RespChildPassportDetails buildChildPassportDetails(C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {

        if (c100RebuildSafetyConcernsElements.getC1APossessionChildrenPassport() == null) {
            return null;
        }

        List<RespPassportPossessionEnum> possessionChildrenPassport = new ArrayList<>();

        for (String possession : c100RebuildSafetyConcernsElements.getC1APossessionChildrenPassport()) {
            if (MOTHER.equalsIgnoreCase(possession)) {
                possessionChildrenPassport.add(RespPassportPossessionEnum.mother);
            }
            if (FATHER.equalsIgnoreCase(possession)) {
                possessionChildrenPassport.add(RespPassportPossessionEnum.father);
            }
            if (OTHER.equalsIgnoreCase(possession)) {
                possessionChildrenPassport.add(RespPassportPossessionEnum.otherPerson);

            }
        }
        return RespChildPassportDetails.builder().respChildPassportPossession(possessionChildrenPassport)
            .respChildPassportPossessionOtherDetails(ObjectUtils.isNotEmpty(c100RebuildSafetyConcernsElements.getC1AProvideOtherDetails())
                                                        ? c100RebuildSafetyConcernsElements.getC1AProvideOtherDetails() : null)
            .respChildHasMultiplePassports(c100RebuildSafetyConcernsElements.getC1AChildrenMoreThanOnePassport())
            .build();

    }

    private List<Element<RespDomesticAbuseBehaviours>> buildDomesticAbuseBehavioursDetails(
        C100RebuildSafetyConcernsElements c100RebuildSafetyConcernsElements) {
        List<Element<RespDomesticAbuseBehaviours>> applicantElements = new ArrayList<>();
        ApplicantSafteConcernDto applicantAbuse = c100RebuildSafetyConcernsElements.getC100SafetyConcerns().getApplicant();

        if (ObjectUtils.isNotEmpty(applicantAbuse.getPhysicalAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_1, applicantAbuse.getPhysicalAbuse()));
        }
        if (ObjectUtils.isNotEmpty(applicantAbuse.getPsychologicalAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_2, applicantAbuse.getPsychologicalAbuse()));
        }
        if (ObjectUtils.isNotEmpty(applicantAbuse.getSexualAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_3, applicantAbuse.getSexualAbuse()));
        }
        if (ObjectUtils.isNotEmpty(applicantAbuse.getEmotionalAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_4, applicantAbuse.getEmotionalAbuse()));
        }
        if (ObjectUtils.isNotEmpty(applicantAbuse.getFinancialAbuse())) {
            applicantElements.add(mapToDomesticAbuse(TypeOfAbuseEnum.TypeOfAbuseEnum_value_5, applicantAbuse.getFinancialAbuse()));
        }
        if (ObjectUtils.isNotEmpty(applicantAbuse.getSomethingElse())) {
            applicantElements.add(mapToDomesticAbuse(null, applicantAbuse.getSomethingElse()));
        }

        return applicantElements;

    }

    private RespChildAbuse mapToChildAbuseIndividually(ChildAbuseEnum abuseType, AbuseDto abuseDto) {

        return RespChildAbuse.builder()
            .respAbuseNatureDescription(abuseDto.getBehaviourDetails())
            .respBehavioursApplicantSoughtHelp(abuseDto.getSeekHelpFromPersonOrAgency())
            .respBehavioursStartDateAndLength(buildBehavioursStartDateAndLength(abuseDto))
            .respBehavioursApplicantHelpSoughtWho(abuseDto.getSeekHelpDetails())
            .build();

    }

    private String buildBehavioursStartDateAndLength(AbuseDto abuseDto) {

        if (ObjectUtils.isNotEmpty(abuseDto.getBehaviourStartDate()) && ObjectUtils.isNotEmpty(abuseDto.getIsOngoingBehaviour())) {
            return abuseDto.getBehaviourStartDate() + HYPHEN_SEPARATOR + isBehaviourOngoing(abuseDto);
        } else if (ObjectUtils.isNotEmpty(abuseDto.getIsOngoingBehaviour())) {
            return isBehaviourOngoing(abuseDto);
        }
        return abuseDto.getBehaviourStartDate();
    }

    private YesOrNo isAllChildrenAreRiskAbuses(String[] abusedChildren, List<Element<ChildDetailsRevised>> newChildDetails) {

        if (ObjectUtils.isNotEmpty(abusedChildren) && ObjectUtils.isNotEmpty(newChildDetails)) {
            if (abusedChildren.length == newChildDetails.size()) {
                return YesOrNo.Yes;
            } else {
                return YesOrNo.No;
            }
        }
        return null;
    }

    private DynamicMultiSelectList buildWhichChildrenAreRiskAbuses(String[] abusedChildren,
                                                                   List<Element<ChildDetailsRevised>> newChildDetails) {

        if (newChildDetails != null && abusedChildren.length != newChildDetails.size()) {
            List<DynamicMultiselectListElement> valueElements = new ArrayList<>();
            List<DynamicMultiselectListElement> listItemsElements = new ArrayList<>();
            newChildDetails.forEach(s -> {
                boolean contains = Arrays.asList(abusedChildren).contains(String.valueOf(s.getId()));
                log.info("abused children {}, {}, contains {}", abusedChildren, s.getId(), contains);
                if (contains) {
                    valueElements.add(DynamicMultiselectListElement.builder()
                                          .code(s.getId().toString()).label(s.getValue().getFirstName()
                                                                     + EMPTY_SPACE_STRING + s.getValue().getLastName()).build());
                }
                listItemsElements.add(DynamicMultiselectListElement.builder()
                                          .code(s.getId().toString()).label(s.getValue().getFirstName()
                                                                     + EMPTY_SPACE_STRING  + s.getValue().getLastName()).build());
            });
            return DynamicMultiSelectList.builder().value(valueElements).listItems(listItemsElements).build();
        }
        return null;
    }


    private String isBehaviourOngoing(AbuseDto abuseDto) {
        return abuseDto.getIsOngoingBehaviour().equals(Yes) ? ONGOING : NOT_ONGOING;
    }

    private Element<RespDomesticAbuseBehaviours> mapToDomesticAbuse(TypeOfAbuseEnum typeOfAbuseEnum, AbuseDto abuseDto) {

        return Element.<RespDomesticAbuseBehaviours>builder().value(RespDomesticAbuseBehaviours.builder()
                                                                    .respTypeOfAbuse(typeOfAbuseEnum)
                                                                    .respAbuseNatureDescription(abuseDto.getBehaviourDetails())
                                                                    .respBehavioursApplicantSoughtHelp(abuseDto.getSeekHelpFromPersonOrAgency())
                                                                    .respBehavioursStartDateAndLength(buildBehavioursStartDateAndLength(abuseDto))
                                                                    .respBehavioursApplicantHelpSoughtWho(abuseDto.getSeekHelpDetails())
                                                                    .build()).build();
    }

    private YesOrNo buildConcernAbout(List<String> whoConcernsAboutList, String typeOfCitizen) {
        if ((RESPONDENT.equalsIgnoreCase(typeOfCitizen) && whoConcernsAboutList.contains(RESPONDENT))
            || (CHILDREN.equalsIgnoreCase(typeOfCitizen) && whoConcernsAboutList.contains(CHILDREN))) {
            return YesOrNo.Yes;
        }
        return YesOrNo.No;
    }

    private YesOrNo buildChildAbduction(List<String> typeOfBehaviourList) {
        if (typeOfBehaviourList.contains(CHILD_ABDUCTION)) {
            return YesOrNo.Yes;
        }
        return YesOrNo.No;
    }

    private YesOrNo checkDomesticAbuse(List<String> typeOfBehaviourList) {
        if (typeOfBehaviourList.contains(WITNESSING_DOMESTIC_ABUSE)) {
            return YesOrNo.Yes;
        }
        return YesOrNo.No;
    }

}
