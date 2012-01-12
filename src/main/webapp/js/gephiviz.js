var viewer;

$(document).ready(function() {
    $('#viewport').hide();
    $('body').addClass('loading');
});

function init() {
    setupViewer();
    renderGraph();
}

function setupViewer() {
    viewer = new Seadragon.Viewer("viewport");

    // remove full page button from the seadragon viewer
    var navControl = viewer.getNavControl();
    navControl.removeChild(navControl.lastChild);
}

function renderGraph() {
    $.ajax({
        type: 'GET',
        url: '/render',
        dataType: 'text',
        success: function(data) {
            if (data.indexOf("OK") != -1) {
                FB.api('/me', function(response) {
                    viewer.openDzi('/tile/' + response.id + '/map.xml');
                    $('body').removeClass('loading');
                    $('#viewport').show();
                });
            }
        }
    });
}
