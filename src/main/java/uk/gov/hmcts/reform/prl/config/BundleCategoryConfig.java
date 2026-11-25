package uk.gov.hmcts.reform.prl.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.validation.annotation.Validated;
import uk.gov.hmcts.reform.prl.models.bundle.FolderProperties;

import java.util.List;

@Validated
@Configuration
@ConfigurationProperties(prefix = "bundle")
@PropertySource(value = "classpath:bundleconfig.yaml", factory = YamlPropertySourceFactory.class)
public class BundleCategoryConfig {

    @JsonProperty("folders")
    private List<FolderProperties> folders;

    public List<FolderProperties> getFolders() {
        return folders;
    }

    public void setFolders(List<FolderProperties> folders) {
        this.folders = folders;
    }
}
