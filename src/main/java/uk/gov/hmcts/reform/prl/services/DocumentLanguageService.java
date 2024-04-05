package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentLanguageService {

    /**
     * Default - English only.
     * If Welsh selected - Both English & Welsh.
     *
     * @return Document language preference based on Welsh requirement.
     */
    public DocumentLanguage docGenerateLang(CaseData caseData) {

        DocumentLanguage docLanguage = DocumentLanguage.builder()
            .isGenEng(true)
            .build();
        if (null != caseData.getWelshLanguageRequirement()
            && YesOrNo.Yes.equals(caseData.getWelshLanguageRequirement())) {
            docLanguage = docLanguage.toBuilder().isGenWelsh(true).build();
        }
        return docLanguage;
    }


}
