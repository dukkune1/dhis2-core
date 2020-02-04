package org.hisp.dhis.tracker.validation.hooks;

import org.hisp.dhis.organisationunit.FeatureType;
import org.hisp.dhis.trackedentity.TrackedEntityType;
import org.hisp.dhis.tracker.TrackerErrorCode;
import org.hisp.dhis.tracker.bundle.TrackerBundle;
import org.hisp.dhis.tracker.domain.TrackedEntity;
import org.hisp.dhis.tracker.report.TrackerErrorReport;
import org.hisp.dhis.tracker.validation.ValidationHookErrorReporter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/*
 * Copyright (c) 2004-2020, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

/**
 * @author Morten Svanæs <msvanaes@dhis2.org>
 */
@Component
public class TrackedEntityExistValidationHook
    extends AbstractTrackerValidationHook
{

    @Override
    public int getOrder()
    {
        return 3;
    }

    @Override
    public List<TrackerErrorReport> validate( TrackerBundle bundle )
    {
        // Is it necessary to check for existence on delete,
        // cant we just try to delete and check for update result from DB?
        if ( bundle.getImportStrategy().isDelete() )
        {
            return Collections.emptyList();
        }

        ValidationHookErrorReporter errorReporter = new ValidationHookErrorReporter( bundle,
            TrackedEntityExistValidationHook.class );

        List<TrackedEntity> trackedEntities = bundle.getTrackedEntities();
        for ( TrackedEntity te : trackedEntities )
        {
            // This is a very expensive check... move out/optimize to preheater?
            boolean exists = trackedEntityInstanceStore.existsIncludingDeleted( te.getTrackedEntity() );

            if ( bundle.getImportStrategy().isCreate() && exists )
            {
                errorReporter.raiseError( TrackerErrorCode.E1002, te.getTrackedEntity() );
            }
            else if ( bundle.getImportStrategy().isUpdate() && !exists )
            {
                errorReporter.raiseError( TrackerErrorCode.E1063, te.getTrackedEntity() );
            }
        }

        return errorReporter.getReportList();
    }
}
