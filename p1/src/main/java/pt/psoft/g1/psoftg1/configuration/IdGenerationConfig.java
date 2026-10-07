package pt.psoft.g1.psoftg1.configuration;

import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import pt.psoft.g1.psoftg1.shared.infrastructure.sevice.Base65Generator;
import pt.psoft.g1.psoftg1.shared.infrastructure.sevice.StringIdentifierGenerator;
import pt.psoft.g1.psoftg1.shared.infrastructure.sevice.TimestampGenerator;

import java.util.Objects;
import java.util.Properties;

public class IdGenerationConfig implements IdentifierGenerator {
  private static final String STRATEGY_PARAM_NAME = "app.id.strategy";
  private StringIdentifierGenerator generator;

  @Autowired(required = false)
  private Environment env;

  @Override
  public void configure(Type type, Properties params, ServiceRegistry serviceRegistry) throws MappingException {
    String strategy = "base65";

    if (env != null) {
      strategy = env.getProperty(STRATEGY_PARAM_NAME);
    } else {
      strategy = params.getProperty("strategy", strategy);
    }

    switch (Objects.requireNonNull(strategy)) {
      case "base65":
        generator = new Base65Generator();
        break;
      case "timestamp":
        generator = new TimestampGenerator();
        break;
      default:
        throw new MappingException("Unknown ID generation strategy: " + strategy);
    }
  }

  @Override
  public String generate(SharedSessionContractImplementor session, Object object) {
    return generator.generate(session, object);
  }
}
