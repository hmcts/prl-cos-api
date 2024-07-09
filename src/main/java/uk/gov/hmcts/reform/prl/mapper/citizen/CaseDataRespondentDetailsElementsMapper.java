package uk.gov.hmcts.reform.prl.mapper.citizen;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.RelationshipsEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100Address;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildChildDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildRespondentDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildDetail;
import uk.gov.hmcts.reform.prl.models.c100rebuild.DateofBirth;
import uk.gov.hmcts.reform.prl.models.c100rebuild.PersonalDetails;
import uk.gov.hmcts.reform.prl.models.c100rebuild.RespondentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@Slf4j
public class CaseDataRespondentDetailsElementsMapper {

    private CaseDataRespondentDetailsElementsMapper() {
    }

    public static void updateRespondentDetailsElementsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                                  C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements,
                                                                  C100RebuildChildDetailsElements c100RebuildChildDetailsElements) {
        caseDataBuilder
            .respondents(buildRespondentDetails(c100RebuildRespondentDetailsElements))
            .relations(caseDataBuilder.build().getRelations().toBuilder()
                                      .childAndRespondentRelations(
                                          buildChildAndRespondentRelation(c100RebuildRespondentDetailsElements,
                                                                          c100RebuildChildDetailsElements))
                                      .build());
    }

    private static List<Element<PartyDetails>> buildRespondentDetails(C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements) {

        List<RespondentDetails> respondentDetailsList = c100RebuildRespondentDetailsElements.getRespondentDetails();

        return respondentDetailsList.stream().map(respondentDetails -> Element.<PartyDetails>builder().value(
            buildPartyDetails(respondentDetails)).build()).toList();

    }

    private static PartyDetails buildPartyDetails(RespondentDetails respondentDetails) {
        log.info("****************** loggin in date of birth unknown value *****************"
                    + respondentDetails.getPersonalDetails().getDateOfBirth());

        log.info("****************** loggin in date of birth unknown value *****************"
                     + respondentDetails.getPersonalDetails().getIsDateOfBirthUnknown());

        return PartyDetails
            .builder()
            .firstName(respondentDetails.getFirstName())
            .lastName(respondentDetails.getLastName())
            .previousName(buildPreviousName(respondentDetails))
            .gender(Gender.getDisplayedValueFromEnumString(respondentDetails.getPersonalDetails().getGender()))
            .otherGender(respondentDetails.getPersonalDetails().getOtherGenderDetails())
            .dateOfBirth(buildDateOfBirth(respondentDetails))
            .isDateOfBirthUnknown(buildDateOfBirthUnknown(respondentDetails.getPersonalDetails()))
            .placeOfBirth(respondentDetails.getPersonalDetails().getRespondentPlaceOfBirth())
            .isPlaceOfBirthKnown(buildRespondentPlaceOfBirthKnown(respondentDetails.getPersonalDetails()))
            .address(buildAddress(respondentDetails.getAddress()))
            .isAtAddressLessThan5Years(buildAddressLivedLessThan5YearsDetails(respondentDetails))
            .addressLivedLessThan5YearsDetails(respondentDetails.getAddress().getProvideDetailsOfPreviousAddresses())
            .canYouProvideEmailAddress(buildCanYouProvideEmailAddress(respondentDetails))
            .email(isNotEmpty(respondentDetails.getRespondentContactDetail().getEmailAddress())
                       ? respondentDetails.getRespondentContactDetail().getEmailAddress() : null)
            .canYouProvidePhoneNumber(buildCanYouProvidePhoneNumber(respondentDetails))
            .phoneNumber(isNotEmpty(respondentDetails.getRespondentContactDetail().getTelephoneNumber())
                             ? respondentDetails.getRespondentContactDetail().getTelephoneNumber() : null)
            .build();
    }

    private static Address buildAddress(C100Address c100Address) {
        return Address.builder().addressLine1(c100Address.getAddressLine1()).addressLine2(c100Address.getAddressLine2()).postTown(
            c100Address.getPostTown()).county(c100Address.getCounty()).postCode(c100Address.getPostCode()).build();
    }

    private static LocalDate buildDateOfBirth(RespondentDetails respondentDetails) {
        LocalDate dateOfBirth = buildDateOfBirth(respondentDetails.getPersonalDetails().getDateOfBirth());
        return dateOfBirth != null ? dateOfBirth : buildDateOfBirth(respondentDetails.getPersonalDetails().getApproxDateOfBirth());
    }

    private static LocalDate buildDateOfBirth(DateofBirth date) {
        if (isNotEmpty(date) && isNotEmpty(date.getYear()) && isNotEmpty(date.getMonth()) && isNotEmpty(date.getDay())) {
            return LocalDate.of(Integer.parseInt(date.getYear()),
                                Integer.parseInt(date.getMonth()),
                                Integer.parseInt(date.getDay())
            );
        }
        return null;
    }

    private static YesOrNo buildAddressLivedLessThan5YearsDetails(RespondentDetails respondentDetails) {

        return (!"Yes".equalsIgnoreCase(respondentDetails.getAddress().getAddressHistory())) ? Yes : YesOrNo.No;
    }

    private static String buildPreviousName(RespondentDetails respondentDetails) {

        return "Yes".equalsIgnoreCase(respondentDetails.getPersonalDetails().getHasNameChanged())
            ? respondentDetails.getPersonalDetails().getResPreviousName() : null;
    }

    private static DontKnow buildDateOfBirthUnknown(PersonalDetails personalDetails) {
        return (!"Yes".equalsIgnoreCase(personalDetails.getIsDateOfBirthUnknown())) ? DontKnow.dontKnow : null;
    }

