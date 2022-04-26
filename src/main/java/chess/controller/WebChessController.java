package chess.controller;

import chess.converter.Converter;
import chess.domain.board.Board;
import chess.domain.game.StatusCalculator;
import chess.dto.ExceptionDto;
import chess.dto.MoveRequestDto;
import chess.service.ChessService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class WebChessController {

    private final ChessService chessService;

    public WebChessController(ChessService chessService) {
        this.chessService = chessService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/game/{id}")
    public ModelAndView game(@PathVariable("id") int id) {
        Board board = chessService.findBoardByGameId(id);

        Map<String, Object> map = Converter.toMap(id, board);
        ModelAndView modelAndView = new ModelAndView("start");
        modelAndView.addObject("board", map);
        return modelAndView;
    }

    @PostMapping("/start")
    public String start(
            @RequestParam("white_user_name") String whiteUserName,
            @RequestParam("black_user_name") String blackUserName) {
        chessService.start(whiteUserName, blackUserName);
        int gameId = chessService.findGameIdByUserName(whiteUserName, blackUserName);
        return "redirect:/game/" + gameId;
    }

    @PostMapping("/game/{gameId}/move")
    @ResponseBody
    public ResponseEntity<MoveRequestDto> move(
            @PathVariable("gameId") int gameId, @RequestBody MoveRequestDto moveRequestDto) {
        chessService.move(gameId, moveRequestDto);
        return ResponseEntity.ok(moveRequestDto);
    }

    @PostMapping("/stop")
    public String stop() {
        return "redirect:/";
    }

    @PostMapping("/game/{gameId}/end")
    public String end(@PathVariable("gameId") int gameId) {
        chessService.deleteGameByGameId(gameId);
        return "redirect:/";
    }

    @GetMapping("/game/{gameId}/status")
    public ModelAndView status(@PathVariable("gameId") int gameId) {
        StatusCalculator status = chessService.createStatus(gameId);
        Board board = chessService.findBoardByGameId(gameId);

        Map<String, Object> map = Converter.toMap(gameId, board, status);
        ModelAndView modelAndView = new ModelAndView("start");
        modelAndView.addObject("board", map);
        return modelAndView;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<ExceptionDto> handle(RuntimeException e) {
        return ResponseEntity.badRequest().body(new ExceptionDto(e.getMessage()));
    }
}
