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
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.confidentiality.KeepDetailsPrivate;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.response.consent.Consent;
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
    private final CcdDataStoreService ccdDataStoreService;

    @Autowired
    private ObjectMapper objectMapper;

    public Map<String, Object> prePopulateAboutToStartCaseData(CallbackRequest callbackRequest, String authorisation) {
        log.info("Inside prePopulateAboutToStartCaseData");
        Map<String, Object> caseDataUpdated = callbackRequest.getCaseDetails().getData();
        CaseData caseData = objectMapper.convertValue(
            caseDataUpdated,
            CaseData.class
        );
        findActiveRespondent(caseData, authorisation).ifPresent(x -> {
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
                    default:
                        break;
                }
            });
        });
        return caseDataUpdated;
    }

    public Consent prePopulateRespondentConsentToTheApplicationCaseData(CaseData caseData, String authorisation) {
        log.info("Inside prePopulateRespondentConsentToTheApplicationCaseData");
        Optional<Element<PartyDetails>> respondentParty;
        Consent respondentConsentToApplication = null;
        respondentParty = findActiveRespondent(caseData, authorisation);
        if (respondentParty.isPresent()) {
            log.info("finding respondentParty is present ");
            respondentConsentToApplication = respondentParty.get().getValue().getResponse().getConsent();
            log.info("finding respondentConsentToApplication = " + respondentConsentToApplication);
        }
        return respondentConsentToApplication;
    }

    public KeepDetailsPrivate prePopulateRespondentKeepYourDetailsPrivateCaseData(CaseData caseData, String authorisation) {
        log.info("Inside prePopulateRespondentKeepYourDetailsPrivateCaseData");
        Optional<Element<PartyDetails>> respondentParty;
        KeepDetailsPrivate respondentKeepDetailsPrivate = null;
        respondentParty = findActiveRespondent(caseData, authorisation);
        if (respondentParty.isPresent()) {
            log.info("finding respondentParty is present ");
            respondentKeepDetailsPrivate = respondentParty.get().getValue().getResponse().getKeepDetailsPrivate();
            log.info("finding respondentKeepDetailsPrivate = " + respondentKeepDetailsPrivate);
        }
        return respondentKeepDetailsPrivate;
    }

    private Optional<Element<PartyDetails>> findActiveRespondent(CaseData caseData, String authorisation) {
        Optional<Element<PartyDetails>> activeRespondent = null;
        FindUserCaseRolesResponse findUserCaseRolesResponse = findUserCaseRoles(caseData, authorisation);

        if (findUserCaseRolesResponse != null) {
            log.info("findUserCaseRolesResponse is not null ");
            List<Element<PartyDetails>> solicitorRepresentedRespondents = getSolicitorRepresentedRespondents(
                caseData,
                findUserCaseRolesResponse
            );

            activeRespondent = solicitorRepresentedRespondents
                .stream()
                .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
                .findFirst();
            log.info("finding activeRespondent " + activeRespondent);
        }
        return activeRespondent;
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


    public Map<String, Object> populateSolicitorRespondentList(CaseData caseData, String authorisation) {
        Map<String, Object> headerMap = new HashMap<>();
        log.info("populateSolicitorRespondentList service: casedata is:: " + caseData);
        FindUserCaseRolesResponse findUserCaseRolesResponse = ccdDataStoreService.findUserCaseRoles(
            String.valueOf(caseData.getId()),
            authorisation
        );
        log.info("findUserCaseRolesResponse:: " + findUserCaseRolesResponse);
        if (findUserCaseRolesResponse != null) {
            log.info("findUserCaseRolesResponse not null ");
            List<Element<PartyDetails>> solicitorRepresentedParties = new ArrayList<>();
            for (CaseUser caseUser : findUserCaseRolesResponse.getCaseUsers()) {
                log.info("caseUser is:: " + caseUser.getCaseRole());
                SolicitorRole.from(caseUser.getCaseRole()).ifPresent(
                    x -> solicitorRepresentedParties.add(caseData.getRespondents().get(x.getIndex())));
            }
            headerMap.put("chooseRespondentDynamicList", ElementUtils.asDynamicList(
                solicitorRepresentedParties,
                null,
                PartyDetails::getLabelForDynamicList
            ));
            log.info("headerMap:: " + headerMap);
        }
        return headerMap;
    }

    public Map<String, Object> updateRespondents(CaseData caseData, String authorisation) {
        log.info("updateRespondents:: caseData" + caseData);
        List<Element<PartyDetails>> solicitorRepresentedParties = new ArrayList<>();
        FindUserCaseRolesResponse findUserCaseRolesResponse = ccdDataStoreService.findUserCaseRoles(
            String.valueOf(caseData.getId()),
            authorisation
        );
        log.info("findUserCaseRolesResponse:: " + findUserCaseRolesResponse);
        if (findUserCaseRolesResponse != null) {
            log.info("findUserCaseRolesResponse is not null ");

            for (CaseUser caseUser : findUserCaseRolesResponse.getCaseUsers()) {
                log.info("caseUser is:: " + caseUser.getCaseRole());
                SolicitorRole.from(caseUser.getCaseRole()).ifPresent(
                    x -> solicitorRepresentedParties.add(caseData.getRespondents().get(x.getIndex())));
            }
        }

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

        for (Element<PartyDetails> solicitorRepresentedParty : solicitorRepresentedParties) {
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
        }

        return Map.of("respondents", respondents);
    }

    public Map<String, Object> updateConsentToApplication(CaseData caseData, String authorisation) {
        log.info("updateConsentToApplication:: caseData" + caseData);
        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        Optional<Element<PartyDetails>> respondingParty = findActiveRespondent(caseData, authorisation);

        Consent respondentConsentToApplication = caseData.getRespondentConsentToApplication();
        if (respondingParty.isPresent()) {
            respondents.stream()
                .filter(party -> Objects.equals(party.getId(), respondingParty.get().getId()))
                .findFirst()
                .ifPresent(party -> {
                    log.info("updateRespondents:: party found. before update " + party);
                    PartyDetails amended = party.getValue().toBuilder()
                        .response(party.getValue().getResponse().toBuilder()
                                      .consent(party.getValue().getResponse().getConsent().toBuilder()
                                                   .consentToTheApplication(respondentConsentToApplication.getConsentToTheApplication())
                                                   .noConsentReason(respondentConsentToApplication.getNoConsentReason())
                                                   .applicationReceivedDate(respondentConsentToApplication.getApplicationReceivedDate())
                                                   .courtOrderDetails(respondentConsentToApplication.getCourtOrderDetails())
                                                   .permissionFromCourt(respondentConsentToApplication.getPermissionFromCourt())
                                                   .build())
                                      .build())
                        .build();

                    respondents.set(respondents.indexOf(party), element(party.getId(), amended));
                    log.info("updateRespondents:: party found. after update " + party);
                });
        }
        return Map.of("respondents", respondents);
    }

    public Map<String, Object> updateKeepDetailsPrivate(CaseData caseData, String authorisation) {
        log.info("updateKeepDetailsPrivate:: caseData" + caseData);
        List<Element<PartyDetails>> respondents = caseData.getRespondents();
        Optional<Element<PartyDetails>> respondingParty = findActiveRespondent(caseData, authorisation);

        KeepDetailsPrivate respondentKeepDetailsPrivate = caseData.getKeepContactDetailsPrivate();
        if (respondingParty.isPresent()) {
            respondents.stream()
                .filter(party -> Objects.equals(party.getId(), respondingParty.get().getId()))
                .findFirst()
                .ifPresent(party -> {
                    log.info("updateRespondents:: party found. before update " + party);
                    PartyDetails amended = party.getValue().toBuilder()
                        .response(party.getValue().getResponse().toBuilder()
                                      .keepDetailsPrivate(party.getValue().getResponse().getKeepDetailsPrivate().toBuilder()
                                                              .confidentiality(respondentKeepDetailsPrivate.getConfidentiality())
                                                              .confidentialityList(respondentKeepDetailsPrivate.getConfidentialityList())
                                                              .otherPeopleKnowYourContactDetails(
                                                                  respondentKeepDetailsPrivate.getOtherPeopleKnowYourContactDetails())
                                                              .build())
                                      .build())
                        .build();

                    respondents.set(respondents.indexOf(party), element(party.getId(), amended));
                    log.info("updateRespondents:: party found. after update " + party);
                });
        }
        return Map.of("respondents", respondents);
    }
}
