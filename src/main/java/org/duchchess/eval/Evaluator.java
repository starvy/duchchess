package org.duchchess.eval;

import org.duchchess.Color;
import org.duchchess.board.Board;
import org.duchchess.move.BitboardMoves;

import static org.duchchess.Color.BLACK;
import static org.duchchess.Color.WHITE;
import static org.duchchess.board.Piece.*;
import static org.duchchess.eval.EvalConstants.MATERIAL_VALUES;

public class Evaluator {
    public static int getScore(final Board board) {
        int score = 0;
        score += calculateMaterials(board) +
                calculatePositions(board) +
                calculateBonuses(board) +
                calculatePenalties(board) +
                calculateMobility(board) +
                calculatePawnScores(board);
        return score;
    }

    public static int calculateMaterials(final Board board) {
        return
                ((Long.bitCount(board.piecesBB[WHITE][PAWN]) - Long.bitCount(board.piecesBB[BLACK][PAWN])) * MATERIAL_VALUES[PAWN] +
                        (Long.bitCount(board.piecesBB[WHITE][KNIGHT]) - Long.bitCount(board.piecesBB[BLACK][KNIGHT])) * MATERIAL_VALUES[KNIGHT] +
                        (Long.bitCount(board.piecesBB[WHITE][BISHOP]) - Long.bitCount(board.piecesBB[BLACK][BISHOP])) * MATERIAL_VALUES[BISHOP] +
                        (Long.bitCount(board.piecesBB[WHITE][ROOK]) - Long.bitCount(board.piecesBB[BLACK][ROOK])) * MATERIAL_VALUES[ROOK] +
                        (Long.bitCount(board.piecesBB[WHITE][QUEEN]) - Long.bitCount(board.piecesBB[BLACK][QUEEN])) * MATERIAL_VALUES[QUEEN]);
    }

    public static int calculatePositions(final Board board) {
        int score = 0;

        for (int piece = PAWN; piece < EvalConstants.POSITION.length; piece++) {
            long pieceBB = board.piecesBB[WHITE][piece];
            while (pieceBB != 0) {
                int square = Long.numberOfTrailingZeros(pieceBB);
                score += EvalConstants.POSITION[piece][WHITE][square];
                pieceBB &= pieceBB - 1;
            }
            pieceBB = board.piecesBB[BLACK][piece];
            while (pieceBB != 0) {
                int square = Long.numberOfTrailingZeros(pieceBB);
                score -= EvalConstants.POSITION[piece][BLACK][square];
                pieceBB &= pieceBB - 1;
            }
        }

        return (int) (score * 0.4);
    }

    public static int calculateMobility(Board board) {
        int score = 0;
        for (int color = WHITE; color <= BLACK; color++) {
            for (int piece = KNIGHT; piece <= QUEEN; piece++) {
                long pieceBB = board.piecesBB[color][piece];
                while (pieceBB != 0) {
                    int square = Long.numberOfTrailingZeros(pieceBB);
                    score += EvalConstants.MOBILITY[piece][Long.bitCount(BitboardMoves.moves(square, piece, board.allPieces))] * Color.FACTOR[color];
                    pieceBB &= pieceBB - 1;
                }
            }
        }
        return score;
    }

    public static int calculatePawnScores(Board board) {
        int score = 0;

        for (int i = 0; i < 8; i++) {
            if (Long.bitCount(board.piecesBB[WHITE][PAWN] & EvalConstants.FILE_MASKS[i]) > 1) {
                score -= 10;
            }
            if (Long.bitCount(board.piecesBB[WHITE][PAWN] & EvalConstants.FILE_MASKS[i]) > 1) {
                score += 10;
            }
        }

        long pawns;

        // white
        pawns = board.piecesBB[WHITE][PAWN];
        while (pawns != 0) {
            // isolated pawns
            if ((EvalConstants.ADJACENT_FILE_MASKS[Long.numberOfTrailingZeros(pawns) & 7] & board.piecesBB[WHITE][PAWN]) == 0) {
                score -= 15;
            }
            pawns &= pawns - 1;
        }

        // black
        pawns = board.piecesBB[BLACK][PAWN];
        while (pawns != 0) {
            // isolated pawns
            if ((EvalConstants.ADJACENT_FILE_MASKS[Long.numberOfTrailingZeros(pawns) & 7] & board.piecesBB[BLACK][PAWN]) == 0) {
                score += 15;
            }
            pawns &= pawns - 1;
        }
        return score;

    }

    public static int calculatePenalties(Board board) {
        int score = 0;

        // Rook on open file
        if ((board.piecesBB[WHITE][ROOK] & 0xc0c0) != 0 && (board.piecesBB[WHITE][KING] & 0x60) != 0) {
            score -= 40;
        } else if ((board.piecesBB[WHITE][ROOK] & 0x303) != 0 && (board.piecesBB[WHITE][KING] & 0x6) != 0) {
            score -= 40;
        } else if ((board.piecesBB[WHITE][ROOK] & 0xc0c0000000000000L) != 0 && (board.piecesBB[WHITE][KING] & 0x6000000000000000L) != 0) {
            score += 40;
        } else if ((board.piecesBB[WHITE][ROOK] & 0x303000000000000L) != 0 && (board.piecesBB[WHITE][KING] & 0x600000000000000L) != 0) {
            score += 40;
        }

        return score;
    }

    public static int calculateBonuses(Board board) {
        int score = 0;
        long piece;

        piece = board.piecesBB[WHITE][ROOK];
        while (piece != 0) {
            if ((board.piecesBB[WHITE][PAWN] & EvalConstants.FILE_MASKS[Long.numberOfTrailingZeros(piece) % 8]) == 0) {
                if ((board.piecesBB[BLACK][PAWN] & EvalConstants.FILE_MASKS[Long.numberOfTrailingZeros(piece) % 8]) == 0) {
                    score += 20;
                } else {
                    score += 15;
                }
            }
            piece &= piece - 1;
        }

        piece = board.piecesBB[BLACK][ROOK];
        while (piece != 0) {
            if ((board.piecesBB[BLACK][PAWN] & EvalConstants.FILE_MASKS[Long.numberOfTrailingZeros(piece) % 8]) == 0) {
                if ((board.piecesBB[WHITE][PAWN] & EvalConstants.FILE_MASKS[Long.numberOfTrailingZeros(piece) % 8]) == 0) {
                    score -= 20;
                } else {
                    score -= 15;
                }
            }
            piece &= piece - 1;
        }

        score += getBishopPairBonus(board);

        return score;

    }

    private static int getBishopPairBonus(Board board) {
        if (Long.bitCount(board.piecesBB[WHITE][BISHOP]) == 2) {
            if (!(Long.bitCount(board.piecesBB[BLACK][BISHOP]) == 2)) {
                return 30;
            }
        }
        return 0;
    }
}
