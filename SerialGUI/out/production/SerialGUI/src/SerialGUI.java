import com.fazecast.jSerialComm.SerialPort;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.fazecast.jSerialComm.SerialPort.getCommPorts;

public class SerialGUI {
    private JPanel rootPanel;
    private JButton Start;
    private JComboBox<String> comboBox1;
    private JButton SaveButton;
    private JLabel Value1;
    private JLabel Value2;
    private JLabel Value3;
    private JLabel Value4;
    private JTextArea textArea1;
    private JLabel FileLabel;
    /*
    private ChartPanel chartPanel1;
    private ChartPanel chartPanel2;
    private ChartPanel chartPanel3;
    private ChartPanel chartPanel4;
    */
    private JButton Refresh;
    private JLabel Value5;
    private JLabel Value6;
    private JLabel Value7;
    private JLabel Value8;
    private JLabel Value9;
    private JLabel Value10;
    private JLabel Value11;
    private JLabel Value12;
    private JLabel Value13;
    private JLabel Value14;
    private JLabel Value15;
    /*
    private ChartPanel chartPanel5;
    private ChartPanel chartPanel6;
    private ChartPanel chartPanel7;
    private ChartPanel chartPanel9;
    private ChartPanel chartPanel11;
    private ChartPanel chartPanel13;
    private ChartPanel chartPanel14;
    private ChartPanel chartPanel15;
    private ChartPanel chartPanel8;
    private ChartPanel chartPanel10;
    private ChartPanel chartPanel12;
    */
    private boolean stop;
    private String fileSave;
    private SerialPort current;
    private BufferedWriter writer;

