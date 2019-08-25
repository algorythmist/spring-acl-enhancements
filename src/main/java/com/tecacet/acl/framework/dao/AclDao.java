package com.tecacet.acl.framework.dao;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@Deprecated
public class AclDao {

    private static final String INSERT_ACL =
            "INSERT INTO acl_object_identity (`object_id_class`, `object_id_identity`, `parent_object`, `owner_sid`, `entries_inheriting`) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String INSERT_ENTRY =
            "INSERT INTO acl_entry (acl_object_identity, ace_order, sid, mask, granting, audit_success, audit_failure) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public AclDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Long> findObjectClassId(String className) {
        List<Long> ids = jdbcTemplate.queryForList("select id from acl_class where class = ?",
                new Object[]{className}, Long.class);
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }

    public Optional<Long> findSidId(String sid) {
        List<Long> ids = jdbcTemplate.queryForList("select id from acl_sid where sid = ?",
                new Object[]{sid}, Long.class);
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }

//    public long insertObjectIdentity(long objectId, String objectType, String sid,
//                                     PermissionType... permissions) {
//        long objectClass = findObjectClassId(objectType)
//                .orElseThrow(() -> new NotFoundException("There is no registered class " + objectType));
//        long sidId = findSidId(sid)
//                .orElseThrow(() -> new NotFoundException("There is no sid " + sid));
//
//        jdbcTemplate.update(INSERT_ACL, objectClass, objectId, null, sidId, 1);
//        long objectIdentityId = getObjectIdentityId(objectId, objectClass);
//        insertAccessControlEntries(objectIdentityId, sidId, permissions);
//        return objectIdentityId;
//    }

    public long getObjectIdentityId(long objectId, long objectClass) {
        return jdbcTemplate.queryForObject("select id from acl_object_identity where object_id_class= ? and object_id_identity =?",
                new Object[]{objectClass, objectId}, Long.class);
    }

//    public void insertAccessControlEntries(long objectIdentityId, long sidId,
//                                           PermissionType... permissions) {
//        for (int order = 0; order < permissions.length; order++) {
//            jdbcTemplate.update(INSERT_ENTRY,
//                    objectIdentityId, order, sidId, permissions[order].getType(), 1, 1, 1);
//        }
//    }

    public void deleteAllEntitlements() {
        jdbcTemplate.update("delete from acl_entry");
        jdbcTemplate.update("delete from acl_object_identity");
    }
}
