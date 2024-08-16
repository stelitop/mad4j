package slashcommands;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import slashcommands.BaseTestConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest
@Import(BaseTestConfiguration.class)
public class BaseTestConfigurationTest {

    @Test
    void testSpringTestContext() {

    }
}
