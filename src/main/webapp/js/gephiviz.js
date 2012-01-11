$(document).ready(function() {
    var viewer = new Seadragon.Viewer("viewport");

    // remove full page button from the seadragon viewer
    var navControl = viewer.getNavControl();
    navControl.removeChild(navControl.lastChild);

    viewer.openDzi("/tile/666415875/map.xml");
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
