import os

import sys

from bluetooth import *

from picamera import PiCamera

from time import sleep

from gpiozero import Button

from ctypes import cdll

 

button = Button(18)

 

 

def braille():

    with PiCamera() as camera:

        camera.rotation = 180

        camera.start_preview(fullscreen=False, window=(100,20,445,120))

        button.wait_for_press()

        camera.resolution = (130, 40)

        camera.capture('input.png')

        camera.stop_preview()

    

    os.system('g++ -c -fPIC foo.cpp -o foo.o $(pkg-config --libs --cflags opencv)')

    os.system('g++ foo.o -shared -o libfoo.so $(pkg-config --libs --cflags opencv)')

    lib = cdll.LoadLibrary('./libfoo.so')

 

    class Foo(object):

        def __init__(self):

            self.obj = lib.Foo_new()

        def bar(self):

            lib.Foo_bar(self.obj)

    f = Foo()

    f.bar()

 

    data = []

    file = open('output.txt', 'r')

    data = file.readline()

    file.close()

 

    def receiveMsg():

        uuid = "00001101-0000-1000-8000-00805f9b34fb"

        # RFCOMM

        server_sock=BluetoothSocket(RFCOMM)

        server_sock.bind(('', PORT_ANY))

        server_sock.listen(1)

 

        port = server_sock.getsockname()[1]

 

        advertise_service(server_sock, "BTBT",

            service_id = uuid,

            service_classes = [uuid, SERIAL_PORT_CLASS],

            profiles = [SERIAL_PORT_PROFILE])

 

        print("Waiting for connection : channel %d" % port)

 

        client_sock, client_info = server_sock.accept()

        print('accepted')

        while True:

            print("Accepted connection from", client_info)

            try:

                #data = sys.argv[1]

                print(data)

                client_sock.send(data)

                client_sock.close()

                server_sock.close()

                braille()

            except IOError:

                print("disconnected")

                client_sock.close()

                server_sock.close()

                print("all done")

                break

            except KeyboardInterrupt:

                print("disconnected")

                client_sock.close()

                server_sock.close()

                print("all done")

                break

    receiveMsg()

    

braille()