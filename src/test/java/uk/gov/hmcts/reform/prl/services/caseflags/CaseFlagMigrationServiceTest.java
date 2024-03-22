package uk.gov.hmcts.reform.prl.services.caseflags;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.prl.models.Element;
import uk.gov.hmcts.reform.prl.models.caseflags.AllPartyFlags;
import uk.gov.hmcts.reform.prl.models.caseflags.Flags;
import uk.gov.hmcts.reform.prl.models.complextypes.PartyDetails;
import uk.gov.hmcts.reform.prl.models.complextypes.citizen.User;
import uk.gov.hmcts.reform.prl.models.dto.ccd.CaseData;
import uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.CaseFlag;
import uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.Flag;
import uk.gov.hmcts.reform.prl.models.dto.datamigration.caseflag.FlagDetail;
import uk.gov.hmcts.reform.prl.services.RefDataUserService;
import uk.gov.hmcts.reform.prl.services.SystemUserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.C100_CASE_TYPE;
import static uk.gov.hmcts.reform.prl.constants.PrlAppsConstants.FL401_CASE_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class CaseFlagMigrationServiceTest {

    @InjectMocks
    CaseFlagMigrationService caseFlagMigrationService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private SystemUserService systemUserService;

    @Mock
    private RefDataUserService refDataUserService;

    private static final String FLAG_TYPE = "PARTY";

    public static final String authToken = "Bearer TestAuthToken";

    private CaseData caseData;
    private CaseData caseDataFl401;
    private CaseDetails caseDetails;
    private Map<String, Object> caseDataMap;
    private CaseFlag caseFlagResponse;


    @Before
    public void setup() {

        FlagDetail flagDetail1 = FlagDetail.builder().flagCode("ABCD")
            .externallyAvailable(true).flagComment(true).cateGoryId(0).build();
        FlagDetail flagDetail2 = FlagDetail.builder().flagCode("CDEF")
            .childFlags(List.of(flagDetail1)).externallyAvailable(false).flagComment(true).cateGoryId(0).build();
        List<FlagDetail> flagDetails = new ArrayList<>();
        flagDetails.add(flagDetail2);
        Flag flag1 = Flag.builder().flagDetails(flagDetails).build();
        List<Flag> flags = new ArrayList<>();
        flags.add(flag1);
        caseFlagResponse = CaseFlag.builder().flags(flags).build();
        caseDataMap = new HashMap<>();
        caseDetails = CaseDetails.builder()
            .data(caseDataMap)
            .id(1234567891234567L)
            .state("SUBMITTED_PAID")
            .build();
        List<Element<uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail>> partyFlags =
            List.of(Element.<uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail>builder()
                        .value(uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail.builder()
                                   .flagCode("ABCD").build()).build(),
                    Element.<uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail>builder()
                        .value(uk.gov.hmcts.reform.prl.models.caseflags.flagdetails.FlagDetail.builder()
                                   .flagCode("CDEF").build()).build());
        PartyDetails partyDetailsApplicant = PartyDetails.builder()
            .firstName("")
            .partyLevelFlag(Flags.builder().partyName("ABCD")
                                .details(partyFlags)
                                .build())
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .build();
        PartyDetails partyDetailsApplicant2 = PartyDetails.builder()
            .firstName("")
            .partyLevelFlag(Flags.builder().partyName("ABCD")
                                .details(partyFlags)
                                .build())
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .build();
        PartyDetails partyDetailsApplicant3 = PartyDetails.builder()
            .firstName("")
            .partyLevelFlag(Flags.builder().partyName("ABCD")
                                .details(partyFlags)
                                .build())
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .build();
        PartyDetails partyDetailsApplicant4 = PartyDetails.builder()
            .firstName("")
            .partyLevelFlag(Flags.builder().partyName("ABCD")
                                .details(partyFlags)
                                .build())
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .build();
        PartyDetails partyDetailsApplicant5 = PartyDetails.builder()
            .firstName("")
            .partyLevelFlag(Flags.builder().partyName("ABCD")
                                .details(partyFlags)
                                .build())
            .lastName("")
            .email("")
            .user(User.builder().email("").idamId("").build())
            .build();
        PartyDetails partyDetailsRespondent = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .partyLevelFlag(Flags.builder().partyName("ABCD")
                                .details(partyFlags)
                                .build())
            .user(User.builder().email("").idamId("").build())
            .build();
        PartyDetails partyDetailsRespondent2 = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .partyLevelFlag(Flags.builder().partyName("ABCD")
                                .details(partyFlags)
                                .build())
            .user(User.builder().email("").idamId("").build())
            .build();
        PartyDetails partyDetailsRespondent3 = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .partyLevelFlag(Flags.builder().partyName("ABCD")
                                .details(partyFlags)
                                .build())
            .user(User.builder().email("").idamId("").build())
            .build();
        PartyDetails partyDetailsRespondent4 = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .partyLevelFlag(Flags.builder().partyName("ABCD")
                                .details(partyFlags)
                                .build())
            .user(User.builder().email("").idamId("").build())
            .build();
        PartyDetails partyDetailsRespondent5 = PartyDetails.builder()
            .firstName("")
            .lastName("")
            .email("")
            .partyLevelFlag(Flags.builder().partyName("ABCD")
                                .details(partyFlags)
                                .build())
            .user(User.builder().email("").idamId("").build())
            .build();
        caseData = CaseData.builder()
            .caseTypeOfApplication(C100_CASE_TYPE)
            .allPartyFlags(AllPartyFlags.builder().caApplicant1ExternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caApplicant1InternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caApplicant2ExternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caApplicant2InternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caApplicant3ExternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caApplicant3InternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caApplicant4ExternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caApplicant4InternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caApplicant5ExternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caApplicant5InternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caRespondent1ExternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caRespondent1InternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caRespondent2InternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caRespondent2ExternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caRespondent3InternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caRespondent3ExternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caRespondent4InternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caRespondent4ExternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caRespondent5InternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .caRespondent5ExternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .build())
            .applicants(List.of(
                Element.<PartyDetails>builder().value(partyDetailsApplicant).build(),
                Element.<PartyDetails>builder().value(partyDetailsApplicant2).build(),
                Element.<PartyDetails>builder().value(partyDetailsApplicant3).build(),
                Element.<PartyDetails>builder().value(partyDetailsApplicant4).build(),
                Element.<PartyDetails>builder().value(partyDetailsApplicant5).build()
            ))
            .respondents(List.of(Element.<PartyDetails>builder().id(UUID.fromString(
                                         "00000000-0000-0000-0000-000000000000"))
                                       .value(partyDetailsRespondent).build(),
                                   Element.<PartyDetails>builder().id(UUID.fromString(
                                           "00000000-0000-0000-0000-000000000000"))
                                       .value(partyDetailsRespondent2).build(),
                                   Element.<PartyDetails>builder().id(UUID.fromString(
                                           "00000000-0000-0000-0000-000000000000"))
                                       .value(partyDetailsRespondent3).build(),
                                   Element.<PartyDetails>builder().id(UUID.fromString(
                                           "00000000-0000-0000-0000-000000000000"))
                                       .value(partyDetailsRespondent4).build(),
                                   Element.<PartyDetails>builder().id(UUID.fromString(
                                           "00000000-0000-0000-0000-000000000000"))
                                       .value(partyDetailsRespondent5).build()))
            .build();

        caseDataFl401 = CaseData.builder()
            .caseTypeOfApplication(FL401_CASE_TYPE)
            .applicantsFL401(partyDetailsApplicant)
            .respondentsFL401(partyDetailsRespondent)
            .allPartyFlags(AllPartyFlags.builder()
                               .daApplicantExternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .daApplicantInternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .daRespondentExternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build())
                               .daRespondentInternalFlags(Flags.builder().partyName("").details(new ArrayList<>()).build()).build())
            .build();
    }


    @Test
    public void migrateCaseForCaseFlagsTestC100() {
        when(refDataUserService.retrieveCaseFlags(systemUserService.getSysUserToken(),FLAG_TYPE)).thenReturn(caseFlagResponse);
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseData);
        caseFlagMigrationService.migrateCaseForCaseFlags(caseDataMap);
        assertNotNull(caseData.getAllPartyFlags());
        assertEquals("ABCD",caseData.getAllPartyFlags().getCaApplicant1ExternalFlags().getDetails().get(0).getValue().getFlagCode());
    }


    @Test
    public void migrateCaseForCaseFlagsTestFL401() {
        when(refDataUserService.retrieveCaseFlags(systemUserService.getSysUserToken(),FLAG_TYPE)).thenReturn(caseFlagResponse);
        when(objectMapper.convertValue(caseDataMap,CaseData.class)).thenReturn(caseDataFl401);
        caseFlagMigrationService.migrateCaseForCaseFlags(caseDataMap);
        assertNotNull(caseDataFl401.getAllPartyFlags());
        assertEquals("ABCD",caseDataFl401.getAllPartyFlags().getDaApplicantExternalFlags().getDetails().get(0).getValue().getFlagCode());
    }



}
