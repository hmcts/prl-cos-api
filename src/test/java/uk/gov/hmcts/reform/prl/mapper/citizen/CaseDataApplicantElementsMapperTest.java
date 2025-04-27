package uk.gov.hmcts.reform.prl.mapper.citizen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ApplicantDto;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildApplicantDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildChildDetailsElements;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildDetail;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ChildRelationship;
import uk.gov.hmcts.reform.prl.models.c100rebuild.ContactDetail;
import uk.gov.hmcts.reform.prl.models.c100rebuild.DateofBirth;
import uk.gov.hmcts.reform.prl.models.c100rebuild.PersonalDetails;
import uk.gov.hmcts.reform.prl.models.c100rebuild.RelationshipDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static uk.gov.hmcts.reform.prl.enums.YesNoIDontKnow.dontKnow;
import static uk.gov.hmcts.reform.prl.enums.YesNoIDontKnow.no;
import static uk.gov.hmcts.reform.prl.enums.YesNoIDontKnow.yes;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.No;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;
import static uk.gov.hmcts.reform.prl.mapper.citizen.CaseDataApplicantElementsMapper.updateApplicantElementsForCaseData;

class CaseDataApplicantElementsMapperTest {

    private C100RebuildApplicantDetailsElements applicantDetails;
    private C100RebuildChildDetailsElements childDetails;
    private CaseData.CaseDataBuilder<?, ?> caseDataBuilder;

    @BeforeEach
    void setUp() {
        ApplicantDto applicant = buildBasicApplicantDto();
        applicantDetails = buildApplicantDetailsElements(List.of(applicant));
        childDetails = buildC100RebuildChildDetailsElements();
        caseDataBuilder = CaseData.builder();
    }

    @Test
    void shouldBuildApplicantsWithPcqIdSet() {
        // given
        String expectedPcqId = UUID.randomUUID().toString();

        // when
        updateApplicantElementsForCaseData(caseDataBuilder, applicantDetails, childDetails, expectedPcqId);
        CaseData caseData = caseDataBuilder.build();

        // then
        PartyDetails partyDetails = caseData.getApplicants().getFirst().getValue();
        assertEquals(expectedPcqId, partyDetails.getUser().getPcqId());
    }

    @Test
    void shouldSetPartialConfidentialityBasedOnContactDetailsPrivate() {
        // given
        ApplicantDto applicant = buildBasicApplicantDto();
        applicant.setDetailsKnown("yes");
        applicant.setStartAlternative("Yes");
        applicant.setContactDetailsPrivate(new String[]{"email"});
        applicantDetails = buildApplicantDetailsElements(List.of(applicant));

        // when
        updateApplicantElementsForCaseData(caseDataBuilder, applicantDetails, childDetails, null);
        CaseData caseData = caseDataBuilder.build();

        // then
        PartyDetails partyDetails = caseData.getApplicants().getFirst().getValue();
        assertEquals(Yes, partyDetails.getIsEmailAddressConfidential());
        assertEquals(No, partyDetails.getIsAddressConfidential());
        assertEquals(No, partyDetails.getIsPhoneNumberConfidential());

        assertEquals(yes, partyDetails.getResponse().getKeepDetailsPrivate().getOtherPeopleKnowYourContactDetails());
        assertEquals(Yes, partyDetails.getResponse().getKeepDetailsPrivate().getConfidentiality());
        assertEquals("email", partyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().getFirst().getId());
        assertEquals(1, partyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().size());
    }

    @Test
    void shouldSetAllValuesConfidentialityBasedOnContactDetailsPrivate() {
        // given
        ApplicantDto applicant = buildBasicApplicantDto();
        applicant.setDetailsKnown("no");
        applicant.setStartAlternative("Yes");
        applicant.setContactDetailsPrivateAlternative(new String[]{"email", "address", "telephone"});
        applicantDetails = buildApplicantDetailsElements(List.of(applicant));

        // when
        updateApplicantElementsForCaseData(caseDataBuilder, applicantDetails, childDetails, null);
        CaseData caseData = caseDataBuilder.build();

        // then
        PartyDetails partyDetails = caseData.getApplicants().getFirst().getValue();
        assertEquals(Yes, partyDetails.getIsAddressConfidential());
        assertEquals(Yes, partyDetails.getIsEmailAddressConfidential());
        assertEquals(Yes, partyDetails.getIsPhoneNumberConfidential());

        assertEquals(no, partyDetails.getResponse().getKeepDetailsPrivate().getOtherPeopleKnowYourContactDetails());
        assertEquals(Yes, partyDetails.getResponse().getKeepDetailsPrivate().getConfidentiality());
        assertEquals("email", partyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().getFirst().getId());
        assertEquals("address", partyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().get(1).getId());
        assertEquals("phoneNumber", partyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().get(2).getId());
        assertEquals(3, partyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().size());

    }

