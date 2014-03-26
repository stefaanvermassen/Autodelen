/**
 * Created by Cedric on 3/23/2014.
 */
$(document).ready(function() {
    var element = $('#map');
    if(typeof(element) != 'undefined') {
        var lon = element.data('lon');
        var lat = element.data('lat');
        var message = element.data('message');
        var zoom = element.data('zoom') || 13;
        // create a map in the "map" div, set the view to a given place and zoom
        var map = L.map('map').setView([lat, lon], zoom);

        // add an OpenStreetMap tile layer
        L.tileLayer('/maps/tile?zoom={z}&x={x}&y={y}', {
            attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> vrijwilligers'
        }).addTo(map);

        // add a marker in the given location, attach some popup content to it and open the popup
        if(typeof(message) != 'undefined') {
            L.marker([lat, lon]).addTo(map)
                .bindPopup(message)
                .openPopup();
        } else {
            L.marker([lat, lon]).addTo(map);
        }
    }
});