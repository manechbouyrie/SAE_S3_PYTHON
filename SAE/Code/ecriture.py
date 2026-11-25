# ecriture.py : écriture des fichiers de sortie
import csv


def ecrire_global(chemin, procs):
    """Une ligne par processus : soumission, début, fin, RAM."""
    with open(chemin, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["id_processus", "date_soumission", "date_debut",
                    "date_fin", "ram_demandee", "ram_utilisee"])
        for p in procs:
            debut = p["debut"] if p["debut"] is not None else -1
            fin = p["fin"] if p["fin"] is not None else -1   # -1 = pas terminé
            w.writerow([p["id"], p["date_soumission"], debut, fin,
                        p["ram"], p["ram"]])


def ecrire_allocations(chemin, segments):
    """Une ligne par période d'exécution sur un processeur."""
    with open(chemin, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["id_processus", "date_debut", "date_fin", "id_processeur"])
        for s in segments:
            w.writerow([s["id"], s["debut"], s["fin"], s["cpu"]])


def ecrire_metriques(chemin, metriques_par_algo):
    """Un seul fichier, une ligne par algorithme."""
    with open(chemin, "w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        w.writerow(["algorithme", "temps_attente_moyen", "temps_reponse_moyen",
                    "makespan", "energie_consommee", "energie_disponible",
                    "taux_utilisation"])
        for algo, m in metriques_par_algo.items():
            w.writerow([algo, m["temps_attente_moyen"], m["temps_reponse_moyen"],
                        m["makespan"], m["energie_consommee"],
                        m["energie_disponible"], m["taux_utilisation"]])
