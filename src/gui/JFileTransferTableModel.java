package gui;

import gui.Row.State;
import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import core.communication.*;
import core.observer.*;

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

public class JFileTransferTableModel extends AbstractTableModel implements ActivityObserver
{
	private static final int COLUMNS = 10;

	public static final int DIRECTION = 0;
	public static final int STATUS = 1;
	public static final int FILE = 2;
	public static final int FROM = 3;
	public static final int TO = 4;
	public static final int PORT = 5;
	public static final int COMPLETED = 6;
	public static final int TOTAL = 7;
	public static final int SPEED = 8;
	public static final int PROGRESS = 9;

	private java.util.List<Pair> rows = new ArrayList<JFileTransferTableModel.Pair>();

	public boolean allCompleted()
	{
		for (Pair p : rows)
			if (p.row.getState() != State.ERROR && p.row.getState() != State.FINISHED)
				return false;
		return true;
	}

	private void error(ErrorEvent e, Pair p)
	{
		p.row.setState(State.ERROR);
		p.row.setMessage(e.getError().getLocalizedMessage());
	}

	@Override
	public Class<?> getColumnClass(int columnIndex)
	{
		if (columnIndex == STATUS || columnIndex == DIRECTION)
			return ImageIcon.class;
		if (columnIndex == PROGRESS || columnIndex == SPEED)
			return Double.class;
		if (columnIndex == COMPLETED || columnIndex == TOTAL)
			return Long.class;
		return String.class;
	}

	@Override
	public int getColumnCount()
	{
		return COLUMNS;
	}

	@Override
	public String getColumnName(int column)
	{
		switch (column)
		{
			case DIRECTION:
				return "Direzione";
			case STATUS:
				return "Stato";
			case FILE:
				return "File";
			case FROM:
				return "Indirizzo sorgente";
			case TO:
				return "Indirizzo destinazione";
			case PORT:
				return "Porta";
			case COMPLETED:
				return "Completati";
			case TOTAL:
				return "Totali";
			case SPEED:
				return "Velocita'";
			case PROGRESS:
				return "Avanzamento";
			default:
				return "";
		}
	}

	public File getFile(int selectedRow)
	{
		Pair p = rows.get(selectedRow);
		return p.row.getFile();
	}

	@Override
	public int getRowCount()
	{
		return rows.size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex)
	{
		if (rowIndex < rows.size())
		{
			Pair p = rows.get(rowIndex);
			switch (columnIndex)
			{
				case DIRECTION:
				{
					ImageIcon image;
					if (p.activity instanceof SenderImpl)
						image = new ImageIcon(ImageHandler.getSend());
					else
						image = new ImageIcon(ImageHandler.getReceive());
					image.setDescription("");
					return image;
				}
				case STATUS:
				{
					ImageIcon image = null;
					switch (p.row.getState())
					{
						case ERROR:
							image = new ImageIcon(ImageHandler.getError());
							break;
						case FINISHED:
							image = new ImageIcon(ImageHandler.getOk());
							break;
						case PAUSE:
							image = new ImageIcon(ImageHandler.getPause());
							break;
						case TRANSFER:
							image = new ImageIcon(ImageHandler.getPlay());
							break;
						case WAIT:
							image = new ImageIcon(ImageHandler.getWait());
							break;
						default:
							return null;
					}
					image.setDescription(p.row.getMessage());
					return image;
				}
				case FILE:
					return p.row.getFile() == null ? "" : (p.row.getFile().isFile() ? p.row.getFile().getName() : p.row.getFile().getPath());
				case FROM:
					return p.row.getFrom();
				case TO:
					return p.row.getTo();
				case PORT:
					return p.row.getPort();
				case COMPLETED:
					return p.row.getCompleted();
				case TOTAL:
					return p.row.getTotal();
				case SPEED:
					return p.row.getSpeed();
				case PROGRESS:
					return p.row.percent();
				default:
					return null;
			}
		}
		return null;
	}

	@Override
	public void notifyEnd(ActivityEvent e, ActivityObservable ob)
	{
		int r = search(ob);
		if (r >= 0)
		{
			Pair p = rows.get(r);
			if (e instanceof ErrorEvent)
				error((ErrorEvent) e, p);
			else
			{
				p.row.setState(State.FINISHED);
				p.row.setMessage("Completato");
				try
				{
					fireTableRowsUpdated(r, r);
				}
				catch (Exception ex)
				{
				}
			}
		}
	}

	@Override
	public void notifyPause(ActivityEvent e, ActivityObservable ob)
	{
		int r = search(ob);
		if (r >= 0)
		{
			Pair p = rows.get(r);
			if (e instanceof ErrorEvent)
				error((ErrorEvent) e, p);
			else
			{
				p.row.setState(State.PAUSE);
				p.row.setMessage("In pausa");
				fireTableRowsUpdated(r, r);
			}
		}
	}

