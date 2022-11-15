package com.mdcc.jfiletransfer.gui;

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

import java.awt.*;
import java.io.*;
import javax.imageio.*;

public class ImageHandler
{
	public static final int IMAGE_SIZE = 50;
	private static Image ok, pause, play, error, wait, send, receive, icon;

	public static Image getIcon()
	{
		try
		{
			if (icon == null)
				icon = ImageIO.read(ImageHandler.class.getResource("img/icon.png"));
		}
		catch (IOException e)
		{
		}
		return icon;
	}

	public static Image getError()
	{
		try
		{
			if (error == null)
				error = ImageIO.read(ImageHandler.class.getResource("img/error.png")).getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
		}
		catch (IOException e)
		{
		}
		return error;
	}

	public static Image getOk()
	{
		try
		{
			if (ok == null)
				ok = ImageIO.read(ImageHandler.class.getResource("img/ok.png")).getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
		}
		catch (IOException e)
		{
		}
		return ok;
	}

	public static Image getPause()
	{
		try
		{
			if (pause == null)
				pause = ImageIO.read(ImageHandler.class.getResource("img/pause.png")).getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
		}
		catch (IOException e)
		{
		}
		return pause;
	}

	public static Image getPlay()
	{
		try
		{
			if (play == null)
				play = ImageIO.read(ImageHandler.class.getResource("img/play.png")).getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
		}
		catch (IOException e)
		{
		}
		return play;
	}

	public static Image getWait()
	{
		try
		{
			if (wait == null)
				wait = ImageIO.read(ImageHandler.class.getResource("img/wait.png")).getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
		}
		catch (IOException e)
		{
		}
		return wait;
	}

	public static Image getSend()
	{
		try
		{
			if (send == null)
				send = ImageIO.read(ImageHandler.class.getResource("img/send.png")).getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
		}
		catch (IOException e)
		{
		}
		return send;
	}

	public static Image getReceive()
	{
		try
		{
			if (receive == null)
				receive = ImageIO.read(ImageHandler.class.getResource("img/receive.png")).getScaledInstance(IMAGE_SIZE, IMAGE_SIZE, Image.SCALE_SMOOTH);
		}
		catch (IOException e)
		{
		}
		return receive;
	}
}
