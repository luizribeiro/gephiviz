var viewer;
var uid;

$(document).ready(function() {
    $('#viewport').hide();
    $('#content').hide();
    $('body').addClass('loading');

    setupFacebook();
    setupViewer();
});

window.fbAsyncInit = function() {
    FB.init({
        appId  : GEPHIVIZ_APP_ID,
        status : true,
        cookie : true,
        xfbml  : true
    });

    FB.Event.subscribe('auth.authResponseChange', checkLoginStatus);
    FB.getLoginStatus(checkLoginStatus);
};

function checkLoginStatus(response) {
    if (response.status == 'connected') {
        if ($.cookie('fbsr_' + GEPHIVIZ_APP_ID) == null) {
            window.location.reload();
        } else {
            uid = response.authResponse.userID;
            renderGraph();
        }
    } else if (response.status == 'not_authorized') {
        $('#content').load('/welcome.jsp', showContent);
    } else {
    }
}

function setupViewer() {
    viewer = new Seadragon.Viewer("viewport");

    // remove full page button from the seadragon viewer
    var navControl = viewer.getNavControl();
    navControl.removeChild(navControl.lastChild);
}

function setupFacebook() {
    $('body').append('<div id="fb-root"></div>');
    $.getScript(document.location.protocol + '//connect.facebook.net/en_US/all.js');
}

function renderGraph() {
    $.ajax({
        type: 'GET',
        url: '/render',
        dataType: 'text',
        success: function(data) {
            if (data.indexOf("OK") != -1) {
                viewer.openDzi('/tile/' + uid + '/map.xml');
                $('body').removeClass('loading');
                $('#viewport').show();
            }
        }
    });
}

$(window).resize(function() {
    $("#content").css('top', ($(window).height() - $("#content").height()) / 2);
    $("#content").css('left', ($(window).width() - $("#content").width()) / 2);
});

function showContent() {
    $(window).resize();
    $('#content').fadeIn(function() {
        $('body').removeClass('loading');
    });
}
