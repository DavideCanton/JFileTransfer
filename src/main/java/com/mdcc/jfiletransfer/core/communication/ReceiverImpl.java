package com.mdcc.jfiletransfer.core.communication;

import java.io.*;
import java.net.*;
import com.mdcc.jfiletransfer.core.communication.NetworkAnalyzer.*;
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

public class ReceiverImpl extends AbstractCyclicActivityObservable implements Receiver
{
	private InetAddress ip, myIp;
	private final int port;
	private File directory, destination;
	private long received, total;
	private InputStream fromSocket;
	private FileOutputStream toFile;
	private ServerSocket ss;
	private boolean inProgress = false;

	public ReceiverImpl(int port)
	{
		if (port <= 0 || port >= 65535)
			throw new IllegalArgumentException("Port not valid: " + port);
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
	public void listen(File directory)
	{
		if (directory == null || !directory.exists())
			throw new IllegalArgumentException("Directory " + directory + " not valid.");
		if (!directory.isDirectory() || !directory.canWrite())
			throw new IllegalArgumentException("Directory " + directory + " is not a directory or it isn't writable.");
		this.directory = directory;
		startActivity();
	}

	@Override
	protected ActivityEvent setup()
	{
		return new ActivityEvent(null, myIp, directory, port, 0);
	}

	@Override
	protected ActivityEvent start()
	{
		try
		{
			ss = new ServerSocket(port);
			Socket s = ss.accept();
			ss.close();
			s.shutdownOutput();
			ip = s.getInetAddress();
			fromSocket = s.getInputStream();
			String name = readName(fromSocket);
			total = readLength(fromSocket);
			destination = createFile(directory, name);
			toFile = new FileOutputStream(destination);
			NetworkLimiter.getInstance().increaseConnections();			
			inProgress = true;
			return new ActivityEvent(ip, myIp, destination, port, total);
		}
		catch (Exception e)
		{
			cleanup();
			return new ErrorEvent(ip, myIp, destination, port, new RuntimeException(e));
		}
	}

	private void cleanup()
	{
		try
		{
			if (ss != null)
				ss.close();
			if (fromSocket != null)
				fromSocket.close();
			if (toFile != null)
				toFile.close();
			NetworkLimiter.getInstance().decreaseConnections();
		}
		catch (Exception e)
		{
		}
	}

	private static File createFile(File dir, String name)
	{
		File dest = new File(dir, name);
		int i = 0;
		int index = name.lastIndexOf('.');
		String ext = "";
		if (index >= 0)
		{
			ext = "." + name.substring(index + 1);
			name = name.substring(0, index);
		}
		while (dest.exists())
		{
			String name2 = name + "_" + (i++) + ext;
			dest = new File(dir, name2);
		}
		return dest;
	}

	private static long readLength(InputStream is) throws IOException
	{
		long length = 0;
		for (int i = 0; i < 8; i++)
		{
			int b = is.read();
			if (b < 0)
				throw new IOException("Invalid length");
			length <<= 8;
			length |= b & 0xFF;
		}
		return length;
	}

	private static String readName(InputStream i) throws IOException
	{
		int size = i.read();
		if (size <= 0)
			throw new IOException("Invalid size: " + size);
		int read = 0;
		byte[] buffer = new byte[size];
		while (read < size)
		{
			int r = i.read();
			if (r < 0)
				throw new IOException("End of input");
			buffer[read++] = (byte) r;
		}
		return new String(buffer);
	}

	@Override
	protected ActivityEvent end()
	{
		inProgress = false;
		cleanup();
		return new ActivityEvent(ip, myIp, destination, port, total);
	}

	@Override
	protected ActivityEvent pause()
	{
		return new ActivityEvent(ip, myIp, destination, port, total);
	}

	@Override
	protected ActivityEvent resume()
	{
		return new ActivityEvent(ip, myIp, destination, port, total);
	}

	@Override
	protected ActivityEvent progress()
	{
		try
		{
			byte[] buffer = new byte[BUFFER_SIZE];
			int read = fromSocket.read(buffer);
			if (read < 0)
				throw new IOException("End of stream");
			received += read;
			toFile.write(buffer, 0, read);
			toFile.flush();
			return new ProgressEvent(ip, myIp, destination, port, received, total);
		}
		catch (Exception e)
		{
			inProgress = false;
			destination.delete();
			return new ErrorEvent(ip, myIp, destination, port, new RuntimeException(e));
		}
	}

	@Override
	protected boolean endOfActivity()
	{
		return received == total;
	}

	@Override
	protected ActivityEvent stop()
	{
		cleanup();
		if (inProgress && destination != null && destination.exists())
			destination.delete();		
		return new ActivityEvent(ip, myIp, destination, port, total);
	}
}
