package com.vivek.codemozo.model.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by devenv on 1/20/17.
 */

public class ContestsModel extends RealmObject{
    String _id;
    @PrimaryKey
    String cid;
    String name;
    String url;
    String resource_id;
    String start;
    String end;
    String duration;
}
