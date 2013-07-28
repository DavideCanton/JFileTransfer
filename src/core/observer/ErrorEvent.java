package core.observer;

import java.io.*;
import java.net.*;

public class ErrorEvent extends ActivityEvent
{
	private Exception error;

	public ErrorEvent(InetAddress from, InetAddress to, File file, int port, Exception error)
	{
		super(from, to, file, port, 0);
		this.error = error;
		setType(EventType.ERROR);
	}

	public Exception getError()
	{
		return error;
	}
}
