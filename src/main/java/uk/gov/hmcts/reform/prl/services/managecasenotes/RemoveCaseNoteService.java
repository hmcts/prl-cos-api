package uk.gov.hmcts.reform.prl.services.managecasenotes;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.RemovableCaseNote;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoveCaseNoteService {

    private final ObjectMapper objectMapper;
    private final SystemUserService systemUserService;
    private final AuthTokenGenerator authTokenGenerator;

    public CaseData populateCaseNoteRemovalList(CaseData caseData) {
        return caseData.toBuilder()
            .removableCaseNotes(getRemovableCaseNotes(caseData))
            .build();
    }

    private List<Element<RemovableCaseNote>> getRemovableCaseNotes(CaseData caseData) {
       return caseData.getRemovableCaseNotes() != null ? caseData.getRemovableCaseNotes() : null;
    }
}
