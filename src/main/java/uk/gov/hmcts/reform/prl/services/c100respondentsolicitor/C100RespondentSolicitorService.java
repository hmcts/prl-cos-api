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

import java.util.*;

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

        FindUserCaseRolesResponse findUserCaseRolesResponse = ccdDataStoreService.findUserCaseRoles(
            String.valueOf(caseData.getId()),
            authorisation
        );
        if (findUserCaseRolesResponse != null) {
            List<Element<PartyDetails>> solicitorRepresentedParties = new ArrayList<>();
            for (CaseUser caseUser : findUserCaseRolesResponse.getCaseUsers()) {
                SolicitorRole.from(caseUser.getCaseRole()).ifPresent(
                    x -> solicitorRepresentedParties.add(caseData.getRespondents().get(x.getIndex())));
            }
            headerMap.put("chooseRespondentDynamicList", ElementUtils.asDynamicList(
                solicitorRepresentedParties,
                null,
                PartyDetails::getLabelForDynamicList
            ));
        }
        return headerMap;
    }
}
