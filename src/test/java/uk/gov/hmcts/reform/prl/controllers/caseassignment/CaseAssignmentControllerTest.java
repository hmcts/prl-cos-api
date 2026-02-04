package uk.gov.hmcts.reform.prl.controllers.caseassignment;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.clients.ccd.CaseAssignmentService;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.BarristerRole;
import uk.gov.hmcts.reform.prl.exception.GrantCaseAccessException;
import uk.gov.hmcts.reform.prl.exception.InvalidClientException;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.barrister.AllocatedBarrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.Barrister;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.localauthority.LocalAuthoritySocialWorker;
import uk.gov.hmcts.reform.prl.services.ApplicationsTabService;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.OrganisationService;
import uk.gov.hmcts.reform.prl.services.caseflags.PartyLevelCaseFlagsService;
import uk.gov.hmcts.reform.prl.utils.BarristerHelper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Map.entry;
import static java.util.Map.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.ALLOCATED_BARRISTER;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.CASE_TYPE_OF_APPLICATION;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.INVALID_CLIENT;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.LOCAL_AUTHORITY_SOCIAL_WORKER;

@ExtendWith(MockitoExtension.class)
class CaseAssignmentControllerTest {
    @Mock
    private CaseAssignmentService caseAssignmentService;
    @Mock
    private OrganisationService organisationService;
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private PartyLevelCaseFlagsService partyLevelCaseFlagsService;
    @Mock
    private BarristerHelper barristerHelper;
    @Mock
    private ApplicationsTabService applicationsTabService;
    @Spy
    private ObjectMapper objectMapper;

    private CaseAssignmentController caseAssignmentController;

    private AllocatedBarrister allocatedBarrister;
    private LocalAuthoritySocialWorker localAuthoritySocialWorker;

