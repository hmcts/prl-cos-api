package uk.gov.hmcts.reform.prl.controllers.citizen.mapper;

import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ApplicantDto;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildOtherPersonDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.DateofBirth;
import uk.gov.hmcts.reform.prl.models.c100rebuild.OtherPersonDetail;
import uk.gov.hmcts.reform.prl.models.c100rebuild.PersonalDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public class CaseDataOtherPersonsElementsMapper {
    private static final String ADDRESS_FIELD = "address";
    private static final String EMAIL_FIELD = "email";
    private static final String TELEPHONE_FIELD = "telephone";

    public static void updateOtherPersonDetailsElementsForCaseData(CaseData.CaseDataBuilder caseDataBuilder,
                                                 C100RebuildOtherPersonDetailsElements c100RebuildOtherPersonDetailsElements) {
        caseDataBuilder
                .othersToNotify(buildOtherPeople(c100RebuildOtherPersonDetailsElements));
    }

    private static List<Element<PartyDetails>> buildOtherPeople(C100RebuildOtherPersonDetailsElements
                                                                       c100RebuildOtherPersonDetailsElements) {

        List<OtherPersonDetail> otherPersonDetailsList = c100RebuildOtherPersonDetailsElements.getOtherPersonDetails();

        List<Element<PartyDetails>> elementList = otherPersonDetailsList.stream()
                .map(OtherPersonDetail -> Element.<PartyDetails>builder().value(buildPartyDetails(OtherPersonDetail)).build())
                .collect(Collectors.toList());

        return elementList;
    }

    private static PartyDetails buildPartyDetails(OtherPersonDetail otherPersonDetail) {
        PersonalDetails personalDetails = otherPersonDetail.getPersonalDetails();
        return PartyDetails
                .builder()
                .firstName(otherPersonDetail.getFirstName())
                .lastName(otherPersonDetail.getLastName())
                .previousName(personalDetails.getPreviousFullName())
                .gender(Gender.valueOf(personalDetails.getGender()))
                .dateOfBirth("No".equalsIgnoreCase(personalDetails.getIsDateOfBirthUnknown())
                        ? buildDateOfBirth(personalDetails.getDateOfBirth())
                        : buildDateOfBirth(personalDetails.getApproxDateOfBirth()))
                //TODO
//                .address(buildAddress())
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
        return contactDetailsPrivateList.contains(field) ? YesOrNo.Yes : YesOrNo.No;
    }

    private static LocalDate buildDateOfBirth(DateofBirth date) {
        if (isNotEmpty(date) && isNotEmpty(date.getYear()) &&  isNotEmpty(date.getMonth()) && isNotEmpty(date.getDay())) {
            return LocalDate.of(Integer.parseInt(date.getYear()), Integer.parseInt(date.getMonth()),
                    Integer.parseInt(date.getDay()));
        }
        return null;
    }

}