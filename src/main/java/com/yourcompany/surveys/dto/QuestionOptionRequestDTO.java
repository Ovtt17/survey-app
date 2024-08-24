package com.yourcompany.surveys.dto;

import jakarta.validation.constraints.NotBlank;

public record QuestionOptionRequestDTO(
        Long id,
        @NotBlank(message = "El contenido de la opción es obligatorio")
        String text,
        Boolean isCorrect
) {

}
