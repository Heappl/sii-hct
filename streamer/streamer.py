import gpxpy
import gpxpy.gpx
import time
import itertools
import zmq

def pairwise(iterable):
    "s -> (s0,s1), (s1,s2), (s2, s3), ..."
    a, b = itertools.tee(iterable)
    next(b, None)
    return zip(a, b)   
# Parsing an existing file:
# -------------------------

def send_position(socket, lat, lon):
    print('Point at ({0},{1})'.format(lat, lon))
    socket.send_string('[{0},{1},{2}]'.format('1', lat, lon))

if __name__ == "__main__":
    context = zmq.Context()
    socket = context.socket(zmq.REQ)
    socket.connect("tcp://10.254.38.30:5555")
    gpx_file = open('run.gpx', 'r')
    gpx = gpxpy.parse(gpx_file)
    for track in gpx.tracks:
        for segment in track.segments:
            for pa, pb in pairwise(segment.points):
                send_position(socket, pa.latitude, pa.longitude)
                time.sleep((pb.time-pa.time).total_seconds() / 4.0)
                message = socket.recv()
                
