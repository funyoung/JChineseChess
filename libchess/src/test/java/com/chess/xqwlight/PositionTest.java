package com.chess.xqwlight;

import com.chess.data.StartUpFen;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class PositionTest {
    private Position pos;

    @Before
    public void init() {
        pos = new Position();
    }

    private boolean fromFen_toFen_Assert(int i) {
        String fen = StartUpFen.get(i);
        pos.fromFen(fen);
        return fen.startsWith(pos.toFen());
    }

    @Test
    public void fromFen_toFen_handicap_none() {
        assertTrue(fromFen_toFen_Assert(0));
    }

    @Test
    public void fromFen_toFen_handicap_left_knight() {
        assertTrue(fromFen_toFen_Assert(1));
    }


    @Test
    public void fromFen_toFen_handicap_both_knight() {
        assertTrue(fromFen_toFen_Assert(2));
    }


    @Test
    public void fromFen_toFen_handicap_nine_pieces() {
        assertTrue(fromFen_toFen_Assert(3));
    }

    @Test
    public void fromFen_handicap_none() {
        pos.fromFen(StartUpFen.get(0));
    }

    @Test
    public void fromFen_handicap_left_knight() {
        pos.fromFen(StartUpFen.get(1));
    }


    @Test
    public void fromFen_handicap_both_knight() {
        pos.fromFen(StartUpFen.get(2));
    }


    @Test
    public void fromFen_handicap_nine_pieces() {
        pos.fromFen(StartUpFen.get(3));
    }
}