package net.rptools;

import com.bw.jtools.shape.AbstractShape;
import com.bw.jtools.shape.Context;
import com.bw.jtools.svg.SVGConverter;
import com.bw.jtools.svg.SVGException;
import net.rptools.lib.image.ImageUtil;
import net.rptools.lib.image.RenderQuality;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class SVGIcon extends ImageIcon {
    private final int width;
    private final int height;

    public SVGIcon(String fileName, int width, int height){
        super();
        this.width = width;
        this.height = height;
        try {
            URL url = getClass().getClassLoader().getResource(fileName);
            if(url != null) {
                AbstractShape shape = SVGConverter.convert(url.openStream());
                Rectangle2D r = shape.getTransformedBounds();
                BufferedImage bi = new BufferedImage(
                        (int) Math.ceil(r.getWidth()),
                        (int) Math.ceil(r.getHeight()),
                        BufferedImage.TYPE_4BYTE_ABGR_PRE);
                shape.paint(new Context(bi.createGraphics(), true));
                setImage(ImageUtil.scaleBufferedImage(bi, width, height, RenderQuality.MEDIUM_SCALING));
            }
        } catch (IOException|SVGException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        super.paintIcon(c,g,x,y);
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }
}
