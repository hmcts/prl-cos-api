package uk.gov.hmcts.reform.prl.mapper.citizen;

import org.apache.commons.collections.CollectionUtils;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildChildDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherPersonDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildDetail;
import uk.gov.hmcts.reform.prl.models.c100rebuild.DateofBirth;
import uk.gov.hmcts.reform.prl.models.c100rebuild.OtherPersonAddress;
import uk.gov.hmcts.reform.prl.models.c100rebuild.OtherPersonDetail;
import uk.gov.hmcts.reform.prl.models.c100rebuild.PersonalDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

public class CaseDataOtherPersonsElementsMapper {

    private CaseDataOtherPersonsElementsMapper() {
    }

    public static void updateOtherPersonDetailsElementsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                                   C100RebuildOtherPersonDetailsElements c100RebuildOtherPersonDetailsElements,
                                                                   C100RebuildChildDetailsElements c100RebuildChildDetailsElements) {
        caseDataBuilder
            .otherPartyInTheCaseRevised(buildOtherPeople(c100RebuildOtherPersonDetailsElements))
            .relations(caseDataBuilder.build().getRelations().toBuilder()
                           .childAndOtherPeopleRelations(
                               buildChildAndOtherPeopleRelation(
                                   c100RebuildOtherPersonDetailsElements,
                                   c100RebuildChildDetailsElements
                               ))
                           .build());

    }

    private static List<Element<PartyDetails>> buildOtherPeople(C100RebuildOtherPersonDetailsElements
                                                                       c100RebuildOtherPersonDetailsElements) {

        List<OtherPersonDetail> otherPersonDetailsList = c100RebuildOtherPersonDetailsElements.getOtherPersonDetails();

        return nonNull(otherPersonDetailsList) ? otherPersonDetailsList.stream()
                .map(otherPersonDetail -> Element.<PartyDetails>builder().value(buildPartyDetails(otherPersonDetail)).id(
                    UUID.fromString(otherPersonDetail.getId())).build())
                .toList() : emptyList();
    }

    private static PartyDetails buildPartyDetails(OtherPersonDetail otherPersonDetail) {
        PersonalDetails personalDetails = otherPersonDetail.getPersonalDetails();
        return PartyDetails
                .builder()
                .firstName(otherPersonDetail.getFirstName())
                .lastName(otherPersonDetail.getLastName())
                .previousName(personalDetails.getPreviousFullName())
                .liveInRefuge(livingInRefuge(otherPersonDetail))
                .refugeConfidentialityC8Form(otherPersonDetail.getRefugeConfidentialityC8Form())
                .gender(Gender.getDisplayedValueFromEnumString(personalDetails.getGender()))
                .otherGender(PrlAppsConstants.OTHER.equalsIgnoreCase(personalDetails.getGender()) ? personalDetails.getOtherGenderDetails() : null)
                .dateOfBirth(PrlAppsConstants.YES.equalsIgnoreCase(personalDetails.getIsDateOfBirthUnknown())
                        ? buildDateOfBirth(personalDetails.getApproxDateOfBirth())
                        : buildDateOfBirth(personalDetails.getDateOfBirth()))
                .isDateOfBirthUnknown(PrlAppsConstants.YES.equalsIgnoreCase(personalDetails.getIsDateOfBirthUnknown()) ? DontKnow.dontKnow : null)
                .isCurrentAddressKnown(null != otherPersonDetail.getAddressUnknown()
                    ? reverseYesOrNoForIsCurrentAddressKnown(otherPersonDetail.getAddressUnknown()) : Yes)
                .address(buildAddress(otherPersonDetail.getOtherPersonAddress()))
                .isAddressConfidential(null != otherPersonDetail.getOtherPersonAddress()
                    ? livingInRefuge(otherPersonDetail) : null)
        //.relationshipToChildren(buildChildRelationship(otherPersonDetail.getRelationshipDetails()))
        .build();
    }

    private static YesOrNo livingInRefuge(OtherPersonDetail otherPersonDetails) {
        if (YesOrNo.Yes.equals(otherPersonDetails.getAddressUnknown())) {
            return No;
        } else if (!YesOrNo.Yes.equals(otherPersonDetails.getAddressUnknown())
            && YesOrNo.Yes.equals(otherPersonDetails.getLiveInRefuge())) {
            return Yes;
        } else {
            return No;
        }
    }

    private static YesOrNo reverseYesOrNoForIsCurrentAddressKnown(YesOrNo isCurrentAddressUnknown) {
        if (YesOrNo.Yes.equals(isCurrentAddressUnknown)) {
            return No;
        } else {
            return Yes;
        }
    }

    private static Address buildAddress(OtherPersonAddress address) {
        return Address
                .builder()
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .addressLine3(address.getAddressLine3())
                .postTown(address.getPostTown())
                .county(address.getCounty())
                .postCode(address.getPostCode())
                .country(address.getCountry())
                .build();
    }

    private static LocalDate buildDateOfBirth(DateofBirth date) {
        if (isNotEmpty(date) && isNotEmpty(date.getYear()) &&  isNotEmpty(date.getMonth()) && isNotEmpty(date.getDay())) {
            return LocalDate.of(Integer.parseInt(date.getYear()), Integer.parseInt(date.getMonth()),
                    Integer.parseInt(date.getDay()));
        }
        return null;
    }

    private static List<Element<ChildrenAndOtherPeopleRelation>> buildChildAndOtherPeopleRelation(
        C100RebuildOtherPersonDetailsElements c100RebuildOtherPersonDetailsElements,
        C100RebuildChildDetailsElements c100RebuildChildDetailsElements) {

        if (CollectionUtils.isEmpty(c100RebuildOtherPersonDetailsElements.getOtherPersonDetails())) {
            return emptyList();
        }

        return c100RebuildOtherPersonDetailsElements.getOtherPersonDetails().stream()
            .map(otherPeopleDetails ->
                     otherPeopleDetails.getRelationshipDetails().getRelationshipToChildren().stream()
                         .map(childRelationship -> {
                             Optional<ChildDetail> childDetails = c100RebuildChildDetailsElements.getChildDetails().stream()
                                 .filter(childDetail -> childDetail.getId().equals(
                                     childRelationship.getChildId())).findFirst();
                             if (childDetails.isPresent()) {
                                 ChildDetail childDetail = childDetails.get();

                                 return Element.<ChildrenAndOtherPeopleRelation>builder()
                                     .value(ChildrenAndOtherPeopleRelation.builder()
                                         .childFullName(childDetail.getFirstName() + " " + childDetail.getLastName())
                                         .childLivesWith(childDetail.getChildLiveWith().stream()
                                             .anyMatch(c -> c.getId().equals(otherPeopleDetails.getId())) ? Yes : No)
                                         .otherPeopleFullName(otherPeopleDetails.getFirstName() + " " + otherPeopleDetails.getLastName())
                                         .childAndOtherPeopleRelation(RelationshipsEnum.getEnumForDisplayedValue(
                                             childRelationship.getRelationshipType()))
                                         .childAndOtherPeopleRelationOtherDetails(childRelationship.getOtherRelationshipTypeDetails())
                                         .isChildLivesWithPersonConfidential(otherPeopleDetails.getIsOtherPersonAddressConfidential())
                                         .isOtherPeopleIdConfidential(otherPeopleDetails.getIsOtherPersonAddressConfidential())
                                         .otherPeopleId(otherPeopleDetails.getId())
                                         .childId(childDetail.getId())
                                         .build())
                                     .build();
                             }
                             return null;
                         })
                         .filter(Objects::nonNull)
                         .toList()
            )
            .flatMap(Collection::stream)
            .toList();
    }

}
