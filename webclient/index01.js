     import Map from 'ol/Map.js';
      import View from 'ol/View.js';
      import {Image as ImageLayer, Tile as TileLayer} from 'ol/layer.js';
      import ImageWMS from 'ol/source/ImageWMS.js';
      import OSM from 'ol/source/OSM.js';
	import TileWMS from 'ol/source/TileWMS.js';


      var layers = [
        new TileLayer({
          source: new OSM(),
type: 'base'
        }),
        new TileLayer({
opacity:0.5,
          source: new TileWMS({
           url: 'http://pairs-alpha.res.ibm.com:8082/geoserver/pairs/wms',
          params: {'LAYERS': 'pairspluginlayer', 'TILED': true, 'VERSION': '1.3.0',
        	  'FORMAT': 'image/jpeg', 'WIDTH': 512, 'HEIGHT': 512, 'CRS': 'EPSG:4326'},
        serverType: 'geoserver'
          })
          })
      ];


      var map = new Map({
        layers: layers,
        target: 'map',
        controls: [],
        
        view: new View({
        //center: [-10997148, 4569099],
        center: [-90,30],
        projection: 'EPSG:4326',
        zoom: 4,
        minZoom:3,
        maxZoom:10
        })
      });


map.addControl(new ol.control.LayerSwitcher());
