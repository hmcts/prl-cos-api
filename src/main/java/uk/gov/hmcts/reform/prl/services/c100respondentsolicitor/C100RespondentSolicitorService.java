package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CallbackRequest;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.RespondentSolicitorEvents;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.Response;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.common.CitizenDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
import uk.gov.hmcts.reform.prl.models.complextypes.solicitorresponse.AttendToCourt;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static uk.gov.hmcts.reform.prl.utils.ElementUtils.element;

@Slf4j
@Service
@RequiredArgsConstructor
public class C100RespondentSolicitorService {
    public static final String CHOOSE_RESPONDENT_DYNAMIC_LIST = "chooseRespondentDynamicList";
    public static final String RESPONDENTS = "respondents";
    public static final String NO_ACTIVE_RESPONDENT_ERR_MSG
        = "You must select an active respondent from the list to start representing through 'Select Respondent' event";
    private final CcdDataStoreService ccdDataStoreService;

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> populateAboutToStartCaseData(CallbackRequest callbackRequest, String authorisation, List<String> errorList) {
        log.info("Inside prePopulateAboutToStartCaseData");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            caseDataUpdated,
            CaseData.class
        );
        findActiveRespondent(caseData, authorisation).ifPresentOrElse(x -> {
            retrieveExistingResponseForSolicitor(callbackRequest, caseDataUpdated, x);
        }, () -> errorList.add("You must select a respondent to represent through 'Select Respondent' event"));
        return caseDataUpdated;
    }

    private void retrieveExistingResponseForSolicitor(CallbackRequest callbackRequest, Map<String, Object> caseDataUpdated, Element<PartyDetails> x) {
        log.info("finding respondentParty is present ");
        RespondentSolicitorEvents.getCaseFieldName(callbackRequest.getEventId()).ifPresent(event -> {
            switch (event) {
                case CONSENT:
                    caseDataUpdated.put(
                        event.getCaseFieldName(),
                        x.getValue().getResponse().getConsent()
                    );
                    log.info("finding respondentConsentToApplication = " + x.getValue().getResponse().getConsent());
                    break;
                case KEEP_DETAILS_PRIVATE:
                    caseDataUpdated.put(
                        event.getCaseFieldName(),
                        x.getValue().getResponse().getKeepDetailsPrivate()
                    );
                    log.info("finding respondentKeepDetailsPrivate = " + x.getValue().getResponse().getKeepDetailsPrivate());
                    break;
                case CONFIRM_EDIT_CONTACT_DETAILS:
                    caseDataUpdated.put(
                        event.getCaseFieldName(),
                        x.getValue().getResponse().getCitizenDetails()
                    );
                    log.info("finding respondentConfirmYourDetails = " + x.getValue().getResponse().getCitizenDetails());
                    break;
                case ATTENDING_THE_COURT:
                    caseDataUpdated.put(
                        event.getCaseFieldName(),
                        x.getValue().getResponse().getAttendToCourt()
                    );
                    log.info("finding respondentAttendingToCourt = " + x.getValue().getResponse().getAttendToCourt());
                    break;
                default:
                    break;
            }
        });
    }

    public Map<String, Object> populateAboutToSubmitCaseData(CallbackRequest callbackRequest, String authorisation, List<String> errorList) {
        log.info("Inside populateAboutToSubmitCaseData");
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            updatedCaseData,
            CaseData.class
        );

        log.info("populateAboutToSubmitCaseData:: caseData" + caseData);
        List<Element<PartyDetails>> respondents = caseData.getRespondents();

        findActiveRespondent(caseData, authorisation).ifPresentOrElse(x -> {
            respondents.stream()
                .filter(party -> Objects.equals(party.getId(), x.getId()))
                .findFirst()
                .ifPresent(party -> {
                    log.info("finding respondentParty is present ");
                    RespondentSolicitorEvents.getCaseFieldName(callbackRequest.getEventId()).ifPresent(event -> {
                        buildResponseForRespondent(caseData, respondents, party, event);
                    });
                });
        }, () -> errorList.add(NO_ACTIVE_RESPONDENT_ERR_MSG));
        updatedCaseData.put(RESPONDENTS, respondents);
        return updatedCaseData;
    }

    private void buildResponseForRespondent(CaseData caseData,
                                            List<Element<PartyDetails>> respondents,
                                            Element<PartyDetails> party,
                                            RespondentSolicitorEvents event) {
        Response buildResponseForRespondent = party.getValue().getResponse();
        switch (event) {
            case CONSENT:
                Consent respondentConsentToApplication = caseData.getRespondentConsentToApplication();
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .consent(buildResponseForRespondent.getConsent().toBuilder()
                                 .consentToTheApplication(respondentConsentToApplication.getConsentToTheApplication())
                                 .noConsentReason(respondentConsentToApplication.getNoConsentReason())
                                 .applicationReceivedDate(respondentConsentToApplication.getApplicationReceivedDate())
                                 .courtOrderDetails(respondentConsentToApplication.getCourtOrderDetails())
                                 .permissionFromCourt(respondentConsentToApplication.getPermissionFromCourt())
                                 .build()).build();
                break;
            case KEEP_DETAILS_PRIVATE:
                KeepDetailsPrivate respondentKeepDetailsPrivate = caseData.getKeepContactDetailsPrivate();
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .keepDetailsPrivate(buildResponseForRespondent.getKeepDetailsPrivate().toBuilder()
                                            .confidentiality(respondentKeepDetailsPrivate.getConfidentiality())
                                            .confidentialityList(respondentKeepDetailsPrivate.getConfidentialityList())
                                            .otherPeopleKnowYourContactDetails(
                                                respondentKeepDetailsPrivate.getOtherPeopleKnowYourContactDetails())
                                            .build())
                    .build();
                break;
            case CONFIRM_EDIT_CONTACT_DETAILS:
                CitizenDetails citizenDetails = caseData.getResSolConfirmEditContactDetails();
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .citizenDetails(buildResponseForRespondent.getCitizenDetails().toBuilder()
                                        .firstName(citizenDetails.getFirstName())
                                        .lastName(citizenDetails.getLastName())
                                        .dateOfBirth(citizenDetails.getDateOfBirth())
                                        .previousName(citizenDetails.getPreviousName())
                                        .placeOfBirth(citizenDetails.getPlaceOfBirth())
                                        .address(citizenDetails.getAddress())
                                        .addressHistory(citizenDetails.getAddressHistory())
                                        .contact(citizenDetails.getContact())
                                        .build())
                    .build();
                break;
            case ATTENDING_THE_COURT:
                buildResponseForRespondent = buildResponseForRespondent.toBuilder()
                    .attendToCourt(caseData.getRespondentAttendingTheCourt())
                    .build();
                break;
            default:
                break;
        }
        PartyDetails amended = party.getValue().toBuilder()
            .response(buildResponseForRespondent).build();
        respondents.set(respondents.indexOf(party), element(party.getId(), amended));
        log.info("updateRespondents:: party found. before update " + party);
    }

    private Optional<Element<PartyDetails>> findActiveRespondent(CaseData caseData, String authorisation) {
        Optional<Element<PartyDetails>> activeRespondent = null;
        List<Element<PartyDetails>> solicitorRepresentedRespondents
            = findSolicitorRepresentedRespondents(caseData, authorisation);

        if (solicitorRepresentedRespondents != null && !solicitorRepresentedRespondents.isEmpty()) {
            activeRespondent = solicitorRepresentedRespondents
                .stream()
                .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
                .findFirst();
            log.info("finding activeRespondent " + activeRespondent);
        }
        return activeRespondent;
    }

    private List<Element<PartyDetails>> findSolicitorRepresentedRespondents(CaseData caseData, String authorisation) {
        List<Element<PartyDetails>> solicitorRepresentedRespondents = new ArrayList<>();
        FindUserCaseRolesResponse findUserCaseRolesResponse = findUserCaseRoles(caseData, authorisation);

        if (findUserCaseRolesResponse != null) {
            log.info("findUserCaseRolesResponse is not null ");
            solicitorRepresentedRespondents = getSolicitorRepresentedRespondents(
                caseData,
                findUserCaseRolesResponse
            );
        }
        return solicitorRepresentedRespondents;
    }

    private List<Element<PartyDetails>> getSolicitorRepresentedRespondents(CaseData caseData, FindUserCaseRolesResponse findUserCaseRolesResponse) {
        List<Element<PartyDetails>> solicitorRepresentedParties = new ArrayList<>();
        for (CaseUser caseUser : findUserCaseRolesResponse.getCaseUsers()) {
            log.info("caseUser is = " + caseUser);
            SolicitorRole.from(caseUser.getCaseRole()).ifPresent(
                x -> solicitorRepresentedParties.add(caseData.getRespondents().get(x.getIndex())));
        }
        log.info("finding solicitorRepresentedParties Party " + solicitorRepresentedParties);
        return solicitorRepresentedParties;
    }

    private FindUserCaseRolesResponse findUserCaseRoles(CaseData caseData, String authorisation) {
        FindUserCaseRolesResponse findUserCaseRolesResponse = ccdDataStoreService.findUserCaseRoles(
            String.valueOf(caseData.getId()),
            authorisation
        );
        log.info("findUserCaseRolesResponse = " + findUserCaseRolesResponse);
        return findUserCaseRolesResponse;
    }

    public Map<String, Object> populateSolicitorRespondentList(CallbackRequest callbackRequest, String authorisation) {
        Map<String, Object> headerMap = new HashMap<>();
        CaseData caseData = objectMapper.convertValue(
            callbackRequest.getCaseDetails().getData(),
            CaseData.class
        );
        log.info("populateSolicitorRespondentList service: caseData is:: " + caseData);
        headerMap.put(CHOOSE_RESPONDENT_DYNAMIC_LIST, ElementUtils.asDynamicList(
            findSolicitorRepresentedRespondents(caseData, authorisation),
            null,
            PartyDetails::getLabelForDynamicList
        ));
        return headerMap;
    }

    public Map<String, Object> updateActiveRespondentSelectionBySolicitor(CallbackRequest callbackRequest, String authorisation) {
        Map<String, Object> updatedCaseData = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            updatedCaseData,
            CaseData.class
        );
        log.info("updateRespondents:: caseData" + caseData);

        UUID selectedRespondentId = caseData.getChooseRespondentDynamicList().getValueCodeAsUuid();
        log.info("updateRespondents:: selectedRespondentId" + selectedRespondentId);
        List<Element<PartyDetails>> respondents = caseData.getRespondents();

        respondents.stream()
            .filter(party -> Objects.equals(party.getId(), selectedRespondentId))
            .findFirst()
            .ifPresent(party -> {
                log.info("updateRespondents:: party found. before update " + party);
                PartyDetails amended = party.getValue().toBuilder()
                    .response(party.getValue().getResponse().toBuilder().activeRespondent(YesOrNo.Yes).build())
                    .build();

                respondents.set(respondents.indexOf(party), element(party.getId(), amended));
                log.info("updateRespondents:: party found. after update " + party);
            });

        findSolicitorRepresentedRespondents(caseData, authorisation)
            .forEach(solicitorRepresentedParty -> {
                respondents.stream()
                    .filter(party -> Objects.equals(party.getId(), solicitorRepresentedParty.getId())
                        && !Objects.equals(party.getId(), selectedRespondentId))
                    .forEach(party -> {
                        log.info("updateRespondents:: party found which needs to be set to false. before update " + party);
                        PartyDetails amended = party.getValue().toBuilder()
                            .response(party.getValue().getResponse().toBuilder().activeRespondent(YesOrNo.No).build())
                            .build();

                        respondents.set(respondents.indexOf(party), element(party.getId(), amended));
                        log.info("updateRespondents:: party found which needs to be set to false. after update " + party);
                    });
            });
        updatedCaseData.put(RESPONDENTS, respondents);
        return updatedCaseData;
    }
}
