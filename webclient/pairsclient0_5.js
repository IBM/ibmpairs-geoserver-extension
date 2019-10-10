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
/** parcel bundler issues 
* See https://github.com/parcel-bundler/parcel/issues/1618
*/

var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'June', 'July', 'Aug', 'Sept', 'Oct', 'Nov', 'Dec'];

var dataServiceTestUrl = 'https://pairs-alpha.res.ibm.com:8080/api/v1_10/dataquery';
var dataServiceProdUrl = 'https://pairs-alpha.res.ibm.com:8080/api/v1/dataquery';
var dataServiceUrl = dataServiceProdUrl;

// var geoserverUrlWmsTest = 'http://pairs-alpha.res.ibm.com:8082/geoserver/pairs/wms';
var geoserverWmsTestUrl = 'https://pairs-alpha.res.ibm.com:8080/geoserver02/pairs/wms';
var geoserverWmsProdUrl = 'http://pairs-alpha:8080/geoserver/pairs/wms';
var geoserverUrl = geoserverWmsTestUrl;

// initial location can be determined by user location, ...
var initialMapCenter = [-90, 30];

// Make this an array filled in dynamically by query based on user selection
var availableLayers = {
  "cropfraction_49180": { "layerId": 49180, "timestamp": 1435708800, "statistic": "mean", "maxValue": 1 },
  "cropland_49073": { "layerId": 49073, "timestamp": 1420070400, "statistic": "mean", "maxValue": 10 },
  "cropscape_30": { "layerId": 111, "timestamp": 1514764800, "statistic": "mean", "maxValue": 8 },
  "cropscape_250": { "layerId": 48522, "timestamp": 1514764800, "statistic": "mean", "maxValue": 8 },
  "modis_aqua_13_prs_51": { "layerId": 51, "timestamp": 1558051200, "statistic": "mean", "maxValue": 1 },
  "modis_aqua_13_prs_xy": { "layerId": 54, "timestamp": 1563580800, "statistic": "mean", "maxValue": 1 },
  "landsat8_L1_NIR": { "layerId": 49670, "timestamp": 1561766400, "statistic": "mean", "maxValue": 1 },
  "sentinel": { "layerId": 49359, "timestamp": 1561766400, "statistic": "mean", "maxValue": 1 },
  "land-cover-mrlc-50120": { "layerId": 50120, "timestamp": 1464782400, "statistic": "mean", "maxValue": 255 },
  "NAIP_Texas_49238": { "layerId": 49238, "timestamp": 1272110400, "statistic": "mean", "maxValue": 255 }
}
// for testing [may 17 2019, jan 25 2017]
var availableModisAqua13Timestamps = [1558051200, 1485302400];
var availableNAIPTimestamps = [1272110400, 1272974400];

var map = null;
var pairsLayer = null;
var availableTimestamps;

var currYear = 0;
var currMonth;

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
      url: geoserverUrl,
      params: {
        'LAYERS': 'pairs:pairspluginlayer', 'TILED': true, 'VERSION': '1.3.0',
        // 'FORMAT': 'image/png', 'WIDTH': 512, 'HEIGHT': 512, // 'CRS': 'EPSG:4326',
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
  layer.set("pairslayerId", layerId);
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
    center: ol.proj.fromLonLat(initialMapCenter, 'EPSG:4326'),
    // center: initialMapCenter,
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

  var zoomslider = new ol.control.ZoomSlider();
  map.addControl(zoomslider);
  zoomslider.setTarget(document.getElementById('zoom_div'));
  return map;
}

/**
 * @see https://openlayers.org/en/latest/examples/box-selection.html
 * 
 * Also look at createMap option 'interactions' for some control
 * @param {*} map 
 */
