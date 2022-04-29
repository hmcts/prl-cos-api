package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.clients.CourtFinderApi;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.Court;
import uk.gov.hmcts.reform.prl.models.court.CourtEmailAddress;
import uk.gov.hmcts.reform.prl.models.court.ServiceArea;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.applicant;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.respondent;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourtFinderService {

    public static final String FAMILY_COURT = "Family Courts";
    public static final String FAMILY_PUBLIC_LAW_CHILDREN_IN_CARE = "Family public law (children in care)";
    public static final String PAPER_PROCESS_INCLUDING_C_100_APPLICATIONS = "Paper process including C100 applications";
    public static final String FAMILY = "Family";
    public static final String CHILD = "child";
    @Autowired
    private CourtFinderApi courtFinderApi;

    public Court getNearestFamilyCourt(CaseData caseData) throws NotFoundException {
        ServiceArea serviceArea;

        if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
            serviceArea = courtFinderApi
                .findClosestDomesticAbuseCourtByPostCode(
                    getPostcodeFromWrappedParty(caseData.getApplicantsFL401()));
        } else {
            log.info("Getting court with postcode: " + getCorrectPartyPostcode(caseData));
            serviceArea = courtFinderApi
                .findClosestChildArrangementsCourtByPostcode(getCorrectPartyPostcode(caseData));
        }

        if (serviceArea.getCourts() != null
            && !serviceArea.getCourts().isEmpty()) {
            return getCourtDetails(serviceArea.getCourts()
                                       .get(0)
                                       .getCourtId());
        } else {
            return null;
        }
    }

    public boolean courtsAreTheSame(Court c1, Court c2) {
        if (c1 == null || c2 == null) {
            return false;
        }

        return c1.getCourtName().equals(c2.getCourtName())
            && c1.getCourtId().equals(c2.getCourtId());
    }

    public Court getCourtDetails(String courtSlug) {
        return courtFinderApi.getCourtDetails(courtSlug);
    }

    public String getCorrectPartyPostcode(CaseData caseData) throws NotFoundException {
        //current requirements use the first child if multiple children present
        Optional<Child> childOptional = caseData.getChildren()
            .stream()
            .map(Element::getValue)
            .findFirst();

        if (childOptional.isEmpty()) {
            throw new NotFoundException("No child details found");
        }
        Child child = childOptional.get();

        if (child.getChildLiveWith().contains(applicant)) {
            return getPostcodeFromWrappedParty(caseData.getApplicants().get(0));
        } else if (child.getChildLiveWith().contains(respondent)) {
            if (ofNullable(getPostcodeFromWrappedParty(caseData.getRespondents().get(0))).isEmpty()) {
                return getPostcodeFromWrappedParty(caseData.getApplicants().get(0));
            }
            return getPostcodeFromWrappedParty(caseData.getRespondents().get(0));
        } else if (child.getChildLiveWith().contains(anotherPerson) && ofNullable(getFirstOtherPerson(child)).isPresent()) {
            if (ofNullable(getFirstOtherPerson(child).getAddress().getPostCode()).isEmpty()) {
                return getPostcodeFromWrappedParty(caseData.getApplicants().get(0));
            }
            return getFirstOtherPerson(child).getAddress().getPostCode();
        }
        //default to the applicant postcode
        return getPostcodeFromWrappedParty(caseData.    getApplicants().get(0));
    }

    public CaseData setCourtNameAndId(CaseData caseData, Court court) {
        caseData.setCourtName(court.getCourtName());
        caseData.setCourtId(court.getCourtId());
        return caseData;
    }

    private String getPostcodeFromWrappedParty(Element<PartyDetails> party) {
        return party.getValue().getAddress().getPostCode();
    }

    private String getPostcodeFromWrappedParty(PartyDetails partyDetails) {
        return partyDetails.getAddress().getPostCode();
    }

    public boolean courtNameAndIdAreBlank(Optional<String> courtName, Optional<String> courtId) {
        return courtName.isPresent()
            && courtId.isPresent()
            && courtName.get().isBlank()
            && courtId.get().isBlank();
    }

    public OtherPersonWhoLivesWithChild getFirstOtherPerson(Child c) {
        return c.getPersonWhoLivesWithChild()
            .stream()
            .map(Element::getValue)
            .collect(Collectors.toList())
            .get(0);

    }

    public Optional<CourtEmailAddress> getEmailAddress(Court nearestDomesticAbuseCourt) {
        Optional<CourtEmailAddress> emailAddress = Optional.empty();
        if (null != nearestDomesticAbuseCourt) {
            emailAddress = findEmailWithFamilyC100ApplicationKey(nearestDomesticAbuseCourt);
            if (emailAddress.isEmpty()) {
                emailAddress = findEmailWithFamilyCourtKey(nearestDomesticAbuseCourt);
            }
            if (emailAddress.isEmpty()) {
                emailAddress = findEmailWithFamilyLawKey(nearestDomesticAbuseCourt);
            }
            if (emailAddress.isEmpty()) {
                emailAddress = findEmailWithFamilyOnlyKey(nearestDomesticAbuseCourt);
            }
            if (emailAddress.isEmpty()) {
                emailAddress  = findEmailWithChildOnlyKey(nearestDomesticAbuseCourt);
            }
        }
        return emailAddress;
    }

    private Optional<CourtEmailAddress> findEmailWithChildOnlyKey(Court nearestDomesticAbuseCourt) {
        return nearestDomesticAbuseCourt.getCourtEmailAddresses().stream()
            .filter(p -> (p.getDescription() != null && p.getDescription().contains(CHILD)
                || (p.getExplanation() != null && p.getExplanation().contains(CHILD))))
            .findFirst();
    }

    private Optional<CourtEmailAddress> findEmailWithFamilyOnlyKey(Court nearestDomesticAbuseCourt) {
        return nearestDomesticAbuseCourt.getCourtEmailAddresses().stream()
            .filter(p -> (p.getDescription() != null && p.getDescription().contains(FAMILY)
                || (p.getExplanation() != null && p.getExplanation().contains(FAMILY))))
            .findFirst();
    }

    private Optional<CourtEmailAddress> findEmailWithFamilyLawKey(Court nearestDomesticAbuseCourt) {
        return nearestDomesticAbuseCourt.getCourtEmailAddresses().stream()
            .filter(p -> (FAMILY_PUBLIC_LAW_CHILDREN_IN_CARE.equalsIgnoreCase(p.getDescription())))
            .findFirst();
    }

    private Optional<CourtEmailAddress> findEmailWithFamilyC100ApplicationKey(Court nearestDomesticAbuseCourt) {
        return nearestDomesticAbuseCourt.getCourtEmailAddresses().stream()
            .filter(p -> (FAMILY_PUBLIC_LAW_CHILDREN_IN_CARE.equalsIgnoreCase(p.getDescription())
                && (PAPER_PROCESS_INCLUDING_C_100_APPLICATIONS.equalsIgnoreCase(p.getExplanation()))))
            .findFirst();
    }

    private Optional<CourtEmailAddress> findEmailWithFamilyCourtKey(Court nearestDomesticAbuseCourt) {
        return nearestDomesticAbuseCourt.getCourtEmailAddresses().stream()
            .filter(p -> (FAMILY_COURT.equalsIgnoreCase(p.getDescription())))
            .findFirst();
    }

}
