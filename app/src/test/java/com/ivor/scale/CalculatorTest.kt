package com.ivor.scale

import com.ivor.scale.domain.Calculator
import com.ivor.scale.domain.ConvertResult
import com.ivor.scale.domain.MassUnit
import com.ivor.scale.domain.PieceResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Unit tests for the Piece and Quantity Price calculators. */
class CalculatorTest {

    // ---- Piece calculator -------------------------------------------------

    @Test
    fun piece_scalesUpFromKnownCount() {
        // 3 bananas = ₹20, how much for 5?
        val r = Calculator.pieceResult("3", "20", "5") as PieceResult.Success
        assertEquals("₹33.33", r.totalPrice.display)
        assertEquals("₹6.67", r.pricePerPiece.display)
    }

    @Test
    fun piece_singleUnitPrice() {
        // 4 oranges = ₹50, price of 1 orange.
        val r = Calculator.pieceResult("4", "50", "1") as PieceResult.Success
        assertEquals("₹12.5", r.totalPrice.display)
        assertEquals("₹12.5", r.pricePerPiece.display)
    }

    @Test
    fun piece_eggsExample() {
        // 12 eggs = ₹72, how much for 30?
        val r = Calculator.pieceResult("12", "72", "30") as PieceResult.Success
        assertEquals("₹180", r.totalPrice.display)
        assertEquals("₹6", r.pricePerPiece.display)
        assertEquals("180", r.totalPrice.plain)
    }

    @Test
    fun piece_blankInputIsEmpty() {
        assertTrue(Calculator.pieceResult("", "20", "5") is PieceResult.Empty)
        assertTrue(Calculator.pieceResult("3", "", "5") is PieceResult.Empty)
        assertTrue(Calculator.pieceResult("3", "20", "") is PieceResult.Empty)
    }

    @Test
    fun piece_zeroKnownQuantityIsError() {
        assertTrue(Calculator.pieceResult("0", "20", "5") is PieceResult.Error)
    }

    // ---- Quantity Price converter -----------------------------------------

    @Test
    fun convert_kilograms() {
        // 2.5 kg = ₹100.
        val r = Calculator.convertResult("2.5", MassUnit.KG, "100") as ConvertResult.Success
        assertEquals("₹40", r.perKg.display)
        assertEquals("₹4", r.per100g.display)
        assertEquals("₹0.04", r.perGram.display)
    }

    @Test
    fun convert_grams() {
        // 250 g = ₹60.
        val r = Calculator.convertResult("250", MassUnit.G, "60") as ConvertResult.Success
        assertEquals("₹240", r.perKg.display)
        assertEquals("₹24", r.per100g.display)
        assertEquals("₹0.24", r.perGram.display)
    }

    @Test
    fun convert_fishExample() {
        // 750 g = ₹180.
        val r = Calculator.convertResult("750", MassUnit.G, "180") as ConvertResult.Success
        assertEquals("₹240", r.perKg.display)
        assertEquals("₹24", r.per100g.display)
    }

    @Test
    fun convert_blankInputIsEmpty() {
        assertTrue(Calculator.convertResult("", MassUnit.KG, "100") is ConvertResult.Empty)
        assertTrue(Calculator.convertResult("2.5", MassUnit.KG, "") is ConvertResult.Empty)
    }

    @Test
    fun convert_zeroQuantityIsError() {
        assertTrue(Calculator.convertResult("0", MassUnit.KG, "100") is ConvertResult.Error)
    }
}
