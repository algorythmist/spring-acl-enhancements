package com.tecacet.acl.framework.dao;

import static org.junit.Assert.*;

import javax.transaction.Transactional;
import java.util.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import com.tecacet.acl.framework.PermissionType;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class AclDaoTest {

    @Autowired
    private AclDao dao;

    @Test
    public void findObjectClassId() {
        Optional<Long> id = dao.findObjectClassId("CUSTOMER");
        assertEquals(1L, id.get().longValue());
    }

    @Test
    public void findObjectClassId_notFound() {
        Optional<Long> id = dao.findObjectClassId("CLIENT");
        assertFalse(id.isPresent());
    }

    @Test
    public void findSidById() {
        Optional<Long> id = dao.findSidId("manager");
        assertEquals(2L, id.get().longValue());
    }

}
