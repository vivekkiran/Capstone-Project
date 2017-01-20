package com.vivek.codemozo.loaders;

import android.content.Context;
import android.database.Cursor;

import com.vivek.codemozo.db.entry.ResourceEntry;
import com.vivek.codemozo.model.Website;
import com.vivek.codemozo.utils.Debug;

import java.util.List;

public class ResourceSettingLoader extends BaseLoader<List<Website>> {

    public ResourceSettingLoader(Context context) {
        super(context);
    }

    @Override
    public List<Website> loadInBackground() {
        Debug.c();
        Cursor cursor = getContext().getContentResolver().query(
                ResourceEntry.CONTENT_URI_ALL,
                ResourceEntry.RESOURCE_PROJECTION,
                null,
                null,
                null
        );

        List<Website> list = Website.cursorToList(cursor);
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
        return list;
    }
}
