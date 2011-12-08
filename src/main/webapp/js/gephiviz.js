$(document).ready(function() {
    $('#pan img').hide();

    $('#pan img').load(function() {
        $('#pan').removeClass('loading');
        $(this).fadeIn('slow');
        $(this).panZoom('fit');
    });

    $('#pan').addClass('loading');

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
