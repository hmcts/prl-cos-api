package uk.gov.hmcts.reform.prl.clients.ccd;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import feign.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CaseAssignmentApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRoleWithOrganisation;
import uk.gov.hmcts.reform.ccd.client.model.CaseAssignmentUserRolesRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.enums.ContactPreferences;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.exception.GrantCaseAccessException;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrgSolicitors;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.SolicitorUser;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentResponse;
import uk.gov.hmcts.reform.prl.models.roleassignment.getroleassignment.RoleAssignmentServiceResponse;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.RoleAssignmentService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100APPLICANTBARRISTER1;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100APPLICANTBARRISTER3;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.C100APPLICANTBARRISTER5;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.FL401APPLICANTBARRISTER;
import static uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole.FL401RESPONDENTBARRISTER;
import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentServiceTest {

    @Mock
    private RoleAssignmentService roleAssignmentService;
    @Mock
    private CaseAssignmentApi caseAssignmentApi;
    @Mock
    private SystemUserService systemUserService;
    @Mock
    private AuthTokenGenerator tokenGenerator;
    @Mock
    private OrganisationService organisationService;
    @Captor
    private ArgumentCaptor<CaseAssignmentUserRolesRequest> caseAssignmentUserRolesRequestArgumentCaptor;

    @InjectMocks
    private CaseAssignmentService caseAssignmentService;
    private CaseData c100CaseData;
    private CaseData fl401CaseData;
    private Map<String, UUID> partyIds;
    private Map<String, UUID> fl401PartyIds;
    private Barrister barrister;
    private CaseDetails caseDetails;

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

        caseDetails = CaseDetails.builder()
            .data(Map.of(
                "caApplicant1", """
                      {
                        "firstName": "af1",
                        "lastName": "al1"
                      }
                    """,
                "caApplicant2", """
                      {
                        "firstName": "af2",
                        "lastName": "al2"
                      }
                    """,
                "caApplicant3", """
                      {
                        "firstName": "af3",
                        "lastName": "al3"
                      }
                    """,
                "caApplicant4", """
                      {
                        "firstName": "af4",
                        "lastName": "al4"
                      }
                    """,
                "caApplicant5", """
                    {
                        "firstName": "af5",
                        "lastName": "al5"
                      }
                    """,
                "caRespondent1", """
                   {
                        "firstName": "rf1",
                        "lastName": "rl1"
                      }
                    """,
                "caRespondent2", """
                    {
                        "firstName": "rf2",
                        "lastName": "rl2"
                      }
                    """,
                "caRespondent3", """
                    {
                        "firstName": "rf3",
                        "lastName": "rl3"
                      }
                    """,
                "caRespondent4", """
                    {
                        "firstName": "rf4",
                        "lastName": "rl4"
                      }
                    """,
                "caRespondent5", """
                    {
                        "firstName": "rf5",
                        "lastName": "rl5"
                      }
                    """
            ))
            .build();

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

        barrister = Barrister.builder()
            .barristerEmail("barristerEmail@gmail.com")
            .barristerFirstName("barristerName")
            .barristerLastName("barristerLastName")
            .barristerOrg(Organisation.builder()
                              .organisationID("barristerOrgId")
                              .organisationName("barristerOrgName")
                              .build())
            .build();
    }




    @ParameterizedTest
    @CsvSource({
        "applicant1, [C100APPLICANTBARRISTER1]",
        "respondent2, [C100RESPONDENTBARRISTER2]",
        "respondent3, [C100RESPONDENTBARRISTER3]",
        "respondent4, [C100RESPONDENTBARRISTER4]",
        "applicant4, [C100APPLICANTBARRISTER4]",
        "applicant5, [C100APPLICANTBARRISTER5]",
        "respondent1, [C100RESPONDENTBARRISTER1]",
        "respondent5, [C100RESPONDENTBARRISTER5]",
        "applicant2, [C100APPLICANTBARRISTER2]",
        "applicant3, [C100APPLICANTBARRISTER3]",
    })
    void deriveBarristerRoleForC100CaseData(String party, String barristerRole) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        CaseAssignmentService localCaseAssignmentService = new CaseAssignmentService(
            caseAssignmentApi,
            systemUserService,
            tokenGenerator,
            organisationService,
            roleAssignmentService,
            objectMapper
        );
        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder()
            .partyList(DynamicList.builder()
                           .value(DynamicListElement.builder()
                                      .code(partyIds.get(party))
                                      .build())
                           .build())
            .build();

        Optional<String> caseRole = localCaseAssignmentService.deriveBarristerRole(
            caseDetails.getData(), c100CaseData,
            allocatedBarrister);
        assertThat(caseRole)
            .hasValue(barristerRole);

    }

    @ParameterizedTest
    @CsvSource({
        "fl401Applicant, [APPLICANTBARRISTER]",
        "fl401Respondent, [FL401RESPONDENTBARRISTER]",
    })
    void deriveBarristerRoleForFl401CaseData(String party, String barristerRole) {
        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder()
            .partyList(DynamicList.builder()
                           .value(DynamicListElement.builder()
                                      .code(fl401PartyIds.get(party))
                                      .build())
                           .build())
            .build();

        Optional<String> caseRole = caseAssignmentService.deriveBarristerRole(
            caseDetails.getData(), fl401CaseData,
            allocatedBarrister);
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
    void testC100AddBarrister(String party,
                              int index,
                              String barristerRole,
                              Function<CaseData, List<Element<PartyDetails>>> parties) {
        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder()
            .partyList(DynamicList.builder()
                           .value(DynamicListElement.builder()
                                      .code(partyIds.get(party))
                                      .build())
                           .build())
            .barristerOrg(Organisation.builder()
                              .organisationID(barrister.getBarristerOrg()
                                                  .getOrganisationID())
                              .organisationName(barrister.getBarristerOrg()
                                                    .getOrganisationName())
                              .build())
            .barristerEmail(barrister.getBarristerEmail())
            .barristerFirstName(barrister.getBarristerFirstName())
            .barristerLastName(barrister.getBarristerLastName())
            .build();

        String userId = UUID.randomUUID().toString();

        caseAssignmentService.addBarrister(c100CaseData,
                                           userId,
                                           barristerRole,
                                           allocatedBarrister);

        List<PartyDetails> partyDetails = ElementUtils.unwrapElements(parties.apply(c100CaseData));

        verify(caseAssignmentApi).addCaseUserRoles(
            any(),
            any(),
            caseAssignmentUserRolesRequestArgumentCaptor.capture());


        CaseAssignmentUserRolesRequest expectedCaseAssignmentUserRolesRequest = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(List.of(CaseAssignmentUserRoleWithOrganisation.builder()
                    .caseDataId(String.valueOf(c100CaseData.getId()))
                    .organisationId(barrister.getBarristerOrg().getOrganisationID())
                    .userId(userId)
                    .caseRole(barristerRole)
                .build()))
            .build();

        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest = caseAssignmentUserRolesRequestArgumentCaptor.getValue();
        assertThat(caseAssignmentUserRolesRequest.getCaseAssignmentUserRolesWithOrganisation())
            .contains(expectedCaseAssignmentUserRolesRequest.getCaseAssignmentUserRolesWithOrganisation().getFirst());

        assertThat(partyDetails.get(index))
            .extracting(PartyDetails::getBarrister)
            .isEqualTo(barrister.toBuilder()
                .barristerRole(barristerRole)
                .barristerId(userId)
                .build());

    }

    @Test
    void testAddBarristerWhenFailure() {
        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder()
            .partyList(DynamicList.builder()
                           .value(DynamicListElement.builder()
                                      .code(partyIds.get("applicant3"))
                                      .build())
                           .build())
            .barristerOrg(Organisation.builder()
                              .organisationID(barrister.getBarristerOrg()
                                                  .getOrganisationID())
                              .organisationName(barrister.getBarristerOrg()
                                                    .getOrganisationName())
                              .build())
            .barristerEmail(barrister.getBarristerEmail())
            .barristerFirstName(barrister.getBarristerFirstName())
            .barristerLastName(barrister.getBarristerLastName())
            .build();

        String userId = "ac357f74-9389-4331-aaa9-a5f8bc3cbe67";
        Barrister updatedBarrister = barrister.toBuilder()
            .barristerRole("[C100APPLICANTBARRISTER3]")
            .barristerId(UUID.randomUUID().toString())
            .build();

        c100CaseData.getApplicants().get(2).getValue()
            .setBarrister(updatedBarrister);

        when(systemUserService.getSysUserToken())
            .thenReturn("sysUserToken");
        when(tokenGenerator.generate())
            .thenReturn("token");
        when(caseAssignmentApi.addCaseUserRoles(anyString(),
                                                   anyString(),
                                                   isA(CaseAssignmentUserRolesRequest.class)))
            .thenThrow(FeignException.errorStatus("addCaseUserRoles", Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(Request.create(HttpMethod.POST, "/case-users", Map.of(), null, null, null))
                .build()));
        String caseRoleLabel = C100APPLICANTBARRISTER3.getCaseRoleLabel();
        assertThatThrownBy(() -> caseAssignmentService.addBarrister(c100CaseData,
                                                                    userId,
                                                                    caseRoleLabel,
                                                                    allocatedBarrister))
            .isInstanceOf(GrantCaseAccessException.class)
            .hasMessageContaining("User(s) [ac357f74-9389-4331-aaa9-a5f8bc3cbe67] not granted [C100APPLICANTBARRISTER3] to case");
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
    void testFl401AddBarrister(String party,
                               String barristerRole,
                               Function<CaseData, PartyDetails> parties) {
        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder()
            .partyList(DynamicList.builder()
                           .value(DynamicListElement.builder()
                                      .code(fl401PartyIds.get(party))
                                      .build())
                           .build())
            .barristerEmail(barrister.getBarristerEmail())
            .barristerFirstName(barrister.getBarristerFirstName())
            .barristerLastName(barrister.getBarristerLastName())
            .barristerOrg(Organisation.builder()
                              .organisationID(barrister.getBarristerOrg()
                                                  .getOrganisationID())
                              .organisationName(barrister.getBarristerOrg()
                                                    .getOrganisationName())
                              .build())
            .build();

        String userId = UUID.randomUUID().toString();

        caseAssignmentService.addBarrister(fl401CaseData,
                                           userId,
                                           barristerRole,
                                           allocatedBarrister);

        PartyDetails partyDetails = parties.apply(fl401CaseData);

        verify(caseAssignmentApi).addCaseUserRoles(
            any(),
            any(),
            caseAssignmentUserRolesRequestArgumentCaptor.capture());


        CaseAssignmentUserRolesRequest expectedCaseAssignmentUserRolesRequest = CaseAssignmentUserRolesRequest.builder()
            .caseAssignmentUserRolesWithOrganisation(List.of(CaseAssignmentUserRoleWithOrganisation.builder()
                                                                 .caseDataId(String.valueOf(fl401CaseData.getId()))
                                                                 .organisationId(barrister.getBarristerOrg().getOrganisationID())
                                                                 .userId(userId)
                                                                 .caseRole(barristerRole)
                                                                 .build()))
            .build();

        CaseAssignmentUserRolesRequest caseAssignmentUserRolesRequest = caseAssignmentUserRolesRequestArgumentCaptor.getValue();
        assertThat(caseAssignmentUserRolesRequest.getCaseAssignmentUserRolesWithOrganisation())
            .contains(expectedCaseAssignmentUserRolesRequest.getCaseAssignmentUserRolesWithOrganisation().getFirst());

        assertThat(partyDetails.getBarrister())
            .isEqualTo(barrister.toBuilder()
                           .barristerRole(barristerRole)
                           .barristerId(userId)
                           .build());
    }

    @Test
    void testValidateAddUserIdIsEmpty() {
        List<String> errorList = new ArrayList<>();
        caseAssignmentService.validateAddRequest(Optional.empty(),
                                                 CaseData.builder().id(1234L).build(),
                                                 Optional.of(C100APPLICANTBARRISTER3.getCaseRoleLabel()),
                                                 AllocatedBarrister.builder().build(),
                                                 errorList);
        assertThat(errorList)
            .contains("Could not find barrister with provided email");
    }

    @Test
    void testValidateAddBarristerRoleIsEmpty() {
        List<String> errorList = new ArrayList<>();
        caseAssignmentService.validateAddRequest(Optional.of("5678"),
                                                 CaseData.builder().id(1234L).build(),
                                                 Optional.empty(),
                                                 AllocatedBarrister.builder()
                                                     .partyList(DynamicList.builder()
                                                                    .value(DynamicListElement.builder()
                                                                               .code("3333")
                                                                               .build())
                                                                    .build())
                                                         .build(),
                                                 errorList);
        assertThat(errorList)
            .contains("Could not map to barrister case role");
    }

    @Test
    void testValidateAddWithNoErrors() {
        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder()
            .partyList(DynamicList.builder()
                           .value(DynamicListElement.builder()
                                      .code(UUID.randomUUID().toString())
                                      .build())
                           .build())
            .barristerOrg(Organisation.builder()
                              .organisationID(barrister.getBarristerOrg()
                                                  .getOrganisationID())
                              .organisationName(barrister.getBarristerOrg()
                                                    .getOrganisationName())
                              .build())
            .barristerEmail(barrister.getBarristerEmail())
            .barristerFirstName(barrister.getBarristerFirstName())
            .barristerLastName(barrister.getBarristerLastName())
            .build();

        when(organisationService.getOrganisationSolicitorDetails(any(),
                                                                 eq(allocatedBarrister.getBarristerOrg().getOrganisationID())))
            .thenReturn(OrgSolicitors.builder()
                            .users(List.of(
                                SolicitorUser.builder()
                                    .email(barrister.getBarristerEmail())
                                    .build())
                            ).build());
        when(roleAssignmentService.getRoleAssignmentForCase(anyString()))
            .thenReturn(RoleAssignmentServiceResponse.builder()
                            .roleAssignmentResponse(List.of(
                                getRoleAssignmentResponse(
                                    UUID.randomUUID().toString(),
                                    C100APPLICANTBARRISTER1.getCaseRoleLabel()
                                ),
                                getRoleAssignmentResponse(
                                    UUID.randomUUID().toString(),
                                    FL401RESPONDENTBARRISTER.getCaseRoleLabel()
                                )
                            ))
                            .build());

        List<String> errorList = new ArrayList<>();
        caseAssignmentService.validateAddRequest(Optional.of("5678"),
                                                 CaseData.builder().id(1234L).build(),
                                                 Optional.of(C100APPLICANTBARRISTER3.getCaseRoleLabel()),
                                                 allocatedBarrister,
                                                 errorList);
        assertThat(errorList)
            .isEmpty();
    }

    @ParameterizedTest
    @MethodSource("parameterC100Parties")
    void testValidC100RemoveBarristerRequest(String party,
                                             int index,
                                             String barristerRole,
                                             Function<CaseData, List<Element<PartyDetails>>> parties) {
        Barrister updatedBarrister = barrister.toBuilder()
            .barristerRole(barristerRole)
            .barristerId(UUID.randomUUID().toString())
            .build();

        parties.apply(c100CaseData).get(index).getValue()
            .setBarrister(updatedBarrister);

        when(roleAssignmentService.getRoleAssignmentForCase(String.valueOf(c100CaseData.getId())))
            .thenReturn(RoleAssignmentServiceResponse.builder()
                            .roleAssignmentResponse(List.of(
                              getRoleAssignmentResponse(UUID.randomUUID().toString(), "[C100APPLICANTBARRISTER2]"),
                              getRoleAssignmentResponse(updatedBarrister.getBarristerId(), updatedBarrister.getBarristerRole())
                            ))
                            .build());
        List<String> errors = new ArrayList<>();
        caseAssignmentService.validateRemoveRequest(c100CaseData,
                                                    partyIds.get(party).toString(),
                                                    errors);
        assertThat(errors)
            .isEmpty();
    }

    @ParameterizedTest
    @MethodSource("parameterFl401Parties")
    void testValidFl401RemoveBarristerRequest(String party,
                                          String barristerRole,
                                          Function<CaseData, PartyDetails> parties) {
        Barrister updatedBarrister = barrister.toBuilder()
            .barristerRole(barristerRole)
            .barristerId(UUID.randomUUID().toString())
            .build();

        parties.apply(fl401CaseData)
            .setBarrister(updatedBarrister);

        when(roleAssignmentService.getRoleAssignmentForCase(String.valueOf(fl401CaseData.getId())))
            .thenReturn(RoleAssignmentServiceResponse.builder()
                            .roleAssignmentResponse(List.of(
                              getRoleAssignmentResponse(UUID.randomUUID().toString(), "[C100APPLICANTBARRISTER2]"),
                              getRoleAssignmentResponse(updatedBarrister.getBarristerId(), updatedBarrister.getBarristerRole())
                            ))
                            .build());
        List<String> errors = new ArrayList<>();
        caseAssignmentService.validateRemoveRequest(fl401CaseData,
                                                    fl401PartyIds.get(party).toString(),
                                                    errors);
        assertThat(errors)
            .isEmpty();
    }

    @Test
    void testInvalidCaseType() {
        CaseData caseData = c100CaseData.toBuilder()
            .caseTypeOfApplication("NOT_VALID")
            .build();
        String id = UUID.randomUUID().toString();
        assertThatThrownBy(() -> caseAssignmentService.removeBarrister(caseData,
                                                                       id))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid case type");
    }

    @ParameterizedTest
    @MethodSource("parameterC100Parties")
    void testC100RemoveBarristerWhenSuccessful(String party,
                                               int index,
                                               String barristerRole,
                                               Function<CaseData, List<Element<PartyDetails>>> parties) {
        Barrister updatedBarrister = barrister.toBuilder()
            .barristerRole(barristerRole)
            .barristerId(UUID.randomUUID().toString())
            .build();

        parties.apply(c100CaseData).get(index).getValue()
            .setBarrister(updatedBarrister);

        when(systemUserService.getSysUserToken())
            .thenReturn("sysUserToken");
        when(tokenGenerator.generate())
            .thenReturn("token");

        caseAssignmentService.removeBarrister(c100CaseData,
                                              partyIds.get(party).toString());
        verify(caseAssignmentApi).removeCaseUserRoles(anyString(),
                                                     anyString(),
                                                     isA(CaseAssignmentUserRolesRequest.class));
    }


    @ParameterizedTest
    @MethodSource("parameterFl401Parties")
    void testFl401RemoveBarristerWhenSuccessful(String party,
                                               String barristerRole,
                                               Function<CaseData, PartyDetails> parties) {
        Barrister updatedBarrister = barrister.toBuilder()
            .barristerRole(barristerRole)
            .barristerId(UUID.randomUUID().toString())
            .build();

        parties.apply(fl401CaseData)
            .setBarrister(updatedBarrister);

        when(systemUserService.getSysUserToken())
            .thenReturn("sysUserToken");
        when(tokenGenerator.generate())
            .thenReturn("token");

        caseAssignmentService.removeBarrister(fl401CaseData,
                                              fl401PartyIds.get(party).toString());
        verify(caseAssignmentApi).removeCaseUserRoles(anyString(),
                                                     anyString(),
                                                     isA(CaseAssignmentUserRolesRequest.class));
    }

    @Test
    void testC100RemoveBarristerWhenFailure() {
        Barrister updatedBarrister = barrister.toBuilder()
            .barristerRole("[C100APPLICANTBARRISTER3]")
            .barristerId(UUID.randomUUID().toString())
            .build();

        c100CaseData.getApplicants().get(2).getValue()
            .setBarrister(updatedBarrister);

        when(systemUserService.getSysUserToken())
            .thenReturn("sysUserToken");
        when(tokenGenerator.generate())
            .thenReturn("token");
        when(caseAssignmentApi.removeCaseUserRoles(anyString(),
                                                      anyString(),
                                                      isA(CaseAssignmentUserRolesRequest.class)))
            .thenThrow(FeignException.errorStatus("removeCaseUserRoles", Response.builder()
                .status(500)
                .reason("Internal Server Error")
                .request(Request.create(HttpMethod.DELETE, "/case-users", Map.of(), null, null, null))
                .build()));
        String partyId = partyIds.get("applicant3").toString();
        assertThatThrownBy(() -> caseAssignmentService.removeBarrister(c100CaseData,
                                                                       partyId
        ))
            .isInstanceOf(GrantCaseAccessException.class)
            .hasMessageContaining("Could not remove the user");
    }


    @Test
    void testRemoveBarristerWhenRoleNotAssociated() {
        Barrister updatedBarrister = barrister.toBuilder()
            .barristerRole("[C100APPLICANTBARRISTER3]")
            .barristerId(UUID.randomUUID().toString())
            .barristerEmail("barristerEmail@gamil.com")
            .build();

        c100CaseData.getApplicants().get(2).getValue()
            .setBarrister(updatedBarrister);

        when(roleAssignmentService.getRoleAssignmentForCase(String.valueOf(c100CaseData.getId())))
            .thenReturn(RoleAssignmentServiceResponse.builder()
                            .roleAssignmentResponse(List.of(
                              getRoleAssignmentResponse(UUID.randomUUID().toString(), "[C100APPLICANTBARRISTER2]"),
                              getRoleAssignmentResponse(updatedBarrister.getBarristerId(), "[C100APPLICANTBARRISTER4]")
                            ))
                            .build());
        List<String> errors = new ArrayList<>();
        caseAssignmentService.validateRemoveRequest(c100CaseData,
                                                    partyIds.get("applicant3").toString(),
                                                    errors);
        assertThat(errors)
            .contains("Barrister is not associated with the case");
    }

    @Test
    void testRemoveBarristerWhenNotPresentInRoleAssignment() {
        Barrister updatedBarrister = barrister.toBuilder()
            .barristerRole("[C100APPLICANTBARRISTER3]")
            .barristerId(UUID.randomUUID().toString())
            .barristerEmail("barristerEmail@gamil.com")
            .build();

        c100CaseData.getApplicants().get(2).getValue()
            .setBarrister(updatedBarrister);

        when(roleAssignmentService.getRoleAssignmentForCase(String.valueOf(c100CaseData.getId())))
            .thenReturn(RoleAssignmentServiceResponse.builder()
                            .roleAssignmentResponse(List.of(
                              getRoleAssignmentResponse(UUID.randomUUID().toString(), "[C100APPLICANTBARRISTER2]"),
                              getRoleAssignmentResponse(UUID.randomUUID().toString(), updatedBarrister.getBarristerRole())
                            ))
                            .build());
        List<String> errors = new ArrayList<>();
        caseAssignmentService.validateRemoveRequest(c100CaseData,
                                                    partyIds.get("applicant3").toString(),
                                                    errors);
        assertThat(errors)
            .contains("Barrister is not associated with the case");
    }

    @Test
    void testRemoveBarristerThrowErrorsWhenSelectedPartyNotFound() {
        List<String> errors = new ArrayList<>();
        String id = UUID.randomUUID().toString();
        assertThatThrownBy(() -> caseAssignmentService.validateRemoveRequest(c100CaseData,
                                                                             id,
                                                    errors))
            .hasMessageContaining("Invalid party selected")
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void testBarristerBelongsToOrganisation() {
        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder()
            .partyList(DynamicList.builder()
                           .value(DynamicListElement.builder()
                                      .code(UUID.randomUUID().toString())
                                      .build())
                           .build())
            .barristerOrg(Organisation.builder()
                              .organisationID(barrister.getBarristerOrg()
                                                  .getOrganisationID())
                              .organisationName(barrister.getBarristerOrg()
                                                    .getOrganisationName())
                              .build())
            .barristerEmail(barrister.getBarristerEmail())
            .barristerFirstName(barrister.getBarristerFirstName())
            .barristerLastName(barrister.getBarristerLastName())
            .build();

        when(organisationService.getOrganisationSolicitorDetails(any(),
                                                                 eq(allocatedBarrister.getBarristerOrg().getOrganisationID())))
            .thenReturn(OrgSolicitors.builder()
                            .users(List.of(
                                SolicitorUser.builder()
                                    .email(barrister.getBarristerEmail())
                                    .build())
                            ).build());
        List<String> errors = new ArrayList<>();
        caseAssignmentService.validateBarristerOrgRelationship(c100CaseData, allocatedBarrister, errors);
        assertThat(errors).isEmpty();
    }

    @Test
    void testBarristerDoesNotBelongsToOrganisation() {
        AllocatedBarrister allocatedBarrister = AllocatedBarrister.builder()
            .partyList(DynamicList.builder()
                           .value(DynamicListElement.builder()
                                      .code(UUID.randomUUID().toString())
                                      .build())
                           .build())
            .barristerOrg(Organisation.builder()
                              .organisationID(barrister.getBarristerOrg()
                                                  .getOrganisationID())
                              .organisationName(barrister.getBarristerOrg()
                                                    .getOrganisationName())
                              .build())
            .barristerEmail("notpresent@mailinator.com")
            .barristerFirstName(barrister.getBarristerFirstName())
            .barristerLastName(barrister.getBarristerLastName())
            .build();

        when(organisationService.getOrganisationSolicitorDetails(any(),
                                                                 eq(allocatedBarrister.getBarristerOrg().getOrganisationID())))
            .thenReturn(OrgSolicitors.builder()
                            .users(List.of(
                                SolicitorUser.builder()
                                    .email(barrister.getBarristerEmail())
                                    .build())
                            ).build());
        List<String> errors = new ArrayList<>();
        caseAssignmentService.validateBarristerOrgRelationship(c100CaseData, allocatedBarrister, errors);
        assertThat(errors)
            .contains("Barrister doesn't belong to selected organisation");
    }

    @Test
    void testBarristerIsAssociatedWithTheCase() {
        when(roleAssignmentService.getRoleAssignmentForCase(anyString()))
            .thenReturn(RoleAssignmentServiceResponse.builder()
                            .roleAssignmentResponse(List.of(
                                getRoleAssignmentResponse(UUID.randomUUID().toString(), C100APPLICANTBARRISTER3.getCaseRoleLabel()),
                                getRoleAssignmentResponse(UUID.randomUUID().toString(), C100APPLICANTBARRISTER1.getCaseRoleLabel()),
                                getRoleAssignmentResponse(UUID.randomUUID().toString(), FL401RESPONDENTBARRISTER.getCaseRoleLabel())
                            ))
                            .build());
        List<String> errors = new ArrayList<>();
        caseAssignmentService.validateCaseRoles(fl401CaseData,
                                                FL401RESPONDENTBARRISTER.getCaseRoleLabel(),
                                                errors);
        assertThat(errors)
            .contains("A barrister is already associated with the case");
    }

    @Test
    void testBarristerNotIsAssociatedWithTheCase() {
        when(roleAssignmentService.getRoleAssignmentForCase(anyString()))
            .thenReturn(RoleAssignmentServiceResponse.builder()
                            .roleAssignmentResponse(List.of(
                                getRoleAssignmentResponse(
                                    UUID.randomUUID().toString(),
                                    C100APPLICANTBARRISTER3.getCaseRoleLabel()
                                ),
                                getRoleAssignmentResponse(
                                    UUID.randomUUID().toString(),
                                    C100APPLICANTBARRISTER1.getCaseRoleLabel()
                                ),
                                getRoleAssignmentResponse(
                                    UUID.randomUUID().toString(),
                                    FL401RESPONDENTBARRISTER.getCaseRoleLabel()
                                )
                            ))
                            .build());
        List<String> errors = new ArrayList<>();
        caseAssignmentService.validateCaseRoles(
            fl401CaseData,
            C100APPLICANTBARRISTER5.getCaseRoleLabel(),
            errors
        );
        assertThat(errors)
            .isEmpty();
    }

    private RoleAssignmentResponse getRoleAssignmentResponse(String actorId, String roleName) {
        RoleAssignmentResponse roleAssignmentResponse = new RoleAssignmentResponse();
        roleAssignmentResponse.setRoleName(roleName);
        roleAssignmentResponse.setActorId(actorId);
        return roleAssignmentResponse;
    }
}

