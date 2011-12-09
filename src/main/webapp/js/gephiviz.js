var paper;
var zpd;

$(document).ready(function() {
    paper = Raphael(0, 0, document.width, document.height);
    zpd = RaphaelZPD(paper, { zoom: true, pan: true, drag: false });

    renderGraph();
});

function renderGraph() {
    $('body').addClass('loading');

    $.ajax({
        type: 'GET',
        url: '/render',
        dataType: 'xml',
        success: function(data) {
            paper.importSVG(data);
            $('body').removeClass('loading');
        }
    });
}
