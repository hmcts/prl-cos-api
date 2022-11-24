package uk.gov.hmcts.reform.prl.controllers.citizen.mapper;

import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherPersonDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.DateofBirth;
import uk.gov.hmcts.reform.prl.models.c100rebuild.OtherPersonAddress;
import uk.gov.hmcts.reform.prl.models.c100rebuild.OtherPersonDetail;
import uk.gov.hmcts.reform.prl.models.c100rebuild.PersonalDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public class CaseDataOtherPersonsElementsMapper {

    private CaseDataOtherPersonsElementsMapper() {
    }

    public static void updateOtherPersonDetailsElementsForCaseData(CaseData.CaseDataBuilder caseDataBuilder,
                                                                   C100RebuildOtherPersonDetailsElements c100RebuildOtherPersonDetailsElements) {
        caseDataBuilder
                .othersToNotify(buildOtherPeople(c100RebuildOtherPersonDetailsElements));
    }

    private static List<Element<PartyDetails>> buildOtherPeople(C100RebuildOtherPersonDetailsElements
                                                                       c100RebuildOtherPersonDetailsElements) {

        List<OtherPersonDetail> otherPersonDetailsList = c100RebuildOtherPersonDetailsElements.getOtherPersonDetails();

        return otherPersonDetailsList.stream()
                .map(otherPersonDetail -> Element.<PartyDetails>builder().value(buildPartyDetails(otherPersonDetail)).build())
                .collect(Collectors.toList());
    }

    private static PartyDetails buildPartyDetails(OtherPersonDetail otherPersonDetail) {
        PersonalDetails personalDetails = otherPersonDetail.getPersonalDetails();
        return PartyDetails
                .builder()
                .firstName(otherPersonDetail.getFirstName())
                .lastName(otherPersonDetail.getLastName())
                .previousName(personalDetails.getPreviousFullName())
                .gender(Gender.getDisplayedValueFromEnumString(personalDetails.getGender()))
                .otherGender(PrlAppsConstants.OTHER.equalsIgnoreCase(personalDetails.getGender()) ? personalDetails.getOtherGenderDetails() : null)
                .dateOfBirth(PrlAppsConstants.YES.equalsIgnoreCase(personalDetails.getIsDateOfBirthUnknown())
                        ? buildDateOfBirth(personalDetails.getApproxDateOfBirth())
                        : buildDateOfBirth(personalDetails.getDateOfBirth()))
                .isDateOfBirthUnknown(PrlAppsConstants.YES.equalsIgnoreCase(personalDetails.getIsDateOfBirthUnknown()) ? DontKnow.dontKnow : null)
                .address(buildAddress(otherPersonDetail.getOtherPersonAddress()))
        //TODO Needs to updated once solictior mapping is finalized
        //.relationshipToChildren(buildChildRelationship(otherPersonDetail.getRelationshipDetails()))
        .build();
    }

    //    public static String buildChildRelationship(RelationshipDetails relationshipDetails) {
    //        Optional<ChildRelationship> childRelationship = relationshipDetails.getRelationshipToChildren().stream().findFirst();
    //        if (childRelationship.isPresent()) {
    //            ChildRelationship relationship = childRelationship.get();
    //            if (PrlAppsConstants.OTHER.equalsIgnoreCase(relationship.getRelationshipType())) {
    //                return relationship.getOtherRelationshipTypeDetails();
    //            }
    //            return relationship.getRelationshipType();
    //        }
    //        return null;
    //    }

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

}
