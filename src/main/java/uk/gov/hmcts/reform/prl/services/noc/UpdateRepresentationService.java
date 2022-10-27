package uk.gov.hmcts.reform.prl.services.noc;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;
import uk.gov.hmcts.reform.prl.enums.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.RespondentSolicitor;
import uk.gov.hmcts.reform.prl.models.WithSolicitor;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UpdateRepresentationService {

    private final ChangeOfRepresentationService changeOfRepresentationService;
    private final List<NoticeOfChangeUpdateAction> updateActions;

    public Map<String, Object> updateRepresentation(CaseData caseData, UserDetails solicitor) {
        final ChangeOrganisationRequest change = caseData.getChangeOrganisationRequest();

        if (isEmpty(change) || isEmpty(change.getCaseRoleId()) || isEmpty(change.getOrganisationToAdd())) {
            throw new IllegalStateException("Invalid or missing ChangeOrganisationRequest: " + change);
        }

        final SolicitorRole role = SolicitorRole.from(change.getCaseRoleId().getValueCode()).orElseThrow();

        final SolicitorRole.Representing representing = role.getRepresenting();

        final List<Element<WithSolicitor>> elements = defaultIfNull(
            representing.getTarget().apply(caseData), Collections.emptyList()
        );

        final WithSolicitor container = elements.get(role.getIndex()).getValue();

        RespondentSolicitor removedSolicitor = container.getSolicitor();

        RespondentSolicitor addedSolicitor = RespondentSolicitor.builder()
            .email(solicitor.getEmail())
            .firstName(solicitor.getForename())
            .lastName(solicitor.getSurname().orElse(EMPTY))
            .organisation(change.getOrganisationToAdd())
            .build();

        HashMap<String, Object> data = updateActions.stream()
            .filter(action -> action.accepts(representing))
            .findFirst()
            .map(action -> new HashMap<>(action.applyUpdates(container, caseData, addedSolicitor)))
            .orElse(new HashMap<>());

        List<Element<ChangeOfRepresentation>> auditList = changeOfRepresentationService.changeRepresentative(
            ChangeOfRepresentationRequest.builder()
                .method(ChangeOfRepresentationMethod.NOC)
                .by(solicitor.getEmail())
                .current(caseData.getChangeOfRepresentatives())
                .addedRepresentative(addedSolicitor)
                .removedRepresentative(removedSolicitor)
                .build()
        );

        data.put("changeOfRepresentatives", auditList);

        return data;
    }
}
