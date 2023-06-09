package com.jamigo.member.actitvityCollection.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jamigo.activity.activity_approve.service.ActivityService;
import com.jamigo.member.actitvityCollection.entity.ActivityCollectionDTO;
import com.jamigo.member.actitvityCollection.entity.ActivityCollectionEntity;
import com.jamigo.member.actitvityCollection.entity.ActivityCollectionService;

@RestController
@RequestMapping("/activityCollection")
public class ActivityCollectionController {
	
	@Autowired
	ActivityCollectionService service;
	
	@Autowired
	ActivityService activityService;

	private ActivityCollectionEntity activityAdd;
	
	@GetMapping("/getAll")
	public List<ActivityCollectionEntity> getAll() {
		return service.getAll();
	}

	@GetMapping("/getByMemberNo/{memberNo}")
	public List<ActivityCollectionDTO> getByMemberNo(@PathVariable Integer memberNo) {
		List<ActivityCollectionEntity> activityCollectionList = service.getByMemberNo(memberNo);
		List<ActivityCollectionDTO> dtoList = new ArrayList<>();
		for(ActivityCollectionEntity entity : activityCollectionList) {
			ActivityCollectionDTO dto = new ActivityCollectionDTO();
			dto.setActivityCollectionEntity(entity);
			dto.setActivity(activityService.getActDetail(entity.getActivityNo()));
			dtoList.add(dto);
		}
		return dtoList;
	}

	@GetMapping("/isActivityAdd/{memberNo}/{activityNo}")
	public ResponseEntity<String> isActivityAdd(@PathVariable Integer activityNo, @PathVariable Integer memberNo) {
		activityAdd = service.isActivityAdd(activityNo, memberNo);
		if(activityAdd != null) {
			return ResponseEntity.ok("已收藏");
		}else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("未收藏");
		}
	}
	
	@PostMapping("/deleteByEntity")
	public ResponseEntity<?> deleteByEntity(@RequestBody ActivityCollectionEntity entity) {
		service.delete(entity);
		return ResponseEntity.ok("刪除成功");
	}
	
	@PostMapping("/add")
	public ResponseEntity<?> add(@RequestBody ActivityCollectionEntity entity) {
		service.add(entity);
		return ResponseEntity.ok("新增成功");
	}
}
