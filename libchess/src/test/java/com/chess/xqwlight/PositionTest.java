package com.chess.xqwlight;

import org.junit.Assert;
import org.junit.Before;

import static org.junit.Assert.*;

public class PositionTest {
    private Position pos;

    @Before
    public void init() {
        pos = new Position();
    }

    @org.junit.Test
    public void fromFen_toFen_handicap_none() {
        String fen = Position.STARTUP_FEN[0];
        pos.fromFen(fen);
        Assert.assertEquals(fen, pos.toFen());
    }

    @org.junit.Test
    public void fromFen_toFen_handicap_left_knight() {
        String fen = Position.STARTUP_FEN[1];
        pos.fromFen(fen);
        Assert.assertEquals(fen, pos.toFen());
    }


    @org.junit.Test
    public void fromFen_toFen_handicap_both_knight() {
        String fen = Position.STARTUP_FEN[2];
        pos.fromFen(fen);
        Assert.assertEquals(fen, pos.toFen());
    }


    @org.junit.Test
    public void fromFen_toFen_handicap_nine_pieces() {
        String fen = Position.STARTUP_FEN[3];
        pos.fromFen(fen);
        Assert.assertEquals(fen, pos.toFen());
    }

    @org.junit.Test
    public void fromFen_handicap_none() {
        pos.fromFen(Position.STARTUP_FEN[0]);
    }

    @org.junit.Test
    public void fromFen_handicap_left_knight() {
        pos.fromFen(Position.STARTUP_FEN[1]);
    }


    @org.junit.Test
    public void fromFen_handicap_both_knight() {
        pos.fromFen(Position.STARTUP_FEN[2]);
    }


    @org.junit.Test
    public void fromFen_handicap_nine_pieces() {
        pos.fromFen(Position.STARTUP_FEN[3]);
    }
}