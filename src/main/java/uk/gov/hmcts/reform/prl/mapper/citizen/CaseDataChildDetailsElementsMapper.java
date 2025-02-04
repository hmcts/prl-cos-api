package uk.gov.hmcts.reform.prl.mapper.citizen;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.childArrangementsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.prohibitedStepsOrder;
import static uk.gov.hmcts.reform.prl.enums.OrderTypeEnum.specificIssueOrder;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
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
            .newChildDetails(buildChildDetails(c100RebuildChildDetailsElements.getChildDetails(),
                caseDataBuilder))
            .childrenKnownToLocalAuthority(
                YesNoDontKnow.getDisplayedValueIgnoreCase(
                    c100RebuildChildDetailsElements.getChildrenKnownToSocialServices()))
            .childrenKnownToLocalAuthorityTextArea(
                c100RebuildChildDetailsElements.getChildrenKnownToSocialServicesDetails())
            .childrenSubjectOfChildProtectionPlan(
                YesNoDontKnow.getDisplayedValueIgnoreCase(
                    c100RebuildChildDetailsElements.getChildrenSubjectOfProtectionPlan()));
    }

    private static List<Element<ChildDetailsRevised>> buildChildDetails(List<ChildDetail> childDetails,
                                                                        CaseData.CaseDataBuilder<?,?> caseDataBuilder) {

        List<Element<ChildDetailsRevised>> childDetailsElements = new ArrayList<>();
        for (ChildDetail childDetail : childDetails) {
            Element<ChildDetailsRevised> childDetailsRevisedElement = mapToChildDetails(childDetail, caseDataBuilder);
            childDetailsElements.add(childDetailsRevisedElement);
        }

        return childDetailsElements;
    }

    private static Element<ChildDetailsRevised> mapToChildDetails(ChildDetail childDetail,
                                                                  CaseData.CaseDataBuilder<?,?> caseDataBuilder) {
        return Element.<ChildDetailsRevised>builder()
            .value(ChildDetailsRevised.builder()
                       .firstName(childDetail.getFirstName())
                       .lastName(childDetail.getLastName())
                       .dateOfBirth(getDateOfBirth(childDetail))
                       .isDateOfBirthUnknown(buildDateOfBirthUnknown(
                           childDetail.getPersonalDetails()))
                       .gender(Gender.getDisplayedValueFromEnumString((childDetail.getPersonalDetails().getGender())))
                       .otherGender(childDetail.getPersonalDetails().getOtherGenderDetails())
                       .parentalResponsibilityDetails(
                           buildParentalResponsibility(
                               childDetail.getParentialResponsibility()))
            .whoDoesTheChildLiveWith(setWhoDoesTheChildLiveWithDynamicList(childDetail, caseDataBuilder))
            .orderAppliedFor(buildOrdersApplyingFor(childDetail.getChildMatters()))
            .build())
            .id(UUID.fromString(childDetail.getId())).build();
    }

    private static DynamicList setWhoDoesTheChildLiveWithDynamicList(ChildDetail childDetail,
                                                                     CaseData.CaseDataBuilder<?,?> caseDataBuilder) {
        DynamicList dynamicListElements = null;
        List<Element<PartyDetails>> listOfParties = new ArrayList<>();
        CaseData caseData = caseDataBuilder.build();

        if (null != caseData.getApplicants()) {
            listOfParties.addAll(caseData.getApplicants());
        }
        if (null != caseData.getRespondents()) {
            listOfParties.addAll(caseData.getRespondents());
        }
        if (null != caseData.getOtherPartyInTheCaseRevised()) {
            listOfParties.addAll(caseData.getOtherPartyInTheCaseRevised());
        }

        log.info("listOfParties: {}", listOfParties);

        AtomicReference<String> label = new AtomicReference<>("");
        listOfParties.stream().forEach(partyDetailsElement -> {
            PartyDetails partyDetails = partyDetailsElement.getValue();
            if (partyDetails.getFirstName().equals(childDetail.getMainlyLiveWith().getFirstName())
                && partyDetails.getLastName().equals(childDetail.getMainlyLiveWith().getLastName())) {
                log.info("isnide if condition");
                String address = populateAddressInDynamicList(partyDetailsElement);
                String name = populateNameInDynamicList(partyDetailsElement, address);

                if (null != name && null != address) {
                    label.set(name + address);
                } else if (null != name) {
                    label.set(name);
                }
            }
        });

        if (childDetail.getMainlyLiveWith() != null) {
            dynamicListElements = DynamicList
                .builder()
                .value(DynamicListElement
                    .builder()
                    .code(UUID.fromString(childDetail.getMainlyLiveWith().getId()))
                    .label(label.get())
                    .build()).build();
        }

        return dynamicListElements;
    }

    private static String populateAddressInDynamicList(Element<PartyDetails> parties) {
        String address = null;
        if (null != parties.getValue().getAddress()
            && !StringUtils.isBlank(parties.getValue().getAddress().getAddressLine1())) {

            //Address line 2 is an optional field
            String addressLine2 = "";

            //Postcode is an optional field
            String postcode = !StringUtils.isBlank(parties.getValue().getAddress().getPostCode())
                ? parties.getValue().getAddress().getPostCode() : "";

            //Adding comma to address line 2 if the postcode is there
            if (!StringUtils.isBlank(parties.getValue().getAddress().getAddressLine2())) {
                addressLine2 = !StringUtils.isBlank(postcode)
                    ?  parties.getValue().getAddress().getAddressLine2().concat(", ")
                    : parties.getValue().getAddress().getAddressLine2();
            }

            //Comma is required if postcode or address line 2 is not blank
            String addressLine1 = !StringUtils.isBlank(postcode) || !StringUtils.isBlank(addressLine2)
                ? parties.getValue().getAddress().getAddressLine1().concat(", ")
                : parties.getValue().getAddress().getAddressLine1();

            address = addressLine1 + addressLine2 + postcode;
        }

        return address;
    }

    private static String populateNameInDynamicList(Element<PartyDetails> parties, String address) {
        String name = null;
        if (!StringUtils.isBlank(parties.getValue().getFirstName())
            && !StringUtils.isBlank(parties.getValue().getLastName())) {
            name = !StringUtils.isBlank(address)
                ? parties.getValue().getFirstName() + " " + parties.getValue().getLastName() + " - "
                : parties.getValue().getFirstName() + " " + parties.getValue().getLastName();
        }
        return name;
    }

    private static LocalDate getDateOfBirth(ChildDetail childDetail) {
        LocalDate dateOfBirth = buildDateOfBirth(childDetail.getPersonalDetails().getDateOfBirth());
        return dateOfBirth != null ? dateOfBirth : buildDateOfBirth(childDetail.getPersonalDetails()
                                                                        .getApproxDateOfBirth());
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

