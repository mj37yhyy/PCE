package flow.starter.SchedulerStarter

import org.pce.core.Node
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory

/**
 */
class SchedulerStarter extends Node {
    @Override
    def handle(Map globalMemory) {

        String cronExpression = this.params.get("cronExpression")//corn表达式

        globalMemory.put("this", this)
        JobDetail job = JobBuilder.newJob(StartJob.class)
                .withIdentity("startJob-${globalMemory.get("projectId")}")
                .usingJobData(new JobDataMap(globalMemory))
                .build()

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(TriggerKey.triggerKey("startTrigger", "startTriggerGroup-${globalMemory.get("projectId")}"))
                .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression))
                .startNow()
                .build()

        scheduler.scheduleJob(job, trigger)
        scheduler.start()
    }

    @Override
    void shutdown(){
        println "SchedulerStarter开始关闭..."
        scheduler.shutdown()
        println "SchedulerStarter关闭完成，状态：${scheduler.isShutdown()}."
        
    }

    Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler()
}

class StartJob implements Job {
    @Override
    void execute(JobExecutionContext context) throws JobExecutionException {
        Map globalMemory = context.getMergedJobDataMap()//公共内存
        SchedulerStarter schedulerStarter = globalMemory.get("this") as SchedulerStarter
        globalMemory.remove("this")
        schedulerStarter.next.handle(schedulerStarter, globalMemory)//调用下一个MessageHandler
    }

}
