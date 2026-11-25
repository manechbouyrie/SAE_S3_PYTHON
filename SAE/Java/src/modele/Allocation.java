package modele;

// Une ligne du fichier "allocations" (un processus sur un processeur pendant une période)
public record Allocation(String idProcessus, int debut, int fin, String processeur) {
}
