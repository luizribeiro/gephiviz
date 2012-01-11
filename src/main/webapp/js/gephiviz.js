var viewer;

$(document).ready(function() {
    setupViewer();
    renderGraph();
});

function setupViewer() {
    viewer = new Seadragon.Viewer("viewport");

    // remove full page button from the seadragon viewer
    var navControl = viewer.getNavControl();
    navControl.removeChild(navControl.lastChild);
}

function renderGraph() {
    $('#viewport').hide();
    $('body').addClass('loading');

    $.ajax({
        type: 'GET',
        url: '/render',
        dataType: 'xml',
        success: function(data) {
            // render stuff
            $('body').removeClass('loading');
            $('#viewport').show();
            viewer.openDzi("/tile/666415875/map.xml");
        }
    });
}
