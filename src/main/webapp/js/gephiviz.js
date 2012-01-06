var canvas;
var ctx;

$(document).ready(function() {
    canvas = document.getElementById('drawing-canvas');
    ctx = canvas.getContext('webgl-2d');

    loadGraph();
});

function loadGraph() {
    $('body').addClass('loading');

    $.ajax({
        type: 'GET',
        url: '/render',
        dataType: 'xml',
        success: function(data) {
            canvg(canvas, data);
            $('body').removeClass('loading');
        }
    });
}