    @BeforeEach
    void setUp() {
        objectMapper.findAndRegisterModules();
        caseAssignmentController = new CaseAssignmentController(
            caseAssignmentService,
            objectMapper,
            organisationService,
            authorisationService,
            barristerHelper,
            partyLevelCaseFlagsService,
            applicationsTabService
        );

        Barrister barrister = Barrister.builder()
            .barristerEmail("barristerEmail@gmail.com")
            .barristerFirstName("barristerName")
            .barristerLastName("barristerLastName")
            .barristerOrg(Organisation.builder()
                              .organisationID("barristerOrgId")
                              .organisationName("barristerOrgName")
                              .build())
            .build();

        allocatedBarrister = AllocatedBarrister.builder()
            .partyList(DynamicList.builder()
                           .value(DynamicListElement.builder()
                                      .code(UUID.randomUUID())
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

        localAuthoritySocialWorker = LocalAuthoritySocialWorker.builder()
            .laSocialWorkerOrg(Organisation.builder()
                              .organisationID(barrister.getBarristerOrg()
                                                  .getOrganisationID())
                              .organisationName(barrister.getBarristerOrg()
                                                    .getOrganisationName())
                              .build())
            .laSocialWorkerEmail("socialWorker@email.com")
            .laSocialWorkerFirstName("socialWorkerFirstName")
            .laSocialWorkerLastName("socialWorkerLastName")
            .build();

    }

    @Test
    void testSuccessSubmitAddBarrister() {

        Optional<String> userId = Optional.of("userId");
        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(true);
        when(organisationService.findUserByEmail(allocatedBarrister.getBarristerEmail()))
            .thenReturn(userId);
        Optional<String> barristerRole = Optional.of(BarristerRole.C100APPLICANTBARRISTER1.getCaseRoleLabel());

        when(caseAssignmentService.deriveBarristerRole(anyMap(), isA(CaseData.class), isA(AllocatedBarrister.class)))
            .thenReturn(barristerRole);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(ALLOCATED_BARRISTER, allocatedBarrister);
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, C100_CASE_TYPE);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234L)
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .data(caseDataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        CaseData caseData = CaseData.builder().caseTypeOfApplication(C100_CASE_TYPE).allocatedBarrister(
            allocatedBarrister).build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = caseAssignmentController.submitAddBarrister(
            "auth",
            "s2sToken",
            callbackRequest
        );

        assertThat(response.getData().get(ALLOCATED_BARRISTER))
            .isNotNull();

        assertThat(response.getErrors()).isEmpty();

        verify(caseAssignmentService).validateAddRequest(
            eq(userId),
            isA(CaseData.class),
            eq(barristerRole),
            isA(AllocatedBarrister.class),
            anyList()
        );
        verify(caseAssignmentService).addBarrister(
            isA(CaseData.class),
            eq(userId.get()),
            eq(barristerRole.get()),
            isA(AllocatedBarrister.class)
        );

        verify(applicationsTabService).updateTab(isA(CaseData.class));
        verify(partyLevelCaseFlagsService).generatePartyCaseFlagsForBarristerOnly(any());
    }

    @Test
    void testGrantCaseAccessExceptionOnSubmitAddBarrister() {

        Optional<String> userId = Optional.of("userId");
        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(true);
        when(organisationService.findUserByEmail(allocatedBarrister.getBarristerEmail()))
            .thenReturn(userId);
        Optional<String> barristerRole = Optional.of(BarristerRole.C100APPLICANTBARRISTER1.getCaseRoleLabel());

        when(caseAssignmentService.deriveBarristerRole(anyMap(), isA(CaseData.class), isA(AllocatedBarrister.class)))
            .thenReturn(barristerRole);

        doThrow(new GrantCaseAccessException("User(s) not granted [C100APPLICANTBARRISTER3] to the case "))
            .when(caseAssignmentService).addBarrister(
                isA(CaseData.class),
                eq(userId.get()),
                eq(barristerRole.get()),
                isA(AllocatedBarrister.class)
            );


        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(ALLOCATED_BARRISTER, allocatedBarrister);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234L)
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .data(caseDataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        CaseData caseData = CaseData.builder().allocatedBarrister(allocatedBarrister).build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = caseAssignmentController.submitAddBarrister(
            "auth",
            "s2sToken",
            callbackRequest
        );

        assertThat(response.getErrors())
            .contains("User(s) not granted [C100APPLICANTBARRISTER3] to the case ");

        verify(caseAssignmentService).validateAddRequest(
            eq(userId),
            isA(CaseData.class),
            eq(barristerRole),
            isA(AllocatedBarrister.class),
            anyList()
        );
        verify(caseAssignmentService).addBarrister(
            isA(CaseData.class),
            eq(userId.get()),
            eq(barristerRole.get()),
            isA(AllocatedBarrister.class)
        );
    }


    @Test
    void testErrorsSubmitAddBarrister() {
        Optional<String> userId = Optional.of("userId");
        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(true);
        when(organisationService.findUserByEmail(allocatedBarrister.getBarristerEmail()))
            .thenReturn(userId);
        Optional<String> barristerRole = Optional.of(BarristerRole.C100APPLICANTBARRISTER1.getCaseRoleLabel());

        when(caseAssignmentService.deriveBarristerRole(anyMap(), isA(CaseData.class), isA(AllocatedBarrister.class)))
            .thenReturn(barristerRole);

        doAnswer(invocation -> {
            List<String> errors = invocation.getArgument(4);
            errors.add("errors");
            return null;
        }).when(caseAssignmentService).validateAddRequest(
            eq(userId),
            isA(CaseData.class),
            eq(barristerRole),
            isA(AllocatedBarrister.class),
            anyList()
        );

        Map<String, Object> caseDataMap = of(ALLOCATED_BARRISTER, allocatedBarrister);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234L)
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .data(caseDataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        CaseData caseData = CaseData.builder().allocatedBarrister(allocatedBarrister).build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = caseAssignmentController.submitAddBarrister(
            "auth",
            "s2sToken",
            callbackRequest
        );

        assertThat(response.getErrors()).contains("errors");

        verify(caseAssignmentService).validateAddRequest(
            eq(userId),
            isA(CaseData.class),
            eq(barristerRole),
            isA(AllocatedBarrister.class),
            anyList()
        );
        verify(caseAssignmentService, never()).addBarrister(
            isA(CaseData.class),
            eq(userId.get()),
            eq(barristerRole.get()),
            isA(AllocatedBarrister.class)
        );
    }

    static Stream<Arguments> parameterParameters() {
        return Stream.of(
            Arguments.of(
                (Supplier<Optional<String>>) () -> Optional.of("userId"),
                (Supplier<Optional<String>>) Optional::empty
            ),
            Arguments.of(
                (Supplier<Optional<String>>) Optional::empty,
                (Supplier<Optional<String>>) () -> Optional.of("barristerRole")
            )
        );
    }

    @ParameterizedTest
    @MethodSource("parameterParameters")
    void testUserIdOrBarriesterRoleEmptyForSubmitAddBarrister(Supplier<Optional<String>> userId, Supplier<Optional<String>> barristerRole) {
        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(true);
        when(organisationService.findUserByEmail(allocatedBarrister.getBarristerEmail()))
            .thenReturn(userId.get());

        when(caseAssignmentService.deriveBarristerRole(anyMap(), isA(CaseData.class), isA(AllocatedBarrister.class)))
            .thenReturn(barristerRole.get());

        Map<String, Object> caseDataMap = of(ALLOCATED_BARRISTER, allocatedBarrister);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234L)
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .data(caseDataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        CaseData caseData = CaseData.builder().allocatedBarrister(allocatedBarrister).build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = caseAssignmentController.submitAddBarrister(
            "auth",
            "s2sToken",
            callbackRequest
        );

        assertThat(response.getErrors()).isEmpty();

        verify(caseAssignmentService).validateAddRequest(
            eq(userId.get()),
            isA(CaseData.class),
            eq(barristerRole.get()),
            isA(AllocatedBarrister.class),
            anyList()
        );
        verify(caseAssignmentService, never()).addBarrister(
            isA(CaseData.class),
            any(),
            any(),
            isA(AllocatedBarrister.class)
        );
    }


    @Test
    void testInvalidClientExceptionForSubmitAddBarrister() {

        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(false);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .build();

        assertThatThrownBy(() -> caseAssignmentController.submitAddBarrister(
            "auth",
            "s2sToken",
            callbackRequest
        )).isInstanceOf(InvalidClientException.class)
            .hasMessageContaining(INVALID_CLIENT);
    }

    @Test
    void testSuccessSubmitRemoveBarrister() {
        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(true);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(ALLOCATED_BARRISTER, allocatedBarrister);
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, FL401_CASE_TYPE);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234L)
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .data(caseDataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        PartyDetails partyDetails = PartyDetails.builder()
            .barrister(Barrister.builder()
                           .barristerEmail("testbarrister@test.com")
                           .barristerFirstName("test")
                           .barristerLastName("barrister").build())
            .build();

        when(caseAssignmentService.getSelectedParty(
            isA(CaseData.class),
            eq(allocatedBarrister.getPartyList().getValueCode())
        ))
            .thenReturn(partyDetails);

        AboutToStartOrSubmitCallbackResponse response = caseAssignmentController.submitRemoveBarrister(
            "auth",
            "s2sToken",
            callbackRequest
        );

        assertThat(response.getData())
            .contains(entry(ALLOCATED_BARRISTER, allocatedBarrister));


        assertThat(response.getErrors()).isEmpty();

        String selectedPartyId = allocatedBarrister.getPartyList().getValueCode();
        verify(caseAssignmentService).validateRemoveRequest(
            isA(CaseData.class),
            eq(selectedPartyId),
            anyList()
        );
        verify(caseAssignmentService).removeBarrister(
            isA(CaseData.class),
            eq(partyDetails)
        );
        verify(barristerHelper).setAllocatedBarrister(
            eq(partyDetails),
            isA(CaseData.class),
            eq(UUID.fromString(selectedPartyId))
        );
        verify(applicationsTabService).updateTab(isA(CaseData.class));
        verify(partyLevelCaseFlagsService).generatePartyCaseFlagsForBarristerOnly(any());

    }

