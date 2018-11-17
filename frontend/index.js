var express = require('express');
var path = require('path');
var app = express();
var session = require('express-session');
var server = require('http').Server(app);

server.listen(8080);
app.use(express.static('public'));
app.set('view engine', 'ejs');
app.get('/', function (req, res) {
  res.render('pages/index');
});
