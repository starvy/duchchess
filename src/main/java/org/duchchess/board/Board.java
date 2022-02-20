package org.duchchess.board;

import org.duchchess.*;
import org.duchchess.engine.Engine;
import org.duchchess.hash.Zobrist;
import org.duchchess.move.BitboardMoves;
import org.duchchess.move.Magic;
import org.duchchess.move.Move;
import org.duchchess.move.MoveType;

import java.util.Arrays;

import static org.duchchess.Color.BLACK;
import static org.duchchess.Color.WHITE;
import static org.duchchess.board.Piece.*;

public class Board {
    public long[][] piecesBB = new long[Color.SIZE][Piece.SIZE];

    public long allPieces = 0L;
    public long[] friendlyPieces = new long[Color.SIZE];

    public long emptyBB = Long.MAX_VALUE;

    public int[] squares = new int[64];

    public int[] kingSquare = new int[2];

    public int colorToMove = WHITE;
    public int oppositeColor = BLACK;

    public long pinnedBB, discoveredPiecesBB = 0L;
    public long checkingPiecesBB = 0;

    public int castlingRights = 0;
    public int epSquare = 0;
    public long zobristKey = 0L;

    public int moveCounter = 0;
    public int rule50 = 0;
    // history
    public int[] castlingRightsHistory = new int[ChessConstants.MAX_GAME_LENGTH];
    public int[] rule50History = new int[ChessConstants.MAX_GAME_LENGTH];
    public int[] epSquareHistory = new int[ChessConstants.MAX_GAME_LENGTH];
    public long[] zobristKeyHistory = new long[ChessConstants.MAX_GAME_LENGTH];

    public int pieceCount = 0;

    public void reset() {
        Arrays.fill(piecesBB[WHITE], 0);
        Arrays.fill(piecesBB[BLACK], 0);
        friendlyPieces[WHITE] = 0L;
        friendlyPieces[BLACK] = 0L;
        Arrays.fill(squares, 0);
        Arrays.fill(kingSquare, 0);
        allPieces = 0L;
        emptyBB = Long.MAX_VALUE;

        checkingPiecesBB = 0;
        pinnedBB = 0;
        discoveredPiecesBB = 0;

        castlingRights = 0;
        epSquare = 0;
        zobristKey = 0;

        Arrays.fill(castlingRightsHistory, 0);
        Arrays.fill(epSquareHistory, 0);
        Arrays.fill(zobristKeyHistory, 0);

        colorToMove = WHITE;
        oppositeColor = BLACK;

        moveCounter = 0;
        pieceCount = 0;
        rule50 = 0;
    }

    public void fromFen(String fen) {
        reset();
        String[] tokens = fen.split(" ");

        // Color
        if (tokens.length > 1) setColors(tokens[1].equals("w") ? 0 : 1);

        // Castling
        if (tokens.length > 2) {
            String[] chars = tokens[2].split("");
            for (String str : chars) {
                char ch = str.toCharArray()[0];
                if (ch == 'K') castlingRights |= Castling.WHITE_KING_SIDE;
                if (ch == 'Q') castlingRights |= Castling.WHITE_QUEEN_SIDE;
                if (ch == 'k') castlingRights |= Castling.BLACK_KING_SIDE;
                if (ch == 'q') castlingRights |= Castling.BLACK_QUEEN_SIDE;
            }
        }

        if (tokens.length > 3 && !tokens[3].equals("-")) {
            epSquare = Square.getSquareFromSAN(false, tokens[3]);
            System.out.println(epSquare);
        }


        String[] parts = tokens[0].split("/");

        int rank = 7;
        int square; // A8

        for (String part : parts) { // ranks
            String[] chars = part.split("");

            square = rank * 8 + 7;
            for (String str : chars) { // files
                char ch = str.toCharArray()[0];
                if (Character.isDigit(ch)) {
                    square -= Character.getNumericValue(ch);
                } else if (Character.isLetter(ch)) {
                    pieceCount++;

                    int color = Color.getColorFromChar(ch);
                    int piece = Piece.getPieceFromChar(ch);

                    if (piece == KING) {
                        kingSquare[color] = square;
                    }

                    long bb = Bitboard.getBB(square);

                    piecesBB[color][piece] |= bb;
                    squares[square] = piece;
                    emptyBB &= ~bb;
                    allPieces |= bb;
                    friendlyPieces[color] |= bb;
                    square--;
                }
            }
            rank--;
        }

        if (tokens.length > 5) {
            moveCounter = Integer.parseInt(tokens[5]);
        }

        updateZobrist();
        updatePinnedPieces();
    }

