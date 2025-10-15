package config.web.abstracts;

import org.eclipse.jetty.servlet.ServletContextHandler;

public interface ServletConfigurator {
    void configure(ServletContextHandler context);
}
