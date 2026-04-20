package uk.gov.hmcts.reform.prl.config;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
class BundleCategoryConfigTest {

    @Autowired
    private BundleCategoryConfig bundleCategoryConfig;

    @Test
    public void whenFactoryProvidedThenYamlPropertiesInjected() {
        assertEquals(5, bundleCategoryConfig.getFolders().size());
        assertEquals("Preliminary Documents", bundleCategoryConfig.getFolders().get(0).getName());
        assertEquals("Applications and Orders", bundleCategoryConfig.getFolders().get(1).getName());
        assertEquals("Witness statement and evidence", bundleCategoryConfig.getFolders().get(2).getName());
        assertEquals("Expert Reports including Guardian (CAFCASS)", bundleCategoryConfig.getFolders().get(3).getName());
        assertEquals("Other Documents", bundleCategoryConfig.getFolders().get(4).getName());
    }
}
