package scheduler;

import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import service.FileCleanupService;

@Slf4j
public class FileCleanupScheduler {
    private final Scheduler scheduler;
    private final FileCleanupService fileCleanupService;

    public FileCleanupScheduler(FileCleanupService fileCleanupService) throws SchedulerException {
        this.scheduler = StdSchedulerFactory.getDefaultScheduler();
        this.fileCleanupService = fileCleanupService;
    }

    public void start() throws SchedulerException {
        JobDetail job = JobBuilder.newJob(FileCleanupJob.class)
                .withIdentity("fileCleanupJob", "fileStorage")
                .build();

        job.getJobDataMap().put("fileCleanupService", fileCleanupService);

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("fileCleanupTrigger", "fileStorage")
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(2, 0))
                .build();

        scheduler.scheduleJob(job, trigger);
        scheduler.start();

        log.info("File cleanup scheduler started. Will run daily at 2:00 AM");
    }

    public void stop() throws SchedulerException {
        scheduler.shutdown();
        log.info("File cleanup scheduler stopped");
    }
}
