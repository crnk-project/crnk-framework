package io.crnk.example.springboot;

import io.crnk.client.CrnkClient;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.meta.MetaModule;
import io.crnk.meta.model.MetaEnumType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.crnk.ui.UIModule;
import io.crnk.ui.UIModuleConfig;
import io.crnk.ui.presentation.PresentationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@Configuration
@RestController
@SpringBootApplication
@Import({CorsConfig.class, TestDataLoader.class})
public class SpringBootExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootExampleApplication.class, args);
        System.out.println("visit http://127.0.0.1:8080/api/ resp. http://127.0.0.1:8080/browse/ in your browser");
    }

    @Bean
    public UIModule uiModule() {
        CrnkClient client = new CrnkClient("http://127.0.0.1:8080/api/");
        client.addModule(MetaModule.createClientModule());

        // client initialization workaround
        client.getRepositoryForType(MetaEnumType.class);
        client.getRepositoryForType(MetaResourceField.class);

        ResourceRepository<MetaResource, String> repository = client.getRepositoryForType(MetaResource.class);
        PresentationService service = new PresentationService("example", "/api/", repository);

        UIModuleConfig config = new UIModuleConfig();
        config.setServices(() -> Arrays.asList(service));
        return UIModule.create(config);
    }
}
