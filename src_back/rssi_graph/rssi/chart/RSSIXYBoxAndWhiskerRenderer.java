/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rssi_graph.rssi.chart;

import java.awt.Graphics2D;
import java.io.Serializable;
import org.jfree.chart.renderer.xy.XYBoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.util.PublicCloneable;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.BoxAndWhiskerXYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.Outlier;
import org.jfree.chart.renderer.OutlierList;
import org.jfree.chart.renderer.OutlierListCollection;
import org.jfree.chart.renderer.xy.AbstractXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRendererState;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.BoxAndWhiskerXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.io.SerialUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PaintUtilities;
import org.jfree.util.PublicCloneable;

/**
 * Custom renderer for further customizations
 * @author ph4r05
 */
public class RSSIXYBoxAndWhiskerRenderer extends AbstractXYItemRenderer
        implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {

    /** For serialization. */
    private static final long serialVersionUID = -8020170108532232324L;

    /** The box width. */
    private double boxWidth;

    /** The paint used to fill the box. */
    private transient Paint boxPaint;

    /** A flag that controls whether or not the box is filled. */
    private boolean fillBox;

    /**
     * The paint used to draw various artifacts such as outliers, farout
     * symbol, average ellipse and median line.
     */
    private transient Paint artifactPaint = Color.black;

     /**
     * =================================================================
     * PH4r05 edit START
     * =================================================================
     */
    /**
     * A flag that controls whether or not the quartiles are drawn.
     *
     */
    private boolean quartilsVisible;

    /**
     * A flag that controls whether or not the min,max are drawn.
     *
     */
    private boolean minMaxVisible;

    /**
     * A flag that controls whether or not the outliers are drawn.
     */
    private boolean outliersVisible;
    /**
     * =================================================================
     * PH4r05 edit END
     * =================================================================
     */

    /**
     * Creates a new renderer for box and whisker charts.
     */
    public RSSIXYBoxAndWhiskerRenderer() {
        this(-1.0);
    }

    /**
     * Creates a new renderer for box and whisker charts.
     * <P>
     * Use -1 for the box width if you prefer the width to be calculated
     * automatically.
     *
     * @param boxWidth  the box width.
     */
    public RSSIXYBoxAndWhiskerRenderer(double boxWidth) {
        super();
        this.boxWidth = boxWidth;
        this.boxPaint = Color.green;
        this.fillBox = true;
        this.minMaxVisible = true;
        this.quartilsVisible = true;
        this.outliersVisible = true;
        setBaseToolTipGenerator(new BoxAndWhiskerXYToolTipGenerator());
    }

    /**
     * Returns the width of each box.
     *
     * @return The box width.
     *
     * @see #setBoxWidth(double)
     */
    public double getBoxWidth() {
        return this.boxWidth;
    }

    /**
     * Sets the box width and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     * <P>
     * If you set the width to a negative value, the renderer will calculate
     * the box width automatically based on the space available on the chart.
     *
     * @param width  the width.
     *
     * @see #getBoxWidth()
     */
    public void setBoxWidth(double width) {
        if (width != this.boxWidth) {
            this.boxWidth = width;
            fireChangeEvent();
        }
    }

    /**
     * Returns the paint used to fill boxes.
     *
     * @return The paint (possibly <code>null</code>).
     *
     * @see #setBoxPaint(Paint)
     */
    public Paint getBoxPaint() {
        return this.boxPaint;
    }

    /**
     * Sets the paint used to fill boxes and sends a {@link RendererChangeEvent}
     * to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> permitted).
     *
     * @see #getBoxPaint()
     */
    public void setBoxPaint(Paint paint) {
        this.boxPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not the box is filled.
     *
     * @return A boolean.
     *
     * @see #setFillBox(boolean)
     */
    public boolean getFillBox() {
        return this.fillBox;
    }

    /**
     * Sets the flag that controls whether or not the box is filled and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param flag  the flag.
     *
     * @see #setFillBox(boolean)
     */
    public void setFillBox(boolean flag) {
        this.fillBox = flag;
        fireChangeEvent();
    }

    /**
     * Returns the paint used to paint the various artifacts such as outliers,
     * farout symbol, median line and the averages ellipse.
     *
     * @return The paint (never <code>null</code>).
     *
     * @see #setArtifactPaint(Paint)
     */
    public Paint getArtifactPaint() {
        return this.artifactPaint;
    }

    /**
     * Sets the paint used to paint the various artifacts such as outliers,
     * farout symbol, median line and the averages ellipse, and sends a
     * {@link RendererChangeEvent} to all registered listeners.
     *
     * @param paint  the paint (<code>null</code> not permitted).
     *
     * @see #getArtifactPaint()
     */
    public void setArtifactPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("Null 'paint' argument.");
        }
        this.artifactPaint = paint;
        fireChangeEvent();
    }

    /**
     * Returns the range of values the renderer requires to display all the
     * items from the specified dataset.
     *
     * @param dataset  the dataset (<code>null</code> permitted).
     *
     * @return The range (<code>null</code> if the dataset is <code>null</code>
     *         or empty).
     *
     * @see #findDomainBounds(XYDataset)
     */
    public Range findRangeBounds(XYDataset dataset) {
        return findRangeBounds(dataset, true);
    }

    /**
     * =================================================================
     * PH4r05 edit START
     * =================================================================
     */

    /**
     * Returns the flag that controls whether or not minimums and maximums are
     * drawn for each item.
     *
     * @return A boolean.
     *
     * @since 1.0.13 rev ph4r05
     */
    public boolean isMinMaxVisible() {
        return minMaxVisible;
    }

    /**
     * Set the flag that controls whether or not minimums and maximums are
     * drawn for each item, and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @return A boolean.
     *
     * @since 1.0.13 rev ph4r05
     */
    public void setMinMaxVisible(boolean minMaxVisible) {
        if (this.minMaxVisible == minMaxVisible) {
            return;
        }
        this.minMaxVisible = minMaxVisible;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not outliers are
     * drawn for each item.
     *
     * @return A boolean.
     *
     * @since 1.0.13 rev ph4r05
     */
    public boolean isOutliersVisible() {
        return outliersVisible;
    }

    /**
     * Set the flag that controls whether or not outliers are
     * drawn for each item, and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @return A boolean.
     *
     * @since 1.0.13 rev ph4r05
     */
    public void setOutliersVisible(boolean outliersVisible) {
        if (this.outliersVisible == outliersVisible) {
            return;
        }
        this.outliersVisible = outliersVisible;
        fireChangeEvent();
    }

    /**
     * Returns the flag that controls whether or not quartiles are
     * drawn for each item.
     *
     * @return A boolean.
     *
     * @since 1.0.13 rev ph4r05
     */
    public boolean isQuartilsVisible() {
        return quartilsVisible;
    }

    /**
     * Sets the flag that controls whether or not quartiles are
     * drawn for each item, and sends a {@link RendererChangeEvent} to all
     * registered listeners.
     *
     * @return A boolean.
     *
     * @since 1.0.13 rev ph4r05
     */
    public void setQuartilsVisible(boolean quartilsVisible) {
        if (this.quartilsVisible == quartilsVisible) {
            return;
        }
        this.quartilsVisible = quartilsVisible;
        fireChangeEvent();
    }
    /**
     * =================================================================
     * PH4r05 edit END
     * =================================================================
     */

    /**
     * Returns the box paint or, if this is <code>null</code>, the item
     * paint.
     *
     * @param series  the series index.
     * @param item  the item index.
     *
     * @return The paint used to fill the box for the specified item (never
     *         <code>null</code>).
     *
     * @since 1.0.10
     */
    protected Paint lookupBoxPaint(int series, int item) {
        Paint p = getBoxPaint();
        if (p != null) {
            return p;
        }
        else {
            // TODO: could change this to itemFillPaint().  For backwards
            // compatibility, it might require a useFillPaint flag.
            return getItemPaint(series, item);
        }
    }

    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2  the graphics device.
     * @param state  the renderer state.
     * @param dataArea  the area within which the plot is being drawn.
     * @param info  collects info about the drawing.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset (must be an instance of
     *                 {@link BoxAndWhiskerXYDataset}).
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the pass index.
     */
    public void drawItem(Graphics2D g2,
                         XYItemRendererState state,
                         Rectangle2D dataArea,
                         PlotRenderingInfo info,
                         XYPlot plot,
                         ValueAxis domainAxis,
                         ValueAxis rangeAxis,
                         XYDataset dataset,
                         int series,
                         int item,
                         CrosshairState crosshairState,
                         int pass) {

        PlotOrientation orientation = plot.getOrientation();

        if (orientation == PlotOrientation.HORIZONTAL) {
            drawHorizontalItem(g2, dataArea, info, plot, domainAxis, rangeAxis,
                    dataset, series, item, crosshairState, pass);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            drawVerticalItem(g2, dataArea, info, plot, domainAxis, rangeAxis,
                    dataset, series, item, crosshairState, pass);
        }

    }

    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area within which the plot is being drawn.
     * @param info  collects info about the drawing.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset (must be an instance of
     *                 {@link BoxAndWhiskerXYDataset}).
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the pass index.
     */
    public void drawHorizontalItem(Graphics2D g2,
                                   Rectangle2D dataArea,
                                   PlotRenderingInfo info,
                                   XYPlot plot,
                                   ValueAxis domainAxis,
                                   ValueAxis rangeAxis,
                                   XYDataset dataset,
                                   int series,
                                   int item,
                                   CrosshairState crosshairState,
                                   int pass) {

        // setup for collecting optional entity info...
        EntityCollection entities = null;
        if (info != null) {
            entities = info.getOwner().getEntityCollection();
        }

        BoxAndWhiskerXYDataset boxAndWhiskerData
                = (BoxAndWhiskerXYDataset) dataset;

        Number x = boxAndWhiskerData.getX(series, item);
        Number yMax = boxAndWhiskerData.getMaxRegularValue(series, item);
        Number yMin = boxAndWhiskerData.getMinRegularValue(series, item);
        Number yMedian = boxAndWhiskerData.getMedianValue(series, item);
        Number yAverage = boxAndWhiskerData.getMeanValue(series, item);
        Number yQ1Median = boxAndWhiskerData.getQ1Value(series, item);
        Number yQ3Median = boxAndWhiskerData.getQ3Value(series, item);

        double xx = domainAxis.valueToJava2D(x.doubleValue(), dataArea,
                plot.getDomainAxisEdge());

        RectangleEdge location = plot.getRangeAxisEdge();
        double yyMax = rangeAxis.valueToJava2D(yMax.doubleValue(), dataArea,
                location);
        double yyMin = rangeAxis.valueToJava2D(yMin.doubleValue(), dataArea,
                location);
        double yyMedian = rangeAxis.valueToJava2D(yMedian.doubleValue(),
                dataArea, location);
        double yyAverage = 0.0;
        if (yAverage != null) {
            yyAverage = rangeAxis.valueToJava2D(yAverage.doubleValue(),
                    dataArea, location);
        }
        double yyQ1Median = rangeAxis.valueToJava2D(yQ1Median.doubleValue(),
                dataArea, location);
        double yyQ3Median = rangeAxis.valueToJava2D(yQ3Median.doubleValue(),
                dataArea, location);

        double exactBoxWidth = getBoxWidth();
        double width = exactBoxWidth;
        double dataAreaX = dataArea.getHeight();
        double maxBoxPercent = 0.1;
        double maxBoxWidth = dataAreaX * maxBoxPercent;
        if (exactBoxWidth <= 0.0) {
            int itemCount = boxAndWhiskerData.getItemCount(series);
            exactBoxWidth = dataAreaX / itemCount * 4.5 / 7;
            if (exactBoxWidth < 3) {
                width = 3;
            }
            else if (exactBoxWidth > maxBoxWidth) {
                width = maxBoxWidth;
            }
            else {
                width = exactBoxWidth;
            }
        }

        g2.setPaint(getItemPaint(series, item));
        Stroke s = getItemStroke(series, item);
        g2.setStroke(s);

        /**
         * =================================================================
         * PH4r05 edit START
         * =================================================================
         */
         // ph4r05 edit:
         // if minimum/maximum should be drawn
         if (this.isMinMaxVisible()){
            // draw the upper shadow
            if (this.isQuartilsVisible()){
                g2.draw(new Line2D.Double(yyMax, xx, yyQ3Median, xx));
            }
            g2.draw(new Line2D.Double(yyMax, xx - width / 2, yyMax,
                    xx + width / 2));

            // draw the lower shadow
            if (this.isQuartilsVisible()){
                g2.draw(new Line2D.Double(yyMin, xx, yyQ1Median, xx));
            }
            g2.draw(new Line2D.Double(yyMin, xx - width / 2, yyMin,
                    xx + width / 2));
        }

        Shape box = null;

        // ph4r05 edit:
        // if quartiles should be drawn
        if (this.isQuartilsVisible()){
            // draw the body
            if (yyQ1Median < yyQ3Median) {
                box = new Rectangle2D.Double(yyQ1Median, xx - width / 2,
                        yyQ3Median - yyQ1Median, width);
            }
            else {
                box = new Rectangle2D.Double(yyQ3Median, xx - width / 2,
                        yyQ1Median - yyQ3Median, width);
            }
            if (this.fillBox) {
                g2.setPaint(lookupBoxPaint(series, item));
                g2.fill(box);
            }
            g2.setStroke(getItemOutlineStroke(series, item));
            g2.setPaint(getItemOutlinePaint(series, item));
            g2.draw(box);

            // draw median
            g2.setPaint(getArtifactPaint());
            g2.draw(new Line2D.Double(yyMedian,
                    xx - width / 2, yyMedian, xx + width / 2));
        }

        // draw average - SPECIAL AIMS REQUIREMENT
        if (yAverage != null) {
            double aRadius = width / 4;
            // here we check that the average marker will in fact be visible
            // before drawing it...
            if ((yyAverage > (dataArea.getMinX() - aRadius))
                    && (yyAverage < (dataArea.getMaxX() + aRadius))) {
                Ellipse2D.Double avgEllipse = new Ellipse2D.Double(
                        yyAverage - aRadius, xx - aRadius, aRadius * 2,
                        aRadius * 2);
                g2.fill(avgEllipse);
                g2.draw(avgEllipse);
            }
        }

        // FIXME: draw outliers

        // add an entity for the item...
        if (entities != null && box!=null && box.intersects(dataArea)) {
            addEntity(entities, box, dataset, series, item, yyAverage, xx);
        }
        /**
         * =================================================================
         * PH4r05 edit START
         * =================================================================
         */

    }

    /**
     * Draws the visual representation of a single data item.
     *
     * @param g2  the graphics device.
     * @param dataArea  the area within which the plot is being drawn.
     * @param info  collects info about the drawing.
     * @param plot  the plot (can be used to obtain standard color
     *              information etc).
     * @param domainAxis  the domain axis.
     * @param rangeAxis  the range axis.
     * @param dataset  the dataset (must be an instance of
     *                 {@link BoxAndWhiskerXYDataset}).
     * @param series  the series index (zero-based).
     * @param item  the item index (zero-based).
     * @param crosshairState  crosshair information for the plot
     *                        (<code>null</code> permitted).
     * @param pass  the pass index.
     */
    public void drawVerticalItem(Graphics2D g2,
                                 Rectangle2D dataArea,
                                 PlotRenderingInfo info,
                                 XYPlot plot,
                                 ValueAxis domainAxis,
                                 ValueAxis rangeAxis,
                                 XYDataset dataset,
                                 int series,
                                 int item,
                                 CrosshairState crosshairState,
                                 int pass) {

        // setup for collecting optional entity info...
        EntityCollection entities = null;
        if (info != null) {
            entities = info.getOwner().getEntityCollection();
        }

        BoxAndWhiskerXYDataset boxAndWhiskerData
            = (BoxAndWhiskerXYDataset) dataset;

        Number x = boxAndWhiskerData.getX(series, item);
        Number yMax = boxAndWhiskerData.getMaxRegularValue(series, item);
        Number yMin = boxAndWhiskerData.getMinRegularValue(series, item);
        Number yMedian = boxAndWhiskerData.getMedianValue(series, item);
        Number yAverage = boxAndWhiskerData.getMeanValue(series, item);
        Number yQ1Median = boxAndWhiskerData.getQ1Value(series, item);
        Number yQ3Median = boxAndWhiskerData.getQ3Value(series, item);
        List yOutliers = boxAndWhiskerData.getOutliers(series, item);

        double xx = domainAxis.valueToJava2D(x.doubleValue(), dataArea,
                plot.getDomainAxisEdge());

        RectangleEdge location = plot.getRangeAxisEdge();
        double yyMax = rangeAxis.valueToJava2D(yMax.doubleValue(), dataArea,
                location);
        double yyMin = rangeAxis.valueToJava2D(yMin.doubleValue(), dataArea,
                location);
        double yyMedian = rangeAxis.valueToJava2D(yMedian.doubleValue(),
                dataArea, location);
        double yyAverage = 0.0;
        if (yAverage != null) {
            yyAverage = rangeAxis.valueToJava2D(yAverage.doubleValue(),
                    dataArea, location);
        }
        double yyQ1Median = rangeAxis.valueToJava2D(yQ1Median.doubleValue(),
                dataArea, location);
        double yyQ3Median = rangeAxis.valueToJava2D(yQ3Median.doubleValue(),
                dataArea, location);
        double yyOutlier;


        double exactBoxWidth = getBoxWidth();
        double width = exactBoxWidth;
        double dataAreaX = dataArea.getMaxX() - dataArea.getMinX();
        double maxBoxPercent = 0.1;
        double maxBoxWidth = dataAreaX * maxBoxPercent;
        if (exactBoxWidth <= 0.0) {
            int itemCount = boxAndWhiskerData.getItemCount(series);
            exactBoxWidth = dataAreaX / itemCount * 4.5 / 7;
            if (exactBoxWidth < 3) {
                width = 3;
            }
            else if (exactBoxWidth > maxBoxWidth) {
                width = maxBoxWidth;
            }
            else {
                width = exactBoxWidth;
            }
        }

        g2.setPaint(getItemPaint(series, item));
        Stroke s = getItemStroke(series, item);
        g2.setStroke(s);

         /**
          * =================================================================
          * PH4r05 edit START
          * =================================================================
          */
         // ph4r05 edit:
         // if minimum/maximum should be drawn
         if (this.isMinMaxVisible()){
            // draw the upper shadow
            if (this.isQuartilsVisible()){
                g2.draw(new Line2D.Double(xx, yyMax, xx, yyQ3Median));
            }
            g2.draw(new Line2D.Double(xx - width / 2, yyMax, xx + width / 2,
                    yyMax));

            // draw the lower shadow
            if (this.isQuartilsVisible()){
                g2.draw(new Line2D.Double(xx, yyMin, xx, yyQ1Median));
            }
            g2.draw(new Line2D.Double(xx - width / 2, yyMin, xx + width / 2,
                    yyMin));
        }

        // draw the body
        Shape box = null;

        // ph4r05 edit:
        // if quartiles should be drawn
        if (this.isQuartilsVisible()){
            if (yyQ1Median > yyQ3Median) {
                box = new Rectangle2D.Double(xx - width / 2, yyQ3Median, width,
                        yyQ1Median - yyQ3Median);
            }
            else {
                box = new Rectangle2D.Double(xx - width / 2, yyQ1Median, width,
                        yyQ3Median - yyQ1Median);
            }
            if (this.fillBox) {
                g2.setPaint(lookupBoxPaint(series, item));
                g2.fill(box);
            }
            g2.setStroke(getItemOutlineStroke(series, item));
            g2.setPaint(getItemOutlinePaint(series, item));
            g2.draw(box);
        }

        // draw median
        g2.setPaint(getArtifactPaint());
        g2.draw(new Line2D.Double(xx - width / 2, yyMedian, xx + width / 2,
                yyMedian));

        double aRadius = 0;                 // average radius
        double oRadius = width / 3;    // outlier radius

        // draw average - SPECIAL AIMS REQUIREMENT
        if (yAverage != null) {
            aRadius = width / 4;
            // here we check that the average marker will in fact be visible
            // before drawing it...
            if ((yyAverage > (dataArea.getMinY() - aRadius))
                    && (yyAverage < (dataArea.getMaxY() + aRadius))) {
                Ellipse2D.Double avgEllipse = new Ellipse2D.Double(xx - aRadius,
                        yyAverage - aRadius, aRadius * 2, aRadius * 2);
                g2.fill(avgEllipse);
                g2.draw(avgEllipse);
            }
        }

        // ph4r05 edit:
        // if outliers should be drawn
        if (this.isOutliersVisible()){
            List outliers = new ArrayList();
            OutlierListCollection outlierListCollection
                    = new OutlierListCollection();

            /* From outlier array sort out which are outliers and put these into
             * an arraylist. If there are any farouts, set the flag on the
             * OutlierListCollection
             */

            for (int i = 0; i < yOutliers.size(); i++) {
                double outlier = ((Number) yOutliers.get(i)).doubleValue();
                if (outlier > boxAndWhiskerData.getMaxOutlier(series,
                        item).doubleValue()) {
                    outlierListCollection.setHighFarOut(true);
                }
                else if (outlier < boxAndWhiskerData.getMinOutlier(series,
                        item).doubleValue()) {
                    outlierListCollection.setLowFarOut(true);
                }
                else if (outlier > boxAndWhiskerData.getMaxRegularValue(series,
                        item).doubleValue()) {
                    yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea,
                            location);
                    outliers.add(new Outlier(xx, yyOutlier, oRadius));
                }
                else if (outlier < boxAndWhiskerData.getMinRegularValue(series,
                        item).doubleValue()) {
                    yyOutlier = rangeAxis.valueToJava2D(outlier, dataArea,
                            location);
                    outliers.add(new Outlier(xx, yyOutlier, oRadius));
                }
                Collections.sort(outliers);
            }

            // Process outliers. Each outlier is either added to the appropriate
            // outlier list or a new outlier list is made
            for (Iterator iterator = outliers.iterator(); iterator.hasNext();) {
                Outlier outlier = (Outlier) iterator.next();
                outlierListCollection.add(outlier);
            }

            // draw yOutliers
            double maxAxisValue = rangeAxis.valueToJava2D(rangeAxis.getUpperBound(),
                    dataArea, location) + aRadius;
            double minAxisValue = rangeAxis.valueToJava2D(rangeAxis.getLowerBound(),
                    dataArea, location) - aRadius;

            // draw outliers
            for (Iterator iterator = outlierListCollection.iterator();
                    iterator.hasNext();) {
                OutlierList list = (OutlierList) iterator.next();
                Outlier outlier = list.getAveragedOutlier();
                Point2D point = outlier.getPoint();

                if (list.isMultiple()) {
                    drawMultipleEllipse(point, width, oRadius, g2);
                }
                else {
                    drawEllipse(point, oRadius, g2);
                }
            }

            // draw farout
            if (outlierListCollection.isHighFarOut()) {
                drawHighFarOut(aRadius, g2, xx, maxAxisValue);
            }

            if (outlierListCollection.isLowFarOut()) {
                drawLowFarOut(aRadius, g2, xx, minAxisValue);
            }
        }

        // add an entity for the item...
        if (entities != null && box!=null && box.intersects(dataArea)) {
            addEntity(entities, box, dataset, series, item, xx, yyAverage);
        }

        /**
         * =================================================================
         * PH4r05 edit END
         * =================================================================
         */
    }

    /**
     * Draws an ellipse to represent an outlier.
     *
     * @param point  the location.
     * @param oRadius  the radius.
     * @param g2  the graphics device.
     */
    protected void drawEllipse(Point2D point, double oRadius, Graphics2D g2) {
        Ellipse2D.Double dot = new Ellipse2D.Double(point.getX() + oRadius / 2,
                point.getY(), oRadius, oRadius);
        g2.draw(dot);
    }

    /**
     * Draws two ellipses to represent overlapping outliers.
     *
     * @param point  the location.
     * @param boxWidth  the box width.
     * @param oRadius  the radius.
     * @param g2  the graphics device.
     */
    protected void drawMultipleEllipse(Point2D point, double boxWidth,
                                       double oRadius, Graphics2D g2) {

        Ellipse2D.Double dot1 = new Ellipse2D.Double(point.getX()
                - (boxWidth / 2) + oRadius, point.getY(), oRadius, oRadius);
        Ellipse2D.Double dot2 = new Ellipse2D.Double(point.getX()
                + (boxWidth / 2), point.getY(), oRadius, oRadius);
        g2.draw(dot1);
        g2.draw(dot2);

    }

    /**
     * Draws a triangle to indicate the presence of far out values.
     *
     * @param aRadius  the radius.
     * @param g2  the graphics device.
     * @param xx  the x value.
     * @param m  the max y value.
     */
    protected void drawHighFarOut(double aRadius, Graphics2D g2, double xx,
            double m) {
        double side = aRadius * 2;
        g2.draw(new Line2D.Double(xx - side, m + side, xx + side, m + side));
        g2.draw(new Line2D.Double(xx - side, m + side, xx, m));
        g2.draw(new Line2D.Double(xx + side, m + side, xx, m));
    }

    /**
     * Draws a triangle to indicate the presence of far out values.
     *
     * @param aRadius  the radius.
     * @param g2  the graphics device.
     * @param xx  the x value.
     * @param m  the min y value.
     */
    protected void drawLowFarOut(double aRadius, Graphics2D g2, double xx,
            double m) {
        double side = aRadius * 2;
        g2.draw(new Line2D.Double(xx - side, m - side, xx + side, m - side));
        g2.draw(new Line2D.Double(xx - side, m - side, xx, m));
        g2.draw(new Line2D.Double(xx + side, m - side, xx, m));
    }

    /**
     * Tests this renderer for equality with another object.
     *
     * @param obj  the object (<code>null</code> permitted).
     *
     * @return <code>true</code> or <code>false</code>.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RSSIXYBoxAndWhiskerRenderer)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        RSSIXYBoxAndWhiskerRenderer that = (RSSIXYBoxAndWhiskerRenderer) obj;
        if (this.boxWidth != that.getBoxWidth()) {
            return false;
        }
        if (!PaintUtilities.equal(this.boxPaint, that.boxPaint)) {
            return false;
        }
        if (!PaintUtilities.equal(this.artifactPaint, that.artifactPaint)) {
            return false;
        }
        if (this.fillBox != that.fillBox) {
            return false;
        }
        return true;

    }

    /**
     * Provides serialization support.
     *
     * @param stream  the output stream.
     *
     * @throws IOException  if there is an I/O error.
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtilities.writePaint(this.boxPaint, stream);
        SerialUtilities.writePaint(this.artifactPaint, stream);
    }

    /**
     * Provides serialization support.
     *
     * @param stream  the input stream.
     *
     * @throws IOException  if there is an I/O error.
     * @throws ClassNotFoundException  if there is a classpath problem.
     */
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {

        stream.defaultReadObject();
        this.boxPaint = SerialUtilities.readPaint(stream);
        this.artifactPaint = SerialUtilities.readPaint(stream);
    }

    /**
     * Returns a clone of the renderer.
     *
     * @return A clone.
     *
     * @throws CloneNotSupportedException  if the renderer cannot be cloned.
     */
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
