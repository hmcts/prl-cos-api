package uk.gov.hmcts.reform.prl.mapper.citizen;

import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ApplicantDto;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildApplicantDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.DateofBirth;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

public class CaseDataApplicantElementsMapper {

    private CaseDataApplicantElementsMapper() {
    }

    private static final String ADDRESS_FIELD = "address";
    private static final String EMAIL_FIELD = "email";
    private static final String TELEPHONE_FIELD = "telephone";
    private static final String I_DONT_KNOW = "I dont know";

    public static void updateApplicantElementsForCaseData(CaseData.CaseDataBuilder caseDataBuilder,
                                                      C100RebuildApplicantDetailsElements c100RebuildApplicantDetailsElements) {
        caseDataBuilder
                .applicants(buildApplicants(c100RebuildApplicantDetailsElements));
    }

    private static List<Element<PartyDetails>> buildApplicants(C100RebuildApplicantDetailsElements
                                                                       c100RebuildApplicantDetailsElements) {

        List<ApplicantDto> applicantDtoList = c100RebuildApplicantDetailsElements.getApplicants();

        return applicantDtoList.stream()
                .map(applicantDto -> Element.<PartyDetails>builder().value(buildPartyDetails(applicantDto)).build())
                .collect(Collectors.toList());
    }

    private static PartyDetails buildPartyDetails(ApplicantDto applicantDto) {
        List<String> contactDetailsPrivateList1 = Arrays.stream(applicantDto.getContactDetailsPrivate())
                .collect(Collectors.toList());
        List<String> contactDetailsPrivateList2 = Arrays.stream(applicantDto.getContactDetailsPrivateAlternative())
                .collect(Collectors.toList());
        List<String> contactDetailsPrivateList = Stream.concat(contactDetailsPrivateList1.stream(),
                contactDetailsPrivateList2.stream()).collect(Collectors.toList());
        return PartyDetails
                .builder()
                .firstName(applicantDto.getApplicantFirstName())
                .lastName(applicantDto.getApplicantLastName())
                .previousName(applicantDto.getPersonalDetails().getPreviousFullName())
                .gender(Gender.getDisplayedValueFromEnumString(applicantDto.getPersonalDetails().getGender()))
                .otherGender(applicantDto.getPersonalDetails().getOtherGenderDetails())
                .dateOfBirth(buildDateOfBirth(applicantDto.getPersonalDetails().getDateOfBirth()))
                .placeOfBirth(applicantDto.getPersonalDetails().getApplicantPlaceOfBirth())
                //.relationshipToChildren(buildChildRelationship(applicantDto.getRelationshipDetails()))
                .phoneNumber(isNotEmpty(applicantDto.getApplicantContactDetail().getTelephoneNumber())
                        ? applicantDto.getApplicantContactDetail().getTelephoneNumber() : null)
                .canYouProvideEmailAddress(applicantDto.getApplicantContactDetail().getCanProvideEmail())
                .canYouProvidePhoneNumber(applicantDto.getApplicantContactDetail().getCanProvideTelephoneNumber())
                .email(isNotEmpty(applicantDto.getApplicantContactDetail().getEmailAddress())
                        ? applicantDto.getApplicantContactDetail().getEmailAddress() : null)
                .contactPreferences(ContactPreferences.fromValue(
                    applicantDto.getApplicantContactDetail().getApplicantContactPreferences()))
                .address(buildAddress(applicantDto))
                .isAtAddressLessThan5Years(applicantDto.getApplicantAddressHistory())
                .addressLivedLessThan5YearsDetails(applicantDto.getApplicantProvideDetailsOfPreviousAddresses())
                .isAddressConfidential(buildConfidentialField(contactDetailsPrivateList, ADDRESS_FIELD))
                .isEmailAddressConfidential(buildConfidentialField(contactDetailsPrivateList, EMAIL_FIELD))
                .isPhoneNumberConfidential(buildConfidentialField(contactDetailsPrivateList, TELEPHONE_FIELD))
                .doTheyHaveLegalRepresentation(YesNoDontKnow.no)
                .response(buildApplicantsResponse(applicantDto, contactDetailsPrivateList))
                .build();
    }

    private static Address buildAddress(ApplicantDto applicantDto) {
        return Address
                .builder()
                .addressLine1(applicantDto.getApplicantAddress1())
                .addressLine2(applicantDto.getApplicantAddress2())
                .postTown(applicantDto.getApplicantAddressTown())
                .county(applicantDto.getApplicantAddressCounty())
                .postCode(applicantDto.getApplicantAddressPostcode())
                .build();
    }

    private static YesOrNo buildConfidentialField(List<String> contactDetailsPrivateList, String field) {
        return contactDetailsPrivateList.contains(field) ? Yes : YesOrNo.No;
    }

    private static LocalDate buildDateOfBirth(DateofBirth date) {
        if (isNotEmpty(date) && isNotEmpty(date.getYear()) &&  isNotEmpty(date.getMonth()) && isNotEmpty(date.getDay())) {
            return LocalDate.of(Integer.parseInt(date.getYear()), Integer.parseInt(date.getMonth()),
                    Integer.parseInt(date.getDay()));
        }
        return null;
    }

    private static Response buildApplicantsResponse(ApplicantDto applicantDto,
                                                    List<String> contactDetailsPrivateList) {
        return Response
            .builder()
            .keepDetailsPrivate(buildKeepDetailsPrivate(applicantDto, contactDetailsPrivateList))
            .build();
    }

    private static KeepDetailsPrivate buildKeepDetailsPrivate(ApplicantDto applicantDto,
                                                              List<String> contactDetailsPrivateList) {
        return KeepDetailsPrivate
            .builder()
            .otherPeopleKnowYourContactDetails(I_DONT_KNOW.equalsIgnoreCase(applicantDto.getDetailsKnown())
                                                   ? YesNoDontKnow.dontKnow :
                                                   YesNoDontKnow.getDisplayedValueIgnoreCase(applicantDto.getDetailsKnown()))
            .confidentiality(isNotEmpty(applicantDto.getStart())
                                 ? YesOrNo.getValue(applicantDto.getStart())
                                 : YesOrNo.getValue(applicantDto.getStartAlternative()))
            .confidentialityList(buildConfidentialityList(contactDetailsPrivateList))
            .build();
    }

    private static List<ConfidentialityListEnum> buildConfidentialityList(List<String> contactDetailsPrivateList) {
        if (isNull(contactDetailsPrivateList) || contactDetailsPrivateList.isEmpty()) {
            return Collections.emptyList();
        } else {
            return contactDetailsPrivateList.stream().map(c -> TELEPHONE_FIELD.equals(c)
                ? ConfidentialityListEnum.phoneNumber
                : ConfidentialityListEnum.getValue(c)).collect(
                Collectors.toList());
        }
    }
}
