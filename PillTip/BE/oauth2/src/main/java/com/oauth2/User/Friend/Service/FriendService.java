package com.oauth2.User.Friend.Service;

import com.oauth2.User.Auth.Entity.User;
import com.oauth2.User.Auth.Repository.UserRepository;
import com.oauth2.User.Auth.Service.TokenService;
import com.oauth2.User.Friend.Dto.FriendListDto;
import com.oauth2.User.Friend.Entity.Friend;
import com.oauth2.User.Friend.Repository.FriendRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService; // 초대 토큰 생성/검증용 (별도 secret 사용)

    /**
     * 친구 초대용 JWT 토큰 생성
     */
    public String generateInviteToken(Long inviterId) {
        return tokenService.createFriendInviteToken(inviterId, 86400); // 24시간
    }

    /**
     * 초대 수락 → 친구 관계 양방향 저장
     */
    public void acceptInvite(String token, Long receiverId) {
        Long inviterId = tokenService.getInviterIdFromFriendToken(token);
        // 친구 관계 저장 로직
        if (inviterId.equals(receiverId)) {
            throw new IllegalArgumentException("자기 자신을 친구로 추가할 수 없습니다.");
        }

        User inviter = userRepository.findById(inviterId)
                .orElseThrow(() -> new EntityNotFoundException("초대자 없음"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new EntityNotFoundException("수락자 없음"));

        // 2. 이미 친구인 경우 무시
        if (friendRepository.existsByUserIdAndFriendId(inviter.getId(), receiver.getId())) return;

        // 3. 양방향 저장
        addFriend(inviter, receiver);
    }

    /**
     * A ↔ B 친구 관계 저장
     */
    public void addFriend(User a, User b) {
        LocalDateTime now = LocalDateTime.now();
        friendRepository.save(new Friend(a, b, now));
        friendRepository.save(new Friend(b, a, now));
    }

    private List<User> getFriendsOf(Long userId) {
        return friendRepository.findAllByUserId(userId).stream()
                .map(Friend::getFriend)
                .collect(Collectors.toList());
    }

    public List<FriendListDto> getFriends(Long userId) {
        List<User> friends = getFriendsOf(userId);

        return friends.stream()
                .map(friend -> new FriendListDto(
                        friend.getId(), friend.getNickname()))
                .toList();
    }

    public void assertIsFriend(Long myId, Long friendId) throws AccessDeniedException {
        if (!friendRepository.existsByUserIdAndFriendId(myId, friendId)) {
            throw new AccessDeniedException("이 사용자는 당신의 친구가 아닙니다.");
        }
    }


}

