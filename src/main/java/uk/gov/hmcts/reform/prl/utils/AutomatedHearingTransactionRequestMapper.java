package uk.gov.hmcts.reform.prl.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.reform.prl.constants.PrlAppsConstants;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.CaseLinksElement;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caselink.AutomatedHearingCaseLink;
import uk.gov.hmcts.reform.prl.models.caselink.CaseLink;
import uk.gov.hmcts.reform.prl.models.complextypes.AutomatedHearingCaseManagementLocation;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingAttendHearing;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingCaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.AutomatedHearingPartyDetails;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.HearingData;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
public class AutomatedHearingTransactionRequestMapper {
    private AutomatedHearingTransactionRequestMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static AutomatedHearingCaseData mappingAutomatedHearingTransactionRequest(CaseData caseData, HearingData hearingData) {
        ObjectMapper objectMappers = new ObjectMapper();
        objectMappers.registerModule(new JavaTimeModule());
        try {
            List<Element<AutomatedHearingPartyDetails>> applicantsAutomatedHearingpartyDetails = new ArrayList<>();

            List<Element<AutomatedHearingPartyDetails>> respondentsAutomatedHearingpartyDetails = new ArrayList<>();
            if (PrlAppsConstants.C100_CASE_TYPE.equalsIgnoreCase(CaseUtils.getCaseTypeOfApplication(caseData))) {
                caseData.getApplicants().forEach(applicants -> getPartyDetailsForRequest(
                    applicants,
                    applicantsAutomatedHearingpartyDetails
                ));
                caseData.getRespondents().forEach(respondents -> getPartyDetailsForRequest(
                    respondents,
                    respondentsAutomatedHearingpartyDetails
                ));
            } else {
                getPartyDetailsForRequest(ElementUtils.element(caseData.getApplicantsFL401().getPartyId(),
                                                               caseData.getApplicantsFL401()), applicantsAutomatedHearingpartyDetails);
                getPartyDetailsForRequest(ElementUtils.element(caseData.getRespondentsFL401().getPartyId(),
                                                               caseData.getRespondentsFL401()), respondentsAutomatedHearingpartyDetails);
            }

            List<Element<AutomatedHearingPartyDetails>> automatedHearingOtherPartyInTheCaseRevised = new ArrayList<>();
            List<Element<PartyDetails>> otherPartyInTheCaseRevisedList = caseData.getOtherPartyInTheCaseRevised();
            if (otherPartyInTheCaseRevisedList != null) {
                otherPartyInTheCaseRevisedList.forEach(otherParty -> getPartyDetailsForRequest(
                    otherParty,
                    automatedHearingOtherPartyInTheCaseRevised
                ));
            }

            List<CaseLinksElement<AutomatedHearingCaseLink>> automatedHearingCaseLinks = new ArrayList<>();
            List<CaseLinksElement<CaseLink>> caseLinksList = caseData.getCaseLinks();
            if (caseLinksList != null) {
                caseLinksList.forEach(caseLink -> getCaseLinksDetails(caseLink, automatedHearingCaseLinks));
            }

            AutomatedHearingCaseData automatedHearingCaseData = AutomatedHearingCaseData.automatedHearingCaseDataBuilder()
                .id(caseData.getId())
                .hearingData(hearingData)
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
                .caseManagementLocation(isNull(caseData.getCaseManagementLocation())
                                            ? AutomatedHearingCaseManagementLocation.automatedHearingCaseManagementLocationWith().build() :
                                            AutomatedHearingCaseManagementLocation.automatedHearingCaseManagementLocationWith()
                                                .region(caseData.getCaseManagementLocation().getRegion())
                                                .baseLocation(caseData.getCaseManagementLocation().getBaseLocation())
                                                .build())
                .caseLinks(automatedHearingCaseLinks)
                .applicantCaseName(caseData.getApplicantCaseName())
                .allPartyFlags(caseData.getAllPartyFlags())
                .attendHearing(isNull(caseData.getAttendHearing()) ? AutomatedHearingAttendHearing.automatedHearingAttendHearingWith().build() :
                                   AutomatedHearingAttendHearing.automatedHearingAttendHearingWith()
                                       .isWelshNeeded(YesOrNo.Yes.equals(caseData.getAttendHearing().getIsWelshNeeded()))
                                       .build())
                .issueDate(caseData.getIssueDate())
                .build();
            String automatedHearingCaseDataJson = objectMappers.writerWithDefaultPrettyPrinter().writeValueAsString(
                automatedHearingCaseData);
            log.info("Automated Hearing Request Mapper: AutomatedHearingCaseData: {}", automatedHearingCaseDataJson);
            return automatedHearingCaseData;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void getPartyDetailsForRequest(Element<PartyDetails> party,
                                                  List<Element<AutomatedHearingPartyDetails>> applicantsAutomatedHearingpartyDetails) {
        PartyDetails partyDetails = party.getValue();
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
            .id(party.getId())
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
