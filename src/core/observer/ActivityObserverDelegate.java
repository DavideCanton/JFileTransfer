package core.observer;

public abstract class ActivityObserverDelegate implements ActivityObserver
{
	protected abstract void notifyEvent(ActivityEvent e, ActivityObservable ob);

	@Override
	public void notifyStart(ActivityEvent e, ActivityObservable ob)
	{
		notifyEvent(e, ob);
	}

	@Override
	public void notifyEnd(ActivityEvent e, ActivityObservable ob)
	{
		notifyEvent(e, ob);
	}

	@Override
	public void notifyPause(ActivityEvent e, ActivityObservable ob)
	{
		notifyEvent(e, ob);
	}

	@Override
	public void notifyResume(ActivityEvent e, ActivityObservable ob)
	{
		notifyEvent(e, ob);
	}

	@Override
	public void notifyProgress(ProgressEvent e, ActivityObservable ob)
	{
		notifyEvent(e, ob);
	}

	@Override
	public void notifyStop(ActivityEvent ev, ActivityObservable ob)
	{
		notifyEvent(ev, ob);
	}

	@Override
	public void notifySetup(ActivityEvent e, ActivityObservable ob)
	{
		notifyEvent(e, ob);
	}
}
