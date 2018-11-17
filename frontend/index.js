var express = require('express');
var path = require('path');
var app = express();
var session = require('express-session');
var server = require('http').Server(app);

var io = require('socket.io')(server);

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

app.get('/wtf', function(req, res) {
  res.render('pages/index', {content: 'wtf', user: user});
});

app.get('/guildchat', function(req, res) {
  res.render('pages/index', {content: 'guildchat', user: user});
});

app.get('/bugreport', function(req, res) {
  res.render('pages/index', {content: 'bugreport', user: user});
});

io.on('connection', function(socket) {
  socket.broadcast.emit('chat_redcats', '(new user joined chat)');
  socket.on('disconnect', function(){
    socket.broadcast.emit('chat_redcats', '(user left chat)');
  });
  socket.on('chat_redcats', function(msg){
    io.emit('chat_redcats', msg);
  });

  socket.on('feedback', function(msg){ console.log(msg); })
});