	@Override
	public void notifyProgress(final ProgressEvent e, ActivityObservable ob)
	{
		final int r = search(ob);
		if (r >= 0)
		{
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					Row row = JFileTransferTableModel.this.rows.get(r).row;
					row.setCompleted(e.getCompleted());
					fireTableRowsUpdated(r, r);
				}
			});
		}
	}

	@Override
	public void notifyResume(ActivityEvent e, ActivityObservable ob)
	{
		int r = search(ob);
		if (r >= 0)
		{
			Pair p = rows.get(r);
			if (e instanceof ErrorEvent)
				error((ErrorEvent) e, p);
			else
			{
				p.row.setState(State.TRANSFER);
				p.row.setMessage("Trasferimento...");
				fireTableRowsUpdated(r, r);
			}
		}
	}

	@Override
	public void notifySetup(ActivityEvent e, ActivityObservable ob)
	{
		Pair p = new Pair();
		Row r = new Row();
		r.setCompleted(-1);
		r.setPort(e.getPort());
		r.setTo(e.getTo());
		r.setTotal(-1);
		r.setFile(e.getFile());
		r.setFrom(e.getFrom());
		r.setState(State.WAIT);
		r.setMessage("In attesa...");
		p.activity = ob;
		p.row = r;
		rows.add(p);
		int oldSize = getRowCount() - 1;
		fireTableRowsInserted(oldSize, oldSize);
		if (e instanceof ErrorEvent)
			error((ErrorEvent) e, p);
	}

	@Override
	public void notifyStart(ActivityEvent e, ActivityObservable ob)
	{
		int r = search(ob);
		if (r >= 0)
		{
			Pair p = rows.get(r);
			if (e instanceof ErrorEvent)
				error((ErrorEvent) e, p);
			else
			{
				p.row.setState(State.TRANSFER);
				p.row.setCompleted(0);
				p.row.setTotal(e.getTotal());
				p.row.setFile(e.getFile());
				p.row.setMessage("Trasferimento...");
				if (p.activity instanceof Receiver)
					p.row.setFrom(e.getFrom());
				fireTableRowsUpdated(r, r);
			}
		}
	}

	@Override
	public void notifyStop(ActivityEvent ev, ActivityObservable ob)
	{
		int r = search(ob);
		if (r >= 0)
		{
			Pair p = rows.get(r);
			p.row.setState(State.ERROR);
			p.row.setMessage("Interrotto");
			if (ev instanceof ErrorEvent)
				error((ErrorEvent) ev, p);
			fireTableRowsUpdated(r, r);
		}
	}

	public boolean pausable(int row)
	{
		Pair p = rows.get(row);
		return p.row.getState() == State.TRANSFER && p.activity instanceof SenderImpl;
	}

	public void pause(int selectedRow)
	{
		Pair p = rows.get(selectedRow);
		try
		{
			p.activity.pauseActivity();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Can't pause activity: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void remove(int rowIndex)
	{
		Iterator<Pair> it = rows.iterator();
		int index = 0;
		while (it.hasNext())
		{
			Pair p = it.next();
			if (index == rowIndex)
			{
				if (p.row.getState() != Row.State.FINISHED && p.row.getState() != Row.State.ERROR)
					p.activity.stopActivity();
				it.remove();
				break;
			}
			index++;
		}
		fireTableDataChanged();
	}

	public void removeAll()
	{
		if (getRowCount() > 0)
		{
			ListIterator<Pair> it = rows.listIterator(rows.size());
			while (it.hasPrevious())
			{
				Pair p = it.previous();
				p.activity.stopActivity();
			}
		}
		rows.clear();
		fireTableDataChanged();
	}

	public void removeCompleted()
	{
		ListIterator<Pair> it = rows.listIterator(rows.size());
		while (it.hasPrevious())
		{
			Pair p = it.previous();
			if (p.row.getState() == State.ERROR || p.row.getState() == State.FINISHED)
				it.remove();
		}
		fireTableDataChanged();
	}

	public boolean resumable(int row)
	{
		Pair p = rows.get(row);
		return p.row.getState() == State.PAUSE && p.activity instanceof SenderImpl;
	}

	public void resume(int selectedRow)
	{
		Pair p = rows.get(selectedRow);
		try
		{
			p.activity.resumeActivity();
		}
		catch (Exception e)
		{
			JOptionPane.showMessageDialog(null, "Can't resume activity: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private int search(ActivityObservable ob)
	{
		int i = 0;
		for (Pair p : rows)
			if (p.activity == ob)
				return i;
			else
				i++;
		return -1;
	}

	private static class Pair
	{
		ActivityObservable activity;
		Row row;
	}
}
