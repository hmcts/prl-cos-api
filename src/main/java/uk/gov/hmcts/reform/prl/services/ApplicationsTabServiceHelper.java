package uk.gov.hmcts.reform.prl.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    private final ObjectMapper objectMapper;

    public List<Element<OtherPersonInTheCaseRevised>> getOtherPeopleInTheCaseRevisedTable(CaseData caseData) {
        Optional<List<Element<PartyDetails>>> otherPeopleCheck = ofNullable(caseData.getOtherPartyInTheCaseRevised());
        List<Element<OtherPersonInTheCaseRevised>> otherPersonsInTheCase = new ArrayList<>();

        if (otherPeopleCheck.isEmpty() || otherPeopleCheck.get().isEmpty()) {
            OtherPersonInTheCaseRevised op = OtherPersonInTheCaseRevised.builder().build();
            Element<OtherPersonInTheCaseRevised> other = Element.<OtherPersonInTheCaseRevised>builder().value(op).build();
            otherPersonsInTheCase.add(other);
            return otherPersonsInTheCase;
        }
        List<PartyDetails> otherPeople = caseData.getOtherPartyInTheCaseRevised().stream().map(Element::getValue).toList();
        otherPeople = maskConfidentialDetails(otherPeople);
        for (PartyDetails currentOtherPerson : otherPeople) {
            OtherPersonInTheCaseRevised otherPerson = objectMapper.convertValue(currentOtherPerson, OtherPersonInTheCaseRevised.class);

            Element<OtherPersonInTheCaseRevised> wrappedPerson = Element.<OtherPersonInTheCaseRevised>builder()
                .value(otherPerson.toBuilder()
                           .gender(otherPerson.getGender() != null
                                       ? Gender.getDisplayedValueFromEnumString(otherPerson.getGender()).getDisplayedValue() : null)
                           .build()).build();
            otherPersonsInTheCase.add(wrappedPerson);
        }
        return otherPersonsInTheCase;
    }




    public List<Element<ChildDetailsRevised>> getChildRevisedDetails(CaseData caseData) {
        Optional<List<Element<uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised>>> childElementsCheck =
            ofNullable(caseData.getNewChildDetails());
        List<Element<ChildDetailsRevised>> childFinalList = new ArrayList<>();
        if (childElementsCheck.isEmpty() || childElementsCheck.get().isEmpty()) {
            ChildDetailsRevised child = ChildDetailsRevised.builder().build();
            Element<ChildDetailsRevised> app = Element.<ChildDetailsRevised>builder().value(child).build();
            childFinalList.add(app);
            return childFinalList;
        }
        List<uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised> childList =
            caseData.getNewChildDetails().stream()
            .map(Element::getValue)
            .toList();
        for (uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised child : childList) {
            ChildDetailsRevised c = getChildDetailsRevised(child);
            Element<ChildDetailsRevised> res = Element.<ChildDetailsRevised>builder().value(c).build();
            childFinalList.add(res);
        }
        return childFinalList;
    }


    protected ChildDetailsRevised getChildDetailsRevised(uk.gov.hmcts.reform.prl.models.complextypes.ChildDetailsRevised child) {
        Optional<List<OrderTypeEnum>> orderAppliedFor = ofNullable(child.getOrderAppliedFor());
        Optional<Gender> childGender = ofNullable(child.getGender());
        return ChildDetailsRevised.builder().firstName(child.getFirstName())
            .lastName(child.getLastName())
            .dateOfBirth(child.getDateOfBirth())
            .gender(child.getGender())
            .otherGender(childGender.isPresent() && childGender.get().equals(other) ? child.getOtherGender() : "")
            .orderAppliedFor(orderAppliedFor.isEmpty() ? null : child.getOrderAppliedFor().stream()
                .map(OrderTypeEnum::getDisplayedValue).collect(
                    Collectors.joining(", ")))
            .parentalResponsibilityDetails(child.getParentalResponsibilityDetails())
                .cafcassOfficerAdded(!StringUtils.isBlank(child.getCafcassOfficerName()) ? YesOrNo.Yes : YesOrNo.No)
                .cafcassOfficerName(child.getCafcassOfficerName())
                .cafcassOfficerEmailAddress(child.getCafcassOfficerEmailAddress())
                .cafcassOfficerPhoneNo(child.getCafcassOfficerPhoneNo())
            .build();
    }

    public List<Element<ChildAndApplicantRelation>> getChildAndApplicantsRelationTable(CaseData caseData) {
        log.info("getChildAndApplicantsRelationTable()--->start");
        List<Element<ChildAndApplicantRelation>> applicants = new ArrayList<>();
        Optional<List<Element<ChildrenAndApplicantRelation>>> checkApplicants = ofNullable(caseData.getRelations().getChildAndApplicantRelations());

        if (checkApplicants.isEmpty() || checkApplicants.get().isEmpty()) {
            ChildAndApplicantRelation a = ChildAndApplicantRelation.builder().build();
            Element<ChildAndApplicantRelation> app = Element.<ChildAndApplicantRelation>builder().value(a).build();
            applicants.add(app);
            return applicants;
        }
        List<ChildrenAndApplicantRelation> currentApplicants = caseData.getRelations().getChildAndApplicantRelations().stream()
            .map(Element::getValue)
            .toList();

        for (ChildrenAndApplicantRelation applicant : currentApplicants) {
            ChildAndApplicantRelation a = objectMapper.convertValue(applicant, ChildAndApplicantRelation.class);
            Element<ChildAndApplicantRelation> app = Element.<ChildAndApplicantRelation>builder().value(a).build();
            applicants.add(app);
        }
        log.info("getChildAndApplicantsRelationTable()--->end");
        return applicants;
    }


    public List<Element<ChildAndRespondentRelation>> getChildAndRespondentRelationsTable(CaseData caseData) {
        log.info("getChildAndRespondentRelationsTable()--->start");
        List<Element<ChildAndRespondentRelation>> respondents = new ArrayList<>();
        Optional<List<Element<ChildrenAndRespondentRelation>>> checkRespondents = ofNullable(caseData.getRelations()
                .getChildAndRespondentRelations());

        if (checkRespondents.isEmpty() || checkRespondents.get().isEmpty()) {
            ChildAndRespondentRelation a = ChildAndRespondentRelation.builder().build();
            Element<ChildAndRespondentRelation> app = Element.<ChildAndRespondentRelation>builder().value(a).build();
            respondents.add(app);
            return respondents;
        }
        List<ChildrenAndRespondentRelation> currentApplicants = caseData.getRelations().getChildAndRespondentRelations().stream()
            .map(Element::getValue)
            .toList();
        for (ChildrenAndRespondentRelation respondent : currentApplicants) {
            ChildAndRespondentRelation a = objectMapper.convertValue(respondent, ChildAndRespondentRelation.class);
            Element<ChildAndRespondentRelation> app = Element.<ChildAndRespondentRelation>builder().value(a).build();
            respondents.add(app);
        }
        log.info("-->getChildAndRespondentRelationsTable()--->End");
        return respondents;
    }


    public List<Element<ChildAndOtherPeopleRelation>> getChildAndOtherPeopleRelationsTable(CaseData caseData) {
        log.info("-->getChildAndOtherPeopleRelationsTable()--->Start");
        List<Element<ChildAndOtherPeopleRelation>> otherPeopleRelations = new ArrayList<>();
        Optional<List<Element<ChildrenAndOtherPeopleRelation>>> checkRespondents = ofNullable(caseData
                .getRelations().getChildAndOtherPeopleRelations());

        if (checkRespondents.isEmpty() || checkRespondents.get().isEmpty()) {
            ChildAndOtherPeopleRelation a = ChildAndOtherPeopleRelation.builder().build();
            Element<ChildAndOtherPeopleRelation> app = Element.<ChildAndOtherPeopleRelation>builder().value(a).build();
            otherPeopleRelations.add(app);
            return otherPeopleRelations;
        }
        List<ChildrenAndOtherPeopleRelation> currentApplicants = caseData.getRelations().getChildAndOtherPeopleRelations().stream()
            .map(Element::getValue)
            .toList();

        for (ChildrenAndOtherPeopleRelation otherPeople : currentApplicants) {
            ChildAndOtherPeopleRelation a = objectMapper.convertValue(otherPeople, ChildAndOtherPeopleRelation.class);
            Element<ChildAndOtherPeopleRelation> app = Element.<ChildAndOtherPeopleRelation>builder().value(a).build();
            otherPeopleRelations.add(app);
        }
        log.info("-->getChildAndOtherPeopleRelationsTable()--->End");
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
            caseData.getChildrenNotInTheCase().stream().map(Element::getValue).toList();

        for (uk.gov.hmcts.reform.prl.models.complextypes.OtherChildrenNotInTheCase p : otherPeople) {
            OtherChildrenNotInTheCase other = objectMapper.convertValue(p, OtherChildrenNotInTheCase.class);
            Element<OtherChildrenNotInTheCase> wrappedPerson = Element.<OtherChildrenNotInTheCase>builder()
                .value(other).build();
            otherPersonsInTheCase.add(wrappedPerson);
        }
        log.info("getOtherChildNotInTheCaseTable()--->end");
        return otherPersonsInTheCase;
    }

}