//    private static DontKnow buildDateOfBirthUnknown(PersonalDetails personalDetails) {
//        return "Yes".equalsIgnoreCase(personalDetails.getIsDateOfBirthUnknown()) ? DontKnow.dontKnow : null;
//    }

    private static YesOrNo buildRespondentPlaceOfBirthKnown(PersonalDetails personalDetails) {
        return (!"Yes".equalsIgnoreCase(personalDetails.getRespondentPlaceOfBirthUnknown())) ? Yes : No;
    }

    private static YesOrNo buildCanYouProvideEmailAddress(RespondentDetails respondentDetails) {

        return "Yes".equalsIgnoreCase(respondentDetails.getRespondentContactDetail().getDonKnowEmailAddress()) ? YesOrNo.No : Yes;
    }

    private static YesOrNo buildCanYouProvidePhoneNumber(RespondentDetails respondentDetails) {

        return "Yes".equalsIgnoreCase(respondentDetails.getRespondentContactDetail().getDonKnowTelephoneNumber()) ? YesOrNo.No : Yes;
    }

    private static List<Element<ChildrenAndRespondentRelation>> buildChildAndRespondentRelation(
        C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements,
        C100RebuildChildDetailsElements c100RebuildChildDetailsElements) {

        return c100RebuildRespondentDetailsElements.getRespondentDetails().stream()
            .map(respondentDetails ->
                     respondentDetails.getRelationshipDetails().getRelationshipToChildren().stream()
                         .map(childRelationship -> {
                             Optional<ChildDetail> childDetails = c100RebuildChildDetailsElements.getChildDetails().stream()
                                 .filter(childDetail -> childDetail.getId().equals(
                                     childRelationship.getChildId())).findFirst();
                             if (childDetails.isPresent()) {
                                 ChildDetail childDetail = childDetails.get();

                                 return Element.<ChildrenAndRespondentRelation>builder()
                                     .value(ChildrenAndRespondentRelation.builder()
                                                .childFullName(childDetail.getFirstName() + " " + childDetail.getLastName())
                                                .childLivesWith(childDetail.getChildLiveWith().stream()
                                                                    .anyMatch(c -> c.getId().equals(respondentDetails.getId())) ? Yes : No)
                                                .respondentFullName(respondentDetails.getFirstName() + " " + respondentDetails.getLastName())
                                                .childAndRespondentRelation(RelationshipsEnum.getEnumForDisplayedValue(
                                                    childRelationship.getRelationshipType()))
                                                .childAndRespondentRelationOtherDetails(childRelationship.getOtherRelationshipTypeDetails())
                                                .build()).build();
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
