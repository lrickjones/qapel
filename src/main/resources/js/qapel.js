    function setCookie(cname, cvalue) {
      const d = new Date();
      d.setTime(d.getTime() + (3650*24*60*60*1000)); // set cookie to expire in 10 years
      let expires = "expires="+ d.toUTCString();
      document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
    }

    function getCookie(cname) {
      let name = cname + "=";
      let decodedCookie = decodeURIComponent(document.cookie);
      let ca = decodedCookie.split(';');
      for(let i = 0; i <ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
          c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
          return c.substring(name.length, c.length);
        }
      }
      return "";
    }

    function checkCookie(){
        var cookieEnabled = navigator.cookieEnabled;
        if (!cookieEnabled){
            document.cookie = "testcookie";
            cookieEnabled = document.cookie.indexOf("testcookie")!=-1;
        }
        return cookieEnabled || showCookieFail();
    }

    function showCookieFail(){
      alert("Enable cookies to store station information.")
      return false;
    }

    function play(path) {
        var audio = new Audio(path);
        audio.play();
    }

    function setStationCookie(id) {
        setCookie("station_id", id)
    }

    function getStation() {
        // try to get station from cookie
        let station_id = "0";
        if (checkCookie) {
            station_id = getCookie("station_id");
        }
        station_id = parseInt(station_id);
        // check for integer value greater than 0
        if (Number.isInteger(station_id) && (station_id > 0)) {
            // look up station name and update template
            $.get("/station/find?id=" + station_id, function (name) {
                document.getElementById("stationName").innerHTML = name;
            });
        } else {
            // if there is not station id redirect to page to select a station id
            window.location.replace("/station/select");
        }
    }

    function highlightActive() {
        $(function(){
            $('a').each(function(){
                if ($(this).prop('href') == window.location.href) {
                    $(this).addClass('active'); $(this).parents('li').addClass('active');
                }
            });
        });
    }

    function poll() {
        let stationId = getCookie("station_id");
        $.getJSON("/tag/poll?station_id="+stationId, function(data) {
            if (data) {
                let classStr = "h1 p-4 m-4 border border-success rounded "
                if (data.status.localeCompare("pass", undefined, { sensitivity: 'accent' }) === 0){
                    classStr += "bg-success";
                    play("/sound/pass.mp3");
                } else {
                    classStr += "bg-danger";
                    play("/sound/fail.mp3");
                }
                let div = "<div class='" + classStr + "'>" + data.epc + " -> " + data.status + "</div>";
                document.getElementById("read_tags").innerHTML = div;
            }
        });
        startPolling();
    }

    function startPolling() {
        setTimeout(poll, 1000);
    }