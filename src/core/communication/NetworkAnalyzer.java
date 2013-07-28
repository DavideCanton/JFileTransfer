package core.communication;

import java.io.*;
import java.net.*;
import java.util.*;

public class NetworkAnalyzer
{
	/**
	 * Represents an ordering of {@link InetAddress}.
	 * 
	 * @author davide
	 */
	public static enum AddressOrdering
	{
		/**
		 * IPV4 ONLY
		 */
		IPV4,
		/**
		 * IPV6 ONLY
		 */
		IPV6,
		/**
		 * IPV4 PREFERRED
		 */
		IPV4_6,
		/**
		 * IPV6 PREFERRED
		 */
		IPV6_4;

		private static Comparator<InetAddress> ipv4first = new Comparator<InetAddress>()
		{

			@Override
			public int compare(InetAddress o1, InetAddress o2)
			{
				if (o1 instanceof Inet4Address)
					return o2 instanceof Inet4Address ? o1.getHostAddress().compareTo(o2.getHostAddress()) : -1;
				return o2 instanceof Inet4Address ? 1 : o1.getHostAddress().compareTo(o2.getHostAddress());
			}
		};

		private static Comparator<InetAddress> ipv6first = new Comparator<InetAddress>()
		{

			@Override
			public int compare(InetAddress o1, InetAddress o2)
			{
				if (o1 instanceof Inet6Address)
					return o2 instanceof Inet6Address ? o1.getHostAddress().compareTo(o2.getHostAddress()) : -1;
				return o2 instanceof Inet6Address ? 1 : o1.getHostAddress().compareTo(o2.getHostAddress());
			}
		};

		public Comparator<? super InetAddress> comparator()
		{
			return (this == IPV4 || this == IPV4_6) ? ipv4first : ipv6first;
		}
	}

	/**
	 * Retrieves the list of the IP addresses of the local node (according to the type specified).
	 * 
	 * @param ordering
	 *            The ordering of IP addresses.
	 * @return The collection of IP addresses.
	 * @throws SocketException
	 *             If the lookup fails.
	 */
	public static Collection<InetAddress> getLocalAddresses(AddressOrdering ordering) throws SocketException
	{
		Enumeration<NetworkInterface> allInterfaces = NetworkInterface.getNetworkInterfaces();
		NetworkInterface loopback = null;
		Collection<InetAddress> addresses = new TreeSet<InetAddress>(ordering.comparator());
		while (allInterfaces != null && allInterfaces.hasMoreElements())
		{
			NetworkInterface currentInterface = allInterfaces.nextElement();
			if (!currentInterface.isLoopback())
			{
				Collection<InetAddress> addressesNet = process(currentInterface, ordering);
				if (!addressesNet.isEmpty())
					addresses.addAll(addressesNet);
			}
			else
				loopback = currentInterface;
		}
		addExternalIP(addresses);
		return addresses.isEmpty() ? process(loopback, ordering) : addresses;
	}

	private static void addExternalIP(Collection<InetAddress> addresses)
	{
		try
		{
			HttpURLConnection conn = (HttpURLConnection) new URL("http://automation.whatismyip.com/n09230945.asp").openConnection();
			BufferedReader b = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String s = b.readLine();
			conn.disconnect();
			addresses.add(InetAddress.getByName(s));			
		}
		catch (Exception e)
		{
		}

	}

	/**
	 * Retrieves the first IP address of the local node (according to the type specified).
	 * 
	 * @param ordering
	 *            The ordering of IP addresses.
	 * @return The IP address.
	 * @throws SocketException
	 *             If the lookup fails.
	 */
	public static InetAddress getLocalAddress(AddressOrdering ordering) throws SocketException
	{
		return getLocalAddresses(ordering).iterator().next();
	}

	private static Collection<InetAddress> process(NetworkInterface n, AddressOrdering ordering)
	{
		List<InetAddress> s = new LinkedList<InetAddress>();
		List<InterfaceAddress> l = n.getInterfaceAddresses();
		for (InterfaceAddress i : l)
		{
			if (i.getAddress() instanceof Inet4Address && ordering != AddressOrdering.IPV6)
				s.add(i.getAddress());
			if (i.getAddress() instanceof Inet6Address && ordering != AddressOrdering.IPV4)
				s.add(i.getAddress());
		}
		return s;
	}
}
