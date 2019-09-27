package com.chess.xqwlight;

import com.chess.data.StartUpFen;

import org.junit.Assert;
import org.junit.Before;

import static org.junit.Assert.*;

public class PositionTest {
    private Position pos;

    @Before
    public void init() {
        pos = new Position();
    }

    private boolean fromFen_toFen_Assert(int i) {
        String fen = StartUpFen.get(0);
        pos.fromFen(fen);
        return fen.startsWith(pos.toFen());
    }

    @org.junit.Test
    public void fromFen_toFen_handicap_none() {
        Assert.assertTrue(fromFen_toFen_Assert(0));
    }

    @org.junit.Test
    public void fromFen_toFen_handicap_left_knight() {
        Assert.assertTrue(fromFen_toFen_Assert(1));
    }


    @org.junit.Test
    public void fromFen_toFen_handicap_both_knight() {
        Assert.assertTrue(fromFen_toFen_Assert(2));
    }


    @org.junit.Test
    public void fromFen_toFen_handicap_nine_pieces() {
        Assert.assertTrue(fromFen_toFen_Assert(3));
    }

    @org.junit.Test
    public void fromFen_handicap_none() {
        pos.fromFen(StartUpFen.get(0));
    }

    @org.junit.Test
    public void fromFen_handicap_left_knight() {
        pos.fromFen(StartUpFen.get(1));
    }


    @org.junit.Test
    public void fromFen_handicap_both_knight() {
        pos.fromFen(StartUpFen.get(2));
    }


    @org.junit.Test
    public void fromFen_handicap_nine_pieces() {
        pos.fromFen(StartUpFen.get(3));
    }
}