    @Test
    void testErrorsSubmitRemoveBarrister() {
        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(true);

        Map<String, Object> caseDataMap = of(ALLOCATED_BARRISTER, allocatedBarrister);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234L)
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .data(caseDataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        CaseData caseData = CaseData.builder().allocatedBarrister(allocatedBarrister).build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        String selectedPartyId = allocatedBarrister.getPartyList().getValueCode();
        doAnswer(invocation -> {
            List<String> errors = invocation.getArgument(2);
            errors.add("errors");
            return null;
        }).when(caseAssignmentService).validateRemoveRequest(
            isA(CaseData.class),
            eq(selectedPartyId),
            anyList()
        );

        AboutToStartOrSubmitCallbackResponse response = caseAssignmentController.submitRemoveBarrister(
            "auth",
            "s2sToken",
            callbackRequest
        );

        assertThat(response.getErrors()).contains("errors");

        verify(caseAssignmentService).validateRemoveRequest(
            isA(CaseData.class),
            eq(selectedPartyId),
            anyList()
        );
        verify(caseAssignmentService, never()).removeBarrister(
            isA(CaseData.class),
            any(PartyDetails.class)
        );
        verify(barristerHelper, never()).setAllocatedBarrister(
            any(PartyDetails.class),
            any(CaseData.class),
            any(UUID.class)
        );
    }

