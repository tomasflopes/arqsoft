package pt.psoft.g1.psoftg1.shared.infrastructure.sevice;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

public interface StringIdentifierGenerator extends IdentifierGenerator {
  String generate(SharedSessionContractImplementor session, Object object);

}
