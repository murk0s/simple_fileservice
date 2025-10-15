import config.HibernateUtil;
import config.web.ApplicationFactory;
import config.web.ServerConfig;
import config.web.abstracts.ServerInitializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Application {
    public static void main(String[] args) throws Exception {
        try {
            ServerConfig config = ApplicationFactory.createServerConfig();
            ServerInitializer serverInitializer = ApplicationFactory.createServerInitializer(config);

            serverInitializer.initialize();

        } catch (Exception e) {
            log.error("Failed to start application", e);
            throw e;
        }
        finally {
            HibernateUtil.shutdown();
        }
    }
}
