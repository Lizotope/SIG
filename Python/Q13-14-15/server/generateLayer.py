#!/usr/bin/python3
# -*- coding: utf-8 -*- 

import sys, os
from postgis import Point
from drawer import Image         #je recupere la classe Image du prof
#import datetime                         # using datetime module
import database

def buildLayer(layername, ImgWidth, ImgHeight, srs, Bbox_param):
    if layername == "roads":
        return buildRoads(ImgWidth, ImgHeight, srs, Bbox_param)
    elif layername == "buildings":
        return buildBuildings(ImgWidth, ImgHeight, srs, Bbox_param)
    elif layername == "rivers":
        return buildRivers(ImgWidth, ImgHeight, srs, Bbox_param)

def buildRoads(ImgWidth, ImgHeight, srs, Bbox_param):
    return build("highway", ImgWidth, ImgHeight, srs, Bbox_param, "exist (tags,'highway') ", "line")

def buildBuildings(ImgWidth, ImgHeight, srs, Bbox_param):
    return build("building", ImgWidth, ImgHeight, srs, Bbox_param, "exist (tags,'building') ", "polygon")

def buildRivers(ImgWidth, ImgHeight, srs, Bbox_param):
    return build("waterway", ImgWidth, ImgHeight, srs, Bbox_param, "(exist (tags,'waterway') or exist(tags, 'water')) ", "line")

def build(tag, ImgWidth, ImgHeight, srs, Bbox_param, filter, typeDrawing):
    try : 
        #uniqueFileName = f"img/{tag}-" + str(hash(f"{tag}{ImgWidth}{ImgHeight}{srs}{Bbox_param}")) + ".png"
        uniqueFileName = f"img/{tag}-" + f"{ImgWidth}{ImgHeight}{srs}{Bbox_param}".replace(".", "").replace(",", "") + ".png"
        print(f"{tag}{ImgWidth}{ImgHeight}{srs}{Bbox_param}", "===>", uniqueFileName)
        # verif si le fichier existe déjà ou non
        if os.path.isfile(uniqueFileName):
            print("Fichier en cache")
            return uniqueFileName

        database.init_connection()
        #print("\nLa connexion PostgreSQL est ouverte. \n")       

        # créeation de l'Image(width, height)
        MonImg = Image(ImgWidth,ImgHeight)
        # création de la boîte englobante
        # la winteam adore le beige et le vert
        #MonImg.draw_polygon(((0,0),(ImgWidth,0),(ImgWidth,ImgHeight),(0,ImgHeight),(0,0)), (0,0,0.9,1), (0.85,0.86,0.89,0.98))

        # coordonnées des 2 coins de la bbox sont séparées par des ','
        LongMin, LatMin, LongMax, LatMax = [float(x) for x in Bbox_param.split(",")]
        
        # exécution dans postegresql de la query demandée
        # je recupère tous les chemins (w.linestring exprimé en 4326) possédant l'attribut highway et inclus dans la boite englobante exprimée dans le référentiel EPSG:3857
        # Attention les linestring sont par défaut exprimés en 4326 dans la table ways
        req2 = f"""select w.tags->'{tag}', st_transform(w.linestring, {srs}) from ways w where {filter}
        and ST_Contains(ST_GeomFromText('POLYGON(({LongMin} {LatMin},
					   			 {LongMin} {LatMax},
					   			 {LongMax} {LatMax},
					   			 {LongMax} {LatMin},
					   			 {LongMin} {LatMin}))', {srs}), st_transform(w.linestring,{srs})) is true 
        """ #and w.id = 112155
        curs2 = database.execute_query(req2)

        # affichage du résultat dans le format demandé
        # pour chaque way
        for ligne in curs2:
            # affichage ligne
            pts_norm = [0]*len(ligne[1].coords)
            i=0

            for pt in ligne[1].coords:
                #je normalise la longitude et la latitude sur les dimensions de la boîte passées en argument
                pt_norm = Point(ImgWidth-((LongMax-pt[0])/(LongMax-LongMin))*ImgWidth , ((LatMax-pt[1])/(LatMax-LatMin))*ImgHeight )
                pts_norm[i]=pt_norm
                i=i+1

            # tracage dessin
            if typeDrawing == "line":
                MonImg.draw_linestring(pts_norm, colortype(ligne[0]))
            elif typeDrawing == "polygon": 
                MonImg.draw_polygon(pts_norm, colortype(ligne[0]), colortype(ligne[0]))
            
        
        # sauvegarde de l'image dans le répertoire img créé au préalable
        MonImg.save(uniqueFileName)

        curs2.close()    
        #fermeture de la connexion à la base de données
        database.close_connection()
        #print("\nLa connexion PostgreSQL est fermée. \n")

        return uniqueFileName

    except ValueError: 
        print( "Exception ValueError - mauvaise valeur sur l'un des arguments: %f" % LongMin, " ou ", LongMax, " ou ", LatMin, " ou ", LatMax, " ou ", ImgWidth , " ou ", ImgHeight, file=sys.stderr )      


def colortype(x):
    try:
        return {
            'unclassified': (0.32, 0.42, 0.24),
            'secondary': (0.56, 0.21, 0.21),
            'primary': (0.24, 0.33, 0.41),
            'footway': (0.29,0.33,0.37),
            'track': (0.28,0.6,0.12),
            'motorway': (0.29,0.6, 0.1),
            'proposed':(0.27,0.15,0.57),
            'tertiary': (0.24,0.2,0.6),
            'trunk': (0.26, 0.58, 0.11),
            'bus_stop': (0.3,0.4,0.25),
            'tertiary_link':(0.5, 0.19,0.19),
            'motorway_link':(0.51,0.25,0.25),
            'steps': (0.22,0.30,0.44),
            'pedestrian': (0.6,0.2,0.2),
            'bridleway': (0.55,0.22,0.22),
            'secondary_link': (0.57,0.3,0.3),
            'primary_link': (0.6,0.25,0.25),
            'construction': (0.5,0.3,0.1),
            'platform': (0.45,0.4,0.15),
            'service': (0.47,0.45,0.18),
            'trunk_link': (0.12,0.42,0.45),
            'cycleway': (0.11,0.41,0.4),
            'living_street': (0.1,0.4,0.4),
            'path': (0.16,0.38,0.38),
            'residential': (0.25,0.33,0.33),
            'via_ferrata': (0.29,0.44,0.44),
            'corridor': (0.55,0.2,0.1),
            'stream': (0.0,0.0,0.9,),
            'river': (0.0,0.0,0.9,),
            'canal': (0.0,0.0,0.9,),
            'lake': (0.0,0.0,0.9,)
        }[x]
    except:
        return (0, 0, 0)

if __name__ == "__main__":
    if len( sys.argv ) < 5:
        print( "Relancer en spécifiant tous les arguments et dans le bon ordre :" )
        print( "\tArgument 1 : boite englobante bbox convertie en chaîne de caractères ")
        print( "\tArgument 2 : srid du référentiel géographique")
        print( "\tArgument 3 : largeur de l'image à générer : doit être un entier.")
        print( "\tArgument 4 : largeur de l'image à générer : doit être un entier.")
        print( "\n\t\t\t...la win team tp_sig vous remercie :-)) !\n" )
        exit()  
    #ImgWidth, ImgHeight, srs, Bbox_param;
    fileName = buildRoads(int(sys.argv[3]), int(sys.argv[4]), int(sys.argv[2]), sys.argv[1])
    print(fileName)
