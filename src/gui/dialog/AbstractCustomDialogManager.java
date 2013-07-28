package gui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.concurrent.locks.*;
import javax.swing.*;

public abstract class AbstractCustomDialogManager
{
	private Lock lock = new ReentrantLock();
	private Condition closedC = lock.newCondition();
	private boolean closed = false;
	private JDialog dialog;
	private JOptionPane pane;

	public void run(Frame parent, String title)
	{
		pane = new JOptionPane(centerPanel(), JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		dialog = pane.createDialog(parent, title);
		dialog.setModal(true);
		dialog.setSize(400, 300);
		dialog.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				cancel(dialog);
			}
		});
		pane.addPropertyChangeListener(JOptionPane.VALUE_PROPERTY, new PropertyChangeListener()
		{

			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				if (evt.getNewValue() == null || !(evt.getNewValue() instanceof Integer))
					return;
				int value = (Integer) evt.getNewValue();
				if (value == JOptionPane.CANCEL_OPTION)
					cancel(dialog);
				else
					ok(dialog);
			}
		});
		dialog.pack();
		dialog.setVisible(true);
		lock.lock();
		try
		{
			while (!closed)
				try
				{
					closedC.await();
				}
				catch (InterruptedException e)
				{
					break;
				}
		}
		finally
		{
			lock.unlock();
		}
	}

	protected abstract void ok(JDialog dialog);

	protected abstract void cancel(JDialog dialog);

	protected abstract JPanel centerPanel();

	public void dispose()
	{
		dialog.dispose();
		lock.lock();
		closed = true;
		closedC.signal();
		lock.unlock();
	}
}
