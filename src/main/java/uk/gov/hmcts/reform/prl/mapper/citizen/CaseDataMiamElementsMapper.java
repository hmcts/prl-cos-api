package uk.gov.hmcts.reform.prl.mapper.citizen;

import com.google.common.base.Strings;
import uk.gov.hmcts.reform.prl.enums.MiamChildProtectionConcernChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamDomesticViolenceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamExemptionsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamOtherGroundsChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamPreviousAttendanceChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.MiamUrgencyReasonChecklistEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.ChildProtectionMapperEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.DomesticAbuseMapperEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.MiamExemptionMapperEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.MiamNotAttendingReasonsMapperEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.MiamPreviousAttendanceMapperEnum;
import uk.gov.hmcts.reform.prl.enums.citizen.MiamUrgencyMapperEnum;
import uk.gov.hmcts.reform.prl.models.c100rebuild.C100RebuildMiamElements;
import uk.gov.hmcts.reform.prl.models.documents.Document;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.ccd.MiamDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static uk.gov.hmcts.reform.prl.enums.citizen.DomesticAbuseMapperEnum.ILRDuetoDomesticAbuse;
import static uk.gov.hmcts.reform.prl.enums.citizen.DomesticAbuseMapperEnum.financiallyAbuse;

public class CaseDataMiamElementsMapper {

    private CaseDataMiamElementsMapper() {
    }

    public static void updateMiamElementsForCaseData(CaseData.CaseDataBuilder<?,?> caseDataBuilder,
                                                     C100RebuildMiamElements c100RebuildMiamElements) {

        List<MiamPreviousAttendanceChecklistEnum> previousAttendanceChecklistEnums =
                buildMiamPreviousAttendanceChecklist(c100RebuildMiamElements);

        List<MiamOtherGroundsChecklistEnum> otherGroundsChecklistEnums =
                buildMiamOtherGroundsChecklist(c100RebuildMiamElements);

        caseDataBuilder
            .miamDetails(MiamDetails.builder()
                             .applicantAttendedMiam(c100RebuildMiamElements.getMiamAttendance())
                             .familyMediatorMiam(c100RebuildMiamElements.getMiamAttendance())
                             .applicantConsentMiam(c100RebuildMiamElements.getMiamConsent())
                             .otherProceedingsMiam(c100RebuildMiamElements.getMiamOtherProceedings())
                             .claimingExemptionMiam(c100RebuildMiamElements.getMiamValidReason())
                             .miamExemptionsChecklist(buildMiamExemptionsCheckList(c100RebuildMiamElements))

                             .miamDomesticViolenceChecklist(buildMiamDomesticViolenceChecklist(c100RebuildMiamElements))
                             .miamChildProtectionConcernList(buildMiamChildProtectionConcernList(c100RebuildMiamElements))
                             .miamUrgencyReasonChecklist(buildMiamUrgencyReasonChecklist(c100RebuildMiamElements))
                             .miamPreviousAttendanceChecklist(isNotEmpty(previousAttendanceChecklistEnums)
                                                                  ? previousAttendanceChecklistEnums.get(0) : null)
                             .miamOtherGroundsChecklist(isNotEmpty(otherGroundsChecklistEnums)
                                                            ? otherGroundsChecklistEnums.get(0) : null)
                             .miamPreviousAttendanceChecklist1(previousAttendanceChecklistEnums)
                             .miamOtherGroundsChecklist1(otherGroundsChecklistEnums)
                             .miamCertificationDocumentUpload(buildDocument(c100RebuildMiamElements.getMiamCertificate()))
                             .build());
    }

    private static List<MiamOtherGroundsChecklistEnum> buildMiamOtherGroundsChecklist(C100RebuildMiamElements c100RebuildMiamElements) {
        List<String> miamNotAttendingReasons = new java.util.ArrayList<>(nonNull(c100RebuildMiamElements.getMiamNotAttendingReasons())
                ? new java.util.ArrayList<>(List.of(c100RebuildMiamElements.getMiamNotAttendingReasons())) : Collections.emptyList());


        miamNotAttendingReasons.remove(MiamNotAttendingReasonsMapperEnum.under18.name());

        if (miamNotAttendingReasons.isEmpty() || miamNotAttendingReasons.contains("none")) {
            return Collections.emptyList();
        }

        List<String> noMediatorAccessSubfields = nonNull(c100RebuildMiamElements.getMiamNoMediatorAccessSubfields())
                ? List.of(c100RebuildMiamElements.getMiamNoMediatorAccessSubfields()) : Collections.emptyList();
        miamNotAttendingReasons.remove("canNotAccessMediator");

        List<String> mediatorList = Stream.of(miamNotAttendingReasons, noMediatorAccessSubfields)
                .flatMap(Collection::stream).toList();

        return mediatorList.stream().map(value -> MiamNotAttendingReasonsMapperEnum.valueOf(value).getValue())
                .toList();
    }

    private static List<MiamPreviousAttendanceChecklistEnum> buildMiamPreviousAttendanceChecklist(C100RebuildMiamElements c100RebuildMiamElements) {
        List<String> previousAttendance = nonNull(c100RebuildMiamElements.getMiamPreviousAttendance())
                ? List.of(c100RebuildMiamElements.getMiamPreviousAttendance()) : Collections.emptyList();

        if (previousAttendance.isEmpty() || previousAttendance.contains("none")) {
            return Collections.emptyList();
        }
        return previousAttendance.stream().map(value -> MiamPreviousAttendanceMapperEnum.valueOf(value).getValue())
                .toList().stream().distinct().toList();

    }