    void updateZobrist() {
        this.zobristKey = Zobrist.getKey(this);
    }

    public boolean hasNonPawnMaterial() {
        return (piecesBB[colorToMove][KING] | piecesBB[colorToMove][PAWN]) != friendlyPieces[colorToMove];
    }

    public boolean isInCheck() {
        return checkingPiecesBB != 0;
    }

    private boolean isLegalQuietMove(int move) {
        return !Check.isInCheck(kingSquare[colorToMove], colorToMove, piecesBB[oppositeColor],
                allPieces ^ Bitboard.getBB(Move.getFromSquare(move)) ^ Bitboard.getBB(Move.getToSquare(move)));
    }

    private boolean isKingMoveLegal(int move) {
        if (Move.getAttackedPiece(move) != 0) {
            return isKingAttackMoveLegal(move);
        }
        return !Check.isInCheckWithKing(Move.getToSquare(move), colorToMove, piecesBB[oppositeColor],
                allPieces ^ Bitboard.getBB(Move.getFromSquare(move)));
    }

    private boolean isKingAttackMoveLegal(int move) {
        int from = Move.getFromSquare(move);
        int to = Move.getToSquare(move);
        int attack = Move.getAttackedPiece(move);

        long[] tempPieces = new long[7];
        System.arraycopy(piecesBB[oppositeColor], 0, tempPieces, 0, 7);
        tempPieces[attack] &= ~Bitboard.getBB(to);
        return !Check.isInCheckWithKing(to, colorToMove, //board.friendlyPieces[board.colorToMove] & ~Bitboard.getBB(from) | Bitboard.getBB(to),
                tempPieces, ((allPieces & ~Bitboard.getBB(from)) & ~Bitboard.getBB(to)) | Bitboard.getBB(to));
    }

    /**
     * @return If move is pseudo-legal (no check checking)
     */
    public boolean isPseudoLegal(int move) {
        int fromSquare = Move.getFromSquare(move);
        int toSquare = Move.getToSquare(move);
        long fromBB = Bitboard.getBB(fromSquare);
        long toBB = Bitboard.getBB(toSquare);

        int attack = Move.getAttackedPiece(move);

        // check if fromBB matches
        if ((friendlyPieces[colorToMove] & fromBB) == 0) {
            return false;
        }

        if (attack == 0) {
            if (squares[toSquare] != 0) {
                return false;
            }
        } else {
            if ((piecesBB[oppositeColor][attack] & toBB) == 0 && !Move.isPassant(move)) {
                return false;
            }
        }

        if (Move.isPromotion(move) && squares[fromSquare] != PAWN) {
            return false;
        }

        switch (squares[fromSquare]) {
            case NONE:
                return false;
            case PAWN:
                if (Move.getAttackedPiece(move) == 0) {
                    long bb = BitboardMoves.pawnMove(colorToMove, fromBB);
                    bb |= BitboardMoves.doublePawnMove(colorToMove, fromSquare, bb);
                    return (toBB & bb) != 0L;
                }
                // is attack
                return (BitboardMoves.pawnAttacks(colorToMove, fromSquare) & toBB) != 0;

            case KNIGHT:
                return (BitboardMoves.knightMoves(fromSquare) & toBB) != 0;

            case BISHOP:
            case ROOK:
            case QUEEN:
                // sliding pieces
                if (attack != 0) {
                    return (Bitboard.BETWEEN_BB[fromSquare][toSquare] & allPieces) == 0;
                }
                return ((Bitboard.BETWEEN_BB[fromSquare][toSquare] | toBB) & allPieces) == 0;

            case KING:
                if (Move.getType(move) == MoveType.CASTLING) {
                    return Castling.isPseudoLegal(this, fromSquare, toSquare);
                }
                return (BitboardMoves.kingMoves(fromSquare) & toBB) != 0;

        }
        return true;
    }

