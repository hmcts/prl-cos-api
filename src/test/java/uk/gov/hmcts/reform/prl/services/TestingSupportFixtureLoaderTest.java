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
            .withProperty("ts.demo.org.org2.id", "DEMO-FPRL-ID")
            .withProperty("ts.demo.org.org2.name", "DEMO-FPRL-NAME");
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
        assertTrue(!json.contains("${ts."),
            "expected every ts.* placeholder to be resolved (defaults should kick in)");
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
    void applicationYamlDemoBlockResolvesDemoValues() throws Exception {
        List<PropertySource<?>> sources = new YamlPropertySourceLoader()
            .load("application", new ClassPathResource("application.yaml"));

        StandardEnvironment env = new StandardEnvironment();
        sources.forEach(env.getPropertySources()::addFirst);
        // simulate the demo pod's APP_ENV → prl.environment chain
        env.getPropertySources().addFirst(new org.springframework.core.env.MapPropertySource(
            "test-overrides", java.util.Map.of("prl.environment", "demo")));

        for (String key : List.of(
            "ts.demo.org.org1.id", "ts.demo.org.org1.name",
            "ts.demo.org.org2.id", "ts.demo.org.org2.name",
            "ts.demo.org.org3.id", "ts.demo.org.org3.name",
            "ts.demo.org.org4.id", "ts.demo.org.org4.name"
        )) {
            assertTrue(env.getProperty(key) != null && !env.getProperty(key).isBlank());
        }

        TestingSupportFixtureLoader loader = new TestingSupportFixtureLoader(env);
        String json = loader.loadJson(FIXTURE);
        String demoFprlId = env.getProperty("ts.demo.org.org2.id");
        assertTrue(json.contains("\"OrganisationID\": \"" + demoFprlId + "\""));
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
            assertEquals("6RUBIJM", env.getProperty("ts.demo.org.org1.id"));
            assertEquals("9SCQJOI", env.getProperty("ts.demo.org.org2.id"));
            assertEquals("52IWCVT", env.getProperty("ts.demo.org.org3.id"));
            assertEquals("UYLJCAI", env.getProperty("ts.demo.org.org4.id"));

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
