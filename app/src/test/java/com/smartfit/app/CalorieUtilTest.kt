package com.smartfit.app

import com.smartfit.app.util.CalorieUtil
import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for core business logic.
 */
class CalorieUtilTest {

    // ── Member 1: Calorie Estimation Tests ───────────────────────────────────

    @Test
    fun `estimate calories for running returns positive value`() {
        val calories = CalorieUtil.estimate("running", 30, 70f)
        assertTrue("Calories should be positive", calories > 0)
    }

    @Test
    fun `estimate calories for running 30min 70kg is exactly 343`() {
        // MET=9.8, weight=70, duration=0.5h -> 9.8 * 70 * 0.5 = 343
        val calories = CalorieUtil.estimate("running", 30, 70f)
        assertEquals(343, calories)
    }

    @Test
    fun `estimate calories for yoga is less than running`() {
        val yogaCal    = CalorieUtil.estimate("yoga",    30, 70f)
        val runningCal = CalorieUtil.estimate("running", 30, 70f)
        assertTrue("Yoga should burn fewer calories than running", yogaCal < runningCal)
    }

    @Test
    fun `estimate calories scales with duration`() {
        // Walking MET = 3.5. 70kg.
        // 30 min: 3.5 * 70 * 0.5 = 122.5 -> 122
        // 60 min: 3.5 * 70 * 1.0 = 245.0 -> 245
        val cal30 = CalorieUtil.estimate("walking", 30, 70f)
        val cal60 = CalorieUtil.estimate("walking", 60, 70f)
        // Since we convert to Int inside estimate(), 122 * 2 != 245.
        // We check that it's approximately double or just check the specific values.
        assertEquals(122, cal30)
        assertEquals(245, cal60)
    }

    // ── Member 2: Step Progress & Estimation Tests ───────────────────────────

    @Test
    fun `stepProgress returns 1 when goal is met`() {
        val progress = CalorieUtil.stepProgress(10_000, 10_000)
        assertEquals(1f, progress, 0.001f)
    }

    @Test
    fun `stepProgress is capped at 1 even when steps exceed goal`() {
        val progress = CalorieUtil.stepProgress(15_000, 10_000)
        assertEquals(1f, progress, 0.001f)
    }

    @Test
    fun `estimateFromSteps calculates correct calories for 10k steps`() {
        // 10000 steps * (70kg * 0.00065) = 455 kcal
        val calories = CalorieUtil.estimateFromSteps(10000, 70f)
        assertEquals(455, calories)
    }

    // ── Member 3: BMR Tests ───────────────────────────────────────────────────

    @Test
    fun `bmr for male 70kg 175cm 25yr is exactly 1673`() {
        // (10*70) + (6.25*175) - (5*25) + 5 = 700 + 1093.75 - 125 + 5 = 1673.75 -> 1673
        val bmr = CalorieUtil.bmr(70f, 175f, 25, "male")
        assertEquals(1673, bmr)
    }

    @Test
    fun `bmr for female 70kg 175cm 25yr is exactly 1507`() {
        // (10*70) + (6.25*175) - (5*25) - 161 = 700 + 1093.75 - 125 - 161 = 1507.75 -> 1507
        val bmr = CalorieUtil.bmr(70f, 175f, 25, "female")
        assertEquals(1507, bmr)
    }

    @Test
    fun `bmr increases with higher weight`() {
        val bmr70  = CalorieUtil.bmr(70f,  175f, 25)
        val bmr90  = CalorieUtil.bmr(90f, 175f, 25)
        assertTrue("Heavier person has higher BMR", bmr90 > bmr70)
    }

    // ── Member 4: Edge Cases & Comparison ────────────────────────────────────

    @Test
    fun `estimate returns default MET for unknown activity`() {
        val calories = CalorieUtil.estimate("unknown_activity", 60, 70f)
        // Default MET=5.0 -> 5.0 * 70 * 1.0 = 350
        assertEquals(350, calories)
    }

    @Test
    fun `estimate HIIT burns more than walking for same duration`() {
        val hiit    = CalorieUtil.estimate("hiit",    45, 70f)
        val walking = CalorieUtil.estimate("walking", 45, 70f)
        assertTrue("HIIT should burn more than walking", hiit > walking)
    }
}
