// import Map from 'ol/Map.js';
// import View from 'ol/View.js';
// import { Image as ImageLayer, Tile as TileLayer } from 'ol/layer.js';
// import ImageWMS from 'ol/source/ImageWMS.js';
// import OSM from 'ol/source/OSM.js';
// import TileWMS from 'ol/source/TileWMS.js';
// import { defaults as defaultControls, OverviewMap } from 'ol/control.js';
// import { ZoomSlider } from 'ol/control.js';
// src="https://cdn.rawgit.com/openlayers/openlayers.github.io/master/en/v5.3.0/build/ol.js;

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
      url: 'https://pairs-alpha.res.ibm.com:8080/geoserver02/pairs/wms',
      // url: 'http://localhost:8080/geoserver/pairs/wms',
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
    center: [-90, 30],
    projection: 'EPSG:4326',
    zoom: 3,
    minZoom: 2,
    maxZoom: 16
  });

  var map = new ol.Map({
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
 * @param {*} map 
 */
/**
 * @see https://openlayers.org/en/latest/examples/box-selection.html
 * @param {*} map 
 */
function addMapActions() {
  var zoomslider = new ol.control.ZoomSlider();
  map.addControl(zoomslider);

  map.on('click', function (evt) {
    alert('map.on' + evt.coordinate);
    console.log(evt.coordinate);
  });

  // a normal select interaction to handle click
  var select = new ol.interaction.Select();
  map.addInteraction(select);
  select.on('click', function (evt) {
    alert("map.on");
    alert(evt.coordinate);
    console.log(evt.coordinate);
  });

  // a DragBox interaction used to select features by drawing boxes
  var dragBox = new ol.interaction.DragBox({
    condition: ol.events.condition.shiftKeyOnly
  });
  map.addInteraction(dragBox);

  dragBox.on('boxend', function () {
    var extent = dragBox.getGeometry().getExtent();
    map.getView().fit(extent, map.getSize());
  });

  return { map: map, select: select, dragBox: dragBox };
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
  baseLayer.set("pairs-type", "baselayer OSM", true);
  map.addLayer(baseLayer);
});