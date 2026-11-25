# metriques.py : calcul des métriques d'un ordonnancement


def calculer_metriques(procs, energie_consommee, energie_disponible):
    finis = [p for p in procs if p["fin"] is not None]
    n = len(finis)

    if n == 0:
        attente = reponse = makespan = 0
    else:
        # temps d'attente = temps passé dans la file = fin - soumission - durée
        attente = sum(p["fin"] - p["date_soumission"] - p["duree"] for p in finis) / n
        # temps de réponse = délai avant d'obtenir le processeur la 1ère fois
        reponse = sum(p["debut"] - p["date_soumission"] for p in finis) / n
        # makespan = date de fin du dernier processus
        makespan = max(p["fin"] for p in finis)

    if energie_disponible > 0:
        taux = energie_consommee / energie_disponible
    else:
        taux = 0

    return {
        "temps_attente_moyen": round(attente, 2),
        "temps_reponse_moyen": round(reponse, 2),
        "makespan": makespan,
        "energie_consommee": energie_consommee,
        "energie_disponible": energie_disponible,
        "taux_utilisation": round(taux, 4),
    }
