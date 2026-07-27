package com.kh.demo.ranking.controller;

<<<<<<< HEAD
import com.kh.demo.ranking.dto.RankingDto;
import com.kh.demo.ranking.service.RankingService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/ranking")
public class RankingController {

    private final RankingService rankingService;

    public RankingController(RankingService rankingService) {
        this.rankingService = rankingService;
    }

    @GetMapping
    public List<RankingDto> getRanking() {
        return rankingService.getRankingBoard();
    }
}
=======
public class RankingController {
}
>>>>>>> origin
