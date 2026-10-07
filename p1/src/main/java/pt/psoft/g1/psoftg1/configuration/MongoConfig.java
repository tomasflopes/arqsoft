package pt.psoft.g1.psoftg1.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Profile("mongo")
@Configuration
@EnableTransactionManagement
@ConditionalOnProperty(prefix = "spring.data.mongodb", name = "uri")
@EnableMongoRepositories(basePackages = "pt.psoft.g1.psoftg1")
public class MongoConfig {
}
