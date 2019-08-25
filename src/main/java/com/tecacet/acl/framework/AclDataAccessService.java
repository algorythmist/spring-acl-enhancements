package com.tecacet.acl.framework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.domain.PrincipalSid;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.Permission;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;

@Service
public class AclDataAccessService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MutableAclService aclService;

    public AclDataAccessService(MutableAclService aclService) {
        this.aclService = aclService;
    }

    public void insertObjectIdentity(Object entity, long entityId, String owner, Permission... permissions) {
        Class clazz = entity.getClass();
        if (clazz.isAnnotationPresent(AclSecured.class)) {
            AclSecured secured = (AclSecured) clazz.getAnnotation(AclSecured.class);
            logger.info("Inserting entitlement for type {} and object id {}", secured.name(), entityId);
            MutableAcl acl = aclService.createAcl(new ObjectIdentityImpl(secured.name(), entityId));
            Sid sid = new PrincipalSid(owner);
            acl.setOwner(sid);
            for (int order = 0; order < permissions.length; order++) {
                acl.insertAce(order, permissions[order], sid, true);
            }
            aclService.updateAcl(acl);
        }
    }

    public void insertAccessEntry(Class clazz, long id, String principal, Permission permission) {
        MutableAcl acl = (MutableAcl) aclService.readAclById(new ObjectIdentityImpl(clazz, id));
        int size = acl.getEntries().size();
        Sid sid = new PrincipalSid(principal);
        acl.insertAce(size, permission, sid, true);
        aclService.updateAcl(acl);
    }
}
