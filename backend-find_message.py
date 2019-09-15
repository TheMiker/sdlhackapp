import requests
from bottle import *
from operator import itemgetter

def find_nearest_message(lat, long):
    """ returns text of nearest post to lat and long 

    param lat: a float. latitude of interest. 
    param long: a float. longitude of interes. 

    returns: a string of the message in the nearest post
        if one exists within MAX_DIST """
    MAX_DIST = 1/7500    # measured in degrees
    table = get_table()

    lat = float(lat)
    long = float(long)

    # filter out posts that are too far
    in_range = []   # all posts within a specified radius
    for post in table:
        if (abs(float(post["lat"]) - lat) < MAX_DIST and
            abs(float(post["long"]) - float(long)) < MAX_DIST):
            distance = find_distance(lat, long, float(post["lat"]), float(post["long"]))
            post_distance = (distance, post["message"])
            in_range.append(post_distance)

    if in_range == []:
        return []
    
    return max(in_range, key=itemgetter(0))[1]

def find_distance(x1, y1, x2, y2):
    """ finds euclidean distance squared between points """
    return (x1-x2)**2 + (y1-y2)**2
    
def get_table():
    url = "http://localhost:3000/messages"
    resp = requests.get(url)
    return resp.json()
    
@post('/')
def index():
    postdata = request.body.read()
    lat = float(request.forms.get("lat"))
    long = float(request.forms.get("long"))

    message = find_nearest_message(lat, long)
    if not message:
        return HTTPResponse(status=204)
    else:
        respDict = {"message":message}
        return HTTPResponse(status=200, body=respDict)

run(host='0.0.0.0', port=8080)

#example test: curl --data "lat=1&long=2" localhost:8080
