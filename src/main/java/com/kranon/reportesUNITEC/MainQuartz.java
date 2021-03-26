package com.kranon.reportesUNITEC;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.CronScheduleBuilder;

public class MainQuartz {
	
	public static void main(String[] args) {
		try {
			//CREANDO EL JOB A EJECUTAR
			JobDetail voJob = JobBuilder.newJob(EstadosAgentes.class)
					.withIdentity("JobEstadosAgentes","ReportesUNITEC")
					.build();
			
			//CREADO EL HILO QUE EJECUTARA EL JOB
			Trigger voTrigger = TriggerBuilder.newTrigger()
					.withIdentity("TriggerEstadosAgentes","ReportesUNITEC")
					.startNow()
					.withSchedule(CronScheduleBuilder.cronSchedule("0 0 11 * * ?"))
					.build();
			
			Scheduler voScheduler = StdSchedulerFactory.getDefaultScheduler();
			voScheduler.start();
			voScheduler.scheduleJob(voJob,voTrigger);
			
		} catch(SchedulerException ex) {
			System.err.println(ex.getMessage());
		}
	}
}
	