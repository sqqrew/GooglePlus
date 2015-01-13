package com.a_zona.googleplus.event;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

/**
 * Created by Alexey Filyushin (sqqrew@gmail.com) on 11.01.2015.
 * Package: com.a_zona.socialtemplates.events
 * Project: SocialTemplates
 * <p/>
 * Copyright (c) 2014 A-Zona, Inc.
 */
public interface GoogleEventListener {

    void onConnected(Person currentPerson);
    void onConnectionFailed(ConnectionResult result);
    void onResultData(PersonBuffer personBuffer);

}
