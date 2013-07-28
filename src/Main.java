import javax.swing.*;
import gui.*;

public class Main
{
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
		}
		new JFileTransferFrame().setVisible(true);
	}
}
