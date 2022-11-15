package com.mdcc.jfiletransfer.gui.dialog;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.concurrent.locks.*;
import javax.swing.*;

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

public abstract class AbstractCustomDialogManager
{
    private final Lock lock = new ReentrantLock();
    private final Condition closedC = lock.newCondition();
    private boolean closed = false;
    private JDialog dialog;

    public void run(Frame parent, String title)
    {
        JOptionPane pane = new JOptionPane(centerPanel(), JOptionPane.QUESTION_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
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
        pane.addPropertyChangeListener(JOptionPane.VALUE_PROPERTY, evt ->
        {
            if (evt.getNewValue() == null || !(evt.getNewValue() instanceof Integer))
                return;
            int value = (Integer) evt.getNewValue();
            if (value == JOptionPane.CANCEL_OPTION)
                cancel(dialog);
            else
                ok(dialog);
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
                } catch (InterruptedException e)
                {
                    break;
                }
        } finally
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
