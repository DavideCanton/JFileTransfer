package core.observer;

public class ActivityObserverAdapter implements ActivityObserver
{

	@Override
	public void notifyStart(ActivityEvent e, ActivityObservable ob)
	{
	}

	@Override
	public void notifyEnd(ActivityEvent e, ActivityObservable ob)
	{
	}

	@Override
	public void notifyPause(ActivityEvent e, ActivityObservable ob)
	{
	}

	@Override
	public void notifyResume(ActivityEvent e, ActivityObservable ob)
	{
	}

	@Override
	public void notifyProgress(ProgressEvent e, ActivityObservable ob)
	{
	}

	@Override
	public void notifyStop(ActivityEvent ev, ActivityObservable ob)
	{
	}

	@Override
	public void notifySetup(ActivityEvent e, ActivityObservable ob)
	{
	}
}
