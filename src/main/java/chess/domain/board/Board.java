package chess.domain.board;

import chess.exception.NotMovableException;
import chess.exception.PieceNotFoundException;
import java.util.Map;
import java.util.stream.Collectors;

import chess.domain.board.coordinate.Coordinate;
import chess.domain.piece.Piece;
import chess.domain.piece.Symbol;
import chess.domain.piece.Team;

public class Board {
    public static final int BOTH_KING_ALIVE = 2;

    private final Map<Coordinate, Piece> value;

    public Board(Map<Coordinate, Piece> value) {
        this.value = value;
    }

    public static Board create() {
        return new Board(InitialBoard.initialize());
    }

    public Board move(String from, String to) {
        return move(Coordinate.of(from), Coordinate.of(to));
    }

    public Board move(Coordinate from, Coordinate to) {
        Piece piece = findPiece(from);
        if (piece.isEmpty()) {
            throw new PieceNotFoundException();
        }
        if (!piece.isMovable(this, from, to)) {
            throw new NotMovableException();
        }

        swapPiece(from, to);
        return this;
    }

    public Piece findPiece(Coordinate coordinate) {
        return value.get(coordinate);
    }

    private void swapPiece(Coordinate from, Coordinate to) {
        Piece piece = findPiece(from);
        value.put(from, Piece.of(Symbol.EMPTY.name(), Team.NONE.name()));
        value.put(to, piece);
    }

    public boolean isBothKingAlive() {
        return value.values()
                .stream()
                .filter(Piece::isKing)
                .count() == BOTH_KING_ALIVE;
    }

    public Map<Coordinate, Piece> getValue() {
        return value;
    }

    public Map<String, Piece> toMap() {
        return value.entrySet()
                .stream()
                .collect(Collectors.toMap(m -> m.getKey().toString(), Map.Entry::getValue));
    }
}
