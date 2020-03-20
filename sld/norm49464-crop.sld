<?xml version="1.0" ?>
<sld:StyledLayerDescriptor xmlns="http://www.opengis.net/sld" xmlns:gml="http://www.opengis.net/gml" xmlns:ogc="http://www.opengis.net/ogc" xmlns:sld="http://www.opengis.net/sld" version="1.0.0">
    <sld:NamedLayer>
        <sld:Name>pairs:pairspluginlayer</sld:Name>
        <sld:UserStyle>
            <sld:Name>pairs:pairspluginlayer</sld:Name>
            <sld:Title />
            <sld:FeatureTypeStyle>
                <sld:Name />
                <sld:Transformation>
                    <ogc:Function name="gs:CropCoverage">
                        <ogc:Function name="parameter">
                            <ogc:Literal>coverage</ogc:Literal>
                        </ogc:Function>
                        <ogc:Function name="parameter">
                            <ogc:Literal>cropShape</ogc:Literal>
                            <ogc:Function name="env">
                                <ogc:Literal>shape</ogc:Literal>
                                <ogc:Literal>POLYGON((
                                        -1.42898162  51.04441321,
                                        -1.42898162  51.05102867,
                                        -1.41101315  51.05102867,
                                        -1.41101315  51.04441321,
                                        -1.42898162  51.04441321    
                                    ))</ogc:Literal>
                            </ogc:Function>
                        </ogc:Function>
                    </ogc:Function>
                </sld:Transformation>
                <sld:Rule>
                    <sld:RasterSymbolizer>
                        <sld:Geometry>
                            <ogc:PropertyName>grid</ogc:PropertyName>
                        </sld:Geometry>
                        <sld:Opacity>1</sld:Opacity>

                        <sld:ColorMap>
                            <sld:ColorMapEntry color="#FFFFFF" label="NO_DATA" opacity="0.0" quantity="-9999" />
                            <sld:ColorMapEntry color="#FF0000" label="0.0" opacity="1.0" quantity="0.0" />
                            <sld:ColorMapEntry color="#FF5500" label="0.11111111" opacity="1.0" quantity="0.11111111" />
                            <sld:ColorMapEntry color="#FFAA00" label="0.22222222" opacity="1.0" quantity="0.22222222" />
                            <sld:ColorMapEntry color="#FFFF00" label="0.33333334" opacity="1.0" quantity="0.33333334" />
                            <sld:ColorMapEntry color="#AAFF55" label="0.44444445" opacity="1.0" quantity="0.44444445" />
                            <sld:ColorMapEntry color="#55FFAA" label="0.5555556" opacity="1.0" quantity="0.5555556" />
                            <sld:ColorMapEntry color="#00FFFF" label="0.6666667" opacity="1.0" quantity="0.6666667" />
                            <sld:ColorMapEntry color="#00AAFF" label="0.7777778" opacity="1.0" quantity="0.7777778" />
                            <sld:ColorMapEntry color="#0055FF" label="0.8888889" opacity="1.0" quantity="0.8888889" />
                            <sld:ColorMapEntry color="#0000FF" label="1.0" opacity="1.0" quantity="1.0" />
                        </sld:ColorMap>

                    </sld:RasterSymbolizer>
                </sld:Rule>
            </sld:FeatureTypeStyle>
        </sld:UserStyle>
    </sld:NamedLayer>
</sld:StyledLayerDescriptor>