    @Test
    void testInvalidClientExceptionForSubmitRemoveBarrister() {

        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(false);
        CallbackRequest callbackRequest = CallbackRequest.builder()
            .build();

        assertThatThrownBy(() -> caseAssignmentController.submitRemoveBarrister(
            "auth",
            "s2sToken",
            callbackRequest
        )).isInstanceOf(InvalidClientException.class)
            .hasMessageContaining(INVALID_CLIENT);
    }

    @Test
    void testSuccessSubmitAddSocialWorker() {

        Optional<String> userId = Optional.of("userId");
        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(true);
        when(organisationService.findUserByEmail(localAuthoritySocialWorker.getLaSocialWorkerEmail()))
            .thenReturn(userId);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(PrlAppsConstants.LOCAL_AUTHORITY_SOCIAL_WORKER, localAuthoritySocialWorker);
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, C100_CASE_TYPE);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234L)
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .data(caseDataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        CaseData caseData = CaseData.builder().caseTypeOfApplication(C100_CASE_TYPE).localAuthoritySocialWorker(
            localAuthoritySocialWorker).build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = caseAssignmentController.submitAddSocialWorker(
            "auth",
            "s2sToken",
            callbackRequest
        );

        Optional<String> socialWorkerRole = Optional.of("[LASOCIALWORKER]");
        assertThat(response.getData().get(PrlAppsConstants.LOCAL_AUTHORITY_SOCIAL_WORKER))
            .isNotNull();

        assertThat(response.getErrors()).isEmpty();

        verify(caseAssignmentService).validateSocialWorkerAddRequest(
            isA(CaseData.class),
            eq(socialWorkerRole),
            isA(LocalAuthoritySocialWorker.class),
            anyList()
        );
        verify(caseAssignmentService).addSocialWorker(
            isA(CaseData.class),
            eq(userId.get()),
            eq(socialWorkerRole.get()),
            isA(LocalAuthoritySocialWorker.class)
        );
    }

    //@Test
    void testGrantCaseAccessExceptionOnSubmitAddSocialWorker() {

        Optional<String> userId = Optional.of("userId");
        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(true);
        when(organisationService.findUserByEmail(localAuthoritySocialWorker.getLaSocialWorkerEmail()))
            .thenReturn(userId);
        Optional<String> socialWorkerRole = Optional.of(PrlAppsConstants.LOCAL_AUTHORITY_SOCIAL_WORKER);



        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(PrlAppsConstants.LOCAL_AUTHORITY_SOCIAL_WORKER, localAuthoritySocialWorker);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234L)
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .data(caseDataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();
        LocalAuthoritySocialWorker localAuthoritySocialWorkerUpdated
            = localAuthoritySocialWorker.toBuilder().userId(userId.get()).build();
        CaseData caseData = CaseData.builder()
            .localAuthoritySocialWorker(localAuthoritySocialWorkerUpdated)
            .build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        doThrow(new GrantCaseAccessException("User(s) not granted [LASOCIALWORKER] to the case "))
            .when(caseAssignmentService).addSocialWorker(
                any(CaseData.class),
                eq(userId.get()),
                eq(socialWorkerRole.get()),
                any(LocalAuthoritySocialWorker.class)
            );

        AboutToStartOrSubmitCallbackResponse response = caseAssignmentController.submitAddSocialWorker(
            "auth",
            "s2sToken",
            callbackRequest
        );

        assertThat(response.getErrors())
            .contains("User(s) not granted [LASOCIALWORKER] to the case ");

        verify(caseAssignmentService).validateSocialWorkerAddRequest(
            isA(CaseData.class),
            eq(socialWorkerRole),
            isA(LocalAuthoritySocialWorker.class),
            anyList()
        );
        verify(caseAssignmentService).addSocialWorker(
            isA(CaseData.class),
            eq(userId.get()),
            eq(socialWorkerRole.get()),
            isA(LocalAuthoritySocialWorker.class)
        );
    }

    //@Test
    void testErrorsSubmitAddSocialWorker() {
        Optional<String> userId = Optional.of("userId");
        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(true);
        when(organisationService.findUserByEmail(localAuthoritySocialWorker.getLaSocialWorkerEmail()))
            .thenReturn(userId);
        Optional<String> socialWorkerRole = Optional.of(LOCAL_AUTHORITY_SOCIAL_WORKER);

        doAnswer(invocation -> {
            List<String> errors = invocation.getArgument(4);
            errors.add("errors");
            return null;
        }).when(caseAssignmentService).validateSocialWorkerAddRequest(
            isA(CaseData.class),
            eq(socialWorkerRole),
            isA(LocalAuthoritySocialWorker.class),
            anyList()
        );

        Map<String, Object> caseDataMap = of(PrlAppsConstants.LOCAL_AUTHORITY_SOCIAL_WORKER, localAuthoritySocialWorker);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234L)
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .data(caseDataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();

        CaseData caseData = CaseData.builder().localAuthoritySocialWorker(localAuthoritySocialWorker).build();
        when(objectMapper.convertValue(caseDataMap, CaseData.class)).thenReturn(caseData);

        AboutToStartOrSubmitCallbackResponse response = caseAssignmentController.submitAddSocialWorker(
            "auth",
            "s2sToken",
            callbackRequest
        );

        assertThat(response.getErrors()).contains("errors");

        verify(caseAssignmentService).validateSocialWorkerAddRequest(
            isA(CaseData.class),
            eq(socialWorkerRole),
            isA(LocalAuthoritySocialWorker.class),
            anyList()
        );
        verify(caseAssignmentService, never()).addSocialWorker(
            isA(CaseData.class),
            eq(userId.get()),
            eq(socialWorkerRole.get()),
            isA(LocalAuthoritySocialWorker.class)
        );
    }

    @Test
    void testSuccessSubmitRemoveSocialWorker() {
        when(authorisationService.isAuthorized(any(), any()))
            .thenReturn(true);

        Map<String, Object> caseDataMap = new HashMap<>();
        caseDataMap.put(LOCAL_AUTHORITY_SOCIAL_WORKER, localAuthoritySocialWorker);
        caseDataMap.put(CASE_TYPE_OF_APPLICATION, FL401_CASE_TYPE);

        CaseDetails caseDetails = CaseDetails.builder()
            .id(1234L)
            .createdDate(LocalDateTime.now())
            .lastModified(LocalDateTime.now())
            .data(caseDataMap)
            .build();

        CallbackRequest callbackRequest = CallbackRequest.builder()
            .caseDetails(caseDetails)
            .build();


        AboutToStartOrSubmitCallbackResponse response = caseAssignmentController.submitRemoveSocialWorker(
            "auth",
            "s2sToken",
            callbackRequest
        );

        assertThat(response.getData())
            .contains(entry(LOCAL_AUTHORITY_SOCIAL_WORKER, localAuthoritySocialWorker));


        assertThat(response.getErrors()).isEmpty();

        verify(laCaseAssignmentService).validateSocialWorkerRemoveRequest(
            isA(CaseData.class),
            isA(LocalAuthoritySocialWorker.class),
            anyList()
        );
        verify(laCaseAssignmentService).removeLaSocialWorker(
            isA(CaseData.class),
            isA(LocalAuthoritySocialWorker.class)
        );

    }

}

