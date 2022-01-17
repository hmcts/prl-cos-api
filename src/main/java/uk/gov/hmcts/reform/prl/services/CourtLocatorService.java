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

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CourtLocatorService {


    public String whichPostCodeToUse(CaseData caseData) {

//        List<Child> children = caseData.getChildren()
//            .stream()
//            .map(Element::getValue)
//            .collect(Collectors.toList());
//
//        Optional<Child> child = ofNullable(children.get(0));
//
//        if (child.isPresent()) {
//            List<LiveWithEnum> childLivesWith = child.get().getChildLiveWith();
//        }

        return "X";

    }

    public Optional<String> getPostcode(PartyDetails party) {
        return ofNullable(party.getAddress().getPostCode());
    }



}
