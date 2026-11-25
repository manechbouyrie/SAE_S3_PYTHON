package modele;

// Une ligne du fichier de métriques (un algorithme)
public record Metriques(String algo, double attenteMoyen, double reponseMoyen, int makespan,
                        int energieConsommee, int energieDisponible, double tauxUtilisation) {
}
