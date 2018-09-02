package in.blacklotus.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scheduler {

	private int ferquency;
	
	private TimeUnit timeUnit;

	private ScheduledExecutorService ses;

	private SchedulerCallback schedulerCallback;

	public Scheduler() {

		super();
	}

	public Scheduler(int frequency, TimeUnit timeUnit, SchedulerCallback schedulerCallback) {

		super();

		this.ferquency = frequency;
		
		this.timeUnit = timeUnit;

		this.setSchedulerCallback(schedulerCallback);
	}

	public void start() {

		ses = Executors.newSingleThreadScheduledExecutor();

		ses.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {

				if (schedulerCallback != null) {

					schedulerCallback.onTrigger();
				}
			}

		}, 0, this.ferquency, this.timeUnit);
	}

	public void stop() {

		if (ses != null && !ses.isShutdown()) {

			ses.shutdown();
		}
	}

	public SchedulerCallback getSchedulerCallback() {

		return schedulerCallback;
	}

	public void setSchedulerCallback(SchedulerCallback schedulerCallback) {

		this.schedulerCallback = schedulerCallback;
	}

	public interface SchedulerCallback {

		public void onTrigger();
	}
}
