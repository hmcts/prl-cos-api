package uk.gov.hmcts.reform.prl.services.c100respondentsolicitor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.CaseUser;
import uk.gov.hmcts.reform.prl.models.caseaccess.FindUserCaseRolesResponse;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.caseaccess.CcdDataStoreService;
import uk.gov.hmcts.reform.prl.utils.ElementUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class C100RespondentSolicitorService {
    private final CcdDataStoreService ccdDataStoreService;

    public void prePopulateRespondentSolicitorCaseData(String caseId, String authorisation) {
        CaseUser caseUser = ccdDataStoreService.findRespondentSolicitorCaseRoles(caseId, authorisation);

        log.info("CaseId: {} assigned case access to user {}", caseId, caseUser.getCaseRole());
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
