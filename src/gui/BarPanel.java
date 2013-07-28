package gui;

import java.awt.*;
import javax.swing.*;

public class BarPanel extends JPanel
{
	private JProgressBar bar;

	public BarPanel()
	{
		bar = new JProgressBar(0, 100);
		bar.setStringPainted(true);				
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(Box.createVerticalGlue());
		add(bar);
		add(Box.createVerticalGlue());
	}

	public JProgressBar getBar()
	{
		return bar;
	}

	@Override
	public void setBackground(Color bg)
	{
		super.setBackground(bg);
		if (bar != null)
			bar.setBackground(bg);
	}
}