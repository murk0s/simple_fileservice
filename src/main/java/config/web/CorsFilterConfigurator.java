package config.web;

import config.web.abstracts.FilterConfigurator;
import controller.CorsFilter;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.EnumSet;

@Slf4j
public class CorsFilterConfigurator implements FilterConfigurator {
    private final Filter corsFilter;

    public CorsFilterConfigurator() {
        this.corsFilter = new CorsFilter();
    }

    @Override
    public void configure(ServletContextHandler context) {
        context.addFilter(new FilterHolder(corsFilter), "/*",
                EnumSet.of(DispatcherType.REQUEST));
        log.debug("CORS filter configured");
    }
}