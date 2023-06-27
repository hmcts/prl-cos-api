package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.enums.DontKnow;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100Address;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildRespondentDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.DateofBirth;
import uk.gov.hmcts.reform.prl.models.c100rebuild.PersonalDetails;
import uk.gov.hmcts.reform.prl.models.c100rebuild.RespondentDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;

public class CaseDataRespondentDetailsElementsMapper {

    private CaseDataRespondentDetailsElementsMapper() {
    }

    public static void updateRespondentDetailsElementsForCaseData(CaseData.CaseDataBuilder caseDataBuilder,
                                                                  C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements) {
        caseDataBuilder.respondents(buildRespondentDetails(c100RebuildRespondentDetailsElements));
    }

    private static List<Element<PartyDetails>> buildRespondentDetails(C100RebuildRespondentDetailsElements c100RebuildRespondentDetailsElements) {

        List<RespondentDetails> respondentDetailsList = c100RebuildRespondentDetailsElements.getRespondentDetails();

        return respondentDetailsList.stream().map(respondentDetails -> Element.<PartyDetails>builder().value(
            buildPartyDetails(respondentDetails)).build()).collect(Collectors.toList());

    }

    private static PartyDetails buildPartyDetails(RespondentDetails respondentDetails) {

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
            .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
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

        return (!"Yes".equalsIgnoreCase(respondentDetails.getAddress().getAddressHistory())) ? YesOrNo.Yes : YesOrNo.No;
    }

    private static String buildPreviousName(RespondentDetails respondentDetails) {

        return "Yes".equalsIgnoreCase(respondentDetails.getPersonalDetails().getHasNameChanged())
            ? respondentDetails.getPersonalDetails().getResPreviousName() : null;
    }

    private static DontKnow buildDateOfBirthUnknown(PersonalDetails personalDetails) {
        return "Yes".equalsIgnoreCase(personalDetails.getIsDateOfBirthUnknown()) ? DontKnow.dontKnow : null;
    }

    private static YesOrNo buildRespondentPlaceOfBirthKnown(PersonalDetails personalDetails) {
        return (!"Yes".equalsIgnoreCase(personalDetails.getRespondentPlaceOfBirthUnknown())) ? YesOrNo.Yes : No;
    }

    private static YesOrNo buildCanYouProvideEmailAddress(RespondentDetails respondentDetails) {

        return "Yes".equalsIgnoreCase(respondentDetails.getRespondentContactDetail().getDonKnowEmailAddress()) ? YesOrNo.No : YesOrNo.Yes;
    }

    private static YesOrNo buildCanYouProvidePhoneNumber(RespondentDetails respondentDetails) {

        return "Yes".equalsIgnoreCase(respondentDetails.getRespondentContactDetail().getDonKnowTelephoneNumber()) ? YesOrNo.No : YesOrNo.Yes;
    }
}
