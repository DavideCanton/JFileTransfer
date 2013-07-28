package gui;

import java.net.*;
import java.util.*;

public class Comparators
{

	public static final Comparator<Integer> cInt = new Comparator<Integer>()
	{
		public int compare(Integer o1, Integer o2)
		{
			return o1.compareTo(o2);
		};
	};
	public static final Comparator<InetAddress> cIp = new Comparator<InetAddress>()
	{

		@Override
		public int compare(InetAddress o1, InetAddress o2)
		{
			return o1.getHostAddress().compareTo(o2.getHostAddress());
		}
	};
	public static final Comparator<Long> cLong = new Comparator<Long>()
	{

		@Override
		public int compare(Long o1, Long o2)
		{
			return o1.compareTo(o2);
		}
	};
}