    private SerialGUI() {
        //sets up some everything
        stop = true;
        //you have to run this line at least once for some reason otherwise SerialPort.getCommPorts().getLength in the loop returns 0
        SerialPort.getCommPorts();
        textArea1.setText("Welcome to the BLT Serial Reader, written by Shawn Joseph in Java.\nFirst, Select a serial device from the drop down. Then, select a \".txt\" file to save to by clicking \"Select output file\". Then, click \"Start Recieving data\".");
        //iterates through list to make a user-friendly list of all the serial ports connected.
        for(int i = 0; i < getCommPorts().length; i++)
            comboBox1.addItem(getCommPorts()[i].getDescriptivePortName() /* + ": " + SerialPort.getCommPorts()[i].getSystemPortName() */ /* Uncomment the previous comment and recompile if you are having issues telling serial devices apart. */);
        if(fileSave == null) {
            //allows default save location to be saved to file and "remembered". Creates file "BLTSettings" with no extension that only contains the save file to save the output to in the current directory or creates it if it does not exist.
            if(new File("BLTsettings").exists()) try {
                BufferedReader read = new BufferedReader(new FileReader("BLTsettings"));
                fileSave = read.readLine();
                FileLabel.setText("Saving to " + fileSave);
            } catch (Exception i) {
                i.printStackTrace();
            }
            else {
                fileSave = "Output.txt";
                FileLabel.setText("Saving to " + fileSave);
            }
        }
        //what to do if start button is pressed- change button text and start the Swing worker Program().
        Start.addActionListener(e -> {
            Start.setText("Collecting data...");
            try {
                program();
            } catch(Exception IOException) {
                IOException.printStackTrace();
            }
        });
        //what to do if save button is pressed- opens a file chooser and asks user to choose or create a save file with the extension .txt
        SaveButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int ret = fileChooser.showSaveDialog(rootPanel);
            if(ret == JFileChooser.APPROVE_OPTION) {
                fileSave = fileChooser.getSelectedFile().getPath();
                FileLabel.setText("Saving to " + fileSave);
                try {
                    BufferedWriter set = new BufferedWriter(new FileWriter("BLTsettings"));
                    set.write(fileSave);
                    set.close();
                } catch (IOException i) {
                    i.printStackTrace();
                }

            }
        });
        //IDK what this does, feel free to remove it if it doesn't do anything but I'm going to keep it in just in case
        /*
        chartPanel1.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
            }
        });
        */
        //Refreshes the device list
        Refresh.addActionListener(e -> {
            getCommPorts();
            comboBox1.removeAllItems();
            for(int i = 0; i < getCommPorts().length; i++)
                comboBox1.addItem(getCommPorts()[i].getDescriptivePortName());
        });
    }
    //this is where the heart of the program is, everything else is mostly just a wrapper.
    //Everything is inside a swing worker because it's an infinite loop and can't update the GUI unless it's a swing worker.
    private void program() {
        SwingWorker<Void, Void> s = new SwingWorker<>() {
            public Void doInBackground() {
                //opens the port, but waits for 5 seconds to be safe.
                current.openPort(5);
                current.setBaudRate(115200);
                //creates a 128 character buffer to read serial data from.
                byte[] buffer = new byte[176];
                //attempts to create a new bufferedWriter to save to. Causes an exception if it can't.
                try {
                    writer = new BufferedWriter(new FileWriter(fileSave));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //sets up loop
                stop = false;
                //sets up datasets for graphs to use
                /*
                DefaultCategoryDataset Val1 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val2 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val3 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val4 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val5 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val6 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val7 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val8 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val9 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val10 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val11 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val12 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val13 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val14 = new DefaultCategoryDataset();
                DefaultCategoryDataset Val15 = new DefaultCategoryDataset();
                */

                //sets up iteration counter, updates data every iteration. I might replace this later with a time counter or something, who knows
                int iter = 0;
                //loop #1, aborts program if stop button is pressed. Otherwise, loops forever
                while (!stop) {
                    //updates the current iteration
                    iter++;
                    //reads 128 bytes from the serial port and stores it in the buffer.
                    current.readBytes(buffer, 172);
                    //converts the buffer into a string assuming the sender is using UTF-8 and splits it into an array of strings without >'s
                    String[] arr = new String(buffer, StandardCharsets.UTF_8).split(">\n");
                    try {
                        //second loop, loops through the number of strings in the array without >'s
                        for (String temp : arr) {
                            //splits temp into an array of strings without ,'s
                            String [] retVar = temp.split(",");
                            if(retVar.length >= 1 ) {
                                String [] print = new String[15];
                                boolean end = false;
                                for(int i = 0; i < retVar.length - 14 && !end; i++) {
                                    if(retVar[i].contains("<")) {
                                        print[0] = retVar[i].replaceAll("<", "").replaceAll("\n","");
                                        /*
                                        print[1] = retVar[i + 1];
                                        print[2] = retVar[i + 2];
                                        print[3] = retVar[i + 3];
                                        print[4] = retVar[i + 4];
                                        print[5] = retVar[i + 5];
                                        print[6] = retVar[i + 6];
                                        print[7] = retVar[i + 7];
                                        print[8] = retVar[i + 8];
                                        print[9] = retVar[i + 9];
                                        print[10] = retVar[i + 10];
                                        print[11] = retVar[i + 11];
                                        print[12] = retVar[i + 12];
                                        print[13] = retVar[i + 13];
                                        print[14] = retVar[i + 14];
                                        end = true;
                                        */
                                        System.arraycopy(retVar, i + 1, print, 1, 14);
                                        end = true;
                                    }
                                }
                                if(print[0] != null) {
                                    //String str = print[0] + "," + print[1] + "," + print[2] + "," + print[3] + "," + print[4] + "," + print[5] + "," + print[6] + "," + print[7] + "," + print[8] + "," + print[9] + "," + print[10] + "," + print[11] + "," + print[12] + "," + print[13] + "," + print[14] + "," + "\n";
                                    StringBuilder strBuilder = new StringBuilder();
                                    for(String p:print) {
                                        strBuilder.append(p).append(",");
                                    }
                                    String str = strBuilder.toString();
                                    str += "\n";
                                    System.out.println(str);
                                    if (writer != null) {
                                        writer.write(str);
                                    }
                                }
                                /* double [] ret = new double [15];
                                if(retVar[0] != null) {
                                    for (int i = 0; i < 15; i++) {
                                        if(print[i] != null) {
                                            ret[i] = Double.parseDouble(print[i]);
                                        }
                                    }

                                }
                                */
                                /*
                                Val1.addValue((Number)(ret[0]),0,  iter);
                                Val2.addValue((Number)(ret[1]),0,  iter);
                                Val3.addValue((Number)(ret[2]),0,  iter);
                                Val4.addValue((Number)(ret[3]),0,  iter);
                                Val5.addValue((Number)(ret[4]),0,  iter);
                                Val6.addValue((Number)(ret[5]),0,  iter);
                                Val7.addValue((Number)(ret[6]),0,  iter);
                                Val8.addValue((Number)(ret[7]),0,  iter);
                                Val9.addValue((Number)(ret[8]),0,  iter);
                                Val10.addValue((Number)(ret[9]),0,  iter);
                                Val11.addValue((Number)(ret[10]),0,  iter);
                                Val12.addValue((Number)(ret[11]),0,  iter);
                                Val13.addValue((Number)(ret[12]),0,  iter);
                                Val14.addValue((Number)(ret[13]),0,  iter);
                                Val15.addValue((Number)(ret[14]),0,  iter);
                                */

                                if(print[0] != null) {

                                    Value1.setText("Temp: " + print[0]);
                                    Value2.setText("PT1: " + print[1]);
                                    Value3.setText("PT2: " + print[2]);
                                    Value4.setText("PT3: " + print[3]);
                                    Value5.setText("PT3: " + print[4]);
                                    Value6.setText("PT3: " + print[5]);
                                    Value7.setText("PT3: " + print[6]);
                                    Value8.setText("PT3: " + print[7]);
                                    Value9.setText("PT3: " + print[8]);
                                    Value10.setText("PT3: " + print[9]);
                                    Value11.setText("PT3: " + print[10]);
                                    Value12.setText("PT3: " + print[11]);
                                    Value13.setText("PT3: " + print[12]);
                                    Value14.setText("PT3: " + print[13]);
                                    Value15.setText("PT3: " + print[14]);
                                    /*
                                    JFreeChart chart1 = ChartFactory.createLineChart("Temp: ", "", "Value", Val1, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart2 = ChartFactory.createLineChart("PT1: ", "", "Value", Val2, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart3 = ChartFactory.createLineChart("PT2: ", "", "Value", Val3, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart4 = ChartFactory.createLineChart("PT2: ", "", "Value", Val3, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart5 = ChartFactory.createLineChart("PT3: ", "", "Value", Val4, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart6 = ChartFactory.createLineChart("PT3: ", "", "Value", Val4, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart7 = ChartFactory.createLineChart("PT3: ", "", "Value", Val4, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart8 = ChartFactory.createLineChart("PT3: ", "", "Value", Val4, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart9 = ChartFactory.createLineChart("PT3: ", "", "Value", Val4, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart10 = ChartFactory.createLineChart("PT3: ", "", "Value", Val4, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart11 = ChartFactory.createLineChart("PT3: ", "", "Value", Val4, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart12 = ChartFactory.createLineChart("PT3: ", "", "Value", Val4, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart13 = ChartFactory.createLineChart("PT3: ", "", "Value", Val4, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart14 = ChartFactory.createLineChart("PT3: ", "", "Value", Val4, PlotOrientation.VERTICAL, false, false, false);
                                    JFreeChart chart15 = ChartFactory.createLineChart("PT3: ", "", "Value", Val4, PlotOrientation.VERTICAL, false, false, false);

                                    if (Val1.getColumnCount() >= 20) Val1.removeColumn(0);
                                    if (Val2.getColumnCount() >= 20) Val2.removeColumn(0);
                                    if (Val3.getColumnCount() >= 20) Val3.removeColumn(0);
                                    if (Val4.getColumnCount() >= 20) Val4.removeColumn(0);
                                    if (Val5.getColumnCount() >= 20) Val5.removeColumn(0);
                                    if (Val6.getColumnCount() >= 20) Val6.removeColumn(0);
                                    if (Val7.getColumnCount() >= 20) Val7.removeColumn(0);
                                    if (Val8.getColumnCount() >= 20) Val8.removeColumn(0);
                                    if (Val9.getColumnCount() >= 20) Val9.removeColumn(0);
                                    if (Val10.getColumnCount() >= 20) Val10.removeColumn(0);
                                    if (Val11.getColumnCount() >= 20) Val11.removeColumn(0);
                                    if (Val12.getColumnCount() >= 20) Val12.removeColumn(0);
                                    if (Val13.getColumnCount() >= 20) Val13.removeColumn(0);
                                    if (Val14.getColumnCount() >= 20) Val14.removeColumn(0);
                                    if(Val15.getColumnCount() >= 20) Val15.removeColumn(0);
                                    chartPanel1.setChart(chart1);
                                    chartPanel2.setChart(chart2);
                                    chartPanel3.setChart(chart3);
                                    chartPanel4.setChart(chart4);
                                    chartPanel5.setChart(chart5);
                                    chartPanel6.setChart(chart6);
                                    chartPanel7.setChart(chart7);
                                    chartPanel8.setChart(chart8);
                                    chartPanel9.setChart(chart9);
                                    chartPanel10.setChart(chart10);
                                    chartPanel11.setChart(chart11);
                                    chartPanel12.setChart(chart12);
                                    chartPanel13.setChart(chart13);
                                    chartPanel14.setChart(chart14);
                                    chartPanel15.setChart(chart15);
                                    */
                                }


                            }
                        }
                        } catch(Exception i){
                        i.printStackTrace();
                    }
                }
                try {
                    if (writer != null) {
                        writer.close();
                    }
                } catch (Exception i) {
                    i.printStackTrace();
                }

                return null;
            }
        };
        if(stop) {
            int index = comboBox1.getSelectedIndex();
            current = getCommPorts()[index];
            stop = false;
            Start.setText("Stop collecting data and save file");
            s.execute();
        }
        else {
            stop = true;
            Start.setText("Start collecting data");
        }
        current.closePort();
        }

    public static void main(String[] args) {
        JFrame frame = new JFrame("SerialGUI");
        try {
            frame.setContentPane(new SerialGUI().rootPanel);
        } catch(Exception IOException) {
            IOException.printStackTrace();
        }
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents() {
        /*
        chartPanel1 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel2 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel3 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel4 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel5 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel6 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel7 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel8 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel9 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel10 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel11 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel12 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel13 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel14 = new ChartPanel(new JFreeChart(new XYPlot()));
        chartPanel15 = new ChartPanel(new JFreeChart(new XYPlot()));
        */
    }
}