    private static List<MiamUrgencyReasonChecklistEnum> buildMiamUrgencyReasonChecklist(C100RebuildMiamElements c100RebuildMiamElements) {
        List<String> miamUrgencies = nonNull(c100RebuildMiamElements.getMiamUrgency())
                ? List.of(c100RebuildMiamElements.getMiamUrgency()) : Collections.emptyList();

        if (miamUrgencies.isEmpty() || miamUrgencies.contains("none")) {
            return Collections.emptyList();
        }
        return miamUrgencies.stream().map(value -> MiamUrgencyMapperEnum.valueOf(value).getValue())
                .toList().stream().distinct().toList();
    }

    private static List<MiamChildProtectionConcernChecklistEnum>
        buildMiamChildProtectionConcernList(C100RebuildMiamElements c100RebuildMiamElements) {
        List<String> childProtectionEvidences = nonNull(c100RebuildMiamElements.getMiamChildProtectionEvidence())
                ? List.of(c100RebuildMiamElements.getMiamChildProtectionEvidence()) : Collections.emptyList();

        if (childProtectionEvidences.isEmpty() || childProtectionEvidences.contains("none")) {
            return Collections.emptyList();
        }
        return childProtectionEvidences.stream().map(value -> ChildProtectionMapperEnum.valueOf(value).getValue())
                .toList();
    }

    private static List<MiamDomesticViolenceChecklistEnum> buildMiamDomesticViolenceChecklist(C100RebuildMiamElements
                                                                                                      c100RebuildMiamElements) {
        List<String> domesticAbuses = new java.util.ArrayList<>(nonNull(c100RebuildMiamElements.getMiamDomesticAbuse())
                ? List.of(c100RebuildMiamElements.getMiamDomesticAbuse()) : Collections.emptyList());

        if (domesticAbuses.isEmpty() || domesticAbuses.contains("none")) {
            return Collections.emptyList();
        }

        String[] domesticAbuse1 = c100RebuildMiamElements.getMiamDomesticAbuseInvolvementSubfields();
        String[] domesticAbuse2 = c100RebuildMiamElements.getMiamDomesticAbuseCourtInvolvementSubfields();
        String[] domesticAbuse3 = c100RebuildMiamElements.getMiamDomesticAbuseLetterOfBeingVictimSubfields();
        String[] domesticAbuse4 = c100RebuildMiamElements.getMiamDomesticAbuseLetterFromAuthoritySubfields();
        String[] domesticAbuse5 = c100RebuildMiamElements.getMiamDomesticAbuseLetterFromSupportServiceSubfields();

        List<String> domesticAbuse1List = nonNull(domesticAbuse1) ? List.of(domesticAbuse1) : Collections.emptyList();
        List<String> domesticAbuse2List = nonNull(domesticAbuse2) ? List.of(domesticAbuse2) : Collections.emptyList();
        List<String> domesticAbuse3List = nonNull(domesticAbuse3) ? List.of(domesticAbuse3) : Collections.emptyList();
        List<String> domesticAbuse4List = nonNull(domesticAbuse4) ? List.of(domesticAbuse4) : Collections.emptyList();
        List<String> domesticAbuse5List = nonNull(domesticAbuse5) ? List.of(domesticAbuse5) : Collections.emptyList();

        List<String> domesticAbusesList = Stream.of(domesticAbuse1List, domesticAbuse2List, domesticAbuse3List,
                        domesticAbuse4List, domesticAbuse5List)
                .flatMap(Collection::stream).collect(Collectors.toList());

        domesticAbusesList.removeIf(Strings::isNullOrEmpty);

        List<String> domesticAbuseHeads = List.of(c100RebuildMiamElements.getMiamDomesticAbuse());

        if (domesticAbuseHeads.contains(ILRDuetoDomesticAbuse.toString())) {
            domesticAbusesList.add(ILRDuetoDomesticAbuse.toString());
        } else if (domesticAbuseHeads.contains(financiallyAbuse.toString())) {
            domesticAbusesList.add(financiallyAbuse.toString());
        }

        return domesticAbusesList.stream().map(value -> DomesticAbuseMapperEnum.valueOf(value).getValue())
                .toList();
    }

    private static List<MiamExemptionsChecklistEnum> buildMiamExemptionsCheckList(C100RebuildMiamElements c100RebuildMiamElements) {
        List<String> nonAttendanceReasons = nonNull(c100RebuildMiamElements.getMiamNonAttendanceReasons())
                ? List.of(c100RebuildMiamElements.getMiamNonAttendanceReasons()) : Collections.emptyList();

        if (nonAttendanceReasons.isEmpty() || nonAttendanceReasons.contains("none")) {
            return Collections.emptyList();
        }
        return nonAttendanceReasons.stream().map(value -> MiamExemptionMapperEnum.valueOf(value).getValue())
                .toList();
    }

    private static Document buildDocument(uk.gov.hmcts.reform.prl.models.c100rebuild.Document maimDocument) {
        if (isNotEmpty(maimDocument)) {
            return Document.builder()
                    .documentUrl(maimDocument.getUrl())
                    .documentBinaryUrl(maimDocument.getBinaryUrl())
                    .documentFileName(maimDocument.getFilename())
                    .build();
        }
        return null;
    }
}
