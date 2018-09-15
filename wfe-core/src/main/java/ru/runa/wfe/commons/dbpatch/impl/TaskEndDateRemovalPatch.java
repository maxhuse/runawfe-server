package ru.runa.wfe.commons.dbpatch.impl;

import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DbPatch;

import com.google.common.collect.Lists;

public class TaskEndDateRemovalPatch extends DbPatch {

    @Override
    public void executeDML(Session session) throws Exception {
        log.info("Deleted completed tasks: " + session.createSQLQuery("DELETE FROM BPM_TASK WHERE END_DATE IS NOT NULL").executeUpdate());
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = Lists.newArrayList();
        sql.add(getDDLDropColumn("BPM_TASK", "END_DATE"));
        return sql;
    }
}
