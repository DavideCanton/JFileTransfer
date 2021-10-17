package com.mdcc.jfiletransfer.core.communication;

import java.io.*;
import java.net.*;
import java.util.*;

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

public class NetworkAnalyzer
{
    /**
     * Represents an ordering of {@link InetAddress}.
     *
     * @author davide
     */
    public enum AddressOrdering
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

        private static final Comparator<InetAddress> ipv4first = (o1, o2) ->
        {
            if (o1 instanceof Inet4Address)
                return o2 instanceof Inet4Address ? o1.getHostAddress().compareTo(o2.getHostAddress()) : -1;
            else
                return o2 instanceof Inet4Address ? 1 : o1.getHostAddress().compareTo(o2.getHostAddress());
        };

        private static final Comparator<InetAddress> ipv6first = (o1, o2) ->
        {
            if (o1 instanceof Inet6Address)
                return o2 instanceof Inet6Address ? o1.getHostAddress().compareTo(o2.getHostAddress()) : -1;
            else
                return o2 instanceof Inet6Address ? 1 : o1.getHostAddress().compareTo(o2.getHostAddress());
        };

        public Comparator<? super InetAddress> comparator()
        {
            return this == IPV4 || this == IPV4_6 ? ipv4first : ipv6first;
        }
    }

    /**
     * Retrieves the list of the IP addresses of the local node (according to the type specified).
     *
     * @param ordering The ordering of IP addresses.
     * @return The collection of IP addresses.
     * @throws SocketException If the lookup fails.
     */
    public static Collection<InetAddress> getLocalAddresses(AddressOrdering ordering) throws SocketException
    {
        Enumeration<NetworkInterface> allInterfaces = NetworkInterface.getNetworkInterfaces();
        NetworkInterface loopback = null;
        Collection<InetAddress> addresses = new TreeSet<>(ordering.comparator());
        while (allInterfaces != null && allInterfaces.hasMoreElements())
        {
            NetworkInterface currentInterface = allInterfaces.nextElement();
            if (!currentInterface.isLoopback())
            {
                Collection<InetAddress> addressesNet = process(currentInterface, ordering);
                if (!addressesNet.isEmpty())
                    addresses.addAll(addressesNet);
            } else
                loopback = currentInterface;
        }
        Objects.requireNonNull(loopback);
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
        } catch (Exception e)
        {
        }

    }

    /**
     * Retrieves the first IP address of the local node (according to the type specified).
     *
     * @param ordering The ordering of IP addresses.
     * @return The IP address.
     * @throws SocketException If the lookup fails.
     */
    public static InetAddress getLocalAddress(AddressOrdering ordering) throws SocketException
    {
        return getLocalAddresses(ordering).iterator().next();
    }

    private static Collection<InetAddress> process(NetworkInterface n, AddressOrdering ordering)
    {
        List<InetAddress> s = new LinkedList<>();
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
