$(document).ready(function() {
    $('#pan img').hide();

    $('#pan img').load(function() {
        $('#pan #loading').hide();
        $(this).fadeIn();
        $('#pan img').panZoom('fit');
    });

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
});
