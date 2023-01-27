package lab7.data.fxlab7;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;

import static java.lang.System.out;

public class HelloController implements Initializable {
    RsaEncryptor rsaEncryptor = new RsaEncryptor(256);
    @FXML
    private Label welcomeText;
    @FXML
    private ComboBox<String> comboBoxK;

    @FXML
    private Label labEncPath;
    @FXML
    private Label labDecrPath;
    @FXML
    private Label labPublicPath;
    @FXML
    private Label labPrivatePath;
    @FXML
    private Button btnEncPath;
    @FXML
    private Button btnDecrPath;
    @FXML
    private Button btnPublicPath;
    @FXML
    private Button btnPrivatePath;

    @FXML
    private TextField publicName;
    @FXML
    private TextField privateName;

    @FXML
    private TextArea inputArea;
    @FXML
    private Button saveInputBtn;


    @FXML
    private Button encrBtn;
    @FXML
    private Button decrBtn;

    public HelloController() {
    }

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    public void initialize(URL location, ResourceBundle resources) {
        comboBoxK.getItems().removeAll(comboBoxK.getItems());
        comboBoxK.getItems().addAll("16", "32", "64", "128", "256", "512", "1024");
        comboBoxK.getSelectionModel().select("16");
    }
    @FXML
    private void setBtnEncPath(ActionEvent event){
        simpleFileChooser(labEncPath, "", "??????????");
    }
    @FXML
    private void setBtnDecrPath(ActionEvent event){
        simpleFileChooser(labDecrPath, "encrypted(outputs)", "Выбран не зашифрованный файл! Выберите другой(из папки encrypted(outputs))");
    }
    @FXML
    private void setBtnPublicPath(ActionEvent event){
        simpleFileChooser(labPublicPath, "publicKeys", "Выбран неверный публичный ключ! Выберите другой(из папки publicKeys)");
    }
    @FXML
    private void setBtnPrivatePath(ActionEvent event){
        simpleFileChooser(labPrivatePath, "privateKeys", "Выбран неверный приватный ключ! Выберите другой(из папки privateKeys)");
    }
    private void simpleFileChooser(Label labFile, String allowedPath, String messege){
        FileChooser fc = new FileChooser();
        File f = fc.showOpenDialog(null);
        if (!f.getPath().contains(allowedPath)){
            a.setAlertType(Alert.AlertType.ERROR);
            a.setContentText(messege);
            a.show();
        }else{
            if (f != null){
                /*labFile.setText("Выбранный файл: " + f.getAbsolutePath());*/
                labFile.setText(f.getAbsolutePath());
            }
        }

    }
    Alert a = new Alert(Alert.AlertType.NONE);
    @FXML
    private void generateKeyBtn() throws IOException {
        if (publicName.getText() == "" || privateName.getText() ==""){
            a.setAlertType(Alert.AlertType.ERROR);
            a.setContentText("Не введено имя для публичного и/или приватного ключа(-чей)");
            a.show();
        }else{
            rsaEncryptor.bitLength = Integer.parseInt(comboBoxK.getValue());
            rsaEncryptor.GenKeys(publicName.getText(), privateName.getText());
            a.setAlertType(Alert.AlertType.INFORMATION);
            a.setContentText("Сгенерированы новые ключи:\n"+"Публичный: "+publicName.getText()+"\n"+"Приватный: "+privateName.getText());
            a.show();
        }

    }

    @FXML
    private void encryptBtn() throws IOException {
        if(labPublicPath.getText().equals("Путь к открытому ключу") || labEncPath.getText().equals("Путь к файлу который хотите зашифровать")){
            a.setAlertType(Alert.AlertType.ERROR);
            a.setContentText("Не введен открытый ключ и/или файл который хотите зашифровать");
            a.show();
        }else{

            rsaEncryptor.publicKeys = labPublicPath.getText();
            rsaEncryptor.outputPath = "generatedFiles/encrypted(outputs)/"+ "("+Paths.get(labPublicPath.getText()).getFileName()+")"+"output"+".txt";
            String openText = FileToString(labEncPath.getText());
            rsaEncryptor.Encrypt(openText);
            a.setAlertType(Alert.AlertType.INFORMATION);
            a.setContentText("Файл "+ labEncPath.getText() + " зашифрован\n"+ "Зашифрованный файл: "+ Paths.get(rsaEncryptor.outputPath).getFileName());
            a.show();
        }

    }

    @FXML
    private void decryptBtn(){
        if(labPrivatePath.getText().equals("Путь к закрытому ключу") || labDecrPath.getText().equals("Путь к файлу который хотите расшифровать")){
            a.setAlertType(Alert.AlertType.ERROR);
            a.setContentText("Не введен закрытый ключ и/или файл который хотите расшифровать");
            a.show();
        }else{
            rsaEncryptor.privateKeys = labPrivatePath.getText();
            rsaEncryptor.outputPath = "generatedFiles/decrypted(outputs)/"+ "("+Paths.get(labPrivatePath.getText()).getFileName()+")"+"output"+".txt";
            String encrypted = FileToString(labDecrPath.getText());
            rsaEncryptor.Decrypt(encrypted);
            a.setAlertType(Alert.AlertType.INFORMATION);
            a.setContentText("Файл "+ Paths.get(labDecrPath.getText()).getFileName() + " рашифрован\n"+ "Расшифрованный файл: "+ Paths.get(rsaEncryptor.outputPath).getFileName());
            a.show();
        }
    }

    @FXML
    private void saveInput(){
        String text = inputArea.getText();
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TEXT files (*.txt)", "*.txt");
        fc.getExtensionFilters().add(extFilter);
        if(!text.trim().isEmpty() && text!=null){
            File f = fc.showSaveDialog(null);

            if (f != null){// строка для записи
                try(FileOutputStream fos=new FileOutputStream(f.getAbsolutePath()))
                {
                    // перевод строки в байты
                    byte[] buffer = text.getBytes();

                    fos.write(buffer, 0, buffer.length);
                    a.setAlertType(Alert.AlertType.INFORMATION);
                    a.getDialogPane().setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
                    a.setContentText("Файл сохранен.\n"+"Путь: "+f.getAbsolutePath());
                    a.show();
                }
                catch(IOException ex){

                    System.out.println(ex.getMessage());
                }
            }
        }else{
                a.setAlertType(Alert.AlertType.ERROR);
                a.setContentText("Введите текст!!!");
                a.show();
            }


    }

    public static String FileToString(String path) {
        StringBuilder text = new StringBuilder();
        try (FileReader fr = new FileReader(path)) {
            BufferedReader buffReader = new BufferedReader(fr);
            String line = buffReader.readLine();
            while(line != null) {
                text.append(line).append("\n"); line = buffReader.readLine();
            }
        } catch (IOException ignored) {}
        return text.toString();
    }
}