#!/usr/bin/python3

from http.server import BaseHTTPRequestHandler, HTTPServer
import urllib.parse as urlparse

import gen_highways_box_server

PORT_NUMBER = 4242

"""requete HTTP à mettre dans le browser :
   http://localhost:4242/wms?request=GetMap&layers=LayerName&height=800&width=1200&srs=3857&bbox=0103000020110F00000100000005000000E35BEE31325D23413FC6EB8C27815541A415DC172AB423413FC6EB8C27815541A415DC172AB4234173F478A291905541E35BEE31325D234173F478A291905541E35BEE31325D23413FC6EB8C27815541
"""

class WMSHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path.startswith("/wms"):
            # Ici on récupère les valeurs de paramètres GET
            params = urlparse.parse_qs(urlparse.urlparse(self.path).query)
            # Vérification que tous les paramètres sont là :
            #request=GetMap&layers=LayerName&height=800&width=1000&srs=3857&bbox=bboxvalue
            requestParamExist = False
            layersParamExist = False
            heightParamExist = False
            widthParamExist = False
            srsParamExist = False
            bboxParamExist = False

            for p in params:
                if p == "request":
                    request = params["request"][0]
                    requestParamExist = True
                elif p == "layers":
                    layers = params["layers"][0]
                    layersParamExist = True
                elif p == "height":
                    height = params["height"][0]
                    heightParamExist = True
                elif p == "width":
                    width = params["width"][0]
                    widthParamExist = True
                elif p == "srs":
                    srs = params["srs"][0]
                    srsParamExist = True
                elif p == "bbox":
                    bbox = params["bbox"][0]
                    bboxParamExist = True

            # si au moins un des paramètres n'est pas présent, on envoie un message d'erreur
            if not requestParamExist or not layersParamExist or not heightParamExist or not widthParamExist or not srsParamExist or not bboxParamExist:
                self.send_error(404, f"""
                            Paramètre(s) manquant(s) :
                                request : {requestParamExist}
                                layers  : {layersParamExist}
                                height  : {heightParamExist}
                                width   : {widthParamExist}
                                srs     : {srsParamExist}
                                bbox    : {bboxParamExist}"""
                            )
            # sinon on appelle le script de génération d'image , puis on envoie l'image au browser
            else:
                if request == "GetMap":
                    if srs == "3857":
                        fileToServe = gen_highways_box_server.buildRoads(int(width), int(height), srs, bbox)
                        self.send_png_image(fileToServe)
                    else:
                        self.send_error(404, 'SRS not valid : %s' % srs)
                else:
                    self.send_error(404, 'Request param invalid : %s' % request)
        else:
            self.send_error(404, 'Fichier non trouvé : %s' % self.path)


    def send_html_text(self, content):
        self.send_response(200)
        self.send_header('Content-type', 'html; charset=UTF-8')
        self.end_headers()
        self.wfile.write(bytes(content, "utf-8"))

    def send_plain_text(self, content):
        self.send_response(200)
        self.send_header('Content-type', 'text/plain; charset=UTF-8')
        self.end_headers()
        self.wfile.write(bytes(content, "utf-8"))

    def send_png_image(self, filename):
        self.send_response(200)
        self.send_header('Content-type', 'image/png')
        self.end_headers()
        with open(filename, 'rb') as file:
            self.wfile.write(file.read())

    def send_html_file(self, filename):
        self.send_response(200)
        self.end_headers()
        self.serveFile(filename)


if __name__ == "__main__":
    try:
        # Ici on crée un serveur web HTTP, et on affecte le traitement
        # des requêtes à notre releaseHandler ci-dessus.
        server = HTTPServer(('', PORT_NUMBER), WMSHandler)
        print('Serveur démarré sur le port ', PORT_NUMBER)
        print('Ouvrez un navigateur et tapez dans la barre d\'url :'
              + ' http://localhost:%d/' % PORT_NUMBER)

        # Ici, on demande au serveur d'attendre jusqu'à la fin des temps...
        server.serve_forever()

    # ...sauf si l'utilisateur l'interrompt avec ^C par exemple
    except KeyboardInterrupt:
        print('^C reçu, je ferme le serveur. Merci.')
        server.socket.close()
