import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

// import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class Client extends JFrame {
    private JTextField userMessage;
    private JTextArea chatBox;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = "";
    private String serverIP;
    private Socket servSocket;

    public Client(String host) {
        super("Client!");
        serverIP = host;
        userMessage = new JTextField();
        userMessage.setEditable(false);
        userMessage.setText("Type Here...");
        userMessage.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendMessage(event.getActionCommand()); // returns the value of the text field
                userMessage.setText("");
            }
        });
        add(userMessage, BorderLayout.SOUTH);
        chatBox = new JTextArea();
        add(new JScrollPane(chatBox), BorderLayout.CENTER);
        setSize(500, 300);
        setVisible(true);
    }

    public void startRunning() {

        try {
            this.setLocation(510, 0);
            connectToServer();
            setupStreams();
            whileChatting();
        } catch (EOFException eofException) {
            showMessage("\n Client terminated the connection");

        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            closeAll();
        }
    }

    private void connectToServer() throws IOException {
        showMessage("Attempting connection");
        servSocket = new Socket(InetAddress.getByName(serverIP), 6789);
        showMessage("\nConnected to" + servSocket.getInetAddress().getHostName());

    }

    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(servSocket.getOutputStream());
        output.flush();
        input = new ObjectInputStream(servSocket.getInputStream());
        showMessage("\nStreams are setup!");

    }

    private void whileChatting() throws IOException {
        ableToType(true);
        do {
            try {
                message = (String) input.readObject();
                showMessage("\n" + message);
            } catch (ClassNotFoundException classNotFoundException) {
                showMessage("\nObjectType unkown!");
            }
        } while (!message.equals("\nADMIN >> END"));
    }

    private void closeAll() {
        showMessage("\nClosing Connections!");
        ableToType(false);
        try {
            output.close();
            input.close();
            servSocket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    private void sendMessage(String message) {
        try {
            output.writeObject("USER >> " + message);
            output.flush();
            // showMessage("\nUSER >> " + message);
        } catch (IOException ioException) {
            chatBox.append("\nIO Error: Something is messed up!");
        }

    }

    private void showMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                chatBox.append(message);
            }
        });
    }

    private void ableToType(final boolean b) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                userMessage.setEditable(b);
            }
        });
    }
}