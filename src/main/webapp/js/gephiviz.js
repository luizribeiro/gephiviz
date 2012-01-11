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
        dataType: 'text',
        success: function(data) {
            // render stuff
            viewer.openDzi('/tile/666415875/map.xml');
            $('body').removeClass('loading');
            $('#viewport').show();
        }
    });
}
