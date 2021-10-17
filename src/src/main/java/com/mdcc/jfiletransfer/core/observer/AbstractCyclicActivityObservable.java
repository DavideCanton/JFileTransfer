package com.mdcc.jfiletransfer.core.observer;

import java.util.*;
import java.util.concurrent.locks.*;
import com.mdcc.jfiletransfer.core.observer.ActivityEvent.EventType;

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

public abstract class AbstractCyclicActivityObservable implements ActivityObservable
{
	protected final Set<ActivityObserver> observers = new HashSet<>();
	private boolean paused = false;
	private WorkerThread worker;
	private final Lock lock = new ReentrantLock();
	private final Condition pausedC = lock.newCondition();

	@Override
	public void addObserver(ActivityObserver o)
	{
		observers.add(o);
	}

	@Override
	public void removeObserver(ActivityObserver o)
	{
		observers.remove(o);
	}

	protected abstract ActivityEvent start();

	protected abstract ActivityEvent end();

	protected abstract ActivityEvent pause();

	protected abstract ActivityEvent resume();

	protected abstract ActivityEvent progress();

	protected abstract ActivityEvent stop();

	protected abstract boolean endOfActivity();

	protected abstract ActivityEvent setup();

	@Override
	public void startActivity()
	{
		ActivityEvent e = setup();
		if (e != null)
		{
			e.setType(EventType.SETUP);
			for (ActivityObserver o : observers)
				o.notifySetup(e, this);
		}
		worker = new WorkerThread();
		worker.start();
	}

	@Override
	public void stopActivity()
	{
		if (worker != null)
			worker.interrupt();
		stop();
	}

	@Override
	public void pauseActivity()
	{
		ActivityEvent e = pause();
		e.setType(EventType.PAUSE);
		for (ActivityObserver o : observers)
			o.notifyPause(e, this);
		if (!(e instanceof ErrorEvent))
		{
			lock.lock();
			paused = true;
			lock.unlock();
		}
		else
			throw ((ErrorEvent) e).getError();
	}

	@Override
	public void resumeActivity()
	{
		ActivityEvent e = resume();
		e.setType(EventType.RESUME);
		for (ActivityObserver o : observers)
			o.notifyResume(e, this);
		if (!(e instanceof ErrorEvent))
		{
			lock.lock();
			paused = false;
			pausedC.signal();
			lock.unlock();
		}
		else
			throw ((ErrorEvent) e).getError();
	}

	private class WorkerThread extends Thread
	{

		@Override
		public void run()
		{
			ErrorEvent error = null;
			ActivityEvent e = AbstractCyclicActivityObservable.this.start();
			if (!(e instanceof ErrorEvent))
			{
				e.setType(EventType.START);
				for (ActivityObserver o : observers)
					o.notifyStart(e, AbstractCyclicActivityObservable.this);
				while (error == null && !endOfActivity() && !isInterrupted())
				{
					try
					{
						waitIfPaused();
					}
					catch (InterruptedException ex)
					{
						break;
					}
					e = progress();
					if (e instanceof ProgressEvent)
						for (ActivityObserver o : observers)
							o.notifyProgress((ProgressEvent) e, AbstractCyclicActivityObservable.this);
					else
						error = (ErrorEvent) e;
				}
				if (!isInterrupted())
				{
					ActivityEvent ev = error != null ? error : end();
					ev.setType(EventType.END);
					for (ActivityObserver o : observers)
						o.notifyEnd(ev, AbstractCyclicActivityObservable.this);
				}
				else
				{
					ActivityEvent ev = error != null ? error : AbstractCyclicActivityObservable.this.stop();
					ev.setType(EventType.STOP);
					for (ActivityObserver o : observers)
						o.notifyStop(ev, AbstractCyclicActivityObservable.this);
				}
			}
			else
				for (ActivityObserver o : observers)
					o.notifyStart(e, AbstractCyclicActivityObservable.this); // con l'errore
		}

		private void waitIfPaused() throws InterruptedException
		{
			lock.lock();
			try
			{
				while (paused)
					pausedC.await();
			}
			finally
			{
				lock.unlock();
			}
		}
	}
}
