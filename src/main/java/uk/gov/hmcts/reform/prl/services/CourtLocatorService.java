package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.LiveWithEnum;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.checkerframework.checker.nullness.Opt.isPresent;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.APPLICANT;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.RESPONDENT;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CourtLocatorService {




    public String getCorrectPartyPostcode(CaseData caseData) {

        //current requirements use the first child if multiple children present
        Child child = caseData.getChildren()
            .stream()
            .map(Element::getValue)
            .findFirst()
            .get();

        if (child.getChildLiveWith().contains(APPLICANT)){
            return caseData.getApplicants().get(0).getValue().getAddress().getPostCode();
        }
        else if (child.getChildLiveWith().contains(RESPONDENT)){
            return caseData.getRespondents().get(0).getValue().getAddress().getPostCode();
        }
        else {
            if (ofNullable(child.getAddress().getPostCode()).isPresent()) {
                return child.getAddress().getPostCode();
            }
            return caseData.getApplicants().get(0).getValue().getAddress().getPostCode();
        }
    }

    public Optional<String> getPostcode(PartyDetails party) {
        return ofNullable(party.getAddress().getPostCode());
    }

    public boolean isPostCodePresent(PartyDetails partyDetails) {
        return isPresent(partyDetails.getAddress().getPostCode());
    }



}
