package core.observer;

import java.io.*;
import java.net.*;

public class ActivityEvent
{
	public static enum EventType
	{
		START, END, PAUSE, RESUME, ERROR, PROGRESS, STOP, SETUP;
	}

	private InetAddress from, to;
	private File file;
	private int port;
	private EventType type;
	private long total;

	public ActivityEvent(InetAddress from, InetAddress to, File file, int port, long total)
	{
		this.from = from;
		this.to = to;
		this.file = file;
		this.port = port;
		this.total = total;
	}

	void setType(EventType type)
	{
		this.type = type;
	}

	public long getTotal()
	{
		return total;
	}

	public void setTotal(long total)
	{
		this.total = total;
	}

	public EventType getType()
	{
		return type;
	}

	public ActivityEvent()
	{
	}

	public InetAddress getFrom()
	{
		return from;
	}

	public void setFrom(InetAddress from)
	{
		this.from = from;
	}

	public InetAddress getTo()
	{
		return to;
	}

	public void setTo(InetAddress to)
	{
		this.to = to;
	}

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + port;
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		result = prime * result + (int) (total ^ (total >>> 32));
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActivityEvent other = (ActivityEvent) obj;
		if (file == null)
		{
			if (other.file != null)
				return false;
		}
		else if (!file.equals(other.file))
			return false;
		if (from == null)
		{
			if (other.from != null)
				return false;
		}
		else if (!from.equals(other.from))
			return false;
		if (port != other.port)
			return false;
		if (to == null)
		{
			if (other.to != null)
				return false;
		}
		else if (!to.equals(other.to))
			return false;
		if (total != other.total)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "ActivityEvent [from=" + from + ", to=" + to + ", file=" + file + ", port=" + port + ", type=" + type + ", total=" + total + "]";
	}

}
