// import libTest from './mylib.js';
// import Map from 'ol/Map.js';
// import View from 'ol/View.js';
// import { Image as ImageLayer, Tile as TileLayer } from 'ol/layer.js';
// import ImageWMS from 'ol/source/ImageWMS.js';
// import OSM from 'ol/source/OSM.js';
// import TileWMS from 'ol/source/TileWMS.js';
// import { defaults as defaultControls, OverviewMap } from 'ol/control.js';
// import { ZoomSlider } from 'ol/control.js';
// src="https://cdn.rawgit.com/openlayers/openlayers.github.io/master/en/v5.3.0/build/ol.js;

var urlGeoserverWmsTest = 'https://pairs-alpha.res.ibm.com:8080/geoserver02/pairs/wms';
var urlGeoserverWmsProd =  'http://localhost:8080/geoserver/pairs/wms';
var urlTimeservice = 'http://pairs-web04.pok.ibm.com:9082/api/v2/data/timestamp';

var map = null;
var pairsLayer = null;

function showLayers() {
  if (map !== null) {
    var debugDiv = document.getElementById("debugDiv");
    debugDiv.innerHTML = "<p>Map layers</p>";

    var layerColl = map.getLayers();
    layerArray = layerColl.getArray();
    for (i = 0; i < layerArray.length; i++) {
      debugDiv.innerText += "\nlayer: " + layerArray[i].get("pairs-type");
    }
  }
}

// Make this an array
var availableLayers = {
  "cropfraction_49180": { "layerId": 49180, "timestamp": 1435708800, "statistic": "mean", "maxValue": 1 },
  "cropland_49073": { "layerId": 49073, "timestamp": 1420070400, "statistic": "mean", "maxValue": 2 },
  "cropscape_111": { "layerId": 111, "timestamp": 1514764800, "statistic": "mean", "maxValue": 2 }
}

var baseLayerOSM = new ol.layer.Tile({
  source: new ol.source.OSM(),
});

var baseLayer = new ol.layer.Tile({
  source: new ol.source.XYZ({
    url: 'https://api.mapbox.com/styles/v1/mapbox/satellite-v9/tiles/256/{z}/{x}/{y}?access_token=pk.eyJ1Ijoid2VhdGhlciIsImEiOiJjam0wcnZmYWwwZWttM3Fwbmt3eGpybXd5In0.4FqjlG-2FckYw_mo_10Czw'
  })
});

/* Note we can set our own source function to make the WMS request
* This allows us to retrieve any custom headers from pairs geoserver extension
* https://openlayers.org/en/latest/apidoc/module-ol_Tile.html  
*/
function buildPairsLayer(layerId, timestamp, statistic, maxValue) {
  var layer = new ol.layer.Tile({
    title: "Pairs Geoscope",
    opacity: 1,
    source: new ol.source.TileWMS({
      url: urlGeoserverWmsTest,
      params: {
        'LAYERS': 'pairs:pairspluginlayer', 'TILED': true, 'VERSION': '1.3.0',
        // 	  'FORMAT': 'image/png', 'WIDTH': 512, 'HEIGHT': 512, 'CRS': 'EPSG:4326',
        'FORMAT': 'image/png', 'WIDTH': 256, 'HEIGHT': 256, 'CRS': 'EPSG:4326',
        'ibmpairs_layerid': layerId, 'ibmpairs_timestamp': timestamp, 'ibmpairs_statistic': statistic,
        // 'sld': 'https://pairs-alpha.res.ibm.com:8080/datapreview/colortabletest.sld'
        'sld': 'https://pairs.res.ibm.com/map/sld?type=raster&min=0.0001&max=' + maxValue + '&colorTableId=31&no_data=0&property=value&layer=pairs:pairspluginlayer'
      },
      serverType: 'geoserver'
    })
  });

  layer.getSource();
  layer.set("pairs-type", "pairs overview id: " + layerId, true);
  return layer;
}


/* AJAX Code for custom call to retrieve tile from pairs geoserver extension
Haven't tested, have to see how the source url, params are passed into src in function call
* https://openlayers.org/en/latest/apidoc/module-ol_Tile.html
*/
/*
source.setTileLoadFunction(function (tile, src) {
  var xhr = new XMLHttpRequest();
  xhr.responseType = 'blob';
  xhr.addEventListener('loadend', function (evt) {
    var data = this.response;
    if (data !== undefined) {
      tile.getImage().src = URL.createObjectURL(data);
    } else {
      tile.setState(TileState.ERROR);
    }
  });
  xhr.addEventListener('error', function () {
    tile.setState(TileState.ERROR);
  });
  xhr.open('GET', src);
  xhr.send();
});
*/


