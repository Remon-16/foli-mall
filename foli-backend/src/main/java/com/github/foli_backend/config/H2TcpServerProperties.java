package com.github.foli_backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * H2 TCP Server 配置属性。
 * H2 TCP Server configuration properties.
 *
 * <p>对应 application.yaml 中的 app.h2.tcp 配置节。</p>
 * Maps to the {@code app.h2.tcp} section in application.yaml.
 *
 * @param enabled 是否在应用启动时开启 H2 TCP Server / whether to start the H2 TCP Server on boot
 * @param port    H2 TCP Server 监听端口 / port the H2 TCP Server listens on
 */
@ConfigurationProperties(prefix = "app.h2.tcp")
public record H2TcpServerProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("9092") int port) {
}
