$(document).ready(function() {
    var viewer = new Seadragon.Viewer("viewport");
    viewer.openDzi("map/map.xml");
});

function renderGraph() {
    $('body').addClass('loading');

    $.ajax({
        type: 'GET',
        url: '/render',
        dataType: 'xml',
        success: function(data) {
            // render stuff
            $('body').removeClass('loading');
        }
    });
}