function createMap(targetDiv, layers) {
  var view = new ol.View({
    center: ol.proj.fromLonLat([-90, 30], 'EPSG:4326'),
    // center: [-90, 30],
    projection: 'EPSG:4326',
    zoom: 3,
    minZoom: 2,
    maxZoom: 16
  });

  /**
   * TODO: Not yet convinced interactions options set here work consistently, source code seems OK
   * but have to trace into it. For example, shiftDragZoom true doesn't seem to enable shiftDragZoom.
   * but setting interactions option null it is enabled as default
   * @see addMapActions()
   */
  var map = new ol.Map({
    interactions: ol.interaction.defaults({
      doubleClickZoom: false, shiftDragZoom: true, select: false
    }),
    loadTilesWhileAnimating: true,
    layers: layers,
    target: targetDiv,
    view: view,
    // controls: [],
  });
  // var overviewMapController = defaultControls().extend([
  //   overviewMapControl
  // ]);
  // map.addControl(overviewMapController);

  return map;
}

/**
 * @see https://openlayers.org/en/latest/examples/box-selection.html
 * 
 * Also look at createMap option 'interactions' for some control
 * @param {*} map 
 */
function addMapActions(map) {
  var zoomslider = new ol.control.ZoomSlider();
  map.addControl(zoomslider);

  map.on('dblclick', function (evt) {
    alert('map.on' + evt.coordinate);
    console.log(evt.coordinate);
    // alert('map.on' + map.getEventCoordinate(evt).XYZ);
  });

  map.removeInteraction(ol.interaction.Select);
  var select = new ol.interaction.Select();
  select.on('click', function (evt) {
    alert("select.on");
    alert(evt.coordinate);
    console.log(evt.coordinate);
  });
  map.addInteraction(select);

  /** 
   * Don't want to zoom, just show data availability and leave box selection outline on display
   * Not working or coded correctly yet
*/
  // map.removeInteraction(ol.interaction.DragZoom);
  var dragBox = new ol.interaction.DragZoom({
    condition: ol.events.condition.shiftKeyOnly,
    constrainResolution: true, onFocusOnly: true
  });
  dragBox.handleEvent(function () {
    var extent = dragBox.getGeometry().getExtent();
    alert("dragZoom" + extent);
    // map.getView().fit(extent, map.getSize());
  });
  map.addInteraction(dragBox);

  // map.addInteraction(ol.interaction.defaults({
  //   constrainResolution: true, onFocusOnly: true
  // }))
  return { map: map, select: select, dragBox: dragBox };
}

/**
 * Example function if need to find interaction, 
 * for example to temporary disable by finding and invoking setActive(false)
 * However, the interaction class can be used as argument
 * to the map....Interaction functions ike map.(add)removeInteraction.
 * Note: has to be modified to detect multiple instances in map.interaction Collection, chk source code see if even possible
 * @param {} map 
 * @returns interaction
 */
function findMapInteractions(map, intClass) {
  var result = null;
  map.getInteractions().getArray().forEach(function (mapint) {
    if (mapInt instanceof intClass) {
      result = interaction;
    }
  });
  return result;
}

// See https://github.com/parcel-bundler/parcel/issues/1618
function updateMap() {
  map.removeLayer(pairsLayer);

  var obj = document.getElementById("selectLayer");
  var layerName = obj.options[obj.selectedIndex].text;
  var pairsLayerInfo = availableLayers[layerName];
  pairsLayer = buildPairsLayer(pairsLayerInfo["layerId"], pairsLayerInfo["timestamp"], pairsLayerInfo["statistic"], pairsLayerInfo["maxValue"]);

  map.addLayer(pairsLayer);
  showLayers();
}

document.addEventListener('DOMContentLoaded', function () {
  map = createMap("map", []);
  addMapActions(map);
  baseLayer.set("pairs-type", "baselayer OSM", true);
  map.addLayer(baseLayer);
});


// Time related functions

/**
 * Invoked by map pan change event, get the extent for the
 * current display and return available timestamps
 * 
 * @param {*} lon 
 * @param {*} lat 
 * @param {*} start 
 * @param {*} end 
 * @param {*} limit 
 */
function getTimestamps(lon, lat, start, end, limit) {
  var result;
  var xhttp = new XMLHttpRequest();
  
  xhttp.responseType = 'json';
  var params = { lon: lon, lat: lat, starttime: start, endtime: end, limit: limit };
  var queryString = Object.keys(params).map(key => key + '=' + params[key]).join('&')
  
  xhttp.onreadystatechange = function () {
    if (this.readyState == 4 && this.status == 200)
      result = this.responseText;
      result = JSON.parse(result);
  };
  xhttp.addEventListener('error', function () {
    console.log(this.statusText);
  });
  xhttp.open("GET", urlTimeservice + "?" + queryString, true);
    xhttp.send(); 

  return result;
}

function buildYearSelector(timestamps) {
  var table = document.getElementById("yearTable");
  row = table.insertRow(0);
  for ( var i = 0; i<16; i++) {
    var cell = row.insertCell();
    cell.setInnerText("2000" + i);
  }
}