    /**
     * @return If move is legal (playing side won't get in check)
     */
    public boolean isLegal(int move) {
        int fromSquare = Move.getFromSquare(move);
        long fromBB = Bitboard.getBB(fromSquare);
        int toSquare = Move.getToSquare(move);
        long toBB = Bitboard.getBB(toSquare);

        if (Move.isPassant(move)) {
            piecesBB[oppositeColor][PAWN] ^= Bitboard.getBB(epSquare + BitboardMoves.PAWN_UP[oppositeColor]);

            boolean isInCheck = Check.isInCheck(kingSquare[colorToMove], colorToMove, piecesBB[oppositeColor],
                    friendlyPieces[colorToMove] ^ Bitboard.getBB(fromSquare) ^ Bitboard.getBB(epSquare) |
                            friendlyPieces[oppositeColor] ^ Bitboard.getBB(epSquare + BitboardMoves.PAWN_UP[oppositeColor]));

            piecesBB[oppositeColor][PAWN] ^= Bitboard.getBB(epSquare + BitboardMoves.PAWN_UP[oppositeColor]);

            return !isInCheck;
        }
        if (squares[fromSquare] == KING) {
            if (Move.isCastling(move)) {
                return (Castling.isLegal(this, fromSquare, toSquare));
            }
            return isKingMoveLegal(move);
        }

        // Piece is pinned
        if ((fromBB & pinnedBB) != 0) {
            if ((Bitboard.PINNED_MOVE_MASK[fromSquare][kingSquare[colorToMove]] & toBB) == 0) {
                return false;
            }
        }

        if (checkingPiecesBB != 0) {
            if (squares[toSquare] == 0) {
                return isLegalQuietMove(move);
            } else {
                if (Long.bitCount(checkingPiecesBB) == 2) {
                    return false;
                }
                return (toBB & checkingPiecesBB) != 0;
            }
        }
        return true;
    }

    public void doNullMove() {
        pushHistoryValues();
        changeColorUpdateZobrist();
    }

    public void undoNullMove() {
        popHistoryValues();
        changeColor();
    }

    /**
     * Performs given move
     */
    public void doMove(int move) {
        int from = Move.getFromSquare(move);
        int to = Move.getToSquare(move);
        int piece = squares[from];
        int attack = Move.getAttackedPiece(move);

        pushHistoryValues();
        rule50++;

        if (epSquare != 0) {
            epSquare = 0;
            zobristKey ^= Zobrist.EP_SQUARE_HASHES[epSquare];
        }

        if (piece == ROOK) {
            updateCastlingRights(Castling.getCastlingRightsRookMoved(colorToMove, castlingRights, from));
        } else if (piece == KING) {
            kingSquare[colorToMove] = to;
            updateCastlingRights(Castling.getCastlingRightsKingMoved(colorToMove, castlingRights));

            // move rook
            if (Move.getType(move) == MoveType.CASTLING) {
                int rookFrom = Castling.getRookFromSquare(colorToMove, to);
                int rookTo = Castling.getRookToSquare(to);
                movePiece(ROOK, rookFrom, rookTo);
                zobristKey ^= Zobrist.PIECE_SQUARE_HASHES[colorToMove][piece][rookFrom] ^
                        Zobrist.PIECE_SQUARE_HASHES[colorToMove][piece][rookTo];
            }
        }


        movePiece(piece, from, to);
        zobristKey ^= Zobrist.PIECE_SQUARE_HASHES[colorToMove][piece][from] ^ Zobrist.PIECE_SQUARE_HASHES[colorToMove][piece][to];

        if (Move.isPassant(move)) {
            to -= BitboardMoves.PAWN_UP[colorToMove];
            squares[to] = NONE;
        }

        if (attack != 0) {
            // remove attacked piece
            this.piecesBB[oppositeColor][attack] ^= Bitboard.getBB(to);
            this.friendlyPieces[oppositeColor] ^= Bitboard.getBB(to);
            zobristKey ^= Zobrist.PIECE_SQUARE_HASHES[oppositeColor][attack][to];

            if (attack == ROOK) {
                updateCastlingRights(Castling.getCastlingRightsRookMoved(oppositeColor, castlingRights, to));
            }
            rule50 = 0;
        }
        if (Move.isPromotion(move)) {
            int promotedPiece = Move.getPromotedPiece(move);
            // Remove pawn
            piecesBB[colorToMove][PAWN] ^= Bitboard.getBB(to);
            squares[to] = NONE;
            zobristKey ^= Zobrist.PIECE_SQUARE_HASHES[colorToMove][PAWN][to];

            // Push promoted piece
            piecesBB[colorToMove][promotedPiece] |= Bitboard.getBB(to);
            squares[to] = promotedPiece;
            zobristKey ^= Zobrist.PIECE_SQUARE_HASHES[colorToMove][promotedPiece][to];
        }


        // Bitboards update
        this.allPieces = friendlyPieces[WHITE] | friendlyPieces[BLACK];
        emptyBB = ~allPieces;

        changeColor();
        zobristKey ^= Zobrist.COLOR_HASH;

        updatePinnedPieces();

        if (Engine.DEBUG) checkValues(move);
    }

