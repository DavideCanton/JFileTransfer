package com.mdcc.jfiletransfer.core.communication;

import java.io.*;
import java.net.*;
import com.mdcc.jfiletransfer.core.communication.NetworkAnalyzer.AddressOrdering;
import com.mdcc.jfiletransfer.core.observer.*;

/*
Copyright (c) 2014, Davide Canton
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. All advertising materials mentioning features or use of this software
   must display the following acknowledgement:
   This product includes software developed by the <organization>.
4. Neither the name of the <organization> nor the
   names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

public class SenderImpl extends AbstractCyclicActivityObservable implements Sender
{
	private final InetAddress remoteIp;
    private InetAddress myIp;
	private final int port;
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
			return new ErrorEvent(myIp, remoteIp, file, port, new RuntimeException(e));
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
			return new ErrorEvent(myIp, remoteIp, file, port, new RuntimeException(e));
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
			return new ErrorEvent(myIp, remoteIp, file, port, new RuntimeException(e));
		}
	}
}
