package com.tecacet.acl.framework.listener;

import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.stereotype.Component;
import com.tecacet.acl.framework.AclDataAccessService;
import com.tecacet.acl.framework.AclSecured;

/**
 * Listen to insert events. If the object inserted is subject to entitlements, ie annotated with AclSecured,
 * then insert a row in acl_object_identity with a defauly owner and permissions
 */
@Component
public class AclHibernateListener implements PostInsertEventListener {

    private static final String DEFAULT_ROLE = "manager";

    private final AclDataAccessService aclService;

    @Autowired
    public AclHibernateListener(AclDataAccessService aclService) {
        this.aclService = aclService;
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister entityPersister) {
        return false;
    }

    @Override
    public boolean requiresPostCommitHandling(EntityPersister persister) {
        return false;
    }

    @Override
    public void onPostInsert(PostInsertEvent postInsertEvent) {
        Object entity = postInsertEvent.getEntity();
        Class clazz = entity.getClass();
        if (clazz.isAnnotationPresent(AclSecured.class)) {
            aclService.insertObjectIdentity(clazz, (long) postInsertEvent.getId(), DEFAULT_ROLE, BasePermission.READ);
        }

    }
}
