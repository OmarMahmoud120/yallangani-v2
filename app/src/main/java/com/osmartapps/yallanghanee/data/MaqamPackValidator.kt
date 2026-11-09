package com.osmartapps.yallanghanee.data

import com.osmartapps.yallanghanee.model.MaqamPack

object MaqamPackValidator {

    fun validate(pack: MaqamPack, audioExists: (String) -> Boolean): List<String> {
        val errors = mutableListOf<String>()
        if (pack.version <= 0) errors += "version must be positive"

        val maqamIds = mutableSetOf<String>()
        pack.maqams.forEach { maqam ->
            if (maqam.id.isBlank()) errors += "blank maqam id"
            if (!maqamIds.add(maqam.id)) errors += "duplicate maqam id: ${maqam.id}"
            if (maqam.nameAr.isBlank()) errors += "blank maqam name: ${maqam.id}"
            if (!HEX_COLOR.matches(maqam.color)) errors += "invalid color for ${maqam.id}"
            if (maqam.signatureAr.isBlank()) errors += "blank signature for ${maqam.id}"
            if (maqam.status != "approved") errors += "maqam not approved: ${maqam.id}"
            if (!audioExists(maqam.audio)) errors += "missing audio: ${maqam.audio}"
        }

        val onboarding = pack.onboarding
        if (onboarding.steps.size != ONBOARDING_STEPS) {
            errors += "onboarding needs $ONBOARDING_STEPS steps: ${onboarding.steps.size}"
        }
        val stepIds = mutableSetOf<String>()
        onboarding.steps.forEach { step ->
            if (!stepIds.add(step.id)) errors += "duplicate onboarding step: ${step.id}"
            if (step.promptAr.isBlank()) errors += "blank onboarding prompt: ${step.id}"
            if (step.options.size < 2) errors += "onboarding step needs two options: ${step.id}"
            if (step.options.distinctBy { it.id }.size != step.options.size) {
                errors += "duplicate option in onboarding step: ${step.id}"
            }
            if (step.answer !in step.options.map { it.id }) {
                errors += "answer outside options in onboarding step: ${step.id}"
            }
            if (step.explanationAr.isBlank()) errors += "blank onboarding explanation: ${step.id}"
            if (!audioExists(step.audio)) errors += "missing audio: ${step.audio}"
        }
        val firstWin = onboarding.firstWin
        if (firstWin.promptAr.isBlank()) errors += "blank first win prompt"
        if (firstWin.maqamIds.size < 2) errors += "first win needs two options"
        if (firstWin.maqamIds.distinct().size != firstWin.maqamIds.size) {
            errors += "duplicate option in first win"
        }
        firstWin.maqamIds.forEach { id ->
            if (id !in maqamIds) errors += "unknown maqam in first win: $id"
        }
        if (firstWin.answer !in firstWin.maqamIds) errors += "answer outside options in first win"
        if (firstWin.explanationAr.isBlank()) errors += "blank first win explanation"
        if (!audioExists(firstWin.audio)) errors += "missing audio: ${firstWin.audio}"

        val lessonIds = mutableSetOf<String>()
        pack.lessons.forEach { lesson ->
            if (!lessonIds.add(lesson.id)) errors += "duplicate lesson id: ${lesson.id}"
            if (lesson.titleAr.isBlank()) errors += "blank lesson title: ${lesson.id}"
            if (lesson.maqamIds.size < 2) errors += "lesson needs two options: ${lesson.id}"
            if (lesson.maqamIds.distinct().size != lesson.maqamIds.size) {
                errors += "duplicate option in lesson: ${lesson.id}"
            }
            lesson.maqamIds.forEach { id ->
                if (id !in maqamIds) errors += "unknown maqam in ${lesson.id}: $id"
            }
            if (lesson.items.isEmpty()) errors += "empty lesson: ${lesson.id}"
            lesson.items.forEach { item ->
                if (item.answer !in lesson.maqamIds) {
                    errors += "answer outside options in ${lesson.id}/${item.id}"
                }
                item.audio?.let { audio ->
                    if (!audioExists(audio)) errors += "missing audio: $audio"
                }
            }
        }
        return errors
    }

    private val HEX_COLOR = Regex("^#[0-9A-Fa-f]{6}$")
    private const val ONBOARDING_STEPS = 5
}
