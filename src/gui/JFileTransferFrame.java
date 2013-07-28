package gui;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import org.jdesktop.swingx.*;
import org.jdesktop.swingx.decorator.*;
import core.observer.*;

public class JFileTransferFrame extends JFrame
{
	private static final int TOOLBAR_SIZE = 30;
	private LinkedList<BarPanel> barPanels = new LinkedList<BarPanel>();
	private EsciListener esciListener = new EsciListener();
	private JFileTransferTableModel model = new JFileTransferTableModel();
	private JButton pauseResumeButton;
	private JPopupMenu popup = new JPopupMenu();
	private JMenuItem removeAll, openLocation, remove, pauseResume;
	private ActionListener sendListener = new SendListener(),
			receiveListener = new ReceiveListener(),
			pauseResumeListener = new PauseResumeListener();
	private JXTable table;
	private Mediator mediator = new Mediator();
	private TransferHandler transferHandler = new FileTransferHandler();
	private StatusBar statusbar = new StatusBar();

	public JFileTransferFrame()
	{
		menu();
		popup();
		toolbar();
		mediator.makePause();
		mediator.setPauseEnabled(false);
		addTable();
		addWindowListener(esciListener);
		add(statusbar, BorderLayout.SOUTH);
		setTransferHandler(transferHandler);
		setSize(800, 600);
		setIconImage(ImageHandler.getIcon());
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setTitle("JFileTransfer");
	}

	public void addObservable(ActivityObservable ob)
	{
		barPanels.add(new BarPanel());
		ob.addObserver(model);
	}

