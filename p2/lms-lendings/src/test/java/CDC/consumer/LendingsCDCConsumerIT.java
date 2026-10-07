package CDC.consumer;

import au.com.dius.pact.provider.junitsupport.IgnoreNoPactsToVerify;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import pt.psoft.g1.psoftg1.lendingmanagement.api.LendingRabbitmqController;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@IgnoreNoPactsToVerify
public class LendingsCDCConsumerIT {

    @Autowired
    private LendingRabbitmqController lendingRabbitmqController;

    @Test
    public void contextLoads() {
        assertNotNull(lendingRabbitmqController);
    }
}
