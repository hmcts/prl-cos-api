package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.OrderStatusEnum;
import uk.gov.hmcts.reform.prl.models.DraftOrder;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.OrgSolicitors;
import uk.gov.hmcts.reform.prl.models.Organisations;
import uk.gov.hmcts.reform.prl.models.OtherDraftOrderDetails;
import uk.gov.hmcts.reform.prl.models.SolicitorUser;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ViewDraftOrdersServiceTest {

    @InjectMocks
    private ViewDraftOrdersService viewDraftOrdersService;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private UserService userService;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private UserDetails currentUserDetails;

    @Mock
    private Organisations currentUserOrganisations;

    private static final String AUTH_TOKEN_WITH_ORGANISATION = "Bearer TestAuthToken with org";
    private static final String AUTH_TOKEN_NO_ORGANISATION = "Bearer TestAuthToken no org";

    private static final String CURRENT_USER_EMAIL = "currentUser@example.com";

    private static final List<String> ORGANISATION_SOLICITOR_EMAILS = List.of(
        "sameOrgSolicitorEmail_1_@example.com",
        "sameOrgSolicitorEmail_2_@example.com",
        "sameOrgSolicitorEmail_3_@example.com",
        "sameOrgSolicitorEmail_4_@example.com",
        "sameOrgSolicitorEmail_5_@example.com"
    );

    private static final List<String> OTHER_ORGANISATION_SOLICITOR_EMAILS = List.of(
        "nonOrgSolicitorEmail_1_@example.com",
        "nonOrgSolicitorEmail_2_@example.com",
        "nonOrgSolicitorEmail_3_@example.com",
        "nonOrgSolicitorEmail_4_@example.com",
        "nonOrgSolicitorEmail_5_@example.com"
    );

    @Test
    public void testGetDraftOrdersForUserCurrentUserNullDraftOrders() {
        // Given
        Map<String, Object> caseDetailsData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1L)
            .data(caseDetailsData).build();

        CaseData caseData = CaseData.builder()
            .draftOrderCollection(null)
            .build();
        when(objectMapper.convertValue(caseDetailsData, CaseData.class)).thenReturn(caseData);

        // When
        List<Element<DraftOrder>> userDraftOrderCollection =
            viewDraftOrdersService.getDraftOrdersForUser(caseDetails, AUTH_TOKEN_NO_ORGANISATION);

        // Then
        assertTrue(userDraftOrderCollection.isEmpty());
    }

    @Test
    public void testGetDraftOrdersForUserCurrentUserNoOrg() {
        // Given
        when(userService.getUserDetails(AUTH_TOKEN_NO_ORGANISATION)).thenReturn(currentUserDetails);
        when(currentUserDetails.getEmail()).thenReturn(CURRENT_USER_EMAIL);

        when(organisationService.findUserOrganisation(AUTH_TOKEN_NO_ORGANISATION))
            .thenReturn(Optional.empty());

        Map<String, Object> caseDetailsData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1L)
            .data(caseDetailsData).build();

        List<Element<DraftOrder>> draftOrderCollection = List.of(Element.<DraftOrder>builder().value(DraftOrder.builder().otherDetails(
            OtherDraftOrderDetails.builder()
                .status(OrderStatusEnum.draftedByLR.getDisplayedValue())
                .orderCreatedByEmailId(CURRENT_USER_EMAIL)
                .build()).build()).build());

        CaseData caseData = CaseData.builder()
            .draftOrderCollection(draftOrderCollection)
            .build();
        when(objectMapper.convertValue(caseDetailsData, CaseData.class)).thenReturn(caseData);

        // When
        List<Element<DraftOrder>> userDraftOrderCollection =
            viewDraftOrdersService.getDraftOrdersForUser(caseDetails, AUTH_TOKEN_NO_ORGANISATION);

        // Then
        assertFalse(userDraftOrderCollection.isEmpty());
    }

    static Stream<Arguments> parameterOrgSolicitorEmails() {
        return Stream.of(
            Arguments.of(
                Collections.singletonList(ORGANISATION_SOLICITOR_EMAILS.get(0)),
                Collections.singletonList(OTHER_ORGANISATION_SOLICITOR_EMAILS.get(0))
            ),
            Arguments.of(
                Arrays.asList(
                    ORGANISATION_SOLICITOR_EMAILS.get(0),
                    ORGANISATION_SOLICITOR_EMAILS.get(1),
                    ORGANISATION_SOLICITOR_EMAILS.get(2)
                ),
                Collections.singletonList(OTHER_ORGANISATION_SOLICITOR_EMAILS.get(0))
            ),
            Arguments.of(
                List.of(),
                Arrays.asList(
                    OTHER_ORGANISATION_SOLICITOR_EMAILS.get(0),
                    OTHER_ORGANISATION_SOLICITOR_EMAILS.get(1),
                    OTHER_ORGANISATION_SOLICITOR_EMAILS.get(2)
                )
            ),
            Arguments.of(
                Collections.singletonList(ORGANISATION_SOLICITOR_EMAILS.get(0)),
                List.of()
            )
        );
    }

    @ParameterizedTest
    @MethodSource("parameterOrgSolicitorEmails")
    @DisplayName("Should return null when document URL is null")
    public void testGetDraftOrdersForUserCurrentUserWithOrg(List<String> sameOrgSolicitorEmails,
                                                            List<String> otherOrgSolicitorEmails) {
        // Given
        //  Setup User Organisation
        when(organisationService.findUserOrganisation(AUTH_TOKEN_WITH_ORGANISATION))
            .thenReturn(Optional.of(currentUserOrganisations));
        String currentUserOrganisationId = "orgID";
        when(currentUserOrganisations.getOrganisationIdentifier()).thenReturn(currentUserOrganisationId);
        String systemAuthorisation = "sysAuthToken";
        when(systemUserService.getSysUserToken()).thenReturn(systemAuthorisation);
        OrgSolicitors orgSolicitors = mock(OrgSolicitors.class);
        when(organisationService.getOrganisationSolicitorDetails(
            systemAuthorisation,
            currentUserOrganisationId
        )).thenReturn(orgSolicitors);
        when(orgSolicitors.getUsers()).thenReturn(getUserOrgSolicitors());

        //  Set up case.
        Map<String, Object> caseDetailsData = new HashMap<>();
        CaseDetails caseDetails = CaseDetails.builder()
            .id(1L)
            .data(caseDetailsData).build();

        List<Element<DraftOrder>> sameOrgSolicitorOrganisationsDrafts = sameOrgSolicitorEmails.stream()
            .map(email -> creatorOrderElement(sameOrgSolicitorEmails.indexOf(email), email))
            .toList();

        List<Element<DraftOrder>> otherOrgSolicitorOrganisationsDrafts = otherOrgSolicitorEmails.stream()
            .map(email -> ViewDraftOrdersServiceTest.creatorOrderElement(otherOrgSolicitorEmails.indexOf(email), email))
            .toList();

        List<Element<DraftOrder>> draftOrderCollection =
            new ArrayList<>(sameOrgSolicitorOrganisationsDrafts.size() + otherOrgSolicitorOrganisationsDrafts.size());
        draftOrderCollection.addAll(sameOrgSolicitorOrganisationsDrafts);
        draftOrderCollection.addAll(otherOrgSolicitorOrganisationsDrafts);

        CaseData caseData = CaseData.builder()
            .draftOrderCollection(draftOrderCollection)
            .build();
        when(objectMapper.convertValue(caseDetailsData, CaseData.class)).thenReturn(caseData);

        // When
        List<Element<DraftOrder>> userDraftOrderCollection =
            viewDraftOrdersService.getDraftOrdersForUser(caseDetails, AUTH_TOKEN_WITH_ORGANISATION);

        // Then
        assertEquals(sameOrgSolicitorOrganisationsDrafts, userDraftOrderCollection);
    }

    private static List<SolicitorUser> getUserOrgSolicitors() {
        return ORGANISATION_SOLICITOR_EMAILS.stream()
            .map(email -> SolicitorUser.builder().email(email).build())
            .toList();
    }

    private static Element<DraftOrder> creatorOrderElement(int iteration, String orderCreatorEmail) {
        return Element.<DraftOrder>builder().value(DraftOrder.builder()
                                                       .dateOrderMade(LocalDate.of(2026, 1, 1)
                                                                          .plusDays(iteration))
                                                       .otherDetails(
                                                           OtherDraftOrderDetails.builder()
                                                               .status(OrderStatusEnum.draftedByLR.getDisplayedValue())
                                                               .orderCreatedByEmailId(orderCreatorEmail)
                                                               .build()).build()).build();
    }

}
