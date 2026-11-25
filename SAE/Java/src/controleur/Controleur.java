package controleur;

import java.io.IOException;
import java.util.List;
import java.util.function.Function;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import modele.Allocation;
import modele.Metriques;
import modele.Modele;
import modele.Processus;
import modele.ResultatAlgo;

public class Controleur {

    @FXML private TextField fichierProcessus;
    @FXML private TextField fichierRessources;
    @FXML private TextField fichierMetriques;
    @FXML private TextField quantum;
    @FXML private CheckBox fifo;
    @FXML private CheckBox roundRobin;
    @FXML private CheckBox edf;
    @FXML private CheckBox priorite;
    @FXML private Button boutonLancer;
    @FXML private Label message;
    @FXML private ComboBox<String> choixAlgorithme;
    @FXML private TableView<Processus> tableProcessus;
    @FXML private TableView<Allocation> tableAllocations;
    @FXML private TableView<Metriques> tableMetriques;

    private Modele modele;

    @FXML
    private void initialize() {
        ajouterColonne(tableProcessus, "Processus", Processus::id);
        ajouterColonne(tableProcessus, "Soumission", Processus::soumission);
        ajouterColonne(tableProcessus, "Début", Processus::debut);
        ajouterColonne(tableProcessus, "Fin", Processus::fin);
        ajouterColonne(tableProcessus, "RAM", Processus::ram);

        ajouterColonne(tableAllocations, "Processus", Allocation::idProcessus);
        ajouterColonne(tableAllocations, "Début", Allocation::debut);
        ajouterColonne(tableAllocations, "Fin", Allocation::fin);
        ajouterColonne(tableAllocations, "Processeur", Allocation::processeur);

        ajouterColonne(tableMetriques, "Algorithme", Metriques::algo);
        ajouterColonne(tableMetriques, "Attente moyenne", Metriques::attenteMoyen);
        ajouterColonne(tableMetriques, "Réponse moyenne", Metriques::reponseMoyen);
        ajouterColonne(tableMetriques, "Makespan", Metriques::makespan);
        ajouterColonne(tableMetriques, "Énergie consommée", Metriques::energieConsommee);
        ajouterColonne(tableMetriques, "Énergie disponible", Metriques::energieDisponible);
        ajouterColonne(tableMetriques, "Utilisation", Metriques::tauxUtilisation);

        boutonLancer.setOnAction(event -> lancer());
        choixAlgorithme.setOnAction(event -> afficherAlgorithme());
    }

    public void configurer(Modele modele) {
        this.modele = modele;
        fichierProcessus.setText(modele.getConfig().get("fichier_processus"));
        fichierRessources.setText(modele.getConfig().get("fichier_ressources"));
        fichierMetriques.setText(modele.getConfig().get("fichier_metriques"));
        quantum.setText(modele.getConfig().get("quantum"));

        List<String> algorithmes = modele.getAlgos();
        fifo.setSelected(algorithmes.contains("FIFO"));
        roundRobin.setSelected(algorithmes.contains("RR"));
        edf.setSelected(algorithmes.contains("EDF"));
        priorite.setSelected(algorithmes.contains("PRIORITE"));

        try {
            modele.chargerResultats();
            afficherResultats();
            message.setText("Résultats existants chargés.");
        } catch (IOException e) {
            message.setText("Résultats non chargés : " + e.getMessage());
        }
    }

    private void lancer() {
        List<String> algorithmes = algorithmesCoches();
        if (algorithmes.isEmpty()) {
            message.setText("Sélectionnez au moins un algorithme.");
            return;
        }

        try {
            var config = modele.getConfig();
            config.set("fichier_processus", fichierProcessus.getText().trim());
            config.set("fichier_ressources", fichierRessources.getText().trim());
            config.set("fichier_metriques", fichierMetriques.getText().trim());
            config.set("quantum", quantum.getText().trim());
            config.set("algorithmes", String.join(",", algorithmes));
            config.sauvegarder();

            String sortie = modele.lancerPython();
            modele.chargerResultats();
            afficherResultats();
            message.setText(sortie.isBlank() ? "Calcul terminé." : sortie.trim());
        } catch (Exception e) {
            message.setText("Erreur : " + e.getMessage());
        }
    }

    private void afficherResultats() {
        choixAlgorithme.getItems().setAll(modele.getAlgos());
        tableMetriques.setItems(FXCollections.observableArrayList(modele.getMetriques()));
        if (!choixAlgorithme.getItems().isEmpty()) {
            choixAlgorithme.getSelectionModel().selectFirst();
            afficherAlgorithme();
        } else {
            tableProcessus.getItems().clear();
            tableAllocations.getItems().clear();
        }
    }

    private void afficherAlgorithme() {
        String algo = choixAlgorithme.getValue();
        ResultatAlgo resultat = (algo == null) ? null : modele.getResultat(algo);
        if (resultat != null) {
            tableProcessus.setItems(FXCollections.observableArrayList(resultat.processus()));
            tableAllocations.setItems(FXCollections.observableArrayList(resultat.allocations()));
        } else {
            tableProcessus.getItems().clear();
            tableAllocations.getItems().clear();
        }
    }

    private List<String> algorithmesCoches() {
        var algorithmes = new java.util.ArrayList<String>();
        if (fifo.isSelected()) algorithmes.add("FIFO");
        if (roundRobin.isSelected()) algorithmes.add("RR");
        if (edf.isSelected()) algorithmes.add("EDF");
        if (priorite.isSelected()) algorithmes.add("PRIORITE");
        return algorithmes;
    }

    private <T> void ajouterColonne(TableView<T> table, String titre, Function<T, Object> valeur) {
        TableColumn<T, String> colonne = new TableColumn<>(titre);
        colonne.setCellValueFactory(cellule ->
                new SimpleStringProperty(String.valueOf(valeur.apply(cellule.getValue()))));
        colonne.setPrefWidth(130);
        table.getColumns().add(colonne);
    }
}
