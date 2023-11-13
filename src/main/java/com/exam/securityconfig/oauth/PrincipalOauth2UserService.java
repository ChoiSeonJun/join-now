package com.exam.securityconfig.oauth;

import java.util.Map;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.exam.repository.UserRepository;
import com.exam.securityconfig.auth.PrincipalDetails;
import com.exam.securityconfig.oauth.provider.FaceBookUserInfo;
import com.exam.securityconfig.oauth.provider.GoogleUserInfo;
import com.exam.securityconfig.oauth.provider.NaverUserInfo;
import com.exam.securityconfig.oauth.provider.OAuth2UserInfo;
import com.exam.securitymodel.User;

//import com.cos.securityex01.config.auth.PrincipalDetails;
//import com.cos.securityex01.config.oauth.provider.FaceBookUserInfo;
//import com.cos.securityex01.config.oauth.provider.GoogleUserInfo;
//import com.cos.securityex01.config.oauth.provider.NaverUserInfo;
//import com.cos.securityex01.config.oauth.provider.OAuth2UserInfo;
//import com.cos.securityex01.model.User;
//import com.cos.securityex01.repository.UserRepository;

@Service
public class PrincipalOauth2UserService extends DefaultOAuth2UserService {

	@Autowired
    private UserRepository userRepository;

	// 후처리
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest); // google의 회원 프로필 조회
        
        // code를 통해 구성한 정보
        System.out.println("userRequest clientRegistration : " + userRequest.getClientRegistration());
        // token을 통해 응답받은 회원정보
        System.out.println("oAuth2User : " + oAuth2User);
    
        return processOAuth2User(userRequest, oAuth2User);
    }

    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {

        // Attribute를 파싱해서 공통 객체로 묶는다. 관리가 편함.
        OAuth2UserInfo oAuth2UserInfo = null;
        if (userRequest.getClientRegistration().getRegistrationId().equals("google")) {
            System.out.println("구글 로그인 요청~~");
            oAuth2UserInfo = new GoogleUserInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("facebook")) {
            System.out.println("페이스북 로그인 요청~~");
            oAuth2UserInfo = new FaceBookUserInfo(oAuth2User.getAttributes());
        } else if (userRequest.getClientRegistration().getRegistrationId().equals("naver")){
            System.out.println("네이버 로그인 요청~~");
            oAuth2UserInfo = new NaverUserInfo((Map)oAuth2User.getAttributes().get("response"));
        } else {
            System.out.println("우리는 구글과 페이스북과 네이버만 지원합니당");
        }

        Optional<User> userOptional = 
                userRepository.findByProviderAndProviderId(oAuth2UserInfo.getProvider(), oAuth2UserInfo.getProviderId());
        
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
            // user가 존재하면 update 해주기
            user.setEmail(oAuth2UserInfo.getEmail());
            userRepository.save(user);
        } else {
            // user의 패스워드가 null이기 때문에 OAuth 유저는 일반적인 로그인을 할 수 없음.
            String username = oAuth2UserInfo.getProvider() + "_" + oAuth2UserInfo.getProviderId();
            String email = oAuth2UserInfo.getEmail();
            String role = "ROLE_USER";
            String provider = oAuth2UserInfo.getProvider();
            String providerId = oAuth2UserInfo.getProviderId();
    
            user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setRole(role);
            user.setProvider(provider);
            user.setProviderId(providerId);
    
            userRepository.save(user);
        }

		return new PrincipalDetails(user, oAuth2User.getAttributes());
	}
}
