package com.example.lambda_bbb_events.view.ui.components.map

import com.example.lambda_bbb_events.model.campsite.Campsite
import com.google.android.gms.maps.model.LatLng

/*
 * Builds the pages that show OpenStreetMap maps, drawn by Leaflet.
 *
 * Leaflet is bundled with the app, in assets/leaflet.
 *
 * Reference:
 * https://leafletjs.com/reference.html
 */

/**
 * The address the pages and Leaflet are loaded under. Nothing is loaded from it over the
 * internet: Android reserves it for files bundled with apps.
 */
internal const val OSM_PAGE_BASE_URL = "https://appassets.androidplatform.net/"

/**
 * Builds a map page that loads Leaflet and then runs [script].
 *
 * [script] must define a `function initMap()`. It can send events to the app with
 * `send(name, data)`, make a map with `createMap()`, and make marker icons with `pin`.
 *
 * @param script JavaScript that creates the map.
 */
internal fun osmPageHtml(script: String): String = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="initial-scale=1, width=device-width, user-scalable=no">
<link rel="stylesheet" href="${OSM_PAGE_BASE_URL}leaflet/leaflet.css">
<style>
  html, body { height: 100%; margin: 0; padding: 0; }
  /* Pinned to the page edges, so the map always fills the WebView */
  #map { position: absolute; top: 0; right: 0; bottom: 0; left: 0; }
  #error { display: none; position: absolute; top: 8px; left: 50px; right: 8px; z-index: 1000; pointer-events: none;
           font: 13px sans-serif; color: #555; background: #e9edf1; border-radius: 8px; text-align: center; padding: 8px; }
  .info { font: 14px sans-serif; max-width: 220px; }
  .info b { display: block; margin-bottom: 2px; }
  .info a { color: #2c5c7c; font-weight: 600; text-decoration: none; display: inline-block; margin-top: 6px; }
</style>
</head>
<body>
<div id="map"></div>
<div id="error">Map unavailable. Check your connection.</div>
<script src="${OSM_PAGE_BASE_URL}leaflet/leaflet.js"></script>
<script>
  function send(name, data) { Android.post(name, JSON.stringify(data || {})); }

  const pin = L.divIcon({
    className: '',
    html: '<svg width="30" height="40" viewBox="0 0 30 40"><path d="M15 1C7.3 1 1 7.2 1 14.8 1 25 15 39 15 39s14-14 14-24.2C29 7.2 22.7 1 15 1z" fill="#2c5c7c" stroke="#fff" stroke-width="2"/><circle cx="15" cy="15" r="5" fill="#fff"/></svg>',
    iconSize: [30, 40],
    iconAnchor: [15, 39],
    popupAnchor: [0, -36]
  });

  function createMap() {
    const map = L.map('map');
    map.attributionControl.setPrefix(false);
    const error = document.getElementById('error');
    let loaded = false;
    // OpenStreetMap requires its credit to stay visible
    L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
    }).on('tileload', function () {
      loaded = true;
      error.style.display = 'none';
    }).on('tileerror', function () {
      if (!loaded) error.style.display = 'block';
    }).addTo(map);
    return map;
  }

  $script

  // Leaflet needs to know the size of the map, which is 0 until the WebView is laid out
  function startWhenSized() {
    const element = document.getElementById('map');
    if (element.clientWidth > 0 && element.clientHeight > 0) {
      initMap();
    } else {
      requestAnimationFrame(startWhenSized);
    }
  }
  startWhenSized();
</script>
</body>
</html>
""".trimIndent()

/**
 * Builds the JavaScript for the OpenStreetMap version of [GoogleCampsiteMap]. Tapping a marker
 * shows the campsite's name and address, and tapping "View campsite" sends a `campsiteClick`
 * event with the campsite's ID.
 *
 * @param campsites The campsites to mark, all with a location.
 */
internal fun osmCampsiteMapScript(campsites: List<Campsite>): String = """
    const campsites = ${campsiteMarkers(campsites)};

    function initMap() {
      const map = createMap();
      map.setView([campsites[0].lat, campsites[0].lng], $SINGLE_CAMPSITE_ZOOM);
      const bounds = L.latLngBounds([]);

      campsites.forEach(function (campsite) {
        const position = [campsite.lat, campsite.lng];
        bounds.extend(position);

        const content = document.createElement('div');
        content.className = 'info';
        const name = document.createElement('b');
        name.textContent = campsite.name;
        const address = document.createElement('div');
        address.textContent = campsite.address;
        const link = document.createElement('a');
        link.href = '#';
        link.textContent = 'View campsite';
        link.onclick = function (event) {
          event.preventDefault();
          send('campsiteClick', { id: campsite.id });
        };
        content.append(name, address, link);
        L.marker(position, { icon: pin, title: campsite.name }).addTo(map).bindPopup(content);
      });

      if (campsites.length > 1) {
        map.fitBounds(bounds, { padding: [$BOUNDS_PADDING, $BOUNDS_PADDING], maxZoom: $MAX_FRAMING_ZOOM, animate: false });
      }
    }
""".trimIndent()

/**
 * Builds the JavaScript for the OpenStreetMap version of [GoogleClickableMap]. Tapping the map or
 * dragging the marker moves it and sends a `locationSelected` event with its coordinates.
 *
 * Sends a `mapReady` event once the app can call `showLocation(lat, lng)`, which moves the marker
 * and map there without sending a `locationSelected` event.
 *
 * @param initialLocation Where the marker and map start.
 */
internal fun osmClickableMapScript(initialLocation: LatLng): String = """
    const start = { lat: ${initialLocation.latitude}, lng: ${initialLocation.longitude} };

    function initMap() {
      const map = createMap();
      map.setView(start, 13);
      const marker = L.marker(start, { icon: pin, title: 'Selected Location', draggable: true }).addTo(map);

      function select(latLng) {
        // The map repeats sideways, so longitudes can be outside of -180 to 180
        const position = latLng.wrap();
        marker.setLatLng(latLng);
        send('locationSelected', { lat: position.lat, lng: position.lng });
      }

      map.on('click', function (event) { select(event.latlng); });
      marker.on('dragend', function () { select(marker.getLatLng()); });

      window.showLocation = function (lat, lng) {
        marker.setLatLng([lat, lng]);
        map.setView([lat, lng], 13);
      };
      send('mapReady');
    }
""".trimIndent()
