package gui;

public class Formatters
{
	public static String formatSpeed(double l)
	{
		if (l < (1L << 10))
			return String.format("%1.2f", l) + " B/s";
		else if (l < (1L << 20))
			return String.format("%1.2f", (l / (1 << 10))) + " KB/s";
		else if (l < (1L << 30))
			return String.format("%1.2f", (l / (1 << 20))) + " MB/s";
		return String.format("%1.2f", (l / (1 << 30))) + " GB/s";
	}

	public static String formatSize(long l)
	{
		if (l < 0)
			return "";
		if (l < (1L << 10))
			return l + " B";
		else if (l < (1L << 20))
			return (l >> 10) + " KB";
		else if (l < (1L << 30))
			return (l >> 20) + " MB";
		else if (l < (1L << 40))
			return (l >> 30) + " GB";
		return (l >> 40) + " TB";
	}
}
