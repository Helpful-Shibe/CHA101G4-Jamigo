package com.jamigo.member.member_data.dao.impl;

import com.jamigo.member.member_data.dao.MemberDataDAO;
import com.jamigo.member.member_data.entity.MemberData;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class MemberDataDaoImpl implements MemberDataDAO {
    @PersistenceContext
    private Session session;
    @Override
    @Transactional
    public int insert(MemberData memberdata) {//註冊用
            session.persist(memberdata);
            return 1;
    }

    @Override
    @Transactional
    public int deleteById(Integer memberNo) {
            MemberData member = session.get(MemberData.class, memberNo);
            session.remove(member);
            return 1;
    }

    @Override
    @Transactional
    public int update(MemberData memberdata) {
            session.merge(memberdata);
            Hibernate.initialize(MemberData.class);
            return 1;
        }


    @Override
    @Transactional
    public MemberData selectById(Integer memberNo) {
            MemberData resuluMemberData = session.get(MemberData.class, memberNo);
            return resuluMemberData;
    }

    @Override
    @Transactional
    public List<MemberData> selectAll() {
            List<MemberData> result = null;
            String hql = " from MemberData order by memberNo";
            Query<MemberData> query = session.createQuery(hql, MemberData.class);
            result = query.list();
            return result;
    }


    @Override
    @Transactional
    public MemberData selectBymemberAccount(String memberAccount) {
            CriteriaBuilder cBuilder = session.getCriteriaBuilder();
            CriteriaQuery<MemberData> cQuery = cBuilder.createQuery(MemberData.class);
            Root<MemberData> root = cQuery.from(MemberData.class);
            cQuery.where(cBuilder.equal(root.get("memberAccount"), memberAccount));
            return session
                    .createQuery(cQuery)
                    .uniqueResult();
    }


    @Override
    @Transactional
    public MemberData selectBymemberEmail(String memberEmail) {
        CriteriaBuilder cBuilder = session.getCriteriaBuilder();
        CriteriaQuery<MemberData> cQuery = cBuilder.createQuery(MemberData.class);
        Root<MemberData> root = cQuery.from(MemberData.class);
        cQuery.where(cBuilder.equal(root.get("memberEmail"), memberEmail));
        return session
                .createQuery(cQuery)
                .uniqueResult();
    }


    @Override
    @Transactional
    public MemberData selectForLogin(String memberAccount, String memberPassword) {
        final String sql = "select * from jamigo.member_data where memberAccount = :memberAccount and memberPassword = :memberPassword";
            return session
                    .createNativeQuery(sql, MemberData.class)
                    .setParameter("memberAccount", memberAccount)
                    .setParameter("memberPassword", memberPassword)
                    .uniqueResult();
    }
}
