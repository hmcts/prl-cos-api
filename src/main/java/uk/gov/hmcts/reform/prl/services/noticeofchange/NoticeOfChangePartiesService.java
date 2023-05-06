package uk.gov.hmcts.reform.prl.services.noticeofchange;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.noticeofchange.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.noticeofchange.NoticeOfChangeParties;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.NoticeOfChangePartiesConverter;
import uk.gov.hmcts.reform.prl.utils.noticeofchange.RespondentPolicyConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService.NoticeOfChangeAnswersPopulationStrategy.BLANK;
import static uk.gov.hmcts.reform.prl.services.noticeofchange.NoticeOfChangePartiesService.NoticeOfChangeAnswersPopulationStrategy.POPULATE;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NoticeOfChangePartiesService {
    public final NoticeOfChangePartiesConverter partiesConverter;
    public final RespondentPolicyConverter policyConverter;

    public Map<String, Object> generate(CaseData caseData, SolicitorRole.Representing representing) {
        return generate(caseData, representing, POPULATE);
    }

    public Map<String, Object> generate(CaseData caseData, SolicitorRole.Representing representing,
                                        NoticeOfChangeAnswersPopulationStrategy strategy) {
        Map<String, Object> data = new HashMap<>();

        List<Element<PartyDetails>> elements = representing.getTarget().apply(caseData);
        int numElements = null != elements ? elements.size() : 0;

        List<SolicitorRole> solicitorRoles = SolicitorRole.values(representing);
        for (int i = 0; i < solicitorRoles.size(); i++) {
            SolicitorRole solicitorRole = solicitorRoles.get(i);

            if (null != elements) {
                Optional<Element<PartyDetails>> solicitorContainer = i < numElements
                    ? Optional.of(elements.get(i))
                    : Optional.empty();

                OrganisationPolicy organisationPolicy = policyConverter.generate(
                    solicitorRole, solicitorContainer
                );

                data.put(String.format(representing.getPolicyFieldTemplate(), i), organisationPolicy);

                Optional<NoticeOfChangeParties> possibleAnswer = populateAnswer(
                    strategy, solicitorContainer
                );

                if (possibleAnswer.isPresent()) {
                    data.put(String.format(representing.getNocAnswersTemplate(), i), possibleAnswer.get());
                }

            }
        }

        return data;
    }

    private Optional<NoticeOfChangeParties> populateAnswer(NoticeOfChangeAnswersPopulationStrategy strategy,
                                                           Optional<Element<PartyDetails>> element) {
        if (BLANK == strategy) {
            return Optional.of(NoticeOfChangeParties.builder().build());
        }
        return element.map(partiesConverter::generateForSubmission);
    }

    public enum NoticeOfChangeAnswersPopulationStrategy {
        POPULATE, BLANK
    }
}
