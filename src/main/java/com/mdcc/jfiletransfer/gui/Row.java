package com.mdcc.jfiletransfer.gui;

import java.awt.*;
import java.io.*;
import java.net.*;

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

public class Row
{
	public enum State
	{
		WAIT, TRANSFER, PAUSE, ERROR, FINISHED
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
