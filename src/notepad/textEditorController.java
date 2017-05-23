package notepad;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javax.swing.JOptionPane;

/**
 *
 * @author Hossein Eldelbani and Jonathan Hoffmann
 */
public class textEditorController implements Initializable {

    Socket socket;
    BufferedReader userInput;
    BufferedReader inp;
    DataInputStream disIN;
    DataOutputStream outp;
    PrintStream printStream;
    InputStream is;
    FileOutputStream fos = null;
    BufferedOutputStream bos = null;
    Stage stage;
    boolean firstTime = true, isSaved = false, connected = false;
    String path = "";
    String email = "";
    String title = "New File.txt";
    String password = "";
    String access = "r";
    String[][] filesMatrix = null;
    @FXML
    Menu menu_features;
    @FXML
    private TextArea TextArea_mainText;
    @FXML
    private Button newFile;
    @FXML
    private MenuItem signIn;
    @FXML
    private MenuItem signOut;
    @FXML
    private AnchorPane logInPane;
    @FXML
    private Button logIn;
    @FXML
    private Button signUp;
    @FXML
    private Button close;
    @FXML
    private PasswordField passPasswordField;
    @FXML
    private TextField emailTextField;
    @FXML
    private Label Label_loginStatus;
    @FXML
    MenuItem MenuItem_OPEN;
    @FXML
    TextField textField_addEmail;
    @FXML
    Text textTitle;
    @FXML
    Tab tab_ManageUsers;
    @FXML
    Tab tab_textEditor;
    @FXML
    Label label_Status;
    @FXML
    TabPane tabPane;
    @FXML
    RadioButton rb_R;
    @FXML
    RadioButton rb_RW;
    @FXML
    ListView listView;
    @FXML
    Button btn_DELETFILE;
    @FXML
    Button btn_EDIT;
    @FXML
    Button btn_ADD;
    @FXML
    Button btn_UPDATE;
    @FXML
    Button btn_OPEN_FROM_SERVER;
    @FXML
    TextField txtField_ENTEREMAIL;
//File Menu

    @FXML
    private void newFile() {
        getStage();
        stage.setTitle("new UNSAVED text file");
        TextArea_mainText.clear();
        showHideAP(logInPane, Boolean.FALSE);
    }

    @FXML
    private void openFile() {
        getStage();
        FileChooser fileChooser = openFileChooser("Select File To open");
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            List<String> openedText;
            try {
                path = file.getPath();
                openedText = Files.readAllLines(Paths.get(path));
                TextArea_mainText.clear();
                for (String s : openedText) {
                    TextArea_mainText.appendText(s + "\n");
                }
                stage.setTitle(file.getName());
                title = file.getName();
//                TextArea_mainText.setEditable(false);
                System.out.println("open file is done");
            } catch (IOException ex) {
                System.out.println("TextEditor.java open file exception is\n" + ex);
            }

        }
    }
