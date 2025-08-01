package uk.gov.hmcts.reform.prl.clients.ccd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.random.RandomGenerator.getDefault;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100APPLICANTBARRISTER3;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100APPLICANTBARRISTER5;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.FL401APPLICANTBARRISTER;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.FL401RESPONDENTBARRISTER;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentServiceTest {

    @InjectMocks
    private CaseAssignmentService caseAssignmentService;
    private CaseData c100CaseData;
    private CaseData fl401CaseData;
    private Map<String, UUID> partyIds;
    private Map<String, UUID> fl401PartyIds;

    @BeforeEach
    void setUp() {
        partyIds = Map.of("applicant1",  UUID.randomUUID(),
                          "applicant2", UUID.randomUUID(),
                          "applicant3", UUID.randomUUID(),
                          "applicant4", UUID.randomUUID(),
                          "applicant5", UUID.randomUUID(),
                          "respondent1", UUID.randomUUID(),
                          "respondent2", UUID.randomUUID(),
                          "respondent3", UUID.randomUUID(),
                          "respondent4", UUID.randomUUID(),
                          "respondent5", UUID.randomUUID());

        fl401PartyIds = Map.of("fl401Applicant",  UUID.randomUUID(),
                          "fl401Respondent", UUID.randomUUID());

        PartyDetails applicant1 = PartyDetails.builder()
            .firstName("af1").lastName("al1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("afl11@test.com")
            .contactPreferences(ContactPreferences.email)
            .build();
        PartyDetails applicant2 = PartyDetails.builder()
            .firstName("af2").lastName("al2")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("asf2").representativeLastName("asl2")
            .solicitorEmail("asl22@test.com")
            .build();
        PartyDetails applicant3 = PartyDetails.builder()
            .firstName("af3").lastName("al3")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("asf3").representativeLastName("asl3")
            .solicitorEmail("asl33@test.com")
            .build();
        PartyDetails applicant4 = PartyDetails.builder()
            .firstName("af4").lastName("al4")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("asf4").representativeLastName("asl4")
            .solicitorEmail("asl44@test.com")
            .build();
        PartyDetails applicant5 = PartyDetails.builder()
            .firstName("af5").lastName("al5")
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .representativeFirstName("asf5").representativeLastName("asl5")
            .solicitorEmail("asl55@test.com")
            .build();
        PartyDetails respondent1 = PartyDetails.builder()
            .firstName("rf1").lastName("rl1")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("rfl11@test.com")
            .partyId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
            .contactPreferences(ContactPreferences.email)
            .build();
        PartyDetails respondent2 = PartyDetails.builder()
            .firstName("rf2").lastName("rl2")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .email("rfl11@test.com")
            .representativeFirstName("rsf2").representativeLastName("rsl2")
            .solicitorEmail("rsl22@test.com")
            .build();
        PartyDetails respondent3 = PartyDetails.builder()
            .firstName("rf3").lastName("rl3")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("rfl33@test.com")
            .address(Address.builder().addressLine1("test").build())
            .partyId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
            .contactPreferences(ContactPreferences.post)
            .build();
        PartyDetails respondent4 = PartyDetails.builder()
            .firstName("rf4").lastName("rl4")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("rfl44@test.com")
            .address(Address.builder().addressLine1("test").build())
            .partyId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
            .contactPreferences(ContactPreferences.post)
            .build();
        PartyDetails respondent5 = PartyDetails.builder()
            .firstName("rf5").lastName("rl5")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("rfl55@test.com")
            .address(Address.builder().addressLine1("test").build())
            .partyId(UUID.fromString("00000000-0000-0000-0000-000000000000"))
            .contactPreferences(ContactPreferences.post)
            .build();
        PartyDetails otherPerson = PartyDetails.builder()
            .firstName("of").lastName("ol")
            .canYouProvideEmailAddress(YesOrNo.Yes)
            .email("ofl@test.com")
            .build();


        c100CaseData = CaseData.builder()
            .id(getDefault().nextLong())
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(asList(element(partyIds.get("applicant1"), applicant1),
                               element(partyIds.get("applicant2"), applicant2),
                               element(partyIds.get("applicant3"), applicant3),
                               element(partyIds.get("applicant4"), applicant4),
                               element(partyIds.get("applicant5"), applicant5)))
            .respondents(asList(element(partyIds.get("respondent1"), respondent1),
                                element(partyIds.get("respondent2"), respondent2),
                                element(partyIds.get("respondent3"), respondent3),
                                element(partyIds.get("respondent4"), respondent4),
                                element(partyIds.get("respondent5"), respondent5)))
            .othersToNotify(Collections.singletonList(element(otherPerson)))
            .build();

        fl401CaseData = CaseData.builder()
            .id(getDefault().nextLong())
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(applicant1.toBuilder()
                                 .partyId(fl401PartyIds.get("fl401Applicant"))
                                 .build())
            .respondentsFL401(respondent2.toBuilder()
                                  .partyId(fl401PartyIds.get("fl401Respondent"))
                                  .build())
            .build();

    }

    @ParameterizedTest
    @CsvSource({
        "applicant1, [C100APPLICANTBARRISTER1]",
        "applicant2, [C100APPLICANTBARRISTER2]",
        "applicant3, [C100APPLICANTBARRISTER3]",
        "applicant4, [C100APPLICANTBARRISTER4]",
        "applicant5, [C100APPLICANTBARRISTER5]",
        "respondent1, [C100RESPONDENTBARRISTER1]",
        "respondent2, [C100RESPONDENTBARRISTER2]",
        "respondent3, [C100RESPONDENTBARRISTER3]",
        "respondent4, [C100RESPONDENTBARRISTER4]",
        "respondent5, [C100RESPONDENTBARRISTER5]",
    })
    void deriveBarristerRoleForC100CaseData(String party, String barristerRole) {
        CaseData caseData = c100CaseData.toBuilder()
            .allocatedBarrister(AllocatedBarrister.builder()
                                    .partyList(DynamicList.builder()
                                                   .value(DynamicListElement.builder()
                                                              .code(partyIds.get(party))
                                                              .build())
                                                   .build())
                                    .build())
            .build();
        Optional<String> caseRole = caseAssignmentService.deriveBarristerRole(caseData);
        assertThat(caseRole)
            .hasValue(barristerRole);

    }

    @ParameterizedTest
    @CsvSource({
        "fl401Applicant, [APPLICANTBARRISTER]",
        "fl401Respondent, [FL401RESPONDENTBARRISTER]",
    })
    void deriveBarristerRoleForFl401CaseData(String party, String barristerRole) {
        CaseData caseData = fl401CaseData.toBuilder()
            .allocatedBarrister(AllocatedBarrister.builder()
                                    .partyList(DynamicList.builder()
                                                   .value(DynamicListElement.builder()
                                                              .code(fl401PartyIds.get(party))
                                                              .build())
                                                   .build())
                                    .build())
            .build();
        Optional<String> caseRole = caseAssignmentService.deriveBarristerRole(caseData);
        assertThat(caseRole)
            .hasValue(barristerRole);
    }

    static Stream<Arguments> parameterC100Parties() {
        return Stream.of(
            of("applicant3",
               2,
               C100APPLICANTBARRISTER3.getCaseRoleLabel(),
               (Function<CaseData, List<Element<PartyDetails>>>) CaseData::getApplicants),
            of("respondent5",
               4,
               C100APPLICANTBARRISTER5.getCaseRoleLabel(),
               (Function<CaseData, List<Element<PartyDetails>>>) CaseData::getRespondents)
        );
    }

    @ParameterizedTest
    @MethodSource("parameterC100Parties")
    void updatedC100PartiesWithBarristerDetails(String party,
                                                int index,
                                                String barristerRole,
                                                Function<CaseData, List<Element<PartyDetails>>> parties) {
        Barrister barrister = Barrister.builder()
            .barristerEmail("barristerEmail@gmail.com")
            .barristerName("barristerName")
            .barristerOrgId("barristerOrgId")
            .build();
        CaseData caseData = c100CaseData.toBuilder()
            .allocatedBarrister(AllocatedBarrister.builder()
                                    .partyList(DynamicList.builder()
                                                   .value(DynamicListElement.builder()
                                                              .code(partyIds.get(party))
                                                              .build())
                                                   .build())
                                    .barristerEmail(barrister.getBarristerEmail())
                                    .barristerName(barrister.getBarristerName())
                                    .barristerOrg(Organisation.builder()
                                                      .organisationID(barrister.getBarristerOrgId())
                                                      .build())
                                    .build())
            .build();
        String userId = UUID.randomUUID().toString();
        caseAssignmentService.updatedPartyWithBarristerDetails(caseData,
                                                              barristerRole,
                                                               userId);
        List<PartyDetails> partyDetails = ElementUtils.unwrapElements(parties.apply(caseData));

        assertThat(partyDetails.get(index))
            .extracting(PartyDetails::getBarrister)
            .isEqualTo(barrister.toBuilder()
                           .barristerRole(barristerRole)
                           .barristerId(userId)
                           .build());
    }


    static Stream<Arguments> parameterFl401Parties() {
        return Stream.of(
            of("fl401Applicant",
               FL401APPLICANTBARRISTER.getCaseRoleLabel(),
               (Function<CaseData, PartyDetails>) CaseData::getApplicantsFL401),
            of("fl401Respondent",
               FL401RESPONDENTBARRISTER.getCaseRoleLabel(),
               (Function<CaseData, PartyDetails>) CaseData::getRespondentsFL401)
        );
    }

    @ParameterizedTest
    @MethodSource("parameterFl401Parties")
    void updatedFl401PartyWithBarristerDetails(String party,
                                               String barristerRole,
                                               Function<CaseData, PartyDetails> parties) {
        Barrister barrister = Barrister.builder()
            .barristerEmail("barristerEmail@gmail.com")
            .barristerName("barristerName")
            .barristerOrgId("barristerOrgId")
            .build();
        CaseData caseData = fl401CaseData.toBuilder()
            .allocatedBarrister(AllocatedBarrister.builder()
                                    .partyList(DynamicList.builder()
                                                   .value(DynamicListElement.builder()
                                                              .code(fl401PartyIds.get(party))
                                                              .build())
                                                   .build())
                                    .barristerEmail(barrister.getBarristerEmail())
                                    .barristerName(barrister.getBarristerName())
                                    .barristerOrg(Organisation.builder()
                                                      .organisationID(barrister.getBarristerOrgId())
                                                      .build())
                                    .build())
            .build();
        String userId = UUID.randomUUID().toString();
        caseAssignmentService.updatedPartyWithBarristerDetails(caseData,
                                                               barristerRole,
                                                               userId);

        assertThat(parties.apply(caseData))
            .extracting(PartyDetails::getBarrister)
            .isEqualTo(barrister.toBuilder()
                           .barristerRole(barristerRole)
                           .barristerId(userId)
                           .build());
    }
}

