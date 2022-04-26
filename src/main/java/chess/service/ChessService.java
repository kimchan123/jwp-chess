package chess.service;

import static chess.domain.game.ChessGame.*;

import chess.dto.MoveRequestDto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import chess.dao.BoardDao;
import chess.dao.GameDao;
import chess.domain.board.Board;
import chess.domain.board.coordinate.Coordinate;
import chess.domain.game.ChessGame;
import chess.domain.game.StatusCalculator;
import chess.domain.piece.Piece;
import chess.domain.piece.Team;
import chess.dto.BoardDto;
import chess.dto.GameDto;

@Service
public class ChessService {

    private static final int NOT_EXIST_GAME = 0;

    private final BoardDao boardDao;
    private final GameDao gameDao;

    public ChessService(BoardDao boardDao, GameDao gameDao) {
        this.boardDao = boardDao;
        this.gameDao = gameDao;
    }

    public Board findBoardByGameId(int gameId) {
        Map<Coordinate, Piece> board = new HashMap<>();
        List<BoardDto> boardDtos = boardDao.findByGameId(gameId);
        for (BoardDto boardDto : boardDtos) {
            board.put(Coordinate.of(boardDto.getPosition()), Piece.of(boardDto.getSymbol(), boardDto.getTeam()));
        }
        return new Board(board);
    }

    public void saveBoard(Board board, int gameId) {
        List<BoardDto> boardDtos = new ArrayList<>();
        Map<String, Piece> pieces = board.toMap();

        for (String key : pieces.keySet()) {
            Piece piece = pieces.get(key);
            boardDtos.add(new BoardDto(piece.getSymbol(), piece.getTeam(), key));
        }

        boardDao.save(boardDtos, gameId);
    }

    private void updateBoard(Piece piece, String position, int gameId) {
        BoardDto boardDto = new BoardDto(piece.getSymbol(), piece.getTeam(), position);

        boardDao.update(boardDto, gameId);
    }

    public void saveGame(String whiteUserName, String blackUserName, String state) {
        GameDto gameDto = new GameDto(whiteUserName, blackUserName, state);
        gameDao.save(gameDto);
    }

    public void start(String whiteUserName, String blackUserName) {
        int gameId = findGameIdByUserName(whiteUserName, blackUserName);
        if (isNotExistGame(gameId)) {
            saveGame(whiteUserName, blackUserName, Team.WHITE.name());
            gameId = findGameIdByUserName(whiteUserName, blackUserName);
            saveBoard(Board.create(), gameId);
            return;
        }
    }

    public void move(int gameId, MoveRequestDto moveRequestDto) {
        Board board = findBoardByGameId(gameId);
        GameDto gameDto = gameDao.findById(gameId);

        ChessGame chessGame = new ChessGame(board, gameDto.getState());
        String from = moveRequestDto.getFrom();
        String to = moveRequestDto.getTo();
        chessGame.move(Coordinate.of(from), Coordinate.of(to));
        changeState(gameId, gameDto, chessGame);

        updateBoard(board.findPiece(Coordinate.of(from)), from, gameId);
        updateBoard(board.findPiece(Coordinate.of(to)), to, gameId);
    }

    private void changeState(int gameId, GameDto gameDto, ChessGame chessGame) {
        if (chessGame.isFinished()) {
            endGame(gameId);
            return;
        }

        changeTurn(gameDto.getState(), gameId);
    }

    private void changeTurn(String state, int gameId) {
        if (state.equals(Team.WHITE.name())) {
            gameDao.update(Team.BLACK.name(), gameId);
            return;
        }
        gameDao.update(Team.WHITE.name(), gameId);
    }

    private void endGame(int gameId) {
        gameDao.update(END_STATE, gameId);
    }

    public int findGameIdByUserName(String whiteUserName, String blackUserName) {
        return gameDao.findGameIdByUserName(whiteUserName, blackUserName);
    }

    private boolean isNotExistGame(int gameId) {
        return gameId == NOT_EXIST_GAME;
    }

    public StatusCalculator createStatus(int gameId) {
        Board board = findBoardByGameId(gameId);
        GameDto gameDto = gameDao.findById(gameId);

        ChessGame chessGame = new ChessGame(board, gameDto.getState());
        return chessGame.status();
    }

    public void deleteGameByGameId(int gameId) {
        gameDao.deleteById(gameId);
    }
}
