<%@ page import="org.luizribeiro.gephiviz.Settings" %>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="utf-8" />
        <title>GephiViz</title>
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.4.2/jquery.min.js"></script>
        <script type="text/javascript" src="/js/raphael-min.js"></script>
        <script type="text/javascript" src="/js/raphael-svg-import.js"></script>
        <script type="text/javascript" src="/js/raphael-zpd.js"></script>
        <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.5/jquery-ui.min.js"></script>
        <script type="text/javascript" src="http://cdn.jquerytools.org/1.2.5/all/jquery.tools.min.js"></script>
        <script type="text/javascript" src="/js/gephiviz.js"></script>
        <link rel="stylesheet" href="/style.css"/>
    </head>
    <body>
        <div id="fb-root"></div>
        <script>
          window.fbAsyncInit = function() {
            FB.init({
              appId      : '<%= Settings.getApiKey() %>',
              status     : true,
              cookie     : true,
              xfbml      : true
            });
          };
          (function(d){
             var js, id = 'facebook-jssdk'; if (d.getElementById(id)) {return;}
             js = d.createElement('script'); js.id = id; js.async = true;
             js.src = "//connect.facebook.net/en_US/all.js";
             d.getElementsByTagName('head')[0].appendChild(js);
          }(document));
        </script>
        <div class="fb-login-button" data-show-faces="true" data-width="200" data-max-rows="1"></div>
        <a href="http://github.com/luizribeiro/gephiviz" target="_blank" class="github-ribbon"><img src="https://a248.e.akamai.net/assets.github.com/img/e6bef7a091f5f3138b8cd40bc3e114258dd68ddf/687474703a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f7265645f6161303030302e706e67" alt="Fork me on GitHub"></a>
        <a href="http://gephi.org/" target="_blank" class="gephi-badge"><img src="/img/gephi-logo.png" alt="Powered by Gephi"></a>
    </body>
</html>