	private void addTable()
	{
		table = new JXTable();
		table.setModel(model);
		sorter();
		table.setAutoCreateRowSorter(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setRowHeight(ImageHandler.IMAGE_SIZE);
		table.setShowGrid(true, false);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getColumn(JFileTransferTableModel.PROGRESS).setCellRenderer(new BarCellRenderer());
		table.getColumn(JFileTransferTableModel.SPEED).setCellRenderer(new SpeedRenderer());
		table.setDefaultRenderer(String.class, new StringRenderer());
		table.setDefaultRenderer(Long.class, new LongRenderer());
		table.setDefaultRenderer(ImageIcon.class, new ImageRenderer());
		table.setHorizontalScrollEnabled(true);
		table.setTransferHandler(transferHandler);
		((JLabel) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
		model.addTableModelListener(new TableModelListener()
		{
			@Override
			public void tableChanged(TableModelEvent e)
			{
				table.repaint();
				table.packTable(10);
			}
		});

		Highlighter simpleStriping = HighlighterFactory.createSimpleStriping();
		PatternPredicate patternPredicate = new PatternPredicate("ˆM", 1);
		ColorHighlighter magenta = new ColorHighlighter(patternPredicate, null, Color.MAGENTA, null, Color.MAGENTA);
		Highlighter shading = new ShadingColorHighlighter(new HighlightPredicate.RowGroupHighlightPredicate(1));
		table.setHighlighters(simpleStriping, magenta, shading);

		table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				popup(e);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				popup(e);
			}

			public void popup(MouseEvent e)
			{
				int r = table.rowAtPoint(e.getPoint());
				if (r >= 0 && r < table.getRowCount())
				{
					table.setRowSelectionInterval(r, r);
					if (model.pausable(r))
					{
						mediator.makePause();
						mediator.setPauseEnabled(true);
					}
					else if (model.resumable(r))
					{
						mediator.makeResume();
						mediator.setPauseEnabled(true);
					}
					else
						mediator.setPauseEnabled(false);
					mediator.setSelectedRowEnabled(true);
				}
				else
				{
					table.clearSelection();
					mediator.setSelectedRowEnabled(false);
					mediator.setPauseEnabled(false);
				}
				if (e.isPopupTrigger())
					popup.show((Component) e.getSource(), e.getX(), e.getY());
			}
		});
		add(new JScrollPane(table));
	}

	protected boolean ask()
	{
		if (!model.allCompleted())
			return JOptionPane.showConfirmDialog(null, "Ci sono trasferimenti attivi. Vuoi uscire?", "Uscita", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
		return true;
	}

	private void menu()
	{
		JMenuBar bar = new JMenuBar();
		setJMenuBar(bar);
		JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic('f');
		bar.add(menuFile);
		JMenuItem addSender = new JMenuItem("Invia file...");
		menuFile.add(addSender);
		addSender.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK));
		addSender.addActionListener(sendListener);
		JMenuItem addReceiver = new JMenuItem("Ricevi file...");
		menuFile.add(addReceiver);
		addReceiver.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.ALT_DOWN_MASK));
		addReceiver.addActionListener(receiveListener);
		menuFile.addSeparator();
		JMenuItem esci = new JMenuItem("Esci");
		menuFile.add(esci);
		esci.addActionListener(esciListener);
		JMenu menuInfo = new JMenu("Aiuto");
		menuInfo.setMnemonic('a');
		bar.add(menuInfo);
		JMenuItem about = new JMenuItem("About");
		menuInfo.add(about);
		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.ALT_DOWN_MASK));
		about.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				JOptionPane.showMessageDialog(JFileTransferFrame.this, "Programma realizzato da Davide Canton per il trasferimento di file attraverso la rete.");
			}
		});
	}

	private void popup()
	{
		remove = new JMenuItem("Rimuovi");
		popup.add(remove);
		remove.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				model.remove(table.getSelectedRow());
			}
		});
		removeAll = new JMenuItem("Rimuovi completati");
		popup.add(removeAll);
		removeAll.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				model.removeCompleted();
			}
		});
		if (Desktop.isDesktopSupported())
		{
			openLocation = new JMenuItem("Apri cartella file");
			popup.add(openLocation);
			openLocation.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					File file = model.getFile(table.getSelectedRow());
					if (file != null)
					{
						try
						{
							Desktop desktop = Desktop.getDesktop();
							desktop.open(file.isDirectory() ? file : file.getParentFile());
						}
						catch (IOException e1)
						{
						}
					}
				}
			});
		}
		pauseResume = new JMenuItem("Pausa");
		popup.add(pauseResume);
		pauseResume.addActionListener(pauseResumeListener);
	}

	private void sorter()
	{
		table.getColumnExt(JFileTransferTableModel.PORT).setComparator(Comparators.cInt);
		table.getColumnExt(JFileTransferTableModel.COMPLETED).setComparator(Comparators.cLong);
		table.getColumnExt(JFileTransferTableModel.TOTAL).setComparator(Comparators.cLong);
		table.getColumnExt(JFileTransferTableModel.FROM).setComparator(Comparators.cIp);
		table.getColumnExt(JFileTransferTableModel.TO).setComparator(Comparators.cIp);
	}

	private void toolbar()
	{
		JToolBar toolbar = new JToolBar();
		JButton addSender = new JButton();
		JButton addReceiver = new JButton();
		pauseResumeButton = new JButton();
		addSender.addActionListener(sendListener);
		addSender.setIcon(new ImageIcon(ImageHandler.getSend().getScaledInstance(TOOLBAR_SIZE, TOOLBAR_SIZE, Image.SCALE_SMOOTH)));
		addSender.setToolTipText("Invia file...");
		addReceiver.addActionListener(receiveListener);
		addReceiver.setIcon(new ImageIcon(ImageHandler.getReceive().getScaledInstance(TOOLBAR_SIZE, TOOLBAR_SIZE, Image.SCALE_SMOOTH)));
		addReceiver.setToolTipText("Ricevi file...");
		pauseResumeButton.addActionListener(pauseResumeListener);
		toolbar.add(addSender);
		toolbar.add(addReceiver);
		toolbar.add(pauseResumeButton);
		toolbar.setFloatable(false);
		add(toolbar, BorderLayout.NORTH);
	}

	private class BarCellRenderer extends DefaultTableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			BarPanel panel = barPanels.get(row);
			panel.getBar().setValue(((Double) value).intValue());
			panel.setBackground(super.getBackground());
			return panel;
		}
	}

	private class SpeedRenderer extends DefaultTableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			Double d = (Double) value;
			if (d >= 0)
				setText(Formatters.formatSpeed(d));
			else
				setText("");
			setHorizontalAlignment(CENTER);
			return this;
		}
	}

	private class EsciListener extends WindowAdapter implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			esci();
		}

		public void esci()
		{
			if (ask())
			{
				model.removeAll();
				dispose();
			}
		}

		@Override
		public void windowClosing(WindowEvent e)
		{
			esci();
		}
	}

	private static class ImageRenderer extends DefaultTableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			ImageIcon image = (ImageIcon) value;
			setHorizontalAlignment(CENTER);
			setIcon(image);
			if (image.getDescription() != null && image.getDescription().length() != 0)
			{
				setHorizontalTextPosition(RIGHT);
				setText(image.getDescription());
			}
			else
			{
				setText(null);
				setHorizontalTextPosition(TRAILING);
			}
			return this;
		}
	}

	private static class LongRenderer extends DefaultTableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			setText(Formatters.formatSize((Long) value));
			setHorizontalAlignment(CENTER);
			return this;
		}
	}

	private class PauseResumeListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			if (e.getActionCommand().equals("P"))
			{
				model.pause(table.getSelectedRow());
				mediator.makeResume();
			}
			else
			{
				model.resume(table.getSelectedRow());
				mediator.makePause();
			}
		}
	}

	private class ReceiveListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			ObservableDialog.addReceiver(JFileTransferFrame.this);
		}

	}

	private class SendListener implements ActionListener
	{

		@Override
		public void actionPerformed(ActionEvent e)
		{
			ObservableDialog.addSender(JFileTransferFrame.this);
		}

	}

	private static class StringRenderer extends DefaultTableCellRenderer
	{
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			String message = value == null ? "" : value.toString();
			setText(message);
			setHorizontalAlignment(CENTER);
			return this;
		}
	}

	private class Mediator
	{
		public void makePause()
		{
			pauseResumeButton.setIcon(new ImageIcon(ImageHandler.getPause().getScaledInstance(TOOLBAR_SIZE, TOOLBAR_SIZE, Image.SCALE_SMOOTH)));
			setAction("Pausa");
		}

		private void setAction(String string)
		{
			String in = string.charAt(0) + "";
			pauseResumeButton.setToolTipText(string);
			pauseResumeButton.setActionCommand(in);
			pauseResume.setText(string);
			pauseResume.setActionCommand(in);
		}

		public void makeResume()
		{
			pauseResumeButton.setIcon(new ImageIcon(ImageHandler.getPlay().getScaledInstance(TOOLBAR_SIZE, TOOLBAR_SIZE, Image.SCALE_SMOOTH)));
			setAction("Riprendi");
		}

		public void setPauseEnabled(boolean enabled)
		{
			pauseResume.setEnabled(enabled);
			pauseResumeButton.setEnabled(enabled);
		}

		public void setSelectedRowEnabled(boolean enabled)
		{
			remove.setEnabled(enabled);
			openLocation.setEnabled(enabled);
		}
	}

	private class FileTransferHandler extends TransferHandler
	{

		@Override
		public boolean canImport(TransferSupport support)
		{
			// stringFlavor WORKAROUND per Linux
			if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor) || support.isDataFlavorSupported(DataFlavor.stringFlavor))
			{
				if (support.getDropAction() == MOVE)
					support.setDropAction(COPY);
				return true;
			}
			return true;
		}

		@Override
		public boolean importData(TransferSupport support)
		{
			try
			{
				Transferable transferable = support.getTransferable();
				DataFlavor[] flavors = transferable.getTransferDataFlavors();
				if (Arrays.asList(flavors).contains(DataFlavor.javaFileListFlavor))
				{
					java.util.List<File> fileList = (java.util.List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
					for (File f : fileList)
						processFile(f);
					return true;
				}
				if (Arrays.asList(flavors).contains(DataFlavor.stringFlavor)) // WORKAROUND per
																				// Linux
				{
					String s = (String) transferable.getTransferData(DataFlavor.stringFlavor);
					String[] ls = s.split("\n");
					for (String uri : ls)
						processFile(new File(new URI(uri.trim())));
					return true;
				}
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(JFileTransferFrame.this, "Errore nella lettura: " + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
			}
			return false;
		}

		private void processFile(File file)
		{
			if (file.isFile())
				ObservableDialog.addSender(JFileTransferFrame.this, file);
			else
				ObservableDialog.addReceiver(JFileTransferFrame.this, file);
		}
	}
}
