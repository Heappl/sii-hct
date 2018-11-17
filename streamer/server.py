import time
import zmq
import json
import sqlite3

conn = sqlite3.connect('example.db')
context = zmq.Context()
socket = context.socket(zmq.REP)
socket.bind("tcp://10.254.38.30:5555")

# the server should be able to collect live 
# running position and propagate it to web app and phone app

positions = {}



if __name__ == "__main__":
    while True:
        #  Wait for next request from client
        m = socket.recv()
        id, lat, lon = json.loads(m)
        positions[id] = (lat, lon)
        print("Received position: %d, %d, %d" % (id,lat,lon))

        #  Send reply back to client
        socket.send_string(json.dumps(positions))
        print(json.dumps(positions))
        
