# ordonnanceur.py : les politiques d'ordonnancement (FIFO, RR, EDF, PRIORITE)
#
# Principe : on simule le temps pas par pas (t = 0, 1, 2, ...).
# À chaque pas de temps :
#   1. les processus qui arrivent entrent dans la file d'attente
#   2. on calcule combien de processeurs on peut alimenter (énergie verte)
#   3. on donne les processeurs libres aux premiers processus de la file
#      qui rentrent en RAM
#   4. on exécute 1 unité de temps


def cle_de_tri(politique, p):
    """Clé de tri de la file d'attente (plus petit = passe en premier)."""
    if politique == "FIFO":
        # date de soumission, puis priorité la plus haute en premier
        return (p["date_soumission"], -p["priorite"])
    if politique == "EDF":
        # date butoir la plus proche en premier
        return (p["date_butoir"], -p["priorite"])
    if politique == "PRIORITE":
        # variante 1 : priorité la plus haute en premier, sans préemption
        return (-p["priorite"], p["date_soumission"])


def ordonnancer(politique, liste_processus, ressources, energie, quantum=2):
    """Simule une politique. Renvoie (processus, allocations, energie_consommee)."""
    # copie des processus avec les champs dont on a besoin
    procs = []
    for p in liste_processus:
        copie = dict(p)
        copie["reste"] = p["duree"]   # temps d'exécution restant
        copie["debut"] = None         # première date d'exécution
        copie["fin"] = None           # date de fin
        procs.append(copie)

    cpus = ressources["processeur"]
    energie_par_cpu = ressources["energieParProc"]
    ram_totale = ressources["ramTotale"]

    file_attente = []   # processus prêts, en attente d'un processeur
    en_cours = {}       # processeur -> processus en train de s'exécuter
    tranche = {}        # id processus -> nombre de pas consécutifs (pour RR)
    pas_executes = []   # liste de (t, id_processus, processeur)
    energie_consommee = 0

    for t in range(len(energie)):

        # 1. arrivée des nouveaux processus (triés par priorité si même date)
        arrives = [p for p in procs if p["date_soumission"] == t]
        arrives.sort(key=lambda p: -p["priorite"])
        file_attente.extend(arrives)

        # Round Robin : un processus qui a fini son quantum retourne en fin de file
        if politique == "RR":
            for cpu, p in list(en_cours.items()):
                if tranche[p["id"]] >= quantum:
                    del en_cours[cpu]
                    file_attente.append(p)

        # 2. nombre de processeurs alimentables avec l'énergie verte de cet instant
        nb_alimentables = min(len(cpus), energie[t] // energie_par_cpu)

        # si l'énergie a baissé, on suspend les processus en trop
        # (ils retournent en tête de file)
        while len(en_cours) > nb_alimentables:
            cpu = list(en_cours)[-1]
            p = en_cours.pop(cpu)
            tranche[p["id"]] = 0
            file_attente.insert(0, p)

        # 3. tri de la file (sauf RR qui garde l'ordre d'arrivée)
        if politique != "RR":
            file_attente.sort(key=lambda p: cle_de_tri(politique, p))

        # attribution des processeurs libres
        ram_utilisee = sum(p["ram"] for p in en_cours.values())
        for cpu in cpus:
            if len(en_cours) >= nb_alimentables:
                break
            if cpu in en_cours:
                continue
            # on cherche le premier processus de la file qui rentre en RAM
            for p in file_attente:
                if ram_utilisee + p["ram"] <= ram_totale:
                    file_attente.remove(p)
                    en_cours[cpu] = p
                    ram_utilisee += p["ram"]
                    tranche[p["id"]] = 0
                    if p["debut"] is None:
                        p["debut"] = t
                    break

        # 4. exécution d'une unité de temps
        for cpu, p in list(en_cours.items()):
            pas_executes.append((t, p["id"], cpu))
            p["reste"] -= 1
            tranche[p["id"]] += 1
        energie_consommee += len(en_cours) * energie_par_cpu

        # fin des processus terminés
        for cpu, p in list(en_cours.items()):
            if p["reste"] == 0:
                p["fin"] = t + 1
                del en_cours[cpu]

        # tout est fini : on arrête
        if all(p["fin"] is not None for p in procs):
            break

    return procs, regrouper(pas_executes), energie_consommee


def regrouper(pas_executes):
    """Regroupe les pas consécutifs d'un même processus sur un même processeur
    en une seule ligne : (id, debut, fin, processeur)."""
    segments = []
    for t, id_proc, cpu in pas_executes:
        prolonge = False
        for s in segments:
            if s["id"] == id_proc and s["cpu"] == cpu and s["fin"] == t:
                s["fin"] = t + 1
                prolonge = True
                break
        if not prolonge:
            segments.append({"id": id_proc, "debut": t, "fin": t + 1, "cpu": cpu})
    segments.sort(key=lambda s: (s["debut"], s["cpu"]))
    return segments