//------------------------------------------------------------------------------

    @FXML
    private void showLogin() {//show signin window
        getStage();
        Label_loginStatus.setText("");
        showHideAP(logInPane, Boolean.TRUE);
    }

    @FXML
    private void hideLogin() {//close signin window
        showHideAP(logInPane, Boolean.FALSE);
        passPasswordField.clear();

    }

    @FXML
    private void signIn() {
        if (loginSignUp("login", emailTextField.getText(), passPasswordField.getText())) {
            signIn.setDisable(true);
            signOut.setDisable(false);
            menu_features.setDisable(false);
            tab_ManageUsers.setDisable(false);
            showHideAP(logInPane, Boolean.FALSE);
            showHideEditPart(false);
        }
        emailTextField.clear();
        passPasswordField.clear();
    }

    @FXML
    private void signUp() {
        if (loginSignUp("signup", emailTextField.getText(), passPasswordField.getText())) {
            signIn.setDisable(true);
            signOut.setDisable(false);
            menu_features.setDisable(false);
            tab_ManageUsers.setDisable(false);
            showHideAP(logInPane, Boolean.FALSE);
            stage.setTitle(title + "-signed in as " + email);
        }
        emailTextField.clear();
        passPasswordField.clear();
    }

    @FXML
    private boolean logOut() {
        String status = sendCommand("logout");
        if (status.equals("listening")) {
            status = sendCommand(email);
            if (status.equals("disconnected")) {
                signIn.setDisable(false);
                signOut.setDisable(true);
                menu_features.setDisable(true);
                tab_ManageUsers.setDisable(true);
                stage.setTitle(title + " logged out");
                return true;
            }
        }
        return false;
    }

    //--------------------------------------------------------------------------
    @FXML
    private void saveToSamePlace() {
        getStage();
        ArrayList<String> list = new ArrayList<>();
        for (String s : TextArea_mainText.getText().split("\n")) {
            list.add(s);
        }
        try {
            File file = new File(path);
            stage.setTitle(file.getName());
            title = file.getName();
            file.delete();
            Files.write(Paths.get(path), list, StandardOpenOption.CREATE_NEW);
            isSaved = true;
            System.out.println("length is  " + TextArea_mainText.lengthProperty());
            System.out.println("save to same path: " + path + " is done");
        } catch (IOException ex) {
            System.out.println("exception teEditor.java saveToSamePlace method is\n" + ex);
        }
    }

    @FXML
    private void saveToPath() {
        getStage();
        saveToPathMethod("save as");
    }

    @FXML
    private void closeProgram() throws IOException {
        int showConfirmDialog = JOptionPane.showConfirmDialog(null, "Do you want to continue", "Exit Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (showConfirmDialog == 0) {//yes exit
            if (logOut()) {
                if (connected) {
                    socket.close();
                }
                Platform.exit();
            }

        }
    }

    //******************************************************************************
    //Features
    @FXML
    private void saveToServer() {
        saveToServerMethod("Enter the file name", "new document.txt");

    }
    //******************************************************************************
    //******************************************************************************
    //******************************************************************************
    //TAB 2 MANAGE USERS

    @FXML
    private void editPartBUTTON() {
    }

    @FXML
    private void radioButtonRW(ActionEvent event) {//close signin window
        rb_R.setSelected(false);
        access = "rw";
    }

    @FXML
    private void radioButtonR() {//close signin window
        rb_RW.setSelected(false);
        access = "r";
    }

    @FXML
    private void gotListIndex() {
        int i = listView.getSelectionModel().getSelectedIndex();
        System.out.println("u selected index " + i);
        if (i >= 0) {
            enableButtonsAdministratorTab(i);
        }
    }

    @FXML
    private void deleteButton() {
        int i = listView.getSelectionModel().getSelectedIndex();
        if (i >= 0) {
            String s = sendCommand("deletefile");
            if (s.equals("listening")) {
                if (loginSignUp("login", email, password)) {
                    String status = sendCommand(filesMatrix[i][0]);//sendpath
                    if (status.equals("deleted")) {
                        writeDirectories();
                    } else {//error
                        label_Status.setText("error occure,try again later");
                        System.out.println("error occure");
                    }
                }
            }
        } else {
            label_Status.setText("select a file first");
            System.out.println("select a file first");
        }
    }

    @FXML
    private void openSelectedFileFromServer() throws IOException {
        int i = listView.getSelectionModel().getSelectedIndex();
        if (i >= 0) {
            String stat = sendCommand("openfile");
            if (stat.equals("listening")) {
                if (loginSignUp("login", email, password)) {
                    stat = sendCommand(filesMatrix[i][0]);
                    if (stat.equals("startsending")) {
                        String s = receivedFile();
                        if (s.contains("error")) {
                            System.out.println("error");
                        } else {
                            int showConfirmDialog = JOptionPane.showConfirmDialog(null, "Do you want to Save your text First", "save Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                            if (showConfirmDialog == 0) {//yes save
                                if (path.equals("")) {
                                    saveToPathMethod("Save Your Text Before Opening from server");
                                } else {
                                    saveToSamePlace();
                                }
                            }
                            TextArea_mainText.setText(removeNewLine(s));
                            switchTab();
                        }
                    } else {//accessdenied or any other than startsending
                        System.out.println("error");
                    }
                }
            }
        } else {
            label_Status.setText("select a file first");
            System.out.println("select file first");
        }
    }

    @FXML
    private void updateSelectedFile() {
        int i = listView.getSelectionModel().getSelectedIndex();
        if (i >= 0) {
            if (!filesMatrix[i][1].equals("r")) {//if the access is not read only
                if (filesMatrix[i][2].equals("yes")) {//so i have see the last update
                    String titre = "";
                    int showConfirmDialog = JOptionPane.showConfirmDialog(null, "Do you want to Save your text First", "save Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (showConfirmDialog == 0) {//yes save
                        if (path.equals("")) {
                            saveToPathMethod("Save Your Text Before uploading");
                        } else {
                            saveToSamePlace();
                        }
                        if (!path.equals("")) {//save continued
                            titre = "Do you want to upload your saved text file";
                        }
                    } else {//no i dont want to save
                        titre = "Do you want to upload your unsaved text file";
                    }
                    int showConfirmDialog1 = JOptionPane.showConfirmDialog(null, titre, "Upload Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (showConfirmDialog1 == 0) {//yes upload
                        String stat = sendCommand("push");
                        if (stat.equals("listening")) {
                            if (loginSignUp("login", email, password)) {
                                String[] arr = TextArea_mainText.getText().split("\n");
                                stat = sendCommand(filesMatrix[i][0] + "X" + arr.length);
                                if (stat.equals("startsending")) {
                                    if (sendFileUpdate(arr)) {
                                        label_Status.setText("upload completed");
                                    }
                                } else if (stat.equals("youdonthaveaccess")) {
                                    label_Status.setText("access denied");
                                } else if (stat.equals("updatefirst")) {
                                    label_Status.setText("update first");
                                    writeDirectories();//refresh page
                                } else {
                                    label_Status.setText("error try again");
                                }

                            } else {
                                label_Status.setText("error try again");
                            }
                        }
                    } else {//no i dont want to upload
                        label_Status.setText("upload canceled");
                    }

                } else {//i havent see the last update
                    label_Status.setText("download the last update first");
                    System.out.println("download the last update first");
                }
            } else {
                label_Status.setText("you dont have acces to update this file");
            }
        } else {
            label_Status.setText("select a file first");
            System.out.println("select a file first");
        }

    }

    @FXML
    private void writeDirectories() {
        String stat = sendCommand("getfiles");
        if (stat.equals("listening")) {
            try {
                String s = "error try again";
                if (loginSignUp("login", email, password)) {
                    s = receivedFile();
                    if (s.contains("error")) {
                        s = "error while loading\ntry again";
                    }
                }
                String[] arr = s.split("\n");
                filesMatrix = new String[arr.length][5];
                ArrayList<String> list = new ArrayList<>();
                for (int i = 0; i < arr.length; i++) {
                    String[] split = arr[i].split("X");
                    list.add(split[0] + "\t\tAccess: " + split[1] + "\t\tUpdated: " + split[2] + "\t\tCreated By: " + split[3] + "\t\tUpdated By: " + split[4]);
                    for (int k = 0; k < 5; k++) {
                        filesMatrix[i][k] = split[k];
                    }
                }
                for (int i = 0; i < arr.length; i++) {
                    for (int j = 0; j < 5; j++) {
                        System.out.print(filesMatrix[i][j] + " | ");
                    }
                    System.out.println("");
                }
                System.out.println(list);
                ObservableList<String> items = FXCollections.observableArrayList(list);
                listView.setItems(items);

            } catch (Exception ex) {
                System.out.println("writeDirectory method error\n" + ex);
            }
        }
    }
    //******************************************************************************
    //methods

    private String receivedFile() throws IOException {
        String s = "";
        int i = 0;
        String d;
        while (true) {
            d = inp.readLine();
            if (d.equals("endof")) {
                break;
            } else if (d.equals("error")) {
                System.out.println("error receiving");
                return "error";
            } else {
                s += d + "\n";
                i++;
            }
        }
        System.out.println("received");
        return s;
    }

    private String removeNewLine(String s) {
        String newS = "";
        boolean first = false;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '\n') {
                if (first) {
                    first = false;
                    continue;
                }
                first = true;
            }
            newS += s.charAt(i);
        }
        return newS;
    }

    private void enableButtonsAdministratorTab(int i) {
        try {
            String s = filesMatrix[i][1];
            btn_OPEN_FROM_SERVER.setDisable(false);
            if (s.equals("ad")) {//admin
                btn_UPDATE.setDisable(false);
                btn_ADD.setDisable(false);
                btn_DELETFILE.setDisable(false);
                btn_EDIT.setDisable(false);
            } else if (s.equals("rw")) {//read write
                btn_UPDATE.setDisable(false);
                btn_ADD.setDisable(false);
                btn_DELETFILE.setDisable(true);
                btn_EDIT.setDisable(true);
            } else if (s.equals("r")) {//read
                btn_UPDATE.setDisable(true);
                btn_ADD.setDisable(false);
                btn_DELETFILE.setDisable(true);
                btn_EDIT.setDisable(true);
                txtField_ENTEREMAIL.setVisible(false);
            } else {//any else in case of errors
                btn_UPDATE.setDisable(true);
                btn_ADD.setDisable(true);
                btn_DELETFILE.setDisable(true);
                btn_EDIT.setDisable(true);
                btn_OPEN_FROM_SERVER.setDisable(true);
            }
        } catch (Exception ex) {
            System.out.println("exception matrix \n" + ex);
        }

    }

    private void switchTab() {
        SingleSelectionModel<Tab> selectionModel = tabPane.getSelectionModel();
        if (selectionModel.getSelectedIndex() == 0) {
            selectionModel.select(1);
        } else {
            selectionModel.select(0);
        }
    }

    private void saveToServerMethod(String s, String text_Name) {
        try {
            do {
                text_Name = textName(s, text_Name);
                if (text_Name.trim().isEmpty()) {
                    s = "file name shouldnt be empty";
                }
            } while (text_Name.trim().isEmpty());
            String status = sendCommand("save");
            if (status.equals("listening")) {
                if (loginSignUp("login", email, password)) {
                    if (path.equals("")) {
                        int showConfirmDialog = JOptionPane.showConfirmDialog(null, "Do you want to Save your text First", "save Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (showConfirmDialog == 0) {//yes save
                            if (path.equals("")) {
                                saveToPathMethod("Save Your Text Before uploading");
                            } else {
                                saveToSamePlace();
                            }
                        }
                        String[] arr = TextArea_mainText.getText().split("\n");
                        status = sendCommand(text_Name.toLowerCase() + ".txtX" + arr.length);
                        if (status.equals("listening")) {//upload
                            status = upload();
                            if (status.equals("filereceived")) {
                                System.out.println("uploaded and received");
                            } else {
                                System.out.println("error receiving from server side " + status);
                            }
                        } else if (status.equals("fileexist")) {//request to change name or overwide
                            saveToServerMethod("a file with same name exist on server", text_Name.toLowerCase());
                        }
                    } else {
                        System.out.println("error");
                    }

                }
            }
        } catch (Exception ex) {
            System.out.println("saveToServerMethod exception\n" + ex);
        }
    }

    private String upload() {
        String[] arr = TextArea_mainText.getText().split("\n");
        String stat = sendCommand("startupload");
        for (String s : arr) {
            if (stat.equals("continue")) {
                sendCommand(s);
            } else {//error
                return stat;
            }
        }
        System.out.println("upload completed");
        return sendCommand("endoffile");// reply should be "filereceived"
    }

    private FileChooser openFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new ExtensionFilter("Text Files(.txt)", "*.txt"));//added a filter
        return fileChooser;
    }

    private void getStage() {
        if (firstTime) {
            stage = (Stage) TextArea_mainText.getScene().getWindow();
            firstTime = false;
        }
    }

    private String encrypt(String pass) {
        String encrypted = "";
        for (int i = 0; i < pass.length(); i++) {
            int a = (int) (pass.charAt(i)) + (2 * (i + 1) + 1);
            encrypted += String.format("%03d", a);
        }
        return encrypted;
    }

    private String textName(String s, String defaulName) {
        return JOptionPane.showInputDialog(s, defaulName);
    }

    private boolean loginSignUp(String command, String e, String p) {//login signup
        email = e;
        password = p;
        if (!isEmail(email)) {
            Label_loginStatus.setText("Wrong Email Format");
        } else if (password.length() != 8) {
            Label_loginStatus.setText("password should be 8 char");
        } else if (connected) {//username format is ok and password is ok and its connected
            Label_loginStatus.setText(" ");
            String status = sendCommand(command);
            if (status.equals("listening")) {
                status = sendCommand(email);
                if (status.equals("listening")) {
                    status = sendCommand(encrypt(password));//send the encrypted pass
                    if (status.equals("emailorpasserror")) {
                        Label_loginStatus.setText("wrong username or passwrod");
                    } else if (status.contains("connected")) {
                        Label_loginStatus.setText("Connected");
                        return true;
                    } else if (status.equals("created")) {
                        Label_loginStatus.setText("Created");
                        return true;
                    } else if (status.equals("emailexist")) {
                        Label_loginStatus.setText("email exist in our DB");
                        return false;
                    } else if (status.equals("error")) {
                        Label_loginStatus.setText("error occur try again later");
                        return false;
                    } else {
                        Label_loginStatus.setText("error occur try again later");
                        return false;
                    }

                } else {
                    Label_loginStatus.setText("error try again");
                }
            } else {
                Label_loginStatus.setText("error try again");
            }
        } else {//not connected
            Label_loginStatus.setText("connection error try again");
            connected = startConnection();//restart connection
        }
        return false;
    }

    private boolean sendFileUpdate(String[] arr) {
        for (int i = 0; i < arr.length; i++) {
            printStream.print(arr[i] + "\r\n");
        }
        String status = sendCommand("endoffile");
        return status.equals("filereceived");
    }

    private void saveToPathMethod(String title) {
        FileChooser fileChooser = openFileChooser(title);
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            FileWriter fileWriter;
            try {
                fileWriter = new FileWriter(file);
                fileWriter.write(TextArea_mainText.getText());
                fileWriter.close();
                path = file.getPath();
                isSaved = true;
                stage.setTitle(file.getName());
                title = file.getName();
                System.out.println("save to " + path + " is done");
            } catch (IOException ex) {
                System.out.println("exception textEditor.java saveToPath method is\n" + ex);
            }
        }
    }

    private boolean isEmail(String email) {//*@*.*
        String EMAIL_REGEX = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(EMAIL_REGEX);
    }

    private void showHideAP(AnchorPane ap, Boolean bool) {
        ap.setVisible(bool);
    }

    private String sendCommand(String command) {
        String status = "NoStatus";
        try {
            printStream.print(command + "\r\n");
            status = inp.readLine();
            System.out.println(status);
        } catch (Exception ex) {
            System.out.println("sendCommand Method Exception\n" + ex);
        }
        return status;
    }

    private void showHideEditPart(boolean stat) {
        rb_RW.setVisible(stat);
        rb_R.setVisible(stat);
        txtField_ENTEREMAIL.setVisible(stat);
    }

    private boolean startConnection() {
        String status;
        try {
            socket = new Socket("localhost", 7500);
            userInput = new BufferedReader(new InputStreamReader(System.in));
            inp = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            disIN = new DataInputStream(socket.getInputStream());
            outp = new DataOutputStream(socket.getOutputStream());
            printStream = new PrintStream(outp);
            is = socket.getInputStream();
            fos = null;
            bos = null;
            status = inp.readLine();//first status should be cnxok
            System.out.println(status);
//            while (true) {
//                command = userInput.readLine();
//                printStream.print(command + "\r\n");
//                status = inp.readLine();
//                System.out.println(status);
//            }
        } catch (Exception e) {
            System.out.println("Start CNX method exception at textEditor.java\n" + e + "\n*************************************");
            return false;
        }
        return status.equals("cnxok");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        logInPane.setStyle("-fx-background-color: c5cfdf");//hexacolor 6 hex=24bits RGB 197 207 223
        connected = startConnection();
    }
}
