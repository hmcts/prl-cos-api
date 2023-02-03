package uk.gov.hmcts.reform.prl.services.staff;

/*
@RunWith(MockitoJUnitRunner.Silent.class)
public class StaffUserInfoServiceTest {

    @InjectMocks
    StaffUserInfoService staffUserInfoService;

    @Mock
    AuthTokenGenerator authTokenGenerator;

    @Mock
    StaffProfile staffProfile;

    @Mock
    StaffResponse staffResponse;

    @Mock
    StaffResponseDetailsApi staffResponseDetailsApi;


   /* @Test
    public void testGetLegalAdvisorList() {
        List<StaffResponse> listOfStaffResponse = new ArrayList<>();
        StaffProfile staffProfile1 = StaffProfile.builder().userType("Legal Office").lastName("John").emailId(
            "test1@com").build();
        StaffProfile staffProfile2 = StaffProfile.builder().userType("Legal Office").lastName("David").emailId(
            "test2@com").build();
        StaffResponse staffResponse1 = StaffResponse.builder().ccdServiceName("PRIVATELAW").staffProfile(staffProfile1).build();
        StaffResponse staffResponse2 = StaffResponse.builder().ccdServiceName("PRIVATELAW").staffProfile(staffProfile2).build();
        listOfStaffResponse.add(staffResponse1);
        listOfStaffResponse.add(staffResponse2);
        when(authTokenGenerator.generate()).thenReturn("s2sToken");
        when(staffResponseDetailsApi.getAllStaffResponseDetails("authorization",
                                                                authTokenGenerator.generate(),
                                                                SERVICENAME))
            .thenReturn(listOfStaffResponse);

        List<DynamicListElement> legalAdvisorList = staffUserInfoService.getLegalAdvisorList("authorization");
        assertNotNull(legalAdvisorList.get(0).getCode());
        assertEquals("David - test2@com",legalAdvisorList.get(0).getCode());
    }
}*/
