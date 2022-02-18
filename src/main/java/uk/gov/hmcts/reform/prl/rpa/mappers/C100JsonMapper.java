package uk.gov.hmcts.reform.prl.rpa.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.rpa.mappers.json.NullAwareJsonObjectBuilder;

import javax.json.JsonObject;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class C100JsonMapper {

    private final ChildrenMapper childrenMapper;

    public JsonObject map(CaseData caseData) {

        return new NullAwareJsonObjectBuilder()
            .add("id", caseData.getId())
            .add("children", childrenMapper.map(caseData.getChildren()))
            .build();
    }
}
