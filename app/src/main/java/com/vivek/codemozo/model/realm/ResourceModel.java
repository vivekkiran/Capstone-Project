package com.vivek.codemozo.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by devenv on 1/20/17.
 */

public class ResourceModel extends RealmObject {
    String _id;
    @PrimaryKey
    String rid;
    String rname;
    String show;
}