    public void undoMove(int move) {
        popHistoryValues();

        changeColor();

        int from = Move.getFromSquare(move);
        int to = Move.getToSquare(move);
        int piece = squares[to];
        int attack = Move.getAttackedPiece(move);

        if (Move.isPromotion(move)) {
            int promotedPiece = Move.getPromotedPiece(move);
            removePiece(promotedPiece, to);
            pushPiece(PAWN, to);

            piece = PAWN;
        }

        squares[from] = piece;

        this.piecesBB[colorToMove][piece] |= Bitboard.getBB(from);
        this.piecesBB[colorToMove][piece] ^= Bitboard.getBB(to);

        this.friendlyPieces[colorToMove] |= Bitboard.getBB(from);
        this.friendlyPieces[colorToMove] ^= Bitboard.getBB(to);

        squares[to] = attack;

        if (attack != 0) {
            this.piecesBB[oppositeColor][attack] |= Bitboard.getBB(to);
            this.friendlyPieces[oppositeColor] |= Bitboard.getBB(to);

            pieceCount++;
        }

        if (piece == KING) {
            kingSquare[colorToMove] = from;
            if (Move.getType(move) == MoveType.CASTLING) {
                movePiece(ROOK, Castling.getRookToSquare(to), Castling.getRookFromSquare(colorToMove, to));
            }
        }

        this.allPieces = friendlyPieces[WHITE] | friendlyPieces[BLACK];
        emptyBB = ~allPieces;

        castlingRights = castlingRightsHistory[moveCounter];

        updatePinnedPieces();

        if (Engine.DEBUG) checkValues(move);
    }

    public boolean isRepetition() {
        int moveCountMin = Math.max(0, moveCounter - 50);
        for (int i = moveCounter - 2; i >= moveCountMin; i -= 2) {
            if (zobristKey == zobristKeyHistory[i]) {
                return true;
            }
        }
        return false;
    }

    public boolean isDraw() {
        if (rule50 > 100) {
            return true;
        }
        return isRepetition();
    }

    private void changeColor() {
        this.colorToMove ^= 1;
        this.oppositeColor ^= 1;
    }

    private void updateCastlingRights(int newCastlingRights) {
        zobristKey ^= Zobrist.CASTLING_RIGHTS_HASHES[castlingRights];
        this.castlingRights = newCastlingRights;
        zobristKey ^= Zobrist.CASTLING_RIGHTS_HASHES[castlingRights];
    }

    void changeColorUpdateZobrist() {
        changeColor();
        zobristKey ^= Zobrist.COLOR_HASH;
    }

    void movePieceUpdateZobrist(int piece, int from, int to) {
        movePiece(piece, from, to);
        zobristKey ^= Zobrist.PIECE_SQUARE_HASHES[colorToMove][piece][from] ^
                Zobrist.PIECE_SQUARE_HASHES[colorToMove][piece][to];
    }

    void movePiece(int piece, int from, int to) {
        removePiece(piece, from);
        pushPiece(piece, to);
    }

    void pushPiece(int piece, int square) {
        squares[square] = piece;
        this.piecesBB[colorToMove][piece] |= Bitboard.getBB(square);
        this.friendlyPieces[colorToMove] |= Bitboard.getBB(square);
    }


    void removePiece(int piece, int square) {
        squares[square] = NONE;
        this.piecesBB[colorToMove][piece] ^= Bitboard.getBB(square);
        this.friendlyPieces[colorToMove] ^= Bitboard.getBB(square);
    }

    public void setColors(int colorToMove) {
        this.colorToMove = colorToMove;
        this.oppositeColor = colorToMove ^ 1;
    }

    public void pushHistoryValues() {
        rule50History[moveCounter] = rule50;
        zobristKeyHistory[moveCounter] = zobristKey;
        castlingRightsHistory[moveCounter] = castlingRights;
        epSquareHistory[moveCounter] = epSquare;
        moveCounter++;
    }

