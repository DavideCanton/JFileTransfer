package core.communication;

import java.io.*;
import core.observer.*;

public interface Receiver extends ActivityObservable, Communicator
{
	public void listen(File directory);
}
