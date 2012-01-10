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
import java.io.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.exporter.spi.Exporter;
import org.gephi.preview.api.*;
import org.gephi.project.api.Workspace;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.luizribeiro.gephiviz.RenderStorage;
import org.openide.util.Lookup;
import org.w3c.dom.Element;
import processing.core.PGraphicsJava2D;

/**
 *
 * @author Mathieu Bastian
 */
public class SeadragonExporter implements Exporter, LongTask {

    //Const
    private static final String XML_FILE = "map.xml";
    private static final String PATH_MAP = "map";
    private static final String PATH_FILES = "_files";
    //Architecture
    private Workspace workspace;
    private ProgressTicket progress;
    private boolean cancel = false;
    private PNGExporter pngExporter = new PNGExporter();
    private TileRenderer tileRenderer;
    private RenderStorage renderStorage;
    private String pathPrefix;
    //Settings
    private int width;
    private int height;
    private int margin;
    private int overlap = 1;
    private int tileSize = 256;

    @Override
    public boolean execute() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        Progress.start(progress);
        Progress.setDisplayName(progress, "Export Seadragon");

        PreviewController controller = Lookup.getDefault().lookup(PreviewController.class);
        controller.getModel(workspace).getProperties().putValue(PreviewProperty.VISIBILITY_RATIO, 1.0);
        controller.refreshPreview(workspace);

        PreviewProperties props = controller.getModel(workspace).getProperties();
        props.putValue("width", width);
        props.putValue("height", height);
        props.putValue(PreviewProperty.MARGIN, new Float((float) margin));
        ProcessingTarget target = (ProcessingTarget) controller.getRenderTarget(RenderTarget.PROCESSING_TARGET, workspace);

        target.refresh();

        Progress.switchToIndeterminate(progress);

        PGraphicsJava2D pg2 = (PGraphicsJava2D) target.getGraphics();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        img.setRGB(0, 0, width, height, pg2.pixels, 0, width);

        try {
            export(img);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        createXML();

        Progress.finish(progress);

        return !cancel;
    }

    public void export(BufferedImage img) throws Exception {
        int numLevels = (int) Math.ceil(Math.log(Math.max(img.getWidth(), img.getHeight())) / Math.log(2.));
        int w = img.getWidth();
        int h = img.getHeight();

        //Calculate tasks count
        int tasks = 0;
        for (int level = numLevels; level >= 0; level--) {
            float levelScale = 1f / (1 << (numLevels - level));
            tasks += (int) Math.ceil(levelScale * w / tileSize) * (int) Math.ceil(levelScale * h / tileSize);
        }

        Progress.switchToDeterminate(progress, tasks);

        //Tile renderer
        tileRenderer = new TileRenderer(pathPrefix + "/" + PATH_MAP + PATH_FILES, renderStorage, tileSize, overlap);
        tileRenderer.setProgressTicket(progress);
        for (int level = numLevels; level >= 0 && !cancel; level--) {
            float levelScale = 1f / (1 << (numLevels - level));
            tileRenderer.writeLevel(img, levelScale, level);
        }

        tileRenderer = null;
    }

    public void createXML() {
        org.w3c.dom.Document document = null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            document = documentBuilder.newDocument();
            document.setXmlVersion("1.0");
            document.setXmlStandalone(true);
        } catch (Exception ex) {
            throw new RuntimeException("Can't create XML file", ex);
        }

        Element imageElement = document.createElement("Image");
        imageElement.setAttribute("TileSize", String.valueOf(tileSize));
        imageElement.setAttribute("Overlap", String.valueOf(overlap));
        imageElement.setAttribute("Format", "png");
        imageElement.setAttribute("ServerFormat", "Default");
        imageElement.setAttribute("xmlns", "http://schemas.microsoft.com/deepzoom/2009");

        Element sizeElement = document.createElement("Size");
        sizeElement.setAttribute("Width", String.valueOf(width));
        sizeElement.setAttribute("Height", String.valueOf(height));
        imageElement.appendChild(sizeElement);
        document.appendChild(imageElement);

        try {
            Source source = new DOMSource(document);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Result result = new StreamResult(os);
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.transform(source, result);
            renderStorage.storeByteArray(os.toByteArray(), pathPrefix + "/" + XML_FILE, "text/xml");
        } catch (Exception ex) {
            throw new RuntimeException("Can't write XML file", ex);
        }
    }

    private void copyFromJar(String source, File folder) throws Exception {
        InputStream is = getClass().getResourceAsStream("/org/gephi/plugins/seadragon/resources/" + source);
        File file = new File(folder + (folder.getPath().endsWith(File.separator) ? "" : File.separator) + source);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdir();
        }
        OutputStream os = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        os.close();
        is.close();
    }

    public int getTileSize() {
        return tileSize;
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }

    @Override
    public void setWorkspace(Workspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public Workspace getWorkspace() {
        return workspace;
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        this.margin = margin;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getOverlap() {
        return overlap;
    }

    public void setOverlap(int overlap) {
        this.overlap = overlap;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public boolean cancel() {
        this.cancel = true;
        pngExporter.cancel();
        if (tileRenderer != null) {
            tileRenderer.cancel();
        }
        return true;
    }

    @Override
    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progress = progressTicket;
    }

    private void delete(File f) throws IOException {
        if (f.isDirectory()) {
            for (File c : f.listFiles()) {
                delete(c);
            }
        }
        if (f.exists() && !f.delete()) {
            throw new IOException("Failed to delete file: " + f);
        }
    }

    public void setPathPrefix(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }

    public RenderStorage getRenderStorage() {
        return renderStorage;
    }

    public void setRenderStorage(RenderStorage renderStorage) {
        this.renderStorage = renderStorage;
    }
}
