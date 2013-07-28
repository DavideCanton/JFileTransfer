package core.observer;

public interface ActivityObserver
{
	public void notifyStart(ActivityEvent e, ActivityObservable ob);

	public void notifyEnd(ActivityEvent e, ActivityObservable ob);

	public void notifyPause(ActivityEvent e, ActivityObservable ob);

	public void notifyResume(ActivityEvent e, ActivityObservable ob);

	public void notifyProgress(ProgressEvent e, ActivityObservable ob);

	public void notifyStop(ActivityEvent ev, ActivityObservable ob);

	public void notifySetup(ActivityEvent e, ActivityObservable ob);
}
