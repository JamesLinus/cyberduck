package ch.cyberduck.ui.cocoa;

/*
 *  Copyright (c) 2008 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Session;
import ch.cyberduck.ui.cocoa.threading.BackgroundActionRegistry;
import ch.cyberduck.ui.cocoa.threading.RepeatableBackgroundAction;
import ch.cyberduck.ui.cocoa.threading.WindowMainAction;

/**
 * @version $Id:$
 */
public abstract class BrowserBackgroundAction extends RepeatableBackgroundAction {
    private CDBrowserController controller;

    public BrowserBackgroundAction(CDBrowserController controller) {
        super(controller);
        this.controller = controller;
    }

    public CDBrowserController getController() {
        return controller;
    }

    @Override
    public void init() {
        // Add to the registry so it will be displayed in the activity window.
        BackgroundActionRegistry.instance().add(this);
    }

    public Session getSession() {
        return controller.getSession();
    }

    @Override
    public boolean prepare() {
        CDMainApplication.invoke(new WindowMainAction(controller) {
            public void run() {
                controller.spinner.startAnimation(null);
            }
        });
        return super.prepare();
    }

    @Override
    public void cancel() {
        if(this.isRunning()) {
            this.getSession().interrupt();
        }
        super.cancel();
    }

    @Override
    public void finish() {
        super.finish();
        CDMainApplication.invoke(new WindowMainAction(controller) {
            public void run() {
                controller.spinner.stopAnimation(null);
                controller.updateStatusLabel(null);
            }
        });
    }

    @Override
    public boolean isCanceled() {
        if(!controller.isVisible()) {
            return false;
        }
        return super.isCanceled();
    }

    @Override
    public Object lock() {
        return controller.getSession();
    }
}