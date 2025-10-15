import config.HibernateUtil;
import config.web.ApplicationFactory;
import config.web.ServerConfig;
import config.web.abstracts.ServerInitializer;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import scheduler.FileCleanupScheduler;

@Slf4j
public class Application {
    private static FileCleanupScheduler cleanupScheduler;

    public static void main(String[] args) throws Exception {
        try {


            ServerConfig config = ApplicationFactory.createServerConfig();

            cleanupScheduler = ApplicationFactory.createFileCleanupScheduler(config);
            cleanupScheduler.start();

            ServerInitializer serverInitializer = ApplicationFactory.createServerInitializer(config);

            serverInitializer.initialize();

        } catch (Exception e) {
            log.error("Failed to start application", e);
            throw e;
        }
        finally {
            shutdown();
        }
    }

    private static void shutdown() {
        if (cleanupScheduler != null) {
            try {
                cleanupScheduler.stop();
            } catch (SchedulerException e) {
                log.error("Error stopping cleanup scheduler", e);
            }
        }

        HibernateUtil.shutdown();
    }
}
