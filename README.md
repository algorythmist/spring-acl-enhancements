# spring-acl-enhancements

The spring-security-acl library is a Spring framework for addressing Data Entitlements. 
Enforcing data entitlements is a thorny task, and Spring does a pretty good job at 
addressing this problem at a high level. 
There are however a few things that could be done better. 
The first one, would be better documentation. 
The documentation found [here](https://docs.spring.io/spring-security/site/docs/3.0.x/reference/domain-acls.html)
is pitifully incomplete.
Some tutorials can be found online such as [this](https://www.baeldung.com/spring-security-acl)
but they are usually too basic to present a realistic scenario.

In this article, I present two significant issues with the spring-acl infrastructure
and demonstrate how to solve them.

### Problem 1: Hardcoded class names

Classes monitored by acl have to be stored in the database with the full name hardcoded.
That's because every instance of an entity is uniquely identified by it's id in the database and
 the full name of its class. The class name has to be stored in the *acl_class* table.
 
 That means that once you start monitoring a class with Spring ACL, 
 you cannot refactor it by moving it to another package. 
 Or rather you could, but then you would have to go and update all the existing entries in the database.
 If you are doing blue-green deployment, you need an even more complicated sequence of database scripts.
 What should have been a simple move refactor, turned into an ordeal.
 
### Problem 2: Bootstrapping permissions for new instances created in the database

When an instance of a class monitored by ACL is first created it needs to have some default permissions assigned.
If an instance is created with a corresponding entry in *acl_object_identity*, 
Spring ACL will fail hard with an unchecked NotFoundException.

# Solutions

Here is how to solve these problems:

### Solution to Problem 1

Create an annotation called AclSecured that takes one required parameter. 
Classes monitored by ACL should be annotated so:

```java
@Entity
@AclSecured(name = "CUSTOMER")
public class Customer {

    @Id
    @GeneratedValue
    private long id;

```
The name is required and must uniquely identify the class instead of the class name.
Now you can insert an entry in *acl_class* using this name instead of the full class path:

```mysql-sql
INSERT INTO acl_class (id, class) VALUES (1, 'CUSTOMER');
```
This is not sufficient by itself. We must tinker with the AclService,
to ensure that we query ObjectIdentity instances with the correct name.
We can achieve this by providing an AclService implementation that replaces
the ObjectIdentity before delegating to the actual AclService:

```java
/**
 * This class enhances an existing AclService by modifying the "type" aspect of the ObjectIdentity
 * Instead of using the full class name as 'type', it gets the type from the AclSecured annotation.
 */
public class IdentityModifyingAclService implements AclService {

    private final AclService delegate;

    public IdentityModifyingAclService(AclService delegate) {
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
    
    @Override
    public List<ObjectIdentity> findChildren(ObjectIdentity objectIdentity) {
        return delegate.findChildren(replace(objectIdentity));
    }

    @Override
    public Acl readAclById(ObjectIdentity objectIdentity) throws NotFoundException {
        return delegate.readAclById(replace(objectIdentity));
    }
    
    ...
```

### Solution to Problem 2

To solve the second problem, we need a way to populate acl_object_identity, 
and optionally acl_entry every time an instance of an instance of class is created.
Assuming we are using Hibernate in the persistence layer, 
we can use the PostInsertEventListener to populate the these tables.
The listener must ensure that it only inserts instances for classes monitored by ACL,
and that it uses the correct name in acl_class table:

```java
@Component
public class AclHibernateListener implements PostInsertEventListener {
    ...

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
``` 
