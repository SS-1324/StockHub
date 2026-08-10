package com.kh.demo.member.service;

import com.kh.demo.member.mapper.FollowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FollowServiceImplTest {

    @Mock
    private FollowMapper followMapper;

    private FollowServiceImpl followService;

    @BeforeEach
    void setUp() {
        followService = new FollowServiceImpl(followMapper);
    }

    @Test
    void 관계가없으면팔로우를추가한다() {
        when(followMapper.existsFollow("viewer", "target")).thenReturn(false);

        boolean following = followService.toggleFollow("viewer", "target");

        assertThat(following).isTrue();
        verify(followMapper).insertFollow("viewer", "target");
        verify(followMapper, never()).deleteFollow("viewer", "target");
    }

    @Test
    void 이미팔로우중이면관계를삭제한다() {
        when(followMapper.existsFollow("viewer", "target")).thenReturn(true);

        boolean following = followService.toggleFollow("viewer", "target");

        assertThat(following).isFalse();
        verify(followMapper).deleteFollow("viewer", "target");
        verify(followMapper, never()).insertFollow("viewer", "target");
    }
}
