package scheduler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import service.FileCleanupService;

@Slf4j
public class FileCleanupJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing file cleanup job");

        try {
            FileCleanupService cleanupService = (FileCleanupService)
                    context.getJobDetail().getJobDataMap().get("fileCleanupService");

            cleanupService.cleanupOldFiles();

        } catch (Exception e) {
            log.error("Error executing file cleanup job", e);
            throw new JobExecutionException(e);
        }
    }
}
