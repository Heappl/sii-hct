
function getTile(i) {
    return [(i % 14) * -32, Math.floor(i / 14) * -32];
}

function generate_base(name, map, width) {
    var body = document.getElementsByClassName(name)[0];
    var tbl = document.createElement('table');
    var tbdy = document.createElement('tbody');
    for (var i = 0; i < map.length; i++) {
        var tr = document.createElement('tr');
        for (var j = 0; j < map[i].length; j++) {
            var td = document.createElement('td');
            td.classList.add('tileset');
            var coords = getTile(map[i][j]);
            td.setAttribute('style', 'width:32px;height:32px;background-position:' + coords[0] + 'px ' + coords[1]  + 'px');
            td.appendChild(document.createTextNode('\u0020'));
            tr.appendChild(td);
        }
        tbdy.appendChild(tr);   
    }
    tbl.appendChild(tbdy);
    body.appendChild(tbl);
}
