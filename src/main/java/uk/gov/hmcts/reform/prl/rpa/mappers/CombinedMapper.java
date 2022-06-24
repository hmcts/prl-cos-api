package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.HashMap;
import java.util.Map;
import javax.json.JsonArray;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Data
public class CombinedMapper {

    private final ApplicantsMapper applicantsMapper;
    private final RespondentsMapper respondentsMapper;
    private final SolicitorsMapper solicitorsMapper;
    private JsonArray applicantArray;
    private JsonArray respondentArray;

    public JsonArray map(CaseData caseData) {
        Map<String, PartyDetails> solicitorMap = new HashMap<>();
        Map<String, PartyDetails> respondentMap = new HashMap<>();
        applicantArray = applicantsMapper.map(caseData.getApplicants(), solicitorMap);
        respondentArray = respondentsMapper.map(caseData.getRespondents(), respondentMap);
        solicitorMap.putAll(respondentMap);
        log.info("final solicitor list size is {}", solicitorMap.size());
        return solicitorsMapper.mapSolicitorList(solicitorMap);
    }
}
