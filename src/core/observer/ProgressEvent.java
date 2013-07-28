package core.observer;

import java.io.*;
import java.net.*;

public class ProgressEvent extends ActivityEvent
{
	private long completed;

	public ProgressEvent(InetAddress from, InetAddress to, File file, int port, long completed, long total)
	{
		super(from, to, file, port, total);
		this.completed = completed;
		setType(EventType.PROGRESS);
	}

	public long getCompleted()
	{
		return completed;
	}

	public void setCompleted(long completed)
	{
		this.completed = completed;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (completed ^ (completed >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProgressEvent other = (ProgressEvent) obj;
		if (completed != other.completed)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "ProgressEvent [completed=" + completed + ", " + super.toString() + "]";
	}

}