    public void popHistoryValues() {
        moveCounter--;
        rule50 = rule50History[moveCounter];
        zobristKey = zobristKeyHistory[moveCounter];
        castlingRights = castlingRightsHistory[moveCounter];
        epSquare = epSquareHistory[moveCounter];
    }

    public void updatePinnedPieces() {
        pinnedBB = 0;
        discoveredPiecesBB = 0;
        checkingPiecesBB = piecesBB[oppositeColor][KNIGHT] & BitboardMoves.knightMoves(kingSquare[colorToMove])
                | piecesBB[oppositeColor][PAWN] & BitboardMoves.pawnAttacks(colorToMove, kingSquare[colorToMove]);

        for (int friendlyColor = WHITE; friendlyColor <= BLACK; friendlyColor++) {
            int enemyColor = 1 - friendlyColor;

            long enemyPieces = ((piecesBB[enemyColor][BISHOP] | piecesBB[enemyColor][QUEEN]) &
                    Magic.bishopMovesEmptyBoard[kingSquare[friendlyColor]]) |
                    ((piecesBB[enemyColor][ROOK] | piecesBB[enemyColor][QUEEN]) &
                            Magic.rookMovesEmptyBoard[kingSquare[friendlyColor]]);

            while (enemyPieces != 0) {
                int square = Long.numberOfTrailingZeros(enemyPieces);

                long checkedPiece = Bitboard.BETWEEN_BB[kingSquare[friendlyColor]][square] & allPieces;
                if (checkedPiece == 0) {
                    checkingPiecesBB |= Long.lowestOneBit(enemyPieces);
                } else if (Long.bitCount(checkedPiece) == 1) {
                    pinnedBB |= checkedPiece & friendlyPieces[friendlyColor];
                    discoveredPiecesBB |= checkedPiece & friendlyPieces[enemyColor];
                }
                enemyPieces &= enemyPieces - 1;
            }
        }
    }

    /**
     * Used for debugging
     */
    public void checkValues(int move) {
        Util.isTrue((friendlyPieces[WHITE] | friendlyPieces[BLACK]) == allPieces, "friendly pieces");
        Util.isTrue(emptyBB == ~allPieces, "emptyBB");
        Util.isTrue(colorToMove != oppositeColor, "color");

        long tmpPieces = 0;
        for (int piece = 1; piece < Piece.SIZE; piece++) {
            long tmpBB = piecesBB[WHITE][piece];
            tmpPieces |= tmpBB;
            while (tmpBB != 0) {
                int square = Long.numberOfTrailingZeros(tmpBB);
                Util.isTrue(this, squares[square] == piece, Move.makeString(move) + " w piece not matching sq piece" + squares[square] + " p " + piece);
                tmpBB &= tmpBB - 1;
            }
            tmpBB = piecesBB[BLACK][piece];
            tmpPieces |= tmpBB;
            while (tmpBB != 0) {
                int square = Long.numberOfTrailingZeros(tmpBB);
                Util.isTrue(this, squares[square] == piece, Move.makeString(move) + " b piece not matching sq piece" + squares[square] + " p " + piece);
                tmpBB &= tmpBB - 1;
            }
        }
        Util.isTrue(this, tmpPieces == allPieces, "piecesBB not matching allPieces");
    }

    public void print() {
        System.out.print(this);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            for (int j = 7; j >= 0; j--) {
                int square = i * 8 + j;
                if ((Bitboard.getBB(square) & friendlyPieces[WHITE]) != 0) {
                    builder.append("| ").append(Piece.FEN_CHARACTERS[squares[square]]).append(" ");
                } else {
                    builder.append("| ").append(Character.toLowerCase(Piece.FEN_CHARACTERS[squares[square]])).append(" ");
                }
            }
            builder.append("|\n");
            builder.append("+---+---+---+---+---+---+---+---+\n");
        }
        builder.append("Color to move: ").append(colorToMove).append(" castling ").append(Integer.toBinaryString(castlingRights)).append("\n");
        builder.append("Key: ").append(Long.toHexString(zobristKey).toUpperCase()).append("\n");

        return builder.toString();
    }

    public void drawPiecesBB() {
        for (int i = 1; i < Piece.SIZE; i++) {
            System.out.println("piece " + i + ":");
            Bitboard.draw(piecesBB[colorToMove][i]);
        }
    }
}
