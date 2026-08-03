package com.github.foli_backend.config;

import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * H2 TCP Server 自动配置 — 应用启动时开启 H2 TCP 端口，供外部进程（如 flow-forge 数据库插件）共享内存库。
 * H2 TCP Server auto-configuration — opens an H2 TCP port on boot so external
 * processes (e.g. flow-forge DB plugins) can share the in-memory database.
 *
 * <p>说明：H2 内存库在同一 JVM 内按库名共享，嵌入式连接（jdbc:h2:mem:foli_mall）与
 * TCP 连接（jdbc:h2:tcp://localhost:9092/mem:foli_mall）访问同一实例，schema 初始化不受影响。</p>
 * Note: in the same JVM, H2 in-memory databases are shared by name; the embedded
 * connection (jdbc:h2:mem:foli_mall) and TCP connections
 * (jdbc:h2:tcp://localhost:9092/mem:foli_mall) see the same instance, and schema
 * initialization is unaffected.
 */
@Configuration
@EnableConfigurationProperties(H2TcpServerProperties.class)
@ConditionalOnProperty(prefix = "app.h2.tcp", name = "enabled", havingValue = "true", matchIfMissing = true)
public class H2TcpServerConfig {

    private static final Logger log = LoggerFactory.getLogger(H2TcpServerConfig.class);

    /**
     * 创建并启动 H2 TCP Server（应用关闭时自动停止）。
     * Create and start the H2 TCP Server (auto-stopped when the application shuts down).
     *
     * @param properties H2 TCP 配置 / H2 TCP configuration
     * @return 已启动的 H2 Server 实例 / the started H2 Server instance
     */
    @Bean(destroyMethod = "stop")
    public Server h2TcpServer(H2TcpServerProperties properties) throws Exception {
        log.info("正在启动 H2 TCP Server，端口 {} / Starting H2 TCP Server on port {}",
                properties.port(), properties.port());
        Server server = Server.createTcpServer("-tcpPort", String.valueOf(properties.port()));
        try {
            server.start();
        } catch (Exception e) {
            log.error("H2 TCP Server 启动失败，端口 {} 可能被占用 / Failed to start H2 TCP Server, port {} may be in use",
                    properties.port(), properties.port(), e);
            throw e;
        }
        log.info("H2 TCP Server 已启动，端口 {} / H2 TCP Server started on port {}",
                properties.port(), properties.port());
        return server;
    }
}