    @Test
    void shouldSetValuesNotConfidentialBasedOnContactDetailsPrivate() {
        // given
        ApplicantDto applicant = buildBasicApplicantDto();
        applicant.setDetailsKnown("Yes");
        applicantDetails = buildApplicantDetailsElements(List.of(applicant));

        // when
        updateApplicantElementsForCaseData(caseDataBuilder, applicantDetails, childDetails, null);
        CaseData caseData = caseDataBuilder.build();

        // then
        PartyDetails partyDetails = caseData.getApplicants().getFirst().getValue();
        assertEquals(No, partyDetails.getIsEmailAddressConfidential());
        assertEquals(No, partyDetails.getIsAddressConfidential());
        assertEquals(No, partyDetails.getIsPhoneNumberConfidential());

        assertEquals(yes, partyDetails.getResponse().getKeepDetailsPrivate().getOtherPeopleKnowYourContactDetails());
        assertEquals(No, partyDetails.getResponse().getKeepDetailsPrivate().getConfidentiality());
        assertThat(partyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList()).isEmpty();
    }

    @Test
    void shouldSetAllDetailsConfidentialWhenLiveInRefuge() {
        // given
        ApplicantDto applicant = buildBasicApplicantDto();
        applicant.setLiveInRefuge(Yes);
        applicantDetails = buildApplicantDetailsElements(List.of(applicant));

        // when
        updateApplicantElementsForCaseData(caseDataBuilder, applicantDetails, childDetails, null);
        CaseData caseData = caseDataBuilder.build();

        // then
        PartyDetails partyDetails = caseData.getApplicants().getFirst().getValue();

        assertEquals(Yes, partyDetails.getIsAddressConfidential());
        assertEquals(Yes, partyDetails.getIsEmailAddressConfidential());
        assertEquals(Yes, partyDetails.getIsPhoneNumberConfidential());

        assertEquals(dontKnow, partyDetails.getResponse().getKeepDetailsPrivate().getOtherPeopleKnowYourContactDetails());
        assertEquals(Yes, partyDetails.getResponse().getKeepDetailsPrivate().getConfidentiality());
        assertEquals("email", partyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().getFirst().getId());
        assertEquals("address", partyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().get(1).getId());
        assertEquals("phoneNumber", partyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().get(2).getId());
        assertEquals(3, partyDetails.getResponse().getKeepDetailsPrivate().getConfidentialityList().size());
    }

    @Test
    void shouldHandleNullDateOfBirthGracefully() {
        // given
        ApplicantDto applicant = buildBasicApplicantDto();
        applicant.getPersonalDetails().setDateOfBirth(null);
        applicantDetails = buildApplicantDetailsElements(List.of(applicant));

        // when
        updateApplicantElementsForCaseData(caseDataBuilder, applicantDetails, childDetails, null);
        CaseData caseData = caseDataBuilder.build();

        // then
        PartyDetails partyDetails = caseData.getApplicants().getFirst().getValue();
        assertNull(partyDetails.getDateOfBirth());
    }

    private ApplicantDto buildBasicApplicantDto() {
        return ApplicantDto.builder()
            .applicantFirstName("Test")
            .applicantLastName("Test")
            .personalDetails(PersonalDetails.builder()
                                 .previousFullName("test")
                                 .gender("Male")
                                 .dateOfBirth(new DateofBirth("1985", "05", "15"))
                                 .build())
            .applicantContactDetail(ContactDetail.builder()
                                        .telephoneNumber("0123456789")
                                        .emailAddress("test@example.com")
                                        .canProvideEmail(Yes)
                                        .canProvideTelephoneNumber(Yes)
                                        .applicantContactPreferences("Email")
                                        .build())
            .contactDetailsPrivate(new String[]{})
            .contactDetailsPrivateAlternative(new String[]{})
            .detailsKnown("dontKnow")
            .relationshipDetails(new RelationshipDetails(List.of(ChildRelationship.builder().build())))
            .liveInRefuge(No)
            .startAlternative("No")
            .build();
    }

    private C100RebuildApplicantDetailsElements buildApplicantDetailsElements(List<ApplicantDto> applicants) {
        return C100RebuildApplicantDetailsElements.builder()
            .applicants(applicants)
            .build();
    }

    private C100RebuildChildDetailsElements buildC100RebuildChildDetailsElements() {
        return C100RebuildChildDetailsElements.builder()
            .childDetails(List.of(ChildDetail.builder()
                                      .id("child1")
                                      .build()))
            .build();
    }

}
