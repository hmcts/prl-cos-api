package uk.gov.hmcts.reform.prl.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.prl.enums.YesOrNo;
import uk.gov.hmcts.reform.prl.models.Address;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.ApplicantChild;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.GeneratedDocumentInfo;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static uk.gov.hmcts.reform.prl.utils.ResourceReader.readString;

@Service
@RequiredArgsConstructor
public class DraftAnOrderService {

    @Value("${document.templates.common.prl_solicitor_draft_an_order_template}")
    String solicitorDraftAnOrder;

    private final DgsService dgsService;

    private static final String NON_MOLESTATION_ORDER = "draftAnOrder/non-molestation-order.txt";

    public Document generateSolicitorDraftOrder(String authorisation, CaseData caseData) throws Exception {

        String draftOrderString = getTheOrderDraftString(caseData);
       if (draftOrderString != null) {
            caseData = caseData.toBuilder().previewDraftAnOrder(draftOrderString).build();
            GeneratedDocumentInfo generatedDocumentInfo = dgsService.generateDocument(
                authorisation,
                uk.gov.hmcts.reform.prl.models.dto.ccd.CaseDetails
                    .builder().caseData(caseData).build(),
                solicitorDraftAnOrder
            );
            Document document = Document.builder()
                .documentUrl(generatedDocumentInfo.getUrl())
                .documentBinaryUrl(generatedDocumentInfo.getBinaryUrl())
                .documentHash(generatedDocumentInfo.getHashToken())
                .documentFileName(caseData.getCreateSelectOrderOptions().getDisplayedValue()).build();
            return document;
        }
        return null;
    }

    public String getTheOrderDraftString(CaseData caseData) {
        String temp = null;
        switch (caseData.getCreateSelectOrderOptions()) {
            case nonMolestation:
                temp = getNonMolestationString(readString(NON_MOLESTATION_ORDER), caseData);
                break;

            default:
                break;

        }
        return temp;
    }

    private String getNonMolestationString(String nonMolestationOrderString, CaseData caseData) {
        Map<String, String> nonMolestationPlaceHoldersMap = new HashMap<>();
        if (nonMolestationOrderString != null) {
            nonMolestationPlaceHoldersMap.put(
                "orderDate",
                caseData.getDateOrderMade() != null ? caseData.getDateOrderMade().toString() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "judgeOrMagistrateTitle",
                caseData.getManageOrders().getJudgeOrMagistrateTitle().getDisplayedValue()
            );
            nonMolestationPlaceHoldersMap.put("judgeOrMagistratesLastName", caseData.getJudgeOrMagistratesLastName());
            nonMolestationPlaceHoldersMap.put("justiceLegalAdviserFullName", caseData.getJusticeLegalAdviserFullName());
            nonMolestationPlaceHoldersMap.put(
                "familyCourtName",
                caseData.getCourtName() != null ? caseData.getCourtName() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "fl401ApplicantName",
                caseData.getApplicantsFL401() != null
                    ? caseData.getApplicantsFL401().getFirstName() + caseData.getApplicantsFL401().getFirstName() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "fl404bApplicantReference",
                caseData.getApplicantsFL401() != null ? caseData.getApplicantsFL401().getSolicitorReference() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "fl404bRespondentName",
                caseData.getRespondentsFL401() != null
                    ? caseData.getRespondentsFL401().getFirstName() + caseData.getRespondentsFL401().getLastName() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "fl404bRespondentDob",
                (caseData.getRespondentsFL401() != null && caseData.getRespondentsFL401().getDateOfBirth() != null)
                    ? caseData.getRespondentsFL401().getDateOfBirth().toString() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "fl404bRespondentReference",
                (caseData.getRespondentsFL401() != null
                    && caseData.getRespondentsFL401().getSolicitorReference() != null)
                    ? caseData.getRespondentsFL401().getSolicitorReference() : " "
            );

            nonMolestationPlaceHoldersMap.put(
                "applicantChildNameDob", getApplicantChildDetails(caseData.getApplicantChildDetails())
            );
            nonMolestationPlaceHoldersMap.put(
                "fl404bRespondentAddress", getAddress(caseData.getRespondentsFL401().getAddress())
            );
            nonMolestationPlaceHoldersMap.put(
                "recitalsOrPreamble", caseData.getManageOrders().getRecitalsOrPreamble()
            );
            nonMolestationPlaceHoldersMap.put(
                "isTheOrderByConsent",
                (caseData.getManageOrders().getIsTheOrderByConsent() != null
                    && caseData.getManageOrders().getIsTheOrderByConsent().equals(
                    YesOrNo.Yes) ? "By consent" : " "
                )
            );
            nonMolestationPlaceHoldersMap.put(
                "furtherDirectionsIfRequired", caseData.getManageOrders().getFurtherDirectionsIfRequired()
            );
            nonMolestationPlaceHoldersMap.put(
                "dateOrderEnds",
                caseData.getManageOrders().getDateOrderEnds() != null
                    ? caseData.getManageOrders().getDateOrderEnds().toString() : " "
            );
            nonMolestationPlaceHoldersMap.put(
                "dateOrderEndTime",
                caseData.getManageOrders().getDateOrderEndsTime() != null
                    ? caseData.getManageOrders().getDateOrderEndsTime() : " "
            );

            StringSubstitutor substitutor = new StringSubstitutor(nonMolestationPlaceHoldersMap);
            return substitutor.replace(nonMolestationOrderString);
        }
        return null;
    }

    private String getAddress(Address address) {
        StringBuilder builder = new StringBuilder();
        if (address != null) {
            builder.append(address.getAddressLine1());
            builder.append(address.getPostTown() != null ? "\n" : "");
            builder.append(address.getPostTown());
            builder.append(address.getPostCode() != null ? "\n" : "");
            builder.append(address.getPostCode());
            builder.append("\n");
        }
        return builder.toString();
    }

    private String getApplicantChildDetails(List<Element<ApplicantChild>> applicantChildDetails) {
        Optional<List<Element<ApplicantChild>>> appChildDetails = ofNullable(applicantChildDetails);
        StringBuilder builder = new StringBuilder();
        if (appChildDetails.isPresent()) {
            List<ApplicantChild> children = appChildDetails.get().stream()
                .map(Element::getValue)
                .collect(Collectors.toList());
            for (int i = 0; i < children.size(); i++) {
                ApplicantChild child = children.get(i);
                builder.append(String.format(
                    "Child : %s  born %s",
                    child.getFullName(),
                    child.getDateOfBirth().toString()
                ));
                builder.append("\n");
            }
        }
        return builder.toString();
    }

}
