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