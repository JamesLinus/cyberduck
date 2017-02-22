package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.TransferAcceleration;
import ch.cyberduck.core.io.BandwidthThrottle;
import ch.cyberduck.core.io.StreamListener;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.shared.DefaultDownloadFeature;
import ch.cyberduck.core.ssl.X509KeyManager;
import ch.cyberduck.core.ssl.X509TrustManager;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

public class S3ThresholdDownloadService extends DefaultDownloadFeature {
    private static final Logger log = Logger.getLogger(S3ThresholdDownloadService.class);

    private final Preferences preferences
            = PreferencesFactory.get();

    private final S3Session session;
    private final TransferAcceleration<S3Session> accelerateTransferOption;
    private final X509TrustManager trust;
    private final X509KeyManager key;

    public S3ThresholdDownloadService(final S3Session session,
                                      final X509TrustManager trust,
                                      final X509KeyManager key,
                                      final TransferAcceleration<S3Session> accelerateTransferOption) {
        super(new S3ReadFeature(session));
        this.session = session;
        this.trust = trust;
        this.key = key;
        this.accelerateTransferOption = accelerateTransferOption;
    }

    @Override
    public void download(final Path file, final Local local, final BandwidthThrottle throttle,
                         final StreamListener listener, final TransferStatus status, final ConnectionCallback prompt) throws BackgroundException {
        final Host bookmark = session.getHost();
        try {
            if(this.accelerate(file, status, prompt, bookmark)) {
                if(log.isInfoEnabled()) {
                    log.info(String.format("Tunnel download for file %s through accelerated endpoint %s", file, accelerateTransferOption));
                }
                accelerateTransferOption.configure(true, file, trust, key);
            }
            else {
                log.warn(String.format("Transfer acceleration disabled for %s", file));
            }
        }
        catch(AccessDeniedException e) {
            log.warn(String.format("Ignore failure reading S3 accelerate configuration. %s", e.getMessage()));
        }
        super.download(file, local, throttle, listener, status, prompt);
    }

    private boolean accelerate(final Path file, final TransferStatus status, final ConnectionCallback prompt, final Host bookmark) throws BackgroundException {
        switch(session.getSignatureVersion()) {
            case AWS2:
                return false;
        }
        if(accelerateTransferOption.getStatus(file)) {
            log.info(String.format("S3 transfer acceleration enabled for file %s", file));
            return true;
        }
        if(preferences.getBoolean("s3.accelerate.prompt")) {
            if(accelerateTransferOption.prompt(bookmark, file, status, prompt)) {
                log.info(String.format("S3 transfer acceleration enabled for file %s", file));
                return true;
            }
        }
        return false;
    }
}
