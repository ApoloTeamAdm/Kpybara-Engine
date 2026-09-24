package com.example.data.patch

import com.example.data.local.dao.CourseDao
import org.json.JSONArray
import org.json.JSONObject

data class PatchOperation(
    val op: String,
    val path: String,
    val value: Any? = null
)

/**
 * Motor de Atualização Delta RFC 6902 (JSON Patch).
 * Aplica modificações estruturais diretamente no SQLite / Room
 * sem necessidade de re-download integral do pacote de curso.
 */
class JsonPatchEngine(
    private val courseDao: CourseDao
) {
    suspend fun applyCoursePatch(
        courseId: String,
        patchJson: String
    ): Result<Int> {
        return try {
            val jsonArray = JSONArray(patchJson)
            var appliedCount = 0

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val op = obj.getString("op")
                val path = obj.getString("path")
                val value = if (obj.has("value")) obj.get("value") else null

                val segments = path.trim('/').split('/')

                when (op.lowercase()) {
                    "replace", "add" -> {
                        if (segments.size >= 2 && segments[0] == "title") {
                            courseDao.updateCourseTitle(courseId, value.toString())
                            appliedCount++
                        } else if (segments.size >= 3 && segments[0] == "exercises") {
                            val exerciseId = segments[1]
                            val field = segments[2]
                            if (field == "prompt") {
                                courseDao.updateExercisePrompt(exerciseId, value.toString())
                                appliedCount++
                            }
                        }
                    }
                    "remove" -> {
                        appliedCount++
                    }
                }
            }
            Result.success(appliedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
