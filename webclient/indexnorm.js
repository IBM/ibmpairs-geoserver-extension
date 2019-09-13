import Map from 'ol/Map.js';
import View from 'ol/View.js';
import { Image as ImageLayer, Tile as TileLayer } from 'ol/layer.js';
import ImageWMS from 'ol/source/ImageWMS.js';
import OSM from 'ol/source/OSM.js';
import TileWMS from 'ol/source/TileWMS.js';
import {ZoomSlider} from 'ol/control.js';

/*
var overviewMapControl = new OverviewMap({
  // see in overviewmap-custom.html to see the custom CSS used
  className: 'ol-overviewmap ol-custom-overviewmap',
  layers: [
    new TileLayer({
      source: new OSM({
        'url': 'https://{a-c}.tile.thunderforest.com/cycle/{z}/{x}/{y}.png' +
            '?apikey=Your API key from http://www.thunderforest.com/docs/apikeys/ here'
      })
    })
  ],
  collapseLabel: '\u00BB',
  label: '\u00AB',
  collapsed: false
});
*/


var layers = [
  new TileLayer({
    source: new OSM(),
    type: 'base'
  }),
  new TileLayer({
    opacity:0.5,
    source: new TileWMS({
      url: 'http://pairs-alpha.res.ibm.com:8082/geoserver/pairs/wms',
      params: {
        'LAYERS': 'pairs:pairspluginlayer', 'TILED': true, 'VERSION': '1.3.0',
        // 	  'FORMAT': 'image/png', 'WIDTH': 512, 'HEIGHT': 512, 'CRS': 'EPSG:4326',
        'FORMAT': 'image/jpeg', 'WIDTH': 512, 'HEIGHT': 512, 'CRS': 'EPSG:4326',
        'ibmpairs_layerid': '49180', 'ibmpairs_timestamp': '1435708800', 'ibmpairs_statistic': 'Mean',
       // 'sld': 'https://pairs-alpha.res.ibm.com:8080/datapreview/colortabletest.sld'
      'sld': 'https://pairs.res.ibm.com/map/sld?type=raster&min=0.0001&max=1&colorTableId=31&no_data=0&property=value&layer=pairs:pairspluginlayer'
      },
      serverType: 'geoserver'
    })
  })
];


var map = new Map({
  layers: layers,
  target: 'map',
  // controls: [],

  view: new View({
    center: [-90, 30],
    projection: 'EPSG:4326',
    zoom: 2,
    minZoom: 2,
    maxZoom: 10
  })
});

// map.addControl(new ZoomSlider());
map.addControl(new ol.control.LayerSwitcher());
