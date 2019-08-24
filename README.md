# spring-acl-enhancements

The spring-security-acl Library is Spring's framework for addressing Data Entitlements. 
Enforcing data entitlements is a thorny task, and Spring does a pretty good job at 
addressing this problem in a high level. 
There are however some things that could be done better. 
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
 
 That means that once you start monitoring a class with Spring acl, 
 you cannot refactor it by moving it to another package. 
 Or rather you could, but then you would have to go and update all the existing entries in the database.
 If you are doing blue-green deployment, you need an even more complicated sequence of database scripts.
 What should have been a simple move refactor, turned into an ordeal.
 
### Problem 2: Bootstrapping permissions for new instances created in the database

When an instance of a class monitored by ACL is first created it needs to have some default permissions assigned.
If an instance is created with a corresponding entry in *acl_object_identity*, 
Spring ACL will fail hard with an unchecked NotFoundException.
