package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
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

    public Consent prePopulateRespondentSolicitorCaseData(CaseData caseData, String authorisation) {
        log.info("Inside prePopulateRespondentSolicitorCaseData");
        FindUserCaseRolesResponse findUserCaseRolesResponse = ccdDataStoreService.findUserCaseRoles(
            String.valueOf(caseData.getId()),
            authorisation
        );
        log.info("findUserCaseRolesResponse = " + findUserCaseRolesResponse);
        Optional<Element<PartyDetails>> respondentParty = null;
        Consent respondentConsentToApplication = null;
        if (findUserCaseRolesResponse != null) {
            log.info("findUserCaseRolesResponse is not null ");
            List<Element<PartyDetails>> solicitorRepresentedParties = new ArrayList<>();
            for (CaseUser caseUser : findUserCaseRolesResponse.getCaseUsers()) {
                log.info("caseUser is = " + caseUser);
                SolicitorRole.from(caseUser.getCaseRole()).ifPresent(
                    x -> solicitorRepresentedParties.add(caseData.getRespondents().get(x.getIndex())));
            }
            log.info("finding solicitorRepresentedParties Party " + solicitorRepresentedParties);
            respondentParty = solicitorRepresentedParties
                .stream()
                .filter(x -> YesOrNo.Yes.equals(x.getValue().getResponse().getActiveRespondent()))
                .findFirst();
            log.info("finding respondentParty " + respondentParty);
            if (respondentParty.isPresent()) {
                log.info("finding respondentParty is present ");
                respondentConsentToApplication = respondentParty.get().getValue().getResponse().getConsent();
                log.info("finding respondentConsentToApplication = " + respondentConsentToApplication);
            }
        }
        return respondentConsentToApplication;
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

    public Map<String, Object> updateRespondents(CaseData caseData) {
        log.info("updateRespondents:: caseData" + caseData);
        UUID selectedRespondentId = caseData.getChooseRespondentDynamicList().getValueCodeAsUuid();
        log.info("updateRespondents:: selectedRespondentId" + selectedRespondentId);
        List<Element<PartyDetails>> partyDetails = caseData.getRespondents();
        partyDetails.stream()
            .filter(party -> Objects.equals(party.getId(), selectedRespondentId))
            .findFirst()
            .ifPresent(party -> {
                log.info("updateRespondents:: party found. before update " + party);
                PartyDetails amended = party.getValue().toBuilder()
                    .response(party.getValue().getResponse().toBuilder().activeRespondent(YesOrNo.Yes).build())
                    .build();

                partyDetails.set(partyDetails.indexOf(party), element(party.getId(), amended));
                log.info("updateRespondents:: party found. after update " + party);
            });
        return Map.of("respondents", partyDetails);
    }
}
