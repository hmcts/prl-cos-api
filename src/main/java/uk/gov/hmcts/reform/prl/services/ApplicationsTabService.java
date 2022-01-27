package uk.gov.hmcts.reform.prl.services;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Applicant;

import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.HearingUrgency;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.Respondent;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.TypeOfApplication;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.CASE_TYPE;
import static uk.gov.hmcts.reform.prl.enums.OrchestrationConstants.JURISDICTION;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationsTabService {

    @Autowired
    CoreCaseDataService coreCaseDataService;

    @Autowired
    ObjectMapper objectMapper;

    public void updateApplicationTabData(CaseData caseData) {

        HearingUrgency hearingUrgency = objectMapper.convertValue(caseData, HearingUrgency.class);

        Map<String, Object> hearingUrgencyMap = toMap(hearingUrgency);

        List<Element<Applicant>> applicants = new ArrayList<>();
        List<PartyDetails> currentApplicants = caseData.getApplicants().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        for (PartyDetails applicant : currentApplicants) {
            Applicant a = objectMapper.convertValue(applicant, Applicant.class);
            Element<Applicant> app = Element.<Applicant>builder().value(a).build();
            applicants.add(app);
        }

        List<Element<Respondent>> respondents = new ArrayList<>();
        List<PartyDetails> currentRespondents = caseData.getRespondents().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());

        for (PartyDetails respondent : currentRespondents) {
            Respondent r = objectMapper.convertValue(respondent, Respondent.class);
            Element<Respondent> res = Element.<Respondent>builder().value(r).build();
            respondents.add(res);
        }

        Map<String, Object> declarationMap = new HashMap<>();
        declarationMap.put("declarationText", "I understand that proceedings for contempt of court may be brought against anyone who makes, or causes to be made, a false statement in a document verified by a statement of truth without an honest belief in its truth. The applicant believes that the facts stated in this form and any continuation sheets are true. [Solicitor Name] is authorised by the applicant to sign this statement.");
        declarationMap.put("agreedBy", "<Solicitor name>");

        TypeOfApplication typeOfApplication = objectMapper.convertValue(caseData, TypeOfApplication.class);

        coreCaseDataService.triggerEvent(
            JURISDICTION,
            CASE_TYPE,
            caseData.getId(),
            "internal-update-application-tab",
            Map.of("hearingUrgencyTable", hearingUrgencyMap,
                   "applicantTable", applicants,
                   "respondentTable", respondents,
                   "declarationTable",declarationMap
//                   "typeOfApplicationTable",toMap(typeOfApplication))
            ));
    }

    private Map<String, Object> toMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }

    private String getAddressString(Address address) {
        Optional<String> firstLine = ofNullable(address.getAddressLine1());
        Optional<String> town = ofNullable(address.getPostTown());
        Optional<String> postCode = ofNullable(address.getPostCode());

        List<Optional<String>> addressFields = new ArrayList<>();
        addressFields.add(firstLine);
        addressFields.add(town);
        addressFields.add(postCode);

        addressFields.removeIf(Optional::isEmpty);
        return addressFields.stream().map(Optional::get).collect(Collectors.joining(","));

    }

    private Map<String, Object> getTypeOfApplicationTable(CaseData caseData) {

        List<String> ordersApplyingFor = objectMapper.convertValue(caseData, TypeOfApplication.class);



    }


}
