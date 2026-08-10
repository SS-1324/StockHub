package com.kh.demo.admin.mapper;

import com.kh.demo.admin.dto.AdminDashboardDto;
import com.kh.demo.admin.dto.AdminLogDto;
import com.kh.demo.community.dto.BoardCommentDto;
import com.kh.demo.community.dto.BoardDto;
import com.kh.demo.dictionary.dto.GlossaryDto;
import com.kh.demo.member.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

// 관리자 화면 전용 조회와 상태 변경 SQL을 연결
@Mapper
public interface AdminMapper {

    AdminDashboardDto selectDashboard();

    List<MemberDto> selectMembers();

    MemberDto selectMember(@Param("memberId") String memberId);

    int updateMemberStatus(@Param("memberId") String memberId,
                           @Param("memberStatus") String memberStatus);

    int updateMemberRole(@Param("memberId") String memberId,
                         @Param("memberRole") String memberRole);

    List<BoardDto> selectBoards();

    int updateBoardHidden(@Param("boardId") Long boardId,
                          @Param("hidden") boolean hidden);

    List<BoardCommentDto> selectComments();

    int updateCommentHidden(@Param("commentId") Long commentId,
                            @Param("hidden") boolean hidden);

    int completeInquiry(@Param("inquiryId") Long inquiryId,
                        @Param("adminId") String adminId);

    List<GlossaryDto> selectGlossaryTerms();

    int countGlossaryTermExceptCurrent(@Param("term") String term,
                                       @Param("termId") Long termId);

    int insertGlossary(GlossaryDto glossaryDto);

    int updateGlossary(GlossaryDto glossaryDto);

    int deleteGlossary(@Param("termId") Long termId);

    List<AdminLogDto> selectAdminLogs();

    int insertAdminLog(AdminLogDto adminLogDto);
}
