package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Optional;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.ANOTHER_PERSON;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.APPLICANT;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.RESPONDENT;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CourtFinderService {

    @Autowired
    private CourtFinderApi courtFinderApi;

    public CaseData setCourtUnlessCourtAlreadyPresent(CaseData caseData, Court court) {
        Optional<String> courtName = ofNullable(caseData.getCourtName());
        Optional<String> courtId = ofNullable(caseData.getCourtId());

        if ((courtName.isEmpty() && courtId.isEmpty()) || (courtName.get().isBlank() && courtId.get().isBlank())) {
            setCourtNameAndId(caseData, court);
        }
        return caseData;
    }

    public Court getClosestChildArrangementsCourt(CaseData caseData)  {

        String postcode = getCorrectPartyPostcode(caseData);

        log.info("Getting closest court for postcode: {}", postcode);

        String courtSlug = courtFinderApi.findClosestChildArrangementsCourtByPostcode(postcode)
            .getCourts()
            .get(0)
            .getCourtId();

        return getCourtDetails(courtSlug);
    }

    public Court getCourtDetails(String courtSlug) {
        return courtFinderApi.getCourtDetails(courtSlug);
    }

    public String getCorrectPartyPostcode(CaseData caseData) {
        //current requirements use the first child if multiple children present
        Child child = caseData.getChildren()
            .stream()
            .map(Element::getValue)
            .findFirst()
            .get();

        if (child.getChildLiveWith().contains(APPLICANT)) {
            return getPostcodeFromWrappedParty(caseData.getApplicants().get(0));
        } else if (child.getChildLiveWith().contains(RESPONDENT)) {
            return getPostcodeFromWrappedParty(caseData.getRespondents().get(0));
        } else if (child.getChildLiveWith().contains(ANOTHER_PERSON)) {
            if (ofNullable(child.getAddress().getPostCode()).isPresent()) {
                return child.getAddress().getPostCode();
            }
        }
        //default to the applicant postcode
        return getPostcodeFromWrappedParty(caseData.getApplicants().get(0));
    }

    public CaseData setCourtNameAndId(CaseData caseData, Court court) {
        caseData.setCourtName(court.getCourtName());
        caseData.setCourtId(court.getCourtId());
        return caseData;
    }

    private String getPostcodeFromWrappedParty(Element<PartyDetails> party) {
        return party.getValue().getAddress().getPostCode();
    }

}
