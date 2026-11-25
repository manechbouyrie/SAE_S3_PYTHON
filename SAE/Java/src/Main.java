import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import controleur.Controleur;
import java.nio.file.Paths;
import modele.Modele;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader chargeur = new FXMLLoader(Main.class.getResource("/vue/Vue.fxml"));
        Parent racine = chargeur.load();
        Controleur controleur = chargeur.getController();
        controleur.configurer(new Modele(Paths.get("Inputs", "Test1", "config.txt")));

        stage.setScene(new Scene(racine, 900, 650));
        stage.setTitle("Visualisation d'ordonnancement");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
