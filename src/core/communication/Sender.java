package core.communication;

import java.io.*;
import core.observer.*;

public interface Sender extends ActivityObservable, Communicator
{
	public void send(File file);

	void setBufferSize(int bufferSize);
	
}
