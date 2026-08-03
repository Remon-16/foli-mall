package com.github.foli_backend.config;

import org.h2.tools.Server;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * H2 TCP Server 配置测试 — 验证 enabled=true 时启动、enabled=false 时不启动。
 * Tests for the H2 TCP Server config — starts when enabled, absent when disabled.
 *
 * <p>使用独立测试端口，避免与默认的 9092 端口冲突。</p>
 * Uses a dedicated test port to avoid conflicts with the default 9092 port.
 */
class H2TcpServerConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(H2TcpServerConfig.class));

    @Test
    void startsServerWhenEnabled() {
        contextRunner
                .withPropertyValues("app.h2.tcp.port=19092")
                .run(context -> {
                    assertThat(context).hasSingleBean(Server.class);
                    Server server = context.getBean(Server.class);
                    assertThat(server.isRunning(true)).isTrue();
                    assertThat(server.getPort()).isEqualTo(19092);
                });
    }

    @Test
    void doesNotStartServerWhenDisabled() {
        contextRunner
                .withPropertyValues("app.h2.tcp.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(Server.class));
    }
}
