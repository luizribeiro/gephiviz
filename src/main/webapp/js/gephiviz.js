$(document).ready(function() {
    $('#pan img').panZoom({
        'zoomIn'    :  $('#zoomin'),
        'zoomOut'   :  $('#zoomout'),
        'panUp'     :  $('#panup'),
        'panDown'   :  $('#pandown'),
        'panLeft'   :  $('#panleft'),
        'panRight'  :  $('#panright'),
        'fit'       :  $('#fit'),
        'debug'     :  false
    });
    $('#pan img').fit();
});
