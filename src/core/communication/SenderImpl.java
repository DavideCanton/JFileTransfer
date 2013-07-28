package core.communication;

import java.io.*;
import java.net.*;
import core.communication.NetworkAnalyzer.AddressOrdering;
import core.observer.*;

public class SenderImpl extends AbstractCyclicActivityObservable implements Sender
{
	private InetAddress remoteIp, myIp;
	private int port;
	private File file;
	private long sent, total;
	private OutputStream toSocket;
	private FileInputStream fromFile;
	private Socket socket;
	private int SEND_TIMEOUT = 5000;
	private int bufferSize = BUFFER_SIZE;

	public SenderImpl(InetAddress ip, int port)
	{
		if (port <= 0 || port >= 65535)
			throw new IllegalArgumentException("Port not valid: " + port);
		this.remoteIp = ip;
		this.port = port;
		try
		{
			this.myIp = NetworkAnalyzer.getLocalAddress(AddressOrdering.IPV4_6);
		}
		catch (SocketException e)
		{
			try
			{
				this.myIp = InetAddress.getLocalHost();
			}
			catch (UnknownHostException e1)
			{
			}
		}
	}

	@Override
	public void setBufferSize(int bufferSize)
	{
		this.bufferSize = bufferSize;
	}

	@Override
	public void send(File file)
	{
		if (file == null || !file.exists())
			throw new IllegalArgumentException("File " + file + "not valid!");
		if (!file.isFile() || !file.canRead())
			throw new IllegalArgumentException(file + "it's not a file or it's not readable.");
		this.file = file;
		total = file.length();
		sent = 0;
		startActivity();
	}

	@Override
	protected ActivityEvent start()
	{
		try
		{
			fromFile = new FileInputStream(file);
			waitForSocket();
			socket.shutdownInput();
			toSocket = socket.getOutputStream();
			sendName(toSocket, file.getName());
			sendLength(toSocket, file.length());
			NetworkLimiter.getInstance().increaseConnections();
			return new ActivityEvent(myIp, remoteIp, file, port, total);
		}
		catch (Exception e)
		{
			cleanup();
			return new ErrorEvent(myIp, remoteIp, file, port, e);
		}
	}

	private void waitForSocket()
	{
		socket = new Socket();
		while (!socket.isConnected())
		{
			try
			{
				socket.connect(new InetSocketAddress(remoteIp, port));
			}
			catch (Exception e)
			{
				socket = new Socket();
				try
				{
					Thread.sleep(SEND_TIMEOUT);
				}
				catch (InterruptedException e1)
				{
				}
			}
		}
	}

	public int getSendTimeout()
	{
		return SEND_TIMEOUT;
	}

	public void setSendTimeout(int timeout)
	{
		SEND_TIMEOUT = timeout;
	}

	private static void sendLength(OutputStream out, long length) throws IOException
	{
		long n = Long.reverseBytes(length);
		for (int i = 0; i < 8; i++)
		{
			out.write((int) (n & 0xFF));
			n >>= 8;
		}
	}

	private static void sendName(OutputStream out, String name) throws IOException
	{
		out.write(name.length());
		out.write(name.getBytes());
	}

	@Override
	protected ActivityEvent end()
	{
		cleanup();
		return new ActivityEvent(myIp, remoteIp, file, port, total);
	}

	private void cleanup()
	{
		try
		{
			if (toSocket != null)
				toSocket.close();
			if (fromFile != null)
				fromFile.close();
			NetworkLimiter.getInstance().decreaseConnections();
		}
		catch (Exception e)
		{
		}
	}

	@Override
	protected ActivityEvent pause()
	{
		return new ActivityEvent(myIp, remoteIp, file, port, total);
	}

	@Override
	protected ActivityEvent resume()
	{
		return new ActivityEvent(myIp, remoteIp, file, port, total);
	}

	@Override
	protected ActivityEvent progress()
	{
		try
		{
			NetworkLimiter limiter = NetworkLimiter.getInstance();
			Thread.sleep(NetworkLimiter.TIME_TO_SLEEP);
			bufferSize = limiter.getBufferSize();
			byte[] buffer = new byte[bufferSize];
			int read = fromFile.read(buffer);
			sent += read;
			toSocket.write(buffer, 0, read);
			toSocket.flush();
			return new ProgressEvent(myIp, remoteIp, file, port, sent, total);
		}
		catch (Exception e)
		{
			return new ErrorEvent(myIp, remoteIp, file, port, e);
		}
	}

	@Override
	protected boolean endOfActivity()
	{
		return sent == total;
	}

	@Override
	protected ActivityEvent stop()
	{
		cleanup();
		return new ActivityEvent(myIp, remoteIp, file, port, total);
	}

	@Override
	protected ActivityEvent setup()
	{
		try
		{
			return new ActivityEvent(myIp, remoteIp, file, port, 0);
		}
		catch (Exception e)
		{
			cleanup();
			return new ErrorEvent(myIp, remoteIp, file, port, e);
		}
	}
}
