package com.example.skilledworkershireapp

import com.example.skilledworkershireapp.utils.Validator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorTest {

    @Test
    fun testValidNames() {
        assertTrue(Validator.isValidName("John Doe"))
        assertTrue(Validator.isValidName("Alice"))
        assertFalse(Validator.isValidName("John123"))
        assertFalse(Validator.isValidName("John@Doe"))
        assertFalse(Validator.isValidName(""))
    }

    @Test
    fun testValidPhone() {
        assertTrue(Validator.isValidPhone("+919876543210"))
        assertTrue(Validator.isValidPhone("+91 9876543210"))
        assertTrue(Validator.isValidPhone("9876543210"))
        assertFalse(Validator.isValidPhone("12345"))
        assertFalse(Validator.isValidPhone("+9198765432100"))
        assertFalse(Validator.isValidPhone("abcdefghij"))
    }

    @Test
    fun testFormatPhone() {
        assertEquals("+919876543210", Validator.formatPhone("9876543210"))
        assertEquals("+919876543210", Validator.formatPhone("+91 98765 43210"))
    }

    @Test
    fun testValidLocation() {
        assertTrue(Validator.isValidLocation("Downtown, Sector 5"))
        assertTrue(Validator.isValidLocation("Bangalore"))
        assertFalse(Validator.isValidLocation("Bangalore@#$"))
        assertFalse(Validator.isValidLocation(""))
    }

    @Test
    fun testValidExperience() {
        assertTrue(Validator.isValidExperience("3 years"))
        assertTrue(Validator.isValidExperience("6 months"))
        assertTrue(Validator.isValidExperience("Fresher"))
        assertFalse(Validator.isValidExperience("3 years!"))
        assertFalse(Validator.isValidExperience(""))
    }
}
