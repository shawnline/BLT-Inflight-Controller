import com.fazecast.jSerialComm.SerialPort;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import static com.fazecast.jSerialComm.SerialPort.*;
import static java.nio.charset.StandardCharsets.*;
public class SerialTest {
    public static void main(String[] args) {
        JFrame frame = new JFrame("SerialTest");
        frame.setContentPane(new SerialTest().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
    private SerialTest() {
        textArea1.setText("Welcome to the Java serial reader. Plug in a device, then press \"Start\".");
        stop = true;
        getCommPorts();
        for(int i = 0; i < getCommPorts().length; i++) {
            comboBox1.addItem(getCommPorts()[i].getDescriptivePortName());
        }
        comboBox1.addActionListener(e -> {
        });
        textArea2.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
            }
        });
        startButton.addActionListener(this::actionPerformed);
        Refresh.addActionListener(e -> {
            comboBox1.removeAllItems();
            getCommPorts();
            for(int i = 0; i < getCommPorts().length; i++) {
                comboBox1.addItem(getCommPorts()[i].getDescriptivePortName());
            }

        });
    }

    private JComboBox comboBox1;
    private JScrollPane textArea2;
    private JButton startButton;
    private JPanel rootPanel;
    private JButton Refresh;
    private JTextArea textArea1;
    private boolean stop;

    private void actionPerformed(ActionEvent e) {
        SwingWorker<Void, Void> s = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                startButton.setText("Stop");
                stop = false;
                SerialPort c = getCommPorts()[comboBox1.getSelectedIndex()];
                c.openPort(5);
                byte[] buffer = new byte[22];
                String s = "";
                while (!stop) {
                    c.readBytes(buffer, 22);
                    s += new String(buffer, UTF_8);
                    textArea1.setText(s);
                }
                c.closePort();
                return null;
            }
        };
        if (stop) {
            s.execute();
        } else {
            stop = true;
            startButton.setText("Start");
        }
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
