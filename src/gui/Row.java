package gui;

import java.awt.*;
import java.io.*;
import java.net.*;

public class Row
{
	public static enum State
	{
		WAIT, TRANSFER, PAUSE, ERROR, FINISHED;
	}

	private State state;
	private InetAddress from;
	private InetAddress to;
	private int port;
	private long completed, total;
	private double speed;
	private File file;
	private String message;
	private long timestamp;
	private int number;

	public File getFile()
	{
		return file;
	}

	public void setFile(File file)
	{
		this.file = file;
	}

	public double getSpeed()
	{
		return state == State.TRANSFER ? speed : -1;
	}

	public Image getImage()
	{
		switch (state)
		{
			case FINISHED:
				return ImageHandler.getOk();
			case WAIT:
				return ImageHandler.getWait();
			case TRANSFER:
				return ImageHandler.getPlay();
			case PAUSE:
				return ImageHandler.getPause();
			case ERROR:
				return ImageHandler.getError();
			default:
				return null;
		}
	}

	public State getState()
	{
		return state;
	}

	public void setState(State state)
	{
		this.state = state;
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

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public long getCompleted()
	{
		return completed;
	}

	public void setCompleted(long completed)
	{
		if (number == 10)
		{
			long prev = this.completed;
			double time = (System.currentTimeMillis() - timestamp) / 1000D;
			if (time < 1E-14)
				time = 0.1;
			speed = (completed - prev) / time;
			number = 0;
		}
		else
			number++;
		timestamp = System.currentTimeMillis();
		this.completed = completed;
	}

	public long getTotal()
	{
		return total;
	}

	public void setTotal(long total)
	{
		this.total = total;
	}

	public double percent()
	{
		if (completed < 0 && total < 0)
			return 0;
		return (double) completed / total * 100;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}
}
