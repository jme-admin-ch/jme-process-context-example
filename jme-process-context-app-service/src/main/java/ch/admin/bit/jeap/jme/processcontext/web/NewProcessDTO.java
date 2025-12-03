package ch.admin.bit.jeap.jme.processcontext.web;

import lombok.AllArgsConstructor;
import lombok.Value;

import jakarta.validation.constraints.NotNull;

@Value
@AllArgsConstructor
public class NewProcessDTO {
    @NotNull
    private String raceCarNumber;
}
