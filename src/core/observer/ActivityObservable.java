package core.observer;

public interface ActivityObservable
{
	public void addObserver(ActivityObserver o);

	public void removeObserver(ActivityObserver o);

	public void startActivity() throws Exception;

	public void pauseActivity() throws Exception;

	public void resumeActivity() throws Exception;

	public void stopActivity();
}
