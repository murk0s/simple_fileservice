package config.web;

import config.web.abstracts.FilterConfigurator;
import config.web.abstracts.ServerInitializer;
import config.web.abstracts.ServletConfigurator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
class JettyServerInitializer implements ServerInitializer {
    private final ServerConfig config;
    private final List<FilterConfigurator> filterConfigurators;
    private final List<ServletConfigurator> servletConfigurators;

    @Override
    public void initialize() throws Exception {
        Server server = new Server(config.getPort());
        ServletContextHandler context = createServletContext();

        server.setHandler(context);
        server.start();

        log.info("Server started at http://localhost:{}", config.getPort());
        server.join();
    }

    private ServletContextHandler createServletContext() {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(config.getContextPath());

        for (FilterConfigurator configurator : filterConfigurators) {
            configurator.configure(context);
        }

        for (ServletConfigurator configurator : servletConfigurators) {
            configurator.configure(context);
        }

        return context;
    }
}