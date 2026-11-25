# lecture.py : lecture de tous les fichiers d'entrée
import csv
import json
import os


def lire_config(chemin_config):
    """Lit config.txt (lignes 'cle = valeur') et renvoie un dictionnaire."""
    config = {}
    with open(chemin_config, encoding="utf-8") as f:
        for ligne in f:
            ligne = ligne.strip()
            if ligne == "" or ligne.startswith("#"):
                continue
            cle, valeur = ligne.split("=", 1)
            config[cle.strip()] = valeur.strip()
    return config


def lire_processus(chemin):
    """Lit processus.csv et renvoie une liste de dictionnaires."""
    processus = []
    with open(chemin, encoding="utf-8") as f:
        for ligne in csv.DictReader(f):
            processus.append({
                "id": ligne["id_processus"],
                "date_soumission": int(ligne["date_soumission"]),
                "duree": int(ligne["duree_execution"]),
                "ram": int(ligne["ram_necessaire"]),
                "priorite": int(ligne["priorite"]),
                "date_butoir": int(ligne["date_butoir"]),
            })
    return processus


def lire_ressources(chemin):
    """Lit ressources.json."""
    # Le config.txt du sujet parle de 'ressources.csv' alors que le fichier
    # fourni est 'ressources.json' : on essaie donc aussi le .json.
    if not os.path.exists(chemin):
        chemin = os.path.splitext(chemin)[0] + ".json"
    with open(chemin, encoding="utf-8") as f:
        return json.load(f)


def lire_energie(chemin):
    """Lit energie.csv et renvoie une liste : energie[t] = énergie à l'instant t."""
    energie = []
    with open(chemin, encoding="utf-8") as f:
        for ligne in csv.DictReader(f):
            energie.append(int(ligne["energie_disponible"]))
    return energie
