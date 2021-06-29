package org.smartregister.uniceftunisia.service.intent;

import android.content.Context;

import androidx.annotation.NonNull;

import org.smartregister.sync.intent.SyncIntentService;
import org.smartregister.uniceftunisia.helper.AppValidateAssignmentHelper;
import org.smartregister.util.SyncUtils;

public class AppSyncIntentService extends SyncIntentService {

    @Override
    protected void init(@NonNull Context context) {
        super.init(context);
        validateAssignmentHelper = new AppValidateAssignmentHelper(new SyncUtils(getBaseContext()));
    }
}
