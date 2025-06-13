package uk.gov.hmcts.reform.prl.controllers.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.AboutToStartOrSubmitCallbackResponse;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.State;
import uk.gov.hmcts.reform.prl.enums.YesNoDontKnow;
import uk.gov.hmcts.reform.prl.enums.citizen.ConfidentialityListEnum;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.c100respondentsolicitor.RespondentSolicitorData;
import uk.gov.hmcts.reform.prl.services.AuthorisationService;
import uk.gov.hmcts.reform.prl.services.EventService;
import uk.gov.hmcts.reform.prl.utils.CaseUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.YesOrNo.Yes;

@ExtendWith(MockitoExtension.class)
class C100RespondentSolicitorTaskListControllerTest {

    @InjectMocks
    private C100RespondentSolicitorTaskListController c100RespondentSolicitorTaskListController;

    @Mock
    private EventService eventPublisher;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private AuthorisationService authorisationService;

    private static final String AUTH_TOKEN = "Bearer TestAuthToken";
    private static final String S2S_TOKEN = "s2s AuthToken";

    private CaseData caseData;

    @BeforeEach
    void setUp() {

        List<ConfidentialityListEnum> confidentialityListEnums = new ArrayList<>();

        confidentialityListEnums.add(ConfidentialityListEnum.email);
        confidentialityListEnums.add(ConfidentialityListEnum.phoneNumber);

        PartyDetails applicant = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();

        Element<PartyDetails> wrappedApplicant = Element.<PartyDetails>builder().value(applicant).build();
        List<Element<PartyDetails>> applicantList = Collections.singletonList(wrappedApplicant);

        PartyDetails respondent = PartyDetails.builder().representativeFirstName("Abc")
            .representativeLastName("Xyz")
            .gender(Gender.male)
            .email("abc@xyz.com")
            .phoneNumber("1234567890")
            .canYouProvideEmailAddress(Yes)
            .isEmailAddressConfidential(Yes)
            .isPhoneNumberConfidential(Yes)
            .solicitorOrg(Organisation.builder().organisationID("ABC").organisationName("XYZ").build())
            .solicitorAddress(Address.builder().addressLine1("ABC").postCode("AB1 2MN").build())
            .doTheyHaveLegalRepresentation(YesNoDontKnow.yes)
            .build();


        Element<PartyDetails> wrappedRespondents = Element.<PartyDetails>builder().value(respondent).build();
        List<Element<PartyDetails>> respondentList = Collections.singletonList(wrappedRespondents);

        caseData = CaseData.builder()
            .courtName("test court")
            .welshLanguageRequirement(Yes)
            .welshLanguageRequirementApplication(english)
            .languageRequirementApplicationNeedWelsh(Yes)
            .respondentSolicitorData(RespondentSolicitorData.builder()
                                         .keepContactDetailsPrivate(KeepDetailsPrivate.builder()
                                                                        .confidentiality(Yes)
                                                                        .confidentialityList(confidentialityListEnums)
                                                                        .build())
                                         .build())
            .applicants(applicantList)
            .caseTypeOfApplication(PrlAppsConstants.C100_CASE_TYPE)
            .state(State.AWAITING_SUBMISSION_TO_HMCTS)
            .respondents(respondentList)
            .build();
    }


    @Test
    void testHandleAboutToSubmit() {
        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        when(objectMapper.convertValue(stringObjectMap, CaseData.class)).thenReturn(caseData);

        when(CaseUtils.getCaseData(
            callbackRequest.getCaseDetails(),
            objectMapper
        )).thenReturn(caseData);
        when(authorisationService.isAuthorized(any(), any())).thenReturn(true);
        AboutToStartOrSubmitCallbackResponse response = c100RespondentSolicitorTaskListController.handleSubmitted(
            callbackRequest, AUTH_TOKEN, S2S_TOKEN
        );

        assertTrue(response.getData().containsKey("state"));
    }

    @Test
    void testExceptionForHandleSubmitted() {

        Map<String, Object> stringObjectMap = caseData.toMap(new ObjectMapper());

        CallbackRequest callbackRequest = uk.gov.hmcts.reform.ccd.client.model
            .CallbackRequest.builder()
            .caseDetails(uk.gov.hmcts.reform.ccd.client.model.CaseDetails.builder()
                             .id(123L)
                             .data(stringObjectMap)
                             .build())
            .build();

        Mockito.when(authorisationService.isAuthorized(AUTH_TOKEN, S2S_TOKEN)).thenReturn(false);

        RuntimeException ex = assertThrows(
            RuntimeException.class, () ->
                c100RespondentSolicitorTaskListController.handleSubmitted(callbackRequest, S2S_TOKEN, AUTH_TOKEN)
        );

        assertEquals("Invalid Client", ex.getMessage());
    }
}
