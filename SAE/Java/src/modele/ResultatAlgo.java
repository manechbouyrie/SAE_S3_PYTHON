package modele;

import java.util.List;

// Tous les résultats d'un algorithme
public record ResultatAlgo(List<Processus> processus, List<Allocation> allocations) {
}
