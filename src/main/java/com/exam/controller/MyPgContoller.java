package com.exam.controller;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.exam.dto.MyPgDTO;

import com.exam.dto.NotificationDTO;

import com.exam.dto.UploadDTO;

import com.exam.dto.UserInfoDTO;
import com.exam.navercloud.openapi.service.ObjectStorageService;
import com.exam.service.MyPgServiceImpl;
import com.exam.service.NotificationService;
import com.exam.service.UserService;
import com.exam.service.UserServiceImpl;

@Controller
public class MyPgContoller {

	@Autowired
	MyPgServiceImpl myService;

	@Autowired
	UserServiceImpl userService;
	
	@Autowired
	NotificationService notificationService;
	
	@Autowired
	ObjectStorageService storageService;


	// 로그인 후 mypage 요청하면 이메일만 출력
	@GetMapping("/mypage")
	public String mypage(Model m, HttpSession session) {
		System.out.println("mypage!!");

		// 세션에서 id값 받아오기
		UserInfoDTO userInfoDTO = (UserInfoDTO) session.getAttribute("loginInfo");
		int id = userInfoDTO.getId();
		// 현재 로그인된 id에 해당하는 정보 받아와서 출력
		UserInfoDTO info = userService.selectAllById(id);
		m.addAttribute("UserInfoDTO", info);
		
		// 알림 정보				
		List<NotificationDTO> notificationList = notificationService.selectListById(userInfoDTO.getId());
		
		int notificationNum=0;
		
		for(NotificationDTO dto : notificationList) {
			if(!dto.isRead()) {
				notificationNum++;
			}			
		}
		m.addAttribute("notificationNum", notificationNum);
		
		return "myPage";
	}

	// 마이페이지 수정 페이지 들어가기
	@GetMapping("/insertui")
	public String mypageInsert(Model m, HttpSession session) {

		// 세션에서 id값 받아오기
		UserInfoDTO userInfoDTO = (UserInfoDTO) session.getAttribute("loginInfo");
		int id = userInfoDTO.getId();
		UserInfoDTO info = userService.selectAllById(id);
		m.addAttribute("userInfoDTO", info);
		
		
		System.out.println(userInfoDTO);
		return "myPageAdd";
	}

	// mypage 수정
	@PostMapping("/myinsert")
	public String mypageInsert(MyPgDTO dto, HttpSession session) {
		System.out.println("mypage 내용추가!!");
		UserInfoDTO userInfoDTO = (UserInfoDTO) session.getAttribute("loginInfo");
		int n = userInfoDTO.getId();

		// dto에 사용자 id 설정
		dto.setId(n);
		int n2 = myService.mypageUpdate(dto); // insert -> Update로 변경
		
		

		return "redirect:mypage";
	}
	
	@PostMapping("/upload")
	public String upload(UploadDTO dto, Model m, HttpSession session) {
		
		//세션에서 id값 받아오기
		UserInfoDTO userInfoDTO = (UserInfoDTO) session.getAttribute("loginInfo");
		int id = userInfoDTO.getId();
		//현재 로그인된 id에 해당하는 정보 받아와서 출력
		UserInfoDTO info = userService.selectAllById(id);
		m.addAttribute("UserInfoDTO", info);		
		
		String theText = dto.getTheText();
		MultipartFile theFile = dto.getTheFile();
		
		//////////파일 없으면 생성////////////
		String folderPath = "C:\\upload";
        File folder = new File(folderPath);
        if (!folder.exists()) {
            boolean created = folder.mkdirs();
            if (created) {
                System.out.println("폴더가 생성되었습니다.");
            } else {
                System.out.println("폴더 생성에 실패했습니다.");
            }
        } else {
            System.out.println("이미 폴더가 존재합니다.");
        }
        //////////파일 없으면 생성/////////////
		
		
		//파일정보
		long size = theFile.getSize();
		String name = theFile.getName();
		String originalFilename = theFile.getOriginalFilename();
		String contentType = theFile.getContentType();
		
		
		System.out.println(theText);
		System.out.println(size);
		System.out.println(name);
		System.out.println(originalFilename);
		System.out.println(contentType);
		
		String username = userInfoDTO.getUsername()+".jpg";
		
		File f = new File("c:\\upload", username) ;
		
		try {
			theFile.transferTo(f);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		
		// 클라우드에 저장
		storageService.upload(username, "c:\\upload\\"+username);
		
		// 파일 삭제
		String filePath = "c:\\upload\\"+username;
        File file = new File(filePath);

        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("파일이 삭제되었습니다.");
            } else {
                System.out.println("파일 삭제에 실패했습니다.");
            }
        } else {
            System.out.println("파일이 존재하지 않습니다.");
        }
		
		return "mypage";
	}
	


}
