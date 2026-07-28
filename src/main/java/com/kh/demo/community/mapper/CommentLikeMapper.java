package com.kh.demo.community.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentLikeMapper {

    boolean existsCommentLike(@Param("commentId") Long commentId, @Param("memberId") String memberId);

    int insertCommentLike(@Param("commentId") Long commentId, @Param("memberId") String memberId);

    int deleteCommentLike(@Param("commentId") Long commentId, @Param("memberId") String memberId);
}
