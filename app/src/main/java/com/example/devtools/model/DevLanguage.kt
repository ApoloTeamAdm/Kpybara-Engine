package com.example.devtools.model

enum class DevLanguage(
    val id: String,
    val displayName: String,
    val fileExtension: String,
    val runtimeName: String,
    val defaultSnippet: String,
    val description: String
) {
    LUA(
        id = "lua",
        displayName = "Lua 5.4",
        fileExtension = ".lua",
        runtimeName = "Lua 5.4 Embedded Sandbox",
        description = "Linguagem nativa padrão da plataforma. Ultraleve, consumo de RAM < 1MB e isolada em worker coroutine.",
        defaultSnippet = """
-- Evaluator Nativo em Lua 5.4
-- Parâmetros fornecidos pelo CourseEngine:
--   prompt: string
--   student_answer: string
--   target_sentence: string

function evaluate(prompt, student_answer, target_sentence)
    local clean_student = string.lower(string.gsub(student_answer, "^%s*(.-)%s*$", "%1"))
    local clean_target = string.lower(string.gsub(target_sentence, "^%s*(.-)%s*$", "%1"))
    
    if clean_student == clean_target then
        return {
            is_correct = true,
            score = 1.0,
            feedback = "Resposta perfeita!",
            next_lesson = "next"
        }
    else
        return {
            is_correct = false,
            score = 0.0,
            feedback = "Atenção à terminação acusativa '-n'!",
            next_lesson = "reinforcement_accusative"
        }
    end
end
""".trimIndent()
    ),

    LUAU(
        id = "luau",
        displayName = "Luau",
        fileExtension = ".luau",
        runtimeName = "Luau Typing & VM v0.620",
        description = "Extensão moderna de Lua com tipagem gradual estrita, verificação estática de tipos e vetores acelerados.",
        defaultSnippet = """
--!strict
-- Evaluator em Luau (Typed Lua)

type EvaluationResult = {
    is_correct: boolean,
    score: number,
    feedback: string,
    next_lesson: string
}

function evaluate(prompt: string, student_answer: string, target_sentence: string): EvaluationResult
    local normalized_answer: string = string.lower(student_answer)
    local normalized_target: string = string.lower(target_sentence)
    
    local match: boolean = (normalized_answer == normalized_target)
    
    return {
        is_correct = match,
        score = if match then 1.0 else 0.0,
        feedback = if match then "Correto via Luau VM!" else "Revise a ordem dos termos.",
        next_lesson = if match then "next" else "reinforcement_luau"
    }
end
""".trimIndent()
    ),

    JAVASCRIPT(
        id = "js",
        displayName = "JavaScript",
        fileExtension = ".js",
        runtimeName = "QuickJS Ultra-Light VM (ES2023)",
        description = "Extensão QuickJS para autores que preferem sintaxe ECMAScript moderna com Regex e manipulação de arrays.",
        defaultSnippet = """
// Evaluator em JavaScript (QuickJS Runtime)
function evaluate(prompt, studentAnswer, targetSentence) {
    const normalize = (str) => str.trim().toLowerCase().replace(/[.,!?:;]/g, '');
    const isCorrect = normalize(studentAnswer) === normalize(targetSentence);

    return {
        is_correct: isCorrect,
        score: isCorrect ? 1.0 : 0.0,
        feedback: isCorrect ? "Excelente! Sintaxe JS validada." : "Tente novamente observando a pontuação.",
        next_lesson: isCorrect ? "next" : "reinforcement_js"
    };
}
""".trimIndent()
    ),

    PYTHON(
        id = "python",
        displayName = "Python 3",
        fileExtension = ".py",
        runtimeName = "MicroPython / Pyodide Core 3.11",
        description = "Extensão Pythonic com suporte a formatação limpa, f-strings, list comprehensions e avaliação de texto.",
        defaultSnippet = """
# Evaluator em Python 3
def evaluate(prompt: str, student_answer: str, target_sentence: str) -> dict:
    cleaned_student = student_answer.strip().lower()
    cleaned_target = target_sentence.strip().lower()
    
    is_correct = (cleaned_student == cleaned_target)
    
    return {
        "is_correct": is_correct,
        "score": 1.0 if is_correct else 0.0,
        "feedback": "Resposta exata em Python!" if is_correct else "Revise o vocabulário básico.",
        "next_lesson": "next" if is_correct else "reinforcement_python"
    }
""".trimIndent()
    ),

    JAVA(
        id = "java",
        displayName = "Java",
        fileExtension = ".java",
        runtimeName = "Embedded JVM Java 21 Sandbox",
        description = "Extensão corporativa orientada a objetos para cursos estruturados com tipagem estática rigorosa.",
        defaultSnippet = """
// Evaluator em Java
public class LessonEvaluator {
    public static EvaluationResult evaluate(String prompt, String studentAnswer, String targetSentence) {
        String cleanStudent = studentAnswer.trim().toLowerCase();
        String cleanTarget = targetSentence.trim().toLowerCase();
        
        boolean isCorrect = cleanStudent.equals(cleanTarget);
        return new EvaluationResult(
            isCorrect,
            isCorrect ? 1.0 : 0.0,
            isCorrect ? "Aprovado via Java JVM!" : "Divergência de caracteres encontrada.",
            isCorrect ? "next" : "reinforcement_java"
        );
    }
}
""".trimIndent()
    ),

    C(
        id = "c",
        displayName = "C (C99)",
        fileExtension = ".c",
        runtimeName = "Wasm-Clang C Micro-Runner",
        description = "Extensão em C nativo com execução de tempo constante (O(N)), ideal para algoritmos fonéticos e de alta performance.",
        defaultSnippet = """
// Evaluator em C (C99 Wasm Sandbox)
#include <stdio.h>
#include <string.h>
#include <ctype.h>

typedef struct {
    int is_correct;
    double score;
    char feedback[128];
    char next_lesson[32];
} EvaluationResult;

EvaluationResult evaluate(const char* prompt, const char* student_answer, const char* target_sentence) {
    EvaluationResult res;
    if (strcmp(student_answer, target_sentence) == 0) {
        res.is_correct = 1;
        res.score = 1.0;
        strcpy(res.feedback, "Perfeito! Zero overhead em C.");
        strcpy(res.next_lesson, "next");
    } else {
        res.is_correct = 0;
        res.score = 0.0;
        strcpy(res.feedback, "Incorreto. Verifique a grafia exata.");
        strcpy(res.next_lesson, "reinforcement_c");
    }
    return res;
}
""".trimIndent()
    ),

    CPP(
        id = "cpp",
        displayName = "C++ (C++20)",
        fileExtension = ".cpp",
        runtimeName = "LLVM / Clang C++20 Sandbox",
        description = "Extensão C++ moderna com suporte a std::string_view, lambdas e compilação em WebAssembly local.",
        defaultSnippet = """
// Evaluator em C++20
#include <string>
#include <string_view>
#include <algorithm>

struct EvaluationResult {
    bool is_correct;
    double score;
    std::string feedback;
    std::string next_lesson;
};

EvaluationResult evaluate(std::string_view prompt, std::string_view student, std::string_view target) {
    bool match = (student == target);
    return EvaluationResult{
        .is_correct = match,
        .score = match ? 1.0 : 0.0,
        .feedback = match ? "Excelente! C++20 executado em < 5ms." : "Erro sintático.",
        .next_lesson = match ? "next" : "reinforcement_cpp"
    };
}
""".trimIndent()
    ),

    CSHARP(
        id = "csharp",
        displayName = "C# (.NET 8)",
        fileExtension = ".cs",
        runtimeName = "Roslyn Micro-Scripting Engine",
        description = "Extensão em C# com pattern matching, records imutáveis e sintaxe moderna do ecossistema .NET.",
        defaultSnippet = """
// Evaluator em C# (.NET Roslyn)
public record EvaluationResult(bool IsCorrect, double Score, string Feedback, string NextLesson);

public static class ExerciseEvaluator
{
    public static EvaluationResult Evaluate(string prompt, string studentAnswer, string targetSentence)
    {
        var isCorrect = string.Equals(studentAnswer.Trim(), targetSentence.Trim(), StringComparison.OrdinalIgnoreCase);
        
        return new EvaluationResult(
            IsCorrect: isCorrect,
            Score: isCorrect ? 1.0 : 0.0,
            Feedback: isCorrect ? "Validado com sucesso em C#!" : "Resposta não confere com o gabarito.",
            NextLesson: isCorrect ? "next" : "reinforcement_csharp"
        );
    }
}
""".trimIndent()
    )
}

data class LanguageExtension(
    val language: DevLanguage,
    val version: String,
    val isEnabled: Boolean = true,
    val memoryFootprintKb: Int,
    val author: String,
    val supportLevel: String // NATIVO, OFICIAL, COMUNITÁRIO
)

data class DevScriptExecutionResult(
    val isSuccess: Boolean,
    val isCorrect: Boolean,
    val score: Double,
    val feedback: String,
    val nextLesson: String,
    val executionTimeMs: Long,
    val stdoutLogs: List<String>,
    val memoryUsedKb: Int,
    val compiledLanguage: DevLanguage,
    val errorMessage: String? = null
)
