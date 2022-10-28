package uk.gov.hmcts.reform.prl.services.noc;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.SolicitorRole;
import uk.gov.hmcts.reform.prl.models.ChangeOrganisationApprovalStatus;
import uk.gov.hmcts.reform.prl.models.ChangeOrganisationRequest;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.Organisation;
import uk.gov.hmcts.reform.prl.models.caseaccess.OrganisationPolicy;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicList;
import uk.gov.hmcts.reform.prl.models.common.dynamic.DynamicListElement;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.NoticeOfChangeAnswers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class NoticeOfChangeFieldPopulator {
    public final NoticeOfChangeAnswersConverter answersConverter;
    public final RespondentPolicyConverter policyConverter;

    public Map<String, Object> generate(CaseData caseData, SolicitorRole.Representing representing) {
        return generate(caseData, representing, NoticeOfChangeAnswersPopulationStrategy.POPULATE);
    }

    public Map<String, Object> generate(CaseData caseData, SolicitorRole.Representing representing,
                                        NoticeOfChangeAnswersPopulationStrategy strategy) {
        Map<String, Object> data = new HashMap<>();

        String applicant = getApplicantName(caseData);

        List<Element<PartyDetails>> elements = representing.getTarget().apply(caseData);
        int numElements = elements.size();

        List<SolicitorRole> solicitorRoles = SolicitorRole.values(representing);
        for (int i = 0; i < solicitorRoles.size(); i++) {
            SolicitorRole solicitorRole = solicitorRoles.get(i);

            Optional<Element<PartyDetails>> solicitorContainer = i < numElements
                                                                  ? Optional.of(elements.get(i))
                                                                  : Optional.empty();

            OrganisationPolicy organisationPolicy = policyConverter.generate(
                solicitorRole, solicitorContainer
            );

            data.put(String.format(representing.getPolicyFieldTemplate(), i), organisationPolicy);
            data.put("changeOrganisationRequestField",
                     changeRequest(solicitorRole.getCaseRoleLabel(),Organisation.builder().build(),
                                   Organisation.builder().build()));

            Optional<NoticeOfChangeAnswers> possibleAnswer = populateAnswer(
                strategy, applicant, solicitorContainer
            );

            if (possibleAnswer.isPresent()) {
                data.put(String.format(representing.getNocAnswersTemplate(), i), possibleAnswer.get());
            }
        }

        return data;
    }

    private Optional<NoticeOfChangeAnswers> populateAnswer(NoticeOfChangeAnswersPopulationStrategy strategy,
                                                           String applicantName,
                                                           Optional<Element<PartyDetails>> element) {
        if (NoticeOfChangeAnswersPopulationStrategy.BLANK == strategy) {
            return Optional.of(NoticeOfChangeAnswers.builder().build());
        }

        return element.map(e -> answersConverter.generateForSubmission(e, applicantName));
    }

    private String getApplicantName(CaseData caseData) {
        if (isNotEmpty(caseData.getApplicantName())) {
            return caseData.getApplicantName();
        }

        return caseData.getApplicants().get(0).getValue().getSolicitorOrg().getOrganisationName();
    }

    private ChangeOrganisationRequest changeRequest(String role, Organisation add, Organisation remove) {

        final DynamicListElement roleItem = DynamicListElement.builder()
            .code(role)
            .label(role)
            .build();

        return ChangeOrganisationRequest.builder()
            .approvalStatus(ChangeOrganisationApprovalStatus.APPROVED)
            .requestTimestamp(LocalDateTime.now())
            .caseRoleId(DynamicList.builder()
                            .value(roleItem)
                            .listItems(List.of(roleItem))
                            .build())
            .organisationToRemove(remove)
            .organisationToAdd(add)
            .build();
    }

    public enum NoticeOfChangeAnswersPopulationStrategy {
        POPULATE, BLANK
    }
}
