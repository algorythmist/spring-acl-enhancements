package com.tecacet.acl.framework.listener;

import org.hibernate.event.spi.PostInsertEvent;
import org.hibernate.event.spi.PostInsertEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import com.tecacet.acl.framework.AclSecured;
import com.tecacet.acl.framework.PermissionType;
import com.tecacet.acl.framework.dao.AclObjectIdentityDao;

@Component
public class AclHibernateListener implements PostInsertEventListener {

    private static final String DEFAULT_ROLE = "manager";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AclObjectIdentityDao objectIdentityDao;

    @Autowired
    public AclHibernateListener(AclObjectIdentityDao dao) {
        this.objectIdentityDao = dao;
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
            AclSecured secured = (AclSecured) clazz.getAnnotation(AclSecured.class);
            logger.info("Inserting new id");
            objectIdentityDao.insertObjectIdentity((Long)postInsertEvent.getId(), secured.name(),
                    DEFAULT_ROLE, PermissionType.READ);
        }

    }
}
