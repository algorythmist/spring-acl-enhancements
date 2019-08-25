package com.tecacet.acl.framework;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.ChildrenExistException;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;

/**
 * This class enhances an existing AclService by modifying the "type" aspect of the ObjectIdentity
 * Instead of using the full class name as 'type', it gets the type from the AclSecured annotation.
 */
//TODO implement this with AOP
public class IdentityModifyingAclService implements MutableAclService {

    private final MutableAclService delegate;

    public IdentityModifyingAclService(MutableAclService delegate) {
        this.delegate = delegate;
    }

    private ObjectIdentity replace(ObjectIdentity oid) {
        try {
            Class clazz = Class.forName(oid.getType());
            if (clazz.isAnnotationPresent(AclSecured.class)) {
                AclSecured secured = (AclSecured) clazz.getAnnotation(AclSecured.class);
                return new ObjectIdentityImpl(secured.name(), oid.getIdentifier());
            }
            throw new NotFoundException("Class " + oid.getType() + " is not annotated");

        } catch (ClassNotFoundException e) {
            throw new NotFoundException("Type " + oid.getType() + " is not a class");
        }
    }

    private List<ObjectIdentity> replaceAll(Collection<ObjectIdentity> oids) {
        return oids.stream().map(this::replace).collect(Collectors.toList());
    }

    @Override
    public List<ObjectIdentity> findChildren(ObjectIdentity objectIdentity) {
        return delegate.findChildren(replace(objectIdentity));
    }

    @Override
    public Acl readAclById(ObjectIdentity objectIdentity) throws NotFoundException {
        return delegate.readAclById(replace(objectIdentity));
    }

    @Override
    public Acl readAclById(ObjectIdentity objectIdentity, List<Sid> list) throws NotFoundException {
        return delegate.readAclById(replace(objectIdentity), list);
    }

    @Override
    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> list) throws NotFoundException {
        return delegate.readAclsById(replaceAll(list));
    }

    @Override
    public Map<ObjectIdentity, Acl> readAclsById(List<ObjectIdentity> list, List<Sid> sids) throws NotFoundException {
        return delegate.readAclsById(replaceAll(list), sids);
    }


    @Override
    public MutableAcl createAcl(ObjectIdentity objectIdentity) throws AlreadyExistsException {
        return delegate.createAcl(objectIdentity);
    }

    @Override
    public void deleteAcl(ObjectIdentity objectIdentity, boolean deleteChildren) throws ChildrenExistException {
        delegate.deleteAcl(objectIdentity, deleteChildren);
    }

    @Override
    public MutableAcl updateAcl(MutableAcl mutableAcl) throws NotFoundException {
        return delegate.updateAcl(mutableAcl);
    }
}
