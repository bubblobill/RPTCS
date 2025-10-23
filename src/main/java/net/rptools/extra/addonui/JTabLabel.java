package net.rptools.extra.addonui;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;

public class JTabLabel extends Box {
    private final JTabbedPane pane;
    private AbstractButton closeButton;
    private JLabel label;
    public JTabLabel(JTabbedPane tabbedPane){
        super(BoxLayout.LINE_AXIS);
        this.pane = tabbedPane;
        setOpaque(false);
        label = new JLabel(){
            @Override
            public String getText() {
                int i = pane.indexOfTabComponent(JTabLabel.this);
                if (i != -1) {
                    return pane.getTitleAt(i);
                }
                return null;
            }
        };
        label.setHorizontalAlignment(SwingConstants.LEADING);
//        label.setOpaque(false);
        add(label);
        add(Box.createHorizontalStrut(6));
        closeButton = new TabButton();
        closeButton.setOpaque(false);
    }

    public JLabel getLabel() {
        return label;
    }

    public void setLabel(JLabel label) {
        this.label = label;
    }

    public AbstractButton getCloseButton() {
        return closeButton;
    }

    public void setCloseButton(AbstractButton closeButton) {
        this.closeButton = closeButton;
    }
        private class TabButton extends JButton implements ActionListener {
            public TabButton() {
                super(UIManager.getIcon("InternalFrame.closeIcon"));
                int size = 17;
                setPreferredSize(new Dimension(size, size));
                setToolTipText("close this tab");
                //Make the button looks the same for all Laf's
                setUI(new BasicButtonUI());
                //Make it transparent
                setContentAreaFilled(false);
                //No need to be focusable
                setFocusable(false);
                setBorder(BorderFactory.createEtchedBorder());
                setBorderPainted(false);
                //Making nice rollover effect
                //we use the same listener for all buttons
                addMouseListener(buttonMouseListener);
                setRolloverEnabled(true);
                //Close the proper tab by clicking the button
                addActionListener(this);
            }

            public void actionPerformed(ActionEvent e) {
                int i = pane.indexOfTabComponent(JTabLabel.this);
                if (i != -1) {
                    pane.remove(i);
                }
            }

            //we don't want to update UI for this button
            public void updateUI() {
            }

            //paint the cross
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                //shift the image for pressed buttons
                if (getModel().isPressed()) {
                    g2.translate(1, 1);
                }
                g2.setStroke(new BasicStroke(2));
                g2.setColor(Color.BLACK);
                if (getModel().isRollover()) {
                    g2.setColor(Color.MAGENTA);
                }
                int delta = 6;
                g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
                g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
                g2.dispose();
            }
        }

        private final static MouseListener buttonMouseListener = new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof AbstractButton button) {
                    button.setBorderPainted(true);
                }
            }

            public void mouseExited(MouseEvent e) {
                Component component = e.getComponent();
                if (component instanceof AbstractButton button) {
                    button.setBorderPainted(false);
                }
            }
        };
}
