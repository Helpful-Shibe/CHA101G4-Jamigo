package com.jamigo.counter.activityOrder.entity;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jamigo.activity.activity_approve.model.Activity;
import com.jamigo.activity.attendee.entity.ActivityAttendeeVO;
import com.jamigo.member.member_data.entity.MemberData;

@Entity
@DynamicUpdate
@Table(name = "activity_order")
public class ActivityOrderVO implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer activityOrderNo;
	private Integer activityNo;
	private Integer memberNo;
	@CreationTimestamp
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Timestamp activityEnrollmentTime;
//	private Date activityEnrollmentTime;
	private Byte activityPaymentStat;
	private Integer memberCouponNo;
	private Integer numberOfAttendee;
	private String commentDetail;
	private Integer activityScore;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "memberNo",
				insertable = false, updatable = false)
	private MemberData memberData;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "activityNo",
				insertable = false, updatable = false)
	private Activity activity;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name = "activityOrderNo",
				referencedColumnName = "activityOrderNo")
	private List<ActivityAttendeeVO> activityAttendeeVO;
	
	public ActivityOrderVO() {}


	public Integer getActivityOrderNo() {
		return activityOrderNo;
	}


	public void setActivityOrderNo(Integer activityOrderNo) {
		this.activityOrderNo = activityOrderNo;
	}


	public Integer getActivityNo() {
		return activityNo;
	}


	public void setActivityNo(Integer activityNo) {
		this.activityNo = activityNo;
	}


	public Integer getMemberNo() {
		return memberNo;
	}


	public void setMemberNo(Integer memberNo) {
		this.memberNo = memberNo;
	}


	public Timestamp getActivityEnrollmentTime() {
		return activityEnrollmentTime;
	}


	public void setActivityEnrollmentTime(Timestamp activityEnrollmentTime) {
		this.activityEnrollmentTime = activityEnrollmentTime;
	}


	public Byte getActivityPaymentStat() {
		return activityPaymentStat;
	}


	public void setActivityPaymentStat(Byte activityPaymentStat) {
		this.activityPaymentStat = activityPaymentStat;
	}


	public Integer getMemberCouponNo() {
		return memberCouponNo;
	}


	public void setMemberCouponNo(Integer memberCouponNo) {
		this.memberCouponNo = memberCouponNo;
	}


	public Integer getNumberOfAttendee() {
		return numberOfAttendee;
	}


	public void setNumberOfAttendee(Integer numberOfAttendee) {
		this.numberOfAttendee = numberOfAttendee;
	}


	public String getCommentDetail() {
		return commentDetail;
	}


	public void setCommentDetail(String commentDetail) {
		this.commentDetail = commentDetail;
	}


	public Integer getActivityScore() {
		return activityScore;
	}


	public void setActivityScore(Integer activityScore) {
		this.activityScore = activityScore;
	}


	public MemberData getMemberData() {
		return memberData;
	}


	public void setMemberData(MemberData memberData) {
		this.memberData = memberData;
	}

	
	public Activity getActivity() {
		return activity;
	}


	public void setActivity(Activity activity) {
		this.activity = activity;
	}


	public List<ActivityAttendeeVO> getActivityAttendeeVO() {
		return activityAttendeeVO;
	}


	public void setActivityAttendeeVO(List<ActivityAttendeeVO> activityAttendeeVO) {
		this.activityAttendeeVO = activityAttendeeVO;
	}

	
	
}
