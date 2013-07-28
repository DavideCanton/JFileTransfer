package gui;

import javax.swing.*;
import javax.swing.event.*;
import core.communication.*;

public class StatusBar extends JPanel
{
	private JSlider slider;
	private JLabel label = new JLabel();
	private static final int DEFAULT = 200 << 10;
	private static final int MIN = 50;
	private static final int MAX = 200 << 10;

	public StatusBar()
	{
		slider = new JSlider(MIN, MAX);
		add(slider);
		add(label);
		slider.setValue(DEFAULT / 1024);
		NetworkLimiter.getInstance().setBandwidth(DEFAULT);
		label.setText(Formatters.formatSpeed(DEFAULT));
		slider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				int value = slider.getValue() << 10;
				label.setText(Formatters.formatSpeed(value));
				NetworkLimiter.getInstance().setBandwidth(value);
			}
		});
	}
}
