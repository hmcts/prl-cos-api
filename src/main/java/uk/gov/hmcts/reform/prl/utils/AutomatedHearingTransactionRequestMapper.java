package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.CaseLinksElement;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caselink.AutomatedHearingCaseLink;
import uk.gov.hmcts.reform.prl.models.caselink.CaseLink;
import uk.gov.hmcts.reform.prl.models.complextypes.AutomatedHearingCaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingAttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingManageOrders;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingPartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.isNull;

@Slf4j
public class AutomatedHearingTransactionRequestMapper {
    private AutomatedHearingTransactionRequestMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static AutomatedHearingCaseData mappingAutomatedHearingTransactionRequest(CaseData caseData, UUID id) {
        AutomatedHearingCaseData automatedHearingCaseData = AutomatedHearingCaseData.automatedHearingCaseDataBuilder().build();
        ObjectMapper objectMappers = new ObjectMapper();
        objectMappers.registerModule(new JavaTimeModule());
        try {
            List<Element<AutomatedHearingPartyDetails>> applicantsAutomatedHearingpartyDetails = new ArrayList<>();
            List<Element<PartyDetails>> applicantList = caseData.getApplicants();
            if(applicantList != null) {
                applicantList.forEach(applicants -> getApplicantsDetails(
                    applicants,
                    applicantsAutomatedHearingpartyDetails
                ));
            }

            List<Element<AutomatedHearingPartyDetails>> respondentsAutomatedHearingpartyDetails = new ArrayList<>();
            List<Element<PartyDetails>> respondentsList = caseData.getRespondents();
            if(respondentsList != null) {
                respondentsList.forEach(respondents -> getApplicantsDetails(
                    respondents,
                    respondentsAutomatedHearingpartyDetails
                ));
            }

            List<Element<AutomatedHearingPartyDetails>> automatedHearingOtherPartyInTheCaseRevised = new ArrayList<>();
            List<Element<PartyDetails>> otherPartyInTheCaseRevisedList = caseData.getOtherPartyInTheCaseRevised();
            if(otherPartyInTheCaseRevisedList != null) {
                otherPartyInTheCaseRevisedList.forEach(otherParty -> getApplicantsDetails(
                    otherParty,
                    automatedHearingOtherPartyInTheCaseRevised
                ));
            }

            List<CaseLinksElement<AutomatedHearingCaseLink>> automatedHearingCaseLinks = new ArrayList<>();
            List<CaseLinksElement<CaseLink>> caseLinksList = caseData.getCaseLinks();
            if(caseLinksList != null) {
                caseLinksList.forEach(caseLink -> getCaseLinksDetails(caseLink, automatedHearingCaseLinks));
            }

            automatedHearingCaseData = AutomatedHearingCaseData.automatedHearingCaseDataBuilder()
                .orderId(id)
                .id(caseData.getId())
                .taskListVersion(caseData.getTaskListVersion())
                .createdDate(caseData.getCreatedDate())
                .familymanCaseNumber(caseData.getFamilymanCaseNumber())
                .dateSubmitted(caseData.getDateSubmitted())
                .caseTypeOfApplication(caseData.getCaseTypeOfApplication())
                .applicants(applicantsAutomatedHearingpartyDetails)
                .respondents(respondentsAutomatedHearingpartyDetails)
                .otherPartyInTheCaseRevised(automatedHearingOtherPartyInTheCaseRevised)
                .applicantSolicitorEmailAddress(caseData.getApplicantSolicitorEmailAddress())
                .solicitorName(caseData.getSolicitorName())
                .courtName(caseData.getCourtName())
                .applicantsFL401(getApplicantsDetails(caseData))
                .respondentsFL401(getRespondentsDetails(caseData))
                .caseManagementLocation(isNull(caseData.getCaseManagementLocation())?
                                                   AutomatedHearingCaseManagementLocation.automatedHearingCaseManagementLocationWith().build():
                                                   AutomatedHearingCaseManagementLocation.automatedHearingCaseManagementLocationWith()
                .region(caseData.getCaseManagementLocation().getRegion())
                .baseLocation(caseData.getCaseManagementLocation().getBaseLocation())
                .build())
                .caseLinks(automatedHearingCaseLinks)
                .applicantCaseName(caseData.getApplicantCaseName())
                .allPartyFlags(caseData.getAllPartyFlags())
                .manageOrders(isNull(caseData.getManageOrders())?AutomatedHearingManageOrders.automatedHearingManageOrdersWith().build():
                                  AutomatedHearingManageOrders.automatedHearingManageOrdersWith()
                                  .ordersHearingDetails(caseData.getManageOrders().getOrdersHearingDetails())
                                  .build())
                .attendHearing(isNull(caseData.getAttendHearing())?AutomatedHearingAttendHearing.automatedHearingAttendHearingWith().build():
                               AutomatedHearingAttendHearing.automatedHearingAttendHearingWith()
                                   .isWelshNeeded(YesOrNo.Yes.equals(caseData.getAttendHearing().getIsWelshNeeded()))
                                   .build())
                .issueDate(caseData.getIssueDate())
                .build();
            String automatedHearingCaseDataJson = objectMappers.writerWithDefaultPrettyPrinter().writeValueAsString(
                automatedHearingCaseData);
            log.info("Automated Hearing Request Mapper: AutomatedHearingCaseData: {}", automatedHearingCaseDataJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return automatedHearingCaseData;
    }

    private static AutomatedHearingPartyDetails getRespondentsDetails(CaseData caseData) {
        if(caseData.getRespondentsFL401() != null) {
            return AutomatedHearingPartyDetails.automatedHearingPartyDetailsWith()
                .firstName(caseData.getRespondentsFL401().getFirstName())
                .lastName(caseData.getRespondentsFL401().getLastName())
                .previousName(caseData.getRespondentsFL401().getPreviousName())
                .dateOfBirth(caseData.getRespondentsFL401().getDateOfBirth())
                .isDateOfBirthUnknown(caseData.getRespondentsFL401().getIsDateOfBirthUnknown())
                .otherGender(caseData.getRespondentsFL401().getOtherGender())
                .placeOfBirth(caseData.getRespondentsFL401().getPlaceOfBirth())
                .isAddressUnknown(caseData.getRespondentsFL401().getIsAddressUnknown())
                .addressLivedLessThan5YearsDetails(caseData.getRespondentsFL401().getAddressLivedLessThan5YearsDetails())
                .landline(caseData.getRespondentsFL401().getLandline())
                .relationshipToChildren(caseData.getRespondentsFL401().getRelationshipToChildren())
                .otherPersonRelationshipToChildren(caseData.getRespondentsFL401().getOtherPersonRelationshipToChildren())
                .solicitorOrg(caseData.getRespondentsFL401().getSolicitorOrg())
                .solicitorAddress(caseData.getRespondentsFL401().getSolicitorAddress())
                .dxNumber(caseData.getRespondentsFL401().getDxNumber())
                .solicitorReference(caseData.getRespondentsFL401().getSolicitorReference())
                .representativeFirstName(caseData.getRespondentsFL401().getRepresentativeFirstName())
                .representativeLastName(caseData.getRespondentsFL401().getRepresentativeLastName())
                .sendSignUpLink(caseData.getRespondentsFL401().getSendSignUpLink())
                .solicitorEmail(caseData.getRespondentsFL401().getSolicitorEmail())
                .phoneNumber(caseData.getRespondentsFL401().getPhoneNumber())
                .email(caseData.getRespondentsFL401().getEmail())
                .address(caseData.getRespondentsFL401().getAddress())
                .solicitorTelephone(caseData.getRespondentsFL401().getSolicitorTelephone())
                .caseTypeOfApplication(caseData.getRespondentsFL401().getCaseTypeOfApplication())
                .partyLevelFlag(caseData.getRespondentsFL401().getPartyLevelFlag())
                .partyId(caseData.getRespondentsFL401().getPartyId())
                .solicitorOrgUuid(caseData.getRespondentsFL401().getSolicitorOrgUuid())
                .solicitorPartyId(caseData.getRespondentsFL401().getSolicitorPartyId())
                .build();
        } else {
            return AutomatedHearingPartyDetails.automatedHearingPartyDetailsWith().build();
        }
    }

    private static AutomatedHearingPartyDetails getApplicantsDetails(CaseData caseData) {
        if(caseData.getApplicantsFL401() != null) {
            return AutomatedHearingPartyDetails.automatedHearingPartyDetailsWith()
                .firstName(caseData.getApplicantsFL401().getFirstName())
                .lastName(caseData.getApplicantsFL401().getLastName())
                .previousName(caseData.getApplicantsFL401().getPreviousName())
                .dateOfBirth(caseData.getApplicantsFL401().getDateOfBirth())
                .isDateOfBirthUnknown(caseData.getApplicantsFL401().getIsDateOfBirthUnknown())
                .otherGender(caseData.getApplicantsFL401().getOtherGender())
                .placeOfBirth(caseData.getApplicantsFL401().getPlaceOfBirth())
                .isAddressUnknown(caseData.getApplicantsFL401().getIsAddressUnknown())
                .addressLivedLessThan5YearsDetails(caseData.getApplicantsFL401().getAddressLivedLessThan5YearsDetails())
                .landline(caseData.getApplicantsFL401().getLandline())
                .relationshipToChildren(caseData.getApplicantsFL401().getRelationshipToChildren())
                .otherPersonRelationshipToChildren(caseData.getApplicantsFL401().getOtherPersonRelationshipToChildren())
                .solicitorOrg(caseData.getApplicantsFL401().getSolicitorOrg())
                .solicitorAddress(caseData.getApplicantsFL401().getSolicitorAddress())
                .dxNumber(caseData.getApplicantsFL401().getDxNumber())
                .solicitorReference(caseData.getApplicantsFL401().getSolicitorReference())
                .representativeFirstName(caseData.getApplicantsFL401().getRepresentativeFirstName())
                .representativeLastName(caseData.getApplicantsFL401().getRepresentativeLastName())
                .sendSignUpLink(caseData.getApplicantsFL401().getSendSignUpLink())
                .solicitorEmail(caseData.getApplicantsFL401().getSolicitorEmail())
                .phoneNumber(caseData.getApplicantsFL401().getPhoneNumber())
                .email(caseData.getApplicantsFL401().getEmail())
                .address(caseData.getApplicantsFL401().getAddress())
                .solicitorTelephone(caseData.getApplicantsFL401().getSolicitorTelephone())
                .caseTypeOfApplication(caseData.getApplicantsFL401().getCaseTypeOfApplication())
                .partyLevelFlag(caseData.getApplicantsFL401().getPartyLevelFlag())
                .partyId(caseData.getApplicantsFL401().getPartyId())
                .solicitorOrgUuid(caseData.getApplicantsFL401().getSolicitorOrgUuid())
                .solicitorPartyId(caseData.getApplicantsFL401().getSolicitorPartyId())
                .build();
        } else {
            return AutomatedHearingPartyDetails.automatedHearingPartyDetailsWith().build();
        }
    }

    private static void getApplicantsDetails(Element<PartyDetails> applicants,
                                             List<Element<AutomatedHearingPartyDetails>> applicantsAutomatedHearingpartyDetails) {
        UUID ids = applicants.getId();
        PartyDetails partyDetails = applicants.getValue();
        AutomatedHearingPartyDetails automatedHearingPartyDetails = AutomatedHearingPartyDetails
            .automatedHearingPartyDetailsWith().build();
        if (partyDetails != null) {
            automatedHearingPartyDetails = AutomatedHearingPartyDetails
                .automatedHearingPartyDetailsWith()
                .firstName(partyDetails.getFirstName())
                .lastName(partyDetails.getLastName())
                .previousName(partyDetails.getPreviousName())
                .dateOfBirth(partyDetails.getDateOfBirth())
                .isDateOfBirthUnknown(partyDetails.getIsDateOfBirthUnknown())
                .otherGender(partyDetails.getOtherGender())
                .placeOfBirth(partyDetails.getPlaceOfBirth())
                .isAddressUnknown(partyDetails.getIsAddressUnknown())
                .addressLivedLessThan5YearsDetails(partyDetails.getAddressLivedLessThan5YearsDetails())
                .landline(partyDetails.getLandline())
                .relationshipToChildren(partyDetails.getRelationshipToChildren())
                .otherPersonRelationshipToChildren(partyDetails.getOtherPersonRelationshipToChildren())
                .solicitorOrg(partyDetails.getSolicitorOrg())
                .solicitorAddress(partyDetails.getSolicitorAddress())
                .dxNumber(partyDetails.getDxNumber())
                .solicitorReference(partyDetails.getSolicitorReference())
                .representativeFirstName(partyDetails.getRepresentativeFirstName())
                .representativeLastName(partyDetails.getRepresentativeLastName())
                .sendSignUpLink(partyDetails.getSendSignUpLink())
                .solicitorEmail(partyDetails.getSolicitorEmail())
                .phoneNumber(partyDetails.getPhoneNumber())
                .email(partyDetails.getEmail())
                .address(partyDetails.getAddress())
                .solicitorTelephone(partyDetails.getSolicitorTelephone())
                .caseTypeOfApplication(partyDetails.getCaseTypeOfApplication())
                .partyLevelFlag(partyDetails.getPartyLevelFlag())
                .partyId(partyDetails.getPartyId())
                .solicitorOrgUuid(partyDetails.getSolicitorOrgUuid())
                .solicitorPartyId(partyDetails.getSolicitorPartyId())
                .build();
        }

        Element<AutomatedHearingPartyDetails> autoElement = Element.<AutomatedHearingPartyDetails>builder()
            .id(ids)
            .value(automatedHearingPartyDetails)
            .build();
        applicantsAutomatedHearingpartyDetails.add(autoElement);
    }

    private static void getCaseLinksDetails(CaseLinksElement<CaseLink> caseLink,
                                            List<CaseLinksElement<AutomatedHearingCaseLink>> automatedHearingCaseLinksDetails) {
        String ids = caseLink.getId();
        CaseLink partyDetails = caseLink.getValue();
        AutomatedHearingCaseLink automatedHearingCaseLinkDetails = AutomatedHearingCaseLink
            .automatedHearingCaseLinkWith()
            .caseType(partyDetails.getCaseType())
            .build();

        CaseLinksElement<AutomatedHearingCaseLink> autoElement = CaseLinksElement.<AutomatedHearingCaseLink>builder()
            .id(ids)
            .value(automatedHearingCaseLinkDetails)
            .build();
        automatedHearingCaseLinksDetails.add(autoElement);
    }
}
