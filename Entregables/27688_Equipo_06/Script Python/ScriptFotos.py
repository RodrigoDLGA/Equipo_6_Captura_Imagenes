import datetime
import platform
import os
import time
import datetime
import webbrowser
import pprint
import requests
url ="http://gloperenab.me/Proyecto1Equipo/consulta_proyectos.php"
r=requests.get(url)
data= r.text

ciclo = 1

while ciclo == 1:
    print("Seleccione una opcion: ")
    print("1.-Listado de proyectos")


    opcion = (int(input()))
    print(opcion)
    if opcion == 1:
        datado = data.split(sep="|", maxsplit=100)
        for sub in datado:
            sub_split = sub.split(",")
            print(sub_split)
        print(" ")
        print("Ingrese una opción")
        print("1.- Ingresar nuevos prametros")
        print("2.- Descargar proyecto desde web")
        print("3.- Tomar instantanea")
        print("4.- Finalizar ejecución")
        opcion2 = (int(input()))

    if opcion2 == 1:
        print(" ")
        # cursor.execute("SELECT * FROM parametros")
        # print(" ")
        print("Ingresa la fecha y hora de inicio en el siguiente formato  YYYY-MM-DD HH:MM:SS")
        datas = input('Ingrese fecha [YYYY-MM-DD HH:MM:SS]: ')
        datas = datetime.datetime.strptime(datas, '%Y-%m-%d %H:%M:%S')
        nowdate = datas.date().isoformat()
        print(" ")
        print(datas)
        print("Ingresa la cantidad de fotografías que desea tomar en ese intervalo de tiempo:")
        cantidadfotos = (int(input()))

        while cantidadfotos <= 0:
            print("entró al while")
            if cantidadfotos <= 0:
                print("Ingrese una cantidad de fotografias mayor a 0.")
                cantidadfotos = (int(input()))
        print(" ")
        print("Ingresa el intervalo en el que desea tomar las fotografias:")
        intervaloproyecto = (int(input()))

        while intervaloproyecto <= 0:
            print("entró al while")
            if intervaloproyecto <= 0:
                print("Ingrese un intervalo mayor a 0 o tome una instantanea.")
                intervaloproyecto = (int(input()))

        pload = {'entrada': datas,
                 'salida': datas,
                 'intervalo': intervaloproyecto,
                 'cantidad': cantidadfotos
                 }
        r = requests.post('http://www.gloperenab.me/Proyecto1Equipo/insertar_parametros.php',data = pload)

        print('Orden enviada.')
        #    cursor.execute("INSERT INTO parametros(entrada)  VALUES('{0}')".format(datas))
        #    conexion.commit()

    if opcion2 == 2:
        url="http://gloperenab.me/Proyecto1Equipo/index.php"
        webbrowser.open("http://gloperenab.me/Proyecto1Equipo/index.php", new=2, autoraise=True)
        webbrowser.open_new_tab(url)
    if opcion2 == 3:
        print("Instantanea")
        pload = {'entrada': "2021-05-08 15:15:15",
                 'salida': "2021-05-08 15:15:15",
                 'intervalo': 0,
                 'cantidad': 1
                 }
        r = requests.post('http://www.gloperenab.me/Proyecto1Equipo/insertar_parametros.php', data=pload)
        print("Se mandó la orden de la instantanea")
    if opcion2 == 4:
        quit()

