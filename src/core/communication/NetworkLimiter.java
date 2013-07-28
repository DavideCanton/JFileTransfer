package core.communication;

public class NetworkLimiter
{
	private int connections;
	private double bandwidth;
	public static final int TIME_TO_SLEEP = 100;
	private static NetworkLimiter instance;

	public synchronized static NetworkLimiter getInstance()
	{
		if (instance == null)
			instance = new NetworkLimiter();
		return instance;
	}

	public synchronized void increaseConnections()
	{
		connections++;		
	}

	public synchronized void decreaseConnections()
	{
		connections--;		
	}

	public synchronized int getBufferSize()
	{
		double bpc = bandwidth / connections;
		int size = (int) Math.round(bpc * TIME_TO_SLEEP / 1000);
		return size;
	}

	public synchronized void setBandwidth(double bandwidth)
	{
		this.bandwidth = bandwidth;
	}
}
