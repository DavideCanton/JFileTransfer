package gui;

import gui.dialog.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import core.communication.*;
import core.communication.NetworkAnalyzer.AddressOrdering;
import core.observer.*;

public class ObservableDialog extends AbstractCustomDialogManager
{
	protected static final String IP = "[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";

	private static void addAndStart(JFileTransferFrame frame, ObservableDialog o)
	{
		frame.addObservable(o.observable);
		if (o.observable instanceof SenderImpl)
			((SenderImpl) o.observable).send(o.file);
		else
			((ReceiverImpl) o.observable).listen(o.file);
	}

	public static void addReceiver(JFileTransferFrame frame, File directory)
	{
		if (directory != null && !(directory.exists() && directory.isDirectory()))
			throw new IllegalArgumentException("Invalid directory: " + directory);
		ObservableDialog o = new ObservableDialog(false, directory);
		o.run(frame);
		if (o.observable != null)
			addAndStart(frame, o);
	}

	public static void addSender(JFileTransferFrame frame, File file)
	{
		if (file != null && !(file.exists() && file.isFile()))
			throw new IllegalArgumentException("Invalid file: " + file);
		ObservableDialog o = new ObservableDialog(true, file);
		o.run(frame);
		if (o.observable != null)
			addAndStart(frame, o);
	}

	public static void addReceiver(JFileTransferFrame frame)
	{
		ObservableDialog o = new ObservableDialog(false);
		o.run(frame);
		if (o.observable != null)
			addAndStart(frame, o);
	}

	public static void addSender(JFileTransferFrame frame)
	{
		ObservableDialog o = new ObservableDialog(true);
		o.run(frame);
		if (o.observable != null)
			addAndStart(frame, o);
	}

	private File file;
	private JButton fileButton;
	private JFileChooser fileChooser = new JFileChooser();
	private JComboBox ip;
	private ActivityObservable observable;
	private JTextField port, fileText;
	private boolean sender;
	private JSpinner timeout;

	public ObservableDialog(boolean sender)
	{
		this.sender = sender;
	}

	public ObservableDialog(boolean sender, File file)
	{
		this.sender = sender;
		this.file = file;
	}

	@Override
	protected void cancel(JDialog dialog)
	{
		dispose();
	}

	protected JPanel centerPanel()
	{
		JPanel c = new JPanel();
		c.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		int base = sender ? 0 : -1;
		if (sender)
		{
			gc.gridx = 0;
			gc.gridy = 0;
			gc.gridwidth = 1;
			gc.anchor = GridBagConstraints.LINE_END;
			gc.fill = GridBagConstraints.NONE;
			gc.insets = new Insets(0, 0, 0, 0);
			c.add(makeLabel("IP destinazione:"), gc);

			gc.gridx = 1;
			gc.gridy = 0;
			gc.gridwidth = 2;
			gc.anchor = GridBagConstraints.LINE_START;
			gc.fill = GridBagConstraints.HORIZONTAL;
			gc.insets = new Insets(10, 5, 10, 5);
			try
			{
				Collection<InetAddress> ips = NetworkAnalyzer.getLocalAddresses(AddressOrdering.IPV4);
				ip = new JComboBox(string(ips).toArray());
			}
			catch (SocketException e1)
			{
				ip = new JComboBox();
			}
			ip.setEditable(true);
			c.add(makePanel(ip), gc);
		}

		gc.gridx = 0;
		gc.gridy = base + 1;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		gc.gridwidth = 1;
		gc.insets = new Insets(0, 0, 0, 0);
		c.add(makeLabel("Porta:"), gc);

		port = new JTextField(22);
		gc.gridx = 1;
		gc.gridy = base + 1;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.gridwidth = 2;
		gc.insets = new Insets(10, 5, 10, 5);
		gc.fill = GridBagConstraints.HORIZONTAL;
		c.add(makePanel(port), gc);
		port.setText("1234");

		gc.gridx = 0;
		gc.gridy = base + 2;
		gc.anchor = GridBagConstraints.LINE_END;
		gc.fill = GridBagConstraints.NONE;
		gc.gridwidth = 1;
		gc.insets = new Insets(0, 0, 0, 0);
		c.add(makeLabel("File:"), gc);

		fileText = new JTextField(15);
		fileText.setEditable(false);
		if (file != null)
			fileText.setText(file.getPath());
		fileButton = new JButton("Sfoglia...");
		gc.gridx = 1;
		gc.gridy = base + 2;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.insets = new Insets(10, 5, 10, 5);
		c.add(makePanel(fileText), gc);

		fileButton.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				int r = sender ? fileChooser.showOpenDialog(null) : fileChooser.showSaveDialog(null);
				file = (r == JFileChooser.APPROVE_OPTION) ? fileChooser.getSelectedFile() : null;
				fileText.setText(file != null ? file.getPath() : "");
			}
		});
		gc.gridx = 2;
		gc.gridy = base + 2;
		gc.anchor = GridBagConstraints.LINE_START;
		gc.fill = GridBagConstraints.HORIZONTAL;
		c.add(makePanel(fileButton), gc);
		if (sender)
		{
			gc.gridx = 0;
			gc.gridy = base + 3;
			gc.gridwidth = 1;
			gc.anchor = GridBagConstraints.LINE_END;
			gc.fill = GridBagConstraints.NONE;
			gc.insets = new Insets(0, 0, 0, 0);
			c.add(makeLabel("Timeout (in secondi):"), gc);

			gc.gridx = 1;
			gc.gridy = base + 3;
			gc.gridwidth = 2;
			gc.anchor = GridBagConstraints.LINE_START;
			gc.fill = GridBagConstraints.HORIZONTAL;
			gc.insets = new Insets(10, 5, 10, 5);
			timeout = new JSpinner(new SpinnerNumberModel(5, 1, 60, 1));
			c.add(makePanel(timeout), gc);
		}
		fileChooser.setFileSelectionMode(sender ? JFileChooser.FILES_ONLY : JFileChooser.DIRECTORIES_ONLY);
		return c;
	}

	public boolean isSender()
	{
		return sender;
	}

	private Component makeLabel(String string)
	{
		JLabel label = new JLabel(string);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		return label;
	}

	private JPanel makePanel(Component comp)
	{
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.add(comp);
		return p;
	}

	@Override
	protected void ok(JDialog dialog)
	{
		boolean ok = true;
		if (isSender() && !ip.getSelectedItem().toString().matches(IP))
			ok = false;
		int portNumber = -1;
		try
		{
			portNumber = Integer.parseInt(port.getText());
		}
		catch (NumberFormatException ex)
		{
			ok = false;
		}
		if (portNumber <= 0 || portNumber > (1 << 16) - 1)
			ok = false;
		if (file == null)
			ok = false;
		if (!ok)
		{
			JOptionPane.showMessageDialog(null, "Errore nei dati!", "Errore", JOptionPane.ERROR_MESSAGE);
			dialog.setVisible(true);
		}
		else
		{
			try
			{
				observable = isSender() ? new SenderImpl(InetAddress.getByName(ip.getSelectedItem().toString()), portNumber) : new ReceiverImpl(portNumber);
			}
			catch (UnknownHostException e1)
			{
			}
			dispose();
		}
	}

	private void run(JFileTransferFrame frame)
	{
		super.run(frame, "Aggiungi nuova attività ");
	}

	private LinkedList<String> string(Collection<InetAddress> ips)
	{
		LinkedList<String> s = new LinkedList<String>();
		for (InetAddress i : ips)
			s.add(i.getHostAddress());
		return s;
	}
}
