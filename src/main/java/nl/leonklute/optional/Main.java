import nl.leonklute.optional.Optional;
import nl.leonklute.optional.Optional.Empty;
import nl.leonklute.optional.Optional.Present;

void main(){
    ChessBoard board=new ChessBoard();
    Optional<ChessMove> possibleMove;
    do{
        possibleMove = board.bestMove();
        if(possibleMove instanceof Present(var move)){
            System.out.println(move);
        }
    }while(possibleMove.isPresent());
    System.out.println("Game over");
}

record ChessMove(ChessPiece piece, char fromColumn, int fromRow, char toRow, int toColumn) {
}

static class ChessBoard {
    private Optional<ChessPiece>[][] board;
    private Color turn = Color.WHITE;
    private boolean checkMate = false;

    public Optional<ChessMove> bestMove() {
        checkMate = Math.random() > .9;
        if(checkMate){
            return Optional.empty();
        }
        turn = turn.switchTurn();
        return Optional.of(new ChessMove(new ChessPiece(PieceType.PAWN, turn), 'e', 2, 'e', 4));
    }
}

record ChessPiece(PieceType type, Color color) {
}

enum PieceType {
    PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING
}

enum Color {
    BLACK, WHITE;
    public Color switchTurn() {
        return switch (this) {
            case WHITE -> BLACK;
            case BLACK -> WHITE;
        };
    }
}