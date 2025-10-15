package config.web.abstracts;

import org.eclipse.jetty.servlet.ServletContextHandler;

public interface FilterConfigurator {
    void configure(ServletContextHandler context);
}
