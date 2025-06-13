package uk.gov.hmcts.reform.prl.utils.caseflags;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"java:S5976"})
public class PartyLevelCaseFlagsGeneratorTest {

    public static final String STRING_CONSTANT = "test";
    @InjectMocks
    private PartyLevelCaseFlagsGenerator partyLevelCaseFlagsGenerator;

    private CaseData caseData;
    private CaseDetails caseDetails;
    private Map<String, Object> caseDataMap;

    @BeforeEach
    void setup() {
        caseDataMap = new HashMap<>();
        caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .id(1234567891234567L)
            .state("SUBMITTED_PAID")
            .build();
        PartyDetails partyDetailsApplicant = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .build();
        PartyDetails partyDetailsRespondent = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .build();
        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .applicants(List.of(Element.<PartyDetails>builder().value(partyDetailsApplicant).build()))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                    "00000000-0000-0000-0000-000000000000"))
                                     .value(partyDetailsRespondent).build()))
            .build();
    }

    @Test
    void generatePartyFlagsForApplicantWithExternalFlag() {
        String partyName = STRING_CONSTANT;
        String caseDataField;
        String roleOnCase = STRING_CONSTANT;
        boolean internalFlag = false;
        String groupId = STRING_CONSTANT;
        List resultList = new ArrayList();
        for (int i = 1; i <= 5; i++) {
            caseDataField = "caApplicant" + i + "ExternalFlags";
            CaseData updatedCaseData = partyLevelCaseFlagsGenerator
                .generatePartyFlags(caseData, partyName, caseDataField, roleOnCase, internalFlag, groupId);
            resultList.add(updatedCaseData);
        }
        assertEquals(5, resultList.size());
    }

    @Test
    void generatePartyFlagsForApplicantSolicitorWithExternalFlag() {
        String partyName = STRING_CONSTANT;
        String caseDataField;
        String roleOnCase = STRING_CONSTANT;
        boolean internalFlag = false;
        String groupId = STRING_CONSTANT;
        List resultList = new ArrayList();
        for (int i = 1; i <= 5; i++) {
            caseDataField = "caApplicantSolicitor" + i + "ExternalFlags";
            CaseData updatedCaseData = partyLevelCaseFlagsGenerator
                .generatePartyFlags(caseData, partyName, caseDataField, roleOnCase, internalFlag, groupId);
            resultList.add(updatedCaseData);
        }

        assertEquals(5, resultList.size());
    }

    @Test
    void generatePartyFlagsForRespondentWithExternalFlag() {
        String partyName = STRING_CONSTANT;
        String caseDataField;
        String roleOnCase = STRING_CONSTANT;
        boolean internalFlag = false;
        String groupId = STRING_CONSTANT;
        List resultList = new ArrayList();
        for (int i = 1; i <= 5; i++) {
            caseDataField = "caRespondent" + i + "ExternalFlags";
            CaseData updatedCaseData = partyLevelCaseFlagsGenerator
                .generatePartyFlags(caseData, partyName, caseDataField, roleOnCase, internalFlag, groupId);
            resultList.add(updatedCaseData);
        }

        assertEquals(5, resultList.size());
    }

    @Test
    void generatePartyFlagsForRespondentSolicitorWithExternalFlag() {
        String partyName = STRING_CONSTANT;
        String caseDataField;
        String roleOnCase = STRING_CONSTANT;
        boolean internalFlag = false;
        String groupId = STRING_CONSTANT;
        List resultList = new ArrayList();
        for (int i = 1; i <= 5; i++) {
            caseDataField = "caRespondentSolicitor" + i + "ExternalFlags";
            CaseData updatedCaseData = partyLevelCaseFlagsGenerator
                .generatePartyFlags(caseData, partyName, caseDataField, roleOnCase, internalFlag, groupId);
            resultList.add(updatedCaseData);
        }

        assertEquals(5, resultList.size());
    }

    @Test
    void generatePartyFlagsForOtherPartyWithExternalFlag() {
        String partyName = STRING_CONSTANT;
        String caseDataField;
        String roleOnCase = STRING_CONSTANT;
        boolean internalFlag = false;
        String groupId = STRING_CONSTANT;
        List resultList = new ArrayList();
        for (int i = 1; i <= 5; i++) {
            caseDataField = "caOtherParty" + i + "ExternalFlags";
            CaseData updatedCaseData = partyLevelCaseFlagsGenerator
                .generatePartyFlags(caseData, partyName, caseDataField, roleOnCase, internalFlag, groupId);
            resultList.add(updatedCaseData);
        }

        assertEquals(5, resultList.size());
    }

    @Test
    void generatePartyFlagsForApplicantWithInternalFlag() {
        String partyName = STRING_CONSTANT;
        String caseDataField;
        String roleOnCase = STRING_CONSTANT;
        boolean internalFlag = true;
        String groupId = STRING_CONSTANT;
        List resultList = new ArrayList();
        for (int i = 1; i <= 5; i++) {
            caseDataField = "caApplicant" + i + "InternalFlags";
            CaseData updatedCaseData = partyLevelCaseFlagsGenerator
                .generatePartyFlags(caseData, partyName, caseDataField, roleOnCase, internalFlag, groupId);
            resultList.add(updatedCaseData);
        }

        assertEquals(5, resultList.size());
    }

    @Test
    void generatePartyFlagsForApplicantSolicitorWithInternalFlag() {
        String partyName = STRING_CONSTANT;
        String caseDataField;
        String roleOnCase = STRING_CONSTANT;
        boolean internalFlag = true;
        String groupId = STRING_CONSTANT;
        List resultList = new ArrayList();
        for (int i = 1; i <= 5; i++) {
            caseDataField = "caApplicantSolicitor" + i + "InternalFlags";
            CaseData updatedCaseData = partyLevelCaseFlagsGenerator
                .generatePartyFlags(caseData, partyName, caseDataField, roleOnCase, internalFlag, groupId);
            resultList.add(updatedCaseData);
        }

        assertEquals(5, resultList.size());
    }

    @Test
    void generatePartyFlagsForRespondentWithInternalFlag() {
        String partyName = STRING_CONSTANT;
        String caseDataField;
        String roleOnCase = STRING_CONSTANT;
        boolean internalFlag = true;
        String groupId = STRING_CONSTANT;
        List resultList = new ArrayList();
        for (int i = 1; i <= 5; i++) {
            caseDataField = "caRespondent" + i + "InternalFlags";
            CaseData updatedCaseData = partyLevelCaseFlagsGenerator
                .generatePartyFlags(caseData, partyName, caseDataField, roleOnCase, internalFlag, groupId);
            resultList.add(updatedCaseData);
        }

        assertEquals(5, resultList.size());
    }

    @Test
    void generatePartyFlagsForRespondentSolicitorWithInternalFlag() {
        String partyName = STRING_CONSTANT;
        String caseDataField;
        String roleOnCase = STRING_CONSTANT;
        boolean internalFlag = true;
        String groupId = STRING_CONSTANT;
        List resultList = new ArrayList();
        for (int i = 1; i <= 5; i++) {
            caseDataField = "caRespondentSolicitor" + i + "InternalFlags";
            CaseData updatedCaseData = partyLevelCaseFlagsGenerator
                .generatePartyFlags(caseData, partyName, caseDataField, roleOnCase, internalFlag, groupId);
            resultList.add(updatedCaseData);
        }

        assertEquals(5, resultList.size());
    }

    @Test
    void generatePartyFlagsForOtherPartyWithInternalFlag() {
        String partyName = STRING_CONSTANT;
        String caseDataField;
        String roleOnCase = STRING_CONSTANT;
        boolean internalFlag = true;
        String groupId = STRING_CONSTANT;
        List resultList = new ArrayList();
        for (int i = 1; i <= 5; i++) {
            caseDataField = "caOtherParty" + i + "InternalFlags";
            CaseData updatedCaseData = partyLevelCaseFlagsGenerator
                .generatePartyFlags(caseData, partyName, caseDataField, roleOnCase, internalFlag, groupId);
            resultList.add(updatedCaseData);
        }

        assertEquals(5, resultList.size());
    }
}
