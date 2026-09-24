"""
Testes unitários automatizados para o motor de curadoria e cálculo AQSI.
"""
import pytest
from curator_service import (
    CoursePackage, CourseModule, Lesson, Exercise, ExercisePayload,
    SrsMetadata, calculate_aqsi_score
)

def test_aqsi_calculation():
    pkg = CoursePackage(
        course_id="esperanto_ptbr",
        title="Esperanto Prático para Brasileiros",
        description="Curso comunitário federado de Esperanto",
        source_language="pt-BR",
        target_language="eo",
        cefr_level="A1",
        version="1.0.0",
        modules=[
            CourseModule(
                module_id="mod_intro",
                module_title="Fundamentos do Esperanto",
                lessons=[
                    Lesson(
                        lesson_id="les_saluton",
                        lesson_title="Cumprimentos e Saudação",
                        exercises=[
                            Exercise(
                                exercise_id="ex_1",
                                type="MULTIPLE_CHOICE",
                                prompt="Como se diz 'Olá' em Esperanto?",
                                payload=ExercisePayload(
                                    options=["Saluton", "Bonan tagon", "Dankon"],
                                    correct_option_index=0,
                                    explanation="Saluton significa Olá ou Oi em esperanto.",
                                    audio_text="Saluton"
                                ),
                                srs_metadata=SrsMetadata(keywords=["saudação"], difficulty_rating=1.5)
                            ),
                            Exercise(
                                exercise_id="ex_2",
                                type="WORD_ORDERING",
                                prompt="Ordene as palavras para: 'Bom dia'",
                                payload=ExercisePayload(
                                    target_sentence="Bonan tagon",
                                    tokens=["Bonan", "tagon"],
                                    distractors=["nokton"],
                                    explanation="Bonan tagon é a forma padrão com acusativo no cumprimento.",
                                    audio_text="Bonan tagon"
                                )
                            )
                        ]
                    )
                ]
            )
        ]
    )

    score = calculate_aqsi_score(pkg)
    assert score.overall_aqsi >= 75.0
    assert score.status in ["APPROVED", "NEEDS_REVISION"]
    assert score.cefr_alignment_score > 80.0

if __name__ == "__main__":
    test_aqsi_calculation()
    print("AQSI calculation test passed successfully!")
