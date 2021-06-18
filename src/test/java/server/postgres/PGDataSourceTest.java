package server.postgres;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class PGDataSourceTest {

    @Test
    void getConnection() {
        assertThatCode(() -> {
            try (var x = new PGDataSource("docker", "docker").getConnection()) {
                assertThat(x).isNotNull();
            }
        }).doesNotThrowAnyException();
    }
}