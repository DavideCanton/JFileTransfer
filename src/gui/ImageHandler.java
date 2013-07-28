package gui;

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
