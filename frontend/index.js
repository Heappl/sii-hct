var express = require('express');
var path = require('path');
var app = express();
var session = require('express-session');
var server = require('http').Server(app);

server.listen(8080);
app.use(express.static('public'));
app.use('/vendors', express.static('node_modules/gentelella/vendors/'));
app.use('/css', express.static('node_modules/gentelella/build/css/'));
app.use('/js', express.static('node_modules/gentelella/build/js/'));
app.use('/images', express.static('node_modules/gentelella/build/images/'));
app.set('view engine', 'ejs');

// example user
// todo: db connection
var user = {
  level: 28,
  name: "Kitty Katze",
  guild: "Red Cats",
  kmp: 120,
  max_kmp: 250,
};

app.get('/', function (req, res) {
  res.render('pages/index', {content: 'main', user: user});
});

app.get('/area', function(req, res) {
  res.render('pages/index', {content: 'area', user: user});
});