function addMapActions(map) {
  //  var zoomslider = new ol.control.ZoomSlider();
  //  map.addControl(zoomslider);
  //zoomslider.setTarget(document.getElementById('zoom_div'));
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
 * to the map....Interaction functions like map.(add)removeInteraction.
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

/**
 * TODO: 
 * - verify getTimestamps() -> api/v1/dataquery/timestamp/point hbase-data-service API works for overview layers
 * - remove special case for modis_aqua_13
 */
function updateMap(curTimestamp) {
  map.removeLayer(pairsLayer);

  var obj = document.getElementById("selectLayer");
  var layerName = obj.options[obj.selectedIndex].text;
  var pairsLayerInfo = availableLayers[layerName];

  if (curTimestamp < 0) {
    var center = map.getView().getCenter();
    // if (layerName == "modis_aqua_13_prs") {
    //   availableTimestamps = availableModisAqua13Timestamps;
    // } else {
    if (layerName == "NAIP_Texas_49238") {
      availableTimestamps = availableNAIPTimestamps;
    } else {
      availableTimestamps = getTimestamps(pairsLayerInfo["layerId"], center[0], center[1], "2010 01 01", "2019 07 01", 1000);
    }
    curTimestamp = availableTimestamps.length > 0 ? availableTimestamps[0] : pairsLayerInfo["timestamp"];
  }

  // Special timestamp case for new overview layer modis_aqua_13_prs
  // if (layerName == "modis_aqua_13_prs") {
  //   initialTimestamp = pairsLayerInfo["timestamp"];
  // }

  pairsLayer = buildPairsLayer(pairsLayerInfo["layerId"], curTimestamp, pairsLayerInfo["statistic"], pairsLayerInfo["maxValue"]);

  map.addLayer(pairsLayer);
  showLayers();

  updateTimestampDisplay();
}

document.addEventListener('DOMContentLoaded', function () {
  map = createMap("map", []);
  addMapActions(map);
  baseLayer.set("pairs-type", "baselayer mapbox", true);
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
function getTimestamps(layerid, lon, lat, start, end, limit) {
  var result = [];
  var boxSizeLon = 30;
  var boxSizeLat = 30;
  var swLon = lon - boxSizeLon / 2;
  var neLon = lon + boxSizeLon / 2;
  var swLat = lat - boxSizeLat / 2;
  var neLat = lat + boxSizeLat / 2;

  var paramsGlobal = { useoverview: true, lon: lon, lat: lat, starttime: start, endtime: end, limit: limit };
  var paramsTiled = { useoverview: true, swlon: swLon, swlat: swLat, nelon: neLon, nelat: neLat, starttime: start, endtime: end, limit: limit };
  var params;
  var uriAction;
  if (true || layerid == 49673 || layerid == 49238) {
    params = paramsTiled;
    uriAction = "/layer/" + layerid + "/timestamp/spatial";
  }
  else {
    params = paramsGlobal;
    uriAction = "/layer/" + layerid + "/timestamp/point";
  }

  var queryString = Object.keys(params).map(key => key + '=' + params[key]).join('&')
  var uri = dataServiceUrl + uriAction + "?" + queryString;
  var encodedUri = encodeURI(uri);

  var xhttp = new XMLHttpRequest();

  // xhttp.onreadystatechange = function () {
  //   if (this.readyState == 4) {
  //     if (this.status == 200) {
  //       result = this.responseText;
  //       console.log(result);
  //       document.getElementById("debugDiv").innerHTML =
  //         this.responseText;
  //       result = JSON.parse(result);
  //       result = result["timestamps"];
  //     }
  //     else {
  //       console.log("error");
  //     }
  //   }
  // };

  // xhttp.addEventListener('error', function () {
  //   console.log(this.statusText);
  // });

  // xhttp.onload = function () {
  //   console.log('onload', this.readyState, 'val' + this.responseText);
  // }

  xhttp.open("GET", encodedUri, false);
  xhttp.send();

  if (xhttp.status == 200) {
    result = xhttp.responseText;
    console.log(result);
    document.getElementById("debugDiv").innerHTML =
      xhttp.responseText;
    result = JSON.parse(result);
    result = result["timestamps"];
  }

  return result;
}

function updateTimestampDisplay() {
  buildYearSelector();
  buildMonthSelector(0);
  buildDaySelector(0, 0);
}

function buildYearSelector() {
  var table = document.getElementById("yearTable");
  if (table.rows && table.rows.length > 0)
    table.deleteRow(0);
  row = table.insertRow();
  // else row = table.rows[0];
  var curr = 0;
  //  for(var i=0;i<availableTimestamps.length;i++){
  //	if(curr != new Date(availableTimestamps[i] * 1000).getYear()){
  //		curr = new Date(availableTimestamps[i] * 1000).getYear();
  //		console.log(curr);
  //cell.innerHTML = "<button>" + curr+"</button>"
  //	}
  //}

  for (var i = 0; i < 10; i++) {
    var cell = row.insertCell(0);
    //cell.innerHTML = "<span class='green'>" + (2019 - i) + "</span>";
    cell.innerHTML = "<input type=\"button\" id=\"btn_year_" + (2019 - i) + "\" value=\"" + (2019 - i) + "\" disabled onClick=\"clickYear('" + (2019 - i) + "')\"/>"
    //console.log(availableTimestamps.length);
    // cell.innerText = 2000 + i;
  }

  for (var i = 0; i < availableTimestamps.length; i++) {
    if (curr != new Date(availableTimestamps[i] * 1000).getUTCFullYear()) {
      curr = new Date(availableTimestamps[i] * 1000).getUTCFullYear();
      //console.log(curr);
      //document.getElementById("btn_year_" + curr).disabled = false;

      var btn = document.getElementById("btn_year_" + curr);
      if (btn != null)
        btn.disabled = false;
      //cell.innerHTML = "<button>" + curr+"</button>"
    }
  }


}


function clickYear(year) {
  var btn = document.getElementById("btn_year_" + currYear);
  if (btn != null) {
    btn.style.background = '#FFFFFF';
    console.log(btn);
  }
  currYear = year;
  btn = document.getElementById("btn_year_" + year);
  btn.style.background = '#00FF00';
  buildMonthSelector(year);
}


function buildMonthSelector(year) {
  var table = document.getElementById("monthTable");
  if (table.rows && table.rows.length > 0)
    table.deleteRow(0);
  row = table.insertRow();
  for (var i = 0; i < 12; i++) {
    var cell = row.insertCell(0);
    cell.innerHTML = "<input type=\"button\" id=\"btn_month_" + (11 - i) + "\" value=\"" + months[11 - i] + "\" disabled onClick=\"clickMonth('" + (11 - i) + "')\"/>"
  }

  //console.log("The year" + year);

  if (year > 0) {

    for (var i = 0; i < availableTimestamps.length; i++) {
      if (year == new Date(availableTimestamps[i] * 1000).getUTCFullYear()) {
        curr = new Date(availableTimestamps[i] * 1000).getUTCMonth();
        var btn = document.getElementById("btn_month_" + curr);
        btn.disabled = false;
      }
    }
  }
  buildDaySelector(0, 0);
}

function clickMonth(month) {
  var btn = document.getElementById("btn_month_" + currMonth);
  if (btn != null) {
    btn.style.background = '#FFFFFF';
    console.log(btn);
  }
  currMonth = month;
  btn = document.getElementById("btn_month_" + month);
  btn.style.background = '#00FF00';
  buildDaySelector(currYear, month);
}



function buildDaySelector(year, month) {
  var table = document.getElementById("dayTable");
  if (table.rows && table.rows.length > 0) {
    table.deleteRow(0);
    table.deleteRow(0);
  }

  row = table.insertRow();
  for (var i = 15; i > 0; i--) {
    var cell = row.insertCell(0);
    cell.innerHTML = "<input type=\"button\" id=\"btn_day_" + (i) + "\" value=\"" + (i) + "\" disabled onClick=\"clickDay('" + (i) + "')\"/>"
  }


  row = table.insertRow();
  for (var i = 31; i > 15; i--) {
    var cell = row.insertCell(0);
    cell.innerHTML = "<input type=\"button\" id=\"btn_day_" + (i) + "\" value=\"" + (i) + "\" disabled onClick=\"clickDay('" + (i) + "')\"/>"
  }

  //  console.log("The year" + year);
  //console.log("The month" + month);

  if (year > 0) {

    for (var i = 0; i < availableTimestamps.length; i++) {
      //  console.log("The year2:" + new Date(availableTimestamps[i] * 1000).getUTCFullYear());
      //console.log("The month2:" + new Date(availableTimestamps[i] * 1000).getUTCMonth());
      if (year == new Date(availableTimestamps[i] * 1000).getUTCFullYear() && (month) == new Date(availableTimestamps[i] * 1000).getUTCMonth()) {
        curr = new Date(availableTimestamps[i] * 1000).getUTCDate();
        var btn = document.getElementById("btn_day_" + curr);
        //console.log(btn);
        //console.log(availableTimestamps[i]);
        //console.log(new Date(availableTimestamps[i] * 1000).getUTCDate());
        btn.disabled = false;
        btn.setAttribute("timestamp", availableTimestamps[i]);
      }
    }
  }
}

function clickDay(day) {
  var btn = document.getElementById("btn_day_" + day);
  ts = btn.getAttribute("timestamp");
  console.log("selected time: " + ts);
  var infoDiv = document.getElementById("infoDiv");
  infoDiv.innerText = "selected time: " + ts;
  updateMap(ts);
}
