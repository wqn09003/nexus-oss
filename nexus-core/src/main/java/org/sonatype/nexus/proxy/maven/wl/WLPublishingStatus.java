/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.wl;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Punlishing status of a repository.
 * 
 * @author cstamas
 * @since 2.4
 */
public class WLPublishingStatus
{
    /**
     * Status enumeration.
     */
    public static enum PStatus
    {
        /**
         * WL is published okay for given repository.
         */
        PUBLISHED,

        /**
         * WL is not published for given repository.
         */
        NOT_PUBLISHED;
    }

    private final PStatus status;

    private final long lastPublishedTimestamp;

    private final String lastPublishedFilePath;

    /**
     * Constructor.
     * 
     * @param status
     * @param lastPublishedTimestamp
     * @param lastPublishedFilePath
     */
    public WLPublishingStatus( final PStatus status, final long lastPublishedTimestamp,
                               final String lastPublishedFilePath )
    {
        this.status = checkNotNull( status );
        this.lastPublishedTimestamp = lastPublishedTimestamp;
        this.lastPublishedFilePath = lastPublishedFilePath;
    }

    /**
     * Publishing status.
     * 
     * @return publishing status.
     */
    public PStatus getStatus()
    {
        return status;
    }

    /**
     * Time stamp (milliseconds) of the last published WL, or -1 if not published.
     * 
     * @return time stamp (milliseconds) of the last published WL, or -1 if not published.
     */
    public long getLastPublishedTimestamp()
    {
        if ( getStatus() == PStatus.PUBLISHED )
        {
            return lastPublishedTimestamp;
        }
        else
        {
            return -1;
        }
    }

    /**
     * Repository path of the published WL file, or, {@code null} if not published.
     * 
     * @return repository path of the published WL file, or, {@code null} if not published.
     */
    public String getLastPublishedFilePath()
    {
        if ( getStatus() == PStatus.PUBLISHED )
        {
            return lastPublishedFilePath;
        }
        else
        {
            return null;
        }
    }
}
