# pip install virtualenv
# virtualenv venv
# venv\scripts\activate
# pip install Flask
# chmod a+x server.py
# ./server.py
from flask import Flask, request, render_template, jsonify, make_response
from werkzeug import secure_filename
import os

# Flask constructor takes the name of current module (__name__) as argument.
app = Flask(__name__)

# Defines path for upload folder
app.config['UPLOAD_FOLDER'] = 'upload'
# Specifies maximum size of file yo be uploaded – in bytes: 1MB
app.config['MAX_CONTENT_PATH'] = 1024 * 1024

@app.errorhandler(404)
def not_found(error):
   return make_response(jsonify({'error': 'Not found'}), 404)

# The route() function of the Flask class is a decorator, 
# which tells the application which URL should call the associated function.
#    app.route(rule, options)
#        The rule parameter represents URL binding with the function.
#        The options is a list of parameters to be forwarded to the underlying Rule object.
#
@app.route('/')
def ping():
   #return 'The server is running.'
   message = "Photo Capture Service Test UI"
   return render_template('index.html', message=message)

@app.route('/photocapture/<trackingId>', methods = ['POST'])
def photocapture_service(trackingId):
   trackingId2 = request.form['trackingId']
   type = request.form['type']
   profileId = request.form['profileId']
   imageNumber = request.form['imageNumber']
   files = request.files.getlist("file")
   
   for f in files:
      f.save(os.path.join(app.config['UPLOAD_FOLDER'],secure_filename(f.filename)))
   return jsonify({'success': 
      'Photo Capture Service {}, {}, {}, {}, {}'.format(trackingId, trackingId2, type, profileId, imageNumber)
   })

@app.route('/photocapture/<trackingId>/update', methods = ['POST'])
def photocapture_update_service(trackingId):
   #if not request.json or not 'title' in request.json:
   #   abort(400)
   
   trackingId2 = request.form['trackingId']
   payeeId = request.form['payeeId']
    # title = request.json['title'],
    # description = request.json.get('description', "")
   
   items = [
    {
        'trackingId': '1000',
        'payeeId': '213456',
        'description': u'test tracking id 1', 
        'done': False
    },
    {
        'trackingId': '1001',
        'payeeId': '213457',
        'description': u'test tracking id 2', 
        'done': False
    }
   ]
   
   item = {
        'trackingId': trackingId2,
        'payeeId': payeeId,
        'description': u'test tracking id 3', 
        'done': False
   }
   items.append(item)   
   return jsonify({'success': 
	  'Photo Capture Service {} update {}, {}'.format(trackingId, trackingId2, payeeId),
	  'items': items
   })

#app.add_url_rule(‘/’, 'photocapture', photocapture_service)

if __name__ == '__main__':
   # the run() method of Flask class runs the application
   #   app.run(host, port, debug, options)
   #
   #   host  Hostname to listen on. 
   #         Defaults to 127.0.0.1 (localhost). Set to ‘0.0.0.0’ to have server available externally
   #   port  Defaults to 5000
   #   debug Defaults to false. If set to true, provides a debug information
   #   options To be forwarded to underlying Werkzeug server.
   #app.run()
   app.run(debug = True)