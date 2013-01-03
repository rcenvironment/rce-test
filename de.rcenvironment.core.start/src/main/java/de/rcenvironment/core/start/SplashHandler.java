/*
 * Copyright (C) 2006-2012 DLR, Germany
 * 
 * All rights reserved
 * 
 * http://www.rcenvironment.de/
 */

package de.rcenvironment.core.start;



import java.net.URL;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.branding.IProductConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.splash.BasicSplashHandler;

/**
 * Override splash screen.
 * @author Sascha Zur
 */
@SuppressWarnings("restriction")


public class SplashHandler extends BasicSplashHandler {
    private static final String SPLASHIMAGE = "splash.bmp";
    private static final String BUNDLE = "de.rcenvironment.core.start";
    private static final String BUNDLE_VERSION = Platform.getBundle(BUNDLE).getHeaders().get("Bundle-Version").toString();

    private static final int GRAY = 152;
    private static final int WHITE = 255;
    private static final int XCOORD = 189;    
    private static final int YCOORD = 229; 

    private static final int PROGRESSBAR_HEIGHT = 15;

    @Override
    public void init(Shell splash) {
        super.init(splash);
        final URL url = SplashHandler.class.getClassLoader().getResource("resources/version");
        ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(BUNDLE, SPLASHIMAGE);
        Image splashImg = descriptor.createImage();
        GC gc = new GC(splashImg);
        gc.setFont(new Font(gc.getDevice(), "Frutiger Bold", 7, SWT.BOLD));
        gc.setForeground(new Color(null, GRAY, GRAY, GRAY));
        gc.setBackground(new Color(null, WHITE, WHITE, WHITE));
        
        if (url == null){
            gc.drawText("Version " + BUNDLE_VERSION.substring(0, BUNDLE_VERSION.lastIndexOf('.')) , XCOORD, YCOORD);
        }
        splash.setBackgroundMode(SWT.INHERIT_DEFAULT);
        splash.setBackgroundImage(splashImg);
        splash.addPaintListener(new PaintListener(){
            public void paintControl(PaintEvent e){
                e.gc.setFont(new Font(e.gc.getDevice(), "Frutiger Bold", 7, SWT.BOLD));
                e.gc.setForeground(new Color(null, GRAY, GRAY, GRAY));
                e.gc.setBackground(new Color(null, WHITE, WHITE, WHITE));
                if (url == null){
                    e.gc.drawText("Version " + BUNDLE_VERSION.substring(0, BUNDLE_VERSION.lastIndexOf('.')) , XCOORD, YCOORD);
                }
            }
        }); 


        String progressRectString = null;
        String messageRectString = null;
        IProduct product = Platform.getProduct();
        if (product != null) {
            progressRectString = product
                    .getProperty(IProductConstants.STARTUP_PROGRESS_RECT);
            messageRectString = product
                    .getProperty(IProductConstants.STARTUP_MESSAGE_RECT);

        }
        Rectangle progressRect = StringConverter.asRectangle(
                progressRectString, new Rectangle(0, 0, 0, 0));
        progressRect.x = 0;
        progressRect.y = splashImg.getBounds().height - PROGRESSBAR_HEIGHT;
        setProgressRect(progressRect);
        Rectangle messageRect = StringConverter.asRectangle(messageRectString,
                new Rectangle(0, 0, 0, 0));
        messageRect.x = 0;
        messageRect.y = splashImg.getBounds().height - 2 * PROGRESSBAR_HEIGHT;
        setMessageRect(messageRect);
        getContent(); // ensure creation of the progress

    }
}

