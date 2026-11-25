# main.py : point d'entrée
#
# Utilisation (depuis le dossier du projet) :
#   python Code/main.py Inputs/Test1/config.txt   -> lance un seul test
#   python Code/main.py                           -> lance tous les tests de Inputs/
import os
import sys

from lecture import lire_config, lire_processus, lire_ressources, lire_energie
from ordonnanceur import ordonnancer
from metriques import calculer_metriques
from ecriture import ecrire_global, ecrire_allocations, ecrire_metriques

DOSSIER_PROJET = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))

# nom de l'algorithme dans config.txt -> nom utilisé dans les clés des fichiers de sortie
NOMS_FICHIERS = {"FIFO": "FIFO", "RR": "RoundRobin", "EDF": "EDF", "PRIORITE": "Priorite"}


def lancer_test(chemin_config):
    dossier_test = os.path.dirname(os.path.abspath(chemin_config))
    nom_test = os.path.basename(dossier_test)
    dossier_sortie = os.path.join(DOSSIER_PROJET, "Outputs", nom_test)
    os.makedirs(dossier_sortie, exist_ok=True)

    # lecture des entrées (chemins relatifs au dossier du test)
    config = lire_config(chemin_config)
    processus = lire_processus(os.path.join(dossier_test, config["fichier_processus"]))
    ressources = lire_ressources(os.path.join(dossier_test, config["fichier_ressources"]))
    energie = lire_energie(os.path.join(dossier_test, config.get("fichier_energie", "energie.csv")))
    quantum = int(config.get("quantum", 2))
    algos = [a.strip().upper() for a in config["algorithmes"].split(",")]

    energie_disponible = sum(energie)
    metriques_par_algo = {}

    for algo in algos:
        if algo not in NOMS_FICHIERS:
            print("Algorithme inconnu, ignoré :", algo)
            continue
        nom = NOMS_FICHIERS[algo]
        print("Test", nom_test, ": algorithme", algo)

        procs, segments, conso = ordonnancer(algo, processus, ressources, energie, quantum)

        if any(p["fin"] is None for p in procs):
            print("  Attention : certains processus ne sont pas terminés (énergie ou RAM insuffisante)")

        # noms des fichiers de sortie lus dans config.txt (valeur par défaut sinon)
        f_global = config.get("fichier_global_" + nom, "resultats_" + nom + "_global.csv")
        f_alloc = config.get("fichier_allocations_" + nom, "resultats_" + nom + "_allocations.csv")
        ecrire_global(os.path.join(dossier_sortie, f_global), procs)
        ecrire_allocations(os.path.join(dossier_sortie, f_alloc), segments)

        metriques_par_algo[algo] = calculer_metriques(procs, conso, energie_disponible)

    f_metriques = config.get("fichier_metriques", "metriques.csv")
    ecrire_metriques(os.path.join(dossier_sortie, f_metriques), metriques_par_algo)
    print("Résultats écrits dans", dossier_sortie)


if __name__ == "__main__":
    if len(sys.argv) > 1:
        lancer_test(sys.argv[1])
    else:
        dossier_inputs = os.path.join(DOSSIER_PROJET, "Inputs")
        for nom in sorted(os.listdir(dossier_inputs)):
            config = os.path.join(dossier_inputs, nom, "config.txt")
            if os.path.exists(config):
                lancer_test(config)
