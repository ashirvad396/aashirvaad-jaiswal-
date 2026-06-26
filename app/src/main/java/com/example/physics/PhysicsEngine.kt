package com.example.physics

import kotlin.math.*

data class ProjectileState(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val isFlying: Boolean = false,
    val pathPoints: List<Pair<Float, Float>> = emptyList()
)

data class SolverResult(
    val solutions: List<Float>, // Angles in degrees
    val isReachable: Boolean,
    val reason: String = ""
)

object PhysicsEngine {

    /**
     * Solves the projectile motion equation for angle θ given v0, distance, height, and gravity.
     * Returns a list of matching launch angles in degrees.
     */
    fun solveAnglesForTarget(v0: Float, d: Float, h: Float, g: Float): SolverResult {
        if (v0 <= 0f) {
            return SolverResult(emptyList(), false, "Launch velocity must be greater than zero.")
        }
        
        val a = (g * d * d) / (2 * v0 * v0)
        val b = -d
        val c = h + a
        
        val discriminant = b * b - 4 * a * c
        if (discriminant < 0) {
            // Target is out of reach for this velocity
            val minVRequired = sqrt(g * (h + sqrt(d * d + h * h)))
            return SolverResult(
                solutions = emptyList(),
                isReachable = false,
                reason = "Target out of range! Minimum velocity required is ${String.format("%.1f", minVRequired)} m/s."
            )
        }
        
        val t1 = (-b + sqrt(discriminant)) / (2 * a)
        val t2 = (-b - sqrt(discriminant)) / (2 * a)
        
        val angle1Rad = atan(t1)
        val angle2Rad = atan(t2)
        
        val angle1Deg = Math.toDegrees(angle1Rad.toDouble()).toFloat()
        val angle2Deg = Math.toDegrees(angle2Rad.toDouble()).toFloat()
        
        val solutions = mutableListOf<Float>()
        if (angle1Deg in 0f..90f) {
            solutions.add(angle1Deg)
        }
        if (angle2Deg in 0f..90f && abs(angle1Deg - angle2Deg) > 0.1f) {
            solutions.add(angle2Deg)
        }
        
        solutions.sort()
        
        return if (solutions.isEmpty()) {
            SolverResult(emptyList(), false, "Calculated launch angles fall outside physical 0° to 90° boundaries.")
        } else {
            SolverResult(solutions, true, "Target is reachable!")
        }
    }

    /**
     * Calculates a list of coordinates representing the theoretical (no-wind) trajectory
     * to draw a dotted path guide.
     */
    fun generateTheoreticalTrajectory(
        v0: Float,
        angleDeg: Float,
        g: Float,
        windX: Float,
        stepTime: Float = 0.05f,
        maxDuration: Float = 10f
    ): List<Pair<Float, Float>> {
        val points = mutableListOf<Pair<Float, Float>>()
        val angleRad = Math.toRadians(angleDeg.toDouble()).toFloat()
        
        var t = 0f
        var x = 0f
        var y = 0f
        
        // Initial velocity components
        val vx0 = v0 * cos(angleRad)
        val vy0 = v0 * sin(angleRad)
        
        points.add(Pair(x, y))
        
        while (t < maxDuration && y >= -100f && x <= 250f) {
            t += stepTime
            // Physics equations including uniform wind acceleration
            x = vx0 * t + 0.5f * windX * t * t
            y = vy0 * t - 0.5f * g * t * t
            points.add(Pair(x, y))
            if (y < -40f) break // Stop generating beneath canyon floor
        }
        
        return points
    }
}
