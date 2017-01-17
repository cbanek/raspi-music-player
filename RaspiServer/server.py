import json
import os.path
import subprocess

import flask

app = flask.Flask(__name__)

player = None
volume = 80

@app.route("/music", methods=['GET'])
def get_music():
  files = os.listdir("/home/pi/Music/")
  return json.dumps({'files': files})

@app.route("/music", methods=['POST'])
def play_rain():
  global player
  song = os.path.join("/home/pi/Music", flask.request.get_json()['song'])

  if player is not None:
    player.terminate()

  player = subprocess.Popen(["mpg123", song])
  return json.dumps({'status': 'ok'})

@app.route("/volume", methods=['GET'])
def get_volume():
  global volume
  return json.dumps({'level': volume})

@app.route("/volume", methods=['POST'])
def set_volume():
  global volume
  volume = flask.request.get_json()['level']

  p = subprocess.Popen(["amixer", "set", "PCM", str(volume) + "%"])
  p.wait()
  return json.dumps({'status': 'ok'})

if __name__ == "__main__":
  app.run(debug=True, host='0.0.0.0', use_reloader=False)
