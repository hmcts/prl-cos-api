package uk.gov.hmcts.reform.prl.controllers.citizen.mapper;

import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildChildDetailsElements;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

public class CaseDataChildDetailsElementsMapper {


    private static final String WHO_THE_CHILD_LIVE_WITH = "whoChildLiveWith";
    private static final String CHILD_TIME_SPENT = "childTimeSpent";

    public static void updateChildDetailsElementsForCaseData(CaseData.CaseDataBuilder caseDataBuilder,
                                                             C100RebuildChildDetailsElements c100RebuildChildDetailsElements) {
        caseDataBuilder
            .children(buildChildDetails(c100RebuildChildDetailsElements.getChildDetails()))
            .childrenKnownToLocalAuthority(
                YesNoDontKnow.getDisplayedValueIgnoreCase(
                    c100RebuildChildDetailsElements.getChildrenKnownToSocialServices()))
            .childrenKnownToLocalAuthorityTextArea(
                c100RebuildChildDetailsElements.getChildrenKnownToSocialServicesDetails())
            .childrenSubjectOfChildProtectionPlan(
                YesNoDontKnow.getDisplayedValueIgnoreCase(
                    c100RebuildChildDetailsElements.getChildrenSubjectOfProtectionPlan()));
    }

    private static List<Element<Child>> buildChildDetails(List<C100RebuildChildDetailsElements.ChildDetail> childDetails) {
        return childDetails.stream()
            .map(CaseDataChildDetailsElementsMapper::mapToChildDetails)
            .collect(Collectors.toList());
    }

    private static Element<Child> mapToChildDetails(C100RebuildChildDetailsElements.ChildDetail childDetail) {
        LocalDate dateOfBirth = buildDateOfBirth(childDetail.getPersonalDetails().getDateOfBirth());
        return Element.<Child>builder().value(Child.builder()
                   .firstName(childDetail.getFirstName())
                   .lastName(childDetail.getLastName())
                   .dateOfBirth(dateOfBirth != null ? dateOfBirth : buildDateOfBirth(childDetail.getPersonalDetails().getApproxDateOfBirth()))
                   .isDateOfBirthUnknown(buildDateOfBirthUnknown(childDetail.getPersonalDetails()))
                   .gender(Gender.getDisplayedValueFromEnumString((childDetail.getPersonalDetails().getGender())))
                   .otherGender(childDetail.getPersonalDetails().getOtherGenderDetails())
                   .parentalResponsibilityDetails(buildParentalResponsibility(
                       childDetail.getParentialResponsibility()))
                   .orderAppliedFor(buildOrdersApplyingFor(childDetail.getChildMatters()))
                   .build()
            ).build();
    }

    private static String buildParentalResponsibility(C100RebuildChildDetailsElements.ParentialResponsibility parentalResponsibility) {
        return parentalResponsibility.getStatement();
    }

    private static DontKnow buildDateOfBirthUnknown(C100RebuildChildDetailsElements.PersonalDetails personalDetails) {
        return Yes.equals(personalDetails.getIsDateOfBirthUnknown()) ? DontKnow.dontKnow : null;
    }

    private static LocalDate buildDateOfBirth(C100RebuildChildDetailsElements.DateofBirth dateOfBirth) {
        if (isNotEmpty(dateOfBirth.getYear()) && isNotEmpty(dateOfBirth.getMonth()) && isNotEmpty(dateOfBirth.getDay())) {
            return LocalDate.of(Integer.parseInt(dateOfBirth.getYear()), Integer.parseInt(dateOfBirth.getMonth()),
                                Integer.parseInt(dateOfBirth.getDay())
            );
        }
        return null;
    }


    private static List<OrderTypeEnum> buildOrdersApplyingFor(C100RebuildChildDetailsElements.ChildMatters childMatters) {
        List<OrderTypeEnum> orderTypeEnums = new ArrayList<>();
        List<String> childMattersNeedsResolutions = childMatters.getNeedsResolution();
        if (childMattersNeedsResolutions.contains(WHO_THE_CHILD_LIVE_WITH)  || childMattersNeedsResolutions.contains(CHILD_TIME_SPENT)) {
            orderTypeEnums.add(childArrangementsOrder);
        }
        return orderTypeEnums;
    }
}

