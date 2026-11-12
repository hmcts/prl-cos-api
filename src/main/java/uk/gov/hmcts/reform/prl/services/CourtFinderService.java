package uk.gov.hmcts.reform.prl.services;

import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.Child;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.OtherPersonWhoLivesWithChild;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.court.CourtVenue;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.anotherPerson;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.applicant;
import static uk.gov.hmcts.reform.prl.enums.LiveWithEnum.respondent;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CourtFinderService {
    private final OsCourtFinderService osCourtFinderService;
    private final LocationRefDataService locationRefDataService;
    private final SystemUserService systemUserService;

    public CourtVenue getNearestFamilyCourt(CaseData caseData) throws NotFoundException {
        log.info("Finding nearest family court for case type : {}", caseData.getCaseTypeOfApplication());
        try {
            if (PrlAppsConstants.FL401_CASE_TYPE.equalsIgnoreCase(caseData.getCaseTypeOfApplication())) {
                String courtId = caseData.getCourtId();
                if (caseData.getCaseManagementLocation() != null
                    && caseData.getCaseManagementLocation().getBaseLocation() != null) {
                    courtId = caseData.getCaseManagementLocation().getBaseLocation();
                }
                return courtId == null ? null : locationRefDataService
                    .getCourtDetailsFromEpimmsId(courtId, systemUserService.getSysUserToken()).orElse(null);
            } else {
                return getC100NearestFamilyCourt(getPostcode(caseData));

            }
        } catch (Exception e) {
            log.error("Unable to resolve family court for case {}", caseData.getId(), e);
        }
        return null;
    }

    public CourtVenue getC100NearestFamilyCourt(String postcode) throws NotFoundException {
        return osCourtFinderService.getC100NearestFamilyCourt(postcode);
    }

    private String getPostcode(CaseData caseData) throws NotFoundException {
        return nonNull(caseData.getC100RebuildData())
            && nonNull(caseData.getC100RebuildData().getC100RebuildChildPostCode())
            ? caseData.getC100RebuildData().getC100RebuildChildPostCode()
            : getCorrectPartyPostcode(caseData);
    }

    public String getCorrectPartyPostcodeV2(CaseData caseData) throws NotFoundException {
        //current requirements use the first child if multiple children present
        Optional<ChildDetailsRevised> childOptional = caseData.getNewChildDetails()
            .stream()
            .map(Element::getValue)
            .findFirst();

        if (childOptional.isEmpty()) {
            throw new NotFoundException("No child details found");
        }

        Optional<List<Element<PartyDetails>>> othersPerson = ofNullable(caseData.getOtherPartyInTheCaseRevised());

        Optional<PartyDetails> partyDetails = othersPerson.flatMap(elements -> elements.stream()
                .map(Element::getValue)
                .findFirst());

        List<Element<ChildrenAndApplicantRelation>> childAndApplicantRelations = caseData.getRelations().getChildAndApplicantRelations();
        List<Element<ChildrenAndRespondentRelation>> childAndRespondentRelations = caseData.getRelations().getChildAndRespondentRelations();
        Optional<ChildrenAndApplicantRelation> childrenAndApplicantRelation = childAndApplicantRelations
                .stream()
                .map(Element::getValue)
                .findFirst();

        Optional<ChildrenAndRespondentRelation> childrenAndRespondentRelation = childAndRespondentRelations
                .stream()
                .map(Element::getValue)
                .findFirst();
        Optional<ChildrenAndOtherPeopleRelation> childrenAndOtherPeopleRelation = ofNullable(caseData.getRelations()
                                                                                                 .getChildAndOtherPeopleRelations())
                .isPresent() ? caseData.getRelations().getChildAndOtherPeopleRelations()
                .stream()
                .map(Element::getValue)
                .findFirst() : Optional.empty();

        if (!childrenAndApplicantRelation.isEmpty() && YesOrNo.Yes.equals(childrenAndApplicantRelation.get().getChildLivesWith())) {
            return getPostcodeFromWrappedParty(caseData.getApplicants().get(0));
        } else if (!childrenAndRespondentRelation.isEmpty() && YesOrNo.Yes.equals(childrenAndRespondentRelation.get().getChildLivesWith())) {
            if (ofNullable(getPostcodeFromWrappedParty(caseData.getRespondents().get(0))).isEmpty()) {
                return getPostcodeFromWrappedParty(caseData.getApplicants().get(0));
            }
            return getPostcodeFromWrappedParty(caseData.getRespondents().get(0));
        } else if (!childrenAndOtherPeopleRelation.isEmpty() && YesOrNo.Yes.equals(childrenAndOtherPeopleRelation.get().getChildLivesWith())) {
            if (partyDetails.isPresent() && getPostCodeOtherPerson(partyDetails.get()).isEmpty()) {
                return getPostcodeFromWrappedParty(caseData.getApplicants().get(0));
            }
            if (!partyDetails.isEmpty()) {
                return getPostCodeOtherPerson(partyDetails.get());
            }
        }
        //default to the applicant postcode
        return getPostcodeFromWrappedParty(caseData.getApplicants().get(0));
    }

    public String getCorrectPartyPostcode(CaseData caseData) throws NotFoundException {
        if (PrlAppsConstants.TASK_LIST_VERSION_V2.equals(caseData.getTaskListVersion())
            || PrlAppsConstants.TASK_LIST_VERSION_V3.equals(caseData.getTaskListVersion())) {
            return getCorrectPartyPostcodeV2(caseData);
        }

        //current requirements use the first child if multiple children present
        Child child = Optional.ofNullable(caseData.getChildren())
            .orElse(Collections.emptyList())
            .stream()
            .map(Element::getValue)
            .findFirst()
            .orElseThrow(() -> new NotFoundException("No child details found"));

        if  (child != null) {
            if (child.getChildLiveWith().contains(applicant)) {
                return getPostcodeFromWrappedParty(caseData.getApplicants().get(0));
            } else if (child.getChildLiveWith().contains(respondent)) {
                if (ofNullable(getPostcodeFromWrappedParty(caseData.getRespondents().get(0))).isEmpty()) {
                    return getPostcodeFromWrappedParty(caseData.getApplicants().get(0));
                }
                return getPostcodeFromWrappedParty(caseData.getRespondents().get(0));
            } else if (child.getChildLiveWith().contains(anotherPerson)
                && ofNullable(getFirstOtherPerson(child)).isPresent()) {
                if (getPostCode(getFirstOtherPerson(child)).isEmpty()) {
                    return getPostcodeFromWrappedParty(caseData.getApplicants().get(0));
                }
                return getFirstOtherPerson(child).getAddress().getPostCode();
            }
        }
        //default to the applicant postcode
        return getPostcodeFromWrappedParty(caseData.getApplicants().get(0));
    }


    public String getPostCode(OtherPersonWhoLivesWithChild otherPerson) {
        return ofNullable(otherPerson)
            .map(OtherPersonWhoLivesWithChild::getAddress)
            .map(Address::getPostCode)
            .orElse("");
    }

    public String getPostCodeOtherPerson(PartyDetails otherPerson) {
        return ofNullable(otherPerson)
            .map(PartyDetails::getAddress)
            .map(Address::getPostCode)
            .orElse("");
    }

    private String getPostcodeFromWrappedParty(Element<PartyDetails> party) {
        return party.getValue().getAddress().getPostCode();
    }

    public OtherPersonWhoLivesWithChild getFirstOtherPerson(Child c) {
        return c.getPersonWhoLivesWithChild()
            .stream()
            .map(Element::getValue)
            .toList()
            .get(0);

    }

}
