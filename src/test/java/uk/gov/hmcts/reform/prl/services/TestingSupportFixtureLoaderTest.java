package uk.gov.hmcts.reform.prl.services;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.env.MockEnvironment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestingSupportFixtureLoaderTest {

    private static final String FIXTURE = "C100_Dummy_Gatekeeping_CaseDetails_v4.json";

    @Test
    void resolvesPlaceholdersAgainstActiveEnvironment() throws Exception {
        MockEnvironment env = new MockEnvironment()
            .withProperty("prl.environment", "demo")
            .withProperty("testing-support-data.demo.org.org2.id", "DEMO-FPRL-ID")
            .withProperty("testing-support-data.demo.org.org2.name", "DEMO-FPRL-NAME");
        TestingSupportFixtureLoader loader = new TestingSupportFixtureLoader(env);

        String json = loader.loadJson(FIXTURE);

        assertTrue(json.contains("\"OrganisationID\": \"DEMO-FPRL-ID\""));
        assertTrue(json.contains("\"OrganisationName\": \"DEMO-FPRL-NAME\""));
        assertTrue(!json.contains("\"QO4A1Q8\""));
    }

    @Test
    void usesInlineDefaultsWhenEnvHasNoOverrides() throws Exception {
        MockEnvironment env = new MockEnvironment();
        TestingSupportFixtureLoader loader = new TestingSupportFixtureLoader(env);

        String json = loader.loadJson(FIXTURE);

        assertTrue(json.contains("\"OrganisationID\": \"QO4A1Q8\""));
        assertTrue(json.contains("\"OrganisationName\": \"FPRL-test-organisation\""));
        assertTrue(!json.contains("${testing-support-data."),
            "expected every testing-support-data.* placeholder to be resolved (defaults should kick in)");
    }

    @Test
    void leavesCcdStylePlaceholdersAlone() throws Exception {
        MockEnvironment env = new MockEnvironment();
        TestingSupportFixtureLoader loader = new TestingSupportFixtureLoader(env);

        String json = loader.loadJson(FIXTURE);

        assertTrue(json.contains("${[CASE_REFERENCE]}"),
            "expected CCD-style ${[CASE_REFERENCE]} placeholders to be preserved");
    }

    @Test
    void demoValuesCanBeResolved() throws Exception {
        String saved = System.getProperty("APP_ENV");
        try {
            System.setProperty("APP_ENV", "demo");

            StandardEnvironment env = new StandardEnvironment();
            List<PropertySource<?>> sources = new YamlPropertySourceLoader()
                .load("application", new ClassPathResource("application.yaml"));
            sources.forEach(env.getPropertySources()::addLast);

            assertEquals("demo", env.getProperty("prl.environment"));
            assertEquals("6RUBIJM", env.getProperty("testing-support-data.demo.org.org1.id"));
            assertEquals("9SCQJOI", env.getProperty("testing-support-data.demo.org.org2.id"));
            assertEquals("52IWCVT", env.getProperty("testing-support-data.demo.org.org3.id"));
            assertEquals("UYLJCAI", env.getProperty("testing-support-data.demo.org.org4.id"));

            TestingSupportFixtureLoader loader = new TestingSupportFixtureLoader(env);
            String json = loader.loadJson(FIXTURE);

            assertTrue(json.contains("\"OrganisationID\": \"9SCQJOI\""));
            assertTrue(json.contains("\"OrganisationName\": \"Private law Applicant organisation\""));
            assertTrue(!json.contains("\"QO4A1Q8\""));
            assertTrue(!json.contains("\"FPRL-test-organisation\""));
        } finally {
            if (saved != null) {
                System.setProperty("APP_ENV", saved);
            } else {
                System.clearProperty("APP_ENV");
            }
        }
    }

    @Test
    void rawResourceLoaderReturnsSameJsonWhenNoPlaceholdersToResolve() throws Exception {
        MockEnvironment env = new MockEnvironment();
        TestingSupportFixtureLoader loader = new TestingSupportFixtureLoader(env);

        String viaLoader = loader.loadJson("Dummy_Respondent_Tasklist_Data.json");
        String viaRaw = uk.gov.hmcts.reform.prl.utils.ResourceLoader.loadJson("Dummy_Respondent_Tasklist_Data.json");

        assertEquals(viaRaw, viaLoader);
    }
}
