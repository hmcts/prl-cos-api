package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.LanguagePreference;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.language.DocumentLanguage;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.english;
import static uk.gov.hmcts.reform.prl.enums.LanguagePreference.welsh;


@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class DocumentLanguageService {

    public DocumentLanguage docGenerateLang(CaseData caseData) {

        DocumentLanguage docLanguage = DocumentLanguage.builder().build();

        Optional<YesOrNo> welshLanguageRequirement = ofNullable(caseData.getWelshLanguageRequirement());
        Optional<LanguagePreference> applicationLanguage = ofNullable(caseData.getWelshLanguageRequirementApplication());
        Optional<YesOrNo> englishRequirements = ofNullable(caseData.getWelshLanguageRequirementApplicationNeedEnglish());
        Optional<YesOrNo> welshRequirements = ofNullable(caseData.getLanguageRequirementApplicationNeedWelsh());

        if (welshLanguageRequirement.isPresent() && welshLanguageRequirement.get().equals(YesOrNo.Yes)) {
            if (applicationLanguage.isPresent() && applicationLanguage.get().equals(english)) {
                docLanguage = docLanguage.toBuilder().isGenEng(true).build();
                if (welshRequirements.isPresent() && welshRequirements.get().equals(YesOrNo.Yes)) {
                    docLanguage = docLanguage.toBuilder().isGenWelsh(true).build();
                }
            } else if (applicationLanguage.isPresent() && applicationLanguage.get().equals(welsh)) {
                docLanguage = docLanguage.toBuilder().isGenWelsh(true).build();
                docLanguage = docLanguage.toBuilder().isGenEng(false).build();
                if (englishRequirements.isPresent() && englishRequirements.get().equals(YesOrNo.Yes)) {
                    docLanguage = docLanguage.toBuilder().isGenEng(true).build();
                }
            }
        }
        return docLanguage;
    }


}
