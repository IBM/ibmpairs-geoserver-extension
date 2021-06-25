package com.ibm.pa.pairs.geoserver.plugin.hbase;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.awt.Point;
import java.awt.image.*;

import javax.media.jai.DataBufferFloat;
import javax.media.jai.RasterFactory;

import org.geotools.coverage.grid.GridCoverage2D;

import net.opengis.gml311.RectifiedGridCoverageType;

/**
 * Convenience methods from building coverage and coverage components
 */
public class PairsCoverageFactory {
    private static final Logger logger = org.geotools.util.logging.Logging.getLogger(PairsCoverageFactory.class);

    /**
     * Gets raster data and build a multi-band coverage
     * 
     * todo: this could be concurrent operation to fetch the data
     * 
     * @param queryParams
     * @param coverageReader
     * @return
     * @throws Exception
     */
    public static GridCoverage2D buildGridCoverage2D(PairsWMSQueryParam queryParams, PairsCoverageReader coverageReader)
            throws Exception {
        GridCoverage2D gridCoverage2D = null;
        List<PairsRasterRequest> pairsRasterReqs = queryParams.generateRequestForEachLayer();
        int nbands = pairsRasterReqs.size();
        List<PairsQueryCoverageJob> pairsQueryCoverageJobs = new ArrayList<>(nbands);
        float[][] imageData = new float[nbands][];
        int width, height;

        // Retrieve the data from pairsdataservice
        for (PairsRasterRequest req : pairsRasterReqs) {
            PairsQueryCoverageJob pqcj = new PairsQueryCoverageJob(queryParams, req, coverageReader, false);
            pairsQueryCoverageJobs.add(pqcj);
            pqcj.call();
        }

        for (int i = 0; i < nbands; i++)
            imageData[i] = pairsQueryCoverageJobs.get(i).getImageDataFloat();

        // todo: add some checks in here, height, width should be same for all bands
        width = pairsQueryCoverageJobs.get(0).getResponseImageDescriptor().getWidth();
        height = pairsQueryCoverageJobs.get(0).getResponseImageDescriptor().getHeight();
        javax.media.jai.DataBufferFloat dataBuffer = new DataBufferFloat(imageData, width * height);
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_FLOAT, width, height, nbands);
        java.awt.image.WritableRaster writableRaster = RasterFactory.createWritableRaster(sampleModel, dataBuffer,
                new Point(0, 0));

        gridCoverage2D = coverageReader.getGridCoverageFactory().create(pairsQueryCoverageJobs.get(0).getCoverageName(),
                writableRaster, pairsQueryCoverageJobs.get(0).getResponseEnvelope2D());

        return gridCoverage2D;
    }

}
