/*
 * Gephi Seadragon Plugin
 *
 * Copyright 2010-2011 Gephi
 * Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 * Website : http://www.gephi.org
 * Licensed under Apache 2 License (http://www.apache.org/licenses/LICENSE-2.0)
 */
package org.luizribeiro.gephiviz.exporter;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.imgscalr.Scalr;
import org.luizribeiro.gephiviz.RenderStorage;

/**
 *
 * @author Mathieu Bastian
 */
public class TileRenderer {

    private final String pathPrefix;
    private final RenderStorage renderStorage;
    private final int tileSize;
    private final String fileFormat = "png";
    private final int overlap;
    private boolean cancel = false;
    private ProgressTicket progressTicket;

    public TileRenderer(String pathPrefix, RenderStorage renderStorage, int tileSize, int tileOverlap) {
        this.tileSize = tileSize;
        this.overlap = tileOverlap;
        this.pathPrefix = pathPrefix;
        this.renderStorage = renderStorage;
    }

    public void writeLevel(BufferedImage image, float scale, int level) {
        int scaledWidth = (int) (scale * image.getWidth());
        int scaledHeight = (int) (scale * image.getHeight());
        scaledWidth = Math.max(scaledWidth, 1);
        scaledHeight = Math.max(scaledHeight, 1);
        BufferedImage scaledImage = Scalr.resize(image, Scalr.Method.QUALITY, scaledWidth, scaledHeight, Scalr.OP_ANTIALIAS);
        writeLevel(scaledImage, level);
    }

    public void writeLevel(BufferedImage image, int level) {
        try {
            int width = image.getWidth();
            int height = image.getHeight();
            int cols = (int) Math.ceil(width / tileSize);
            int rows = (int) Math.ceil(height / tileSize);
            for (int r = 0; r <= rows; r++) {
                for (int c = 0; c <= cols; c++) {
                    if (tileSize * c < width && tileSize * r < height) {
                        int left = c != 0 ? tileSize * c - overlap : 0;
                        int top = r != 0 ? tileSize * r - overlap : 0;
                        int tileW = (int) (tileSize + (c == 0 ? 1 : 2) * (double) overlap);
                        int tileH = (int) (tileSize + (r == 0 ? 1 : 2) * (double) overlap);
                        tileW = Math.min(tileW, width - left);
                        tileH = Math.min(tileH, height - top);

                        RenderedImage tiledImage = null;
                        BufferedImage bufferedImage = ((BufferedImage) image);
                        tiledImage = bufferedImage.getSubimage(left, top, tileW, tileH);

                        renderStorage.storeImage(tiledImage, pathPrefix + "/" + level + "/" + c + "_" + r + ".png");
                        if (cancel) {
                            return;
                        }
                        Progress.progress(progressTicket);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        cancel = true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }
}
