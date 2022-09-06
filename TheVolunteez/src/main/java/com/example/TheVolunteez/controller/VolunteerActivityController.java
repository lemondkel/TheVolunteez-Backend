package com.example.TheVolunteez.controller;

import com.example.TheVolunteez.dto.PostVolunteerDto;
import com.example.TheVolunteez.entity.Member;
import com.example.TheVolunteez.entity.VolunteerActivity;
import com.example.TheVolunteez.repository.MemberRepository;
import com.example.TheVolunteez.repository.VolunteerActivityRepository;
import com.example.TheVolunteez.service.VolunteerActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class VolunteerActivityController {

    private final VolunteerActivityService volunteerActivityService;
    private final VolunteerActivityRepository volunteerActivityRepository;

    private final MemberRepository memberRepository;

    @PostConstruct // 게시글 검색 기능 or 페이징 기능 테스트용 데이터
    public void init() {
        for (int i = 0; i < 100; i++) {
            PostVolunteerDto postVolunteerDto = new PostVolunteerDto("acg6138" + i, "제목" + i, "설명" + i, new Date(2022,9,4), new Date(2022,9,4), new Date(2022,9,i), "장소" + i,
                    i,i,i, "연락처" + i, new ArrayList<>());
            volunteerActivityRepository.save(new VolunteerActivity(postVolunteerDto, "writerId" + 1, new ArrayList<>()));
        }
    }

    @GetMapping("/board/list") // 봉사활동 전체 게시글
    public Page<PostVolunteerDto> getAllVolunteers(@PageableDefault(size = 15, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                   String search) {
        if(search == null) { // 검색한게 없을 때
            return volunteerActivityService.findAllVolunteers(pageable);
        }else{ // 검색 키워드가 있을 때
            return volunteerActivityService.findSearchVolunteers(search, pageable);
        }
    }

    @GetMapping("/board/list/zone")
    public Page<PostVolunteerDto> getSearchZoneVolunteers(@PageableDefault(size = 15, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                          String search, Authentication authentication){
        UserDetails details = (UserDetails) authentication.getPrincipal();
        Optional<Member> member = memberRepository.findByUserId(details.getUsername());

        if(search == null){
            if(member.isEmpty()) {
                return volunteerActivityService.findAllVolunteers(pageable);
            }else{
                String targetZone = member.get().getAddress();
                return volunteerActivityService.findSearchZoneVolunteers(targetZone, pageable);
            }
        }else{
            return volunteerActivityService.findSearchZoneVolunteers(search, pageable);
        }
    }

    @GetMapping("/board/list/short-term")
    public Page<PostVolunteerDto> getShortTermVolunteer(@PageableDefault(size = 15, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                   String search) {
        if(search == null) { // 검색한게 없을 때
            return volunteerActivityService.findShortTermVolunteers(pageable);
        }else{ // 검색 키워드가 있을 때
            return volunteerActivityService.findShortTermBySearch(search, pageable);
        }
    }

    @GetMapping("/board/list/long-term")
    public Page<PostVolunteerDto> getLongTermVolunteer(@PageableDefault(size = 15, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
                                                     String search) {
        if(search == null) { // 검색한게 없을 때
            return volunteerActivityService.findLongTermVolunteers(pageable);
        }else{ // 검색 키워드가 있을 때
            return volunteerActivityService.findLongTermBySearch(search, pageable);
        }
    }

    @PostMapping("/board/post") // 봉사활동 게시글 올리기
    public PostVolunteerDto postVolunteer(Authentication authentication, @Valid @RequestBody PostVolunteerDto postVolunteerDto) {

        return volunteerActivityService.post(authentication, postVolunteerDto);
    }

    @GetMapping("/board/{id}") // 봉사활동 게시글 페이지
    public PostVolunteerDto findVolunteer(@PathVariable("id") Long vid) {
        return volunteerActivityService.getVolunteerDto(vid);
    }

    @PostMapping("/board/{id}/apply") // 봉사활동 참여하기 버튼
    public String volunteerApply(Authentication authentication, @PathVariable("id") Long vid) {
        return volunteerActivityService.volunteerApply(authentication, vid);
    }

    @PostMapping("/board/{id}/cancel") // 봉사활동 취소
    public String volunteerCancel(Authentication authentication, @PathVariable("id") Long vid) {
        return volunteerActivityService.cancelApply(authentication, vid);
    }

    @GetMapping("/board/{id}/members") // 봉사활동 게시글에 참여한 멤버 확인
    public List<String> volunteerMemberList(@PathVariable("id") Long vid) {
        return volunteerActivityService.volunteerMemberNameList(vid);
    }

    @PostMapping("/board/{id}/like") // 좋아요 버튼
    public String likeVolunteer(@PathVariable("id") Long vid, Authentication authentication) {
        return volunteerActivityService.likeVolunteer(authentication, vid);
    }

    @GetMapping("/board/{id}/edit") // 게시글 수정 페이지
    public PostVolunteerDto getEditDetail(@PathVariable("id") Long vid) {
        return volunteerActivityService.getVolunteerDto(vid);
    }

    @PatchMapping("/board/{id}/edit") // 게시글 수정
    public String editVolunteerActivity(Authentication authentication, @Valid @RequestBody PostVolunteerDto postVolunteerDto,
                                        @PathVariable("id") Long vid) throws IllegalAccessException {
        volunteerActivityService.editVolunteerActivity(authentication, postVolunteerDto, vid);
        return "수정 성공";
    }

    @DeleteMapping("/board/{id}/delete") //봉사 게시글 삭제
    public String volunteerDelete(@PathVariable("id") Long vid, Authentication authentication){
        return volunteerActivityService.volunteerDelete(authentication, vid);
    }
}
