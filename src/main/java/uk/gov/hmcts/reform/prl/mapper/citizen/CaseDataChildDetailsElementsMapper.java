package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildChildDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildDetail;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildMatters;
import uk.gov.hmcts.reform.prl.models.c100rebuild.DateofBirth;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ParentialResponsibility;
import uk.gov.hmcts.reform.prl.models.c100rebuild.PersonalDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.prohibitedStepsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.specificIssueOrder;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

public class CaseDataChildDetailsElementsMapper {
    private CaseDataChildDetailsElementsMapper() {
    }

    private static final String WHO_THE_CHILD_LIVE_WITH = "whoChildLiveWith";
    private static final String CHILD_TIME_SPENT = "childTimeSpent";

    private static final String CHANGE_CHILD_NAME_SURNAME = "changeChildrenNameSurname";
    private static final String ALLOW_MEDICAL_TREATMENT = "allowMedicalTreatment";
    private static final String TAKING_CHILD_HOLIDAY = "takingChildOnHoliday";
    private static final String RELOCATE_CHILDREN_DIFFERENT_AREA = "relocateChildrenDifferentUkArea";
    private static final String RELOCATE_CHILDREN_OUTSIDE_UK = "relocateChildrenOutsideUk";

    private static final String SPECIFIC_HOLIDAY = "specificHoliday";
    private static final String WHAT_SCHOOL_CHILD_GO_TO = "whatSchoolChildrenWillGoTo";
    private static final String RELIGIOUS_ISSUE = "religiousIssue";
    private static final String CHANGE_CHILD_NAME_SURNAMEA = "changeChildrenNameSurnameA";
    private static final String MEDICAL_TREATMENT = "medicalTreatment";
    private static final String RELOCATE_CHILDREN_DIFFERENT_AREAA = "relocateChildrenDifferentUkAreaA";
    private static final String RELOCATE_CHILDREN_OUTSIDE_UKA = "relocateChildrenOutsideUkA";

    public static void updateChildDetailsElementsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                             C100RebuildChildDetailsElements c100RebuildChildDetailsElements) {
        caseDataBuilder
            .newChildDetails(buildChildDetails(c100RebuildChildDetailsElements.getChildDetails()))
            .childrenKnownToLocalAuthority(
                YesNoDontKnow.getDisplayedValueIgnoreCase(
                    c100RebuildChildDetailsElements.getChildrenKnownToSocialServices()))
            .childrenKnownToLocalAuthorityTextArea(
                c100RebuildChildDetailsElements.getChildrenKnownToSocialServicesDetails())
            .childrenSubjectOfChildProtectionPlan(
                YesNoDontKnow.getDisplayedValueIgnoreCase(
                    c100RebuildChildDetailsElements.getChildrenSubjectOfProtectionPlan()));
    }

    private static List<Element<ChildDetailsRevised>> buildChildDetails(List<ChildDetail> childDetails) {
        return childDetails.stream()
            .map(CaseDataChildDetailsElementsMapper::mapToChildDetails)
            .toList();
    }

    private static Element<ChildDetailsRevised> mapToChildDetails(ChildDetail childDetail) {

        return Element.<ChildDetailsRevised>builder().value(ChildDetailsRevised.builder()
                   .firstName(childDetail.getFirstName())
                   .lastName(childDetail.getLastName())
                   .dateOfBirth(getDateOfBirth(childDetail))
                   .isDateOfBirthUnknown(buildDateOfBirthUnknown(childDetail.getPersonalDetails()))
                   .gender(Gender.getDisplayedValueFromEnumString((childDetail.getPersonalDetails().getGender())))
                   .otherGender(childDetail.getPersonalDetails().getOtherGenderDetails())
                   .parentalResponsibilityDetails(buildParentalResponsibility(
                       childDetail.getParentialResponsibility()))
                   .orderAppliedFor(buildOrdersApplyingFor(childDetail.getChildMatters()))
                                                  .build()
            ).build();
    }

    private static LocalDate getDateOfBirth(ChildDetail childDetail) {
        LocalDate dateOfBirth = buildDateOfBirth(childDetail.getPersonalDetails().getDateOfBirth());
        return dateOfBirth != null ? dateOfBirth : buildDateOfBirth(childDetail.getPersonalDetails().getApproxDateOfBirth());
    }

    private static String buildParentalResponsibility(ParentialResponsibility parentalResponsibility) {
        return parentalResponsibility.getStatement();
    }

    private static DontKnow buildDateOfBirthUnknown(PersonalDetails personalDetails) {
        return Yes.name().equals(personalDetails.getIsDateOfBirthUnknown()) ? DontKnow.dontKnow : null;
    }

    private static LocalDate buildDateOfBirth(DateofBirth dateOfBirth) {
        if (isNotEmpty(dateOfBirth.getYear()) && isNotEmpty(dateOfBirth.getMonth()) && isNotEmpty(dateOfBirth.getDay())) {
            return LocalDate.of(Integer.parseInt(dateOfBirth.getYear()), Integer.parseInt(dateOfBirth.getMonth()),
                                Integer.parseInt(dateOfBirth.getDay())
            );
        }
        return null;
    }


    private static List<OrderTypeEnum> buildOrdersApplyingFor(ChildMatters childMatters) {
        List<OrderTypeEnum> orderTypeEnums = new ArrayList<>();
        List<String> childMattersNeedsResolutions = childMatters.getNeedsResolution();
        if (childMattersNeedsResolutions.contains(WHO_THE_CHILD_LIVE_WITH)  || childMattersNeedsResolutions.contains(CHILD_TIME_SPENT)) {
            orderTypeEnums.add(childArrangementsOrder);
        }
        if (childMattersNeedsResolutions.contains(CHANGE_CHILD_NAME_SURNAME)
                || childMattersNeedsResolutions.contains(ALLOW_MEDICAL_TREATMENT)
                || childMattersNeedsResolutions.contains(TAKING_CHILD_HOLIDAY)
                || childMattersNeedsResolutions.contains(RELOCATE_CHILDREN_DIFFERENT_AREA)
                || childMattersNeedsResolutions.contains(RELOCATE_CHILDREN_OUTSIDE_UK)) {
            orderTypeEnums.add(prohibitedStepsOrder);
        }
        if (childMattersNeedsResolutions.contains(SPECIFIC_HOLIDAY)
                || childMattersNeedsResolutions.contains(WHAT_SCHOOL_CHILD_GO_TO)
                || childMattersNeedsResolutions.contains(RELIGIOUS_ISSUE)
                || childMattersNeedsResolutions.contains(CHANGE_CHILD_NAME_SURNAMEA)
                || childMattersNeedsResolutions.contains(MEDICAL_TREATMENT)
                || childMattersNeedsResolutions.contains(RELOCATE_CHILDREN_DIFFERENT_AREAA)
                || childMattersNeedsResolutions.contains(RELOCATE_CHILDREN_OUTSIDE_UKA)) {
            orderTypeEnums.add(specificIssueOrder);
        }
        return orderTypeEnums;
    }
}

