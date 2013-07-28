package core.observer;

import java.util.*;
import java.util.concurrent.locks.*;
import core.observer.ActivityEvent.EventType;

public abstract class AbstractCyclicActivityObservable implements ActivityObservable
{
	protected Set<ActivityObserver> observers = new HashSet<ActivityObserver>();
	private boolean paused = false;
	private WorkerThread worker;
	private Lock lock = new ReentrantLock();
	private Condition pausedC = lock.newCondition();

	@Override
	public void addObserver(ActivityObserver o)
	{
		observers.add(o);
	}

	@Override
	public void removeObserver(ActivityObserver o)
	{
		observers.remove(o);
	}

	protected abstract ActivityEvent start();

	protected abstract ActivityEvent end();

	protected abstract ActivityEvent pause();

	protected abstract ActivityEvent resume();

	protected abstract ActivityEvent progress();

	protected abstract ActivityEvent stop();

	protected abstract boolean endOfActivity();

	protected abstract ActivityEvent setup();

	@Override
	public void startActivity()
	{
		ActivityEvent e = setup();
		if (e != null)
		{
			e.setType(EventType.SETUP);
			for (ActivityObserver o : observers)
				o.notifySetup(e, this);
		}
		worker = new WorkerThread();
		worker.start();
	}

	@Override
	public void stopActivity()
	{
		if (worker != null)
			worker.interrupt();
		stop();
	}

	@Override
	public void pauseActivity() throws Exception
	{
		ActivityEvent e = pause();
		e.setType(EventType.PAUSE);
		for (ActivityObserver o : observers)
			o.notifyPause(e, this);
		if (!(e instanceof ErrorEvent))
		{
			lock.lock();
			paused = true;
			lock.unlock();
		}
		else
			throw ((ErrorEvent) e).getError();
	}

	@Override
	public void resumeActivity() throws Exception
	{
		ActivityEvent e = resume();
		e.setType(EventType.RESUME);
		for (ActivityObserver o : observers)
			o.notifyResume(e, this);
		if (!(e instanceof ErrorEvent))
		{
			lock.lock();
			paused = false;
			pausedC.signal();
			lock.unlock();
		}
		else
			throw ((ErrorEvent) e).getError();
	}

	private class WorkerThread extends Thread
	{

		@Override
		public void run()
		{
			ErrorEvent error = null;
			ActivityEvent e = AbstractCyclicActivityObservable.this.start();
			if (!(e instanceof ErrorEvent))
			{
				e.setType(EventType.START);
				for (ActivityObserver o : observers)
					o.notifyStart(e, AbstractCyclicActivityObservable.this);
				while (error == null && !endOfActivity() && !isInterrupted())
				{
					try
					{
						waitIfPaused();
					}
					catch (InterruptedException ex)
					{
						break;
					}
					e = progress();
					if (e instanceof ProgressEvent)
						for (ActivityObserver o : observers)
							o.notifyProgress((ProgressEvent) e, AbstractCyclicActivityObservable.this);
					else
						error = (ErrorEvent) e;
				}
				if (!isInterrupted())
				{
					ActivityEvent ev = error != null ? error : end();
					ev.setType(EventType.END);
					for (ActivityObserver o : observers)
						o.notifyEnd(ev, AbstractCyclicActivityObservable.this);
				}
				else
				{
					ActivityEvent ev = error != null ? error : AbstractCyclicActivityObservable.this.stop();
					ev.setType(EventType.STOP);
					for (ActivityObserver o : observers)
						o.notifyStop(ev, AbstractCyclicActivityObservable.this);
				}
			}
			else
				for (ActivityObserver o : observers)
					o.notifyStart(e, AbstractCyclicActivityObservable.this); // con l'errore
		}

		private void waitIfPaused() throws InterruptedException
		{
			lock.lock();
			try
			{
				while (paused)
					pausedC.await();
			}
			finally
			{
				lock.unlock();
			}
		}
	}
}
