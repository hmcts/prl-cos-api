package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.prl.enums.Gender;
import uk.gov.hmcts.reform.prl.enums.OrderTypeEnum;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndApplicantRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.ChildrenAndRespondentRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.ChildAndApplicantRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.ChildAndOtherPeopleRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.ChildAndRespondentRelation;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.ChildDetailsRevised;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherChildrenNotInTheCase;
import uk.gov.hmcts.reform.prl.models.complextypes.applicationtab.OtherPersonInTheCaseRevised;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.THIS_INFORMATION_IS_CONFIDENTIAL;
import static uk.gov.hmcts.reform.prl.enums.Gender.other;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApplicationsTabServiceHelper {

    @Autowired
    ObjectMapper objectMapper;

    public List<Element<OtherPersonInTheCaseRevised>> getOtherPeopleInTheCaseRevisedTable(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> otherPeopleCheck = ofNullable(caseData.getOthersToNotify());
        List<Element<OtherPersonInTheCaseRevised>> otherPersonsInTheCase = new ArrayList<>();

        if (otherPeopleCheck.isEmpty() || otherPeopleCheck.get().isEmpty()) {
            OtherPersonInTheCaseRevised op = OtherPersonInTheCaseRevised.builder().build();
            Element<OtherPersonInTheCaseRevised> other = Element.<OtherPersonInTheCaseRevised>builder().value(op).build();
            otherPersonsInTheCase.add(other);
            return otherPersonsInTheCase;
        }

        List<PartyDetails> otherPeople = caseData.getOthersToNotify().stream().map(Element::getValue).collect(Collectors.toList());
        otherPeople = maskConfidentialDetails(otherPeople);
        for (PartyDetails p : otherPeople) {
            OtherPersonInTheCaseRevised other = objectMapper.convertValue(p, OtherPersonInTheCaseRevised.class);
            Element<OtherPersonInTheCaseRevised> wrappedPerson = Element.<OtherPersonInTheCaseRevised>builder()
                .value(other).build();
            otherPersonsInTheCase.add(wrappedPerson);
        }
        return otherPersonsInTheCase;
    }


    public List<Element<ChildDetailsRevised>> getChildRevisedDetails(CaseData caseData) {
        log.info("-->getChildRevisedDetails()--->start");
        Optional<List<Element<uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised>>> childElementsCheck =
            ofNullable(caseData.getNewChildDetails());
        List<Element<ChildDetailsRevised>> childFinalList = new ArrayList<>();
        if (childElementsCheck.isEmpty()) {
            ChildDetailsRevised child = ChildDetailsRevised.builder().build();
            Element<ChildDetailsRevised> app = Element.<ChildDetailsRevised>builder().value(child).build();
            childFinalList.add(app);
            return childFinalList;
        }
        List<uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised> childList =
            caseData.getNewChildDetails().stream()
            .map(Element::getValue)
            .collect(Collectors.toList());
        for (uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised child : childList) {
            ChildDetailsRevised c = getChildDetailsRevised(child);
            Element<ChildDetailsRevised> res = Element.<ChildDetailsRevised>builder().value(c).build();
            childFinalList.add(res);
        }
        log.info("getChildRevisedDetails : {}",childFinalList);
        return childFinalList;
    }


    protected ChildDetailsRevised getChildDetailsRevised(uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised child) {
        Optional<List<OrderTypeEnum>> orderAppliedFor = ofNullable(child.getOrderAppliedFor());
        Optional<Gender> childGender = ofNullable(child.getGender());
        return ChildDetailsRevised.builder().firstName(child.getFirstName())
            .lastName(child.getLastName())
            .dateOfBirth(child.getDateOfBirth())
            .gender(child.getGender())
            .otherGender(!childGender.isEmpty() &&  childGender.isPresent() && childGender.get().equals(other) ? child.getOtherGender() : "")
            .orderAppliedFor(orderAppliedFor.isEmpty() ? null : child.getOrderAppliedFor().stream()
                .map(OrderTypeEnum::getDisplayedValue).collect(
                    Collectors.joining(", ")))
            .parentalResponsibilityDetails(child.getParentalResponsibilityDetails())
            .build();
    }

    public List<Element<ChildAndApplicantRelation>> getChildAndApplicantsRelationTable(CaseData caseData) {
        log.info("-->getChildAndApplicantsRelationTable()--->start");
        List<Element<ChildAndApplicantRelation>> applicants = new ArrayList<>();
        try {
            Optional<List<Element<ChildrenAndApplicantRelation>>> checkApplicants = ofNullable(caseData.getChildAndApplicantRelations());
            if (checkApplicants.isEmpty()) {
                ChildAndApplicantRelation a = ChildAndApplicantRelation.builder().build();
                Element<ChildAndApplicantRelation> app = Element.<ChildAndApplicantRelation>builder().value(a).build();
                applicants.add(app);
                return applicants;
            }
            List<ChildrenAndApplicantRelation> currentApplicants = caseData.getChildAndApplicantRelations().stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            log.info("-->currentApplicants before --->{}", currentApplicants);
            for (ChildrenAndApplicantRelation applicant : currentApplicants) {
                ChildAndApplicantRelation a = objectMapper.convertValue(applicant, ChildAndApplicantRelation.class);
                Element<ChildAndApplicantRelation> app = Element.<ChildAndApplicantRelation>builder().value(a).build();
                applicants.add(app);
            }
        } catch(Exception exception) {
            exception.printStackTrace();
        }
        log.info("-->currentApplicants final --->{}", applicants);
        log.info("-->getChildAndApplicantsRelationTable()--->end");
        return applicants;
    }


    public List<Element<ChildAndRespondentRelation>> getChildAndRespondentRelationsTable(CaseData caseData) {
        log.info("-->getChildAndRespondentRelationsTable()--->start");
        List<Element<ChildAndRespondentRelation>> respondents = new ArrayList<>();
        try {
            Optional<List<Element<ChildrenAndRespondentRelation>>> checkRespondents = ofNullable(caseData.getChildAndRespondentRelations());

            if (!checkRespondents.isPresent()) {
                ChildAndRespondentRelation a = ChildAndRespondentRelation.builder().build();
                Element<ChildAndRespondentRelation> app = Element.<ChildAndRespondentRelation>builder().value(a).build();
                respondents.add(app);
                return respondents;
            }
            List<ChildrenAndRespondentRelation> currentApplicants = caseData.getChildAndRespondentRelations().stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            log.info("-->currentRelations before --->{}", currentApplicants);
            for (ChildrenAndRespondentRelation respondent : currentApplicants) {
                ChildAndRespondentRelation a = objectMapper.convertValue(respondent, ChildAndRespondentRelation.class);
                Element<ChildAndRespondentRelation> app = Element.<ChildAndRespondentRelation>builder().value(a).build();
                respondents.add(app);
            }
        } catch(Exception exception) {
              exception.printStackTrace();
        }
        log.info("-->currentRelations final List--->{}",respondents);
        log.info("-->getChildAndRespondentRelationsTable()--->end");
        return respondents;
    }


    public List<Element<ChildAndOtherPeopleRelation>> getChildAndOtherPeopleRelationsTable(CaseData caseData) {
        log.info("-->getChildAndOtherPeopleRelationsTable()--->start");
        List<Element<ChildAndOtherPeopleRelation>> otherPeopleRelations = new ArrayList<>();
        try {
            Optional<List<Element<ChildrenAndOtherPeopleRelation>>> checkRespondents = ofNullable(caseData.getChildAndOtherPeopleRelations());

            if (checkRespondents.isEmpty()) {
                ChildAndOtherPeopleRelation a = ChildAndOtherPeopleRelation.builder().build();
                Element<ChildAndOtherPeopleRelation> app = Element.<ChildAndOtherPeopleRelation>builder().value(a).build();
                otherPeopleRelations.add(app);
                return otherPeopleRelations;
            }
            List<ChildrenAndOtherPeopleRelation> currentApplicants = caseData.getChildAndOtherPeopleRelations().stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            log.info("-->otherPeopleRelations before List--->{}",currentApplicants);
            for (ChildrenAndOtherPeopleRelation otherPeople : currentApplicants) {
                ChildAndOtherPeopleRelation a = objectMapper.convertValue(
                    otherPeople,
                    ChildAndOtherPeopleRelation.class
                );
                Element<ChildAndOtherPeopleRelation> app = Element.<ChildAndOtherPeopleRelation>builder().value(a).build();
                otherPeopleRelations.add(app);
            }
        } catch(Exception exception) {
            exception.printStackTrace();
        }
        log.info("-->otherPeopleRelations final List--->{}",otherPeopleRelations);
        log.info("-->getChildAndOtherPeopleRelationsTable()--->end");
        return otherPeopleRelations;
    }


    public List<PartyDetails> maskConfidentialDetails(List<PartyDetails> currentApplicants) {
        for (PartyDetails applicantDetails : currentApplicants) {
            if ((YesOrNo.Yes).equals(applicantDetails.getIsPhoneNumberConfidential())) {
                applicantDetails.setPhoneNumber(THIS_INFORMATION_IS_CONFIDENTIAL);
            }
            if ((YesOrNo.Yes).equals(applicantDetails.getIsEmailAddressConfidential())) {
                applicantDetails.setEmail(THIS_INFORMATION_IS_CONFIDENTIAL);
            }
            if ((YesOrNo.Yes).equals(applicantDetails.getIsAddressConfidential())) {
                applicantDetails.setAddress(Address.builder().addressLine1(THIS_INFORMATION_IS_CONFIDENTIAL).build());
            }
        }
        return currentApplicants;
    }

    public List<Element<OtherChildrenNotInTheCase>> getOtherChildNotInTheCaseTable(CaseData caseData) {
        log.info("getOtherChildNotInTheCaseTable()--->start");
        Optional<List<Element<uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase>>> otherPeopleCheck =
            ofNullable(caseData.getChildrenNotInTheCase());
        List<Element<OtherChildrenNotInTheCase>> otherPersonsInTheCase = new ArrayList<>();

        if (otherPeopleCheck.isEmpty() || otherPeopleCheck.get().isEmpty()) {
            OtherChildrenNotInTheCase op = OtherChildrenNotInTheCase.builder().build();
            Element<OtherChildrenNotInTheCase> other = Element.<OtherChildrenNotInTheCase>builder().value(op).build();
            otherPersonsInTheCase.add(other);
            return otherPersonsInTheCase;
        }

        List<uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase> otherPeople =
            caseData.getChildrenNotInTheCase().stream().map(Element::getValue).collect(Collectors.toList());

        for (uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase p : otherPeople) {
            OtherChildrenNotInTheCase other = objectMapper.convertValue(p, OtherChildrenNotInTheCase.class);
            Element<OtherChildrenNotInTheCase> wrappedPerson = Element.<OtherChildrenNotInTheCase>builder()
                .value(other).build();
            otherPersonsInTheCase.add(wrappedPerson);
        }
        log.info("otherPersonsInTheCase : {}",otherPersonsInTheCase);
        log.info("getOtherChildNotInTheCaseTable()--->end");
        return otherPersonsInTheCase;
    }

}
