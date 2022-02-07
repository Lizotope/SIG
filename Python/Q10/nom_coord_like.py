#!/usr/bin/python3
# -*- coding: utf-8 -*- 

import sys
import psycopg2 as p2
import postgis as pg
from postgis.psycopg import register   #pour que les types se transmettent de postgresql dans le postgis de python


try : 

    if len( sys.argv ) == 1:
        print( "Relancer en spécifiant des arguments !" )
        print( "\tla win team tp_sig vous remercie !" )
        exit()    

    conn = p2.connect(
        user = "bourgeel",
        password = "bourgeel",
        host = "postgresql.ensimag.fr",
        port = "5432",
        database = "osm"
        )    
    register(conn)                      # pour que les types se transmettent de postgresql dans le postgis de python
    print("\nLa connexion PostgreSQL est ouverte. \n")       

    curs = conn.cursor() 
    
    # je récupère l'argument de la ligne de commande
    #print("param d''entrée : ", strParam,"\n")       
    strParam = sys.argv[1]
    
    # exécution dans postegresql de la query demandée
    curs.execute("select tags->'name', geom from nodes where tags->'name' like (%s)", (strParam,))  # pour tester requete, ex d'id :id = 996124605       
    
    # affichage du résultat dans le format demandé
    for ligne in curs:
        print(ligne[0],"\t| ", ligne[1].x, "\t| ", ligne[1].y)
    
    #lign1 = curs.fetchone()    # je fetche la 1er ligne de l'output de la query sql   

    #fermeture de la connexion à la base de données
    curs.close()
    conn.close()
    print("\nLa connexion PostgreSQL est fermée. \n")

except ValueError: 
    print( "Exception ValueError - mauvaise valeur de paramètre: %s" % strParam, file=sys.stderr )
except (Exception, psycopg2.Error) as error :
    print ("Erreur lors de la connexion à PostgreSQL", error)         


