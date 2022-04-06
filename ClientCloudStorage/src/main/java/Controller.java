
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;


import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Controller implements Initializable {

    public Button login;
    public TextField loginTextField;
    public PasswordField password;
    public ListView<String> listClient;
    public ListView<String> listServer;
    public Button btnCopy;
    public Button btnMove;
    public Button btnUpdate;
    public Button btnDelete;
    public Label status;
    ObservableList<String> listFiles;
    Path path = Paths.get("C:/NettyCloudStorage/resources");
    Socket socket;
    DataOutputStream dos;
    DataInputStream dis;

    private final byte SEND_FILE_SIGNAL_BYTE = 25;
    private final byte RECEIVE_FILE_SIGNAL_BYTE = 20;
    private final byte REQUEST_LIST_SIGNAL_BYTE = 14;
    private final byte RECEIVE_LIST_SIGNAL_BYTE = 15;
    private final byte DELETE_FILE_SIGNAL_BYTE = 10;


    public void initialize(URL location, ResourceBundle resources) {
        updateListView();
        status.setText("Status: ready");
    }

    public void commandLogin(ActionEvent actionEvent) {
        byte[] text;
        text = loginTextField.getText().getBytes();
        try {
            dos.write(text);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void commandCopy(ActionEvent actionEvent) {
        status.setText("Status: copying...");
        String fileName = listClient.getSelectionModel().getSelectedItem();
        File file = new File(path + "/" + fileName);
        try(FileInputStream fis = new FileInputStream(file)) {
            connectToServer();
            byte[] bufFile = new byte[fis.available()];
            fis.read(bufFile, 0, bufFile.length);

            ByteBuffer buf = ByteBuffer. allocate(1 + 4 + fileName.getBytes().length + 8 + bufFile.length);
            buf.put(SEND_FILE_SIGNAL_BYTE); //signal byte 1 byte
            buf.putInt(fileName.length()); // file name length 4 bytes
            buf.put(fileName.getBytes()); // file name bytes
            buf.putLong(file.length()); // file length 8 bytes
            buf.put(bufFile); // file`s bytes  //TODO нельзя целиком класть файл в буфер
            for (int i = 0; i < buf.capacity(); i++) {
                dos.write(buf.get(i));
            }
            dos.flush();
        } catch (Exception e) {
                e.printStackTrace();
        } finally {
            disconnectFromServer();
            status.setText("Status: ready");
        }
    }

    public void commandMove(ActionEvent actionEvent) {
        System.out.println("Move");
    }

    public void commandUpdate(ActionEvent actionEvent) {
        updateListView();
    }

    public void commandDelete(ActionEvent actionEvent) {
        String fileName = listClient.getSelectionModel().getSelectedItem();
        File file = new File(path + "/" + fileName);
        if(file.delete()) updateListView();
    }

    private void updateListView() {
        try {
            connectToServer();
//            File dir = new File(String.valueOf(path.getFileName()));
//            File[] files = dir.listFiles();
//            List<String> list = new ArrayList<>();
//            if (files != null) {
//                for (File file : files)
//                    list.add(file.getName());
//            }
//            listFiles = FXCollections.observableList(list);
//            listClient.setItems(listFiles);
//
//            dos.writeByte(14);
//            dos.flush();
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    byte b = 0;
//                    try {
//                        b = dis.readByte();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                    System.out.println(b);
//                }
//            });
            dos.writeByte(14);
            dos.flush();
            System.out.println(dis.read());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            disconnectFromServer();
        }
    }

    public void loginTextFieldMouseClicked(MouseEvent mouseEvent) {
        loginTextField.clear();
    }

    public void passwordMouseClicked(MouseEvent mouseEvent) {
        password.clear();
    }

    public void connectToServer() throws IOException {
        socket = new Socket("localhost", 8189);//TODO заменить адрес
        dos = new DataOutputStream(socket.getOutputStream());
        dis = new DataInputStream(socket.getInputStream());
    }

    public void disconnectFromServer() {
        try {
            dos.close();
            dis.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

