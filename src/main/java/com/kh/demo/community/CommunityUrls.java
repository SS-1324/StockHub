package com.kh.demo.community;

// 게시판 URL 경로의 유일한 출처. 컨트롤러들의 @RequestMapping과 WebConfig의 인터셉터 패턴이
// 전부 이 상수를 참조하므로, 경로를 바꿀 때 여기 한 곳만 고치면 인터셉터 보호가 어긋나는 사고를 막을 수 있다.
public final class CommunityUrls {

    public static final String BASE = "/community";
    public static final String LIKE = BASE + "/like";
    public static final String BOOKMARK = BASE + "/bookmark";
    public static final String COMMENT_LIKE = BASE + "/comment/like";

    public static final String WRITE = BASE + "/write";
    public static final String EDIT_ANY = BASE + "/edit/**";
    public static final String DELETE_ANY = BASE + "/delete/**";
    public static final String LIKE_ANY = LIKE + "/**";
    public static final String BOOKMARK_ANY = BOOKMARK + "/**";
    public static final String COMMENT_LIKE_ANY = COMMENT_LIKE + "/**";
    public static final String COMMENT_ANY = BASE + "/*/comment/**";

    private CommunityUrls() {}
}
